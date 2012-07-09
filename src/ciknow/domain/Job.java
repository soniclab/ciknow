package ciknow.domain;

import java.util.Date;

public class Job {	
	private Long id;
	private Long version;
	private String name;
	private String type;
	private String creator;
	private String scheduledRuntime; // daily, weekly, monthly, etc
	private String description;
	private String beanName; // spring bean name
	private String className;
	private String methodName;
	private byte[] parameterTypes;
	private byte[] parameterValues;
	
	private Boolean enabled = true;
	private Date createTS = new Date();
	private Date lastRunTS;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}			
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getScheduledRuntime() {
		return scheduledRuntime;
	}
	public void setScheduledRuntime(String scheduledRuntime) {
		this.scheduledRuntime = scheduledRuntime;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getBeanName() {
		return beanName;
	}
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public byte[] getParameterTypes() {
		return parameterTypes;
	}
	public void setParameterTypes(byte[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	public byte[] getParameterValues() {
		return parameterValues;
	}
	public void setParameterValues(byte[] parameterValues) {
		this.parameterValues = parameterValues;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public Date getCreateTS() {
		return createTS;
	}
	public void setCreateTS(Date createTS) {
		this.createTS = createTS;
	}
	public Date getLastRunTS() {
		return lastRunTS;
	}
	public void setLastRunTS(Date lastRunTS) {
		this.lastRunTS = lastRunTS;
	}
	@Override
	public String toString() {
		return "Job [name=" + name + ", type=" + type + ", creator=" + creator
				+ ", description=" + description + ", scheduledRuntime="
				+ scheduledRuntime + ", createTS=" + createTS + ", lastRunTS="
				+ lastRunTS + ", enabled=" + enabled + ", className="
				+ className + ", methodName=" + methodName + ", beanName="
				+ beanName + "]";
	}	
	
	
}
