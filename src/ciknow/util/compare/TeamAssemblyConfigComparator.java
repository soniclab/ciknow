package ciknow.util.compare;

import java.util.Comparator;

import org.dom4j.Element;

public class TeamAssemblyConfigComparator implements Comparator<Element>{
	public int compare(Element e1, Element e2) {
		double w1 = Double.parseDouble(e1.attributeValue("weight"));
		double w2 = Double.parseDouble(e2.attributeValue("weight"));
		if (w1 < w2)
			return 1;
		else if (w1 > w2)
			return -1;
		else
			return 0;
	}
}
