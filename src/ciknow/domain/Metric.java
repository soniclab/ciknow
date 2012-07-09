package ciknow.domain;

public class Metric implements java.io.Serializable {

	private static final long serialVersionUID = -6348138380534994880L;
	
	private Long id;
	private Node toNode;
	private Node fromNode;
	private String type;
	private String source;
	private Boolean symmetric;
	private Float value;
	
//	private Float pearson;
//	private Float cosine;
//	private Float euclidean;
//	private Float seuclidean;
//	private Float pmatch;
//	private Float spmatch;

	public Metric() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public Node getToNode() {
		return toNode;
	}

	public void setToNode(Node toNode) {
		this.toNode = toNode;
	}

	public Node getFromNode() {
		return fromNode;
	}

	public void setFromNode(Node fromNode) {
		this.fromNode = fromNode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Boolean getSymmetric() {
		return symmetric;
	}

	public void setSymmetric(Boolean symmetric) {
		this.symmetric = symmetric;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("RecSimilarityMetric[");
		sb.append("id=").append(id).append(", ");
		sb.append("fromNode=").append(fromNode.getId()).append(", ");
		sb.append("toNode=").append(toNode.getId()).append(", ");
		sb.append("type=").append(type).append(", ");
		sb.append("source=").append(source).append(", ");
		sb.append("symmetric=").append(symmetric).append(", ");
		sb.append("value=").append(value);
		sb.append("]\n");
		return sb.toString();
	}
}
