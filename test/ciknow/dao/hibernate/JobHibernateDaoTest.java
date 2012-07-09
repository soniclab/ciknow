package ciknow.dao.hibernate;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

import ciknow.dao.JobDao;
import ciknow.domain.Job;
import ciknow.jobs.Cron;
import ciknow.util.GeneralUtil;
import dbunit.Dbunit;
import dbunit.DbunitTestExecutionListener;

@TestExecutionListeners(DbunitTestExecutionListener.class)
//@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)
public class JobHibernateDaoTest extends AbstractHibernateDaoTest {
	
	@Autowired
	private JobDao jobDao;
	
	private static final Logger logger = Logger
			.getLogger(JobHibernateDaoTest.class.getName());
	
	@BeforeTransaction
	public void verifyInitialDatabaseState(){
		//logger.info("verify initial state");
	}
	
	@Before
	public void setup(){
		//logger.info("setting up...");
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/jobs.xml")
	public void getAll(){
		List<Job> jobs = jobDao.getAll();
		assertEquals("query must show the same number of jobs", super.countRowsInTable("jobs"), jobs.size());
		
		
		Job job = jobs.get(0);
		assertEquals("Test_Job_1", job.getName());
		
		job = jobs.get(5);
		assertEquals("Test_Job_6", job.getName());
		
		
	}
	
	@Test
	public void save(){
		
		Job job = createJob("Test_Job_100");
		jobDao.save(job);
		
		Job jobFromDB = jobDao.getByName("Test_Job_100");
		compareJobs(job, jobFromDB);
	}
	
	@Test
	public void saveCollections(){
		
		ArrayList<Job> jobList = new ArrayList<Job>();
		Job job10 = createJob("Test_Job_10");
		jobList.add(job10);
		Job job11 = createJob("Test_Job_11");
		jobList.add(job11);
		Job job12 = createJob("Test_Job_12");
		jobList.add(job12);
		Job job13 = createJob("Test_Job_13");
		jobList.add(job13);
		Job job14 = createJob("Test_Job_14");
		jobList.add(job14);
		
		int beforeInsert = super.countRowsInTable("jobs");
		jobDao.save(jobList);
		int afterInsert = super.countRowsInTable("jobs");
		
		Assert.assertEquals(beforeInsert+5, afterInsert);
		
		
		compareJobs(job10, jobDao.getByName("Test_Job_10"));
		compareJobs(job11, jobDao.getByName("Test_Job_11"));
		compareJobs(job12, jobDao.getByName("Test_Job_12"));
		compareJobs(job13, jobDao.getByName("Test_Job_13"));
		compareJobs(job14, jobDao.getByName("Test_Job_14"));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/jobs.xml")
	public void delete(){
		
		Assert.assertEquals(6, jobDao.getAll().size());
		
		Job job = jobDao.getById(1L);
		jobDao.delete(job);
		Assert.assertEquals(5, jobDao.getAll().size());
		
		Assert.assertTrue(!jobDao.getAll().contains(job));
	
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/jobs.xml")
	public void deleteCollections(){
		
		Assert.assertEquals(6, jobDao.getAll().size());
		
		ArrayList<Job> jobList = new ArrayList<Job>();
		jobList.add(jobDao.getById(2L));
		jobList.add(jobDao.getById(4L));
		jobList.add(jobDao.getById(6L));
		
		jobDao.delete(jobList);
		
		Assert.assertEquals(3, jobDao.getAll().size());
		
		Assert.assertNull(jobDao.getById(2L));
		Assert.assertNull(jobDao.getById(4L));
		Assert.assertNull(jobDao.getById(6L));
		Assert.assertNotNull(jobDao.getById(1L));
		Assert.assertNotNull(jobDao.getById(3L));
		Assert.assertNotNull(jobDao.getById(5L));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/jobs.xml")
	public void deleteAll(){
		
		Assert.assertEquals(6, jobDao.getAll().size());
		
		Assert.assertNotNull(jobDao.getById(1L));
		Assert.assertNotNull(jobDao.getById(2L));
		Assert.assertNotNull(jobDao.getById(3L));
		Assert.assertNotNull(jobDao.getById(4L));
		Assert.assertNotNull(jobDao.getById(5L));
		Assert.assertNotNull(jobDao.getById(6L));
		
		jobDao.deleteAll();
		Assert.assertEquals(0, jobDao.getAll().size());
		
		Assert.assertNull(jobDao.getById(1L));
		Assert.assertNull(jobDao.getById(2L));
		Assert.assertNull(jobDao.getById(3L));
		Assert.assertNull(jobDao.getById(4L));
		Assert.assertNull(jobDao.getById(5L));
		Assert.assertNull(jobDao.getById(6L));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/jobs.xml")
	public void getCount(){
		Assert.assertEquals(6, jobDao.getCount());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/jobs.xml")
	public void getById(){
	
		Job job = jobDao.getById(3L);
		Assert.assertEquals(new Long(0L), job.getVersion());
		Assert.assertEquals("Test_Job_3", job.getName());
		Assert.assertEquals("testType", job.getType());
		Assert.assertEquals("minutely", job.getScheduledRuntime());
		Assert.assertEquals("beanName", job.getBeanName());
		Assert.assertEquals("ciknow.beanClass", job.getClassName());
		Assert.assertEquals("testMethod", job.getMethodName());
		Assert.assertFalse(job.getEnabled());
		
		
	}
	
	@Test
	public void getByName(){
		
		Job job = createJob("Test_Job_100");
		jobDao.save(job);
		
		Job jobFromDB = jobDao.getByName("Test_Job_100");
		compareJobs(job, jobFromDB);
	}
	
	@Test
	public void getByScheduledRuntime(){
		
		Job job = createJob("Test_Job_100");
		jobDao.save(job);
		
		List<Job> jobsFromDB = jobDao.getByScheduledRuntime(job.getScheduledRuntime());
		compareJobs(job, jobsFromDB.get(0));
		
	}
	
	
	private void compareJobs(Job expected, Job actual){
		
		Assert.assertEquals(expected.getVersion(), actual.getVersion());
		Assert.assertEquals(expected.getName(), actual.getName());
		Assert.assertEquals(expected.getType(), actual.getType());
		Assert.assertEquals(expected.getScheduledRuntime(), actual.getScheduledRuntime());
		Assert.assertEquals(expected.getBeanName(), actual.getBeanName());
		Assert.assertEquals(expected.getClassName(), actual.getClassName());
		Assert.assertEquals(expected.getMethodName(), actual.getMethodName());
		
		
		Assert.assertEquals(expected.getEnabled(), actual.getEnabled());
		Assert.assertEquals(expected.getCreateTS(), actual.getCreateTS());
		Assert.assertEquals(expected.getLastRunTS(), actual.getLastRunTS());
		
		byte[] paramTypesExpected = expected.getParameterTypes();
		byte[] paramTypesActual = actual.getParameterTypes();
		Assert.assertEquals(paramTypesExpected.length, paramTypesActual.length);
		for(int i=0; i<paramTypesExpected.length; i++){
			Assert.assertEquals(paramTypesExpected[i], paramTypesActual[i]);
		}
		
		
		byte[] paramValuesExpected = expected.getParameterTypes();
		byte[] paramValuesActual = actual.getParameterTypes();
		Assert.assertEquals(paramValuesExpected.length, paramValuesActual.length);
		for(int i=0; i<paramValuesExpected.length; i++){
			Assert.assertEquals(paramValuesExpected[i], paramValuesActual[i]);
		}
		
		
	}
	
	
	private Job createJob(String name){
		List<String> nodeTypes = new ArrayList<String>();
		nodeTypes.add("user");
		String shortName = "Discipline";
		String fieldName = "";
		Map data = new HashMap();
		data.put("nodeTypes", nodeTypes);
		data.put("shortName", shortName);
		data.put("fieldName", fieldName);
		
		// prepare metadata
		Job job = new Job();
		job.setName(name);
		job.setBeanName("edgeRO");
		job.setCreator("admin");
		job.setClassName("ciknow.ro.EdgeRO");
		job.setMethodName("deriveEdgesByAttribute");	
		job.setType("deriveEdgeByAttribute");
		job.setScheduledRuntime(Cron.MINUTELY);
		//Class[] pTypes = new Class[]{List.class, String.class, String.class};
		Class[] pTypes = new Class[]{Map.class};
		try {
			job.setParameterTypes(GeneralUtil.objectToByteArray(pTypes));
		} catch (IOException e) {
			e.printStackTrace();
		}		
		//Object[] pValues = new Object[]{nodeTypes, shortName, fieldName};
		Object[] pValues = new Object[]{data};
		try {
			job.setParameterValues(GeneralUtil.objectToByteArray(pValues));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return job;
		
	}
}
