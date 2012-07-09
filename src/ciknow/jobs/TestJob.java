package ciknow.jobs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.JobDao;
import ciknow.domain.Job;
import ciknow.ro.EdgeRO;
import ciknow.util.Beans;
import ciknow.util.GeneralUtil;

public class TestJob {
	private static Log logger = LogFactory.getLog(TestJob.class);
	
	public static void main(String[] args) throws Exception{
		Beans.init();
		JobDao jobDao = (JobDao)Beans.getBean("jobDao");
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
		job.setName("testname3");
		job.setBeanName("edgeRO");
		job.setCreator("admin");
		job.setClassName("ciknow.ro.EdgeRO");
		job.setMethodName("deriveEdgesByAttribute");	
		job.setType("deriveEdgeByAttribute");
		job.setScheduledRuntime(Cron.MINUTELY);
		//Class[] pTypes = new Class[]{List.class, String.class, String.class};
		Class[] pTypes = new Class[]{Map.class};
		job.setParameterTypes(GeneralUtil.objectToByteArray(pTypes));		
		//Object[] pValues = new Object[]{nodeTypes, shortName, fieldName};
		Object[] pValues = new Object[]{data};
		job.setParameterValues(GeneralUtil.objectToByteArray(pValues));
		jobDao.save(job);
		
		// use metadata
		job = jobDao.getByName("testname3");
		Class cl = Class.forName(job.getClassName());
		Method method = cl.getMethod(job.getMethodName(), (Class[])GeneralUtil.byteArrayToObject(job.getParameterTypes()));
		Integer count = (Integer) method.invoke(Beans.getBean(job.getBeanName()), (Object[])GeneralUtil.byteArrayToObject(job.getParameterValues()));
		logger.info(count + " edges are derived.");
	}
	

}
