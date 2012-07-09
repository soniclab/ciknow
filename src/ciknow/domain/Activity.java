package ciknow.domain;

import java.util.Date;

public class Activity implements java.io.Serializable {
	private static final long serialVersionUID = -8222885912839117084L;
	
	private Long id;
	private Long version;
	private Node subject;
	private String predicate;
	private Node object;
	private Date timestamp;
	
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
	public Node getSubject() {
		return subject;
	}
	public void setSubject(Node subject) {
		this.subject = subject;
	}
	public String getPredicate() {
		return predicate;
	}
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	public Node getObject() {
		return object;
	}
	public void setObject(Node object) {
		this.object = object;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
        sb.append("Activity(id=").append(id).append(", ");
        sb.append("version=").append(version).append(", ");
        sb.append("subjectId=").append(subject.getId()).append(", ");
        sb.append("predicate=").append(predicate).append(", ");
        sb.append("objectId=").append(object == null?"null":object.getId()).append(", ");        
        sb.append("timestamp=").append(timestamp);        
        sb.append("])");	
		return sb.toString();
	}
}
