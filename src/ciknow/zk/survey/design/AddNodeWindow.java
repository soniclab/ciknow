package ciknow.zk.survey.design;

import ciknow.dao.*;
import ciknow.domain.Group;
import ciknow.domain.*;
import ciknow.ro.NodeRO;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.RandomString;
import ciknow.zk.survey.response.AbstractQuestionRelation;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AddNodeWindow extends Window {

    private static final long serialVersionUID = 507011502637726908L;
    private static Log logger = LogFactory.getLog(AddNodeWindow.class);
    @WireVariable
    SurveyDao surveyDao;
    @WireVariable
    QuestionDao questionDao;
    @WireVariable
    NodeDao nodeDao;
    @WireVariable
    GroupDao groupDao;
    @WireVariable
    RoleDao roleDao;
    @WireVariable
    NodeRO nodeRO;
    
    @Wire
    private Textbox searchBox;
    @Wire
    private Listbox nodeList;
    @Wire
    private Listfooter footer;
    
    @Wire
    private Combobox typeBox;
    @Wire
    private Row firstNameRow;
    @Wire
    private Textbox firstNameBox;
    @Wire
    private Row lastNameRow;
    @Wire
    private Textbox lastNameBox;
    @Wire
    private Row midNameRow;
    @Wire
    private Textbox midNameBox;
    @Wire
    private Textbox labelBox;
    private List<Map<String, String>> ntds;

    public AddNodeWindow(AbstractQuestionRelation p) {
        this.setParent(p);

        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/AddNodeWindow.zul", this, null);
        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);

        // type box and default selection
        ntds = nodeRO.getNodeTypeDescriptions();
        ListModelList<String> typeModel = new ListModelList<String>();
        for (Map<String, String> ntd : ntds) {
        	String nodeTypeLabel = ntd.get("label");
            typeModel.add(nodeTypeLabel);
        }
        String defaultContactType = p.getQuestion().getDefaultNewContactType();
        if (defaultContactType != null) {
            Map<String, String> ntd = GeneralUtil.getNodeDescription(ntds, defaultContactType);
            if (ntd != null) {
            	String nodeTypeLabel = ntd.get("label");
                typeModel.addToSelection(nodeTypeLabel);
            }
        }
        typeBox.setModel(typeModel);
        onNodeTypeChanged();

        this.setWidth("200px");
        this.setVflex("1");
        this.setClosable(true);
    }

    /*************************************************************
     * Search
     *************************************************************/
    @Listen("onClick = #searchBtn")
    public void search(){
    	String searchTerm = searchBox.getValue();
    	List<Node> nodes = nodeDao.matchByLabel("%" + searchTerm + "%");
    	ListModelList<Node> nodeModel = new ListModelList<Node>(nodes);
    	nodeList.setModel(nodeModel);
    	nodeList.setItemRenderer(new NodeRenderer());
    	footer.setLabel(nodes.size() + " nodes found.");
    }
    
    @Listen("onOK = #searchBox")
    public void onOK(){
    	search();
    }
    
    @Listen("onClick = #addBtn")
    public void add() throws InterruptedException {
    	Node node = nodeList.getSelectedItem().getValue();
    	node = nodeDao.loadById(node.getId());
    	AbstractQuestionRelation p = (AbstractQuestionRelation) getParent();
        node.getGroups().addAll((p.getQuestion().getAvailableGroups()));
        nodeDao.save(node);
        
        // update interface
        p.refreshWholePage();
    }
    
    
    /*************************************************************
     * Create
     *************************************************************/
    @SuppressWarnings("rawtypes")
	private void onNodeTypeChanged() {
    	ListModelList typeModel = (ListModelList)typeBox.getModel();
    	Set selections = typeModel.getSelection();
    	if (selections.isEmpty()) return;
    	String selection = (String) selections.iterator().next();
    	logger.info("Node type set to: " + selection);

        Map<String, String> ntd = GeneralUtil.getNodeDescriptionByLabel(ntds, selection);
        String nodeType = ntd.get("type");
        if (nodeType.equals(Constants.NODE_TYPE_USER)) {
            firstNameRow.setVisible(true);
            firstNameBox.setConstraint("no empty");            
            lastNameRow.setVisible(true);
            lastNameBox.setConstraint("no empty");
            midNameRow.setVisible(true);
        } else {
            firstNameRow.setVisible(false);
            firstNameBox.setConstraint("");
            lastNameRow.setVisible(false);
            lastNameBox.setConstraint("");
            midNameRow.setVisible(false);
        }
    }

    @Listen("onChange = #typeBox")
    public void onChange$typeBox() {
    	// have to do this to make sure model is updated
    	typeBox.getSelectedItem();
    	
        onNodeTypeChanged();
    }

    @Listen("onChanging = #firstNameBox")
    public void onChanging$firstNameBox(InputEvent event) {
        onNameChanged(event);
    }

    @Listen("onChanging = #lastNameBox")
    public void onChanging$lastNameBox(InputEvent event) {
        onNameChanged(event);
    }

    private void onNameChanged(InputEvent e) {
        String value = e.getValue();
        Textbox box = (Textbox) e.getTarget();
        String lastName, firstName;
        if (box == lastNameBox) {
            lastName = value;
            firstName = firstNameBox.getValue();
        } else {
            lastName = lastNameBox.getValue();
            firstName = value;
        }
        labelBox.setValue(lastName + ", " + firstName);
    }
    
    @Listen("onClick = #createBtn")
    public void create() throws InterruptedException {
        createNode();
        AbstractQuestionRelation p = (AbstractQuestionRelation) getParent();
        p.refresh();
    }

    @Listen("onClick = #cancelCreateBtn, #cancelAddBtn")
    public void cancel() {
        this.setParent(null);
    }

    
    /*****************************************************
     * Helper methods
     *****************************************************/
    private Node createNode() throws InterruptedException {
        Comboitem item = typeBox.getSelectedItem();
        if (item == null) {
            Messagebox.show("Node type is required.");
            return null;
        }

        String nodeType = GeneralUtil.getNodeDescriptionByLabel(ntds, item.getLabel()).get("type");
        String firstName = firstNameBox.getValue().trim();
        String lastName = lastNameBox.getValue().trim();
        String midName = midNameBox.getValue().trim();
        String label = labelBox.getValue().trim();
        String username;
        if (nodeType.equals(Constants.NODE_TYPE_USER)) {
            if (firstName.isEmpty()) {
                Messagebox.show("First Name is required.");
                return null;
            } else if (firstName.length() > 80) {
            	Messagebox.show("First Name cannot be longer than 80.");
            	return null;
            } else if (GeneralUtil.containSpecialCharacter(firstName)){
            	Messagebox.show("First Name cannot contain special characters.");
            	return null;
            }
            
            if (lastName.isEmpty()) {
                Messagebox.show("Last Name is required.");
                return null;
            } else if (lastName.length() > 80) {
            	Messagebox.show("Last Name cannot be longer than 80.");
            	return null;
            } else if (GeneralUtil.containSpecialCharacter(lastName)){
            	Messagebox.show("Last Name cannot contain special characters.");
            	return null;
            }
            
            username = firstName.replace(" ", "_") + "__" + lastName.replace(" ", "_");
        } else {
            if (label.isEmpty()) {
                Messagebox.show("Label is required.");
                return null;
            } else if (label.length() > 255) {
            	Messagebox.show("Label cannot be longer than 255.");
            	return null;
            }
            username = GeneralUtil.replaceSpecialCharacter(label, "_").replace(" ", "_");
        }

        if (nodeDao.findByUsername(username) != null) {
            Messagebox.show("Username: " + username + " has already been used.");
            return null;
        }

        Survey survey = surveyDao.findById(1L);
        Node node = new Node();
        node.setType(nodeType);
        node.setUsername(username);
        node.setLabel(label);
        if (nodeType.equals(Constants.NODE_TYPE_USER)){
	        node.setFirstName(firstName);
	        node.setLastName(lastName);
	        node.setMidName(midName);
        }

        String defaultPassword = survey.getAttribute(Constants.SURVEY_DEFAULT_PASSWORD);
        if (defaultPassword == null) {
            defaultPassword = "sonic";
        }
        if (defaultPassword.equals("rAnDoM")) {
            RandomString rs = new RandomString(8);
            defaultPassword = rs.nextString();
        }
        node.setPassword(defaultPassword);

        node.setAttribute(Constants.NODE_LOGIN_MODE, survey.getAttribute(Constants.SURVEY_DEFAULT_LOGIN_MODE));


        // role
        node.getRoles().add(roleDao.findByName(Constants.ROLE_USER));

        // groups
        Set<Group> groups = new HashSet<Group>();
        AbstractQuestionRelation p = (AbstractQuestionRelation) getParent();
        groups.addAll(p.getQuestion().getAvailableGroups());
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

        node.setGroups(groups);

        // save
        nodeDao.save(node);
        logger.debug("Node created: " + username);

        return node;
    }
    
    private class NodeRenderer implements ListitemRenderer<Node>{

		@Override
		public void render(Listitem item, Node data, int index) throws Exception {
			item.setValue(data);
			new Listcell(data.getLabel()).setParent(item);
		}
    	
    }
}
