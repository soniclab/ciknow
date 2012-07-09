package ciknow.dao.hibernate;

import ciknow.dao.SurveyDao;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.domain.Survey;
import ciknow.util.Beans;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *
 * @author gyao
 */
public class SurveyHibernateDao extends HibernateDaoSupport implements SurveyDao {

    private static final Log logger = LogFactory.getLog(SurveyHibernateDao.class);

    public static void main(String[] args) {
        Beans.init();
        SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");
        List<Survey> surveys = surveyDao.getAll();
        for (Survey survey : surveys) {
            logger.info("Survey: " + survey.getName());
            for (Page page : survey.getPages()) {
                logger.info("\tPage: " + page.getLabel());
                for (Question question : page.getQuestions()) {
                    logger.info("\t\tQuestion: " + question.getLabel() + ", Respondent: " + question.getVisibleGroups().iterator().next().getName());
                }
            }
        }
        System.exit(0);
    }

    @Override
    public void save(Survey survey) {
        getHibernateTemplate().saveOrUpdate(survey);
    }

    @Override
    public void save(Collection<Survey> surveys) {
        getHibernateTemplate().saveOrUpdateAll(surveys);
    }

    @Override
    public void delete(Survey survey) {
        getHibernateTemplate().delete(survey);
    }

    @Override
    public void delete(Collection<Survey> surveys) {
        getHibernateTemplate().deleteAll(surveys);
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete Survey");
    }

    @Override
    public Survey findById(Long id) {
        return (Survey) getHibernateTemplate().get(Survey.class, id);
    }

    @Override
    public List<Survey> getAll() {
        return getHibernateTemplate().loadAll(Survey.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Survey> findByDesigner(Node designer) {
        String query = "from Survey s where s.designer = ?";
        return getHibernateTemplate().find(query, designer);
    }

    @Override
    public int getCount() {
        return (Integer) getHibernateTemplate().find("select count(*) from Survey").get(0);
    }
}
