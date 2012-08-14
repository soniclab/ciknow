package ciknow.zk.controller;

import java.io.BufferedReader;

import ciknow.dao.NodeDao;
import ciknow.dao.SurveyDao;
import ciknow.io.GroupAttributeReader;
import ciknow.io.NodeAttributeReader;
import ciknow.util.GeneralUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.ClientInfoEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class IOController extends SelectorComposer<Component> {

    private static final long serialVersionUID = -9135576399887656457L;
    private static Log logger = LogFactory.getLog(IOController.class);
    
    // variables
    @WireVariable
    private NodeDao nodeDao;
    @WireVariable
    private SurveyDao surveyDao;
    @WireVariable
    private NodeAttributeReader nodeAttributeReader;
    @WireVariable
    private GroupAttributeReader groupAttributeReader;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        populate();
    }

    @Listen("onClientInfo = #stage")
    public void onClientInfo$stage(ClientInfoEvent evt) {
        Integer width = evt.getDesktopWidth();
        Integer height = evt.getDesktopHeight();
        logger.debug("screen.width: " + evt.getScreenWidth());
        logger.debug("screen.height: " + evt.getScreenHeight());
        logger.debug("desktop.width: " + width);
        logger.debug("desktop.height: " + height);


        Integer currentWidth = GeneralUtil.getDesktopWidth();
        Integer currentHeight = GeneralUtil.getDesktopHeight();

        // first hit
        if (currentWidth == null || currentHeight == null) {
            logger.debug("First Hit");
            GeneralUtil.setDesktopWidth(width);
            GeneralUtil.setDesktopHeight(height);
            //Executions.sendRedirect(null);
            return;
        }

        // browser resize
        if (!width.equals(currentWidth) || !height.equals(currentHeight)) {
            logger.debug("Browser Resize");
            GeneralUtil.setDesktopWidth(width);
            GeneralUtil.setDesktopHeight(height);
            //Executions.sendRedirect(null);
        }
    }
    
	private void populate() {
		logger.info("Creating Import/Export Interface ...");

    }

	@Listen("onUpload = #nodeAttrImportBtn")
	public void importNodeAttributes(UploadEvent event){
		Media media = event.getMedia();
		try {
			nodeAttributeReader.read(new BufferedReader(media.getReaderData()));
			Messagebox.show("Success!");
		} catch (Exception e) {
			Messagebox.show(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Listen("onUpload = #groupAttrImportBtn")
	public void importGroupAttributes(UploadEvent event){
		Media media = event.getMedia();
		try {
			groupAttributeReader.read(new BufferedReader(media.getReaderData()));
			Messagebox.show("Success!");
		} catch (Exception e) {
			Messagebox.show(e.getMessage());
			e.printStackTrace();
		}
	}
}
