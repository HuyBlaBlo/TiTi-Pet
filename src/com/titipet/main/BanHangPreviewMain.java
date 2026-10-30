
package com.titipet.main;

import com.formdev.flatlaf.FlatLightLaf;
import com.titipet.connectDB.ConnectDB;
import com.titipet.gui.panel.BanHangPanel;

import javax.swing.*;
import java.awt.*;

public class BanHangPreviewMain {

    public static void main(String[] args) {

        FlatLightLaf.setup();

        try {
            ConnectDB.getInstance().connect();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Lỗi kết nối SQL Server:\n" + e.getMessage(),
                "TiTi Pet",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TiTi Pet - UC008 Bán hàng");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            BanHangPanel panel = new BanHangPanel();

            // Lấy mã nhân viên từ môi trường kiểm thử.
            String maNV = System.getenv("TITIPET_MA_NV");

            if (maNV != null && !maNV.isBlank()) {
                panel.setMaNhanVienDangNhap(maNV);
            }

            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);
            frame.setSize(1440, 900);
            frame.setMinimumSize(new Dimension(1150, 750));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
