package ciknow.dao;

import java.sql.*;
import java.util.List;
import java.net.URLConnection;
import javax.servlet.http.*;

public class GraphData implements java.io.Serializable
{
    // data members
    protected String name;
    protected String label;
    protected String data;
    protected String nw_type;
    protected String la_type;
    protected String emailAdress;
    protected String htmldata;
    protected String subject;
    protected String msg;
    protected String reply;
    protected String fileName; 
    protected List<Long> nodeIds;
    protected  List<String> edgeFTTypes;
    // constructors
    public GraphData()
    {
    }

    //  accessors
    public String getName()
    {
       
        return name;
    }

    public String getLabel()
    {
        return label;
    }

    public String getData()
    {
        return data;
    }

    public String getType()
    {
        return nw_type;
    }

    public String getLanguage()
    {
        return la_type;
    }

    public void setName(String _name){
       name = _name;
    }

    public void setLabel(String _label){
       label = _label;
    }

    public void setData(String _data){
       data = _data;
    }

    public void setType(String _type){
       nw_type = _type;
    }
    
  
    public void setLanguage(String _la_type){
    	la_type = _la_type;
     }

    public void setReply(String _reply){
    	reply= _reply;
     }

      public String getReply(){
        	return reply;

     }
    public String getEmailAd()
    {
        return emailAdress;
    }


  public void setEmailAd(String _emailAdress){
 	emailAdress = _emailAdress;
  }

  public String getHTMLData()
  {
      return htmldata;
  }

    public void setHTMLData(String _data){
     htmldata = _data;
  }
    
    public String getSub(){
    	return subject;
    	
    }
    
public String getMsg(){
    	return msg;
    }

public String getFileName(){
	return fileName;
}

public void setSub(String _sub){
	subject = _sub;
 }
public void setMsg(String _msg){
    msg = _msg;
 }
public void setFileName(String _file){
   fileName = _file;
 }

public List<Long> getNodeIds(){
	return nodeIds;
}

public void setNodeIds(List<Long> _nodeIds){
	nodeIds = _nodeIds;
 }
    public List<String> getEdgeFTTypes(){
	return edgeFTTypes;
}

public void setEdgeFTTypes(List<String> _edgeFTTypes){
	edgeFTTypes = _edgeFTTypes;
 }

}
