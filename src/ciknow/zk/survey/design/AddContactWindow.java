package ciknow.zk.survey.design;

import ciknow.dao.*;
import ciknow.domain.Group;
import ciknow.domain.*;
import ciknow.ro.NodeRO;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.RandomString;
import ciknow.zk.survey.response.ContactChooser;
import ciknow.zk.survey.response.ContactChooserItem;

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
public class AddContactWindow extends Window {

    private static final long serialVersionUID = 507011502637726908L;
    private static Log logger = LogFactory.getLog(AddContactWindow.class);
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
    private Grid form;
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
    private Map<String, Combobox> boxMap = new HashMap<String, Combobox>();

    public AddContactWindow(ContactChooser cc, LinkedHashMap<String, String> columns) {
        this.setParent(cc);

        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/AddContactWindow.zul", this, null);
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
        String defaultContactType = cc.getQuestion().getDefaultNewContactType();
        if (defaultContactType != null) {
            Map<String, String> ntd = GeneralUtil.getNodeDescription(ntds, defaultContactType);
            if (ntd != null) {
            	String nodeTypeLabel = ntd.get("label");
                typeModel.addToSelection(nodeTypeLabel);
            }
        }
        typeBox.setModel(typeModel);
        onNodeTypeChanged();

        // other dynamically generated rows
        for (String column : columns.keySet()) {
            logger.info("column: " + column);
            if (column.equals(ContactChooserItem.IMAGE_KEY)) {
                continue;
            }
            if (column.equals("type")
                    || column.equals("firstName")
                    || column.equals("lastName")
                    || column.equals("midName")
                    || column.equals("label")) {
                continue;
            }

            Row row = new Row();
            Label label = new Label(columns.get(column));
            row.appendChild(label);
            Combobox box = new Combobox();
            row.appendChild(box);
            if (column.startsWith("Q" + Constants.SEPERATOR)) {
                List<String> fieldLabels = new ArrayList<String>();
                String shortName = column.substring(2);
                Question question = questionDao.findByShortName(shortName);
                if (question != null && question.isSingleChoice()) {
                    for (Field field : question.getFields()) {
                        fieldLabels.add(field.getLabel());
                    }
                    box.setReadonly(true);
                    box.setModel(new SimpleListModel<String>(fieldLabels));
                }
            }


            form.getRows().appendChild(row);

            boxMap.put(column, box);
        }

        this.setWidth("200px");
        this.setVflex("1");
        this.setClosable(true);
    }

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
        Node contact = createContact();
        if (contact == null) {
            return;
        }

        // update interface
        ContactChooser cc = (ContactChooser) getParent();
        cc.onNewContactCreated(contact, false);
        this.setParent(null);
    }

    @Listen("onClick = #createAndAddBtn")
    public void createAndAdd() throws InterruptedException {
        Node contact = createContact();
        if (contact == null) {
            return;
        }

        // update interface
        ContactChooser cc = (ContactChooser) getParent();
        cc.onNewContactCreated(contact, true);
        this.setParent(null);
    }

    @Listen("onClick = #cancelBtn")
    public void cancel() {
        this.setParent(null);
    }

    private Node createContact() throws InterruptedException {
        Comboitem item = typeBox.getSelectedItem();
        if (item == null) {
            Messagebox.show("Contact type is required.");
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
        Node contact = new Node();
        contact.setType(nodeType);
        contact.setUsername(username);
        contact.setLabel(label);
        if (nodeType.equals(Constants.NODE_TYPE_USER)){
	        contact.setFirstName(firstName);
	        contact.setLastName(lastName);
	        contact.setMidName(midName);
        }

        String defaultPassword = survey.getAttribute(Constants.SURVEY_DEFAULT_PASSWORD);
        if (defaultPassword == null) {
            defaultPassword = "sonic";
        }
        if (defaultPassword.equals("rAnDoM")) {
            RandomString rs = new RandomString(8);
            defaultPassword = rs.nextString();
        }
        contact.setPassword(defaultPassword);

        for (String column : boxMap.keySet()) {
            Combobox box = boxMap.get(column);
            String value = box.getValue().trim();
            if (column.equals("department")) {
                contact.setDepartment(value);
            } else if (column.equals("organization")) {
                contact.setOrganization(value);
            } else if (column.equals("unit")) {
                contact.setUnit(value);
            } else if (column.equals("city")) {
                contact.setCity(value);
            } else if (column.equals("state")) {
                contact.setState(value);
            } else if (column.equals("country")) {
                contact.setCountry(value);
            } else if (column.equals("zipcode")) {
                contact.setZipcode(value);
            } else if (column.startsWith("Q" + Constants.SEPERATOR)) {
                String shortName = column.substring(2);
                Question question = questionDao.findByShortName(shortName);
                if (question != null && question.isSingleChoice()) {
                    Field field = question.getFieldByLabel(value);
                    String key = question.makeFieldKey(field);
                    contact.setAttribute(key, "1"); // ignore "Other" case for now	
                }
            }
        }


        contact.setAttribute(Constants.NODE_LOGIN_MODE, survey.getAttribute(Constants.SURVEY_DEFAULT_LOGIN_MODE));


        // role
        contact.getRoles().add(roleDao.findByName(Constants.ROLE_USER));

        // groups
        Set<Group> groups = new HashSet<Group>();
        ContactChooser cc = (ContactChooser) getParent();
        groups.addAll(cc.getQuestion().getAvailableGroups());
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

        contact.setGroups(groups);

        // save
        nodeDao.save(contact);
        logger.debug("Contact created: " + username);

        return contact;
    }
}
