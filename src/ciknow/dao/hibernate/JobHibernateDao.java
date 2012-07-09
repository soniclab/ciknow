package ciknow.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ciknow.dao.JobDao;
import ciknow.domain.Job;

public class JobHibernateDao extends HibernateDaoSupport implements JobDao{
	
	@Override
	public void delete(Job job) {
		getHibernateTemplate().delete(job);
	}

	@Override
	public void delete(Collection<Job> jobs) {
		getHibernateTemplate().deleteAll(jobs);
	}

	@Override
	public void deleteAll() {
		getHibernateTemplate().bulkUpdate("delete Job");
	}

	@Override
	public List<Job> getAll() {
		List<Job> jobs = getHibernateTemplate().loadAll(Job.class);
		return jobs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Job getById(Long id) {
		String query = "from Job j where j.id = ?";
		List<Job> jobs = getHibernateTemplate().find(query, id);
		if (jobs.isEmpty()) return null;
		else return jobs.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Job getByName(String name) {
		String query = "from Job j where j.name = ?";
		List<Job> jobs = getHibernateTemplate().find(query, name);
		if (jobs.isEmpty()) return null;
		else return jobs.get(0);
	}

	@Override
	public int getCount() {
		String query = "select count(*) from Job";
		return (Integer)getHibernateTemplate().find(query).get(0);
	}

	@Override
	public void save(Job job) {
		getHibernateTemplate().saveOrUpdate(job);
	}

	@Override
	public void save(Collection<Job> jobs) {
		getHibernateTemplate().saveOrUpdateAll(jobs);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Job> getByScheduledRuntime(String runtime) {
		String query = "from Job j where j.scheduledRuntime = ?";
		List<Job> jobs = getHibernateTemplate().find(query, runtime);
		return jobs;
	}

}
