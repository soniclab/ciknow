package ciknow.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author gyao
 */
public class Page implements java.io.Serializable {

    private static final long serialVersionUID = 8226550138813341275L;
    private Long id;
    private Long version;
    private Survey survey;
    private String name;
    private String label;
    private String instruction;
    private Map<String, String> attributes = new HashMap<String, String>();
    private List<Question> questions = new ArrayList<Question>(0);

    public Page(){
    	
    }
    
    public Page(Page page){
    	this.survey = page.survey;
    	this.name = page.name;
    	this.label = page.label;
    	this.instruction = page.instruction;
    	this.attributes = new HashMap<String, String>(page.attributes);
    	for (Question question : page.questions){
    		Question newQuestion = new Question(question);
    		newQuestion.setPage(this);
    		questions.add(newQuestion);
    	}
    }
    
    public boolean isVisible(Node node){
    	if (node.isAdmin()) return true;
    	for (Question question : questions){
    		if (question.isVisible(node)) return true;
    	}
    	return false;
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

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public Integer getIndex() {
        return survey.getPages().indexOf(this);
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

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Page other = (Page) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
    
    
}
