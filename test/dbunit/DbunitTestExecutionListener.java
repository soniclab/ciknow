package dbunit;

import java.sql.Connection;

import javax.activation.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.util.StringUtils;


public class DbunitTestExecutionListener implements TestExecutionListener {
	private static final Log log = LogFactory
			.getLog(DbunitTestExecutionListener.class);

	@Override
	public void afterTestClass(TestContext tc) throws Exception {

	}

	@Override
	public void afterTestMethod(TestContext tc) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTestClass(TestContext tc) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTestMethod(TestContext tc) throws Exception {
		String dataSetResourcePath = null;

		// first, the annotation on the test class
		Dbunit dbunit = tc.getTestMethod().getAnnotation(
				Dbunit.class);

		if (dbunit != null) {
			dataSetResourcePath = dbunit.dataSetLocation();
		} else {
			String tempDsRes = tc.getTestClass().getName();
			tempDsRes = StringUtils.replace(tempDsRes, ".", "/");
			tempDsRes = "/" + tempDsRes + "-dataset.xml";
			if (getClass().getResourceAsStream(tempDsRes) != null) {
				dataSetResourcePath = tempDsRes;
			} else {
				// do nothing
			}
		}

		if (dataSetResourcePath != null) {
			Resource dataSetResource = tc.getApplicationContext().getResource(
				dataSetResourcePath
			);
			IDataSet dataSet = new FlatXmlDataSetBuilder()
					.build(dataSetResource.getInputStream());
			
			Connection con = DataSourceUtils
					.getConnection((javax.sql.DataSource) tc
							.getApplicationContext().getBean("dataSource"));
			IDatabaseConnection dbConn = new DatabaseConnection(con);
			DatabaseConfig config = dbConn.getConfig();
			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
			
			DatabaseOperation.CLEAN_INSERT.execute(dbConn, dataSet);
			log.info("load dataset: " + dataSetResourcePath);
		} else {
			log.info("no default dataset");
		}
	}

	@Override
	public void prepareTestInstance(TestContext tc) throws Exception {
		// TODO Auto-generated method stub

	}

}
