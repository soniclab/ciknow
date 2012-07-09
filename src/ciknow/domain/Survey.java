package ciknow.domain;

import ciknow.util.Constants;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author gyao
 */
public class Survey implements java.io.Serializable {
	private static final Log logger = LogFactory.getLog(Survey.class);
    private static final long serialVersionUID = -8696293321997368683L;
    private Long id;
    private Long version;
    private Node designer;
    private String name;
    private String description;
    private Date timestamp;
    private List<Page> pages = new ArrayList<Page>(0);
    private Map<String, String> attributes = new HashMap<String, String>();
    private Map<String, String> longAttributes = new HashMap<String, String>();

    public Survey() {
    }

    public Page getPageByLabel(String label) {
        for (Page page : pages) {
            if (page.getLabel().equals(label)) {
                return page;
            }
        }
        return null;
    }

    public boolean hasContactChooser() {
        for (Page page : pages) {
            for (Question q : page.getQuestions()) {
                if (q.getType().equals(Constants.CONTACT_CHOOSER)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Page> getVisiblePages(Node node){
    	logger.info("Get visible pages for node: " + node.getLabel());
    	if (node.isAdmin()) return new ArrayList<Page>(pages);
    	List<Page> visiblePages = new ArrayList<Page>();
    	for (Page page : pages){
    		if (page.isVisible(node)) visiblePages.add(page);
    	}
    	return visiblePages;
    }
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Node getDesigner() {
        return this.designer;
    }

    public void setDesigner(Node designer) {
        this.designer = designer;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public Map<String, String> getLongAttributes() {
        return longAttributes;
    }

    public void setLongAttributes(Map<String, String> attributes) {
        this.longAttributes = attributes;
    }

    public String getLongAttribute(String key) {
        return longAttributes.get(key);
    }

    public void setLongAttribute(String key, String value) {
        longAttributes.put(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Survey other = (Survey) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
