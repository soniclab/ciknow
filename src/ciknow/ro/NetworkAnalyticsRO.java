package ciknow.ro;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.graph.metrics.NetworkAnalytics;
import ciknow.graph.metrics.NetworkMetrics;
import ciknow.io.AnalyticsWriter;
import ciknow.util.Beans;
import ciknow.util.GeneralUtil;
import ciknow.vis.NetworkExtractor;
import edu.uci.ics.jung.graph.filters.impl.KNeighborhoodFilter;

public class NetworkAnalyticsRO {
	private static Log logger = LogFactory.getLog(NetworkAnalyticsRO.class);
	private GenericRO genericRO;
	private AnalyticsWriter analyticsWriter;
	
	public static void main(String[] args) {
		//new NetworkAnalyticsRO().getLocalNetworkAnalytics(99999L, 10L, 0);
	}

	public NetworkAnalyticsRO() {

	}

	public GenericRO getGenericRO() {
		return genericRO;
	}

	public void setGenericRO(GenericRO genericRO) {
		this.genericRO = genericRO;
	}
	
	public AnalyticsWriter getAnalyticsWriter() {
		return analyticsWriter;
	}

	public void setAnalyticsWriter(AnalyticsWriter analyticsWriter) {
		this.analyticsWriter = analyticsWriter;
	}

	@SuppressWarnings("unchecked")
	public Map getNetworkMetrics(Map params) throws Exception{
		logger.info("************************** get network metrics...");
		Map result = new HashMap();

        Beans.init();
        Map network = null;
        Collection<Node> nodes = null;
        Collection<Edge> edges = null;
        NetworkExtractor extractor = (NetworkExtractor)Beans.getBean("networkExtractor");
        String type = (String) params.get("type");	
        String allowHugeNetwork = (String) params.get("allowHugeNetwork");
		if (type.equals("custom")){	        
	        List<String> edgeTypes = (List<String>)params.get("edgeTypes");
	    	List<String> nodeFilters = (List<String>)params.get("nodeFilters"); 
	    	String nfc = (String)params.get("nfc");
	    	List<String> edgeFilters = (List<String>)params.get("edgeFilters");
	    	String efc = (String)params.get("efc");
	        List<String> nodeAttributes = (List<String>) params.get("nodeAttributes");
	        String questionCombineMethod = (String) params.get("questionCombineMethod");
	        String nodeAttributeCombineMethod = (String) params.get("nodeAttributeCombineMethod");	        
	        String isolate = (String) params.get("isolate");
	        String showRawRelation = (String) params.get("showRawRelation");
	        String operator = (String) params.get("operator");
	        
	        network = extractor.getCustomNetwork(edgeTypes, operator,
	        									nodeFilters, nfc,
	        									edgeFilters, efc,
	        									nodeAttributes, 
	        									nodeAttributeCombineMethod, 
	        									questionCombineMethod, 
	        									isolate, showRawRelation);
		} else if (type.equals("local")){	        
	        List<String> sourceIds = (List<String>)params.get("sourceIds");
	        List<Long> rootIds = new ArrayList<Long>();
	        for (String id : sourceIds){
	        	rootIds.add(Long.parseLong(id));
	        }
	        String depth = (String)params.get("depth");
	        String includeDerivedEdges = (String)params.get("includeDerivedEdges");
	        List<String> edgeTypes = (List<String>)params.get("edgeTypes");
	        network = extractor.getLocalNetwork(rootIds, 
	        									Integer.parseInt(depth), 
	        									includeDerivedEdges.equals("1"),
	        									false,
	        									KNeighborhoodFilter.IN_OUT, 
	        									edgeTypes);
		} else {
			logger.error("unrecognized network type: " + type);
			result.put("type", type);
			result.put("networkMetricsList", new ArrayList<NetworkMetrics>());
			return result;
		}

        nodes = (Collection<Node>) network.get("nodes");
        edges = (Collection<Edge>) network.get("edges");
		String numNodes = "5000";
		String numEdges = "5000";
		// String hardlimit = "10000"; // analytics can handle huge networks, just need to wait
		try {
			Map limits = GeneralUtil.getLargeNetworkLimits();
			numNodes = (String)limits.get("nodes");
			numEdges = (String)limits.get("edges");
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        if (nodes.size() == 0 || edges.size() == 0){
        	logger.error("empty network.");
			result.put("type", type);
			result.put("networkMetricsList", new ArrayList<NetworkMetrics>());
			return result;        	
        } else if (allowHugeNetwork != null && !allowHugeNetwork.equals("1") && 
				(nodes.size() > Integer.parseInt(numNodes) || edges.size() > Integer.parseInt(numEdges))){
        	logger.error("network is too large.");
			result.put("type", type);
			result.put("networkMetricsList", new ArrayList<NetworkMetrics>());
			result.put("msg", "Selected network is (nodes=" + nodes.size() + ", edges=" + edges.size() + "). " +
					"<br>This analytics calculation may take a considerable amount of time. " +
					"<br>Please limit the size of the network, or check the option of 'Allow large network'.");
			
			return result;  	
		}
        
        String direction = (String) params.get("direction");
        String undirectedOperator = (String) params.get("undirectedOperator");
        
        logger.debug("prepare nodeId to fullNode map");
		Map<Long, Node> nodeMap = GeneralUtil.getNodeMap(nodes);
        List<NetworkMetrics> metricList = NetworkAnalytics.getNetworkMetrics(nodes, edges, nodeMap, Integer.parseInt(direction), undirectedOperator);	
        
        // write to zip file for download
        String filename = "";
        if (true){
        	long r = Math.round(Math.random()*1000000);
        	String path = genericRO.getRealPath();
        	filename = "analytics_" + r + ".zip";
        	logger.info("writing analytics to file: " + filename);
        	OutputStream os = new FileOutputStream(path + filename);
        	ZipOutputStream zout = new ZipOutputStream(os);
        	//PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
        	for (NetworkMetrics nm : metricList){
        		ZipEntry entry = new ZipEntry(nm.getNetworkName()+".txt");
        		zout.putNextEntry(entry);
        		analyticsWriter.write(nm, nodeMap, zout);
        	}
        	//pw.close();
        	zout.close();
        }
        
        result.put("type", type);
        result.put("networkMetricsList", metricList);    
        result.put("filename", filename);
        
        logger.info("**************************  done.");
		return result;
	}
}
