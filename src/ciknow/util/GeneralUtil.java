package ciknow.util;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Messagebox;

import ciknow.domain.*;
import ciknow.security.CIKNOWUserDetails;
import ciknow.util.compare.TeamAssemblyConfigComparator;

public class GeneralUtil {
	private static Log logger = LogFactory.getLog(GeneralUtil.class);
	
	public static void main(String[] args){
		try {
			//updateEdgeWeights();
			testXML();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void testXML(){
		List<Map<String, String>> eds = getEdgeDescriptions();
		logger.info(eds);
		
		Map<String, String> ed = new HashMap<String, String>();
		ed.put("type", "edge type");
		ed.put("label", "edge label");
		ed.put("verb", "edge verb");
		eds.add(ed);
		
		saveEdgeDescriptions(eds);
		eds = getEdgeDescriptions();
		logger.info(ed);
	}
	
	
	/****************************************************
	 * Character handling, escape
	 * **************************************************/	
	public static boolean isValidName(String name){
        if (name.isEmpty()){
        	Messagebox.show("Name is required.");
        	return false;
        }
        if (name.length() > 80){
        	Messagebox.show("Name cannot be longer than 80 characters");
        	return false;
        }
        if (GeneralUtil.containSpecialCharacter(name) 
        		|| name.contains(" ") 
        		|| name.contains(Constants.SEPERATOR)){
        	Messagebox.show(Labels.getLabel("invalid.name"));
        	return false;
        }
        return true;
	}
	
	public static boolean isValidLabel(String label){
        if (label.isEmpty()){
        	Messagebox.show("Label is required.");
        	return false;
        }
        if (label.length() > 255){
        	Messagebox.show("Label cannot be longer than 255 characters");
        	return false;
        }
        return true;
	}
	
	/*
	 * these characters cannot exist in file/directory names:
	 * 		/ \ * " < > : | ?
	 */
	private static String[] specialCharacters = {"/", "\\", "*", "\"", "<", ">", ":", "|", "?", ","};

	public static boolean containSpecialCharacter(String s){
		for (String c : specialCharacters){
			if (s.indexOf(c) >= 0) return true;
		}
		return false;
	}
	
	public static String replaceSpecialCharacter(String s, String r){
		for (String c : specialCharacters){
			s = s.replace(c, r);
		}
		return s;
	}
	
	public static String capitalizeInitial(String input){
		String head = input.substring(0, 1);
		String tail = input.substring(1);
		return head.toUpperCase() + tail;			
	}
	
	
	
	public static Map<Long, Node> getNodeMap(Collection<Node> nodes){
		Map<Long, Node> nodeMap = new HashMap<Long, Node>();
		for (Node node : nodes){
			nodeMap.put(node.getId(), node);
		}
		return nodeMap;
	}
	
	public static int measureString(String input, Font font, FontRenderContext fontRenderContext){		    
		FontRenderContext frc = fontRenderContext;     
		if (frc == null) {
			AffineTransform af = new AffineTransform();
			frc = new FontRenderContext(af,true,true);
		}
		
		Font f = font;
		if (f == null) f = new Font("Arial", Font.PLAIN, 12); // default
		
		double width= f.getStringBounds(input, frc).getWidth();  
		int w = ((Double)Math.ceil(width)).intValue();
		//logger.debug("input: " + input + ", length: " + input.length() + ", width: " + w);
		
		return w;
	}
	
	/*********************************************
	 * XML processing (ciknow.xml)
	 *********************************************/
	public static Document readXMLFromClasspath(String filename) throws DocumentException, IOException{
    	URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    	InputStream is = url.openStream();
    	Document doc = readXML(is);
    	is.close();
    	return doc;
	}
	
	public static Document readXML(InputStream is) throws DocumentException{
    	SAXReader reader = new SAXReader();
    	Document doc = reader.read(is);
    	return doc;
	}
	
	public static void writeXMLToClasspath(Document doc, String filename) throws IOException{
		URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
		OutputStream os = new FileOutputStream(new File(url.getFile()));
		writeXML(doc, os);
		os.close();		
	}
	
	public static void writeXML(Document doc, OutputStream out) throws IOException{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new OutputStreamWriter(out, "UTF-8"), format);
		writer.write(doc);
		writer.flush();
	}
	
	public static Map<String, String> getColors(){
		Map<String, String> colorMap = new HashMap<String, String>();
		
		try{
			Document doc = readXMLFromClasspath("ciknow.xml");
			Element config = doc.getRootElement();
			
			String nodeColors = config.element("nodeColors").attributeValue("value");
			if (nodeColors == null || nodeColors.length() == 0) 
				nodeColors = config.element("nodeColors").attributeValue("default");
			colorMap.put("nodeColors", nodeColors);
			
			String groupColors = config.element("groupColors").attributeValue("value");
			if (groupColors == null || groupColors.length() == 0) 
				groupColors = config.element("groupColors").attributeValue("default");
			colorMap.put("groupColors", groupColors);
			
			String edgeColors = config.element("edgeColors").attributeValue("value");
			if (edgeColors == null || edgeColors.length() == 0) 
				edgeColors = config.element("edgeColors").attributeValue("default");
			colorMap.put("edgeColors", edgeColors);
		} catch (Exception e){
			logger.warn("Error reading ciknow.xml !!");
		}
		
		return colorMap;
	}
	
	public static void saveColors(Map<String, String> colorMap){		
		try{
			Document doc = readXMLFromClasspath("ciknow.xml");
			Element config = doc.getRootElement();
			
			
			Element nodeColorsElement = config.element("nodeColors");
			String colors = colorMap.get("nodeColors");
			if (colors != null){
				nodeColorsElement.addAttribute("value", colors);
			}
			
			Element groupColorsElement = config.element("groupColors");
			colors = colorMap.get("groupColors");
			if (colors != null){
				groupColorsElement.addAttribute("value", colors);
			}
			
			Element edgeColorsElement = config.element("edgeColors");
			colors = colorMap.get("edgeColors");
			if (colors != null){
				edgeColorsElement.addAttribute("value", colors);
			}
			
			writeXMLToClasspath(doc, "ciknow.xml");
		} catch (Exception e){
			logger.warn("Error reading ciknow.xml !!");
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Map<String, String>> getNodeDescriptions(){
		logger.info("get node descriptions...");
		List<Map<String, String>> ntds = new LinkedList<Map<String, String>>();
		
		try{
			Document doc = readXMLFromClasspath("ciknow.xml");
			Element config = doc.getRootElement();
			Element ntdElement = config.element("nodeDescriptions");
			if (ntdElement != null){
				for (Iterator eItr = ntdElement.elementIterator("node"); eItr.hasNext();){
					Element nodeElement = (Element)eItr.next();
					String nodeType = nodeElement.attributeValue("type");
					String nodeLabel = nodeElement.attributeValue("label");
					Map<String, String> ed = new HashMap<String, String>();
					ed.put("type", nodeType);
					ed.put("label", nodeLabel);
					ntds.add(ed);
				}
			}
		} catch (Exception e){
			logger.warn("Error reading ciknow.xml !!");
		}
		
		return ntds;
	}
	
	public static Map<String, String> getNodeDescription(String nodeType){
		return getNodeDescription(getNodeDescriptions(), nodeType);
	}
	
	public static Map<String, String> getNodeDescription(List<Map<String, String>> ntds, String nodeType){
		for (Map<String, String> ntd : ntds){
			if (ntd.get("type").equals(nodeType)) return ntd;
		}
		return null;
	}
	
	public static Map<String, String> getNodeDescriptionByLabel(List<Map<String, String>> ntds, String nodeLabel){
		for (Map<String, String> ntd : ntds){
			if (ntd.get("label").equals(nodeLabel)) return ntd;
		}
		return null;
	}
	
	public static String getNodeTypeLabel(String nodeType){
		return getNodeTypeLabel(getNodeDescriptions(), nodeType);
	}
	
	public static String getNodeTypeLabel(List<Map<String, String>> ntds, String nodeType){
		if (nodeType == null || nodeType.length() == 0) return null;
		
		for (Map<String, String> ntd : ntds){
			if (ntd.get("type").equals(nodeType)) return ntd.get("label");
		}
		
		return nodeType;
	}	
	
	public static void saveNodeDescriptions(List<Map<String, String>> ntds){	
		logger.info("save node descriptions...");
		try{
			Document doc = readXMLFromClasspath("ciknow.xml");
			Element config = doc.getRootElement();
			Element ntdElement = config.element("nodeDescriptions");
			if (ntdElement != null) config.remove(ntdElement);
			ntdElement = config.addElement("nodeDescriptions");
			
			for (Map<String, String> ntd : ntds){
				String nodeType = ntd.get("type");
				String nodeLabel = ntd.get("label");
				
				Element nodeElement = ntdElement.addElement("node");
				nodeElement.addAttribute("type", nodeType);
				nodeElement.addAttribute("label", nodeLabel);		
			}
			
			writeXMLToClasspath(doc, "ciknow.xml");
		} catch (Exception e){
			logger.warn("Error reading ciknow.xml !!");
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Map<String, String>> getEdgeDescriptions(){
		logger.info("get edge descriptions...");
		List<Map<String, String>> eds = new LinkedList<Map<String, String>>();
		
		try{
			Document doc = readXMLFromClasspath("ciknow.xml");
			Element config = doc.getRootElement();
			Element edElement = config.element("edgeDescriptions");
			for (Iterator eItr = edElement.elementIterator("edge"); eItr.hasNext();){
				Element edgeElement = (Element)eItr.next();
				String edgeType = edgeElement.attributeValue("type");
				String edgeLabel = edgeElement.attributeValue("label");
				String edgeVerb = edgeElement.attributeValue("verb");
				Map<String, String> ed = new HashMap<String, String>();
				ed.put("type", edgeType);
				ed.put("label", edgeLabel);
				ed.put("verb", edgeVerb);
				eds.add(ed);
			}
		} catch (Exception e){
			logger.warn("Error reading ciknow.xml !!");
		}
		
		return eds;
	}
	
	public static Map<String, String> getEdgeDescription(String edgeType){
		return getEdgeDescription(getEdgeDescriptions(), edgeType);
	}
	
	public static Map<String, String> getEdgeDescription(List<Map<String, String>> eds, String edgeType){
		for (Map<String, String> ed : eds){
			String type = ed.get("type");
			// temp fix for Sunbelt1 project. The chance of case sensitive edgeType is extremely slim
			//if (type.equals(edgeType)) { 
			if (type.equalsIgnoreCase(edgeType)) { 
				return ed;
			}
		}
		return null;
	}
	
	public static String getEdgeLabel(String edgeType){
		return getEdgeLabel(getEdgeDescriptions(), edgeType);
	}
	
	public static String getEdgeLabel(List<Map<String, String>> eds, String edgeType){
		if (edgeType == null || edgeType.length() == 0) return null;
		
		for (Map<String, String> ed : eds){
			if (ed.get("type").equals(edgeType)) return ed.get("label");
		}
		
		return edgeType;
	}	
	
	public static String getEdgeVerb(String edgeType){
		return getEdgeVerb(getEdgeDescriptions(), edgeType);
	}
	
	public static String getEdgeVerb(List<Map<String, String>> eds, String edgeType){
		if (edgeType == null || edgeType.length() == 0) return null;
		
		for (Map<String, String> ed : eds){
			if (ed.get("type").equals(edgeType)) return ed.get("verb");
		}
		
		return edgeType;
	}
	
	public static void saveEdgeDescriptions(List<Map<String, String>> eds){	
		logger.info("save edge descriptions...");
		try{
			Document doc = readXMLFromClasspath("ciknow.xml");
			Element config = doc.getRootElement();
			Element edElement = config.element("edgeDescriptions");
			config.remove(edElement);
			edElement = config.addElement("edgeDescriptions");
			
			for (Map<String, String> ed : eds){
				String edgeType = ed.get("type");
				String edgeLabel = ed.get("label");
				String edgeVerb = ed.get("verb");
				
				Element edgeElement = edElement.addElement("edge");
				edgeElement.addAttribute("type", edgeType);
				edgeElement.addAttribute("label", edgeLabel);
				edgeElement.addAttribute("verb", edgeVerb);				
			}
			
			writeXMLToClasspath(doc, "ciknow.xml");
		} catch (Exception e){
			logger.warn("Error reading ciknow.xml !!");
		}
	}
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map getLargeNetworkLimits() throws DocumentException, IOException{
    	logger.info("get large network limits...");
    	Map map = new HashMap();
    	
    	Document doc = readXMLFromClasspath("ciknow.xml");
    	Element root = doc.getRootElement();
    	Element e = root.element("largeNetworkLimits");
    	if (e == null) {
    		map.put("nodes", "5000");
    		map.put("edges", "5000");
    		map.put("hardlimit", "10000");
    	} else {
    		String numNodes = e.attributeValue("nodes");
    		String numEdges = e.attributeValue("edges");
    		map.put("nodes", numNodes);
    		map.put("edges", numEdges);
    		map.put("hardlimit", e.attributeValue("hardlimit"));
    	}
    	
    	return map;
    }
    
    @SuppressWarnings("rawtypes")
	public static void setLargeNetworkLimits(Map map) throws DocumentException, IOException{
    	logger.info("set large network limits...");
    	Document doc = readXMLFromClasspath("ciknow.xml");
    	Element root = doc.getRootElement();
    	Element e = root.element("largeNetworkLimits");
    	if (e == null) e = root.addElement("largeNetworkLimits");
    	e.addAttribute("nodes", (String)map.get("nodes"));
    	e.addAttribute("edges", (String)map.get("edges"));
    	e.addAttribute("hardlimit", (String)map.get("hardlimit"));
    	writeXMLToClasspath(doc, "ciknow.xml");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map getTeamAssemblyConfig(String questionShortName) throws DocumentException, IOException{
    	logger.info("get team assembly config...");
    	Map map = new HashMap();
    	
    	Document doc = readXMLFromClasspath("ciknow.xml");
    	Element root = doc.getRootElement();
    	Element e = root.element("teamassembly");
    	if (e == null) {
    		logger.debug("create default config.");
    		map.put("diversity", "0.7");
    		map.put("similarity", "0.2");
    		map.put("density", "0.1");
    	} else {
    		map.put("diversity", e.attributeValue("diversity"));
    		map.put("similarity", e.attributeValue("similarity"));
    		map.put("density", e.attributeValue("density"));
    		
    		List<Element> skillElementList = new LinkedList<Element>();
    		Iterator itr = e.elementIterator("skill");
    		logger.debug("questionShortName: " + questionShortName);    		
    		while (itr.hasNext()){
    			Element skillElement = (Element)itr.next();    			
    			String qShortName = skillElement.attributeValue("qShortName");
    			if (qShortName.equals(questionShortName)){ 
    				skillElementList.add(skillElement);
    			}
    		}
    		Collections.sort(skillElementList, new TeamAssemblyConfigComparator());
    		List<Map<String, String>> skillList = new LinkedList<Map<String, String>>();
    		for (Element skillElement : skillElementList){				    			
				Map<String, String> skill = new HashMap<String, String>();
				skill.put("qShortName", skillElement.attributeValue("qShortName"));
				skill.put("sequenceNumber", skillElement.attributeValue("sequenceNumber"));
				skill.put("name", skillElement.attributeValue("name"));
				skill.put("label", skillElement.attributeValue("label"));
				skill.put("weight", skillElement.attributeValue("weight"));
				skillList.add(skill);
				logger.debug("skill: " + skill.get("label"));
    		}
    		logger.debug("added " + skillList.size() + " skillElements.");
    		
    		map.put("skills", skillList);
    	}
    	
    	return map;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void saveTeamAssemblyConfig(Map map) throws DocumentException, IOException{
    	logger.info("save team assembly config...");
    	Document doc = readXMLFromClasspath("ciknow.xml");
    	Element root = doc.getRootElement();
    	Element e = root.element("teamassembly");
    	if (e == null) e = root.addElement("teamassembly");
    	e.addAttribute("diversity", (String)map.get("diversity"));
    	e.addAttribute("similarity", (String)map.get("similarity"));
    	e.addAttribute("density", (String)map.get("density"));
    	
    	// remove old skill element for specified questionId
    	String questionShortName = (String) map.get("questionShortName");
		List<Element> skillElementList = new LinkedList<Element>();
		Iterator itr = e.elementIterator("skill");
		while (itr.hasNext()){
			Element skillElement = (Element)itr.next();
			String qShortName = skillElement.attributeValue("qShortName");
			if (qShortName.equals(questionShortName)){
				skillElementList.add(skillElement);
			}
		}
		logger.debug("remove old skill elements (" + skillElementList.size() + ") for question: " + questionShortName);
		for (Element skillElement : skillElementList){
			e.remove(skillElement);
		}
		
		// add new skill element for specified questionId
    	List<Map> skills = (List<Map>) map.get("skills");
    	for (Map skill : skills){
    		Element skillElement = e.addElement("skill");
    		skillElement.addAttribute("qShortName", (String) skill.get("qShortName"));
    		skillElement.addAttribute("sequenceNumber", (String) skill.get("sequenceNumber"));
    		skillElement.addAttribute("name", (String) skill.get("name"));
    		skillElement.addAttribute("label", (String) skill.get("label"));
    		skillElement.addAttribute("weight", (String) skill.get("weight"));
    	}
    	logger.debug("add " + skills.size() + " skill elements");
    	
    	writeXMLToClasspath(doc, "ciknow.xml");
    }
    


    /***********************************************
     * Conversion
     ***********************************************/
	public static byte[] objectToByteArray(Object o) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(o);
		return bos.toByteArray();
	}
	
	public static Object byteArrayToObject(byte[] ba) throws IOException, ClassNotFoundException{
		ByteArrayInputStream bis = new ByteArrayInputStream(ba);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object o = ois.readObject();
		return o;
	}
	
	public static List<Long> StringListToLongList(List<String> list){
		List<Long> list2 = new ArrayList<Long>();
		for (String s : list){
			list2.add(Long.parseLong(s));
		}
		return list2;
	}
	
	/**
	 * Flatten a ciknow database node (hierachical) into a plain node (map)
	 * @param node
	 * @param fieldNames
	 * @param questionMap
	 * @return
	 */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map flattenNode(Node node, Collection<String> fieldNames, Map<String, Question> questionMap){
    	Map m = new HashMap();    	
    	for (String fieldName : fieldNames){
			if (fieldName.equals("organization")) m.put(fieldName, node.getOrganization());
			else if (fieldName.equals("department")) m.put(fieldName, node.getDepartment());
			else if (fieldName.equals("unit")) m.put(fieldName, node.getUnit());
			else if (fieldName.equals("type")) m.put(fieldName, node.getType());
			else if (fieldName.equals("lastName")) m.put(fieldName, node.getLastName());
			else if (fieldName.equals("firstName")) m.put(fieldName, node.getFirstName());
			else if (fieldName.equals("city")) m.put(fieldName, node.getCity());
			else if (fieldName.equals("state")) m.put(fieldName, node.getState());
			else if (fieldName.equals("country")) m.put(fieldName, node.getCountry());
			else if (fieldName.equals("zipcode")) m.put(fieldName, node.getZipcode());
			else if (fieldName.equals("enabled")) m.put(fieldName, node.getEnabled());
			else if (fieldName.startsWith("Q" + Constants.SEPERATOR)){ // choice (single) or multipleChoice question 
				String shortName = fieldName.substring(2);
				Question question = questionMap.get(shortName);
				String value = null;
				for (Field field : question.getFields()){					
					String key = "F" + Constants.SEPERATOR + shortName + Constants.SEPERATOR + field.getName();
					value = node.getAttribute(key);
					if (value != null) {
						if (value.equals("1")) value = field.getLabel();
						else {
							// this is for "Other" popup
						}
						break;
					}
				}
				
				if (value == null) value = ""; // if no selection, set it as blank
				m.put(fieldName, value);
			} else {
				m.put(fieldName, "");
				logger.warn("Attribute '" + fieldName + "' is not available.");
			}
		}
    	
    	return m;
    }
    
    
    /***************************************************
     * ZK helper methods
     ***************************************************/
    public static Node getLogin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        CIKNOWUserDetails userDetails = (CIKNOWUserDetails) auth.getPrincipal();
        return userDetails.getNode();
    }

    public static Integer getDesktopWidth() {
        return (Integer) Sessions.getCurrent().getAttribute(Constants.SESSION_DESKTOP_WIDTH);
    }

    public static void setDesktopWidth(int width) {
        Sessions.getCurrent().setAttribute(Constants.SESSION_DESKTOP_WIDTH, width);
    }

    public static Integer getDesktopHeight() {
        return (Integer) Sessions.getCurrent().getAttribute(Constants.SESSION_DESKTOP_HEIGHT);
    }

    public static void setDesktopHeight(int height) {
        Sessions.getCurrent().setAttribute(Constants.SESSION_DESKTOP_HEIGHT, height);
    }
    
    /********************************************
     * Misc
     ********************************************/
	// true if attributes contain the key and (value="1" or value="Y")
	public static boolean verify(Map<String, String> attributes, String key){
		String value = attributes.get(key);
		if (value != null && (value.equals("Y") || value.equals("1"))) return true;
		return false;
	}
	
    @SuppressWarnings("rawtypes")
	public static void unzip(File infile, File directory) throws ZipException, IOException{
    	logger.info("unzipping " + infile.getName() + " to " + directory.getAbsolutePath());
    	ZipFile zipFile = new ZipFile(infile);
    	Enumeration files = zipFile.entries();        
        while (files.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) files.nextElement();
            logger.debug(entry.getName());
            InputStream fis = zipFile.getInputStream(entry);
      
            File f = new File(directory.getAbsolutePath() + File.separator + entry.getName());            
            if (entry.isDirectory()) {
              f.mkdirs();
              continue;
            } else {
              f.getParentFile().mkdirs();
              f.createNewFile();
            }            
            FileOutputStream fos = new FileOutputStream(f);
            
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = fis.read(buffer)) != -1) {
              fos.write(buffer, 0, bytesRead);
            }
        }
    }
    
    /**
     *  Sends a file to the ServletResponse output stream.  Typically
     *  you want the browser to receive a different name than the
     *  name the file has been saved in your local database, since
     *  your local names need to be unique.
     *
     *  @param req The request
     *  @param resp The response
     *  @param filename The name of the file you want to download.
     *  @param original_filename The name the browser should receive.
     */
    public static void doDownload( HttpServletRequest req, HttpServletResponse resp,
                             String filename, String original_filename )
        throws IOException
    {
        File                f        = new File(filename);
        int                 length   = 0;
        ServletOutputStream op       = resp.getOutputStream();
        ServletContext      context  = req.getSession().getServletContext();
        String              mimetype = context.getMimeType( filename );

        //
        //  Set the response and go!
        //
        //
        resp.setContentType( (mimetype != null) ? mimetype : "application/octet-stream" );
        resp.setContentLength( (int)f.length() );
        resp.setHeader( "Content-Disposition", "attachment; filename=\"" + original_filename + "\"" );

        //
        //  Stream to the requester.
        //
        byte[] bbuf = new byte[1024];
        DataInputStream in = new DataInputStream(new FileInputStream(f));

        while ((in != null) && ((length = in.read(bbuf)) != -1))
        {
            op.write(bbuf,0,length);
        }

        in.close();
        op.flush();
        op.close();
    }
    
    public static Date getLastUpdateTime(String filename) throws IOException, ParseException{
    	InputStream is = new FileInputStream(filename);
    	Properties p = new Properties();
    	p.load(is);
    	is.close();
    	String lastUpdate = p.getProperty("lastupdate");
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	Date d = sdf.parse(lastUpdate);
    	return d;
    }
}
