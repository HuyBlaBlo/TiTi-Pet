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
    private static final String dTIM_THEO_TEN_DANG_NHAP = """
            SELECT maNV, tenDangNhap, matKhau, hoTen, vaiTro 
            FROM dbo.NhanVien 
            WHERE tenDangNhap = ? AND dangHoatDong = 1
            """;

    private static final String TIM_THEO_TEN_DANG_NHAP =
            "SELECT maNV, tenDangNhap, matKhau, hoTen, vaiTro " +
                    "FROM NhanVien " +
                    "WHERE tenDangNhap = ? AND dangHoatDong = 1";


    /**
     * Tìm một nhân viên đang hoạt động theo tên đăng nhập
     */
    public Optional<NhanVien> timTheoTenDangNhap(String tenDangNhap) throws  SQLException{
        Connection connection = getActiveConnection();
        try(PreparedStatement statement = connection.prepareStatement(TIM_THEO_TEN_DANG_NHAP)){
            statement.setString(1,tenDangNhap);
            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return Optional.of(mapNhanVien(resultSet));
                }
            }
        }
        return Optional.empty();
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
