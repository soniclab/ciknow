package ciknow.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.RoleDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Group;
import ciknow.domain.Node;
import ciknow.domain.Survey;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.RandomString;

public class ContactReader{
	private static Log logger = LogFactory.getLog(ContactReader.class);
	private NodeDao nodeDao;
	private SurveyDao surveyDao;
	private GroupDao groupDao;
	private RoleDao roleDao;
	
	public static void main(String[] args) throws Exception{
		if (args.length < 1) {
			logger.error("filename (e.g. c:/path/to/file) is required.");
			return;
		}
		String filename = args[0];
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		
		Beans.init();
        ContactReader contactReader = (ContactReader) Beans.getBean("contactReader");
        contactReader.read(reader, "0");
	}
	
	public ContactReader() {
		super();
	}

	public void read(BufferedReader reader, String overwrite) throws Exception {
		logger.info("importing contacts (normal)...");
    	
		logger.debug("get survey");
    	Survey survey = surveyDao.findById(1L);
    	String defaultPassword = survey.getAttribute(Constants.SURVEY_DEFAULT_PASSWORD);
    	if (defaultPassword == null) defaultPassword = "sonic";
    	RandomString rs = new RandomString(8);
    	
    	logger.debug("get group map...");
    	List<Group> groups = groupDao.getAll();
    	Map<String, Group> groupMap = new HashMap<String, Group>();
    	for (Group group : groups){
    		groupMap.put(group.getName().trim(), group);
    	}
    	
    	logger.debug("get all existing nodes.");
        List<Node> existingNodes =  nodeDao.loadAll();
        Map<String, Node> nodeMap = new HashMap<String, Node>();
        for (Node n : existingNodes){
            nodeMap.put(n.getUsername().trim(), n);
        }
        Set<String> usernameInFile = new HashSet<String>();
        StringBuilder sb = new StringBuilder();
        
		List<Node> nodes = new ArrayList<Node>();

        // process each node's response (rating)
        logger.debug("reading each row (each node)");
        String line = reader.readLine();
        boolean isNew = false;
        int requiredFieldNum = Constants.getDefaultContactFields().size();
        int count = 0;
        while (line != null){                       
            if (line.startsWith("#") || line.trim().length()==0 || line.startsWith("Username")) {
            	logger.debug("ignored: " + line);
                line = reader.readLine();
                isNew = false;                
                continue;
            }
            
            String[] texts = line.split("\t", -1);            
            if (texts.length < requiredFieldNum){
            	throw new IOException(">> Expected number of contact fields: " + requiredFieldNum + ", Actual: " + texts.length + "\nLine: " + line + "\n");
            }
            
            String username = texts[0].trim();  
            if (username.length() == 0) {
            	sb.append(">> username cannot be empty: " + line + "\n");
                line = reader.readLine();
                isNew = false;
            	continue;
            }
            
            // check username length
            if (username.length() > 255) {
            	sb.append(">> Username is too long (> 255): " + username + "\n");
                line = reader.readLine();
                isNew = false;
            	continue;
            }
            
            if (username.contains(" ")
            		|| GeneralUtil.containSpecialCharacter(username)){
            	sb.append(">> Username cannot contains special characters or spaces: " + username + "\n");
                line = reader.readLine();
                isNew = false;
            	continue;
            }
            
            // check duplicate usernames
            if (usernameInFile.contains(username)){
            	sb.append(">> Duplicated username in file: " + username + "\n");
                line = reader.readLine();
                isNew = false;
            	continue;
            } else {
            	usernameInFile.add(username);
            }
            
            Node node = nodeMap.get(username);
            if (node == null){
            	isNew = true;
            	node = new Node();
            	node.setUsername(username);
            	nodeMap.put(username, node);
            } else {
            	if (overwrite.equals("0")){
            		sb.append(">> Node already exists: " + username + "\n");
                    line = reader.readLine();
                    isNew = false;
                	continue;
            	}
            }
            
            if (isNew){            	
            	if (defaultPassword.equals("rAnDoM")) node.setPassword(rs.nextString());
            	else node.setPassword(defaultPassword);
            }
            node.setFirstName(texts[1].trim());
            node.setLastName(texts[2].trim());
            node.setMidName(texts[3].trim());
            node.setAddr1(texts[4].trim());
            node.setAddr2(texts[5].trim());
            node.setCity(texts[6].trim());
            node.setState(texts[7].trim());
            node.setCountry(texts[8].trim());
            node.setZipcode(texts[9].trim());
            node.setEmail(texts[10].trim());
            node.setPhone(texts[11].trim());
            node.setCell(texts[12].trim());
            node.setFax(texts[13].trim());
            node.setDepartment(texts[14].trim());
            node.setOrganization(texts[15].trim());
            node.setUnit(texts[16].trim());
            node.setEnabled(texts[17].equals("1")?true:false);
            	            
            node.setLabel(texts[18].length()<1024?texts[18].trim():texts[18].substring(0, 1024).trim());
            if (texts[18].trim().length() == 0 && texts[20].equals(Constants.NODE_TYPE_USER)) 
            	node.setLabel(node.getLastName() + ", " + node.getFirstName());
            if (node.getLabel().length() == 0) node.setLabel("!EMPTY!");
            
            node.setUri(texts[19].trim());
            node.setType(texts[20].trim());
            
            // some extra information
            if (isNew){
	            node.setAttribute(Constants.NODE_LOGIN_MODE, survey.getAttribute(Constants.SURVEY_DEFAULT_LOGIN_MODE));
	            
	            // add to "ALL" group
	            node.getGroups().add(groupMap.get(Constants.GROUP_ALL)); 
	            
	            // group based on node type
	            // add to "USER" group
	            String nodeType = node.getType();
	            if (nodeType.equals(Constants.NODE_TYPE_USER)) {
	            	node.getGroups().add(groupMap.get(Constants.GROUP_USER)); 
	            	node.getRoles().add(roleDao.findByName(Constants.ROLE_USER));
	            }
	            // add to group of nodeType
	            else {
	            	String groupName = Constants.GROUP_NODE_TYPE_PREFIX + nodeType;
	            	Group group = groupMap.get(groupName);
	            	if (group == null){
	            		group = new Group();
	            		group.setName(groupName);
	            		groupDao.save(group);
	            		groupMap.put(groupName, group);
	            		logger.debug("creatd new group: " + group.getName());
	            	}
	            	node.getGroups().add(group);
	            }
	            
	            
	            ////////////////////////////////////////////////////////////////
	            // For convenience only. Subsequent change of these properties 
	            // WILL NOT update group relations automatically
	            ////////////////////////////////////////////////////////////////	            
	            // group based on 'department'
	            String dept = node.getDepartment();
	            if (dept != null && dept.trim().length() != 0){
	            	String groupName = Constants.GROUP_DEPT_PREFIX + dept;
	            	Group group = groupMap.get(groupName);
	            	if (group == null){
	            		group = new Group();
	            		group.setName(groupName);
	            		groupDao.save(group);
	            		groupMap.put(groupName, group);
	            		logger.debug("creatd new group: " + group.getName());
	            	}
	            	node.getGroups().add(group);	            	
	            }
	            
	            // group based on 'organization'
	            String org = node.getOrganization();
	            if (org != null && org.trim().length() != 0){
	            	String groupName = Constants.GROUP_ORGANIZATION_PREFIX + org;
	            	Group group = groupMap.get(groupName);
	            	if (group == null){
	            		group = new Group();
	            		group.setName(groupName);
	            		groupDao.save(group);
	            		groupMap.put(groupName, group);
	            		logger.debug("creatd new group: " + group.getName());
	            	}
	            	node.getGroups().add(group);
	            }
	            
	            // group based on 'unit'
	            String unit = node.getUnit();
	            if (unit != null && unit.trim().length() != 0){
	            	String groupName = Constants.GROUP_UNIT_PREFIX + unit;
	            	Group group = groupMap.get(groupName);
	            	if (group == null){
	            		group = new Group();
	            		group.setName(groupName);
	            		groupDao.save(group);
	            		groupMap.put(groupName, group);
	            		logger.debug("creatd new group: " + group.getName());
	            	}
	            	node.getGroups().add(group);
	            }
            }
            
            nodes.add(node);
            
            count++;
            // Nodes are saved in batch, so it is possible that only part of the
            // input file is read and saved into database, but later part is aborted
            // due to exceptional conditions occur later.
            if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
            	if (sb.length() > 0) throw new Exception(sb.toString());
            	nodeDao.save(nodes);
            	logger.debug(count + " nodes created or updated.");
            	nodes = new ArrayList<Node>();
            }
            
            line = reader.readLine();
            isNew = false;
        }

        if (sb.length() > 0) throw new Exception(sb.toString());
        nodeDao.save(nodes);
        logger.debug(count + " nodes created or updated.");
	}
	
	public void updateOnly(BufferedReader reader) throws Exception {
		logger.info("importing contacts (updateOnly)...");

        // process each node's response (rating)
        logger.debug("reading each row (each node)");
        Map<String, String[]> lineMap = new HashMap<String, String[]>();
        int count = 0;
        String line = reader.readLine();        
        while (line != null){                       
            if (line.startsWith("#") || line.trim().length()==0 || line.startsWith("Username")) {
            	logger.debug("ignored: " + line);
                line = reader.readLine();             
                continue;
            }
            
            String[] texts = line.split("\t", -1);                        
            String username = texts[0].trim();  
            lineMap.put(username, texts);
            
            count++;
            if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
            	updateBatch(lineMap);
            	lineMap = new HashMap<String, String[]>();
            	logger.debug(count + " nodes updated.");
            }
            
            line = reader.readLine();
        }

        updateBatch(lineMap);
        logger.debug(count + " nodes updated.");
	}
	
	private void updateBatch(Map<String, String[]> lineMap){
		if (lineMap.isEmpty()) return;
       	List<Node> nodes = nodeDao.findByUsernames(lineMap.keySet());
    	for (Node node : nodes){
    		String[] texts = lineMap.get(node.getUsername());
    		
            node.setFirstName(texts[1].trim());
            node.setLastName(texts[2].trim());
            node.setMidName(texts[3].trim());
            node.setAddr1(texts[4].trim());
            node.setAddr2(texts[5].trim());
            node.setCity(texts[6].trim());
            node.setState(texts[7].trim());
            node.setCountry(texts[8].trim());
            node.setZipcode(texts[9].trim());
            node.setEmail(texts[10].trim());
            node.setPhone(texts[11].trim());
            node.setCell(texts[12].trim());
            node.setFax(texts[13].trim());
            node.setDepartment(texts[14].trim());
            node.setOrganization(texts[15].trim());
            node.setUnit(texts[16].trim());
            node.setEnabled(texts[17].equals("1")?true:false);
            	            
            node.setLabel(texts[18].length()<1024?texts[18].trim():texts[18].substring(0, 1024).trim());
            if (texts[18].trim().length() == 0 && texts[20].equals(Constants.NODE_TYPE_USER)) 
            	node.setLabel(node.getLastName() + ", " + node.getFirstName());
            if (node.getLabel().length() == 0) node.setLabel("!EMPTY!");
            
            node.setUri(texts[19].trim());
            node.setType(texts[20].trim());
    	}
    	
    	nodeDao.save(nodes);
	}
	
	public void createOnly(BufferedReader reader) throws Exception {
		logger.info("importing contacts (createOnly)...");

		logger.debug("get survey for default password.");
    	Survey survey = surveyDao.findById(1L);
    	String defaultPassword = survey.getAttribute(Constants.SURVEY_DEFAULT_PASSWORD);
    	if (defaultPassword == null) defaultPassword = "sonic";
    	RandomString rs = new RandomString(8);
    	
    	logger.debug("get group map...");
    	List<Group> groups = groupDao.getAll();
    	Map<String, Group> groupMap = new HashMap<String, Group>();
    	for (Group group : groups){
    		groupMap.put(group.getName().trim(), group);
    	}
    	 
		List<Node> nodes = new ArrayList<Node>();

        logger.debug("reading each row (each node)");
        String line = reader.readLine();
        int count = 0;
        while (line != null){                       
            if (line.startsWith("#") || line.trim().length()==0 || line.startsWith("Username")) {
            	logger.debug("ignored: " + line);
                line = reader.readLine();              
                continue;
            }
            
            String[] texts = line.split("\t", -1);                        
            String username = texts[0].trim();              
            Node node = new Node();
            node.setUsername(username);

            // set default password
        	if (defaultPassword.equals("rAnDoM")) node.setPassword(rs.nextString());
        	else node.setPassword(defaultPassword);
        	
            node.setFirstName(texts[1].trim());
            node.setLastName(texts[2].trim());
            node.setMidName(texts[3].trim());
            node.setAddr1(texts[4].trim());
            node.setAddr2(texts[5].trim());
            node.setCity(texts[6].trim());
            node.setState(texts[7].trim());
            node.setCountry(texts[8].trim());
            node.setZipcode(texts[9].trim());
            node.setEmail(texts[10].trim());
            node.setPhone(texts[11].trim());
            node.setCell(texts[12].trim());
            node.setFax(texts[13].trim());
            node.setDepartment(texts[14].trim());
            node.setOrganization(texts[15].trim());
            node.setUnit(texts[16].trim());
            node.setEnabled(texts[17].equals("1")?true:false);
            	            
            node.setLabel(texts[18].length()<1024?texts[18].trim():texts[18].substring(0, 1024).trim());
            if (texts[18].trim().length() == 0 && texts[20].equals(Constants.NODE_TYPE_USER)) 
            	node.setLabel(node.getLastName() + ", " + node.getFirstName());
            if (node.getLabel().length() == 0) node.setLabel("!EMPTY!");
            
            node.setUri(texts[19].trim());
            node.setType(texts[20].trim());
            
            // some extra information
            node.setAttribute(Constants.NODE_LOGIN_MODE, survey.getAttribute(Constants.SURVEY_DEFAULT_LOGIN_MODE));
            
            // add to "ALL" group
            node.getGroups().add(groupMap.get(Constants.GROUP_ALL)); 
            
            // group based on node type
            // add to "USER" group
            String nodeType = node.getType();
            if (nodeType.equals(Constants.NODE_TYPE_USER)) {
            	node.getGroups().add(groupMap.get(Constants.GROUP_USER)); 
            	node.getRoles().add(roleDao.findByName(Constants.ROLE_USER));
            }
            // add to group of nodeType
            else {
            	String groupName = Constants.GROUP_NODE_TYPE_PREFIX + nodeType;
            	Group group = groupMap.get(groupName);
            	if (group == null){
            		group = new Group();
            		group.setName(groupName);
            		groupDao.save(group);
            		groupMap.put(groupName, group);
            		logger.debug("creatd new group: " + group.getName());
            	}
            	node.getGroups().add(group);
            }
            
            
            ////////////////////////////////////////////////////////////////
            // For convenience only. Subsequent change of these properties 
            // WILL NOT update group relations automatically
            ////////////////////////////////////////////////////////////////	            
            // group based on 'department'
            String dept = node.getDepartment();
            if (dept != null && dept.trim().length() != 0){
            	String groupName = Constants.GROUP_DEPT_PREFIX + dept;
            	Group group = groupMap.get(groupName);
            	if (group == null){
            		group = new Group();
            		group.setName(groupName);
            		groupDao.save(group);
            		groupMap.put(groupName, group);
            		logger.debug("creatd new group: " + group.getName());
            	}
            	node.getGroups().add(group);	            	
            }
            
            // group based on 'organization'
            String org = node.getOrganization();
            if (org != null && org.trim().length() != 0){
            	String groupName = Constants.GROUP_ORGANIZATION_PREFIX + org;
            	Group group = groupMap.get(groupName);
            	if (group == null){
            		group = new Group();
            		group.setName(groupName);
            		groupDao.save(group);
            		groupMap.put(groupName, group);
            		logger.debug("creatd new group: " + group.getName());
            	}
            	node.getGroups().add(group);
            }
            
            // group based on 'unit'
            String unit = node.getUnit();
            if (unit != null && unit.trim().length() != 0){
            	String groupName = Constants.GROUP_UNIT_PREFIX + unit;
            	Group group = groupMap.get(groupName);
            	if (group == null){
            		group = new Group();
            		group.setName(groupName);
            		groupDao.save(group);
            		groupMap.put(groupName, group);
            		logger.debug("creatd new group: " + group.getName());
            	}
            	node.getGroups().add(group);
            }

            nodes.add(node);            
            count++;
            // Nodes are saved in batch, so it is possible that only part of the
            // input file is read and saved into database, but later part is aborted
            // due to exceptional conditions occur later.
            if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
            	nodeDao.save(nodes);
            	logger.debug(count + " nodes created.");
            	nodes = new ArrayList<Node>();
            }
            
            line = reader.readLine();
        }

        nodeDao.save(nodes);
        logger.debug(count + " nodes created.");
	}

	public void validate(BufferedReader reader) throws Exception {
		logger.info("importing contacts (validate)...");
    	
        Set<String> usernameInFile = new HashSet<String>();
        StringBuilder sb = new StringBuilder();       

        // process each node's response (rating)
        logger.debug("reading each row (each node)");
        String line = reader.readLine();
        int requiredFieldNum = Constants.getDefaultContactFields().size();
        int count = 0;
        while (line != null){   
        	count++;
            if (line.startsWith("#") || line.trim().length()==0 || line.startsWith("Username")) {
            	logger.debug("ignored: " + line);
                line = reader.readLine();             
                continue;
            }
            
            String[] texts = line.split("\t", -1);            
            if (texts.length < requiredFieldNum){
            	sb.append(">> Expected number of contact fields: " + requiredFieldNum + ", Actual: " + texts.length + ", @line: " + count + "\n");
                line = reader.readLine();
            	continue;
            }
            
            String username = texts[0].trim();  
            if (username.length() == 0) {
            	sb.append(">> username cannot be empty: " + line + "\n");
                line = reader.readLine();
            	continue;
            }
            
            // check username length
            if (username.length() > 50) {
            	sb.append(">> Username is too long (> 50): " + username + "\n");
                line = reader.readLine();
            	continue;
            }
            
            if (username.contains(" ")
            		|| username.contains(",")
            		|| username.contains("`")
            		|| username.contains("/")
            		|| username.contains("\\")
            		|| username.contains("*")
            		|| username.contains("\"")
            		|| username.contains(">")
            		|| username.contains("<")
            		|| username.contains(":")
            		|| username.contains("|")
            		|| username.contains("?")){
            	sb.append(">> Username cannot contains special characters or spaces: " + username + "\n");
                line = reader.readLine();
            	continue;
            }
            
            // check duplicate usernames
            if (usernameInFile.contains(username)){
            	sb.append(">> Duplicated username in file: " + username + ", @line " + count + "\n");
                line = reader.readLine();
            	continue;
            } else {
            	usernameInFile.add(username);
            }

            line = reader.readLine();
        }

        if (sb.length() > 0) throw new Exception(sb.toString());
		logger.info("The input file is valid for upload/import.");
	}
	
	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public SurveyDao getSurveyDao() {
		return surveyDao;
	}

	public void setSurveyDao(SurveyDao surveyDao) {
		this.surveyDao = surveyDao;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}
}
