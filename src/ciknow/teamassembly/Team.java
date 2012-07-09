package ciknow.teamassembly;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.domain.Edge;
import ciknow.domain.Node;

public class Team {
	private static Log logger = LogFactory.getLog(Team.class);
	private TeamBuilder tb;
	private int id;
	private List<Node> members = new LinkedList<Node>();
	private double score = 0.0;
	private double diversity = 0.0;
	private double similarity = 0.0;
	private double density = 0.0;
	
	public Team(TeamBuilder tb, int id){
		this.tb = tb;
		this.id = id;
		members = new LinkedList<Node>();
		score = 0.0;
	}
	
	public Team(TeamBuilder tb, String id, String[] memberIds){
		this.tb = tb;
		this.id = Integer.parseInt(id);
    	for (String memberId : memberIds){
    		long nodeId = Long.parseLong(memberId);
    		Node member = tb.getNodeById(nodeId);
    		if (member == null){
    			logger.error("ERROR: CANNOT IDENTIFY NODE (ID=" + nodeId);
    			continue;
    		}
    		members.add(member);
    		member.setAttribute(TeamBuilder.TEAM_KEY, id);
    	}
    	score = calculateScore();
	}
	
	public double getGain(Node newMember){
		members.add(newMember);
		double gain = calculateScore() - score;		
		members.remove(newMember);		
		//return Double.parseDouble(nf.format(gain));
		return gain;
	}
	
	public double getLoss(Node removeMember){
		members.remove(removeMember);
		double loss = score - calculateScore();
		members.add(removeMember);
		//return Double.parseDouble(nf.format(loss));
		return loss;
	}
	
	public double calculateScore(){
		// diversity
		double w = Double.parseDouble((String)tb.getParams().get("diversity"));
		double s = 0.0;
		diversity = calculateDiversity();		
		s += diversity * w;
		
		// size
		//s += members.size();
		
		// hobby (similarity)
		w = Double.parseDouble((String)tb.getParams().get("similarity"));
		similarity = calculateSimilarity(tb.getHobbies());
		s += similarity * w;
		
		// density
		w = Double.parseDouble((String)tb.getParams().get("density"));
		density = calculateDensity();
		s += density * w;
		
		return s;
	}

	@SuppressWarnings("unchecked")
	private double calculateDiversity() {
		double s = 0.0;
		List<Map> skills = (List<Map>)tb.getParams().get("skills");
		for (Map skill : skills){
			String name = (String) skill.get("name");
			double weight = Double.parseDouble((String)skill.get("weight"));
			if (weight <= 0) continue;
			for (Node member : members){
				if (member.getAttribute(name) != null){
					s += weight;
					break;
				}
			}
		}
		return s;
	}
	
	public Node getRemovableMember(){
		Node removable = null;
		if (getSize() <= tb.getMinTeamSize()) return removable;
		
		List<Double> losses = new LinkedList<Double>();
		double minLoss = Double.MAX_VALUE;
		for (int i = 0; i < members.size(); i++){
			Node member = members.get(i);
			double loss = getLoss(member);
			losses.add(loss);
			if (minLoss > loss) minLoss = loss;
		}
		
		int index = TeamBuilder.getRandomIndex(losses, minLoss);
		return members.get(index);
	}
	
	public int getSize(){
		return getMembers().size();
	}
	
	private double calculateSimilarity(List<String> hobbies){
		double score = 0.0;
		
		// max (members of a hobby)
//		for (String hobby : hobbies){
//			int count = 0;
//			for (Node member : members){
//				if (member.getAttribute(hobby) != null) count++;
//			}
//			if (count > score) score = count;
//		}
		
		// yun's 
//		Set<String> livingHobbies = new HashSet<String>();
//		for (Node member : members){
//			Set<String> iHobbies = new HashSet<String>(hobbies);
//			iHobbies.retainAll(member.getAttributes().keySet());
//			livingHobbies.addAll(iHobbies);			
//		}
//		logger.debug("all hobbies: " + hobbies.size() + ", living hobbies: " + livingHobbies.size());
//		if (livingHobbies.size() == 0) return score;
//		
//		int count = 0;
//		for (Node member : members){			
//			Set<String> iHobbies = new HashSet<String>(livingHobbies);
//			iHobbies.retainAll(member.getAttributes().keySet());
//			if (iHobbies.size() == 0) continue;
//			
//			double pi = iHobbies.size()/livingHobbies.size();
//			logger.debug("member: " + member.getLabel() + ", iHobbies: " + iHobbies.size() + ", pi: " + pi);
//			score += pi * pi;
//			count++;
//		}
//		score = score/count;;
		
		// http://en.wikipedia.org/wiki/Diversity_index
		// assumption: 
		// 1) ignore nodes without hobbies; 
		// 2) allow multiple choices, e.g. result is also divided by number of hobbies 
		//    in order to normalized b.w 0 ~ 1. This does not make much sense in practice :(
		Collection<Node> boringMembers = TeamBuilder.getMembersWithoutAttributes(members, hobbies);		
		List<Node> activeMembers = new LinkedList<Node>(members);
		activeMembers.removeAll(boringMembers);
		if (activeMembers.size() == 0) return score;
		
		for (String hobby : hobbies){
			int count = 0;
			for (Node member : activeMembers){
				if (member.getAttribute(hobby) != null) count++;
			}
			double pi = count*1.0/activeMembers.size();
			score += pi*pi;
		}
		score = score/hobbies.size();
		
		return score;
	}
	
	private double calculateDensity(){
		double score = 0.0;
		if (members.size() > 0){
			List<Edge> edges = tb.getEdgesAmongNodes(members);
			
			int numType = 0;
			Map<String, String> typeMap = new HashMap<String, String>();
			for (Edge edge : edges){
				String type = edge.getType();
				if (typeMap.containsKey(type)) continue;
				typeMap.put(type, type);
				numType++;
			}
			
			int size = getSize();
			double total = size > 1?size*(size-1):1;
			
			score = (numType == 0 ? 0 : (edges.size()/numType)/total);
			//logger.debug("team: " + id + ", teamSize: " + size +", numEdges: " + edges.size() + ", density: " + score);
		}
		return score;
	}
	

	public void addMember(Node member){
		members.add(member);
		score = calculateScore();
		member.setAttribute(TeamBuilder.TEAM_KEY, Integer.toString(id));
		logger.debug("Node " + member.getLabel() + " is added to team: " + id);
	}
	
	public void removeMember(Node member){
		members.remove(member);
		score = calculateScore();
		member.getAttributes().remove(TeamBuilder.TEAM_KEY);
		logger.debug("Node " + member.getLabel() + " is removed from team: " + id);
	}	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Node> getMembers() {
		return members;
	}
	public void setMembers(List<Node> members) {
		this.members = members;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	public double getDiversity() {
		return diversity;
	}

	public void setDiversity(double diversity) {
		this.diversity = diversity;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public void setDensity(double density) {
		this.density = density;
	}	

	public double getDensity() {
		return density;
	}

	public TeamBuilder getTb() {
		return tb;
	}

	public void setTb(TeamBuilder tb) {
		this.tb = tb;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Team: " + id + 
				", size: " + getSize() + 
				", score:" + score + 
				", diversity: " + diversity + 
				", similarity: " + similarity + 
				", density: " + density +				
				", members: [");
		
		for (int i=0; i < members.size(); i++){
			Node member = members.get(i);
			if (i > 0) sb.append(", ");
			sb.append(member.getLabel());
		}
		sb.append("]");
		return sb.toString();
	}
}
