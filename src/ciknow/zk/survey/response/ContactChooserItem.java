package ciknow.zk.survey.response;

import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.util.Constants;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author gyao
 */
public class ContactChooserItem {

    public static final String IMAGE_KEY = "photo";
    private static Log logger = LogFactory.getLog(ContactChooserItem.class);
    private Node contact;
    private boolean showImage;
    private LinkedHashMap<String, String> map;
    private boolean selected;

    public ContactChooserItem(Node contact, boolean showImage, LinkedHashMap<String, String> columns, Map<String, Question> questionMap) {
        this.contact = contact;
        this.showImage = showImage;

        map = new LinkedHashMap<String, String>();

        if (showImage) {
            map.put(IMAGE_KEY, "images/photos/" + contact.getUsername() + ".jpg");
        }

        // minimum display contact label
        map.put("label", contact.getLabel());

        // custom columns
        for (String name : columns.keySet()) {
            if (name.equals("label")) {
                continue;
            }

            String value = null;
            if (name.equals("organization")) {
                value = contact.getOrganization();
            } else if (name.equals("department")) {
                value = contact.getDepartment();
            } else if (name.equals("unit")) {
                value = contact.getUnit();
            } else if (name.equals("type")) {
                value = contact.getType();
            } else if (name.equals("lastName")) {
                value = contact.getLastName();
            } else if (name.equals("firstName")) {
                value = contact.getFirstName();
            } else if (name.equals("city")) {
                value = contact.getCity();
            } else if (name.equals("state")) {
                value = contact.getState();
            } else if (name.equals("country")) {
                value = contact.getCountry();
            } else if (name.equals("zipcode")) {
                value = contact.getZipcode();
            } else if (name.startsWith("Q" + Constants.SEPERATOR)) { // choice (single) or multipleChoice question 
                String shortName = name.substring(2);
                Question question = questionMap.get(shortName);
                for (Field field : question.getFields()) {
                    String key = question.makeFieldKey(field);
                    value = contact.getAttribute(key);
                    if (value != null) {
                        if (value.equals("1")) {
                            value = field.getLabel();
                        } else {
                            // this is for "Other" popup
                        }
                        break;
                    }
                }

                if (value == null) {
                    value = ""; // if no selection, set it as blank
                }
            } else {
                value = "";
                logger.warn("Attribute '" + name + "' is not available.");
            }

            map.put(name, value);
        }
    }

    public Set<String> getColumns() {
        return map.keySet();
    }

    public String getValue(String column) {
        return map.get(column);
    }

    public Node getContact() {
        return contact;
    }

    public boolean isShowImage() {
        return showImage;
    }

    public String getImageSource() {
        return getValue(IMAGE_KEY);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
