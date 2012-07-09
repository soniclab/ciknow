package ciknow.dto;

import java.util.*;

import ciknow.domain.Survey;

public class SurveyDTO {
	public Long surveyId;
	public Long version;
	public Long designerId;
	public String name;
	public String description;
	public Date timestamp;
    public Map<String, String> attributes = new HashMap<String, String>();
    public Map<String, String> longAttributes = new HashMap<String, String>();
/*	public Set<Long> questions = new HashSet<Long>(0);*/
	
	public SurveyDTO(){
		
	}
	
	public SurveyDTO(Survey s){
		surveyId = s.getId();
		version = s.getVersion();
		designerId = s.getDesigner().getId();
		name = s.getName();
		description = s.getDescription();
		timestamp =s.getTimestamp();
        attributes.putAll(s.getAttributes());
        longAttributes.putAll(s.getLongAttributes());
/*		for (Question q : s.getQuestions()){
			questions.add(q.getId());
		}*/
	}

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("SurveyDTO[surveyId=").append(surveyId).append(",");
        sb.append("version=").append(version).append(",");
        sb.append("designerId=").append(designerId).append(",");
        sb.append("name=").append(name).append(",");
        sb.append("description=").append(description).append(",");
        sb.append("timestamp=").append(timestamp).append(",");
        sb.append("attributes=").append(attributes).append("]");
        return sb.toString();
    }
}
