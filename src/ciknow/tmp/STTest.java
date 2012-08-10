package ciknow.tmp;

import org.stringtemplate.v4.ST;

import ciknow.dao.NodeDao;
import ciknow.domain.Node;
import ciknow.util.Beans;

public class STTest {
	public static void main(String[] args){
		Beans.init();
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		Node node = nodeDao.loadById(1L);
		ST t = new ST("{user.username}: {user.attributes.NODE_FIRST_TIMER}", '{', '}');
		t.add("user", node);
		
		System.out.println(t.render());
		System.exit(0);
	}
}
