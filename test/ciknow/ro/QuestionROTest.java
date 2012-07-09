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

import ciknow.dao.EdgeDao;
import ciknow.dao.GroupDao;
import ciknow.dao.JobDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.dao.RoleDao;
import ciknow.dao.SurveyDao;
import ciknow.dao.hibernate.AbstractHibernateDaoTest;

@ContextConfiguration(locations={"classpath:/applicationContext-ro.xml", "classpath:/applicationContext-general.xml"})
public class QuestionROTest extends AbstractHibernateDaoTest {
	private Log logger = LogFactory.getLog(this.getClass());
	
	@Autowired private QuestionRO ro;
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
//	@Autowired private EdgeRO edgeRO;

	@BeforeTransaction
	public void verifyInitialDatabaseState(){
	}
	
	@Before
	public void setup(){

	}
	
	@After
	public void teardown(){

	}
	
	@AfterTransaction
	public void verifyFinalDatabaseState(){

	}
}
