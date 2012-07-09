package ciknow.domain;


public class Recommendation implements java.io.Serializable {

	private static final long serialVersionUID = 8984361753811721096L;
	private Node user;
	private Node target;
	private Double identifyScore;
	private String idMetricType = "unknown";
	private Double selectScore;
	private String seMetricType = "unknown";
	private Double finalScore;

	public Recommendation() {
		identifyScore = 0.00;
		selectScore = 0.00;
		finalScore = 0.00;
	}

	public Node getUser() {
		return user;
	}

	public void setUser(Node user) {
		this.user = user;
	}

	public Node getTarget() {
		return target;
	}

	public void setTarget(Node target) {
		this.target = target;
	}

	public Double getIdentifyScore() {
		return identifyScore;
	}

	public void setIdentifyScore(Double identifyScore) {
		this.identifyScore = identifyScore;
	}

	public Double getSelectScore() {
		return selectScore;
	}

	public void setSelectScore(Double selectScore) {
		this.selectScore = selectScore;
	}

	public Double getFinalScore() {
		return finalScore;
	}

	public void setFinalScore(Double finalScore) {
		this.finalScore = finalScore;
	}

	public String getIdMetricType() {
		return idMetricType;
	}

	public void setIdMetricType(String idMetricType) {
		this.idMetricType = idMetricType;
	}

	public String getSeMetricType() {
		return seMetricType;
	}

	public void setSeMetricType(String seMetricType) {
		this.seMetricType = seMetricType;
	}

	public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Recommendation[");
        sb.append("userId=").append(user.getId()).append(", ");
        sb.append("targetId=").append(target.getId()).append(", ");
        sb.append("identifyScore=").append(identifyScore).append(", ");
        sb.append("idMetricType=").append(idMetricType).append(", ");
        sb.append("selectScore=").append(selectScore).append(", ");
        sb.append("seMetricType=").append(seMetricType).append(", ");
        sb.append("finalScore=").append(finalScore).append("]");
        return sb.toString();
    }
}
