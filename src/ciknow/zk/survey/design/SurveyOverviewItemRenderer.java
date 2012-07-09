package ciknow.zk.survey.design;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

import ciknow.domain.Page;
import ciknow.domain.Question;

@SuppressWarnings("rawtypes")
public class SurveyOverviewItemRenderer implements TreeitemRenderer {
	private static Log logger = LogFactory.getLog(SurveyOverviewItemRenderer.class);
	
	private static final SurveyItemListener listener = new SurveyItemListener();
	
	@Override
	public void render(Treeitem item, Object data, int index) throws Exception {
		DefaultTreeNode treeNode = (DefaultTreeNode) data;
		item.setValue(treeNode);
		item.setDraggable("item");
		item.setDroppable("item");
		item.addEventListener("onDrop", listener);
		item.setOpen(true);
		
		// for update treeNode data
		Treerow tr = item.getTreerow();
		if (tr == null){
			tr = new Treerow();
			tr.setParent(item);
		} else {
			tr.getChildren().clear();
		}
		
		// render
		if (treeNode.isLeaf()){
			Question question = (Question)treeNode.getData();
			tr.appendChild(new Treecell(question.getLabel()));
			tr.appendChild(new Treecell(question.getType()));
		} else {
			Page page = (Page)treeNode.getData();
			tr.appendChild(new Treecell(page.getLabel()));
			tr.appendChild(new Treecell("+++++++++++++++++++++++++++"));
		}
	}

	private static class SurveyItemListener implements EventListener<DropEvent>{

		@SuppressWarnings("unchecked")
		@Override
		public void onEvent(DropEvent event) throws Exception {
			Treeitem draggedItem = (Treeitem) event.getDragged();
			DefaultTreeNode draggedNode = (DefaultTreeNode) draggedItem.getValue();
			Treeitem droppedItem = (Treeitem) event.getTarget();
			DefaultTreeNode droppedNode = (DefaultTreeNode) droppedItem.getValue();
			
			//int draggedIndex = 
			DefaultTreeNode droppedParentNode = (DefaultTreeNode)droppedNode.getParent();
			if (draggedNode.isLeaf()){ 		// dragged a question
				if (droppedNode.isLeaf()){ 	// dropped to question
					Question draggedQuestion = (Question) draggedNode.getData();
					Question droppedQuestion = (Question) droppedNode.getData();
					logger.debug("drag question: " + draggedQuestion.getLabel());
					logger.debug("drop question: " + droppedQuestion.getLabel());
					
					droppedParentNode.insert(draggedNode, droppedParentNode.getIndex(droppedNode));				
				} else {					// dropped to page
					Question draggedQuestion = (Question) draggedNode.getData();
					Page droppedPage = (Page) droppedNode.getData();
					logger.debug("drag question: " + draggedQuestion.getLabel());
					logger.debug("drop page: " + droppedPage.getLabel());
					
					droppedNode.add(draggedNode);					
				}
			} else {						// dragged a page
				if (droppedNode.isLeaf()){	// dropped to question
					Page draggedPage = (Page) draggedNode.getData();
					Question droppedQuestion = (Question) droppedNode.getData();
					logger.debug("drag page: " + draggedPage.getLabel());
					logger.debug("drop question: " + droppedQuestion.getLabel());
					
					Messagebox.show("Cannot insert page into questions.");
				} else {					// dropped to page
					Page draggedPage = (Page) draggedNode.getData();
					Page droppedPage = (Page) droppedNode.getData();
					logger.debug("drag page: " + draggedPage.getLabel());
					logger.debug("drop page: " + droppedPage.getLabel());
					
					droppedParentNode.insert(draggedNode, droppedParentNode.getIndex(droppedNode));
				}
			}
		}
		
	}
}
