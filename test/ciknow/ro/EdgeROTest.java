package ciknow.ro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

import ciknow.dao.hibernate.AbstractHibernateDaoTest;

@ContextConfiguration(locations={"classpath:/applicationContext-ro.xml", "classpath:/applicationContext-general.xml"})
public class EdgeROTest extends AbstractHibernateDaoTest {
	private Log logger = LogFactory.getLog(this.getClass());
	
//	@Autowired private GenericRO ro;
//	@Autowired private NodeDao nodeDao;
//	@Autowired private EdgeDao edgeDao;
//	@Autowired private GroupDao groupDao;
//	@Autowired private RoleDao roleDao;
//	@Autowired private SurveyDao surveyDao;
//	@Autowired private QuestionDao questionDao;
//	@Autowired private JobDao jobDao;
//    
//	@Autowired private NodeRO nodeRO;
//	@Autowired private GroupRO groupRO;
//	@Autowired private RoleRO roleRO;
//	@Autowired private OptionRO optionRO;
//	@Autowired private SurveyRO surveyRO;
//	@Autowired private QuestionRO questionRO;
	@Autowired private EdgeRO edgeRO;

	@BeforeTransaction
	public void verifyInitialDatabaseState(){
	}
	
	@Before
	public void setup(){

	}
	

	@Test
	public void saveEdges() throws Exception{
		
	}
	
	@Test
	public void deriveEdgesByRelation() throws Exception{
		Map data = new HashMap();
		data.put("edgeTypeA", "Authorship");
		data.put("directionA", "-1");
		data.put("operator", "multiplication");
		data.put("edgeTypeB", "Authorship");
		data.put("directionB", "1");
		data.put("creatorId", "1");
		
//    	String edgeTypeA = (String)data.get("edgeTypeA");
//    	int directionA = Integer.parseInt((String)data.get("directionA"));
//    	List<String> nodeFilterConditions_a = (List<String>)data.get("nodeFilterConditions_a");
//    	String nodeCombiner_a = (String)data.get("nodeCombiner_a");
//    	List<String> edgeFilterConditions_a = (List<String>)data.get("edgeFilterConditions_a");
//    	String edgeCombiner_a = (String)data.get("edgeCombiner_a");
//    	
//    	String operator = (String)data.get("operator");
//    	
//    	String edgeTypeB = (String)data.get("edgeTypeB");    	
//    	int directionB = Integer.parseInt((String)data.get("directionB"));
//    	List<String> nodeFilterConditions_b = (List<String>)data.get("nodeFilterConditions_b");
//    	String nodeCombiner_b = (String)data.get("nodeCombiner_b");
//    	List<String> edgeFilterConditions_b = (List<String>)data.get("edgeFilterConditions_b");
//    	String edgeCombiner_b = (String)data.get("edgeCombiner_b");
//    	
//    	Long creator = Long.parseLong((String)data.get("creatorId"));
		
		edgeRO.deriveEdgesByRelation(data);
	}
	
	@After
	public void teardown(){

	}
	
	@AfterTransaction
	public void verifyFinalDatabaseState(){

	}
}
