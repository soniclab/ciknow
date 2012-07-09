package ciknow.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;

import ciknow.dao.NodeDao;
import ciknow.dao.RoleDao;
import ciknow.domain.Node;
import ciknow.domain.Role;
import dbunit.Dbunit;
import dbunit.DbunitTestExecutionListener;

@TestExecutionListeners(DbunitTestExecutionListener.class)
//@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)
public class RoleHibernateDaoTest extends AbstractHibernateDaoTest {
	
	
	@Autowired private NodeDao nodeDao;
	@Autowired private RoleDao roleDao;
	
	private static final Logger logger = Logger
			.getLogger(RoleHibernateDaoTest.class.getName());
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void save(){
		
		Node node1 = nodeDao.findById(1L);
		Node node2 = nodeDao.findById(2L);
		
		Role r = new Role();
		r.setName("Test_Role");
		r.setVersion(1L);
		
		Set<Node> nodes = new HashSet<Node>();
		nodes.add(node1);
		nodes.add(node2);
		r.setNodes(nodes);
		
		roleDao.save(r);
		
		List<Role> rolls = roleDao.getAll();
		Role roleFromDB = rolls.get(rolls.size()-1);
		
		Assert.assertEquals(4, rolls.size());
		Assert.assertEquals(r.getName(), roleFromDB.getName());
	
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void saveCollection(){
		
		ArrayList<Role> roles = new ArrayList<Role>();
		
		Node node1 = nodeDao.findById(1L);
		Node node2 = nodeDao.findById(2L);
		
		Role r = new Role();
		r.setName("Test_Role");
		r.setVersion(1L);
		
		Set<Node> nodes = new HashSet<Node>();
		nodes.add(node1);
		nodes.add(node2);
		r.setNodes(nodes);
		
		roles.add(r);
		
		Role r2 = new Role();
		r2.setName("Test_Role_2");
		r2.setVersion(1L);

		Set<Node> nodes2 = new HashSet<Node>();
		nodes2.add(node1);
		r2.setNodes(nodes);
		
		roles.add(r2);
		roleDao.save(roles);
		
		List<Role> rolls = roleDao.getAll();
		Assert.assertEquals(5, rolls.size());
		
		Role roleFromDB = rolls.get(rolls.size()-2);
		Assert.assertEquals(r.getName(), roleFromDB.getName());
		
		roleFromDB = rolls.get(rolls.size()-1);
		Assert.assertEquals(r2.getName(), roleFromDB.getName());
		
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void delete(){
	
		Node node1 = nodeDao.findById(1L);
		Node node2 = nodeDao.findById(2L);
		
		//create and save node with no reference to node_id table
		Role r = new Role();
		r.setName("Test_Role");
		r.setVersion(1L);
		Set<Node> nodes = new HashSet<Node>();
		nodes.add(node1);
		nodes.add(node2);
		r.setNodes(nodes);
		roleDao.save(r);
		
		roleDao.delete(r);
		List<Role> roles = roleDao.getAll();
		Assert.assertEquals(3, roles.size());
		
		//make sure the correct role was deleted
		Assert.assertTrue(!roles.contains(r));
		
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void deleteCollection(){
		
		//create and save node with no reference to node_id table
		ArrayList<Role> roles = new ArrayList<Role>();
		
		Node node1 = nodeDao.findById(1L);
		Node node2 = nodeDao.findById(2L);
		
		Role r = new Role();
		r.setName("Test_Role");
		r.setVersion(1L);
		
		Set<Node> nodes = new HashSet<Node>();
		nodes.add(node1);
		nodes.add(node2);
		r.setNodes(nodes);
		
		roles.add(r);
		
		Role r2 = new Role();
		r2.setName("Test_Role_2");
		r2.setVersion(1L);

		Set<Node> nodes2 = new HashSet<Node>();
		nodes2.add(node1);
		r2.setNodes(nodes);
		
		roles.add(r2);
		Assert.assertEquals(3, roleDao.getAll().size());
		roleDao.save(roles);
		Assert.assertEquals(5, roleDao.getAll().size());
		roleDao.delete(roles);
		Assert.assertEquals(3, roleDao.getAll().size());
		
		List<Role> rolesFromDB = roleDao.getAll();
		Assert.assertTrue(!rolesFromDB.contains(r));
		Assert.assertTrue(!rolesFromDB.contains(r2));
		
		
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void deleteAll(){
		
		//first remove the foreign key references
		List<Role> roles = roleDao.getAll();
		List<Node> nodes = nodeDao.loadAll();
		for(Role r: roles){
			for (Node node : nodes){
				node.getRoles().remove(r);
			}
		}
    	
		//logger.info("BEFORE nodeDao.save <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		nodeDao.save(nodes);
		//logger.info("After nodeDao.save <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		
		//logger.info("BEFORE nodeDao.getAll() <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		nodes = nodeDao.getAll(); 
		//logger.info("AFTER nodeDao.getAll() >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    	
		roleDao.deleteAll();
    	
    	Assert.assertEquals(0, roleDao.getAll().size());
		//logger.info("EXIT METHOD >>>>>>>>>>>>>>>>>>>>>");
    	
    	
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
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void getProxy2(){
		
		Role proxy = roleDao.getProxy(1L);
		Assert.assertEquals(new Long(1L), proxy.getId());
	
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void findById(){
		
		Role roleF = roleDao.findById(1L);
		Assert.assertEquals(new Long(1L), roleF.getId());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void loadById(){
		
		Role roleL = roleDao.loadById(1L);
		Assert.assertEquals(new Long(1L), roleL.getId());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void findByName(){
		
		Role roleF = roleDao.findByName("ROLE_ADMIN");
		
		Assert.assertEquals("ROLE_ADMIN", roleF.getName());
		
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void loadByName(){
		Role roleL = roleDao.loadByName("ROLE_ADMIN");
		
		Assert.assertEquals("ROLE_ADMIN", roleL.getName());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void getNodeIdsByRoleId(){
		
		List<Long> ids = roleDao.getNodeIdsByRoleId(2L);
		
		Assert.assertEquals(new Long(1L), ids.get(0));
		Assert.assertEquals(new Long(2L), ids.get(1));
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void updateNodesInRole(){
		
		Role r2 = roleDao.loadById(2L);
		List<Long> ids = roleDao.getNodeIdsByRoleId(2L);
		Assert.assertEquals(2, ids.size());
		Assert.assertEquals(new Long(1L), ids.get(0));
		Assert.assertEquals(new Long(2L), ids.get(1));
		
		ArrayList<Long> nodeIds = new ArrayList<Long>();
		nodeIds.add(new Long(2L));
		roleDao.updateNodesInRole(r2.getId(), nodeIds);
		
		roleDao.getNodeIdsByRoleId(2L);
		ids = roleDao.getNodeIdsByRoleId(2L);
		Assert.assertEquals(1, ids.size());	
		Assert.assertEquals(new Long(2L), ids.get(0));
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/roles.xml")
	public void getAll(){
		
		List<Role> roles = roleDao.getAll();
		Assert.assertEquals(3, roles.size());
		
		Assert.assertEquals(new Long(1L), roles.get(0).getId());
		Assert.assertEquals("ROLE_ADMIN", roles.get(0).getName());
		Assert.assertEquals(new Long(1L), roles.get(0).getVersion());
		
		Assert.assertEquals(new Long(2L), roles.get(1).getId());
		Assert.assertEquals("ROLE_HIDDEN", roles.get(1).getName());
		Assert.assertEquals(new Long(1L), roles.get(1).getVersion());
		
		Assert.assertEquals(new Long(3L), roles.get(2).getId());
		Assert.assertEquals("ROLE_USER", roles.get(2).getName());
		Assert.assertEquals(new Long(1L), roles.get(2).getVersion());
		
		
	}
	 

}
