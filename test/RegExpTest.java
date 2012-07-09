import ciknow.dao.QuestionDao;
import ciknow.domain.Question;
import ciknow.util.Beans;


public class RegExpTest {
	public static void main(String[] args){
		Beans.init();
		QuestionDao questionDao = (QuestionDao)Beans.getBean("questionDao");
		Question question = questionDao.findById(1L);
		String input = question.getHtmlInstruction();
		String output = input.replaceAll("SIZE=\"[0-9]+\"", "");
		System.out.println(input);
		System.out.println(output);
	}
}
