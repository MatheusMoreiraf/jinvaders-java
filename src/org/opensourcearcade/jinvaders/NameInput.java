package org.opensourcearcade.jinvaders;

import java.awt.*;

public class NameInput {
    private static final String STR_INPUTNAME = "INPUT YOUR NAME";

    public static void draw(Graphics g, FontMetrics metrics, int width, int height, String tmpPlayerName, int inputCaretPos) {
        int h = metrics.getHeight();
        int strW = metrics.stringWidth(STR_INPUTNAME);
        g.drawString(STR_INPUTNAME, width / 2 - strW / 2, h * 7);

        int strLen = tmpPlayerName.length();
        int charW = metrics.stringWidth("M") + 1;

        Help.inputName(width, height, charW, strLen, h, tmpPlayerName, g, inputCaretPos);
    }
}
