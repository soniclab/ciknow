package ciknow.graph.metrics;

import java.util.List;

/*
 * represent a connected component for undirected graph, and
 * strongly connected component for directed graph
 */
public class ConnectedComponent {
	public List<Long> nodes;
	public String label;
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for (Long nodeId : nodes){
			sb.append(nodeId).append(",");
		}
		return sb.toString();
	}
}
