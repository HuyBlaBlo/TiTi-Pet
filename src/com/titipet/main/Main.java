
package com.titipet.main;

import com.formdev.flatlaf.FlatLightLaf;
import com.titipet.bus.AuthService;
import com.titipet.connectDB.ConnectDB;
import com.titipet.gui.LoginFrame;

import javax.swing.*;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();

        try {
            ConnectDB.getInstance().connect();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Không thể kết nối cơ sở dữ liệu!\n"
                            + e.getMessage(),
                    "Lỗi kết nối",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }
        SwingUtilities.invokeLater(() -> {
            AuthService authService = new AuthService();
            LoginFrame loginFrame = new LoginFrame(authService);

            loginFrame.setVisible(true);
        });
    }
}
