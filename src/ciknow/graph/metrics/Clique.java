package ciknow.graph.metrics;

import java.util.List;

/*
 * represent a maximal clique
 */
public class Clique {
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
