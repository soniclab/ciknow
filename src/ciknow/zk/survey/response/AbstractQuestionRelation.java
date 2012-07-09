package ciknow.zk.survey.response;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.util.GeneralUtil;
import ciknow.zk.survey.design.AddNodeWindow;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.select.annotation.Listen;

/**
 *
 * @author gyao
 */
public class AbstractQuestionRelation extends AbstractQuestion {

    private static final long serialVersionUID = 3248791813585811704L;
    private static final Log logger = LogFactory.getLog(AbstractQuestionRelation.class);
    
    // state
    protected List<Node> rowNodes;
    protected List<Node> colNodes;
    protected List<Edge> questionEdges;
    protected List<Node> questionTags;
    protected List<Edge> edgesToSave = new ArrayList<Edge>();
    protected List<Edge> edgesToDelete = new ArrayList<Edge>();

    public AbstractQuestionRelation(Question currentQuestion) {
        super(currentQuestion);
    }

    public AbstractQuestionRelation(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();
    	
        // paging
        grid.setMold("paging");
        grid.setPageSize(currentQuestion.getRowPerPage());

        // get row nodes
        Set<Long> rowNodeIds = Question.getCombinedAvailableNodeIds(currentQuestion, respondent, false);
        rowNodes = nodeDao.findByIds(rowNodeIds);
        Collections.sort(rowNodes, new NodeLabelComparator());

        // get col nodes if needed
        if (currentQuestion.isPerceivedRelationalChoice() || currentQuestion.isPerceivedRelationalRating()) {
            Set<Long> colNodeIds = Question.getCombinedAvailableNodeIds(currentQuestion, respondent, true);
            colNodes = nodeDao.findByIds(colNodeIds);
            if (colNodes.isEmpty()) {
                colNodes = rowNodes;
            } else {
                Collections.sort(colNodes, new NodeLabelComparator());
            }
        }

        // get edges by question and respondent
        questionEdges = edgeDao.loadByQuestionAndNode(currentQuestion, respondent);

        // get tags from perceived choice/rating questions
        if (currentQuestion.isPerceivedChoice() || currentQuestion.isPerceivedRating()) {
            questionTags = nodeDao.findTagsByQuestion(currentQuestion);
        }
    }

    @Listen("onClick = #addNodeBtn")
    @Override
    public void addNode() {
        // save any existing answers because current page will be refresh
        if (!validate()) {
            return;
        }
        respondent.update(nodeDao.loadById(respondent.getId()));
        save();

        // open addNodeWindow
        AddNodeWindow win = new AddNodeWindow(this);
        win.setWidth((GeneralUtil.getDesktopWidth() - 100) + "px");
        win.setVflex("1");
        win.doModal();
    }
    
    protected int getFirstColumnWidth(){
    	int width = 150; // default and minimum width    	
    	if (rowNodes.isEmpty()) return width;
    	
    	int sum = 0;
    	int average = 0;

    	FontRenderContext frc = new FontRenderContext(new AffineTransform(),true,true);
    	Font font = new Font("Arial", Font.PLAIN, 12);
		for (Node node : rowNodes){
			sum += GeneralUtil.measureString(node.getLabel(), font, frc);
		}
		average = sum/rowNodes.size();    	    	
    	if (average > width) width = average;
    	logger.info("Get first column width: " + width + ", average: " + average);
    	
    	return width;
    }
}
