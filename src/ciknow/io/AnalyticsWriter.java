package ciknow.io;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.NodeDao;
import ciknow.domain.Node;
import ciknow.graph.metrics.Clique;
import ciknow.graph.metrics.ConnectedComponent;
import ciknow.graph.metrics.IndividualMetric;
import ciknow.graph.metrics.NetworkMetrics;

public class AnalyticsWriter {
	private Log logger = LogFactory.getLog(AnalyticsWriter.class);
	private NodeDao nodeDao;
	
	public AnalyticsWriter(){
		
	}

	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}
	
	public void write(NetworkMetrics nm, Map<Long, Node> nodeMap, OutputStream os) throws Exception{
		logger.info("Writing network metrics...");
		NumberFormat nf = new DecimalFormat("0.00");
		StringBuilder sb = new StringBuilder();
		sb.append("Network+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
		sb.append("Name:").append(nm.getNetworkName()).append("\n");
		sb.append("Directed: ").append(nm.getDirected()).append("\n");
		sb.append("Total Nodes: ").append(nm.getTotalNodes()).append("\n");
		sb.append("Total Edges: ").append(nm.getTotalEdges()).append("\n");
		sb.append("Density: ").append(nm.getNetworkDensity().isNaN()?nm.getNetworkDensity():nf.format(nm.getNetworkDensity())).append("\n");
		sb.append("In Degree: ").append(nm.getNetworkInDegree().isNaN()?nm.getNetworkInDegree():nf.format(nm.getNetworkInDegree())).append("\n");
		sb.append("Out Degree: ").append(nm.getNetworkOutDegree().isNaN()?nm.getNetworkOutDegree():nf.format(nm.getNetworkOutDegree())).append("\n");
		sb.append("In Closeness: ").append(nm.getNetworkInCloseness().isNaN()?nm.getNetworkInCloseness():nf.format(nm.getNetworkInCloseness())).append("\n");
		sb.append("Out Closeness: ").append(nm.getNetworkOutCloseness().isNaN()?nm.getNetworkOutCloseness():nf.format(nm.getNetworkOutCloseness())).append("\n");
		sb.append("Betweenness: ").append(nm.getNetworkBetweenness().isNaN()?nm.getNetworkBetweenness():nf.format(nm.getNetworkBetweenness())).append("\n");
		sb.append("Clustering Coefficient: ").append(nm.getNetworkClusteringCoefficient().isNaN()?nm.getNetworkClusteringCoefficient():nf.format(nm.getNetworkClusteringCoefficient())).append("\n");
		sb.append("Diameter: ").append(nm.getDiameter().isNaN()?nm.getDiameter():nf.format(nm.getDiameter())).append("\n");
		sb.append("Characteristic Path Length: ").append(nm.getCharacteristicPathLength().isNaN()?nm.getCharacteristicPathLength():nf.format(nm.getCharacteristicPathLength())).append("\n");
		sb.append("In Alpha: ").append(nm.getInAlpha().isNaN()?nm.getInAlpha():nf.format(nm.getInAlpha())).append("\n");
		sb.append("In Beta: ").append(nm.getInBeta().isNaN()?nm.getInBeta():nf.format(nm.getInBeta())).append("\n");
		sb.append("Out Alpha: ").append(nm.getOutAlpha().isNaN()?nm.getOutAlpha():nf.format(nm.getOutAlpha())).append("\n");
		sb.append("Out Beta: ").append(nm.getOutBeta().isNaN()?nm.getOutBeta():nf.format(nm.getOutBeta())).append("\n");
//		sb.append("In Counts: ").append(nm.getInCounts()).append("\n");
//		sb.append("Out Counts: ").append(nm.getOutCounts()).append("\n");
		
		sb.append("Individual+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
		sb.append("ID");
		sb.append("\t").append("Label");
		sb.append("\t").append("In Degree");
		sb.append("\t").append("Out Degree");
		sb.append("\t").append("In Closeness");
		sb.append("\t").append("Out Closeness");
		sb.append("\t").append("Betweenness");
		sb.append("\t").append("Page Rank");
		sb.append("\t").append("Scanning");
		sb.append("\t").append("Clustering Coefficients").append("\n");
		
		/*
		List<Node> nodes = nodeDao.findByIds(nm.getIndividualMetricMap().keySet());		
		Map<Long, Node> nodeMap = new HashMap<Long, Node>();
		for (Node node : nodes){
			nodeMap.put(node.getId(), node);
		}
		*/
		
		for (IndividualMetric im : nm.getIndividualMetrics()){
			sb.append(im.nodeId);
			sb.append("\t").append(nodeMap.get(im.nodeId).getLabel());
			sb.append("\t").append(im.inDegree);
			sb.append("\t").append(im.outDegree);
			sb.append("\t").append(im.inCloseness.isNaN()?im.inCloseness:nf.format(im.inCloseness));
			sb.append("\t").append(im.outCloseness.isNaN()?im.outCloseness:nf.format(im.outCloseness));
			sb.append("\t").append(im.betweenness.isNaN()?im.betweenness:nf.format(im.betweenness));
			sb.append("\t").append(im.pageRank.isNaN()?im.pageRank:nf.format(im.pageRank));
			sb.append("\t").append(im.scanning.isNaN()?im.scanning:nf.format(im.scanning));
			sb.append("\t").append(im.clusteringCoefficient.isNaN()?im.clusteringCoefficient:nf.format(im.clusteringCoefficient));
			sb.append("\n");
		}
		
		sb.append("Cliques+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
		int i = 0;
		for (Clique c : nm.getCliques()){
			sb.append(++i).append(":\t");
			for (Long nodeId : c.nodes){
				Node node = nodeMap.get(nodeId);
				if (node == null){
					logger.error("!!!!!!!!!!! cannot identify node (id=" + nodeId + ") in clique: " + i);
					throw new Exception("cannot identify node(id=" + nodeId + ") in clique: " + i);
				}
				sb.append(node.getLabel()).append(",");
			}
			sb.append("\n");
		}
		
		sb.append("Components+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
		i = 0;
		for (ConnectedComponent c : nm.getConnectedComponents()){
			sb.append(++i).append(":\t");
			for (Long nodeId : c.nodes){
				sb.append(nodeMap.get(nodeId).getLabel()).append(",");
			}
			sb.append("\n");
		}

		os.write(sb.toString().getBytes());
	}
}
