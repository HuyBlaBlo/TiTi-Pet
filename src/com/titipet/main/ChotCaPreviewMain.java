
package com.titipet.main;

import com.formdev.flatlaf.FlatLightLaf;
import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.enums.VaiTro;
import com.titipet.gui.panel.ChotCaPanel;

import javax.swing.*;
import java.awt.*;

public class ChotCaPreviewMain {

    public static void main(String[] args) {

        FlatLightLaf.setup();

        String maNV = System.getenv("TITIPET_MA_NV");
        String role = System.getenv("TITIPET_ROLE");

        if (maNV == null || maNV.isBlank()) {
            JOptionPane.showMessageDialog(
                null, "Chưa thiết lập mã nhân viên.");
            return;
        }

        VaiTro vaiTro;

        try {
            vaiTro = VaiTro.valueOf(role == null ? "" : role.trim());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                null, "Vai trò không hợp lệ.");
            return;
        }

        if (vaiTro != VaiTro.THU_NGAN
                && vaiTro != VaiTro.QUAN_LY) {
            JOptionPane.showMessageDialog(
                null, "Không có quyền sử dụng UC007.");
            return;
        }

        try {
            ConnectDB.getInstance().connect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                null,
                "Lỗi kết nối SQL Server: " + ex.getMessage()
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame(
                "TiTi Pet - UC007 Chốt ca"
            );

            frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
            );

            ChotCaPanel panel = new ChotCaPanel(
                maNV, vaiTro
            );

            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            frame.setSize(1200, 850);
            frame.setMinimumSize(new Dimension(900, 650));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
