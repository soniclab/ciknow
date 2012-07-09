package ciknow.ro;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.dto.FlareVisData;
import ciknow.util.Beans;
import ciknow.vis.NetworkExtractor;
import edu.uci.ics.jung.graph.filters.impl.KNeighborhoodFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;



/**
 * User: gyao
 * Date: Mar 5, 2008
 * Time: 5:55:39 PM
 */
public class FlareRO {

    private Log logger = LogFactory.getLog(NodeRO.class);
    
    public FlareRO(){

    }

    @SuppressWarnings("unchecked")
	public FlareVisData getLocalNetwork(Map request){
    	logger.info("************************** get local network for flare...");
    	logger.debug(request);
    	        
        String nodeId = (String)request.get("nodeId");
        List<Long> rootIDs = new ArrayList<Long>();
        rootIDs.add(Long.parseLong(nodeId));
        String depth = (String)request.get("depth");
        String includeDerivedEdges = (String)request.get("includeDerivedEdges");
      	List<String> edgeTypes = (List<String>)request.get("edgeTypes");
      	
      	NetworkExtractor extractor = (NetworkExtractor) Beans.getBean("networkExtractor");
      	Map m = extractor.getLocalNetwork(rootIDs, 
      									Integer.parseInt(depth),
      									includeDerivedEdges.equals("1"),
      									false,
      									KNeighborhoodFilter.IN_OUT, 
      									edgeTypes);        
        Collection<Node> nodes = (Collection<Node>) m.get("nodes");
        Collection<Edge> edges = (Collection<Edge>) m.get("edges");
        
        logger.debug("converting to flare readable data...");
        FlareVisData d = new FlareVisData(nodes, edges); 
        
        logger.debug("************************** get local network for flare done");
        
        return d;
    }

    @SuppressWarnings("unchecked")
	public FlareVisData getCustomNetwork(Map request){   
    	logger.debug("************************** get custom network for flare ...");
    	List<String> edgeTypes = (List<String>)request.get("edgeTypes");  
    	List<String> nodeFilters = (List<String>)request.get("nodeFilters");
    	String nfc = (String)request.get("nfc");
    	List<String> edgeFilters = (List<String>)request.get("edgeFilters");    
    	String efc = (String)request.get("efc");
    	List<String> nodeAttributes = (List<String>) request.get("nodeAttributes");
    	String nodeAttributeCombineMethod = (String)request.get("nodeAttributeCombineMethod");
    	String questionCombineMethod = (String)request.get("questionCombineMethod");
    	String showIsolate = (String) request.get("isolate");
    	String showRawRelation = (String) request.get("showRawRelation");
    	String operator = (String) request.get("operator");
    	
        NetworkExtractor extractor = (NetworkExtractor) Beans.getBean("networkExtractor");
        Map m = extractor.getCustomNetwork(edgeTypes, operator, nodeFilters, nfc, edgeFilters, efc,
        							nodeAttributes, 
        							nodeAttributeCombineMethod, 
        							questionCombineMethod, 
        							showIsolate, showRawRelation);
        Collection<Node> nodes = (Collection<Node>) m.get("nodes");
        Collection<Edge> edges = (Collection<Edge>) m.get("edges");
        
        logger.debug("converting to flare readable data...");
        FlareVisData d = new FlareVisData(nodes, edges);
        
        logger.debug("************************** get custom network for flare done");
        
        return d;
    }
}
