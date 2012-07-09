package ciknow.domain;

/**
 * 
 * @author gyao
 */
public class Scale implements java.io.Serializable {

    private static final long serialVersionUID = -2471614448510623428L;
    private Long id;
    private Long version;
    private String name;
    private String label;
    private Double value;
    private Question question;

    public Scale() {
    }

    public Scale(Scale s) {
    	this.name = s.name;
    	this.label = s.label;
    	this.value = s.value;
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public int getIndex() {
        return question.getScales().indexOf(this);
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
        final Scale other = (Scale) obj;
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
        sb.append("Scale[");
        sb.append("name: ").append(this.name).append(", ");
        sb.append("label: ").append(this.label).append("]");
        return sb.toString();
    }
}
