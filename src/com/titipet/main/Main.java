package com.titipet.main;

import com.formdev.flatlaf.FlatLightLaf;
import com.titipet.gui.SplashFrame;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            SplashFrame splashFrame = new SplashFrame();
            splashFrame.setVisible(true);
            splashFrame.startLoading();
        });
    }
}
