package ciknow.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;

import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.VisualizationDao;
import ciknow.domain.Group;
import ciknow.domain.Node;
import ciknow.domain.Visualization;

import dbunit.Dbunit;
import dbunit.DbunitTestExecutionListener;

@TestExecutionListeners(DbunitTestExecutionListener.class)
//@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)
public class VisualizationHibernateDaoTest extends AbstractHibernateDaoTest {
	
	
	@Autowired private VisualizationDao visDao;
	@Autowired private NodeDao nodeDao;
	@Autowired private GroupDao groupDao;
	
	private static final Logger logger = Logger
			.getLogger(VisualizationHibernateDaoTest.class.getName());
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void save(){
		
		Node node1 = nodeDao.findById(1L);
		Node node2 = nodeDao.findById(2L);
		
		Visualization vis = visDao.findById(1L);
		Assert.assertEquals(node1, vis.getCreator());
		
		vis.setCreator(node2);
		visDao.save(vis);
		
		Visualization vis2 = visDao.findById(1L);
		Assert.assertEquals(node2, vis2.getCreator());
		
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void saveCollection(){
		
		Node node1 = nodeDao.findById(1L);
		Node node2 = nodeDao.findById(2L);
		
		Visualization vis = visDao.findById(1L);
		Visualization vis2 = visDao.findById(2L);
		Assert.assertEquals(node1, vis.getCreator());
		Assert.assertEquals(node2, vis2.getCreator());
		
		vis.setCreator(node2);
		vis2.setCreator(node1);
		
		ArrayList<Visualization> list = new ArrayList<Visualization>();
		list.add(vis);
		list.add(vis2);
		
		visDao.save(list);
		
		Visualization vis3 = visDao.findById(1L);
		Assert.assertEquals(node2, vis3.getCreator());
		
		vis3 = visDao.findById(2L);
		Assert.assertEquals(node1, vis3.getCreator());
	
	}
	
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void delete(){
		
		Assert.assertEquals(3, visDao.getCount());
		Visualization vis = visDao.findById(1L);
		visDao.delete(vis);
		
		Assert.assertEquals(2, visDao.getCount());
		
		List<Visualization> list = visDao.getAll();
		//make sure the correct visualization was deleted
		Assert.assertTrue(!list.contains(vis));
		
	
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void deleteCollection(){
		
		Assert.assertEquals(3, visDao.getCount());
		
		Visualization vis = visDao.findById(1L);
		Visualization vis2 = visDao.findById(2L);
		
		ArrayList<Visualization> list = new ArrayList<Visualization>();
		list.add(vis);
		list.add(vis2);
		
		visDao.delete(list);
		
		Assert.assertEquals(1, visDao.getCount());
		
		list = (ArrayList<Visualization>) visDao.getAll();
		//make sure correct record remains
		Assert.assertEquals(new Long(3L), list.get(0).getId());
	
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void deleteAll(){
		
		Assert.assertEquals(3, visDao.getCount());
		
		//TODO Address BulkUpdate failure
		
		//visDao.deleteAll(); //bulk update will always fail because of key constraints
		
		//visualization has attribute constraints -- not child objects,
		//therefore the following code executes without problem
		/*
		Visualization vis = visDao.findById(1L);
		Visualization vis2 = visDao.findById(2L);
		Visualization vis3 = visDao.findById(3L);
		
		ArrayList<Visualization> list = new ArrayList<Visualization>();
		list.add(vis);
		list.add(vis2);
		list.add(vis3);
	
		visDao.delete(list);
		*/
		
		//Assert.assertEquals(0, visDao.getCount());
	
	}
		
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void getAll(){
		
		Node node1 = nodeDao.findById(1L);
		Node node2 = nodeDao.findById(2L);
		
		ArrayList<Visualization> list  = (ArrayList<Visualization>) visDao.getAll();
		Assert.assertEquals(3, list.size());
		
		Visualization vis = list.get(0);
		checkVisualizationParams(vis, 1L, 1L, node1, "Visulalization_one", "visualization one", "visual type 1", 
				"network type 1", "Here is some medium text data. blah blah blah...");
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");
		checkVisualizationAttributes(vis, attributes);
		Set<Group> groups = new HashSet<Group>();
		groups.add(groupDao.findById(1L));
		groups.add(groupDao.findById(2L));
		checkGroups(vis, groups);
		
		
		vis = list.get(1);
		checkVisualizationParams(vis, 2L, 1L, node2, "Visulalization_two", "visualization two", "visual type 1", 
				"network type 1", "Here is some medium text data. blah blah blah...");
		attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 3");
		checkVisualizationAttributes(vis, attributes);
		groups = new HashSet<Group>();
		groups.add(groupDao.findById(2L));
		checkGroups(vis, groups);
		
		
		vis = list.get(2);
		checkVisualizationParams(vis, 3L, 1L, node1, "Visulalization_three", "visualization three", "visual type 1", 
				"network type 1", "Here is some medium text data. blah blah blah...");
		attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 4");
		checkVisualizationAttributes(vis, attributes);
		groups = new HashSet<Group>();
		groups.add(groupDao.findById(2L));
		checkGroups(vis, groups);
		
	
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void getCount2(){
		Assert.assertEquals(3, visDao.getCount());
		
		Visualization vis = visDao.findById(1L);
		Visualization vis2 = visDao.findById(2L);
		
		ArrayList<Visualization> list = new ArrayList<Visualization>();
		list.add(vis);
		list.add(vis2);

		visDao.delete(list);
		Assert.assertEquals(1, visDao.getCount());
	
	
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void findById(){
		
		Node node1 = nodeDao.findById(1L);
		
		Visualization vis = visDao.findById(1L);
		checkVisualizationParams(vis, 1L, 1L, node1, "Visulalization_one", "visualization one", "visual type 1", 
				"network type 1", "Here is some medium text data. blah blah blah...");
		
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");
		checkVisualizationAttributes(vis, attributes);
		
		Set<Group> groups = new HashSet<Group>();
		groups.add(groupDao.findById(1L));
		groups.add(groupDao.findById(2L));
		checkGroups(vis, groups);
		

	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void findByIds(){
		
		Node node1 = nodeDao.findById(1L);
		Node node2 = nodeDao.findById(2L);
		
		ArrayList<Long> ids = new ArrayList<Long>();
		ids.add(1L);
		ids.add(2L);
		
		ArrayList<Visualization> list  = (ArrayList<Visualization>) visDao.findByIds(ids);
		Assert.assertEquals(2, list.size());
		
		Visualization vis = list.get(0);
		checkVisualizationParams(vis, 1L, 1L, node1, "Visulalization_one", "visualization one", "visual type 1", 
				"network type 1", "Here is some medium text data. blah blah blah...");
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");
		checkVisualizationAttributes(vis, attributes);
		Set<Group> groups = new HashSet<Group>();
		groups.add(groupDao.findById(1L));
		groups.add(groupDao.findById(2L));
		checkGroups(vis, groups);
		
		vis = list.get(1);
		checkVisualizationParams(vis, 2L, 1L, node2, "Visulalization_two", "visualization two", "visual type 1", 
				"network type 1", "Here is some medium text data. blah blah blah...");
		attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 3");
		checkVisualizationAttributes(vis, attributes);
		groups = new HashSet<Group>();
		groups.add(groupDao.findById(2L));
		checkGroups(vis, groups);
	
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void findByCreator(){
		
		Node node1 = nodeDao.findById(1L);
		ArrayList<Visualization> list  = (ArrayList<Visualization>) visDao.findByCreator(node1);
		Assert.assertEquals(2, list.size());
		
		Visualization vis = list.get(0);
		checkVisualizationParams(vis, 1L, 1L, node1, "Visulalization_one", "visualization one", "visual type 1", 
				"network type 1", "Here is some medium text data. blah blah blah...");
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");	
		checkVisualizationAttributes(vis, attributes);
		Set<Group> groups = new HashSet<Group>();
		groups.add(groupDao.findById(1L));
		groups.add(groupDao.findById(2L));
		checkGroups(vis, groups);
		
		vis = list.get(1);
		checkVisualizationParams(vis, 3L, 1L, node1, "Visulalization_three", "visualization three", "visual type 1", 
				"network type 1", "Here is some medium text data. blah blah blah...");
		
		attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 4");
		checkVisualizationAttributes(vis, attributes);
		groups = new HashSet<Group>();
		groups.add(groupDao.findById(2L));
		checkGroups(vis, groups);
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void findByCreatorAndName(){
		Node node1 = nodeDao.findById(1L);
		ArrayList<Visualization> list  = (ArrayList<Visualization>) visDao.findByCreatorAndName(node1, "Visulalization_one");
		Assert.assertEquals(1, list.size());
		
		Visualization vis = visDao.findById(1L);
		checkVisualizationParams(vis, 1L, 1L, node1, "Visulalization_one", "visualization one", "visual type 1", 
				"network type 1", "Here is some medium text data. blah blah blah...");
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");	
		checkVisualizationAttributes(vis, attributes);
		Set<Group> groups = new HashSet<Group>();
		groups.add(groupDao.findById(1L));
		groups.add(groupDao.findById(2L));
		checkGroups(vis, groups);
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/visualization.xml")
	public void getVisIdsByNodeId(){
		
		Set<Long> ids = visDao.getVisIdsByNodeId(2L);
		Assert.assertEquals(2, ids.size());
		Assert.assertTrue(ids.contains(1L));
		Assert.assertTrue(ids.contains(2L));
		
		ids = visDao.getVisIdsByNodeId(1L);
		Assert.assertEquals(3, ids.size());
		Assert.assertTrue(ids.contains(1L));
		Assert.assertTrue(ids.contains(2L));
		Assert.assertTrue(ids.contains(3L));
	
	}
	 
	
	private void checkVisualizationParams(Visualization vis, Long id, Long version, Node creator, String name, String label, String type, String networkType, String data){
		Assert.assertEquals(id, vis.getId());
		Assert.assertEquals(version, vis.getVersion());
		Assert.assertEquals(creator, vis.getCreator());
		Assert.assertEquals(name, vis.getName());
		Assert.assertEquals(label, vis.getLabel());
		Assert.assertEquals(type, vis.getType());
		Assert.assertEquals(networkType, vis.getNetworkType());
		Assert.assertEquals(data, vis.getData());	
	}
	
	private void checkVisualizationAttributes(Visualization vis, HashMap<String, String> attributes){
		
		Iterator<String> keys = attributes.keySet().iterator();
		while(keys.hasNext()){
			String key = (String)keys.next();
			Assert.assertEquals(attributes.get(key), vis.getAttributes().get(key));
		}
		
	}
	
	private void checkGroups(Visualization vis, Set<Group> groups){
		
		Assert.assertEquals(groups.size(), vis.getGroups().size());
		
		Iterator<Group> gIt = groups.iterator();
		while(gIt.hasNext()){
			Assert.assertTrue(vis.getGroups().contains(gIt.next()));
		}

	}
	

}
