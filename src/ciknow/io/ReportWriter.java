package ciknow.io;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.ActivityDao;
import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.PageDao;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.service.ActivityService;
import ciknow.util.Beans;

public class ReportWriter {
	private static Log logger = LogFactory.getLog(ReportWriter.class);
	private NodeDao nodeDao;
	private GroupDao groupDao;
	//private QuestionDao questionDao;
	private PageDao pageDao;
    private ActivityDao activityDao;
    private ActivityService activityService;
    
	public static void main(String[] args) throws Exception{
		Beans.init();
		ReportWriter writer = (ReportWriter)Beans.getBean("reportWriter");
		writer.writeSystemReport(System.out, 1L);
	}
	
	public void writeSystemReport(OutputStream os, Long groupId) throws Exception{
		logger.info("write system report for group (id=" + groupId + ")...");
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		List<Long> nodeIds = groupDao.getNodeIdsByGroupId(groupId);
		List<Node> nodes = nodeDao.findByIds(nodeIds);
		List<Page> pages = pageDao.getAll();
		List<Map<String, String>> progressData = activityService.getProgress(nodes, pages);
		sb.append("'-' indicate missing or unavailable data.\n\n");
		sb.append("ID").append("\t")
			.append("Username").append("\t")
			.append("Label").append("\t")
			.append("Status").append("\t")
			.append("Progress").append("\t")
			.append("LastAnsweredPage").append("\t")
			.append("LastAnsweredTime").append("\t").append("\n");
		
		logger.debug("writing " + progressData.size() + " entries...");
		DateFormat formater = new SimpleDateFormat("yyyy.MM.dd-HH:mm");
		for (Map<String, String> m : progressData){
			String lastEnteredTime = (String)m.get("lastEnteredTime");
			if (!lastEnteredTime.equals("-")){
				lastEnteredTime = formater.format(new Date(Long.parseLong(lastEnteredTime)));
			}
			
			sb.append(m.get("id")).append("\t")
			.append(m.get("username")).append("\t")
			.append(m.get("label")).append("\t")
			.append(m.get("status")).append("\t")
			.append(m.get("progress")).append("\t")
			.append(m.get("lastEnteredPage")).append("\t")
			.append(lastEnteredTime).append("\t").append("\n");			
		}
		writer.append(sb.toString());
		writer.close();
	}

	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public ActivityDao getActivityDao() {
		return activityDao;
	}

	public void setActivityDao(ActivityDao activityDao) {
		this.activityDao = activityDao;
	}

	public PageDao getPageDao() {
		return pageDao;
	}

	public void setPageDao(PageDao pageDao) {
		this.pageDao = pageDao;
	}

	public ActivityService getActivityService() {
		return activityService;
	}

	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}

	
}
