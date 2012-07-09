package ciknow.util;

import ciknow.dao.GroupDao;
import ciknow.dao.RoleDao;
import ciknow.domain.Group;
import ciknow.domain.Node;

public class NodeUtil {
	public static Node createTag(String name, Group tagGroup){
		Beans.init();
		RoleDao roleDao = (RoleDao)Beans.getBean("roleDao");
		GroupDao groupDao = (GroupDao)Beans.getBean("groupDao");

		Node tag = new Node();
		tag.setType(Constants.NODE_TYPE_TAG);
		tag.setLabel(name);
		tag.setUsername(name);
		tag.getGroups().add(groupDao.getProxy(1L)); 	// GROUP_ALL
		tag.getGroups().add(tagGroup);					// tag group corresponding to node type "tag"
		tag.getRoles().add(roleDao.getProxy(3L));		// ROLE_USER
		tag.setEnabled(false);		
		
		return tag;
	}
}
