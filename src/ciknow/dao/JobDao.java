package ciknow.dao;

import java.util.Collection;
import java.util.List;

import ciknow.domain.Job;

public interface JobDao {
	public void save(Job job);
	public void save(Collection<Job> jobs);
	public void delete(Job job);
	public void delete(Collection<Job> jobs);
	public void deleteAll();
	
	public List<Job> getAll();
	public int getCount();
	public Job getById(Long id);
	public Job getByName(String name);
	public List<Job> getByScheduledRuntime(String runtime);
}
