package ciknow.dao;

import ciknow.domain.Node;
import ciknow.domain.Survey;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author gyao
 */
public interface SurveyDao {

    public void save(Survey survey);

    public void save(Collection<Survey> surveys);

    public void delete(Survey survey);

    public void delete(Collection<Survey> surveys);

    public void deleteAll();

    public List<Survey> getAll();

    public int getCount();

    public Survey findById(Long id);

    public List<Survey> findByDesigner(Node designer);
}
