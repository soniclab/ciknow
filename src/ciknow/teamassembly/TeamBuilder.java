package ciknow.teamassembly;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.io.TeamWriter;
import ciknow.util.Beans;
import ciknow.util.GeneralUtil;

public class TeamBuilder {
	private static Log logger = LogFactory.getLog(TeamBuilder.class);
	
	public static final String TEAM_KEY = "team";
	public static final String STRATEGY_MAX_MIN_SCORE = "maxMinScore";
	public static final String STRATEGY_MIN_VARIANCE = "minVariance";
	private int numTeams;
	private int minTeamSize;
	private int maxTeamSize;
	private String diversityQuestionShortName;
	private List<String> hobbies;
	private List<Node> nodes;
	private List<Edge> edges;
	@SuppressWarnings("unchecked")
	private Map params;
	
	private List<Team> teams;
	private double minScore;
	private double variance;
	
	private List<Team> bestByMaxMinScore = null;
	private List<Team> bestByMinVariance = null;
	private int bestItr4MaxMinScore = 0;
	private int bestItr4MinVariance = 0;
	private double maxMinScore = Double.NEGATIVE_INFINITY;
	private double minVariance = Double.MAX_VALUE;
	
	public static void main(String[] args) throws Exception{	
		String diversityQuestionShortName = "role";
		List<String> hobbies = new LinkedList<String>();
		hobbies.add("F`Gender`Female");
		hobbies.add("F`Gender`Male");
		Beans.init();
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		EdgeDao edgeDao = (EdgeDao)Beans.getBean("edgeDao");
		List<Node> nodes = nodeDao.loadByType("user");
		List<Edge> edges = edgeDao.loadByType("MetinOCMC", false);
		TeamBuilder tb = new TeamBuilder(10, 11, 12, diversityQuestionShortName, hobbies, nodes, edges);
		logger.debug(tb);
		tb.build(1);
		TeamWriter tw = new TeamWriter();
		PrintWriter pw = null;
		for (Team team : tb.getTeams()){
			pw = new PrintWriter(new FileWriter("results/team." + team.getId() + ".html"));
			tw.writeTeam(team, pw);
			pw.close();
			logger.debug(team);
		}
		
		pw = new PrintWriter(new FileWriter("results/summary.html"));
		tw.writeSummary(tb, pw);
		pw.close();
	}
	
	@SuppressWarnings("unchecked")
	public TeamBuilder(int numTeams, int min, int max, 
						String diversityQuestionShortName, List<String> hobbies, 
						List<Node> nodes, List<Edge> edges) throws Exception{		
		this.numTeams = numTeams;
		this.minTeamSize = min;
		this.maxTeamSize = max;
		this.diversityQuestionShortName = diversityQuestionShortName;
		this.hobbies = hobbies;
		this.nodes = nodes;
		this.edges = edges;
		
		if (numTeams*min > nodes.size() || numTeams*max < nodes.size()) {
			String msg = "Number of Nodes = " + nodes.size() + "\n";
			msg += "Number of Teams = " + numTeams + "\n";
			msg += "Minimum Team Size = " + minTeamSize + "\n";
			msg += "Maximum Team Size = " + maxTeamSize + "\n";
			msg += "It is logically not possible to have number of teams based on the min/max team size. \n";
			msg += "Requirement: min_team_size x num_team <= num_nodes <= max_team_size x num_team";
			logger.error(msg);
			throw new Exception(msg);
		}
		
		if (hobbies == null || hobbies.size() == 0) {
			String msg = "There are no available hobbies set";
			logger.error(msg);
			throw new Exception(msg);
		}
		
		params = GeneralUtil.getTeamAssemblyConfig(diversityQuestionShortName);
		List<Map> skills = (List<Map>)params.get("skills");
		if (skills == null || skills.size() == 0){
			logger.info("creating default diversity weights...");
			Beans.init();
			QuestionDao questionDao = (QuestionDao)Beans.getBean("questionDao");
			Question question = questionDao.findByShortName(diversityQuestionShortName);
			skills = new LinkedList<Map>();
			Double weight = 1.0/question.getFields().size();
			for (Field field : question.getFields()){				
				Map skill = new HashMap();
				skill.put("qShortName", diversityQuestionShortName);
				skill.put("sequenceNumber", field.getIndex()+"");
				skill.put("name", question.makeFieldKey(field));
				skill.put("label", field.getLabel());
				skill.put("weight", weight.toString());
				
				skills.add(skill);
			}
			params.put("skills", skills);
		}
	}
	
	private void clearTeamFromNodes(){
		for (Node node : nodes){
			node.getAttributes().remove(TEAM_KEY);
		}
	}
	
//	private void setTeamToNodes(){
//		for (Team team : teams){
//			for (Node member : team.getMembers()){
//				member.setAttribute(TEAM_KEY, Integer.toString(team.getId()));
//			}
//		}
//	}
	
	private void calculateTeamsStat(){
		DoubleArrayList list = new DoubleArrayList(numTeams);
		for (Team team : teams){
			list.add(team.getScore());
		}
		minScore = Descriptive.min(list);
		variance = Descriptive.variance(numTeams, Descriptive.sum(list), Descriptive.sumOfSquares(list));
	}
	
	public void build(int itr){
		for (int i = 0; i < itr; i++){
			logger.info("---------------------- ITERATION " + (i+1) + " ----------------------");
			clearTeamFromNodes();
			build();
			calculateTeamsStat();
			
			if (minScore > maxMinScore) {
				maxMinScore = minScore;
				bestByMaxMinScore = teams;
				bestItr4MaxMinScore = i;
			}
			logger.debug("minScore: " + minScore);
			if (variance < minVariance){
				minVariance = variance;
				bestByMinVariance = teams;
				bestItr4MinVariance = i;
			}
			logger.debug("variance: " + variance);
		}
		
		logger.info("bestItr4MaxMinScore: " + (bestItr4MaxMinScore + 1));
		logger.info("maxMinScore: " + maxMinScore);
		logger.info("bestItr4MinVariance: " + (bestItr4MinVariance + 1));
		logger.info("minVariance: " + minVariance);
	}
	
	@SuppressWarnings("unchecked")
	private List<Team> build(){
		teams = new LinkedList<Team>();
		for (int i=0; i < numTeams; i++){
			teams.add(new Team(this, i+1));
		}
		
		logger.info("assigning node with skills...");
		// skills must has been sorted in descending order
		List<Map> skills = (List<Map>)params.get("skills"); 
		for (Map skill : skills){
			String name = (String) skill.get("name");			
			double weight = Double.parseDouble((String)skill.get("weight"));
			if (weight <= 0) continue;
			logger.debug("skill: " + name);
			List<Node> nodesBySkill = getNodesBySkill(name, false);
			for (Node newMember : nodesBySkill){
				addToTeam(newMember);
			}
		}
		
		logger.info("assigning node without skills...");
		for (Node newMember : nodes){
			if (newMember.getAttribute(TEAM_KEY) == null){
				addToTeam(newMember);
			} else {
				//logger.debug(newMember.getLabel() + " has been added to team: " + newMember.getAttribute(TEAM_KEY));
			}
		}
		
		logger.info("rebalancing...");
		for (Team team : teams){
			while (team.getSize() < minTeamSize){
				List<Node> removableMembers = getRemovableMembers();
				
				double maxGain = Double.NEGATIVE_INFINITY;
				List<Double> gains = new LinkedList<Double>();
				for (Node removableMember : removableMembers){
					if (removableMember == null) {
						gains.add(Double.NEGATIVE_INFINITY);
						continue;
					}
					double gain = team.getGain(removableMember);
					gains.add(gain);
					if (gain > maxGain) maxGain = gain;
				}
				
				if (Math.abs(maxGain - Double.NEGATIVE_INFINITY) < 0.0000001) {
					logger.warn("The min/max team size cannot be achieved for Team: " + team.getId());
					break;
				}
				
				Node member = getMaxGainMaxScoreTeamMember(removableMembers, gains, maxGain);
				Team oldTeam = getTeamByMember(member);

//				int index = getRandomIndex(gains, maxGain);
//				Node member = removableMembers.get(index);
//				Team oldTeam = getTeamById(teams, Integer.parseInt(member.getAttribute(TEAM_KEY)));
				
				oldTeam.removeMember(member);
				team.addMember(member);
			}
		}
		
		logger.info("populate data...");
		for (Team team : teams){
			team.calculateScore();
		}
		
		return teams;
	}
	
	
	private List<Node> getRemovableMembers(){
		List<Node> removableMembers = new LinkedList<Node>();		
		for (Team team : teams){
			Node member = team.getRemovableMember();
			removableMembers.add(member);
		}
		return removableMembers;
	}
	
	private void addToTeam(Node newMember){
		logger.info("Node: " + newMember.getLabel());
		double maxGain = Double.NEGATIVE_INFINITY;

		List<Double> gains = new LinkedList<Double>();		
		for (Team team : teams){
			if (team.getSize() >= maxTeamSize) {
				gains.add(Double.NEGATIVE_INFINITY);
				continue;
			}
			double gain = team.getGain(newMember);
			gains.add(gain);
			if (gain > maxGain) {
				maxGain = gain;
			}
		}		

		if (Math.abs(maxGain - Double.NEGATIVE_INFINITY) < 0.0000001) {
			logger.warn("The min/max team size cannot be achieved when adding node: " + newMember.getLabel());
			return;
		}
		
//		int index = getRandomIndex(gains, maxGain);
//		Team maxGainTeam = teams.get(index);
//		maxGainTeam.addMember(newMember);
		getMaxGainMinScoreTeam(teams, gains, maxGain).addMember(newMember);
	}
	
	private Team getMaxGainMinScoreTeam(List<Team> teams, List<Double> gains, double maxGain){
		logger.debug("gains: " + gains);
		List<Integer> indexes = new LinkedList<Integer>();
		int i = 0;
		for (Double gain : gains){
			if (Math.abs(gain - maxGain) < 0.000000001){
				indexes.add(i);
			}
			i++;
		}
		
		if (indexes.size() == 0) {
			logger.warn("Cannot find the index with maxGain=" + maxGain);
			return null;
		}
		else if (indexes.size() == 1) return teams.get(indexes.get(0));
		else {
			List<Double> scores = new LinkedList<Double>();
			double minScore = Double.MAX_VALUE;
			for (int index : indexes){
				double score = teams.get(index).getScore();
				scores.add(score);
				if (minScore > score) minScore = score;
			}
			int indexIndex = getRandomIndex(scores, minScore);
			Team t = teams.get(indexes.get(indexIndex));
			logger.debug("maxGain indexes: " + indexes);
			logger.debug("scores: " + scores);
			//logger.debug("maxGainMinScoreTeam: " + t.getId());
			return t;
		}
	}

	private Node getMaxGainMaxScoreTeamMember(List<Node> members, List<Double> gains, double maxGain){
		logger.debug("gains: " + gains);
		List<Integer> indexes = new LinkedList<Integer>();
		int i = 0;
		for (Double gain : gains){
			if (Math.abs(gain - maxGain) < 0.000000001){
				indexes.add(i);
			}
			i++;
		}
		
		if (indexes.size() == 0) {
			logger.warn("Cannot find the index with maxGain=" + maxGain);
			return null;
		}
		else if (indexes.size() == 1) return members.get(indexes.get(0));
		else {
			List<Double> scores = new LinkedList<Double>();
			double maxScore = Double.NEGATIVE_INFINITY;
			for (int index : indexes){
				Node member = members.get(index);
				Team team = getTeamByMember(member);
				double score = team.getScore();
				scores.add(score);
				if (maxScore < score) maxScore = score;
			}
			int indexIndex = getRandomIndex(scores, maxScore);
			Node m = members.get(indexes.get(indexIndex));
			logger.debug("maxGain indexes: " + indexes);
			logger.debug("scores: " + scores);
			//logger.debug("maxGainMaxScoreTeamMember: " + m.getLabel());			
			return m;
		}
	}
	
	public static int getRandomIndex(List<Double> items, double item){
		List<Integer> indexes = new LinkedList<Integer>();
		int i = 0;
		for (Double d : items){
			if (Math.abs(d - item) < 0.000000001){
				indexes.add(i);
			}
			i++;
		}
		
		if (indexes.size() == 0) return -1;
		else {
			Random r = new Random();
			return indexes.get(r.nextInt(indexes.size()));
		}
	}
	
	private List<Node> getNodesBySkill(String skill, boolean includeTeamed){
		logger.debug("get nodes by skill: " + skill + ", includeTeamed: " + includeTeamed);
		List<Node> ns = new LinkedList<Node>();
		for (Node node : nodes){
			if (node.getAttribute(skill) == null) continue;
			if (!includeTeamed && node.getAttribute(TEAM_KEY) != null) continue;
			else ns.add(node);
		}
		logger.debug(ns.size() + " nodes retrieved.");
		
		return ns;
	}
	
	public List<Edge> getEdgesAmongNodes(List<Node> nodes){
		List<Edge> es = new LinkedList<Edge>();
		for (Edge edge : edges){
			if (nodes.contains(edge.getFromNode()) && nodes.contains(edge.getToNode())){
				es.add(edge);
			}
		}
		return es;
	}
	
	public Team getTeamById(int id){
		for (Team team : teams){
			if (team.getId() == id) return team;
		}
		return null;
	}
	
	public Team getTeamByMember(Node member){
		String idString = member.getAttribute(TEAM_KEY);
		if (idString == null || idString.length() == 0) return null;
		int id = Integer.parseInt(idString);
		return getTeamById(id);
	}
	
	public Node getNodeById(Long nodeId){
		for (Node node : this.nodes){
			if (node.getId().equals(nodeId)) return node;
		}
		return null;
	}
	
	public int getNumTeams() {
		return numTeams;
	}
	public void setNumTeams(int numTeams) {
		this.numTeams = numTeams;
	}
	public int getMinTeamSize() {
		return minTeamSize;
	}
	public void setMinTeamSize(int minTeamSize) {
		this.minTeamSize = minTeamSize;
	}
	public int getMaxTeamSize() {
		return maxTeamSize;
	}
	public void setMaxTeamSize(int maxTeamSize) {
		this.maxTeamSize = maxTeamSize;
	}
	
	public String getDiversityQuestionShortName() {
		return diversityQuestionShortName;
	}

	public void setDiversityQuestionShortName(String diversityQuestionShortName) {
		this.diversityQuestionShortName = diversityQuestionShortName;
	}

	public List<String> getHobbies() {
		return hobbies;
	}
	public void setHobbies(List<String> hobbies) {
		this.hobbies = hobbies;
	}
	public List<Node> getNodes() {
		return nodes;
	}
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	public List<Edge> getEdges() {
		return edges;
	}
	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
	
	public List<Team> getBestByMaxMinScore() {
		return bestByMaxMinScore;
	}

	public void setBestByMaxMinScore(List<Team> bestByMaxMinScore) {
		this.bestByMaxMinScore = bestByMaxMinScore;
	}

	public List<Team> getBestByMinVariance() {
		return bestByMinVariance;
	}

	public void setBestByMinVariance(List<Team> bestByMinVariance) {
		this.bestByMinVariance = bestByMinVariance;
	}

	public int getBestItr4MaxMinScore() {
		return bestItr4MaxMinScore;
	}

	public void setBestItr4MaxMinScore(int bestItr4MaxMinScore) {
		this.bestItr4MaxMinScore = bestItr4MaxMinScore;
	}

	public int getBestItr4MinVariance() {
		return bestItr4MinVariance;
	}

	public void setBestItr4MinVariance(int bestItr4MinVariance) {
		this.bestItr4MinVariance = bestItr4MinVariance;
	}

	public double getMaxMinScore() {
		return maxMinScore;
	}

	public void setMaxMinScore(double maxMinScore) {
		this.maxMinScore = maxMinScore;
	}

	public double getMinVariance() {
		return minVariance;
	}

	public void setMinVariance(double minVariance) {
		this.minVariance = minVariance;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}
	
	@SuppressWarnings("unchecked")
	public Map getParams() {
		return params;
	}

	@SuppressWarnings("unchecked")
	public void setParams(Map params) {
		this.params = params;
	}

	public Set<String> getSkillNames(){
		List<Map> skills = (List<Map>)params.get("skills");
		Set<String> skillNameSet = new HashSet<String>();
		for (Map skill : skills){			
			skillNameSet.add((String) skill.get("name"));
		}
		return skillNameSet;
	}	

	public static Collection<Node> getMembersWithoutAttributes(Collection<Node> members, Collection<String> attrNames){
		Set<Node> boringMembers = new HashSet<Node>();
		for (Node member : members){
			Set<String> temp = new HashSet<String>(attrNames);
			temp.retainAll(member.getAttributes().keySet());
			if (temp.size() == 0) boringMembers.add(member);
		}	
		return boringMembers;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("TeamBuilder--------------\n");
		sb.append("numTeams: " + numTeams).append("\n");
		sb.append("minTeamSize: " + minTeamSize).append("\n");
		sb.append("maxTeamSize: " + maxTeamSize).append("\n");
		sb.append("diversityQuestionShortName: " + diversityQuestionShortName).append("\n");
		sb.append("hobbies: " + hobbies).append("\n");
		sb.append("number of nodes: " + nodes.size()).append("\n");
		sb.append("number of edges: " + edges.size()).append("\n");
		sb.append("TeamBuilder--------------\n");
		return sb.toString();
	}
}
