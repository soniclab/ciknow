package ciknow.domain;

/**
 * 
 * @author gyao
 */
public class TextField implements java.io.Serializable {

    private static final long serialVersionUID = 4642566106737864290L;
    private Long id;
    private Long version;
    private String name;
    private Boolean large;
    private String label;
    private Question question;

    public TextField() {
    }

    public TextField(TextField tf) {
    	this.name = tf.name;
    	this.label = tf.label;
    	this.large = tf.large;
    }
    
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
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getLarge() {
        return this.large;
    }

    public void setLarge(Boolean large) {
        this.large = large;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public int getIndex() {
        return question.getTextFields().indexOf(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TextField other = (TextField) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TextField[");
        sb.append("name: ").append(this.name).append(",");
        sb.append("label: ").append(this.label).append(",");
        sb.append("large: ").append(this.large).append("]");
        return sb.toString();
    }
}
