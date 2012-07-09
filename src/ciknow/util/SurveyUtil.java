package ciknow.util;

import ciknow.domain.*;
import ciknow.zk.controller.SurveyController;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;

/**
 *
 * @author gyao
 */
public class SurveyUtil {

    private static Log logger = LogFactory.getLog(SurveyUtil.class);

    /**
     * UI helper
     */
    
    public static void removeAllChildren(Component c){
    	while (c.getLastChild() != null) {
    		c.removeChild(c.getLastChild());
    	}
    }
    
    /**
     * In case of data corruption, clear the application and session scope And
     * then refresh the page
     */
    public static void refresh() {
        WebApps.getCurrent().removeAttribute(Constants.APP_SURVEY);
        Sessions.getCurrent().removeAttribute(Constants.SESSION_RESPONDENT);
        Sessions.getCurrent().removeAttribute(Constants.SESSION_CURRENT_PAGE);
        Sessions.getCurrent().removeAttribute(Constants.SESSION_SURVEY_CONTROLLER);
        Executions.sendRedirect(null);
    }

    public static Survey getSurvey() {
        return (Survey) WebApps.getCurrent().getAttribute(Constants.APP_SURVEY);
    }

    public static void setSurvey(Survey survey) {
        WebApps.getCurrent().setAttribute(Constants.APP_SURVEY, survey);
    }

    public static Page getCurrentPage() {
        return (Page) Sessions.getCurrent().getAttribute(Constants.SESSION_CURRENT_PAGE);
    }

    public static void setCurrentPage(Page page) {
        Sessions.getCurrent().setAttribute(Constants.SESSION_CURRENT_PAGE, page);
    }

    public static Node getRespondent() {
        return (Node) Sessions.getCurrent().getAttribute(Constants.SESSION_RESPONDENT);
    }

    public static void setRespondent(Node respondent) {
        Sessions.getCurrent().setAttribute(Constants.SESSION_RESPONDENT, respondent);
    }

    public static SurveyController getController() {
        return (SurveyController) Sessions.getCurrent().getAttribute(Constants.SESSION_SURVEY_CONTROLLER);
    }

    public static void setController(SurveyController controller) {
        Sessions.getCurrent().setAttribute(Constants.SESSION_SURVEY_CONTROLLER, controller);
    }

    public static String getSlideInAnchor(){
    	return (String)Sessions.getCurrent().getAttribute(Constants.SESSION_SLIDE_IN_ANCHOR);
    }
    
    public static void setSlideInAnchor(String value){
    	Sessions.getCurrent().setAttribute(Constants.SESSION_SLIDE_IN_ANCHOR, value);
    }
    
    @SuppressWarnings("unchecked")
	public static List<Page> getVisiblePages(){
    	return (List<Page>)Sessions.getCurrent().getAttribute(Constants.SESSION_VISIBLE_PAGES);
    }
    
    public static void setVisiblePages(List<Page> pages){
    	Sessions.getCurrent().setAttribute(Constants.SESSION_VISIBLE_PAGES, pages);
    }
    
    /***************************************************************
     * Navigation (prev/next) with branching and skipping logics
     **************************************************************/
    public static int getNextPageIndex(List<Page> visiblePages) {
    	Node respondent = getRespondent();
    	Page currentPage = getCurrentPage();
    	List<Question> pageQuestions = currentPage.getQuestions();
    	int currentIndex = visiblePages.indexOf(currentPage);

    	// retrieve jump settings
    	int nextIndex = -1;
    	Field jc = null;
    	Question jq = null;
    	Question question = null;
    	for (int i=0; i<pageQuestions.size(); i++){
    		question = pageQuestions.get(i);
    		jc = question.getJumpCondition();
    		jq = question.getJumpQuestion();
    		if (jq != null) break;
    	}
    	
    	// determine the next page index based on jump settings
    	if (jc == null && jq == null) {	// no jump
    		nextIndex = currentIndex + 1;
    	} else if (jc == null && jq != null){	// unconditional jump
    		int index = visiblePages.indexOf(jq.getPage());
    		if (index < 0) {
    			nextIndex = currentIndex + 1;
    		} else {
    			nextIndex = index;
    		}
    	} else {	// conditional jump
    		String key = question.makeFieldKey(jc);
    		if (respondent.getAttribute(key) == null || jq == null){	// condition unmatched
    			nextIndex = currentIndex + 1;
    		} else {	// condition matched
        		int index = visiblePages.indexOf(jq.getPage());
        		if (index < 0) {
        			nextIndex = currentIndex + 1;
        		} else {
        			nextIndex = index;
        		}
    		}
    	}
    	
    	if (question != null) {
	    	if (nextIndex == currentIndex + 1){
	    		SurveyUtil.removeJump(question);
	    	} else {
	    		SurveyUtil.addJump(question, jq);
	    	}
    	} else { 
    		// This page is empty
    	}
        
    	
        // determine the next page index based on skip settings
    	nextIndex = skip(visiblePages, nextIndex);
    			
        return nextIndex;
    }

    private static int skip(List<Page> visiblePages, int index){
    	// last page, cannot skip anymore
    	if (index == visiblePages.size() - 1) return index;	
    	// admin need to see all page in order to configure questions
    	if (getRespondent().isAdmin()) return index;		
    	
    	Page page = visiblePages.get(index);
    	for (Question question : page.getQuestions()){
    		if (!skipOnEmpty(question)) return index;
    	}
    	
    	// SKIP
    	SurveyUtil.addSkip(page, visiblePages.get(index + 1));
    	return skip(visiblePages, index + 1);
    }
    
    public static boolean skipOnEmpty(Question question){
    	Node respondent = getRespondent();
    	
    	// administrator need to see this question in order to configure it
    	if (respondent.isAdmin()) return false;	
    	
    	if (!question.isRelational()) return false;
    	
    	if (!question.skipOnEmpty()) return false;
    	    	
    	Set<Long> nodeIds = Question.getCombinedAvailableNodeIds(question, respondent, false);
    	if (nodeIds != null && !nodeIds.isEmpty()) return false;
    	else return true;
    }
    
    public static int getPrevPageIndex(List<Page> visiblePages) {
    	Page currentPage = getCurrentPage();    	
    	int currentIndex = visiblePages.indexOf(currentPage);

    	// determine previous page index based on skip settings
    	currentIndex = reverseSkip(visiblePages, currentIndex);
    	currentPage = visiblePages.get(currentIndex);
    	
    	// retrieve jump settings
    	List<Question> pageQuestions = currentPage.getQuestions();
    	int prevIndex = -1;
    	Question fromQuestion = null;
    	Question toQuestion = null;
    	for (int i=0; i < pageQuestions.size(); i++) {
    		toQuestion = pageQuestions.get(i);
    		fromQuestion = SurveyUtil.getJumpFromQuestionByToQuestion(toQuestion);
    		if (fromQuestion != null) break;
    	}
    	
    	// determine previous page index based on jump settings
    	if (fromQuestion == null){
    		prevIndex = currentIndex - 1;
    	} else {
    		int index = visiblePages.indexOf(fromQuestion.getPage());
    		if (index < 0) prevIndex = currentIndex - 1;
    		else prevIndex = index;
    		
    		SurveyUtil.removeJump(fromQuestion);
    	}

    	return prevIndex;
    }
    
    private static int reverseSkip(List<Page> visiblePages, int index){
    	if (index == 0) return index;
    	
    	Page page = visiblePages.get(index);
    	Page prevPage = SurveyUtil.getSkipFromPageByToPage(page);
    	if (prevPage == null) return index;
    	else {
    		SurveyUtil.removeSkip(prevPage);
    		return reverseSkip(visiblePages, index - 1);
    	}
    }
    
	private static void addJump(Question fromQuestion, Question toQuestion){
		Node respondent = getRespondent();
		String pair = fromQuestion.getShortName() + ":" + toQuestion.getShortName();
		String jumpString = respondent.getAttribute(Constants.NODE_JUMP_FROM_TO_QUESTIONS);
		if (jumpString != null && jumpString.length() > 0){
			if (jumpString.indexOf(pair) < 0){
				jumpString = jumpString + "|" + pair;
			}
		} else {
			jumpString = pair;
		}
		respondent.setAttribute(Constants.NODE_JUMP_FROM_TO_QUESTIONS, jumpString);
		logger.debug("add jump settings for respondent: " + jumpString);
	}
	
	private static void removeJump(Question fromQuestion){
		Node respondent = getRespondent();
		String jumpString = respondent.getAttribute(Constants.NODE_JUMP_FROM_TO_QUESTIONS);
		if (jumpString == null) return;
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for (String pair : jumpString.split("\\|", -1)){
			String[] parts = pair.split(":", -1);
			String fromQuestionName = parts[0];
			if (fromQuestionName.equals(fromQuestion.getShortName())){
				continue;
			}
			if (index == 0) sb.append(pair);
			else sb.append("|").append(pair);
			index++;
		}
		String newJumpString = sb.toString();
		if (newJumpString == null || newJumpString.isEmpty()){
			respondent.getAttributes().remove(Constants.NODE_JUMP_FROM_TO_QUESTIONS);
		} else {
			respondent.setAttribute(Constants.NODE_JUMP_FROM_TO_QUESTIONS, newJumpString);
		}
		logger.debug("remove jump settings for respondent: " + newJumpString);
	}
	
	private static Question getJumpFromQuestionByToQuestion(Question toQuestion){
		Node respondent = getRespondent();
		String jumpString = respondent.getAttribute(Constants.NODE_JUMP_FROM_TO_QUESTIONS);
		if (jumpString == null || jumpString.isEmpty()) return null;

		for (String pair : jumpString.split("\\|", -1)){
			String[] parts = pair.split(":", -1);
			String fromQuestionName = parts[0];
			String toQuestionName = parts[1];
			if (toQuestionName.equals(toQuestion.getShortName())){
				return getQuestionByShortName(getAllQuestions(), fromQuestionName);
			}
		}
		return null;		
	}
	
	private static void addSkip(Page fromPage, Page toPage){
		Node respondent = getRespondent();
		String pair = fromPage.getName() + ":" + toPage.getName();
		String skipString = respondent.getAttribute(Constants.NODE_SKIP_FROM_TO_PAGES);
		if (skipString != null && skipString.length() > 0){
			if (skipString.indexOf(pair) < 0){
				skipString = skipString + "|" + pair;
			}
		} else {
			skipString = pair;
		}
		respondent.setAttribute(Constants.NODE_SKIP_FROM_TO_PAGES, skipString);
		logger.debug("add skip settings for respondent: " + skipString);
	}
	
	private static void removeSkip(Page fromPage){
		Node respondent = getRespondent();
		String skipString = respondent.getAttribute(Constants.NODE_SKIP_FROM_TO_PAGES);
		if (skipString == null) return;
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for (String pair : skipString.split("\\|", -1)){
			String[] parts = pair.split(":", -1);
			String fromPageName = parts[0];
			if (fromPageName.equals(fromPage.getName())){
				continue;
			}
			if (index == 0) sb.append(pair);
			else sb.append("|").append(pair);
			index++;
		}
		String newSkipString = sb.toString();
		if (newSkipString == null || newSkipString.isEmpty()){
			respondent.getAttributes().remove(Constants.NODE_SKIP_FROM_TO_PAGES);
		} else {
			respondent.setAttribute(Constants.NODE_SKIP_FROM_TO_PAGES, newSkipString);
		}
		logger.debug("remove skip settings for respondent: " + newSkipString);
	}
	
	private static Page getSkipFromPageByToPage(Page toPage){
		Node respondent = getRespondent();
		String skipString = respondent.getAttribute(Constants.NODE_SKIP_FROM_TO_PAGES);
		if (skipString == null || skipString.isEmpty()) return null;

		for (String pair : skipString.split("\\|", -1)){
			String[] parts = pair.split(":", -1);
			String fromPageName = parts[0];
			String toPageName = parts[1];
			if (toPageName.equals(toPage.getName())){
				return getPageByName(SurveyUtil.getSurvey().getPages(), fromPageName);
			}
		}
		return null;		
	}
	
	

	public static Page getPageByName(List<Page> pages, String pageName){
		if (pages == null || pages.isEmpty() || pageName == null) return null;
		
		for (Page page : pages){
			if (page.getName().equals(pageName)) return page;
		}
		return null;
	}

	public static List<Question> getAllQuestions(){
		List<Question> questions = new ArrayList<Question>();
		for (Page page : getSurvey().getPages()){
			questions.addAll(page.getQuestions());
		}
		return questions;
	}
	
    /**
     * Get a list of question visible to respondent out of given list of
     * questions
     *
     * @param respondent-
     * @param questions	- all questions
     * @return
     */
    public static List<Question> getVisibleQuestions(Node respondent, List<Question> questions) {
        logger.debug("getVisibleQuestions for node " + respondent.getLabel());
        List<Question> visibleQuestions = new ArrayList<Question>();

        for (Question question : questions) {
        	if (question.isVisible(respondent)) visibleQuestions.add(question);
        }

        logger.debug(visibleQuestions.size() + " questions are visible to node " + respondent.getLabel());
        return visibleQuestions;
    }
	
    /**
     * Get a question from a list of questions by shortName
     *
     * @param questions
     * @param shortName
     * @return
     */
    public static Question getQuestionByShortName(List<Question> questions, String shortName) {
        if (questions == null || questions.isEmpty() || shortName == null) {
            return null;
        }

        for (Question question : questions) {
            if (question.getShortName().equals(shortName)) {
                return question;
            }
        }
        return null;
    }

    /**
     * Get a question from a list of questions by question label
     *
     * @param questions
     * @param label
     * @return
     */
    public static Question getQuestionByLabel(List<Question> questions, String label) {
        if (questions == null || questions.isEmpty() || label == null) {
            return null;
        }

        for (Question question : questions) {
            if (question.getLabel().equals(label)) {
                return question;
            }
        }
        return null;
    }



    /**
     * Disble respondent if the respondent is not Admin and "disableOnFinish"
     * option is set for the survey
     *
     * @param respondent
     * @param survey
     */
    public static void disableRespondentOnFinish(Node respondent, Survey survey) {
        if (GeneralUtil.verify(survey.getAttributes(), Constants.SURVEY_DISABLE_USER_ON_FINISH) && !respondent.isAdmin()) {
            respondent.setEnabled(false);
            logger.info("Disabled node: " + respondent.getLabel());
        }
    }
}
