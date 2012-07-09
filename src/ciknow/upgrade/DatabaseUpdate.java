package ciknow.upgrade;

import ciknow.dao.*;
import ciknow.domain.*;
import ciknow.util.Beans;
import java.sql.*;
import java.util.*;

/**
 *
 * @author gyao
 */
public class DatabaseUpdate {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Beans.init();
        SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");
        QuestionDao questionDao = (QuestionDao) Beans.getBean("questionDao");
        FieldDao fieldDao = (FieldDao) Beans.getBean("fieldDao");
        ScaleDao scaleDao = (ScaleDao) Beans.getBean("scaleDao");
        TextFieldDao textFieldDao = (TextFieldDao) Beans.getBean("textFieldDao");
        ContactFieldDao contactFieldDao = (ContactFieldDao) Beans.getBean("contactFieldDao");

        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/_ciknow";
        Connection con = DriverManager.getConnection(url, "sonic", "sonic");


        // Convert into page structure
        Survey survey = surveyDao.findById(1L);
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT question_id FROM questions order by sequence_number");
        int index = 1;
        while (rs.next()) {
            Long questionId = rs.getLong("question_id");
            Question question = questionDao.findById(questionId);
            Page page = new Page();
            page.setName("page_" + index);
            page.setLabel(page.getName());
            page.setInstruction("this is page " + index);

            survey.getPages().add(page);
            page.setSurvey(survey);
            question.setPage(page);
            page.getQuestions().add(question);

            index++;
        }

        surveyDao.save(survey);



        // Re-structure fields/scales/textfields/contactfields
        String[] tables = {"question_fields", "question_scales", "question_text_fields", "question_contact_fields"};
        String[] columns = {"field_id", "scale_id", "text_field_id", "contact_field_id"};

        for (int i = 0; i < tables.length; i++) {
            st = con.createStatement();
            rs = st.executeQuery("SELECT question_id, sequence_number FROM " + tables[i]);
            Map<Long, List<Integer>> map = new LinkedHashMap<Long, List<Integer>>();
            while (rs.next()) {
                Long questionId = rs.getLong("question_id");
                Integer sn = rs.getInt("sequence_number");

                List<Integer> list = map.get(questionId);
                if (list == null) {
                    list = new ArrayList<Integer>();
                    map.put(questionId, list);
                }
                list.add(sn);
            }

            PreparedStatement ps = con.prepareStatement("UPDATE " + tables[i] + " SET " + columns[i] + "=?, version=0 where question_id=? and sequence_number=?");
            long id = 1;
            for (Long questionId : map.keySet()) {
                List<Integer> list = map.get(questionId);
                for (Integer sn : list) {
                    ps.setLong(1, id);
                    ps.setLong(2, questionId);
                    ps.setInt(3, sn);
                    ps.executeUpdate();

                    id++;
                }
            }

        }


        Map<String, Question> questionMap = new HashMap<String, Question>();
        List<Question> questions = questionDao.getAll();
        for (Question question : questions) {
            question.getFields().clear();
            question.getScales().clear();
            question.getTextFields().clear();
            question.getContactFields().clear();
            questionMap.put(question.getShortName(), question);
        }

        List<Field> fields = fieldDao.getAll();
        for (Field field : fields) {
            Question question = questionMap.get(field.getQuestion().getShortName());
            question.getFields().add(field);
        }

        List<Scale> scales = scaleDao.getAll();
        for (Scale scale : scales) {
            Question question = questionMap.get(scale.getQuestion().getShortName());
            question.getScales().add(scale);
        }

        List<TextField> textFields = textFieldDao.getAll();
        for (TextField textField : textFields) {
            Question question = questionMap.get(textField.getQuestion().getShortName());
            question.getTextFields().add(textField);
        }

        List<ContactField> contactFields = contactFieldDao.getAll();
        for (ContactField contactField : contactFields) {
            Question question = questionMap.get(contactField.getQuestion().getShortName());
            question.getContactFields().add(contactField);
        }

        questionDao.save(questions);



        System.exit(0);
    }
}
