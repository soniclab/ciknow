package ciknow.graph.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.graph.converter.NumberEdgeValueImpl;
import ciknow.graph.converter.SparseGraphConverter;
import ciknow.util.Constants;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.importance.NodeRanking;
import edu.uci.ics.jung.algorithms.importance.PageRank;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.transformation.DirectionTransformer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.statistics.DegreeDistributions;
import edu.uci.ics.jung.statistics.GraphStatistics;
import edu.uci.ics.jung.statistics.Histogram;

@SuppressWarnings({ "unchecked", "unused" })
public class NetworkMetrics {	
	private static Log logger = LogFactory.getLog(NetworkMetrics.class);
	
	private String networkName = "unknown";
	private Boolean directed = true;
	
	private Integer totalNodes = 0;
	private Integer totalEdges = 0;	
	private Double networkDensity = Double.NaN;
	
	private Double networkInDegree = Double.NaN;
	private Double networkOutDegree = Double.NaN;
	private Double networkInCloseness = Double.NaN;
	private Double networkOutCloseness = Double.NaN;
	private Double networkBetweenness = Double.NaN;
	
	private Double networkClusteringCoefficient = Double.NaN;
	private Double diameter = Double.NaN;
	private Double characteristicPathLength = Double.NaN;
	
	// y = a + bx
	private Double inAlpha = Double.NaN;
	private Double inBeta = Double.NaN;
	private Double outAlpha = Double.NaN;
	private Double outBeta = Double.NaN;
	// list index = degree, list value = # of nodes
	private List<Double> inCounts;
	private List<Double> outCounts;
	
	
	private List<Clique> cliques;
	private List<ConnectedComponent> connectedComponents;
	
	// internal user only
	private Map<Long, IndividualMetric> individualMetricMap;
	
	public NetworkMetrics(){
		networkName = "empty";
		individualMetricMap = new TreeMap<Long, IndividualMetric>();
		cliques = new ArrayList<Clique>();
		connectedComponents = new ArrayList<ConnectedComponent>();
		inCounts = new ArrayList<Double>();
		outCounts = new ArrayList<Double>();
	}	
	
	///////////////// GETTER/SETTERS ///////////////////////////////
	////////////////////////////////////////////////////////////////
	
	public String getNetworkName() {
		return networkName;
	}

	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	public Boolean getDirected() {
		return directed;
	}

	public void setDirected(Boolean directed) {
		this.directed = directed;
	}

	public Integer getTotalNodes() {
		return totalNodes;
	}

	public void setTotalNodes(Integer totalNodes) {
		this.totalNodes = totalNodes;
	}

	public Integer getTotalEdges() {
		return totalEdges;
	}

	public void setTotalEdges(Integer totalEdges) {
		this.totalEdges = totalEdges;
	}

	public Double getNetworkDensity() {
		return networkDensity;
	}

	public void setNetworkDensity(Double networkDensity) {
		this.networkDensity = networkDensity;
	}

	public Double getNetworkInDegree() {
		return networkInDegree;
	}

	public void setNetworkInDegree(Double networkInDegree) {
		this.networkInDegree = networkInDegree;
	}

	public Double getNetworkOutDegree() {
		return networkOutDegree;
	}

	public void setNetworkOutDegree(Double networkOutDegree) {
		this.networkOutDegree = networkOutDegree;
	}

	public Double getNetworkInCloseness() {
		return networkInCloseness;
	}

	public void setNetworkInCloseness(Double networkInCloseness) {
		this.networkInCloseness = networkInCloseness;
	}

	public Double getNetworkOutCloseness() {
		return networkOutCloseness;
	}

	public void setNetworkOutCloseness(Double networkOutCloseness) {
		this.networkOutCloseness = networkOutCloseness;
	}

	public Double getNetworkBetweenness() {
		return networkBetweenness;
	}

	public void setNetworkBetweenness(Double networkBetweenness) {
		this.networkBetweenness = networkBetweenness;
	}

	public Double getNetworkClusteringCoefficient() {
		return networkClusteringCoefficient;
	}

	public void setNetworkClusteringCoefficient(Double networkClusteringCoefficient) {
		this.networkClusteringCoefficient = networkClusteringCoefficient;
	}

	public Double getDiameter() {
		return diameter;
	}

	public void setDiameter(Double diameter) {
		this.diameter = diameter;
	}

	public Double getCharacteristicPathLength() {
		return characteristicPathLength;
	}

	public void setCharacteristicPathLength(Double characteristicPathLength) {
		this.characteristicPathLength = characteristicPathLength;
	}

	public Double getInAlpha() {
		return inAlpha;
	}

	public void setInAlpha(Double inAlpha) {
		this.inAlpha = inAlpha;
	}

	public Double getInBeta() {
		return inBeta;
	}

	public void setInBeta(Double inBeta) {
		this.inBeta = inBeta;
	}

	public Double getOutAlpha() {
		return outAlpha;
	}

	public void setOutAlpha(Double outAlpha) {
		this.outAlpha = outAlpha;
	}

	public Double getOutBeta() {
		return outBeta;
	}

	public void setOutBeta(Double outBeta) {
		this.outBeta = outBeta;
	}

	public List<Double> getInCounts() {
		return inCounts;
	}

	public void setInCounts(List<Double> inCounts) {
		this.inCounts = inCounts;
	}

	public List<Double> getOutCounts() {
		return outCounts;
	}

	public void setOutCounts(List<Double> outCounts) {
		this.outCounts = outCounts;
	}

	public List<Clique> getCliques() {
		return cliques;
	}

	public void setCliques(List<Clique> cliques) {
		this.cliques = cliques;
	}

	public List<ConnectedComponent> getConnectedComponents() {
		return connectedComponents;
	}

	public void setConnectedComponents(List<ConnectedComponent> connectedComponents) {
		this.connectedComponents = connectedComponents;
	}

	public List<IndividualMetric> getIndividualMetrics() {
		return new ArrayList<IndividualMetric>(individualMetricMap.values());
	}

	public void setIndividualMetrics(List<IndividualMetric> individualMetrics) {
		for (IndividualMetric im : individualMetrics){
			individualMetricMap.put(im.nodeId, im);
		}
	}

	public Map<Long, IndividualMetric> getIndividualMetricMap() {
		return individualMetricMap;
	}

	/////////////////////// UTILITY FUNCTIONS ////////////////////////
	//////////////////////////////////////////////////////////////////
	private IndividualMetric getIndividualMetric(Long nodeId){
		IndividualMetric im = (IndividualMetric) individualMetricMap.get(nodeId);
		if (im == null){
			im = new IndividualMetric();
			im.nodeId = nodeId;
			individualMetricMap.put(nodeId, im);
		}
		return im;
	}
	
	public void setInDegree(Long nodeId, Integer value){
		getIndividualMetric(nodeId).inDegree = value;	
	}
	
	public Integer getInDegree(Long nodeId){
		return getIndividualMetric(nodeId).inDegree;
	}
	
	public void setOutDegree(Long nodeId, Integer value){
		getIndividualMetric(nodeId).outDegree = value;	
	}
	
	public Integer getOutDegree(Long nodeId){
		return getIndividualMetric(nodeId).outDegree;
	}
	
	public void setInCloseness(Long nodeId, Double value){
		getIndividualMetric(nodeId).inCloseness = value;	
	}
	
	public Double getInCloseness(Long nodeId){
		return getIndividualMetric(nodeId).inCloseness;
	}
	
	public void setOutCloseness(Long nodeId, Double value){
		getIndividualMetric(nodeId).outCloseness = value;	
	}
	
	public Double getOutCloseness(Long nodeId){
		return getIndividualMetric(nodeId).outCloseness;
	}
	
	public void setBetweenness(Long nodeId, Double value){
		getIndividualMetric(nodeId).betweenness = value;	
	}
	
	public Double getBetweenness(Long nodeId){
		return getIndividualMetric(nodeId).betweenness;	
	}	
	
	public void setPageRank(Long nodeId, Double value){
		getIndividualMetric(nodeId).pageRank = value;	
	}
	
	public Double getPageRank(Long nodeId){
		return getIndividualMetric(nodeId).pageRank;
	}
	
	public void setScanning(Long nodeId, Double value){
		getIndividualMetric(nodeId).scanning = value;	
	}
	
	public Double getScanning(Long nodeId){
		return getIndividualMetric(nodeId).scanning;
	}
	
	public void setClusteringCoefficient(Long nodeId, Double value){
		getIndividualMetric(nodeId).clusteringCoefficient = value;
	}

	public Double getClusteringCoefficient(Long nodeId){
		return getIndividualMetric(nodeId).clusteringCoefficient;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("NetworkMetrics[\n");
		sb.append("networkName: " + networkName + "\n");
		sb.append("directed: " + directed + "\n");
		sb.append("totalNodes: " + totalNodes + "\n");
		sb.append("totalEdges: " + totalEdges + "\n");
		sb.append("density: " + networkDensity + "\n");
		sb.append("inDegree: " + networkInDegree + "\n");
		sb.append("outDegree: " + networkOutDegree + "\n");
		sb.append("inCloseness: " + networkInCloseness + "\n");
		sb.append("outCloseness: " + networkOutCloseness + "\n");
		sb.append("betweenness: " + networkBetweenness + "\n");
		sb.append("networkClusteringCoefficient: " + networkClusteringCoefficient + "\n");
		sb.append("diameter: " + diameter + "\n");
		sb.append("characteristicPathLength: " + characteristicPathLength + "\n");
		
		sb.append("inAlpha: " + inAlpha + "\n");
		sb.append("inBeta: " + inBeta + "\n");
		sb.append("in degree count[");
		for (Double count : inCounts){
			sb.append(count + ",");
		}		
		sb.append("]\n");
		sb.append("outAlpha: " + outAlpha + "\n");
		sb.append("outBeta: " + outBeta + "\n");
		sb.append("out degree count[");
		for (Double count : outCounts){
			sb.append(count + ",");
		}		
		sb.append("]\n");
		
		sb.append("Cliques[\n");
		for (Clique clique : cliques){
			sb.append(clique + "\n");
		}
		sb.append("]\n");
		
		for (Long nodeId : (Set<Long>)individualMetricMap.keySet()){
			IndividualMetric im = getIndividualMetric(nodeId);
			sb.append(im + "\n");
		}
		
		return sb.toString();
	}
}
