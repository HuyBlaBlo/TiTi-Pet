
package com.titipet.main;

import com.formdev.flatlaf.FlatLightLaf;
import com.titipet.connectDB.ConnectDB;
import com.titipet.gui.panel.DoiTraPanel;

import javax.swing.*;
import java.awt.*;

public class DoiTraPreviewMain {

    public static void main(String[] args) {
        FlatLightLaf.setup();
String maNV = System.getenv("TITIPET_MA_NV");

if (maNV == null || maNV.isBlank()) {
    JOptionPane.showMessageDialog(
            null,
            "Chưa thiết lập TITIPET_MA_NV."
    );
    return;
}
        try {
            ConnectDB.getInstance().connect();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Không kết nối SQL Server: "
                            + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame(
                    "TiTi Pet - UC002 Đổi/trả hàng"
            );

            f.setDefaultCloseOperation(
                    WindowConstants.EXIT_ON_CLOSE
            );

            f.setLayout(new BorderLayout());
            f.add(
                    new DoiTraPanel(maNV),
                    BorderLayout.CENTER
            );

            f.setSize(1200, 790);
            f.setMinimumSize(new Dimension(980, 680));
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
