package ciknow.ro;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.NodeDao;
import ciknow.dao.SurveyDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Survey;
import ciknow.dto.SurveyDTO;
import ciknow.util.SurveyUtil;

public class SurveyRO {
	private static Log logger = LogFactory.getLog(SurveyRO.class);
	SurveyDao surveyDao;
	NodeDao nodeDao;
    QuestionDao questionDao;

    public SurveyRO(){

	}

    public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public SurveyDao getSurveyDao() {
        return surveyDao;
    }

    public void setSurveyDao(SurveyDao surveyDao) {
        this.surveyDao = surveyDao;
    }

    public QuestionDao getQuestionDao() {
        return questionDao;
    }

    public void setQuestionDao(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    public SurveyDTO createSurvey(SurveyDTO dto){
        return saveOrUpdateSurvey(dto);
    }

    public SurveyDTO updateSurvey(SurveyDTO dto){
        return saveOrUpdateSurvey(dto);
    }

    private SurveyDTO saveOrUpdateSurvey(SurveyDTO dto){
        Survey survey;
        if (dto.surveyId == 0){
            logger.debug("creating new survey:" + dto);
            survey = new Survey();
        } else {
            logger.debug("updating survey: " + dto);
            survey = surveyDao.findById(dto.surveyId);
            if (survey == null) return null;
            survey.setVersion(dto.version);
        }

        survey.setName(dto.name);
        survey.setDescription(dto.description);
        survey.setDesigner(nodeDao.getProxy(dto.designerId));
        survey.setTimestamp(new Date());
        survey.setAttributes(dto.attributes);
        survey.setLongAttributes(dto.longAttributes);

/*        Set<Question> questions = new HashSet<Question>();
        for (Long questionId : dto.questions){
            Question q = questionDao.findById(questionId);
            if (q != null) questions.add(q);
        }
        survey.setQuestions(questions);*/

        surveyDao.save(survey);

		SurveyUtil.setSurvey(survey);
		
        return new SurveyDTO(survey);
    }

    public Long deleteSurveyById(Long id){
        Survey survey = surveyDao.findById(id);
        if (survey != null) surveyDao.delete(survey);
        return id;
    }

    public SurveyDTO getSurveyById(Long id){
        Survey survey = surveyDao.findById(id);
        if (survey == null) return null;
        else return new SurveyDTO(survey);
    }

    public List<SurveyDTO> getAllSurveys(){
		logger.debug("getAllSurveys...");
		List<SurveyDTO> dtos = new ArrayList<SurveyDTO>();
		List<Survey> surveys = surveyDao.getAll();
		for (Survey s : surveys){
			dtos.add(new SurveyDTO(s));			
		}
		logger.debug("getAllSurveys...done");
		return dtos;
	}
}
