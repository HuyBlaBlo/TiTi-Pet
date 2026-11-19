package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.NhanVien;
import com.titipet.entity.enums.VaiTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class NhanVienDAO {
    private static final String TIM_THEO_MA_NV = """
            SELECT maNV, tenDangNhap, matKhau, hoTen, vaiTro
            FROM dbo.NhanVien
            WHERE maNV = ? AND dangHoatDong = 1
            """;

    /** Tra theo ma nhan su, role duoc doc tu tung tai khoan trong SQL Server. */
    public Optional<NhanVien> timTheoMaNhanVien(String maNV) throws SQLException {
        if (maNV == null || maNV.isBlank()) return Optional.empty();
        try (PreparedStatement statement = getActiveConnection().prepareStatement(TIM_THEO_MA_NV)) {
            statement.setString(1, maNV.strip().toUpperCase(java.util.Locale.ROOT));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapNhanVien(rs)) : Optional.empty();
            }
        }
    }

    /** Alias tuong thich cac doan code cu. */
    public Optional<NhanVien> timTheoTenDangNhap(String maNV) throws SQLException {
        return timTheoMaNhanVien(maNV);
    }

    private Connection getActiveConnection() throws SQLException {
        Connection connection = ConnectDB.getConnection();
        if (connection == null || connection.isClosed()) {
            ConnectDB.getInstance().connect();
            connection = ConnectDB.getConnection();
        }
        return connection;
    }

    private NhanVien mapNhanVien(ResultSet resultSet) throws SQLException {
        return new NhanVien(
                resultSet.getString("maNV"),
                resultSet.getString("matKhau"),
                resultSet.getString("hoTen"),
                VaiTro.valueOf(resultSet.getString("vaiTro")),
                resultSet.getString("tenDangNhap")
        );
    }

}
