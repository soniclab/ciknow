package ciknow.dao.hibernate;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;

import ciknow.dao.ActivityDao;
import ciknow.dao.NodeDao;
import ciknow.domain.Activity;
import ciknow.domain.Node;
import dbunit.Dbunit;
import dbunit.DbunitTestExecutionListener;


@TestExecutionListeners(DbunitTestExecutionListener.class)
//@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)
public class ActivityHibernateDaoTest extends AbstractHibernateDaoTest{
	
	@Autowired private ActivityDao activityDao;
	@Autowired private NodeDao nodeDao;
	
	private static final Logger logger = Logger
			.getLogger(ActivityHibernateDaoTest.class.getName());

	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/activities.xml")
	public void save() {		
		Node fromNode = nodeDao.findById(1L);
		Node toNode = nodeDao.findById(2L);
	
		
		Activity a = new Activity();
		a.setSubject(fromNode);
		a.setPredicate("PREDICATE");
		a.setObject(toNode);
		Date testTimeStamp = new Date();
		a.setTimestamp(testTimeStamp);
		activityDao.save(a);
		
		Activity latest = activityDao.getLatestActivity();
		Assert.assertEquals("PREDICATE", latest.getPredicate());
		Assert.assertEquals(toNode, latest.getObject());
		Assert.assertEquals(fromNode, latest.getSubject());
		Assert.assertEquals(testTimeStamp, latest.getTimestamp());
		
		
		int size = 100;
		Random r = new Random();
		for (int i=0; i<size; i++){
			a = new Activity();
			a.setSubject(fromNode);
			a.setPredicate(r.nextBoolean()?"login":"logout");
			a.setObject(r.nextBoolean()?toNode:null);
			a.setTimestamp(new Date());
			activityDao.save(a);
		}
		//sf.getCurrentSession().flush();
		Assert.assertEquals("JDBC query must show the same number of activities", 
				super.countRowsInTable("activities"), activityDao.getAll().size());
		
		
		//Test for null subject
		a = new Activity();
		a.setSubject(null);
		a.setPredicate("Predicate");
		a.setObject(toNode);
		a.setTimestamp(new Date());
		try{
			activityDao.save(a);
			Assert.fail();
		}
		catch(Exception e){}
		
		//Test for null predicate
		a = new Activity();
		a.setSubject(fromNode);
		a.setPredicate(null);
		a.setObject(toNode);
		a.setTimestamp(new Date());
		try{
			activityDao.save(a);
			Assert.fail();
		}
		catch(Exception e){}
		
		//Test for null timestamp
		a = new Activity();
		a.setSubject(fromNode);
		a.setPredicate("Predicate");
		a.setObject(toNode);
		a.setTimestamp(null);
		try{
			activityDao.save(a);
			Assert.fail();
		}
		catch(Exception e){}
		
		
				
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/activities.xml")
	public void getLatestActivity(){
		Activity a = activityDao.getLatestActivity();
		Date timestamp = a.getTimestamp();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 5); // June
		cal.set(Calendar.DAY_OF_MONTH, 8);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Assert.assertTrue("Last activity should be after " + cal.getTime(), timestamp.after(cal.getTime()));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/activities.xml")
	public void getActivityBefore(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 4);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Collection<Activity> activities = activityDao.getActivitiesBefore(cal.getTime());
		Assert.assertEquals("There should be 5 activities before " + cal.getTime(), 5, activities.size());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/activities.xml")
	public void getActivityAfter(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 4);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Collection<Activity> activities = activityDao.getActivitiesAfter(cal.getTime());
		Assert.assertEquals("There should be 5 activities after " + cal.getTime(), 5, activities.size());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/activities.xml")
	public void getActivityBetween(){
		Calendar cal = Calendar.getInstance();
		
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		Date begin = cal.getTime();
		

		cal.set(Calendar.MONTH, 4);
		Date end = cal.getTime();
		
		
		Collection<Activity> activities = activityDao.getActivitiesBetween(begin, end);
		Assert.assertEquals("There should be 3 activities after " + begin + " and before " + end, 
				3, activities.size());
	}
	
	
	
}
