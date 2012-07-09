package ciknow.domain;

/**
 *
 * @author gyao
 */
public class ContactField implements java.io.Serializable {

    private static final long serialVersionUID = 122839354445204079L;
    private Long id;
    private Long version;
    private String name;
    private String label;
    private Question question;

    public ContactField() {
    }

    public ContactField(ContactField cf) {
    	this.name = cf.name;
    	this.label = cf.label;
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
        return name;
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

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public int getIndex() {
        return question.getContactFields().indexOf(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContactField that = (ContactField) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ContactField[");
        sb.append("name: ").append(this.name).append(", ");
        sb.append("label: ").append(this.label).append("]");
        return sb.toString();
    }
}
