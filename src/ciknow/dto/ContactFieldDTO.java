package ciknow.dto;

import ciknow.domain.ContactField;

public class ContactFieldDTO {
    public String name;
    public String label;
	
	public ContactFieldDTO(){
		
	}
	
	public ContactFieldDTO(ContactField c){
        name = c.getName();
        label = c.getLabel();
	}
	
	/*
	public ContactField toContactField(){
		ContactField cf = new ContactField();
		cf.setName(name);
		cf.setLabel(label);
		return cf;
	}
	*/
}
