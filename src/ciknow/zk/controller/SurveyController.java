package ciknow.zk.controller;

import ciknow.dao.ActivityDao;
import ciknow.dao.NodeDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Activity;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.domain.Survey;
import ciknow.service.ActivityService;
import ciknow.util.GeneralUtil;
import ciknow.util.SurveyUtil;
import ciknow.zk.survey.design.AddPageWindow;
import ciknow.zk.survey.design.AddQuestionWindow;
import ciknow.zk.survey.design.SurveyOverview;
import ciknow.zk.survey.response.SurveyPage;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.BookmarkEvent;
import org.zkoss.zk.ui.event.ClientInfoEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.theme.Themes;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyController extends SelectorComposer<Component> {

    private static final long serialVersionUID = -9135576397887656457L;
    private static Log logger = LogFactory.getLog(SurveyController.class);
    
    // variables
    @WireVariable
    private NodeDao nodeDao;
    @WireVariable
    private SurveyDao surveyDao;
    @WireVariable
    private ActivityDao activityDao;
    @WireVariable
    private ActivityService activityService;
    
    // components   
    @Wire
    private Label surveyName;
    @Wire
    private Label surveyDescription;
    @Wire
    private Combobox pagesBox;
    @Wire
    private Label impersonateMsg;
    @Wire
    private Listbox themeBox;
    @Wire
    private Progressmeter progressMeter;
    @Wire
    private Hbox projectBar;  
    @Wire
    private Div pageArea;
    @Wire
    private Button prevBtn;
    @Wire
    private Button nextBtn;
    @Wire
    private Button finishBtn;
    
    // internal
    private SurveyPage surveyPage;
    private ListModelList<String> pagesModel;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        populate();
    }

    @Listen("onClientInfo = #stage")
    public void onClientInfo$stage(ClientInfoEvent evt) {
        Integer width = evt.getDesktopWidth();
        Integer height = evt.getDesktopHeight();
        logger.debug("screen.width: " + evt.getScreenWidth());
        logger.debug("screen.height: " + evt.getScreenHeight());
        logger.debug("desktop.width: " + width);
        logger.debug("desktop.height: " + height);


        Integer currentWidth = GeneralUtil.getDesktopWidth();
        Integer currentHeight = GeneralUtil.getDesktopHeight();

        // first hit
        if (currentWidth == null || currentHeight == null) {
            logger.debug("First Hit");
            GeneralUtil.setDesktopWidth(width);
            GeneralUtil.setDesktopHeight(height);
            //Executions.sendRedirect(null);
            return;
        }

        // browser resize
        if (!width.equals(currentWidth) || !height.equals(currentHeight)) {
            logger.debug("Browser Resize");
            GeneralUtil.setDesktopWidth(width);
            GeneralUtil.setDesktopHeight(height);
            //Executions.sendRedirect(null);
        }
    }
    
	private void populate() {
		logger.info("Creating survey interface ...");
        // theme indicator
        //String theme = Themes.getCurrentTheme();
    	String theme = "silvertail";
    	Themes.setTheme(Executions.getCurrent(), theme);
        ListModelList<String> themesModel = new ListModelList<String>(Themes.getThemes());
        themesModel.addToSelection(theme);
        themeBox.setModel(themesModel);

        // Get application attributes		
        Survey survey = SurveyUtil.getSurvey();
        if (survey == null) {
        	logger.info("Get survey from persistent store.");
            survey = surveyDao.findById(1L);
            SurveyUtil.setSurvey(survey);
        } else logger.info("Get survey from application context.");

        // Get session attributes
        SurveyUtil.setController(this);
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
        
        Page currentPage = null;
    	List<Page> visiblePages = survey.getVisiblePages(respondent);
    	SurveyUtil.setVisiblePages(visiblePages);
    	/*
    	if (logger.isDebugEnabled()){
	    	for (Page page : visiblePages){
	    		logger.debug(page.getLabel());
	    	}
	    	logger.debug("Total: " + visiblePages.size());
    	}
    	*/
    	
    	// get currentPage based on logged activities
    	List<Activity> acts = activityDao.getActivitiesBySubject(respondent);
    	String pageName = activityService.getLastEnteredPageName(acts);
    	currentPage = SurveyUtil.getPageByName(visiblePages, pageName);
    	if (currentPage != null) {
    		logger.info("Get currentPage from persistent store.");
    	}    	
    	// if no proper logs, set to the first page
    	if (currentPage == null && !visiblePages.isEmpty()){
    		currentPage = visiblePages.get(0);
    		activityService.startSurvey(respondent);
    		logger.info("Get currentPage as first page of the survey.");
    	}    	
    	if (currentPage != null){
    		SurveyUtil.setCurrentPage(currentPage);
    		activityService.enterPage(respondent, currentPage);
    	} else {
    		// this survey is empty
    	}
    	
        // Top Banner        
        surveyName.setValue(survey.getName());
        surveyDescription.setValue(survey.getDescription());        
        if (login.equals(respondent)){
        	impersonateMsg.setValue(login.getUsername());        	
        } else {
        	impersonateMsg.setValue("Impersonated as: " + respondent.getLabel());
        }
        
        // Project bar
        if (!respondent.isAdmin()){        	
        	projectBar.setParent(null);        	
        }
        populatePagesBox();         
        
        // Render dynamic page
        SurveyUtil.removeAllChildren(pageArea);
        surveyPage = new SurveyPage();
        surveyPage.setParent(pageArea);
        if (currentPage != null) {
            render(true);
        } else {
            String msg;
            if (respondent.isAdmin()) {
                msg = "Please add pages and questions into your survey";
            } else {
                msg = "There is no survey available. Please contact administrator.";
            }
            Messagebox.show(msg);
            prevBtn.setDisabled(true);
            nextBtn.setDisabled(true);
            finishBtn.setDisabled(true);
        }
    }

    public void populatePagesBox() {
        pagesModel = new ListModelList<String>();
        List<Page> visiblePages = SurveyUtil.getVisiblePages();
        for (Page page : visiblePages) {
            pagesModel.add(page.getLabel());
        }
        Page page = SurveyUtil.getCurrentPage();
        if (page != null) {
            pagesModel.addToSelection(page.getLabel());
        }
        pagesBox.setModel(pagesModel);
    }

    public void render(boolean recordBookmark) {
        logger.info("rendering...");

        // sync with database
        sync();

        // Update page area
        Page currentPage = SurveyUtil.getCurrentPage();
        surveyPage.render(currentPage);

        // Update control bar
        updateNavControls();
        
        // Update bookmark (URL)
        if (recordBookmark) setBookmark();
    }
    
    private void updateNavControls() {
    	List<Page> visiblePages = SurveyUtil.getVisiblePages();
        int total = visiblePages.size();
        int currentIndex = visiblePages.indexOf(SurveyUtil.getCurrentPage());
        logger.debug("currentIndex: " + currentIndex);

        // pages box
        if ((pagesBox.getItemCount() - 1) >= currentIndex) {
            pagesBox.setSelectedIndex(currentIndex);
        }

        // progress meter
        int progress = (currentIndex + 1) * 100 / total;
        progressMeter.setValue(progress);

        // bottom control bar
        if (currentIndex == 0) {
            prevBtn.setDisabled(true);
        } else {
            prevBtn.setDisabled(false);
        }

        if (currentIndex == (total - 1)) {
            nextBtn.setDisabled(true);
            finishBtn.setDisabled(false);
        } else {
            nextBtn.setDisabled(false);
            finishBtn.setDisabled(true);
        }
    }
    
    private void setBookmark(){
    	Desktop desktop = Executions.getCurrent().getDesktop();
        desktop.setBookmark(SurveyUtil.getCurrentPage().getName());
        logger.info("Set bookmark: " + desktop.getBookmark());
    }
    
    @Listen("onBookmarkChange = *")
    public void navigateByBookmark(BookmarkEvent e){
    	logger.info("Navigate via bookmark: " + e.getBookmark());
    	String pageName = e.getBookmark();
    	Page page = SurveyUtil.getPageByName(SurveyUtil.getVisiblePages(), pageName);
    	if (page == null){
    		Executions.sendRedirect("/");
    		return;
    	}
    	SurveyUtil.setCurrentPage(page);
        activityService.enterPage(SurveyUtil.getRespondent(), page);
        
        // render
        render(false);
    }
    
    @Listen("onClick = #overviewBtn")
    public void showSurveyOverview() {
        logger.info("Show survey overview ...");
        if (save()) {
            SurveyOverview win = new SurveyOverview(pageArea);
            win.doModal();
        }
    }

    @Listen("onClick = #newPageBtn")
    public void createPage() {
        logger.info("Adding new page...");
        if (save()) {
            AddPageWindow win = new AddPageWindow(pageArea);
            win.doModal();
        }
    }

    @Listen("onClick = #newQuestionBtn")
    public void createQuestion() {
        Page currentPage = SurveyUtil.getCurrentPage();
        if (currentPage == null) {
            Messagebox.show("Please select a page first.");
            return;
        }

        logger.info("Adding new question to current page: " + currentPage.getLabel());
        if (save()) {
            AddQuestionWindow win = new AddQuestionWindow(surveyPage);
            win.doModal();
        }
    }
    
    /*
    @Listen("onSelect = #themeBox")
    public void changeTheme() {
        Listitem item = themeBox.getSelectedItem();
        if (item == null) {
            return;
        }

        String value = item.getLabel();
        logger.info("Theme changed to: " + value);
        Themes.setTheme(Executions.getCurrent(), value);
        Executions.sendRedirect(null);
    }
	*/

    
    @Listen("onChange = #pagesBox")
    public void changePage() {
    	Node respondent = SurveyUtil.getRespondent();
		Page currentPage = SurveyUtil.getCurrentPage();
		
    	if (save()){
	    	activityService.leavePage(respondent, currentPage);
	    	
	        String pageLabel = (String) pagesBox.getSelectedItem().getValue();
	        Page nextPage = SurveyUtil.getSurvey().getPageByLabel(pageLabel);
	        SurveyUtil.setCurrentPage(nextPage);
	        activityService.enterPage(respondent, nextPage);
	        
	        // render
	        if (currentPage.getIndex() > nextPage.getIndex()){
	        	SurveyUtil.setSlideInAnchor("l");
	        }
	        render(true);
	        SurveyUtil.setSlideInAnchor("");
    	} else { // if error, reset to original page
    		Executions.sendRedirect(null);
    	}
    }
    
    @Listen("onClick = #prevBtn")
    public void prev() {
        if (save()) {
        	List<Page> visiblePages = SurveyUtil.getVisiblePages();
        	Node respondent = SurveyUtil.getRespondent();
        	activityService.leavePage(respondent, SurveyUtil.getCurrentPage());
        	
            int prevIndex = SurveyUtil.getPrevPageIndex(visiblePages);
            nodeDao.save(respondent); // possible jump/skip settings
            if (prevIndex < 0) {
                logger.warn("PrevBtn should be disabled at the first question.");
                Messagebox.show("Navigation Error");
            } else {
            	Page currentPage = visiblePages.get(prevIndex);
                SurveyUtil.setCurrentPage(currentPage);
                activityService.enterPage(respondent, currentPage);
                
                // render
                SurveyUtil.setSlideInAnchor("l");
                render(true);
                SurveyUtil.setSlideInAnchor("");
            }
        }
    }

    @Listen("onClick = #nextBtn")
    public void next() {
        if (save()) {
        	List<Page> visiblePages = SurveyUtil.getVisiblePages();
        	Node respondent = SurveyUtil.getRespondent();
        	activityService.leavePage(respondent, SurveyUtil.getCurrentPage());
        	
            int nextIndex = SurveyUtil.getNextPageIndex(visiblePages);
            nodeDao.save(respondent); // possible jump/skip settings
            if (nextIndex < 0) {
                logger.warn("NextBtn should be disabled at the last question.");
                Messagebox.show("Navigation Error");
            } else {
            	Page currentPage = visiblePages.get(nextIndex);
                SurveyUtil.setCurrentPage(currentPage);
                activityService.enterPage(respondent, currentPage);
                
                // render
                render(true);
            }
        }
    }

    @Listen("onClick = #finishBtn")
    public void finish() {
    	if (save()){
        	Node respondent = SurveyUtil.getRespondent();
        	activityService.leavePage(respondent, SurveyUtil.getCurrentPage());
        	activityService.finishSurvey(respondent);
        	SurveyUtil.disableRespondentOnFinish(respondent, SurveyUtil.getSurvey());
        	
	        logger.info("finish: " + SurveyUtil.getRespondent().getLabel());
	        Executions.sendRedirect("/j_spring_security_logout");
    	}
    }
    
    @Transactional
    private boolean save() {
        // validate
        if (!surveyPage.validate()) {
            return false;
        }

        // sync
        sync();
        
        // save question answers
        return surveyPage.save();
    }

    private void sync() {
        Node respondent = SurveyUtil.getRespondent();
        respondent.update(nodeDao.loadById(respondent.getId()));
    }
}
