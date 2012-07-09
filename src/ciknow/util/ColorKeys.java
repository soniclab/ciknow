package ciknow.util;

/**
 * Created by IntelliJ IDEA.
 * User: Li
 * Date: Feb 12, 2008
 * Time: 9:31:45 AM
 * To change this template use File | Settings | File Templates.
 */

import java.awt.*;
import java.util.List;
import java.util.*;
import javax.swing.*;


class ColorKeys {
    Random seed = new Random();
    List colors = new ArrayList();
    int inc = 51;  // 216 unique colors

    public Color getKeyColor() {
        while(true) {
            Color color = getColor();
            if(!colors.contains(color)) {
                colors.add(color);
                return color;
            }
        }
    }

    private Color getColor() {
        int[] n = new int[3];
        for(int j = 0; j < 3; j++) {
            n[j] = seed.nextInt(6);
        }
        return new Color(n[0]*inc, n[1]*inc, n[2]*inc);
    }
}