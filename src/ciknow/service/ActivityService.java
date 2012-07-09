package ciknow.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.ActivityDao;
import ciknow.domain.Activity;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.util.Constants;

public class ActivityService {
	private static Log logger = LogFactory.getLog(ActivityService.class);
	
	private ActivityDao activityDao;

	public ActivityDao getActivityDao() {
		return activityDao;
	}

	public void setActivityDao(ActivityDao activityDao) {
		this.activityDao = activityDao;
	}
	

	/****************************************************
	 * Log respondent activities 
	 * @param respondent
	 ***************************************************/
    public void startSurvey(Node respondent) {
        Activity act = new Activity();
        act.setSubject(respondent);
        act.setPredicate(Constants.ACT_SURVEY_START);
        act.setTimestamp(new Date());
        activityDao.save(act);
    }
    
    public void finishSurvey(Node respondent) {
        Activity act = new Activity();
        act.setSubject(respondent);
        act.setPredicate(Constants.ACT_SURVEY_FINISH);
        act.setTimestamp(new Date());
        activityDao.save(act);
    }

    public void enterPage(Node respondent, Page page){
        Activity act = new Activity();
        act.setSubject(respondent);
        act.setPredicate(Constants.ACT_PAGE_ENTER_PREFIX + page.getName());
        act.setTimestamp(new Date());
        activityDao.save(act);
    }

    public void leavePage(Node respondent, Page page){
        Activity act = new Activity();
        act.setSubject(respondent);
        act.setPredicate(Constants.ACT_PAGE_LEAVE_PREFIX + page.getName());
        act.setTimestamp(new Date());
        activityDao.save(act);
    }
    
    
    /***************************************************
     * Query and analyze respondent activities
     * @param acts - list of activities by a respondent
     **************************************************/
    public Activity getSurveyStart(List<Activity> acts) {
    	for (Activity act : acts){
    		if (act.getPredicate().equals(Constants.ACT_SURVEY_START)) return act;
    	}
        return null;
    }
    
    public Activity getSurveyFinish(List<Activity> acts) {
    	int size = acts.size();
    	for (int i = size; i > 0; i--){
    		Activity act = acts.get(i-1);
    		if (act.getPredicate().equals(Constants.ACT_SURVEY_FINISH)) return act;
    	}
        return null;
    }
    
    /**
     * Get the last page before respondent hit finish/logout button, or close browser
     * This page can be presented as initial page during respondent login
     * @param acts - list of activities for a respondent
     * by default, it is in ascending order by timestamp (or act id)
     */
    public String getLastEnteredPageName(List<Activity> acts){
    	Activity act = getLastPageEnter(acts);
    	if (act == null) return null;
    	return act.getPredicate().substring(Constants.ACT_PAGE_ENTER_PREFIX.length());
    }
    
    public Activity getLastPageEnter(List<Activity> acts){
    	int size = acts.size();
    	for (int i = size; i > 0; i--){
    		Activity act = acts.get(i-1);
    		String p = act.getPredicate();
    		if (p.startsWith(Constants.ACT_PAGE_ENTER_PREFIX)){
    			return act;
    		}
    	}
    	return null;
    }
    
    /**
     * Determine whether a respondent has answered questions in specified page
     * @param acts	- list of activities for a respondent
     * @param page	-
     * @return
     */
    public boolean leavedPage(List<Activity> acts, Page page){
    	int size = acts.size();
    	for (int i = size; i > 0; i--){
    		Activity act = acts.get(i-1);
    		String p = act.getPredicate();
    		if (p.equals(Constants.ACT_PAGE_LEAVE_PREFIX + page.getName())){
    			return true;
    		}
    	}
    	return false;
    }
    
    
    public List<Map<String, String>> getProgress(List<Node> nodes, List<Page> pages){
    	logger.info("Get progress ...");
    	Map<String, Page> pageMap = new HashMap<String, Page>();
    	for (Page page : pages){
    		pageMap.put(page.getName(), page);
    	}
    	int numPage = pages.size();
    	Page lastPage = pages.get(pages.size() - 1);
    	
    	List<Map<String, String>> progress = new ArrayList<Map<String, String>>();
    	for (Node node : nodes){
    		Map<String, String> m = new HashMap<String, String>();
    		progress.add(m);
    		m.put("id", node.getId().toString());
    		m.put("username", node.getUsername());
    		m.put("label", node.getLabel());
    		
    		List<Activity> acts = activityDao.getActivitiesBySubject(node);
    		Activity surveyStart = getSurveyStart(acts);
    		Page lastEnteredPage = null;
    		Date lastEnteredTime = null;
    		if (surveyStart != null){
    			Activity surveyFinish = getSurveyFinish(acts);
				Activity lastPageEnter = getLastPageEnter(acts);
				String lastEnteredPageName = getLastEnteredPageName(acts);
				lastEnteredPage = pageMap.get(lastEnteredPageName);
				lastEnteredTime = lastPageEnter.getTimestamp();
				
    			if (surveyFinish != null){
    				m.put("status", Constants.NODE_PROGRESS_FINISHED);
    			} else {
    				if (lastEnteredPageName.equals(lastPage.getName())){
    					m.put("status", Constants.NODE_PROGRESS_COMPLETED);
    				} else {
    					m.put("status", Constants.NODE_PROGRESS_NOT_COMPLETED);
    				}    				
    			}
    		} else {
    			m.put("status", Constants.NODE_PROGRESS_NOT_STARTED);
    		}
    		
			if (lastEnteredPage == null){ 
				// either respondent not started yet, or the page has been deleted
    			m.put("progress", "0/" + numPage);  
        		m.put("lastEnteredPage", "-");
        		m.put("lastEnteredTime", "-");
			} else {
				int pageIndex = pages.indexOf(lastEnteredPage) + 1;
				m.put("progress", pageIndex + "/" + numPage);
				m.put("lastEnteredPage", lastEnteredPage.getLabel());
				m.put("lastEnteredTime", new Long(lastEnteredTime.getTime()).toString());
			}
    	}
    	
    	return progress;
    }
    
    /**
     * Determine whether respondent has answered a question
     *
     * @param respondent	- survey respondent
     * @param targetQuestion	- the question to be determined whether answered or
     * not by respondent
     * @param visibleQuestions	- list of questions visible to respondent
     * @return
     */
    /*
    public static boolean answeredQuestion(Node respondent, Question targetQuestion, List<Question> visibleQuestions) {
        int targetIdx = visibleQuestions.indexOf(targetQuestion);
        if (targetIdx < 0) {
            return false;
        }

        if (startedSurvey(respondent)) {
            if (finishedSurvey(respondent)) {
                return true;
            } else {
                Question maxQuestion = getMaxAnsweredQuestion(respondent, visibleQuestions);
                if (maxQuestion == null) {
                    // undecided, degenerated to respondent not started survey yet. 
                    // There are several possible scenarios:
                    // 1, respondent never answered the survey
                    // 2, respondent answered a question which was removed from survey later
                    // 3, respondent answered a question which was visible at previous time but made invisible later
                    return false;
                } else {
                    int maxIdx = visibleQuestions.indexOf(maxQuestion);
                    if (targetIdx > maxIdx) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
    }
	*/
    
    /**
     * Get the last answered question by respondent
     *
     * @param respondent
     * @param visibleQuestions	- list of questions visible to respondent
     * @return
     */
    /*
    public static Question getLastAnsweredQuestion(Node respondent, List<Question> visibleQuestions) {
        return getAnsweredQuestion(respondent, visibleQuestions, Constants.NODE_LAST_ANSWERED_QUESTION_NAME);
    }
	*/
    /**
     * Get the max (furthest) answered question by respondent
     *
     * @param respondent
     * @param visibleQuestions
     * @return
     */
    /*
    public static Question getMaxAnsweredQuestion(Node respondent, List<Question> visibleQuestions) {
        return getAnsweredQuestion(respondent, visibleQuestions, Constants.NODE_MAX_ANSWERED_QUESTION_NAME);
    }
	
    private static Question getAnsweredQuestion(Node respondent, List<Question> visibleQuestions, String logType) {
        if (visibleQuestions == null || visibleQuestions.isEmpty()) {
            logger.warn("No questions available for you at this moment.");
            return null;
        }

        Question question;
        String questionName = respondent.getAttribute(logType);
        question = getQuestionByShortName(visibleQuestions, questionName);

        return question;
    }
	*/
    /**
     * Update the max answeredd question
     *
     * @param respondent
     * @param currentQuestion
     * @param visibleQuestions
     */
    /*
    public static void updateMaxAnsweredQuestion(Node respondent, Question currentQuestion, List<Question> visibleQuestions) {
        int currentIdx = visibleQuestions.indexOf(currentQuestion);
        Question maxQuestion = getMaxAnsweredQuestion(respondent, visibleQuestions);
        int maxIdx = visibleQuestions.indexOf(maxQuestion);
        if (maxIdx < currentIdx) {
            respondent.setAttribute(Constants.NODE_MAX_ANSWERED_QUESTION_NAME, currentQuestion.getShortName());
        }
    }
    */
    

    
    /*
    @SuppressWarnings("rawtypes")
    public static List<Map> getProgressByNodeIds(Collection<Long> nodeIds) {
        NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
        List<Node> nodes = nodeDao.loadByIds(nodeIds);
        return getProgressByNodes(nodes);
    }

    // TODO replace getSequenceNumber by getIndex
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<Map> getProgressByNodes(Collection<Node> nodes) {

        List<Map> progressData = new LinkedList<Map>();
        for (Node node : nodes) {
            Map np = new HashMap();

            String ts_start = node.getAttribute(Constants.NODE_TS_START_SURVEY);
            String ts_finish = node.getAttribute(Constants.NODE_TS_FINISH_SURVEY);
            String maxShortName = node.getAttribute(Constants.NODE_MAX_ANSWERED_QUESTION_NAME);
            String lastShortName = node.getAttribute(Constants.NODE_LAST_ANSWERED_QUESTION_NAME);
            String lastTime = node.getAttribute(Constants.NODE_LAST_ANSWERED_QUESTION_TIME);
            Question maxQuestion = questionMap.get(maxShortName);
            Question lastQuestion = questionMap.get(lastShortName);

            if (ts_finish != null) {
                np.put("status", Constants.NODE_PROGRESS_FINISHED);
                np.put("progress", numQuestion + "/" + numQuestion);
                np.put("completeTime", ts_finish);
            } else if (ts_start != null) {
                if (maxQuestion == null) {
                    logger.warn("Question(shortName=" + maxShortName + ") has been deleted.");
                    np.put("status", Constants.NODE_PROGRESS_NOT_COMPLETED);
                } else if (maxQuestion.getIndex() < numQuestion) {
                    np.put("status", Constants.NODE_PROGRESS_NOT_COMPLETED);
                } else {
                    np.put("status", Constants.NODE_PROGRESS_COMPLETED);
                }

                np.put("progress", (maxQuestion == null ? "-1" : maxQuestion.getIndex()) + "/" + numQuestion);
                np.put("completeTime", "-");
            } else {
                if (maxShortName == null) {
                    np.put("status", Constants.NODE_PROGRESS_NOT_STARTED);
                    np.put("progress", "0/" + numQuestion);
                    np.put("completeTime", "-");
                } else { // this situation should not happen, just for handling legacy data
                    // TODO
                    // if (maxQuestion.getSequenceNumber() < numQuestion)
                    if (maxQuestion.getIndex() < numQuestion) {
                        np.put("status", Constants.NODE_PROGRESS_NOT_COMPLETED);
                    } else {
                        np.put("status", Constants.NODE_PROGRESS_COMPLETED);
                    }
                    np.put("progress", (maxQuestion == null ? "-1" : maxQuestion.getIndex()) + "/" + numQuestion);
                    np.put("completeTime", "-");
                }
            }
            np.put("maxQuestion", maxQuestion == null ? "-" : maxQuestion.getLabel());
            np.put("lastQuestion", lastQuestion == null ? "-" : lastQuestion.getLabel());
            np.put("lastTime", lastTime == null ? "-" : lastTime);

            progressData.add(np);
        }

        return progressData;
    }
    */
}
