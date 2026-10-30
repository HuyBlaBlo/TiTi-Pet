package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.KhachHangBanHang;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Truy xuất khách hàng cho chức năng chọn khách của POS. */
public class BanHangKhachHangDAO {
    public List<KhachHangBanHang> timKhachHang(String tuKhoa) throws SQLException {
        Connection con = ConnectDB.getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }
        if (con == null) throw new SQLException("Chưa kết nối SQL Server.");

        String keyword = tuKhoa == null ? "" : tuKhoa.strip();
        String sql = """
            SELECT TOP 100 maKH, hoTen, soDienThoai, hangTV, diemTichLuy
            FROM dbo.KhachHang
            WHERE hoTen LIKE ? OR soDienThoai LIKE ? OR maKH = ?
            ORDER BY hoTen, maKH
            """;
        List<KhachHangBanHang> result = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, keyword);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new KhachHangBanHang(
                        rs.getString("maKH"), rs.getString("hoTen"),
                        rs.getString("soDienThoai"), rs.getString("hangTV"),
                        rs.getInt("diemTichLuy")
                    ));
                }
            }
        }
        return result;
    }
}
