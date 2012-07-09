package ciknow.dao;

import ciknow.domain.Activity;
import ciknow.domain.Node;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Data access interface for Node
 *
 * @author gyao
 *
 */
public interface ActivityDao {

    public void save(Activity activity);
    public void delete(Collection<Activity> acts);
    
    // QUERY
    //get all activities in the database
    public List<Activity> getAll();

    public Activity getLatestActivity();

    public List<Activity> getActivitiesBefore(Date date);

    public List<Activity> getActivitiesAfter(Date date);

    public List<Activity> getActivitiesBetween(Date begin, Date end);
    
    public List<Activity> getActivitiesBySubject(Node subject);
    public List<Activity> getActivitiesByObject(Node object);
}
