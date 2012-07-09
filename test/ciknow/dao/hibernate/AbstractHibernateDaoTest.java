package ciknow.dao.hibernate;

import org.hibernate.SessionFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/applicationContext-dao.xml", "classpath:/applicationContext-datasource.test.ciknow.xml"})
public abstract class AbstractHibernateDaoTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired protected SessionFactory sf;

}
