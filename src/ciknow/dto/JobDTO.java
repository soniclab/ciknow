package ciknow.dto;

import java.util.Date;

import ciknow.domain.Job;

public class JobDTO {
	public String name;
	public String type;
	public String creator;
	public String scheduledRuntime; // daily, weekly, monthly, etc
	public String description;
	/*
	public String beanName; // spring bean name
	public String className;
	public String methodName;
	public byte[] parameterTypes;
	public byte[] parameterValues;
	*/
	
	public Boolean enabled = true;
	public Date createTS = new Date();
	public Date lastRunTS = new Date();
	
	public JobDTO(){
		
	}
	
	public JobDTO(Job job){
		this.name = job.getName();
		this.type = job.getType();
		this.creator = job.getCreator();
		this.scheduledRuntime = job.getScheduledRuntime();
		this.description = job.getDescription();
		this.enabled = job.getEnabled();
		this.createTS = job.getCreateTS();
		this.lastRunTS = job.getLastRunTS();
	}
	
	public Job toJob(){
		Job j = new Job();
		j.setName(name);
		j.setCreator(creator);
		j.setType(type);
		j.setDescription(description);
		j.setScheduledRuntime(scheduledRuntime);
		j.setEnabled(enabled);
		j.setCreateTS(createTS);
		j.setLastRunTS(lastRunTS);
		return j;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("JobDTO[").append("\n");
		sb.append("name=").append(name).append("\n");
		sb.append("description=").append(description).append("\n");
		sb.append("type=").append(type).append("\n");
		sb.append("creator=").append(creator).append("\n");
		sb.append("scheduledRuntime=").append(scheduledRuntime).append("\n");
		sb.append("createTS=").append(createTS).append("\n");
		sb.append("lastRunTS=").append(lastRunTS).append("\n");
		sb.append("enabled=").append(enabled).append("\n");
		sb.append("]").append("\n");
		
		return sb.toString();
	}
}
