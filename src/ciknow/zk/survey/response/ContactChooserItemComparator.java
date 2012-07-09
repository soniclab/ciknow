package ciknow.zk.survey.response;

import java.util.Comparator;

/**
 *
 * @author gyao
 */
public class ContactChooserItemComparator implements Comparator<ContactChooserItem> {

    private String column;
    private boolean ascending;
    private boolean numeric;
    private boolean casesensitive;

    public ContactChooserItemComparator(String column, boolean ascending, boolean numeric, boolean casesensitive) {
        this.column = column;
        this.ascending = ascending;
        this.numeric = numeric;
        this.casesensitive = casesensitive;
    }

    @Override
    public int compare(ContactChooserItem c1, ContactChooserItem c2) {
        String v1 = c1.getValue(column);
        String v2 = c2.getValue(column);

        if (!casesensitive) {
            v1 = v1.toLowerCase();
            v2 = v2.toLowerCase();
        }

        int result;
        if (numeric) {
            Double n1 = v1.isEmpty() ? 0 : Double.parseDouble(v1);
            Double n2 = v2.isEmpty() ? 0 : Double.parseDouble(v2);
            result = n1.compareTo(n2);
        } else {
            result = v1.compareTo(v2);
        }

        result = ascending ? result : -result;

        return result;
    }
}
