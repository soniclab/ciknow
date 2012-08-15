package ciknow.zk.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Messagebox;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DataController extends BaseController {

    private static final long serialVersionUID = -5135576399887656457L;
    private static Log logger = LogFactory.getLog(DataController.class);
    
    // variables

    @Override
	public void init() {
    	super.init();
    	disableButton("data");
    	
		logger.info("Creating Data Management Interface ...");
    }

    @Listen("onClick = #testBtn")
    public void test(){
    	Messagebox.show("To be implemented.");
    }
}
