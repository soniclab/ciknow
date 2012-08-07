package ciknow.domain;

import ciknow.dao.GroupDao;
import ciknow.dao.QuestionDao;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.SurveyUtil;
import ciknow.zk.survey.response.ContactChooserItem;
import ciknow.zk.survey.response.ContactChooserItemComparator;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author gyao
 */
public class Question implements java.io.Serializable {

    private static final long serialVersionUID = 3190999211576331013L;
    private static Log logger = LogFactory.getLog(Question.class);
    private Long id;
    private Long version;
    private Page page;
    private String label;
    private String shortName;
    private String type;
    private String instruction;
    private String htmlInstruction;
    private Integer rowPerPage;
    private Map<String, String> attributes = new HashMap<String, String>();
    private Map<String, String> longAttributes = new HashMap<String, String>();
    private List<Field> fields = new ArrayList<Field>();
    private List<Scale> scales = new ArrayList<Scale>();
    private List<ContactField> contactFields = new ArrayList<ContactField>();
    private List<TextField> textFields = new ArrayList<TextField>();
    private Set<Group> visibleGroups = new HashSet<Group>(); // this question is visible to groups of nodes
    private Set<Group> availableGroups = new HashSet<Group>(); // groups of nodes are available in relational question
    private Set<Group> availableGroups2 = new HashSet<Group>(); // groups of nodes in the columns of the relational matrix

    public Question() {
    }

    /**
     * Copy constructor
     * @param question
     */
    public Question(Question question){
		this.update(question);
		this.setId(null);
		this.setVersion(null);
		
		this.getFields().clear();
		for (Field field : question.getFields()){
			Field newField = new Field(field);
			newField.setQuestion(this);
			this.getFields().add(newField);
		}
		
		this.getScales().clear();
		for (Scale scale : question.getScales()){
			Scale newScale = new Scale(scale);
			newScale.setQuestion(this);
			this.getScales().add(newScale);
		}
		
		this.getTextFields().clear();
		for (TextField textField : question.getTextFields()){
			TextField newTextField = new TextField(textField);
			newTextField.setQuestion(this);
			this.getTextFields().add(newTextField);
		}
		
		this.getContactFields().clear();
		for (ContactField contactField : question.getContactFields()){
			ContactField newContactField = new ContactField(contactField);
			newContactField.setQuestion(this);
			this.getContactFields().add(newContactField);
		}
    }
    
    /**
     * Synchronize current question with specified question (with the same questionId)
     * @param question	- might be the latest version of question from database
     */
    public void update(Question question) {
        this.id = question.getId();
        this.version = question.getVersion();
        this.page = question.getPage();
        this.shortName = question.getShortName();
        this.label = question.getLabel();
        this.type = question.getType();
        this.instruction = question.getType();
        this.htmlInstruction = question.getHtmlInstruction();
        this.rowPerPage = question.getRowPerPage();

        this.attributes = new HashMap<String, String>(question.getAttributes());
        this.longAttributes = new HashMap<String, String>(question.getLongAttributes());

        this.fields = new ArrayList<Field>(question.getFields());
        this.scales = new ArrayList<Scale>(question.getScales());
        this.textFields = new ArrayList<TextField>(question.getTextFields());
        this.contactFields = new ArrayList<ContactField>(question.getContactFields());

        this.visibleGroups = new HashSet<Group>(question.getVisibleGroups());
        this.availableGroups = new HashSet<Group>(question.getAvailableGroups());
        this.availableGroups2 = new HashSet<Group>(question.getAvailableGroups2());
    }
    
    
    /**********************************************************
     * Question Types
     *********************************************************/
    public Boolean isChoice() {
        return type.equals(Constants.CHOICE);
    }

    public Boolean isRating() {
        return type.equals(Constants.RATING);
    }

    public Boolean isContinuous() {
        return type.equals(Constants.CONTINUOUS);
    }

    public Boolean isMultipleChoice() {
        return type.equals(Constants.MULTIPLE_CHOICE);
    }

    public Boolean isMultipleRating() {
        return type.equals(Constants.MULTIPLE_RATING);
    }

    public Boolean isDuration() {
        return type.equals(Constants.DURATION_CHOOSER);
    }

    public Boolean isText() {
        return type.equals(Constants.TEXT);
    }

    /*
    public Boolean isTextQuick() {
        return type.equals(Constants.TEXT_QUICK);
    }
	*/
    
    public Boolean isTextLong() {
        return type.equals(Constants.TEXT_LONG);
    }

    public Boolean isContactInfo() {
        return type.equals(Constants.CONTACT_INFO);
    }

    public Boolean isContactChooser() {
        return type.equals(Constants.CONTACT_CHOOSER);
    }

    public Boolean isContactProvider() {
        return type.equals(Constants.CONTACT_PROVIDER);
    }
    
    public Boolean isDisplayPage() {
        return type.equals(Constants.DISPLAY_PAGE);
    }

    public Boolean isRelationalChoice() {
        return type.equals(Constants.RELATIONAL_CHOICE);
    }

    public Boolean isRelationalContinuous() {
        return type.equals(Constants.RELATIONAL_CONTINUOUS);
    }

    public Boolean isRelationalRating() {
        return type.equals(Constants.RELATIONAL_RATING);
    }

    public Boolean isRelationalChoiceMultiple() {
        return type.equals(Constants.RELATIONAL_CHOICE_MULTIPLE);
    }

    public Boolean isRelationalRatingMultiple() {
        return type.equals(Constants.RELATIONAL_RATING_MULTIPLE);
    }

    public Boolean isPerceivedChoice() {
        return type.equals(Constants.PERCEIVED_CHOICE);
    }

    public Boolean isPerceivedRating() {
        return type.equals(Constants.PERCEIVED_RATING);
    }

    public Boolean isPerceivedRelationalChoice() {
        return type.equals(Constants.PERCEIVED_RELATIONAL_CHOICE);
    }

    public Boolean isPerceivedRelationalRating() {
        return type.equals(Constants.PERCEIVED_RELATIONAL_RATING);
    }

    public Boolean isExportable() {
        return (this.isChoice()
                || this.isRating()
                || this.isContinuous()
                || this.isMultipleChoice()
                || this.isMultipleRating()
                || this.isDuration()
                || this.isText()
                //|| this.isTextQuick()
                || this.isTextLong()
                || this.isRelationalChoice()
                || this.isRelationalContinuous()
                || this.isRelationalRating()
                || this.isRelationalChoiceMultiple()
                || this.isRelationalRatingMultiple()
                || this.isPerceivedRelationalChoice()
                || this.isPerceivedRelationalRating()
                || this.isPerceivedChoice()
                || this.isPerceivedRating()
                || this.isContactChooser()
                || this.isContactProvider());
    }

    public Boolean isAttribute(){
    	return (this.isChoice() || this.isRating() || this.isContinuous()
    			|| this.isMultipleChoice() || this.isMultipleRating()
    			|| this.isDuration() || this.isText() || this.isTextLong()
    			|| this.isContactInfo());
    }
    
    public Boolean isRelational() {
        if (isRelationalChoice()
                || isRelationalRating()
                || isRelationalContinuous()
                || isRelationalChoiceMultiple()
                || isRelationalRatingMultiple()
                || isPerceivedChoice()
                || isPerceivedRating()
                || isPerceivedRelationalChoice()
                || isPerceivedRelationalRating()) {
            return true;
        } else {
            return false;
        }
    }
    
    /*******************************************************
     * Query for field, scale, text field and contact field
     ******************************************************/
    /*
     * get the first field matching given name, ideally there should not be
     * duplicate
     */
    public Field getFieldByName(String name) {
        for (Field f : fields) {
            if (f.getName().equals(name)) {
                return f;
            }
        }

        return null;
    }

    public Field getFieldByLabel(String label) {
        for (Field f : fields) {
            if (f.getLabel().equals(label)) {
                return f;
            }
        }

        return null;
    }

    /*
     * get the first scale matching given name, ideally there should not be
     * duplicate
     */
    public Scale getScaleByName(String name) {
        for (Scale s : scales) {
            if (s.getName().equals(name)) {
                return s;
            }
        }

        return null;
    }

    public Scale getScaleByLabel(String label) {
        for (Scale s : scales) {
            if (s.getLabel().equals(label)) {
                return s;
            }
        }

        return null;
    }

    public Scale getScaleByValue(Double v) {
        for (Scale s : scales) {
            if (s.getValue().equals(v)) {
                return s;
            }
        }
        return null;
    }

    /*
     * get the first contact field matching given name, ideally there should not
     * be duplicate
     */
    public ContactField getContactFieldByName(String name) {
        for (ContactField cf : contactFields) {
            if (cf.getName().equals(name)) {
                return cf;
            }
        }

        return null;
    }

    /*
     * get the first text field matching given name, ideally there should not be
     * duplicate
     */
    public TextField getTextFieldByName(String name) {
        for (TextField tf : textFields) {
            if (tf.getName().equals(name)) {
                return tf;
            }
        }

        return null;
    }

    
    
    /*******************************************
     * Key Making, Parsing and Labeling
     *******************************************/
    public String makeFieldKey(Field field) {
        return "F" + Constants.SEPERATOR + shortName + Constants.SEPERATOR + field.getName();
    }

    public String makeFieldsKey(Field field, TextField tf) {
        return "FT"
                + Constants.SEPERATOR + shortName
                + Constants.SEPERATOR + field.getName()
                + Constants.SEPERATOR + tf.getName();
    }

    public String makeScaleKey(Scale scale) {
        return "S" + Constants.SEPERATOR + shortName + Constants.SEPERATOR + scale.getName();
    }

    public String makeTextFieldKey(TextField field) {
        return "T" + Constants.SEPERATOR + shortName + Constants.SEPERATOR + field.getName();
    }

    public String makeContactFieldKey(ContactField cf) {
        return "CF" + Constants.SEPERATOR + shortName + Constants.SEPERATOR + cf.getName();
    }

    public static String getShortNameFromKey(String key) {
        String[] arr = key.split(Constants.SEPERATOR, -1);
        return arr[1];
    }

    public static String getFieldNameFromKey(String key) {
        String[] arr = key.split(Constants.SEPERATOR, -1);
        return arr[2];
    }

    public static String getScaleNameFromKey(String key) {
        String[] arr = key.split(Constants.SEPERATOR, -1);
        return arr[2];
    }

    public static String getTextFieldNameFromKey(String key) {
        String[] arr = key.split(Constants.SEPERATOR, -1);
        return arr[2];
    }

    public static String getTextFieldNameFromFT(String key) {
        String[] arr = key.split(Constants.SEPERATOR, -1);
        return arr[3];
    }

    public static String getKeyLabel(Map<String, Question> qmap, String key) {
        if (!key.contains(Constants.SEPERATOR)) {
            return key;
        }

        String shortName = getShortNameFromKey(key);
        Question question = qmap.get(shortName);
        if (question == null) {
            return null;
        }

        if (question.isChoice()
                || question.isRating()
                || question.isContinuous()
                || question.isDuration()
                || question.isTextLong()
                //|| question.isTextQuick()
                ) {
            String fieldName = getFieldNameFromKey(key);
            Field field = question.getFieldByName(fieldName);
            if (field == null) {
                return null;
            }
            return question.getLabel() + "::" + field.getLabel();
        } else if (question.isText()) {
            String textFieldName = getTextFieldNameFromKey(key);
            TextField tf = question.getTextFieldByName(textFieldName);
            if (tf == null) {
                return null;
            }
            return question.getLabel() + "::" + tf.getLabel();
        } else if (question.isMultipleChoice()
                || question.isMultipleRating()) {
            String fieldName = getFieldNameFromKey(key);
            Field field = question.getFieldByName(fieldName);
            String textFieldName = getTextFieldNameFromFT(key);
            TextField tf = question.getTextFieldByName(textFieldName);
            if (field == null || tf == null) {
                return null;
            }
            return (question.getLabel() + "::" + tf.getLabel() + "::" + field.getLabel());
        } else {
            return key;
        }
    }

    public static String getValueLabel(Map<String, Question> qmap, String value) {
        if (!value.contains(Constants.SEPERATOR)) {
            return value;
        }

        String shortName = getShortNameFromKey(value);
        Question question = qmap.get(shortName);
        if (question == null) {
            return null;
        }

        if (question.isRating()
                || question.isMultipleRating()
                || question.isRelationalRating()
                || question.isRelationalRatingMultiple()
                || question.isPerceivedRelationalRating()) {
            String scaleName = getScaleNameFromKey(value);
            Scale scale = question.getScaleByName(scaleName);
            if (scale == null) {
                return null;
            }
            return question.getLabel() + "::" + scale.getLabel();
        } else {
            return value;
        }
    }

    
    
    /******************************************
     * Edge Type Making and Parsing
     ******************************************/
    public String getEdgeType() {
        if (type.equals(Constants.RELATIONAL_CHOICE)
                || type.equals(Constants.RELATIONAL_RATING)
                || type.equals(Constants.RELATIONAL_CONTINUOUS)
                || type.equals(Constants.PERCEIVED_RELATIONAL_CHOICE)
                || type.equals(Constants.PERCEIVED_RELATIONAL_RATING)) {
            return shortName;
        } else if (type.equals(Constants.PERCEIVED_CHOICE)
                || type.equals(Constants.PERCEIVED_RATING)) {
            return Constants.TAGGING_PREFIX + shortName;
        } else if (type.equals(Constants.RELATIONAL_CHOICE_MULTIPLE)
                || type.equals(Constants.RELATIONAL_RATING_MULTIPLE)) {
            logger.warn("unsupported question type, please use getEdgeTypeWithField()");
            return null;
        } else {
            logger.warn("unsupported question type.");
            return null;
        }
    }

    public String getEdgeTypeWithField(Field field) {
        return shortName + Constants.SEPERATOR + field.getName();
    }
    
    public static String getShortNameFromEdgeType(String edgeType){
        String[] arr = edgeType.split(Constants.SEPERATOR, -1);
        return arr[0];
    }
    
    public static String getFieldNameFromEdgeType(String edgeType){
        String[] arr = edgeType.split(Constants.SEPERATOR, -1);
        if (arr.length > 1) return arr[1];
        else return null;
    }
    
    /************************************************************
     * Tag Making and Parsing (Perceived Choice/Rating)
     ************************************************************/
    public String getTagName(Field field) {
        return shortName + Constants.SEPERATOR + field.getName();
    }

    public List<String> getTagNames4PerceivedChoice() {
        List<String> names = new LinkedList<String>();
        for (Field f : fields) {
            names.add(getTagName(f));
        }
        return names;
    }

    public String getTagName(Field field, Scale scale) {
        return shortName + Constants.SEPERATOR + field.getName() + Constants.SEPERATOR + scale.getName();
    }

    public List<String> getTagNames4PerceivedRating() {
        List<String> names = new LinkedList<String>();
        for (Field f : fields) {
            for (Scale s : scales) {
                names.add(getTagName(f, s));
            }
        }
        return names;
    }

    public static String getShortNameFromTagName(String tagName) {
        String[] arr = tagName.split(Constants.SEPERATOR, -1);
        return arr[0];
    }

    public static String getFieldNameFromTagName(String tagName) {
        String[] arr = tagName.split(Constants.SEPERATOR, -1);
        return arr[1];
    }

    public static String getScaleNameFromTagName(String tagName) {
        String[] arr = tagName.split(Constants.SEPERATOR, -1);
        return arr[2];
    }



    /********************************************************
     * Visible and Available Nodes
     ********************************************************/
    public Set<Long> getVisibleNodeIds() {
        Beans.init();
        GroupDao groupDao = (GroupDao) Beans.getBean("groupDao");
        Set<Long> nodeIds = new HashSet<Long>();
        for (Group group : visibleGroups) {
            List<Long> ids = groupDao.getNodeIdsByGroupId(group.getId());
            nodeIds.addAll(ids);
        }
        return nodeIds;
    }

    public static Map<Question, Set<Long>> getQuestionVisibleNodeIdsMap(Collection<Question> questions) {
        logger.info("get question -> visible nodeIds map...");
        Map<Question, Set<Long>> m = new HashMap<Question, Set<Long>>();

        for (Question question : questions) {
            if (m.get(question) != null) {
                continue; // avoid duplicate query
            }
            Set<Long> ids = question.getVisibleNodeIds();
            m.put(question, ids);
            logger.debug("question(shortName=" + question.getShortName() + ") is visible to " + ids.size() + " nodes.");
        }

        return m;
    }

    public boolean isVisible(Node node) {
    	if (node.isAdmin()) return true;
        for (Group group : visibleGroups){
        	if (node.getGroups().contains(group)) return true;
        }
        return false;
    }

    public Set<Long> getAvailableNodeIds(boolean column) {
        Beans.init();
        GroupDao groupDao = (GroupDao) Beans.getBean("groupDao");
        Set<Long> nodeIds = new HashSet<Long>();
        for (Group group : column ? availableGroups2 : availableGroups) {
            nodeIds.addAll(groupDao.getNodeIdsByGroupId(group.getId()));
        }
        return nodeIds;
    }

    /**
     * Get available node ids for a given quesiton and login node, considering contact chooser. 
     * Note: end user need to further filter out hidden node from the return node set
     *
     * @param question
     * @param node
     * @param column
     * @return
     * @throws Exception
     */
    public static Set<Long> getCombinedAvailableNodeIds(Question question, Node loginNode, boolean column) {
        logger.info("get available nodeIds...");
        logger.debug("question: " + question.getShortName());
        logger.debug("loginNode: " + loginNode.getUsername());

        Beans.init();
        GroupDao groupDao = (GroupDao) Beans.getBean("groupDao");

        Set<Long> availableNodeIds = new HashSet<Long>();
        Set<Long> contactNodeIds = new HashSet<Long>();
        Set<Long> mandatoryNodeIds = new HashSet<Long>();
        Set<Long> nonMandatoryNodeIds = new HashSet<Long>();

        if (question.getType().equals(Constants.RELATIONAL_CHOICE)
                || question.getType().equals(Constants.RELATIONAL_CONTINUOUS)
                || question.getType().equals(Constants.RELATIONAL_RATING)
                || question.getType().equals(Constants.RELATIONAL_CHOICE_MULTIPLE)
                || question.getType().equals(Constants.RELATIONAL_RATING_MULTIPLE)
                || question.getType().equals(Constants.PERCEIVED_CHOICE)
                || question.getType().equals(Constants.PERCEIVED_RATING)
                || question.getType().equals(Constants.PERCEIVED_RELATIONAL_CHOICE)
                || question.getType().equals(Constants.PERCEIVED_RELATIONAL_RATING)
                //|| question.getType().equals(Constants.TEXT_QUICK)
                ) {
        	Question cp = question.getContactProviderQuestion();
        	if (cp != null){        		
                List<Long> selectedNodeIds = new ArrayList<Long>();
                String groupName = Group.getProviderGroupName(loginNode.getUsername(), cp.getShortName());
                Group providerGroup = groupDao.findByName(groupName);
                if (providerGroup != null) {
                    selectedNodeIds = groupDao.getNodeIdsByGroupId(providerGroup.getId());
                }
                logger.debug("got nodes from contact provider exclusively.");
                return new HashSet<Long>(selectedNodeIds);
        	}
        	
            logger.debug("get mandatory and non-mandatory nodeIds...");
            Set<Group> groups;
            if (column) {
                groups = question.getAvailableGroups2();
            } else {
                groups = question.getAvailableGroups();
            }
            for (Group group : groups) {
                List<Long> nodeIds = groupDao.getNodeIdsByGroupId(group.getId());
                if (group.isMandatory()) {
                    mandatoryNodeIds.addAll(nodeIds);
                } else {
                    nonMandatoryNodeIds.addAll(nodeIds);
                }
            }

            Question cc = question.getContactChooserQuestion();
            if (cc != null && !cc.isContactChooser()) {
            	logger.warn("Question(shortName=" + question.getShortName() + 
            			") is configured to have ContactChooser question (shortName=" + 
            			cc.getShortName() + "), but it is actually NOT a contact chooser!");
            	cc = null;
            }
            //if (cc != null && question.useContactsOnly()) {
            if (cc != null) {
                logger.debug("user selected contacts only");
                boolean negate = question.getCCNegate();
                logger.debug("negate: " + negate);
                Set<Long> ccAvailableNodeIds = getCombinedAvailableNodeIds(cc, loginNode, false);
                List<Long> selectedNodeIds = new ArrayList<Long>();
                String groupName = Group.getPrivateGroupName(loginNode.getUsername(), cc.getShortName());
                Group privateGroup = groupDao.findByName(groupName);
                if (privateGroup != null) {
                    selectedNodeIds = groupDao.getNodeIdsByGroupId(privateGroup.getId());
                    selectedNodeIds.retainAll(ccAvailableNodeIds);
                }

                logger.debug("selectedNodeIds.size()=" + selectedNodeIds.size());
                if (selectedNodeIds.isEmpty()) {
                    String strategy = cc.getCCEmptyStrategy();
                    if (strategy.equals(Constants.CC_EMPTY_NONE)) {
                        if (negate) {
                            contactNodeIds = ccAvailableNodeIds;
                        } else {
                            // empty
                        }
                    } else {
                        if (negate) {
                            // empty
                        } else {
                            contactNodeIds = ccAvailableNodeIds;
                        }
                    }
                } else {
                    if (negate) {
                        ccAvailableNodeIds.removeAll(selectedNodeIds);
                        contactNodeIds = ccAvailableNodeIds;
                    } else {
                        contactNodeIds.addAll(selectedNodeIds);
                    }
                }

                contactNodeIds.retainAll(nonMandatoryNodeIds);
                availableNodeIds = contactNodeIds;
            } else {
                availableNodeIds = nonMandatoryNodeIds;
            }

            availableNodeIds.addAll(mandatoryNodeIds);
        } else if (question.getType().equals(Constants.CONTACT_CHOOSER)) {
            Set<Group> groups = question.getAvailableGroups();

            for (Group group : groups) {
                List<Long> nodeIds = groupDao.getNodeIdsByGroupId(group.getId());
                availableNodeIds.addAll(nodeIds);
            }
        }

        // show self?
        Long nodeId = loginNode.getId();
        if (question.showMyself()) {
            availableNodeIds.add(nodeId);
        } else {
            availableNodeIds.remove(nodeId);
        }
        logger.debug("showMySelf: " + question.showMyself());

        logger.info("availableNodeIds count: " + availableNodeIds.size());
        return availableNodeIds;
    }

    
    
    /************************************************************
     * Question Options
     ************************************************************/
    /*
    public Scale getDefaultRating() {
        String scaleName = getAttribute(Constants.DEFAULT_SCALE);
        if (scaleName != null) {
            return this.getScaleByName(scaleName);
        } else {
            return null;
        }
    }
	*/
    
    /* choice */
    public boolean isSingleChoice() {
        return GeneralUtil.verify(attributes, Constants.OPTION_SINGLE_CHOICE);
    }
    public boolean showSingleChoiceAsList() {
        return GeneralUtil.verify(attributes, Constants.SHOW_SINGLE_CHOICE_AS_LIST);
    }
    public int getMaxChoice(){
    	String choiceLimitString = getAttribute(Constants.CHOICE_LIMIT);
        int choiceLimit = (choiceLimitString != null) ? Integer.parseInt(choiceLimitString) : fields.size();
        return choiceLimit;
    }
    public boolean isMandatory() {
        return GeneralUtil.verify(attributes, Constants.IS_MANDATORY);
    }
    public Field getJumpCondition(){
    	String fieldName = getAttribute(Constants.JUMP_CONDITION);
    	if (fieldName == null) return null;
    	Field field = this.getFieldByName(fieldName);
    	return field;
    }
    public Question getJumpQuestion(){
    	String shortName = getAttribute(Constants.JUMP_QUESTION);
    	Question jq = null;
    	if (shortName == null) return jq;
    	if (shortName.equals(this.shortName)){
    		logger.warn("Jump onto yourself?");
    		return jq;
    	}
    	jq = SurveyUtil.getQuestionByShortName(SurveyUtil.getAllQuestions(), shortName);
    	return jq;
    }
    
    /* rating */
    public boolean displayRatingAsDropdownList() {
        return GeneralUtil.verify(attributes, Constants.DISPLAY_RATING_AS_DROPDOWN_LIST);
    }

    /* choice and relational choice */
    public boolean showSelectAll() {
        return GeneralUtil.verify(attributes, Constants.SHOW_SELECT_ALL);
    }

    /* is attribute question */
    public boolean allowUserCreatedChoice() {
        return GeneralUtil.verify(attributes, Constants.OPTION_ALLOW_USER_CREATE_CHOICE);
    }

    /* relational continuous */
    public boolean isRecordDuration() {
        return GeneralUtil.verify(attributes, Constants.RECORD_DURATION);
    }
    
    /* perceived choice, relational choice multiple */
    public boolean isSingleChoicePerLine() {
        return GeneralUtil.verify(attributes, Constants.SINGLE_CHOICE_PER_LINE);
    }

    /* is relational */
    public Question getContactProviderQuestion() {
        Question question = null;
        String cpShortName = getAttribute(Constants.CP_SHORT_NAME);
        if (cpShortName != null) {
            Beans.init();
            QuestionDao questionDao = (QuestionDao) Beans.getBean("questionDao");
            question = questionDao.findByShortName(cpShortName);
        }
        return question;
    }

    public Question getContactChooserQuestion() {
        Question question = null;
        String ccShortName = getAttribute(Constants.CC_SHORT_NAME);
        if (ccShortName != null) {
            Beans.init();
            QuestionDao questionDao = (QuestionDao) Beans.getBean("questionDao");
            question = questionDao.findByShortName(ccShortName);
        }
        return question;
    }
    
    public boolean showMyself() {
        return GeneralUtil.verify(attributes, Constants.OPTION_SHOW_SELF);
    }
    
    public boolean getCCNegate() {
        return GeneralUtil.verify(attributes, Constants.CC_NEGATE);
    }

	public boolean skipOnEmpty(){
		return GeneralUtil.verify(attributes, Constants.SKIP_ON_EMPTY);
	}
	
    public boolean hideContactChooserInstruction() {
        return GeneralUtil.verify(attributes, Constants.HIDE_CONTACT_CHOOSER_INSTRUCTION);
    }

    /* contact chooser */
    public boolean showCCImage() {
        return GeneralUtil.verify(attributes, Constants.SHOW_IMAGE);
    }
    
    public String getCCEmptyStrategy() {
        if (type.equals(Constants.CONTACT_CHOOSER)) {
            String strategy = getAttribute(Constants.CC_EMPTY);
            if (strategy == null) {
                strategy = Constants.CC_EMPTY_NONE;
            }
            return strategy;
        } else {
            return null;
        }
    }

    public String getDefaultNewContactType(){
    	String type = getAttribute(Constants.NEW_CONTACT_DEFAULT_TYPE);
    	if (type == null || type.trim().isEmpty()) type = Constants.NODE_TYPE_USER;
    	return type;
    }
    
    public String getSelectedNodesHeader() {
        if (!type.equals(Constants.CONTACT_CHOOSER)) {
            return null;
        }
        String header = getAttribute(Constants.CC_SELECTED_NODES_HEADER);
        if (header == null || header.isEmpty()) {
            header = "Selected Contacts";
        }
        return header;
    }

    public LinkedHashMap<String, String> getCCColumns() {
        if (!type.equals(Constants.CONTACT_CHOOSER)) {
            return null;
        }

        logger.info("getColumnFields for question: " + this.getShortName());
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        String s = this.getAttribute(Constants.CC_LEVEL);
        if (s == null) {
            return map;
        }

        String[] parts = s.split(",", -1);
        for (String part : parts) {
            String[] subparts = part.split("=", -1);
            String columnName = subparts[0];
            String columnLabel = (subparts.length == 2) ? subparts[1] : subparts[0];
            map.put(columnName, columnLabel);
        }

        return map;
    }

    public void setCCColumns(LinkedHashMap<String, String> columns){
        if (!type.equals(Constants.CONTACT_CHOOSER)) {
            return;
        }
        
        if (columns == null || columns.isEmpty()){
        	this.getAttributes().remove(Constants.CC_LEVEL);
        	return;
        }
        
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String name : columns.keySet()){
        	String label = columns.get(name);
        	if (index > 0) sb.append(",");
        	sb.append(name).append("=").append(label);
        	
        	index++;
        }
        
        this.setAttribute(Constants.CC_LEVEL, sb.toString());
    }
    
    public List<Map<String, String>> getCCSortColumns() {
        if (!type.equals(Constants.CONTACT_CHOOSER)) {
            return null;
        }

        String s = getAttribute(Constants.ADG_COLUMN_SORT_FIELDS);
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        if (s == null) {
            return list;
        }

        String[] parts = s.split(",", -1);
        for (String part : parts) {
            Map<String, String> m = new HashMap<String, String>();
            String[] subparts = part.split("=", -1); 
            m.put("name", subparts[0]);
            m.put("label", subparts[1]);
            m.put("casesensitive", subparts[2]);
            m.put("numeric", subparts[3]);
            m.put("order", subparts[4]);
            list.add(m);
        }

        return list;
    }
    
    public void setCCSortColumns(List<Map<String, String>> sortColumns){
    	StringBuilder sb = new StringBuilder();
    	int index = 0;
    	for (Map<String, String> m : sortColumns){
    		if (index > 0) sb.append(",");
    		sb.append(m.get("name"));
    		sb.append("=");
    		sb.append(m.get("label"));
    		sb.append("=");
    		sb.append(m.get("casesensitive"));
    		sb.append("=");
    		sb.append(m.get("numeric"));
    		sb.append("=");
    		sb.append(m.get("order"));
    		
    		index++;
    	}
    	
    	this.setAttribute(Constants.ADG_COLUMN_SORT_FIELDS, sb.toString());
    }
    
    public List<Comparator<ContactChooserItem>> getCCSortComparators() {
        if (!type.equals(Constants.CONTACT_CHOOSER)) {
            return null;
        }

        String s = getAttribute(Constants.ADG_COLUMN_SORT_FIELDS);
        List<Comparator<ContactChooserItem>> comps = new ArrayList<Comparator<ContactChooserItem>>();
        if (s == null) {
            return comps;
        }

        String[] parts = s.split(",", -1);
        for (String part : parts) {
            String[] subparts = part.split("=", -1);
            String column = subparts[0];
            boolean casesensitive = subparts[2].equals("1");
            boolean numeric = subparts[3].equals("1");
            boolean ascending = false; // descending as default
            String orderString = subparts[4];
            if (orderString.equals("alternate")) {
                orderString = (Math.random() < 0.5) ? "ascend" : "descend";
            }
            if (orderString.equals("ascend")) {
                ascending = true;
            }

            ContactChooserItemComparator ccic = new ContactChooserItemComparator(column, ascending, numeric, casesensitive);
            comps.add(ccic);
        }

        return comps;
    }
    
    /* contact chooser, is relational */
    public boolean allowUserCreatedNode() {
        return GeneralUtil.verify(attributes, Constants.ALLOW_USER_CREATED_NODE);
    }
    
    /* general */
    // return true if this question's answers are hidden from visualization, etc
    public boolean isHidden() {
        return GeneralUtil.verify(attributes, Constants.OPTION_HIDDEN);
    }
    
    
    
    /*******************************************************************
     * Misc
     *******************************************************************/
    public static Map<String, Question> getShortNameToQuestionMap() {
        Beans.init();
        QuestionDao questionDao = (QuestionDao) Beans.getBean("questionDao");
        return getShortNameToQuestionMap(questionDao.getAll());
    }

    public static Map<String, Question> getShortNameToQuestionMap(Collection<Question> questions) {
        Map<String, Question> qmap = new HashMap<String, Question>();
        for (Question q : questions) {
            qmap.put(q.getShortName(), q);
        }
        return qmap;
    }

    public static Map<Long, Question> getIdToQuestionMap() {
        Beans.init();
        QuestionDao questionDao = (QuestionDao) Beans.getBean("questionDao");
        return getIdToQuestionMap(questionDao.getAll());
    }

    public static Map<Long, Question> getIdToQuestionMap(Collection<Question> questions) {
        Map<Long, Question> qmap = new HashMap<Long, Question>();
        for (Question q : questions) {
            qmap.put(q.getId(), q);
        }
        return qmap;
    }
    
    /**
    * Get possible attribute names (keys) for current question
    * @return null if this question is not for attribute
    */
   public Collection<String> getPossibleAttributeNames() {
       List<String> attrNames = new LinkedList<String>();
       if (isChoice() || isRating() || isContinuous() || isDuration() || isTextLong()) {
           for (Field f : fields) {
               attrNames.add(makeFieldKey(f));
           }
       } else if (isText()) {
           for (TextField tf : textFields) {
               attrNames.add(makeTextFieldKey(tf));
           }
       } else if (isMultipleChoice() || isMultipleRating()) {
           for (Field f : fields) {
               for (TextField tf : textFields) {
                   attrNames.add(makeFieldsKey(f, tf));
               }
           }
       } else {
           return null;
       }
       return attrNames;
   }
   
   
   
    /**************************************************
     * Getters and Setters
     **************************************************/
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Page getPage() {
        return this.page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Integer getIndex() {
        return page.getQuestions().indexOf(this);
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getShortName() {
        return this.shortName;
    }

    public void setShortName(String name) {
        this.shortName = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstruction() {
        return this.instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getHtmlInstruction() {
        return htmlInstruction;
    }

    public void setHtmlInstruction(String htmlInstruction) {
        this.htmlInstruction = htmlInstruction;
    }

    public Integer getRowPerPage() {
        return this.rowPerPage;
    }

    public void setRowPerPage(Integer rowPerPage) {
        this.rowPerPage = rowPerPage;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public Map<String, String> getLongAttributes() {
        return longAttributes;
    }

    public void setLongAttributes(Map<String, String> attributes) {
        this.longAttributes = attributes;
    }

    public String getLongAttribute(String key) {
        return longAttributes.get(key);
    }

    public void setLongAttribute(String key, String value) {
        longAttributes.put(key, value);
    }

    public Set<Group> getVisibleGroups() {
        return visibleGroups;
    }

    public void setVisibleGroups(Set<Group> visibleGroups) {
        this.visibleGroups = visibleGroups;
    }

    public Set<Group> getAvailableGroups() {
        return availableGroups;
    }

    public void setAvailableGroups(Set<Group> availableGroups) {
        this.availableGroups = availableGroups;
    }

    public Set<Group> getAvailableGroups2() {
        return availableGroups2;
    }

    public void setAvailableGroups2(Set<Group> availableGroups) {
        this.availableGroups2 = availableGroups;
    }

    public List<ContactField> getContactFields() {
        return contactFields;
    }

    public void setContactFields(List<ContactField> contactFields) {
        this.contactFields = contactFields;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<Scale> getScales() {
        return scales;
    }

    public void setScales(List<Scale> scales) {
        this.scales = scales;
    }

    public List<TextField> getTextFields() {
        return textFields;
    }

    public void setTextFields(List<TextField> textFields) {
        this.textFields = textFields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((shortName == null) ? 0 : shortName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Question other = (Question) obj;
        if (shortName == null) {
            if (other.shortName != null) {
                return false;
            }
        } else if (!shortName.equals(other.shortName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Question[\n");
        sb.append("id=").append(id).append(", \n");
        sb.append("version=").append(version).append(", \n");
        sb.append("pageId=").append(page.getId()).append(", \n");
        sb.append("index=").append(getIndex()).append(", \n");
        sb.append("type=").append(type).append(", \n");
        sb.append("shortName=").append(shortName).append(", \n");
        sb.append("label=").append(label).append(", \n");
        sb.append("instruction=").append(instruction).append(", \n");
        sb.append("rowPerPage=").append(rowPerPage).append(", \n");
        for (Field field : fields) {
            sb.append(field).append("\n");
        }
        for (Scale scale : scales) {
            sb.append(scale).append("\n");
        }
        for (ContactField field : contactFields) {
            sb.append(field).append("\n");
        }
        for (TextField field : textFields) {
            sb.append(field).append("\n");
        }

        sb.append("]");
        return sb.toString();
    }
}
