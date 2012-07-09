package ciknow.zk.survey.response;

import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 *
 * @author gyao
 */
public class ContactChooserItemLabelRenderer implements ListitemRenderer<ContactChooserItem> {

    @Override
    public void render(Listitem item, ContactChooserItem data, int index) throws Exception {
        new Listcell(data.getContact().getLabel()).setParent(item);
    }
}
