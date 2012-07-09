package ciknow.zk.survey.response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import ciknow.domain.Group;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Survey;
import ciknow.ro.NodeRO;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.RandomString;

public class ContactProvider extends SurveyQuestionBase {
	private static final long serialVersionUID = -648818588286717024L;
	private static Log logger = LogFactory.getLog(ContactProvider.class);
	
	@WireVariable
	private NodeRO nodeRO;
	
	@Wire
	private Listbox listbox;	
	
	@Wire
	private Window addContactWindow;
	@Wire("#addContactWindow #firstNameBox")
	private Textbox firstNameBox;
	@Wire("#addContactWindow #firstNameRow")
	private Row firstNameRow;
	@Wire("#addContactWindow #lastNameBox")
	private Textbox lastNameBox;
	@Wire("#addContactWindow #lastNameRow")
	private Row lastNameRow;
	@Wire("#addContactWindow #labelBox")
	private Textbox labelBox;
	@Wire("#addContactWindow #labelRow")
	private Row labelRow;
	
	
	private Group providerGroup;
	
	public ContactProvider(Question currentQuestion) {
		super(currentQuestion);
	}

	public ContactProvider(Node respondent, Question currentQuestion) {
		super(respondent, currentQuestion);
	}
	
    @Override
    protected void createUI() {
        Executions.createComponents("/WEB-INF/zk/survey/response/ContactProvider.zul", this, null);
    }
    
    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        // get contact list
        List<Node> nodes = new ArrayList<Node>();
        String providerGroupName = Group.getProviderGroupName(respondent.getUsername(), currentQuestion.getShortName());
        providerGroup = groupDao.findByName(providerGroupName);        
        if (providerGroup == null){
        	logger.info("creating contact provider group:  " + providerGroupName);
        	providerGroup = new Group();
        	providerGroup.setName(providerGroupName);
        	groupDao.save(providerGroup);
        } else {
        	List<Long> nodeIds  = groupDao.getNodeIdsByGroupId(providerGroup.getId());
        	nodes = nodeDao.findByIds(nodeIds);
        }
        
        // build UI
        for (Node node : nodes){
        	Listitem li = new Listitem(node.getLabel());
        	li.setParent(listbox);
        	li.setValue(node);
        }   
        addContactWindow.setParent(null);
    }
    
    @Listen("#addContactBtn")
    public void showAddContactWindow(){
    	String defaultType = currentQuestion.getDefaultNewContactType();
    	if (defaultType.equals(Constants.NODE_TYPE_USER)){
    		labelBox.setValue("");
    		labelRow.setVisible(false);
    		firstNameBox.setValue("");
    		firstNameRow.setVisible(true);
    		lastNameBox.setValue("");
    		lastNameRow.setVisible(true);
    	} else {
    		labelBox.setValue("");
    		labelRow.setVisible(true);
    		firstNameBox.setValue("");
    		firstNameRow.setVisible(false);
    		lastNameBox.setValue("");
    		lastNameRow.setVisible(false);
    	}
    	addContactWindow.setParent(this);  
    	addContactWindow.doModal();
    }
    
    @Listen("#addContactWindow #createBtn")
    public void create(){
    	String providerGroupName = providerGroup.getName();
    	String nodeType = currentQuestion.getDefaultNewContactType();
        String firstName = firstNameBox.getValue().trim();
        String lastName = lastNameBox.getValue().trim();
        String label = labelBox.getValue().trim();
        String username;
        
        // validation
        if (nodeType.equals(Constants.NODE_TYPE_USER)) {
            if (firstName.isEmpty()) {
                Messagebox.show("First Name is required.");
                return;
            } else if (firstName.length() > 80) {
            	Messagebox.show("First Name cannot be longer than 80.");
            	return;
            } else if (GeneralUtil.containSpecialCharacter(firstName)){
            	Messagebox.show("First Name cannot contain special characters.");
            	return;
            }
            
            if (lastName.isEmpty()) {
                Messagebox.show("Last Name is required.");
                return;
            } else if (lastName.length() > 80) {
            	Messagebox.show("Last Name cannot be longer than 80.");
            	return;
            } else if (GeneralUtil.containSpecialCharacter(lastName)){
            	Messagebox.show("Last Name cannot contain special characters.");
            	return;
            }
            
            label = lastName + ", " + firstName;
            
            username = providerGroupName + "_" + firstName.replace(" ", "_") + "_" + lastName.replace(" ", "_");
        } else {
        	firstName = "";
        	lastName = "";
            if (label.isEmpty()) {
                Messagebox.show("Label is required.");
                return;
            } else if (label.length() > 255) {
            	Messagebox.show("Label cannot be longer than 255.");
            	return;
            }
            username = providerGroupName + "_" + GeneralUtil.replaceSpecialCharacter(label, "_").replace(" ", "_");
        }

        if (nodeDao.findByUsername(username) != null) {
            Messagebox.show("Username: " + username + " has already been used.");
            return;
        }
        
        
        // create new contact        
        Survey survey = surveyDao.findById(1L);
        Node contact = new Node();
        contact.setType(nodeType);
        contact.setUsername(username);
        contact.setLabel(label);
        contact.setFirstName(firstName);
        contact.setLastName(lastName);

        // default password
        String defaultPassword = survey.getAttribute(Constants.SURVEY_DEFAULT_PASSWORD);
        if (defaultPassword == null) {
            defaultPassword = "sonic";
        }
        if (defaultPassword.equals("rAnDoM")) {
            RandomString rs = new RandomString(8);
            defaultPassword = rs.nextString();
        }
        contact.setPassword(defaultPassword);
        contact.setAttribute(Constants.NODE_LOGIN_MODE, survey.getAttribute(Constants.SURVEY_DEFAULT_LOGIN_MODE));

        // role
        contact.getRoles().add(roleDao.findByName(Constants.ROLE_USER));

        // groups
        Set<Group> groups = new HashSet<Group>();
        groups.add(groupDao.findByName(Constants.GROUP_ALL));
        if (nodeType.equals(Constants.NODE_TYPE_USER)) {
            groups.add(groupDao.findByName(Constants.GROUP_USER));
        } else {
            String groupName = Constants.GROUP_NODE_TYPE_PREFIX + nodeType;
            Group group = groupDao.findByName(groupName);
            if (group == null) {
                group = new Group();
                group.setName(groupName);
                groupDao.save(group);
                logger.info("created new group: " + group.getName());
            }
            groups.add(group);
        }
        groups.add(providerGroup);
        contact.setGroups(groups);

        // save
        nodeDao.save(contact);
        logger.debug("Contact created: " + username);
        
        
        // update UI
    	Listitem li = new Listitem(contact.getLabel());
    	li.setParent(listbox);
    	li.setValue(contact);
    	addContactWindow.setParent(null);
    }
    
    @Listen("#addContactWindow #cancelBtn")
    public void cancel(){
    	addContactWindow.setParent(null);
    }
    
    @Listen("#removeContactBtn")
    public void remove(){
    	Listitem li = listbox.getSelectedItem();
    	if (li == null){
    		Messagebox.show("Please select an item to remove.");
    		return;
    	}
    	Node node = (Node)li.getValue();
    	nodeRO.deleteNodeById(node.getId());
    	
    	// update UI
    	li.setParent(null);
    }
    
    @Transactional
    @Override
    public void save() {
    	// do nothing
    }
}
