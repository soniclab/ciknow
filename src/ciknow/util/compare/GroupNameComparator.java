package ciknow.util.compare;

import ciknow.domain.Group;
import java.util.Comparator;

/**
 *
 * @author gyao
 */
public class GroupNameComparator implements Comparator<Group>{

    private Boolean asc;
    private Boolean casesensitive;

    public GroupNameComparator(Boolean asc, Boolean casesensitive) {
        this.asc = asc;
        this.casesensitive = casesensitive;
    }
        
    @Override
    public int compare(Group g1, Group g2) {
        String s1 = g1.getName();
        String s2 = g2.getName();
        
        if (!casesensitive) {
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();
        }
        
        if (asc) return s1.compareTo(s2);
        else return s2.compareTo(s1);
    }
    
}
