package ciknow.dto;

import ciknow.domain.Scale;

public class ScaleDTO {
	public String name;
	public String label;
	public Double value;
	
	public ScaleDTO(){
		
	}
	
	public ScaleDTO(Scale s){
		name = s.getName();
		label = s.getLabel();
		value = s.getValue();
	}

	/*
    public Scale toScale(){
        Scale s = new Scale();
        s.setName(name);
        s.setLabel(label);
        s.setValue(value);
        return s;
    }
    */
}
