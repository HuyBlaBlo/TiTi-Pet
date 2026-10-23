package com.titipet.main;

import com.formdev.flatlaf.FlatLightLaf;
import com.titipet.connectDB.ConnectDB;
import com.titipet.dao.NhanVienDAO;
import com.titipet.entity.NhanVien;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.Optional;

public class Main {

    public static void main(String[] args) throws SQLException{
        FlatLightLaf.setup();

        try {
            ConnectDB.getInstance().connect();
        } catch (SQLException exception) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                    null,
                    exception.getMessage(),
                    "Lỗi kết nối cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE
            ));
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                if (ConnectDB.getConnection() != null
                        && !ConnectDB.getConnection().isClosed()) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Kết nối cơ sở dữ liệu thành công!",
                            "TiTiPet",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(
                        null,
                        exception.getMessage(),
                        "Lỗi kết nối cơ sở dữ liệu",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });


        NhanVienDAO dao = new NhanVienDAO();

        Optional<NhanVien> ketQua =
                dao.timTheoTenDangNhap("admin");

        if (ketQua.isPresent()) {
            NhanVien nv = ketQua.get();

            System.out.println("Tên: " + nv.getHoTen());
            System.out.println("Vai trò: " + nv.getVaiTro());
        } else {
            System.out.println("Không tìm thấy nhân viên!");
        }
    }
}
