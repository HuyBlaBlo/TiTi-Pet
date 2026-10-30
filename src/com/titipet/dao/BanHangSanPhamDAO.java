package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.SanPhamBanHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/** Chỉ xử lý JDBC; dùng kết nối chung của module connectDB. */
public class BanHangSanPhamDAO {
    // Hạn sử dụng nằm ở bảng con ThucAn, không nằm trong SanPham.
    private static final String SQL = """
        SELECT s.maSanPham, s.tenSanPham, s.maVach,
               s.giaBan, s.soLuongTonKho, s.loaiSanPham,
               ta.hanSuDung
        FROM dbo.SanPham AS s
        LEFT JOIN dbo.ThucAn AS ta ON ta.maSanPham = s.maSanPham
        WHERE s.dangKinhDoanh = 1
          AND (s.tenSanPham LIKE ? OR s.maSanPham = ? OR s.maVach = ?)
        ORDER BY s.tenSanPham, s.maSanPham
        """;

    public List<SanPhamBanHang> timSanPham(String tuKhoa) throws SQLException {
        String key = tuKhoa == null ? "" : tuKhoa.strip();
        Connection connection = layKetNoi();
        List<SanPhamBanHang> ketQua = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SQL)) {
            ps.setString(1, "%" + key + "%");
            ps.setString(2, key);
            ps.setString(3, key);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date hsd = rs.getDate("hanSuDung");
                    ketQua.add(new SanPhamBanHang(
                        rs.getString("maSanPham"),
                        rs.getString("tenSanPham"),
                        rs.getString("maVach"),
                        rs.getBigDecimal("giaBan"),
                        rs.getInt("soLuongTonKho"),
                        rs.getString("loaiSanPham"),
                        hsd == null ? null : hsd.toLocalDate()
                    ));
                }
            }
        }
        return ketQua;
    }

    private Connection layKetNoi() throws SQLException {
        Connection connection = ConnectDB.getConnection();
        if (connection == null || connection.isClosed()) {
            ConnectDB.getInstance().connect();
            connection = ConnectDB.getConnection();
        }
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Không thể kết nối SQL Server.");
        }
        return connection;
    }
}
