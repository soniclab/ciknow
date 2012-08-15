package ciknow.zk.controller;

import java.io.BufferedReader;
import ciknow.io.GroupAttributeReader;
import ciknow.io.NodeAttributeReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class IOController extends BaseController {

    private static final long serialVersionUID = -9135576399887656457L;
    private static Log logger = LogFactory.getLog(IOController.class);
    
    // variables
    @WireVariable
    private NodeAttributeReader nodeAttributeReader;
    @WireVariable
    private GroupAttributeReader groupAttributeReader;
    
    @Override
	public void init() {
    	super.init();
    	disableButton("io");
    	
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
