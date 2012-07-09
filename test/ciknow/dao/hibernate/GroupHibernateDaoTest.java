package ciknow.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;

import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.dao.VisualizationDao;
import ciknow.domain.Group;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Visualization;

import dbunit.Dbunit;
import dbunit.DbunitTestExecutionListener;

@TestExecutionListeners(DbunitTestExecutionListener.class)
//@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)
public class GroupHibernateDaoTest extends AbstractHibernateDaoTest {
	
	
	@Autowired private GroupDao groupDao;
	@Autowired private NodeDao nodeDao;
	@Autowired private QuestionDao questionDao;
	@Autowired private VisualizationDao visDao;
	
	private static final Logger logger = Logger
			.getLogger(GroupHibernateDaoTest.class.getName());
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void save(){
		
		Assert.assertEquals(4, groupDao.getAll().size());
		Group group = new Group();
		group.setName("New Group");
		group.setVersion(1L);
		groupDao.save(group);
		Assert.assertEquals(5, groupDao.getAll().size());
		
		List<Group> groups = groupDao.getAll();
		Group groupFromDb = groups.get(groups.size()-1);
		Assert.assertEquals("New Group", groupFromDb.getName());
		Assert.assertEquals(new Long(1L), groupFromDb.getVersion());
		
		Group group2 = groups.get(0);
		group2.setName("New Group Name");
		groupDao.save(group2);
		groups = groupDao.getAll();
		groupFromDb = groups.get(0);
		Assert.assertEquals("New Group Name", groupFromDb.getName());
		compareGroups(group2, groupFromDb);
		
		
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void saveCollection(){
		
		Assert.assertEquals(4, groupDao.getAll().size());
		Group group = new Group();
		group.setName("New Group");
		group.setVersion(1L);
		Group group2 = new Group();
		group2.setName("Newer Group");
		group2.setVersion(1L);
		ArrayList<Group> groups = new ArrayList<Group>();
		groups.add(group);
		groups.add(group2);
		groupDao.save(groups);
		Assert.assertEquals(6, groupDao.getAll().size());
		
		List<Group> list2 = groupDao.getAll();
		compareGroups(group, list2.get(list2.size()-2));
		compareGroups(group2, list2.get(list2.size()-1));
		
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void delete(){
		
		List<Group> list = groupDao.getAll();
		Assert.assertEquals(4, list.size());
		
		Group group = list.get(0);
		removeConstraints(group);
		groupDao.delete(group);
		
		List<Group> list2 = groupDao.getAll();
		Assert.assertEquals(3, list2.size());
		
		Assert.assertTrue(!list2.contains(group));
		
	}
	
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void deleteCollection(){
		
		List<Group> list = groupDao.getAll();
		Assert.assertEquals(4, list.size());
		
		Group group = list.get(0);
		removeConstraints(group);
		Group group2 = list.get(1);
		removeConstraints(group2);
		List<Group> list2 = new ArrayList<Group>();
		list2.add(group);
		list2.add(group2);
		groupDao.delete(list2);
		
		List<Group> list3 = groupDao.getAll();
		Assert.assertEquals(2, list3.size());
		
		Assert.assertTrue(!list3.contains(group));
		Assert.assertTrue(!list3.contains(group2));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void deleteAll(){
		
		List<Group> list = groupDao.getAll();
		Assert.assertEquals(4, list.size());
		for(Group g: list){
			removeConstraints(g);
		}
		groupDao.deleteAll();
		Assert.assertEquals(0, groupDao.getAll().size());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void getProxy(){
		
		Group proxy = groupDao.getProxy(1L);
		Assert.assertEquals(new Long(1L), proxy.getId());
	}
	
	
	/*
	 * 
	 * There's no way to distinguish between loadBy, findBy, and getProxy methods,
	 * because Hibernate uses lazy fetch for all 3 and since we are still inside
	 * the session all the methods will behave the same.
	 * - i.e for getProxy you would expect a LazyInitialization Error when accessing anything except the ID, but
	 * that doesn't happen. Hibernate just loads the missing parameters from the database. 
	 * 
	 */
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void findById(){
		
		Group group = groupDao.findById(1L);
		Assert.assertEquals(new Long(1L), group.getId());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void loadById(){
		Group group = groupDao.loadById(1L);
		Assert.assertEquals(new Long(1L), group.getId());
		Assert.assertEquals("ALL", group.getName());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void findByName(){
		Group group = groupDao.findByName("USER");
		Assert.assertEquals(new Long(2L), group.getId());
		Assert.assertEquals("USER", group.getName());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void loadByName(){
		
		Node node1 = nodeDao.loadById(1L);	
		
		Group group = groupDao.loadByName("USER");
		Assert.assertEquals(new Long(2L), group.getId());
		Assert.assertEquals("USER", group.getName());
		Assert.assertTrue(group.getNodes().contains(node1));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void getNodeIdsByGroupId(){
		
		List<Long> list = groupDao.getNodeIdsByGroupId(1L);
		Assert.assertEquals(2, list.size());
		Assert.assertTrue(list.contains(new Long(2L)));
		Assert.assertTrue(list.contains(new Long(3L)));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void updateNodesInGroup(){
		
		List<Long> list = groupDao.getNodeIdsByGroupId(2L);
		Assert.assertEquals(1, list.size());
		Assert.assertTrue(list.contains(new Long(1L)));
		
		
		ArrayList<Long> nodeIds = new ArrayList<Long>();
		nodeIds.add(2L);
		nodeIds.add(3L);
		groupDao.updateNodesInGroup(2L, nodeIds);
		
		Group group = groupDao.loadById(2L);
		Node node2 = nodeDao.loadById(2L);	
		Node node3 = nodeDao.loadById(3L);	
		Assert.assertEquals(2, group.getNodes().size());
		Assert.assertTrue(group.getNodes().contains(node2));
		Assert.assertTrue(group.getNodes().contains(node3));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/groups.xml")
	public void getAll(){
		
		Node node1 = nodeDao.loadById(1L);	
		Node node2 = nodeDao.loadById(2L);	
		Node node3 = nodeDao.loadById(3L);	
		
		
		List<Group> list = groupDao.getAll();
		Assert.assertEquals(4, list.size());
		
		Group group = list.get(0);
		Assert.assertEquals(new Long(1L), group.getId());
		Assert.assertEquals("ALL", group.getName());
		Assert.assertTrue(group.getNodes().contains(node2));
		Assert.assertTrue(group.getNodes().contains(node3));
		
		group = list.get(1);
		Assert.assertEquals(new Long(2L), group.getId());
		Assert.assertEquals("USER", group.getName());
		Assert.assertTrue(group.getNodes().contains(node1));
		
		group = list.get(2);
		Assert.assertEquals(new Long(3L), group.getId());
		Assert.assertEquals("UG_Private_group", group.getName());
		Assert.assertEquals(0, group.getNodes().size());
		
		group = list.get(3);
		Assert.assertEquals(new Long(4L), group.getId());
		Assert.assertEquals("MG_Mandatory_group", group.getName());
		Assert.assertEquals(0, group.getNodes().size());
		
	}
	

	private void compareGroups(Group expected, Group actual){
		
		Assert.assertEquals(expected.getName(), actual.getName());
		Assert.assertEquals(expected.getVersion(), actual.getVersion());
		Assert.assertEquals(expected.isMandatory(), actual.isMandatory());
		Assert.assertEquals(expected.isPrivate(), actual.isPrivate());
		Assert.assertEquals(expected.getId(), actual.getId());
		Assert.assertEquals(expected.getVersion(), actual.getVersion());
		Assert.assertTrue(expected.getNodes().containsAll(actual.getNodes()));
		
	}
	
	private void removeConstraints(Group group){
		
		List<Question> questions = questionDao.getAll();
    	for (Question question : questions){
    		question.getVisibleGroups().remove(group);
    		question.getAvailableGroups().remove(group);
    		question.getAvailableGroups2().remove(group);
    	}
    	questionDao.save(questions);
		List<Node> nodes = nodeDao.loadAll();
		for (Node node : nodes){
				node.getGroups().remove(group);
		}
    	nodeDao.save(nodes);
    	List<Visualization> viss = visDao.getAll();
    	for (Visualization vis : viss){
    		vis.getGroups().remove(group);
    	}
    	visDao.save(viss);
		
	}

}
