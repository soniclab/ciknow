package ciknow.tunning;

import java.util.HashMap;
import java.util.Map;

import ciknow.recommend.service.RecommenderService;
import ciknow.recommend.service.RecommenderServiceImpl;
import ciknow.util.Beans;

public class RecommenderServiceTest {
	public static void main(String[] args) throws Exception {
        Beans.init();
        RecommenderService service = (RecommenderService) Beans.getBean("recommenderServiceImpl");
        
        testComputeRec(service);
	}
	
	private static void testComputeRec(RecommenderService service) throws Exception{
    	Map<String, String> map = new HashMap<String, String>();
    	map.put("source", "identification");
    	map.put("row", "user");
    	map.put("col", "Keyword");
    	((RecommenderServiceImpl)service).computeRec(map);
	}
}
