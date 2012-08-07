package ciknow.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Constants {
	private Constants(){
		
	}

    // Application scope attribute names
    public static final String APP_BASE_URL = "ciknow.base.url";
    public static final String APP_REAL_PATH = "ciknow.real.path";
    public static final String APP_SURVEY = "ciknow.survey";
    // Session scope attribute names;
    public static final String SESSION_DESKTOP_WIDTH = "ciknow.desktop.width";
    public static final String SESSION_DESKTOP_HEIGHT = "ciknow.desktop.height";
    public static final String SESSION_SURVEY_CONTROLLER = "ciknow.controller";
    public static final String SESSION_RESPONDENT = "ciknow.respondent";
    public static final String SESSION_CURRENT_PAGE = "ciknow.currentPage";
    public static final String SESSION_SLIDE_IN_ANCHOR = "ciknow.slideInAnchor";
	public static final String SESSION_VISIBLE_PAGES = "ciknow.visiblePages";
	
	// filter operators
	public static final String EQUAL = "eq";
	public static final String NOT_EQUAL = "neq";
	public static final String GREATER_THAN = "gt";
	public static final String LESS_THAN = "lt";
	
	
	//
	public static final String NODE_TYPE_USER = "user";
	public static final String NODE_TYPE_TAG = "tag";
	public static final String NODE_TYPE_FOCAL = "focal";
	public static final String GROUP_ALL = "ALL";
	public static final String GROUP_USER = "USER";
	public static final String GROUP_NODE_TYPE_PREFIX = "TYPE_";
	public static final String GROUP_TAG = GROUP_NODE_TYPE_PREFIX + NODE_TYPE_TAG;	
	public static final String GROUP_DEPT_PREFIX = "DEPT_";
	public static final String GROUP_ORGANIZATION_PREFIX = "ORG_";
	public static final String GROUP_UNIT_PREFIX = "UNIT_";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_HIDDEN = "ROLE_HIDDEN";
	public static final String ROLE_USER = "ROLE_USER";
	
	
    // CIKNOW GRAPH
	// Graph attributes
	public static final String NODE_ID = "id";
	public static final String NODE_VERSION = "version";
	public static final String NODE_TYPE = "type";
	public static final String NODE_LABEL = "label";
	public static final String NODE_URI = "uri";
	public static final String EDGE_ID = "id";
	public static final String EDGE_VERSION = "version";
	public static final String EDGE_TYPE = "type";
	public static final String EDGE_WEIGHT = "weight";
	public static final String EDGE_DIRECTION = "directed";


	// Export options
	public static final String IO_EXPORT_BY_COLUMN="exportByColumn";
	public static final String IO_REMOVE_NON_RESPONDENT="removeNonRespondent";
	// if ignoreActivities=true, always assume respondents have answered question.
	// this is to accommodate the situation when project admin upload data and then export while no survey respondents get involved.
	public static final String IO_IGNORE_ACTIVITIES = "ignoreActivities"; 
	public static final String IO_KEEP_EMPTY_PRIVATE_GROUP="keepEmptyPrivateGroup";
	public static final String IO_OUTPUT_FORMAT="outputFormat";
	
    // CIKNOW RECOMMENDATION
    public static final int AND = 0;
	public static final int OR = 1;

	// algorithm	
	public static final String ALG_SEUCLIDEAN = "seuclidean";
	public static final String ALG_PEARSON = "pearson";
	public static final String ALG_COSINE = "cosine";
	public static final String ALG_EUCLIDEAN = "euclidean";
	public static final String ALG_SPMATCH = "spmatch";
	public static final String ALG_PMATCH = "pmatch";
	//public static final String ALG_GEODESIC = "geodesic";
	public static final String ALG_SP = "sp";
	public static final String ALG_ERGM = "ergm";
	public static final String ALG_SIMILARITY = "similarity";
	public static final String PREFIX_SIMILARITY = "sm.";
	public static final String[] ALG_SIMILARITIES = {ALG_EUCLIDEAN, ALG_SEUCLIDEAN, ALG_PEARSON, ALG_COSINE, ALG_PMATCH, ALG_SPMATCH};
	
	// source
	public static final String SOURCE_GRAND = "grand";
	public static final String SOURCE_PERSON = "Person";
	public static final String SOURCE_PROJECT = "Project";
	public static final String SOURCE_CATEGORY = "Category";
	public static final String SOURCE_PAPER = "Paper";

	// mahout	
	public static final String MAHOUT_RECOMMENDER_TYPE_USER = "GenericUserBasedRecommender";
	public static final String MAHOUT_RECOMMENDER_TYPE_USER_BOOLEAN = "GenericBooleanPrefUserBasedRecommender";
	public static final String MAHOUT_RECOMMENDER_TYPE_ITEM = "GenericItemBasedRecommender";
	public static final String MAHOUT_RECOMMENDER_TYPE_SLOPEONE = "SlopeOneRecommender";
	public static final String MAHOUT_SIMILARITY_PEARSON = "PearsonCorrelationSimilarity";
	public static final String MAHOUT_SIMILARITY_EUCLIDEAN = "EuclideanDistanceSimilarity";
	public static final String MAHOUT_SIMILARITY_SPEARMAN = "SpearmanCorrelationSimilarity";
	public static final String MAHOUT_SIMILARITY_COSINE = "UncenteredCosineSimilarity";
	public static final String MAHOUT_SIMILARITY_TANIMOTO = "TanimotoCoefficientSimilarity";
	public static final String MAHOUT_SIMILARITY_LOG = "LogLikelihoodSimilarity";
	public static final String MAHOUT_NEIGHBORHOOD_NEARESTN = "NearestNUserNeighborhood";
	public static final String MAHOUT_NEIGHBORHOOD_THRESHOLD = "ThresholdUserNeighborhood";
	
	public static final String MAHOUT_EVALUATION_TYPE_DIFF = "diff";
	public static final String MAHOUT_EVALUATION_TYPE_IR = "irstats";
	public static final String MAHOUT_EVALUATION_TYPE_PERFORMANCE = "performance";
	
	public static final String MAHOUT_EVALUATOR_AVERAGE_ABSOLUTE_DIFF = "AverageAbsoluteDifferenceRecommenderEvaluator";
	public static final String MAHOUT_EVALUATOR_RMS = "RMSRecommenderEvaluator";
	public static final String MAHOUT_EVALUATOR_GENERIC_IRSTATS = "GenericRecommenderIRStatsEvaluator";

	// hibernate association
	public static final String PROXY = "proxy";
	public static final String NORMAL = "normal";
	public static final String FETCH = "fetch";
    
    // CIKNOW SURVEY
	public static final String DISPLAY_PAGE = "DisplayPage";
	
    public static final String RATING = "Rating";
	public static final String CHOICE = "Choice";
	public static final String CONTINUOUS = "Continuous";
	public static final String TEXT = "Text";
	//public static final String TEXT_QUICK = "TextQuick";
	public static final String TEXT_LONG = "TextLong";
	public static final String DURATION_CHOOSER = "DurationChooser";
	public static final String CONTACT_INFO = "ContactInfo";
	public static final String MULTIPLE_CHOICE = "MultipleChoice";
	public static final String MULTIPLE_RATING = "MultipleRating";
	
	public static final String CONTACT_CHOOSER = "ContactChooser";
	public static final String CONTACT_PROVIDER = "ContactProvider";
	
	public static final String RELATIONAL_CHOICE = "RelationalChoice";
	public static final String RELATIONAL_RATING = "RelationalRating";
	public static final String RELATIONAL_CONTINUOUS = "RelationalContinuous";
	public static final String PERCEIVED_RATING = "PerceivedRating";
	public static final String PERCEIVED_CHOICE = "PerceivedChoice";
	public static final String PERCEIVED_RELATIONAL_CHOICE = "PerceivedRelationalChoice";
	public static final String PERCEIVED_RELATIONAL_RATING = "PerceivedRelationalRating";
	public static final String RELATIONAL_CHOICE_MULTIPLE = "RelationalChoiceMultiple";
	public static final String RELATIONAL_RATING_MULTIPLE = "RelationalRatingMultiple";

    public static final String[] questionTypes = {
        CHOICE, RATING, CONTINUOUS, TEXT, TEXT_LONG, DURATION_CHOOSER, CONTACT_INFO,
        MULTIPLE_CHOICE, MULTIPLE_RATING,
        CONTACT_CHOOSER, CONTACT_PROVIDER,
        RELATIONAL_CHOICE, RELATIONAL_RATING, RELATIONAL_CONTINUOUS,
        RELATIONAL_CHOICE_MULTIPLE, RELATIONAL_RATING_MULTIPLE,
        PERCEIVED_CHOICE, PERCEIVED_RATING,
        PERCEIVED_RELATIONAL_CHOICE, PERCEIVED_RELATIONAL_RATING,
        DISPLAY_PAGE
    };
    
	public static final String SCALE_KEY = "scale";
	public static final String NOT_ANSWERED="NA";
	
	// activities
	public static final String ACT_SURVEY_START = "SURVEY_START";
	public static final String ACT_SURVEY_FINISH = "SURVEY_FINISH";
	public static final String ACT_PAGE_ENTER_PREFIX = "PAGE_ENTER_";
	public static final String ACT_PAGE_LEAVE_PREFIX = "PAGE_LEAVE_";
	
	//public static final String SURVEY_MAX_SEQUENCE_NUMBER = "SURVEY_MAX_SEQUENCE_NUMBER";
	/*
	public static final String NODE_MAX_ANSWERED_PAGE_NAME = "NODE_MAX_ANSWERED_PAGE_NAME";
	public static final String NODE_LAST_ANSWERED_PAGE_NAME = "NODE_LAST_ANSWERED_PAGE_NAME";
	public static final String NODE_LAST_ANSWERED_PAGE_TIME = "NODE_LAST_ANSWERED_PAGE_TIME";
	public static final String NODE_TS_START_SURVEY = "NODE_TS_START_SURVEY";
	public static final String NODE_TS_FINISH_SURVEY = "NODE_TS_FINISH_SURVEY";
	public static final String NODE_TS_PREFIX = "NODE_TS_";	
	*/
	public static final String NODE_LOGIN_MODE = "NODE_LOGIN_MODE";
	public static final String NODE_FIRST_TIMER = "NODE_FIRST_TIMER";
	public static final String NODE_JUMP_FROM_TO_QUESTIONS = "jumpFromToQuestions";
	public static final String NODE_SKIP_FROM_TO_PAGES = "skipFromToPages";
	public static final String[] NODE_PREFERENCE_KEYS = {
		NODE_LOGIN_MODE, 
		NODE_FIRST_TIMER};
	public static final List<String> getNodePreferenceKeys(){
		List<String> keys = new LinkedList<String>();
		for (String key : NODE_PREFERENCE_KEYS){
			keys.add(key);
		}
		return keys;
	}
	public static final String NODE_PROGRESS_FINISHED = "FINISHED";			// respondent press the "finish" button
	public static final String NODE_PROGRESS_COMPLETED = "COMPLETED";		// complete the survey but not press the "finish" button
	public static final String NODE_PROGRESS_NOT_COMPLETED = "NOT_COMPLETED";	// not yet complete the survey
	public static final String NODE_PROGRESS_NOT_STARTED = "NOT_STARTED";	// not yet started
	public static final String NODE_PROGRESS_ANY = "ANY";
	
	public static final String CLEAR_DATA_TRACES = "traces";
	public static final String CLEAR_DATA_CONTACTS = "contacts";
	public static final String CLEAR_DATA_ALL_BUT_CONTACTS = "allButContacts";
	
	public static final String SEPERATOR = "`";
	public static final String TAGGING_PREFIX = "tagging" + SEPERATOR;
	
	// SURVEY ATTRIBUTES
	public static final String SURVEY_ADMIN_EMAIL = "SURVEY_ADMIN_EMAIL";
	public static final String SURVEY_DEFAULT_PASSWORD = "SURVEY_DEFAULT_PASSWORD";
	public static final String SURVEY_SHOW_LOGIN_LIST = "SURVEY_SHOW_LOGIN_LIST";
	public static final String SURVEY_DEFAULT_LOGIN_MODE = "SURVEY_DEFAULT_LOGIN_MODE";
	public static final String SURVEY_LAST_UPDATE_USERNAME = "SURVEY_LAST_LOGIN_USERNAME";		
	public static final String SURVEY_ALLOW_USER_CREATED_NODE = "SURVEY_ALLOW_USER_CREATED_NODE";
	public static final String SURVEY_ALLOW_SELF_REGISTER = "SURVEY_ALLOW_SELF_REGISTER";
	public static final String SURVEY_SELF_REGISTER_GROUPS = "SURVEY_SELF_REGISTER_GROUPS";
	public static final String SURVEY_FORCE_NEW_USER_CHANGE_PASSWD = "SURVEY_FORCE_NEW_USER_CHANGE_PASSWORD";
	public static final String SURVEY_SHOW_SURVEY = "SURVEY_SHOW_SURVEY";
	public static final String SURVEY_REQUIRE_PASSWORD = "SURVEY_REQUIRE_PASSWORD";
	public static final String SURVEY_PASSWORD = "SURVEY_PASSWORD"; // password to protect the survey which is exposed without authentication
	public static final String SURVEY_SHOW_GAME = "SURVEY_SHOW_GAME";
	public static final String SURVEY_SHOW_REPORT = "SURVEY_SHOW_REPORT";
	public static final String SURVEY_SHOW_RAW_ATTRIBUTES_IN_REPORT = "SURVEY_SHOW_RAW_ATTRIBUTES_IN_REPORT";
	public static final String SURVEY_SHOW_VIS = "SURVEY_SHOW_VIS";
	public static final String SURVEY_SHOW_REC = "SURVEY_SHOW_REC";		
	public static final String SURVEY_DISABLE_USER_ON_FINISH = "SURVEY_DISABLE_USER_ON_FINISH";
	
	// QUESTION ATTRIBUTES
	// question attributes
	//public static final String DEFAULT_FIELD = "defaultField"; // choice.mxml
	//public static final String DEFAULT_SCALE = "defaultScale"; // rating.mxml, relationalrating.mxml
	public static final String IS_MANDATORY = "isMandatory";	// choice (for now)
	public static final String CHOICE_LIMIT = "choiceLimit"; // choice.mxml
	public static final String SHOW_SINGLE_CHOICE_AS_LIST = "showSingleChoiceAsList";
	public static final String DISPLAY_RATING_AS_DROPDOWN_LIST = "displayRatingAsDropdownList"; // rating, relational rating.mxml
	public static final String SHOW_SELECT_ALL = "showSelectAll"; // choice.mxml and relationalChoice.mxml
	public static final String RECORD_DURATION = "recordDuration"; // for relational continous question
	public static final String CC_LEVEL = "ccLevel"; // CCConfig.xml
	public static final String CP_SHORT_NAME = "cpShortName"; // contact provider question shortName linked to a question
	public static final String CC_SHORT_NAME = "ccShortName"; // contact chooser question shortName linked to a question
	public static final String CC_NEGATE = "ccNegate";	// negate the contact chooser
	public static final String CC_SELECTED_NODES_HEADER = "ccSelectedNodesHeader";
	public static final String ADG_COLUMN_SORT_FIELDS = "adgColumnSortFields";	// ContactChooser, CCSort.mxml
	public static final String SKIP_ON_EMPTY = "SKIP_ON_EMPTY"; 
	//public static final String SKIP_ON_INACTIVITY = "SKIP_ON_INACTIVITY";	
	public static final String CC_EMPTY = "ccEmpty";
	public static final String CC_EMPTY_ALL = "ALL";
	public static final String CC_EMPTY_NONE = "NONE";
														
	public static final String ALLOW_USER_CREATED_NODE = "allowUserCreatedNode";
	public static final String HIDE_CONTACT_CHOOSER_INSTRUCTION = "hideContactChooserInstruction";
	public static final String NEW_CONTACT_DEFAULT_TYPE = "newContactDefaultType"; // Msic.mxml
	public static final String SINGLE_CHOICE_PER_LINE = "singleChoicePerLine";	// for multiple relation choice question type only
	public static final String JUMP_CONDITION = "jumpCondition";
	public static final String JUMP_QUESTION = "jumpQuestion";	
	public static final String SHOW_IMAGE = "showImage";	// for contact chooser question
	public static final String OTHER = "other";
	
	public static final String OPTION_HIDDEN = "Option_HiddenFromVisulization";
	public static final String OPTION_SINGLE_CHOICE = "Option_IsSingleChoice";
	public static final String OPTION_ALLOW_USER_CREATE_CHOICE = "Option_AllowUserCreateChoice";
	public static final String OPTION_SHOW_SELF = "Option_ShowSelf";
	
	public static final String S_MAX = "s_max";
	public static final String S_MIN = "s_min";
	public static final String S_AVR = "s_average";	
	
	
	// this should be synchronized with ModelLocator.as
	public static final String[] COLORABLE_PROPERTIES = {"type", "city", "state", "country", "zipcode", "organization", "department", "unit"};
	
	public static final String ACTIVITY_LOGIN = "login";
	public static final String ACTIVITY_LOGOUT = "logout";
	
	public static final int HIBERNATE_BATCH_SIZE = 10000;
	
	private static final String[] DEFAULT_CONTACT_FIELDS={
		"Username"
		, "FirstName"
		, "LastName"
		, "MidName"
		, "Address 1"
		, "Address 2"
		, "City"
		, "State"
		, "Country"
		, "Zip"
		, "Email"
		, "Phone"
		, "Cell"
		, "Fax"
        , "Department"
        , "Organization"
        , "Unit"
        , "Enabled"        
        , "NodeLabel"
        , "NodeURI"
        , "NodeType"};
	
	public static final List<String> getDefaultContactFields(){
		List<String> lists = new ArrayList<String>();
		for (String s : DEFAULT_CONTACT_FIELDS){
			lists.add(s);
		}
		return lists;
	}
	
	// contact fields				
	public static final String CONTACT_FIELD_ADDR1 = "AddressLine1";
	public static final String CONTACT_FIELD_ADDR2 = "AddressLine2";
	public static final String CONTACT_FIELD_CITY = "City";
	public static final String CONTACT_FIELD_STATE = "State";
	public static final String CONTACT_FIELD_COUNTRY = "Country";
	public static final String CONTACT_FIELD_ZIP = "Zip";
	public static final String CONTACT_FIELD_PHONE = "Phone";
	public static final String CONTACT_FIELD_CELL = "Cell";
	public static final String CONTACT_FIELD_FAX = "Fax";
	public static final String CONTACT_FIELD_EMAIL = "Email";
	public static final String CONTACT_FIELD_URL = "URL";
	public static final String CONTACT_FIELD_DEPARTMENT = "Department";
	public static final String CONTACT_FIELD_ORGANIZATION = "Organization";
	public static final String CONTACT_FIELD_UNIT = "Unit";	
	
	public static List<String> PREDEFINED_CONTACT_FIELDS = 
			new ArrayList<String>(
					Arrays.asList(
			Constants.CONTACT_FIELD_ADDR1,
			Constants.CONTACT_FIELD_ADDR2,
			Constants.CONTACT_FIELD_CITY,
			Constants.CONTACT_FIELD_STATE,
			Constants.CONTACT_FIELD_COUNTRY,
			Constants.CONTACT_FIELD_ZIP,
			Constants.CONTACT_FIELD_PHONE,
			Constants.CONTACT_FIELD_CELL,
			Constants.CONTACT_FIELD_FAX,
			Constants.CONTACT_FIELD_EMAIL,
			Constants.CONTACT_FIELD_URL,
			Constants.CONTACT_FIELD_DEPARTMENT,
			Constants.CONTACT_FIELD_ORGANIZATION,
			Constants.CONTACT_FIELD_UNIT));

}