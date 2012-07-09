package ciknow.dao;

import ciknow.domain.Question;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author gyao
 */
public interface QuestionDao {

    public void persist(Question question);

    public void save(Question question);

    public void save(Collection<Question> questions);

    public void delete(Question question);

    public void delete(Collection<Question> questions);

    public void deleteAll();

    public Question findById(Long id);

    public Question findByShortName(String shortName);

    public List<Question> getAll();
    public int getCount();
    //public List<Question> findBySurveyId(Long surveyId);
    //public int getMaxSequenceNumber();

    // for clearing question data
    // remove all rows with attr_value=value from table question_attributes
    // for example, if an affiliated contact chooser quesion is deleted...
    public void deleteAttributeByValue(String value);

    public void deleteLongAttributeByValue(String value);
}
