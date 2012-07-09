package ciknow.dto;

import ciknow.domain.Field;

public class FieldDTO {
	public String name;
	public String label;
	
	public FieldDTO(){
		
	}
	
	public FieldDTO(Field f){
		name = f.getName();
		label = f.getLabel();
	}

	/*
    public Field toField(){
        Field f = new Field();
        f.setName(name);
        f.setLabel(label);
        return f;
    }
    */
}
