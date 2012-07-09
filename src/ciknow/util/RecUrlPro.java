package ciknow.util;

import ciknow.domain.Node;

/**
 * Created by IntelliJ IDEA.
 * User: jinling
 * Date: Mar 20, 2008
 * Time: 11:32:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class RecUrlPro {

   public static String getUrl(Node node){

            String type =    node.getType();
            String url = "http://www.nanohub.org/";
            if(type.equalsIgnoreCase(Constants.NODE_TYPE_USER)){
              url = url + "contributors/" + node.getUri();
            }
            else if (type.equalsIgnoreCase(Constants.NODE_TYPE_TAG)){
              url = url + "resources/tags/" + node.getLabel();
            }else{
              url = url + "resources/" + node.getUri();
            }
            return url;
         }
}
