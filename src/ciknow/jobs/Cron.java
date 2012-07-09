package ciknow.jobs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.JobDao;
import ciknow.domain.Job;
import ciknow.mail.Mailer;
import ciknow.util.Beans;
import ciknow.util.GeneralUtil;

public class Cron {
	private static Log logger = LogFactory.getLog(Cron.class);
	
	public static final String MINUTELY = "minutely";
	public static final String HOURLY = "hourly";	
	public static final String DAILY = "daily";
	public static final String WEEKLY = "weekly";
	public static final String MONTHLY = "monthly";
	public static final String YEARLY = "yearly";	
	
	private JobDao jobDao;
	private Mailer mailer;
	
	public JobDao getJobDao() {
		return jobDao;
	}

	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
	}

	public Mailer getMailer() {
		return mailer;
	}

	public void setMailer(Mailer mailer) {
		this.mailer = mailer;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doMinutely() throws SecurityException, NoSuchMethodException, IOException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{		
		List<Job> jobs = jobDao.getByScheduledRuntime(MINUTELY);
		if (jobs.isEmpty()) return;
		
		logger.info("starting scheduled minutely jobs...");		
		for (Job job : jobs){
			logger.info("job: " + job.getName());
			Class cl = Class.forName(job.getClassName());
			Method method = cl.getMethod(job.getMethodName(), (Class[])GeneralUtil.byteArrayToObject(job.getParameterTypes()));
			method.invoke(Beans.getBean(job.getBeanName()), (Object[])GeneralUtil.byteArrayToObject(job.getParameterValues()));
			job.setLastRunTS(new Date());
		}
		
		jobDao.save(jobs);			
		logger.info("finish minutely jobs.");
		mailer.send("no-reply@northwestern.edu", "yao.gyao@gmail.com", "Scheduled Minutely Jobs Executed.", "FYI");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doHourly() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		List<Job> jobs = jobDao.getByScheduledRuntime(HOURLY);
		if (jobs.isEmpty()) return;
		
		logger.info("starting scheduled hourly jobs...");
		for (Job job : jobs){
			logger.info("job: " + job.getName());
			Class cl = Class.forName(job.getClassName());
			Method method = cl.getMethod(job.getMethodName(), (Class[])GeneralUtil.byteArrayToObject(job.getParameterTypes()));
			method.invoke(Beans.getBean(job.getBeanName()), (Object[])GeneralUtil.byteArrayToObject(job.getParameterValues()));
			job.setLastRunTS(new Date());
		}
		
		jobDao.save(jobs);
		logger.info("finish hourly jobs.");
		mailer.send("no-reply@northwestern.edu", "yao.gyao@gmail.com", "Scheduled Hourly Jobs Executed.", "FYI");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doDaily() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		List<Job> jobs = jobDao.getByScheduledRuntime(DAILY);
		if (jobs.isEmpty()) return;
		
		logger.info("starting scheduled daily jobs...");
		for (Job job : jobs){
			logger.info("job: " + job.getName());
			Class cl = Class.forName(job.getClassName());
			Method method = cl.getMethod(job.getMethodName(), (Class[])GeneralUtil.byteArrayToObject(job.getParameterTypes()));
			method.invoke(Beans.getBean(job.getBeanName()), (Object[])GeneralUtil.byteArrayToObject(job.getParameterValues()));
			job.setLastRunTS(new Date());
		}
		
		jobDao.save(jobs);
		logger.info("finish daily jobs.");
		mailer.send("no-reply@northwestern.edu", "yao.gyao@gmail.com", "Scheduled Daily Jobs Executed.", "FYI");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doWeekly() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		List<Job> jobs = jobDao.getByScheduledRuntime(WEEKLY);
		if (jobs.isEmpty()) return;
		
		logger.info("starting scheduled weekly jobs...");
		for (Job job : jobs){
			logger.info("job: " + job.getName());
			Class cl = Class.forName(job.getClassName());
			Method method = cl.getMethod(job.getMethodName(), (Class[])GeneralUtil.byteArrayToObject(job.getParameterTypes()));
			method.invoke(Beans.getBean(job.getBeanName()), (Object[])GeneralUtil.byteArrayToObject(job.getParameterValues()));
			job.setLastRunTS(new Date());
		}
		
		jobDao.save(jobs);
		logger.info("finish weekly jobs.");
		mailer.send("no-reply@northwestern.edu", "yao.gyao@gmail.com", "Scheduled Weekly Jobs Executed.", "FYI");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doMonthly() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		List<Job> jobs = jobDao.getByScheduledRuntime(MONTHLY);		
		if (jobs.isEmpty()) return;
		
		logger.info("starting scheduled monthly jobs...");
		for (Job job : jobs){
			logger.info("job: " + job.getName());
			Class cl = Class.forName(job.getClassName());
			Method method = cl.getMethod(job.getMethodName(), (Class[])GeneralUtil.byteArrayToObject(job.getParameterTypes()));
			method.invoke(Beans.getBean(job.getBeanName()), (Object[])GeneralUtil.byteArrayToObject(job.getParameterValues()));
			job.setLastRunTS(new Date());
		}
		
		jobDao.save(jobs);
		logger.info("finish monthly jobs.");
		mailer.send("no-reply@northwestern.edu", "yao.gyao@gmail.com", "Scheduled Monthly Jobs Executed.", "FYI");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doYearly() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		List<Job> jobs = jobDao.getByScheduledRuntime(YEARLY);
		if (jobs.isEmpty()) return;
		
		logger.info("starting scheduled yearly jobs...");
		for (Job job : jobs){
			logger.info("job: " + job.getName());
			Class cl = Class.forName(job.getClassName());
			Method method = cl.getMethod(job.getMethodName(), (Class[])GeneralUtil.byteArrayToObject(job.getParameterTypes()));
			method.invoke(Beans.getBean(job.getBeanName()), (Object[])GeneralUtil.byteArrayToObject(job.getParameterValues()));
			job.setLastRunTS(new Date());
		}
		
		jobDao.save(jobs);
		logger.info("finish yearly jobs.");		
		mailer.send("no-reply@northwestern.edu", "yao.gyao@gmail.com", "Scheduled Yearly Jobs Executed.", "FYI");
	}
	
}
