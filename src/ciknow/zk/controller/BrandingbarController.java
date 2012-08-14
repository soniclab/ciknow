package ciknow.zk.controller;

import ciknow.dao.NodeDao;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.ro.GenericRO;
import ciknow.service.ActivityService;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.SurveyUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
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
public class BrandingbarController extends SelectorComposer<Component> {

    private static final long serialVersionUID = -9135576397887656457L;
    private static final String ACTIVE_BUTTON = "activeButton";
    private static final String BTN_SURVEY = "survey";
    private static final String BTN_DATA = "data";
    private static final String BTN_IO = "io";
    
    private static Log logger = LogFactory.getLog(BrandingbarController.class);
    
    // variables
    @WireVariable
    private NodeDao nodeDao;
    @WireVariable
    private ActivityService activityService;
    @WireVariable
    private GenericRO genericRO;
    
    // components
    @Wire
    private Button surveyBtn;
    @Wire
    private Button dataBtn;
    @Wire
    private Button ioBtn;
    
    @Wire
    private Combobutton accountBtn;
    @Wire
    private Combobox projectBox;
    @Wire
    private Combobox nodeBox;
    
    private ListModelList<String> myProjectsModel;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        populate();
    }
 
	private void populate() {
		logger.info("Creating branding bar...");
        Node login = GeneralUtil.getLogin();
        Node respondent = SurveyUtil.getRespondent();
        if (respondent == null) {
            respondent = login;
            if (respondent == null) {
                Messagebox.show("Login is required.");
                return;
            }
            SurveyUtil.setRespondent(respondent);
        }
        accountBtn.setLabel(login.getUsername());
        
    	// Branding top bar
        if (login.isAdmin()){
        	// populate impersonate box
        	populateImpersonateBox();
        	
        	// populate my projects box
        	populateProjectsBox();
        } else {
        	accountBtn.setParent(null);
        } 
        
        // set active button
        List<Component> comps = Selectors.find(this.getPage(), "hbox.topbar > button");
        for (Component comp : comps){
        	Button button = (Button)comp;
        	button.setSclass("");
        }
        Session s = Sessions.getCurrent();
        String b = (String)s.getAttribute(ACTIVE_BUTTON);
        if (b == null) {
        	b = BTN_SURVEY;
        	s.setAttribute(ACTIVE_BUTTON, BTN_SURVEY);
        }
        Button btn = surveyBtn;
        if (b.equalsIgnoreCase(BTN_DATA)) btn = dataBtn;
        else if (b.equalsIgnoreCase(BTN_IO)) btn = ioBtn;
        else btn = surveyBtn;
        btn.setSclass("active");
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void populateImpersonateBox() {		
		List<Node> nodes = nodeDao.findByType(Constants.NODE_TYPE_USER);
		List<String> labels = new ArrayList<String>();
		for (Node node : nodes){
			if (node.getUsername().equals("admin") && !GeneralUtil.getLogin().getUsername().equals("admin")) continue;
			labels.add(node.getLabel());
		}
		Collections.sort(labels);
		ListModelList model = new ListModelList(labels);
		model.addToSelection(SurveyUtil.getRespondent().getLabel());
		nodeBox.setModel(ListModels.toListSubModel(model, new NodeLabelComparator(), 15));
	}
    
    private void populateProjectsBox(){
    	try {
    		Node login = GeneralUtil.getLogin();
			Class.forName("com.mysql.jdbc.Driver");	
			String url = "jdbc:mysql://localhost:3306/ciknowmgr";
			Connection con = DriverManager.getConnection(url, "ciknowmgr", "ciknowmgr");
			
			// find user_id
			Long userId;
			String sql = "SELECT user_id FROM users WHERE username = '" + login.getUsername() + "'";
			logger.debug(sql);			
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);			
			if (rs.next()){
				userId = rs.getLong(1);
			} else throw new Exception("Cannot identify login with username=" + login.getUsername());
			
			// check if login is admin in ciknowmgr
			sql = "SELECT role_id FROM user_role WHERE user_id = " + userId;
			logger.debug(sql);
			st = con.createStatement();
			rs = st.executeQuery(sql);
			boolean isAdmin = false;
			while (rs.next()){
				Long roleId = rs.getLong(1);
				if (roleId.equals(1L)) {
					isAdmin = true;
					break;
				}
			}
			
			// retrieve projects
			if (isAdmin){
				sql = "SELECT name FROM projects WHERE enabled=true";
				logger.debug(sql);
				st = con.createStatement();
				rs = st.executeQuery(sql);
			} else {
				StringBuilder query = new StringBuilder();
				query.append("SELECT name FROM projects WHERE enabled=true AND project_id IN(");
				
				sql = "SELECT project_id FROM user_project WHERE user_id=" + userId;
				logger.debug(sql);
				st = con.createStatement();
				rs = st.executeQuery(sql);
				int count = 0;
				while (rs.next()){
					if (count > 0) query.append(",");
					query.append(rs.getLong(1));
					count++;
				}
				query.append(")");
				
				st = con.createStatement();
				rs = st.executeQuery(query.toString());
				logger.debug(query.toString());
			}
			
			// construct model
			List<String> projects = new ArrayList<String>();
			while (rs.next()){
				projects.add(rs.getString(1));
			}
			Collections.sort(projects);
			logger.debug(projects);
			myProjectsModel = new ListModelList<String>(projects);			
	    	String baseURL = genericRO.getBaseURL();
	    	String context = baseURL.substring(baseURL.lastIndexOf("/") + 1);
	    	String projectName = context.substring(1);
	    	myProjectsModel.addToSelection(projectName);
			projectBox.setModel(myProjectsModel);
    	} catch (Exception e){
    		logger.error(e.getMessage());
    		e.printStackTrace();
    	}
    }

    @Listen("onClick = hbox.topbar > button")
    public void showSurvey(MouseEvent event){
    	Button btn = (Button)event.getTarget();
    	String btnId = btn.getId();
    	String btnName = btnId.substring(0, btnId.length() - 3);
    	
    	Session s = Sessions.getCurrent();
    	String b = (String)s.getAttribute(ACTIVE_BUTTON);
    	if (b.equalsIgnoreCase(btnName)) return;
    	
    	s.setAttribute(ACTIVE_BUTTON, btnName);
    	Executions.sendRedirect(btnName + ".zul");
    }
    
    @Listen("onClick = #accountBtn")
    public void showAccountPopup(){
    	accountBtn.open();
    }
    
    @Listen("onSelect = #nodeBox")
    public void impersonate(){
    	String nodeLabel = nodeBox.getValue();
    	List<Node> nodes = nodeDao.loadByLabel(nodeLabel);
    	if (nodes ==null || nodes.isEmpty()) {
    		Messagebox.show("Invalid node: " + nodeLabel);
    		return;
    	}
    	Node node = nodes.get(0);
    	SurveyUtil.setRespondent(node);
    	
    	Executions.sendRedirect(null);
    }
    
    @Listen("onSelect = #projectBox")
    public void navigateToProject(){
    	String url = genericRO.getBaseURL();
    	String prefix = url.substring(0, url.lastIndexOf("/") + 1);
    	String context = url.substring(url.lastIndexOf("/") + 1);
    	String newContext = "_" + myProjectsModel.getSelection().iterator().next();
    	if (context.equals(newContext)) return;
    	String newURL = prefix + newContext;
    	Executions.sendRedirect(newURL);
    }
    
    @Listen("onClick = #ciknowmgrBtn")
    public void navigateToCiknowmgr(){
    	String url = genericRO.getBaseURL();
    	String prefix = url.substring(0, url.lastIndexOf("/") + 1);
    	Executions.sendRedirect(prefix + "ciknowmgr");
    }
    
    @Listen("onClick = #logoutBtn")
    public void logout() {
    	Node respondent = SurveyUtil.getRespondent();
    	
    	Page page = SurveyUtil.getCurrentPage();
    	if (page != null) activityService.leavePage(respondent, page);
    	
        logger.info("logout: " + SurveyUtil.getRespondent().getLabel());
        Executions.sendRedirect("/j_spring_security_logout");
    }
    
    private class NodeLabelComparator implements Comparator<Object>{
		@Override
		public int compare(Object o1, Object o2) {
			String s1 = ((String)o1).toLowerCase();
			String s2 = ((String)o2).toLowerCase();
			if (s2.contains(s1)) return 0;
			else return -1;
		}
    }
}
