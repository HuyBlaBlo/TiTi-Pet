package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.SanPhamBanHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO tra cứu sản phẩm phục vụ bán hàng.
 * Chỉ thao tác với SQL Server.
 */
public class BanHangSanPhamDAO {

    private static final String SQL_TIM_SAN_PHAM = """
            SELECT
                maSanPham,
                tenSanPham,
                maVach,
                giaBan,
                soLuongTonKho
            FROM dbo.SanPham
            WHERE tenSanPham LIKE ?
               OR maSanPham = ?
               OR maVach = ?
            ORDER BY tenSanPham, maSanPham
            """;

    /**
     * Tìm theo tên, mã sản phẩm hoặc mã vạch.
     * Để từ khóa rỗng sẽ lấy danh sách sản phẩm.
     */
    public List<SanPhamBanHang> timSanPham(String tuKhoa)
            throws SQLException {

        String key = tuKhoa == null
                ? ""
                : tuKhoa.trim();

        Connection connection = getActiveConnection();

        List<SanPhamBanHang> ketQua = new ArrayList<>();

        try (PreparedStatement ps =
                     connection.prepareStatement(SQL_TIM_SAN_PHAM)) {

            ps.setString(1, "%" + key + "%");
            ps.setString(2, key);
            ps.setString(3, key);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    SanPhamBanHang sanPham =
                            new SanPhamBanHang(
                                    rs.getString("maSanPham"),
                                    rs.getString("tenSanPham"),
                                    rs.getString("maVach"),
                                    rs.getBigDecimal("giaBan"),
                                    rs.getInt("soLuongTonKho")
                            );

                    ketQua.add(sanPham);
                }
            }
        }

        return ketQua;
    }

    /**
     * Sử dụng kết nối chung của nhóm.
     * Không đóng Connection tại đây.
     */
    private Connection getActiveConnection()
            throws SQLException {

        Connection connection = ConnectDB.getConnection();

        if (connection == null || connection.isClosed()) {

            ConnectDB.getInstance().connect();
            connection = ConnectDB.getConnection();
        }

        if (connection == null || connection.isClosed()) {
            throw new SQLException(
                    "Không kết nối được với SQL Server."
            );
        }

        return connection;
    }
}