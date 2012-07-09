package ciknow.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import ciknow.domain.ContactField;
import ciknow.domain.Field;
import ciknow.domain.Group;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import ciknow.domain.TextField;

public class QuestionDTO {
	public Long questionId;
	public Long version;
	public Long pageId;
	public String label;
	public String shortName;
	public String type;
	public String instruction;
	public String htmlInstruction;
	public Integer rowPerPage;
    public Map<String, String> attributes = new HashMap<String, String>();
    public Map<String, String> longAttributes = new HashMap<String, String>();
    
	public Set<Long> visibleGroups = new HashSet<Long>(); // this question is visible to groups of nodes
	public Set<Long> availableGroups = new HashSet<Long>(); // groups of nodes are available in relational question
	public Set<Long> availableGroups2 = new HashSet<Long>(); // groups of nodes in the columns of the relational matrix
	
	public List<ContactFieldDTO> contactFields = new ArrayList<ContactFieldDTO>();
    public List<FieldDTO> fields = new ArrayList<FieldDTO>();
	public List<ScaleDTO> scales = new ArrayList<ScaleDTO>();	
	public List<TextFieldDTO> textFields = new ArrayList<TextFieldDTO>();
	
	public QuestionDTO(){
		
	}
	
	public QuestionDTO(Question q){
		questionId = q.getId();
		version = q.getVersion();
		pageId = q.getPage().getId();
		label = q.getLabel();
		shortName = q.getShortName();
		type = q.getType();
		instruction = q.getInstruction();
		htmlInstruction = q.getHtmlInstruction();
		rowPerPage = q.getRowPerPage();
		attributes.putAll(q.getAttributes());
		longAttributes.putAll(q.getLongAttributes());
		
        for (Field f : q.getFields()){
			fields.add(new FieldDTO(f));
		}
		for (Scale s : q.getScales()){
			scales.add(new ScaleDTO(s));
		}
		for (TextField t : q.getTextFields()){
			textFields.add(new TextFieldDTO(t));
		}
		for (ContactField c : q.getContactFields()){
			contactFields.add(new ContactFieldDTO(c));
		}
		
		for (Group group : q.getVisibleGroups()){
			visibleGroups.add(group.getId());
		}
		for (Group group : q.getAvailableGroups()){
			availableGroups.add(group.getId());
		}
		for (Group group : q.getAvailableGroups2()){
			availableGroups2.add(group.getId());
		}

    }
}
