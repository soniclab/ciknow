package ciknow.zk.survey.response;

import ciknow.util.Constants;
import java.io.File;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 *
 * @author gyao
 */
public class ContactChooserItemRowRenderer implements ListitemRenderer<ContactChooserItem> {

    @Override
    public void render(Listitem item, ContactChooserItem data, int index) throws Exception {
        item.setValue(data);

        for (String name : data.getColumns()) {
            String value = data.getValue(name);

            Listcell cell = new Listcell();
            cell.setParent(item);
            if (name.equals(ContactChooserItem.IMAGE_KEY)) {
                Image img = new Image();
                img.setWidth("100px");
                img.setHeight("100px");

                WebApp app = WebApps.getCurrent();
                String realPath = (String) app.getAttribute(Constants.APP_REAL_PATH);
                File file = new File(realPath, value);

                if (!file.exists()) {
                    value = "images/photos/missing.jpg";
                }
                img.setSrc(value);
                img.setParent(cell);
            } else {
                cell.setLabel(value);
                if (data.isSelected()) {
                    cell.setStyle("font-weight:bold; color:navy");
                } else {
                    cell.setStyle("font-weight:normal; color:black");
                }
            }
        }
    }
}
