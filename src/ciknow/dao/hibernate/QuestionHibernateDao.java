package ciknow.dao.hibernate;

import ciknow.dao.QuestionDao;
import ciknow.domain.Question;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *
 * @author gyao
 */
public class QuestionHibernateDao extends HibernateDaoSupport implements QuestionDao {

    @Override
    public void persist(Question question) {
        getHibernateTemplate().persist(question);
    }

    @Override
    public void save(Question question) {
        getHibernateTemplate().saveOrUpdate(question);
    }

    @Override
    public void save(Collection<Question> questions) {
        getHibernateTemplate().saveOrUpdateAll(questions);
    }

    @Override
    public void delete(Question question) {
        getHibernateTemplate().delete(question);
    }

    @Override
    public void delete(Collection<Question> questions) {
        getHibernateTemplate().deleteAll(questions);
    }

    @Override
    public void deleteAll() {
        // this will run into problem because HQL cannot cascade
        //getHibernateTemplate().bulkUpdate("delete Question");

        getHibernateTemplate().deleteAll(getAll());
    }

    @Override
    public Question findById(Long id) {
        return (Question) getHibernateTemplate().get(Question.class, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Question findByShortName(String shortName) {
        String query = "from Question q where q.shortName = :shortName";
        List<Question> questions = getHibernateTemplate().findByNamedParam(query, "shortName", shortName);
        if (questions != null && !questions.isEmpty()) {
            return questions.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Question> getAll() {
        List<Question> questions = getHibernateTemplate().loadAll(Question.class);

        // TODO
        //Collections.sort(questions, new QuestionSequenceComparator());
        return questions;
    }

    public int getCount() {
        return (Integer) getHibernateTemplate().find("select count(*) from Question").get(0);
    }

    @SuppressWarnings("unchecked")
    public List<Question> findBySurveyId(Long surveyId) {
        String query = "from Question q where q.survey.id = :surveyId";
        return getHibernateTemplate().findByNamedParam(query, "surveyId", surveyId);
    }

    // TODO eliminate this junk method
    public int getMaxSequenceNumber() {
        int sequenceNumber = 0;
//		List list = getHibernateTemplate().find("select max(sequenceNumber) from Question");
//		if (list != null && list.size() > 0) sequenceNumber = (Integer)list.get(0);
        List<Question> questions = getAll();
        if (questions.size() > 0) {
            for (Question q : questions) {
                if (q.getIndex() > sequenceNumber) {
                    sequenceNumber = q.getIndex();
                }
            }
        }
        return sequenceNumber;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deleteAttributeByValue(final String value) {
        HibernateTemplate ht = getHibernateTemplate();
        ht.execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                Connection con = session.connection();
                PreparedStatement ps;

                try {

                    String sql = "DELETE FROM question_attributes WHERE attr_value=?";
                    logger.debug("deleting attributes: " + sql);
                    ps = con.prepareStatement(sql);
                    ps.setString(1, value);
                    ps.executeUpdate();

                    logger.debug("question attributes with value=" + value + " are deleted.");
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }

                return null;
            }
        });

    }

    @SuppressWarnings("unchecked")
    @Override
    public void deleteLongAttributeByValue(final String value) {
        HibernateTemplate ht = getHibernateTemplate();
        ht.execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                Connection con = session.connection();
                PreparedStatement ps;

                try {

                    String sql = "DELETE FROM question_long_attributes WHERE attr_value=?";
                    logger.debug("deleting attributes: " + sql);
                    ps = con.prepareStatement(sql);
                    ps.setString(1, value);
                    ps.executeUpdate();

                    logger.debug("question long attributes with value=" + value + " are deleted.");
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }

                return null;
            }
        });
    }
}
