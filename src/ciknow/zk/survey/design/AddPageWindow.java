package ciknow.zk.survey.design;

import java.util.List;

import ciknow.dao.SurveyDao;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.domain.Survey;
import ciknow.service.ActivityService;
import ciknow.util.GeneralUtil;
import ciknow.util.SurveyUtil;
import ciknow.zk.controller.SurveyController;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AddPageWindow extends Window {

    private static final long serialVersionUID = -3902957071713495754L;
    private static Log logger = LogFactory.getLog(AddPageWindow.class);
    @WireVariable
    private SurveyDao surveyDao;
    @WireVariable
    private ActivityService activityService;
    
    @Wire
    private Textbox labelBox;
    @Wire
    private Textbox nameBox;

    public AddPageWindow(Component parent) {
        this.setParent(parent);
        this.setClosable(true);
        this.setSclass("addPageWindow");
        
        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/AddPageWindow.zul", this, null);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
    }

    /*
    @Listen("onChanging = #nameBox")
    public void onChanging$nameBox(InputEvent e) {
        String value = e.getValue();
        labelBox.setValue(value);
    }
    */
    
    @Listen("onClick = #createBtn")
    public void create() throws Exception {
        logger.debug("on create");

        // validate
        String name = nameBox.getValue().trim();
        String label = labelBox.getValue().trim();
        if (!GeneralUtil.isValidName(name)) return;
        if (!GeneralUtil.isValidLabel(label)) return;  
        
        // create new page
        Page page = new Page();
        page.setName(name);
        page.setLabel(label);

        Survey survey = SurveyUtil.getSurvey();
        if (survey.getPages().contains(page)){
        	Messagebox.show("Page Name: " + name + " has been used. Try a different one.");
        	return;
        }
        Page currentPage = SurveyUtil.getCurrentPage();
        if (currentPage == null) {
            survey.getPages().add(page);
        } else {
            int index = currentPage.getIndex();
            if (index == survey.getPages().size() - 1) {
                survey.getPages().add(page);
            } else {
                survey.getPages().add(index + 1, page);
            }
        }
        page.setSurvey(survey);

        try {
            surveyDao.save(survey);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            e.printStackTrace();
            Messagebox.show("Failed to add new page. Press OK to refersh and try again.", "",
                    Messagebox.OK, Messagebox.ERROR, new EventListener<Event>() {

                @Override
                public void onEvent(Event event) throws Exception {
                    SurveyUtil.refresh();
                }
            });
            return;
        }

        // update UI
        Node respondent = SurveyUtil.getRespondent();
        if (currentPage != null) activityService.leavePage(respondent, currentPage);
        activityService.enterPage(respondent, page);
    	List<Page> visiblePages = survey.getVisiblePages(respondent);
    	SurveyUtil.setVisiblePages(visiblePages);
    	SurveyUtil.setCurrentPage(page);
    	SurveyController controller = SurveyUtil.getController();
    	controller.populatePagesBox();
    	controller.render(true);
    	this.setParent(null);
    }

    @Listen("onClick = #cancelBtn")
    public void cancel() {
        logger.debug("on cancel");
        this.setParent(null);
    }
}
