package ciknow.io;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import ciknow.domain.*;
import ciknow.ro.GenericRO;
import ciknow.teamassembly.Team;
import ciknow.teamassembly.TeamBuilder;
import ciknow.util.Beans;
import ciknow.util.Constants;

public class TeamWriter {	
	private NumberFormat nf = new DecimalFormat("0.00");

	@SuppressWarnings("unchecked")
	public void writeSummary(TeamBuilder tb, Writer writer) throws IOException{
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>Summary</title>");
		sb.append("</head>");		
		sb.append("<body style='font-family: Arial,Helvetica,sans-serif'>");
		
		sb.append("Number of Teams=" + tb.getNumTeams()).append("<br>");
		sb.append("Min Team Size=" + tb.getMinTeamSize()).append("<br>");
		sb.append("Max Team Size=" + tb.getMaxTeamSize()).append("<br>");
		sb.append("Skills=");
		List<String> labels = new LinkedList<String>();
		List<Map> skills = (List<Map>)tb.getParams().get("skills");
		for (Map skill : skills){			
			String label = (String) skill.get("label");
			labels.add(label);			
		}
		sb.append(labels);
		sb.append("<br>");
		sb.append("Hobbies=");
		labels = new LinkedList<String>();
		for (String hobby : tb.getHobbies()){
			String label = hobby.substring(hobby.lastIndexOf(Constants.SEPERATOR) + 1);
			labels.add(label);			
		}
		sb.append(labels);
		sb.append("<br>");
		sb.append("Number of nodes=" + tb.getNodes().size()).append("<br>");
		sb.append("Number of edges=" + tb.getEdges().size()).append("<br><br>");		
		
		sb.append("<table style='border:0px solid black'>");
		sb.append("<tr>");
		sb.append("<th style='background-color:#D2B48C; text-align:center; padding-left:2px; padding-right:2px'>").append("ID").append("</th>");
		sb.append("<th style='background-color:#D2B48C; text-align:center; padding-left:2px; padding-right:2px'>").append("Size").append("</th>");
		sb.append("<th style='background-color:#D2B48C; text-align:center; padding-left:2px; padding-right:2px'>").append("Score").append("</th>");
		sb.append("<th style='background-color:#D2B48C; text-align:center; padding-left:2px; padding-right:2px'>").append("Diversity").append("</th>");
		sb.append("<th style='background-color:#D2B48C; text-align:center; padding-left:2px; padding-right:2px'>").append("Similarity").append("</th>");
		sb.append("<th style='background-color:#D2B48C; text-align:center; padding-left:2px; padding-right:2px'>").append("Density").append("</th>");
		sb.append("<th style='background-color:#D2B48C; text-align:center; padding-left:2px; padding-right:2px'>").append("Members").append("</th>");
		sb.append("</tr>");
		int row=0;
		for (Team t : tb.getTeams()){
			if (row%2 == 0) sb.append("<tr style='background-color:#F5F5DC'>");
			else sb.append("<tr>");
			sb.append("<td style='text-align:center'>").append(embedTeamLink(t)).append("</td>");
			sb.append("<td style='text-align:center'>").append(t.getSize()).append("</td>");
			sb.append("<td style='text-align:center'>").append(nf.format(t.getScore())).append("</td>");
			sb.append("<td style='text-align:center'>").append(nf.format(t.getDiversity())).append("</td>");
			sb.append("<td style='text-align:center'>").append(nf.format(t.getSimilarity())).append("</td>");
			sb.append("<td style='text-align:center'>").append(nf.format(t.getDensity())).append("</td>");
			
			sb.append("<td>");
			for (int i=0; i < t.getSize(); i++){
				Node member = t.getMembers().get(i);
				if (i > 0) sb.append(", ");
				sb.append(embedNodeLink(member));
			}
			sb.append("</td>");
			
			sb.append("</tr>");
			row++;
		}
		sb.append("</table>");
		
		sb.append("</body>");
		sb.append("</html>");
		
		writer.append(sb.toString());
	}
	
	@SuppressWarnings("unchecked")
	public void writeTeam(Team t, Writer writer) throws IOException{
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>Team Details</title>");
		sb.append("</head>");		
		sb.append("<body style='font-family: Arial,Helvetica,sans-serif'>");
		
		sb.append("Size=").append(t.getSize()).append("<br>");
		sb.append("Score=").append(nf.format(t.getScore())).append("<br>");
		sb.append("Diversity=").append(nf.format(t.getDiversity())).append("<br>");
		sb.append("Similarity=").append(nf.format(t.getSimilarity())).append("<br>");
		sb.append("Density=").append(nf.format(t.getDensity())).append("<br>");
		
		sb.append("<h4>DIVERSITY<h4>");
		sb.append("<table style='border:0px solid black'>");
		sb.append("<tr><th></th>");
		List<Map> skills = (List<Map>)t.getTb().getParams().get("skills");
		for (Map skill : skills){			
			String label = (String) skill.get("label");
			sb.append("<th style='background-color:#D2B48C; text-align:center; padding-left:2px; padding-right:2px'>").append(label).append("</th>");
		}
		sb.append("</tr>");
		
		// identify those members who don't have any skills
		Collection<Node> uselessMembers = TeamBuilder.getMembersWithoutAttributes(t.getMembers(), t.getTb().getSkillNames());
		
		int row = 0;
		for (Node member : t.getMembers()){
			if (row%2 == 0) sb.append("<tr style='background-color:#F5F5DC'>");
			else sb.append("<tr>");
			sb.append("<td>").append(embedNodeLink(member)).append("</td>");
			
			if (uselessMembers.contains(member)){
				for (Map skill : skills){
					sb.append("<td style='text-align:center'>-</td>");
				}
			} else {
				for (Map skill : skills){
					String name = (String) skill.get("name");
					sb.append("<td style='text-align:center'>").append(member.getAttribute(name) == null ? "&nbsp;":"x").append("</td>");
				}
			}
			sb.append("</tr>");
			row++;
		}		
		sb.append("</table>");		
		
		sb.append("<h4>SIMILARITY<h4>");
		sb.append("<table style='border:0px solid black'>");
		sb.append("<tr><th></th>");
		for (String hobby : t.getTb().getHobbies()){
			String label = hobby.substring(hobby.lastIndexOf(Constants.SEPERATOR) + 1);
			sb.append("<th style='background-color:#D2B48C; text-align:center; padding-left:2px; padding-right:2px'>").append(label).append("</th>");
		}
		sb.append("</tr>");		
		
		Collection<Node> boringMembers = TeamBuilder.getMembersWithoutAttributes(t.getMembers(), t.getTb().getHobbies());		
		
		row = 0;
		for (Node member : t.getMembers()){
			if (row%2 == 0) sb.append("<tr style='background-color:#F5F5DC'>");
			else sb.append("<tr>");
			sb.append("<td>").append(embedNodeLink(member)).append("</td>");
			
			if (boringMembers.contains(member)){
				for (String hobby : t.getTb().getHobbies()){
					sb.append("<td style='text-align:center'>-</td>");
				}
			} else {
				for (String hobby : t.getTb().getHobbies()){
					sb.append("<td style='text-align:center'>").append(member.getAttribute(hobby) == null ? "&nbsp;":"x").append("</td>");
				}
			}
			sb.append("</tr>");
			row++;
		}		
		sb.append("</table>");
		
		
		sb.append("<h4>NETWORK<h4>");
		Map<String, List<Edge>> edgesByType = classifyEdges(t.getTb().getEdges());
		for (String edgeType : edgesByType.keySet()){
			sb.append("Edge Type: " + edgeType).append("<br>");
			List<Edge> edges = edgesByType.get(edgeType);
			
			sb.append("<table style='border:0px solid black'>");
			sb.append("<tr><th></th>");
			for (Node member : t.getMembers()){
				sb.append("<th style='background-color:#D2B48C; text-align:center; padding-left:2px; padding-right:2px'>").append(embedNodeLink(member)).append("</th>");
			}
			sb.append("</tr>");
			
			row = 0;
			for (Node member : t.getMembers()){
				if (row%2 == 0) sb.append("<tr style='background-color:#F5F5DC'>");
				else sb.append("<tr>");
				sb.append("<td>").append(embedNodeLink(member)).append("</td>");
				for (Node member2 : t.getMembers()){
					Edge edge = getEdge(edges, member, member2);
					sb.append("<td style='text-align:center'>").append(edge == null ? "&nbsp;":edge.getWeight()).append("</td>");
				}
				sb.append("</tr>");
				row++;
			}
			
			sb.append("</table>");
			sb.append("<br>");
		}
		
		sb.append("</body>");
		sb.append("</html>");
		
		writer.append(sb.toString());
	}
	
	private Map<String, List<Edge>> classifyEdges(Collection<Edge> edges){
		Map<String, List<Edge>> edgesByType = new HashMap<String, List<Edge>>();
		for (Edge edge : edges){
			List<Edge> edgeList = edgesByType.get(edge.getType());
			if (edgeList == null){
				edgeList = new ArrayList<Edge>();
				edgesByType.put(edge.getType(), edgeList);
			}
			edgeList.add(edge);
		}
		
		return edgesByType;
	}
	
	private Edge getEdge(List<Edge> edges, Node f, Node t){
		for (Edge edge : edges){
			if (edge.getFromNode().equals(f) && edge.getToNode().equals(t)) return edge;
		}
		return null;
	}
	
	private String embedNodeLink(Node node){
		Beans.init();
		GenericRO ro = (GenericRO)Beans.getBean("genericRO");
		String path = ro.getBaseURL();
		String url = path + "/vis_get_node_info.jsp?node=" + node.getId();
		return "<a href='" + url + "'>" + node.getLabel() + "</a>";
	}
	
	private String embedTeamLink(Team t){
		String url = "team." + t.getId() + ".html";
		return "<a href='" + url + "'>" + t.getId() + "</a>";
	}	
}
