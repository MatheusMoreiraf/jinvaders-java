package org.opensourcearcade.jinvaders;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    public static void main(String[] args) {
        try {
            // fix the JNLP desktop icon exec rights bug (Linux only)
            if (System.getProperty("os.name").toLowerCase().indexOf("linux") != -1) {
                java.io.File desktop = new java.io.File(System.getProperty("user.home") + "/Desktop");
                if (desktop.exists()) {
                    // TODO filter files for "jinvaders" and ".desktop" extension
                    java.io.File[] files = desktop.listFiles();
                    for (java.io.File file : files) {
                        if (file.getName().contains("jws_app_shortcut_")) {
                            file.setExecutable(true, false);
                        }
                    }
                }
            }

            Game game = new Game();
            String name = ToolBox.getPackageName();
            Image iconImg = ToolBox.loadImage(ToolBox.getURL(name + ".png"));

            Frame frame = new Frame(name);
            frame.setIconImage(iconImg);
            frame.setLayout(new BorderLayout());
            frame.add(game.getPanel(), BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            frame.setVisible(true);

            game.init();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }
}
