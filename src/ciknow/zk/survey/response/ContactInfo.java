package ciknow.zk.survey.response;

import ciknow.domain.ContactField;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.util.Constants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class ContactInfo extends AbstractQuestionAttribute {

    private static final long serialVersionUID = -1907949663962052175L;
    private static Log logger = LogFactory.getLog(ContactInfo.class);

    public ContactInfo(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    public ContactInfo(Question currentQuestion) {
        super(currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        // header
        Columns columns = new Columns();
        columns.setParent(grid);
        columns.setSizable(true);

        Column column = new Column();
        column.setParent(columns);
        column.setWidth(getFirstColumnWidth() + "px");

        // body
        Rows rows = new Rows();
        rows.setParent(grid);
        for (ContactField field : currentQuestion.getContactFields()) {
            Row row = new Row();
            row.setParent(rows);
            row.setValue(field);

            Label fieldLabel = new Label(field.getLabel());
            fieldLabel.setParent(row);

            Textbox textbox = new Textbox();
            textbox.setParent(row);
            textbox.setWidth("99%");
            textbox.setValue(getContactInfo(field));
            if (field.getName().equals(Constants.CONTACT_FIELD_EMAIL)) {
                textbox.setConstraint("/.+@.+\\.[a-z]+/: e-mail address required");
            }
        }
    }

    @Transactional
    @Override
    public void save() {
        for (Object child : grid.getRows().getChildren()) {
            Row row = (Row) child;
            ContactField field = (ContactField) row.getValue();
            Textbox textbox = (Textbox) row.getChildren().get(1);
            setContactInfo(field, textbox.getValue().trim());
        }

        logger.debug("save to database");
        nodeDao.save(respondent);
    }

    private String getContactInfo(ContactField field) {
        String name = field.getName();
        String value;

        if (name.equals(Constants.CONTACT_FIELD_ADDR1)) {
            value = respondent.getAddr1();
        } else if (name.equals(Constants.CONTACT_FIELD_ADDR2)) {
            value = respondent.getAddr2();
        } else if (name.equals(Constants.CONTACT_FIELD_CITY)) {
            value = respondent.getCity();
        } else if (name.equals(Constants.CONTACT_FIELD_STATE)) {
            value = respondent.getState();
        } else if (name.equals(Constants.CONTACT_FIELD_COUNTRY)) {
            value = respondent.getCountry();
        } else if (name.equals(Constants.CONTACT_FIELD_ZIP)) {
            value = respondent.getZipcode();
        } else if (name.equals(Constants.CONTACT_FIELD_PHONE)) {
            value = respondent.getPhone();
        } else if (name.equals(Constants.CONTACT_FIELD_CELL)) {
            value = respondent.getCell();
        } else if (name.equals(Constants.CONTACT_FIELD_FAX)) {
            value = respondent.getFax();
        } else if (name.equals(Constants.CONTACT_FIELD_EMAIL)) {
            value = respondent.getEmail();
        } else if (name.equals(Constants.CONTACT_FIELD_DEPARTMENT)) {
            value = respondent.getDepartment();
        } else if (name.equals(Constants.CONTACT_FIELD_ORGANIZATION)) {
            value = respondent.getOrganization();
        } else if (name.equals(Constants.CONTACT_FIELD_UNIT)) {
            value = respondent.getUnit();
        } else if (name.equals(Constants.CONTACT_FIELD_URL)) {
            value = respondent.getUri();
        } else {
            String fieldKey = currentQuestion.makeContactFieldKey(field);
            value = respondent.getAttribute(fieldKey);
        }

        if (value == null) {
            value = "";
        }
        return value;
    }

    private void setContactInfo(ContactField field, String value) {
        String name = field.getName();
        if (value == null) {
            value = "";
        }
        if (name.equals(Constants.CONTACT_FIELD_ADDR1)) {
            respondent.setAddr1(value);
        } else if (name.equals(Constants.CONTACT_FIELD_ADDR2)) {
            respondent.setAddr2(value);
        } else if (name.equals(Constants.CONTACT_FIELD_CITY)) {
            respondent.setCity(value);
        } else if (name.equals(Constants.CONTACT_FIELD_STATE)) {
            respondent.setState(value);
        } else if (name.equals(Constants.CONTACT_FIELD_COUNTRY)) {
            respondent.setCountry(value);
        } else if (name.equals(Constants.CONTACT_FIELD_ZIP)) {
            respondent.setZipcode(value);
        } else if (name.equals(Constants.CONTACT_FIELD_PHONE)) {
            respondent.setPhone(value);
        } else if (name.equals(Constants.CONTACT_FIELD_CELL)) {
            respondent.setCell(value);
        } else if (name.equals(Constants.CONTACT_FIELD_FAX)) {
            respondent.setFax(value);
        } else if (name.equals(Constants.CONTACT_FIELD_EMAIL)) {
            respondent.setEmail(value);
        } else if (name.equals(Constants.CONTACT_FIELD_DEPARTMENT)) {
            respondent.setDepartment(value);
        } else if (name.equals(Constants.CONTACT_FIELD_ORGANIZATION)) {
            respondent.setOrganization(value);
        } else if (name.equals(Constants.CONTACT_FIELD_UNIT)) {
            respondent.setUnit(value);
        } else if (name.equals(Constants.CONTACT_FIELD_URL)) {
            respondent.setUri(value);
        } else {
            String fieldKey = currentQuestion.makeContactFieldKey(field);
            if (value.isEmpty()) {
                respondent.getAttributes().remove(fieldKey);
            } else {
                respondent.setAttribute(fieldKey, value);
            }
        }
    }
}
