package ciknow.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.EdgeDao;
import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.RoleDao;
import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.util.Beans;
import ciknow.util.compare.NodeLabelComparator;

public class DLWriter {
	private static Log logger = LogFactory.getLog(DLWriter.class);
	private static int LABEL_LENGTH_MAX = 1024;
	
	private NodeDao nodeDao;
	private EdgeDao edgeDao;
	private GroupDao groupDao;
	private RoleDao roleDao;
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		Beans.init();
		DLWriter dlw = (DLWriter)Beans.getBean("dlWriter");
		String filename = "data/dl2.zip";
		List<String> edgeTypes = new LinkedList<String>();
		//edgeTypes.add("Article by Subject Category");
		edgeTypes.add("Citation");
		dlw.write(new FileOutputStream(filename), null, edgeTypes, false, false);
	}
	
	public DLWriter(){
		
	}

	public void write(String filename, Collection<Node> nodes, List<String> edgeTypes, boolean showIsolate, boolean labelEmbedded) throws FileNotFoundException, IOException{
		write(new FileOutputStream(filename), nodes, edgeTypes, showIsolate, labelEmbedded);
	}
	
	public void write(String filename, Collection<Node> nodes, Collection<Edge> edges, boolean showIsolate, boolean labelEmbedded) throws FileNotFoundException, IOException{
		write(new FileOutputStream(filename), nodes, edges, showIsolate, labelEmbedded);
	}
	
	public void write(OutputStream os, Collection<Node> nodes, List<String> edgeTypes, boolean showIsolate, boolean labelEmbedded) throws IOException{
		List<Edge> edges = new LinkedList<Edge>();
		for (String edgeType : edgeTypes){
			edges.addAll(edgeDao.loadByType(edgeType, true));
		}
		
		write(os, nodes, edges, showIsolate, labelEmbedded);
	}
	
	public void write(OutputStream os, Collection<Node> nodes, Collection<Edge> edges, boolean showIsolate, boolean labelEmbedded) throws IOException{
		logger.info("writing edges to DL zip file...");
		logger.debug("labelEmbedded: " + labelEmbedded);
		logger.debug("showIsolate: " + showIsolate);
		
		ZipOutputStream zos = new ZipOutputStream(os);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(zos, "UTF-8"));
		
		Map<String, Map<NodeTypePair, List<Edge>>> edgesByType = classify(edges);
		Map<String, Set<Node>> typeToNodesMap = null;
		if (showIsolate) typeToNodesMap = classifyNodes(nodes);
		
		for (String edgeType : edgesByType.keySet()){
			Map<NodeTypePair, List<Edge>> edgesByNTP = edgesByType.get(edgeType);
			
			for (NodeTypePair ntp : edgesByNTP.keySet()){
				List<Edge> list = edgesByNTP.get(ntp);
				
				String entryName = "dl__" + ntp.ftype + "__" + edgeType + "__" + ntp.ttype;
				if (labelEmbedded) entryName += ".embedded.txt";
				else entryName += ".txt";
				
				logger.info("writing " + entryName + " ...");
				ZipEntry entry = new ZipEntry(entryName);
				zos.putNextEntry(entry);
				
				String content = "";
				
				if (ntp.ftype.equals(ntp.ttype)) {
					Collection<Node> nodeSet = new TreeSet<Node>(new NodeLabelComparator());
					if (showIsolate) nodeSet.addAll(typeToNodesMap.get(ntp.ftype));
					content = getAsEdgeList1(nodeSet, list, showIsolate, labelEmbedded);
				} else {
					Collection<Node> fnodeSet = new TreeSet<Node>(new NodeLabelComparator());
					Collection<Node> tnodeSet = new TreeSet<Node>(new NodeLabelComparator());
					if (showIsolate) {
						fnodeSet.addAll(typeToNodesMap.get(ntp.ftype));
						tnodeSet.addAll(typeToNodesMap.get(ntp.ttype));
					}
					content = getAsEdgeList2(fnodeSet, tnodeSet, list, showIsolate, labelEmbedded);
				}
				
				writer.print(content);
				writer.flush();
			}
		}
		
		writer.close(); // this is necessary, otherwise the downloaded file is corrupted
		logger.info("finished.");
	}

	private Map<String, Set<Node>> classifyNodes(Collection<Node> nodes){
		Map<String, Set<Node>> typeToNodesMap = new HashMap<String, Set<Node>>();
		
		for (Node node : nodes){
			String nodeType = node.getType();
			Set<Node> nodeSet = typeToNodesMap.get(nodeType);
			if (nodeSet == null){
				nodeSet = new TreeSet<Node>(new NodeLabelComparator());
				typeToNodesMap.put(nodeType, nodeSet);
			}
			nodeSet.add(node);
		}
		
		return typeToNodesMap;
	}
	
	private Map<String, Map<NodeTypePair, List<Edge>>> classify(Collection<Edge> edges) {
		Map<String, Map<NodeTypePair, List<Edge>>> edgesByType = new HashMap<String, Map<NodeTypePair, List<Edge>>>();
		
		for (Edge edge : edges){
			Map<NodeTypePair, List<Edge>> edgesByNTP = edgesByType.get(edge.getType());
			if (edgesByNTP == null){
				edgesByNTP = new HashMap<NodeTypePair, List<Edge>>();
				edgesByType.put(edge.getType(), edgesByNTP);
			}
			
			NodeTypePair ntp = new NodeTypePair(edge);
			List<Edge> list = edgesByNTP.get(ntp);
			if (list == null){
				list = new LinkedList<Edge>();
				edgesByNTP.put(ntp, list);
			}
			
			list.add(edge);
		}
		
		return edgesByType;
	}	
	
	/**
	 * Write to dl format=edgelist1 (only single node type)
	 * @param edges
	 * @param labelEmbedded
	 * @return
	 */
	private String getAsEdgeList1(Collection<Node> nodes, List<Edge> edges, boolean showIsolate, boolean labelEmbedded){
		StringBuilder sb = new StringBuilder();
		
		if (!showIsolate){
			nodes = new TreeSet<Node>(new NodeLabelComparator());
			for (Edge edge : edges){
				nodes.add(edge.getFromNode());
				nodes.add(edge.getToNode());
			}		
		}
		sb.append("dl n=" + nodes.size() + " format=edgelist1\n");
		
		Set<String> labelSet = new HashSet<String>();
		if (showIsolate){
			for (Node node : nodes){
				labelSet.add(node.getLabel());
			}
		}
		
		if (labelEmbedded){
			sb.append("labels embedded\n");
			
			sb.append("data:\n");
			for (Edge edge : edges){
				String flabel = edge.getFromNode().getLabel();
				String tlabel = edge.getToNode().getLabel();
				sb.append(clean(flabel)).append(" ");
				sb.append(clean(tlabel)).append(" ");
				sb.append(edge.getWeight()).append("\n");
				
				if (showIsolate){
					labelSet.remove(flabel);
					labelSet.remove(tlabel);
				}
			}
			
			if (showIsolate){
				for (String label : labelSet){
					sb.append(clean(label)).append(" ").append(clean(label)).append(" ");
					sb.append("0").append("\n");
				}
			}
		} else {
			Map<String, Integer> map = new HashMap<String, Integer>();
			int i = 1;
			sb.append("labels:\n");
			for (Node node : nodes){
				String label = node.getLabel();
				map.put(label, i);
				
				label = clean(label);
				sb.append(label).append("\n");
				i++;
			}
			
			sb.append("data:\n");
			for (Edge edge : edges){
				String flabel = edge.getFromNode().getLabel();
				String tlabel = edge.getToNode().getLabel();
				sb.append(map.get(flabel)).append(" ");
				sb.append(map.get(tlabel)).append(" ");
				sb.append(edge.getWeight()).append("\n");
				
				if (showIsolate){
					labelSet.remove(flabel);
					labelSet.remove(tlabel);
				}
			}
			
			if (showIsolate){
				for (String label : labelSet){
					int index = map.get(label);
					sb.append(index).append(" ").append(index).append(" ");
					sb.append("0").append("\n");
				}
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Write to dl format=edgelist2 (2-mode, e.g. two different node types)
	 * @param edges
	 * @param labelEmbedded
	 * @return
	 */
	private String getAsEdgeList2(Collection<Node> rowNodes, Collection<Node> colNodes, List<Edge> edges, boolean showIsolate, boolean labelEmbedded){
		StringBuilder sb = new StringBuilder();
		if (!showIsolate){
			rowNodes = new TreeSet<Node>(new NodeLabelComparator());
			colNodes = new TreeSet<Node>(new NodeLabelComparator());
			for (Edge edge : edges){
				rowNodes.add(edge.getFromNode());
				colNodes.add(edge.getToNode());
			}
		}
		sb.append("dl nr=" + rowNodes.size() + " nc=" + colNodes.size() + " format=edgelist2\n");
		
		Set<String> rowLabelSet = new HashSet<String>();
		Set<String> colLabelSet = new HashSet<String>();
		if (showIsolate){
			for (Node node : rowNodes){
				rowLabelSet.add(node.getLabel());
			}
			
			for (Node node : colNodes){
				colLabelSet.add(node.getLabel());
			}
		}
		
		if (labelEmbedded){
			sb.append("labels embedded\n");
			
			sb.append("data:\n");
			for (Edge edge : edges){
				String flabel = edge.getFromNode().getLabel();
				String tlabel = edge.getToNode().getLabel();
				sb.append(clean(flabel)).append(" ");
				sb.append(clean(tlabel)).append(" ");
				sb.append(edge.getWeight()).append("\n");
				
				if (showIsolate){
					rowLabelSet.remove(flabel);
					colLabelSet.remove(tlabel);
				}
			}
			
			if (showIsolate){
				String rowDummy = rowNodes.iterator().next().getLabel();
				String colDummy = colNodes.iterator().next().getLabel();
				for (String label : rowLabelSet){
					sb.append(clean(label)).append(" ").append(clean(colDummy)).append(" ");
					sb.append("0").append("\n");
				}
				for (String label : colLabelSet){
					sb.append(clean(rowDummy)).append(" ").append(clean(label)).append(" ");
					sb.append("0").append("\n");
				}
			}
		} else {			
			Map<String, Integer> rowMap = new HashMap<String, Integer>();			
			int i = 1;
			sb.append("row labels:\n");
			for (Node node : rowNodes){
				String label = node.getLabel();
				rowMap.put(label, i++);
				label = clean(label);
				sb.append(label).append("\n");
			}
			
			Map<String, Integer> colMap = new HashMap<String, Integer>();
			i = 1;
			sb.append("col labels:\n");
			for (Node node : colNodes){
				String label = node.getLabel();
				colMap.put(label, i++);
				label = clean(label);
				sb.append(label).append("\n");
			}
			
			sb.append("data:\n");
			for (Edge edge : edges){
				String flabel = edge.getFromNode().getLabel();
				String tlabel = edge.getToNode().getLabel();
				sb.append(rowMap.get(flabel)).append(" ");
				sb.append(colMap.get(tlabel)).append(" ");
				sb.append(edge.getWeight()).append("\n");
			}
		}
		
		return sb.toString();
	}
	
	private String clean(String label){
		label = label.replaceAll("\\s+", "_");
		label = label.replaceAll(",", ".");
		label = label.replaceAll("\r", "(carriage return)");
		label = label.replaceAll("\n", "(new line)");
		label = label.replaceAll("\'", "_");
		label = label.replaceAll("\"", "_");
		label = "\"" + label + "\"";
		
		//label = label.replaceAll("-", "_");
		//label = label.replaceAll("\\(", "[");
		//label = label.replaceAll("\\)", "]");
		//if (label.length() > LABEL_LENGTH_MAX) label = label.substring(0, LABEL_LENGTH_MAX);
		return label;
	}
	
	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public EdgeDao getEdgeDao() {
		return edgeDao;
	}

	public void setEdgeDao(EdgeDao edgeDao) {
		this.edgeDao = edgeDao;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}
	
	private class NodeTypePair{
		private String ftype, ttype;
		
		public NodeTypePair(String ftype, String ttype){
			this.ftype = ftype;
			this.ttype = ttype;
		}

		public NodeTypePair(Edge edge){
			this(edge.getFromNode().getType(), edge.getToNode().getType());
		}
		
		
		public String getFtype() {
			return ftype;
		}

		public void setFtype(String ftype) {
			this.ftype = ftype;
		}

		public String getTtype() {
			return ttype;
		}

		public void setTtype(String ttype) {
			this.ttype = ttype;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ftype == null) ? 0 : ftype.hashCode());
			result = prime * result + ((ttype == null) ? 0 : ttype.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final NodeTypePair other = (NodeTypePair) obj;
			if (ftype == null) {
				if (other.ftype != null)
					return false;
			} else if (!ftype.equals(other.ftype))
				return false;
			if (ttype == null) {
				if (other.ttype != null)
					return false;
			} else if (!ttype.equals(other.ttype))
				return false;
			return true;
		}
		
		public String toString(){
			return ftype + " --> " + ttype;
		}
	}
}
