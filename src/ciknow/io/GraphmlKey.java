package ciknow.io;

public class GraphmlKey {
	private String id;
	private String domain;
	private String attrName;
	private String attrType;
	
	
	public GraphmlKey() {
	}
	
	
	public GraphmlKey(String id, String domain, String attrName, String attrType) {
		super();
		this.id = id;
		this.domain = domain;
		this.attrName = attrName;
		this.attrType = attrType;
	}


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getAttrName() {
		return attrName;
	}
	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}
	public String getAttrType() {
		return attrType;
	}
	public void setAttrType(String attrType) {
		this.attrType = attrType;
	}
}
