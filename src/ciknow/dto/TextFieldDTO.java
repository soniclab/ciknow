package ciknow.dto;

import ciknow.domain.TextField;

public class TextFieldDTO {
	public String name;
	public Boolean large;
	public String label;
	
	public TextFieldDTO(){
		
	}
	
	public TextFieldDTO(TextField t){
		name = t.getName();
		large = t.getLarge();
		label = t.getLabel();
	}

	/*
    public TextField toTextField(){
        TextField tf = new TextField();
        tf.setLarge(large);
        tf.setName(name);
        tf.setLabel(label);
        return tf;
    }
	*/
	
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("TextFieldDTO[");
        sb.append("name=").append(name).append(",");
        sb.append("label=").append(label).append(",");
        sb.append("large=").append(large).append("]");
        return sb.toString();
    }
}
