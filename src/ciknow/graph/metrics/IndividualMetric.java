package ciknow.graph.metrics;

import java.text.NumberFormat;

public class IndividualMetric{
	public Long nodeId;
	
	public Integer inDegree = 0;
	public Integer outDegree = 0; // as "degree" for undirected graph
	public Double inCloseness = Double.NaN;
	public Double outCloseness = Double.NaN; // as "closeness" for undirected graph
	public Double betweenness = Double.NaN;
	public Double pageRank = Double.NaN; // for directed graph (undirected graph should be converted to directed graph)
	public Double scanning = Double.NaN;
	public Double clusteringCoefficient = Double.NaN;
	
	public String toString(){
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		StringBuffer sb = new StringBuffer();
		sb.append("IndividualMetric[");
		sb.append("nodeId: " + nodeId + ",");
		sb.append("inDegree: " + inDegree + ",");
		sb.append("outDegree: " + outDegree + ",");
		sb.append("inCloseness: ").append(inCloseness.isNaN()?"-":nf.format(inCloseness.doubleValue())).append(",");
		sb.append("outCloseness: ").append(outCloseness.isNaN()?"-":nf.format(outCloseness.doubleValue())).append(",");
		sb.append("betweenness: ").append(betweenness.isNaN()?"-":nf.format(betweenness.doubleValue())).append(",");
		sb.append("pageRank: ").append(pageRank.isNaN()?"-":nf.format(pageRank.doubleValue())).append(",");
		sb.append("scanning: ").append(scanning.isNaN()?"-":nf.format(scanning.doubleValue())).append(",");
		sb.append("clusteringCoefficient: ").append(clusteringCoefficient.isNaN()?"-":nf.format(clusteringCoefficient.doubleValue()));
		sb.append("]");
		return sb.toString();
	}
}
