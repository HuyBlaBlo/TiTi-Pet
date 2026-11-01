package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Truy van Spa cua UC008, SQL chi nam trong lop DAO. */
public class BanHangSpaDAO {
    public record DichVu(
            String ma, String ten, BigDecimal giaCoBan,
            BigDecimal thueSuat, boolean giaDaBaoGomThue, String loaiApDung) {
        @Override public String toString() { return ten + " - " + ma; }
    }

    public record ThuCung(
            String ma, String ten, String loai, BigDecimal canNang) {
        @Override public String toString() {
            return ten + " (" + loai + ", " + canNang + " kg)";
        }
    }

    private static Connection ketNoi() throws SQLException {
        Connection con = ConnectDB.getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }
        if (con == null || con.isClosed()) {
            throw new SQLException("Chua ket noi SQL Server.");
        }
        return con;
    }

    public List<DichVu> danhSachDichVu() throws SQLException {
        String sql = """
                SELECT maDichVu, tenDichVu, donGiaCoBan, thueSuat,
                       giaDaBaoGomThue, loaiThuCungApDung
                FROM dbo.DichVuSpa WHERE dangHoatDong = 1
                ORDER BY tenDichVu, maDichVu
                """;
        synchronized (ConnectDB.class) {
            List<DichVu> ketQua = new ArrayList<>();
            try (PreparedStatement ps = ketNoi().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(new DichVu(
                        rs.getString("maDichVu"), rs.getNString("tenDichVu"),
                        rs.getBigDecimal("donGiaCoBan"), rs.getBigDecimal("thueSuat"),
                        rs.getBoolean("giaDaBaoGomThue"),
                        rs.getString("loaiThuCungApDung")));
                }
            }
            return ketQua;
        }
    }

    public List<ThuCung> danhSachThuCung(String maKH) throws SQLException {
        String sql = """
                SELECT maThuCung, tenThuCung, loaiThuCung, canNang
                FROM dbo.ThuCung WHERE maKH = ? ORDER BY tenThuCung
                """;
        synchronized (ConnectDB.class) {
            List<ThuCung> ketQua = new ArrayList<>();
            try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
                ps.setString(1, maKH);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ketQua.add(new ThuCung(
                            rs.getString("maThuCung"), rs.getNString("tenThuCung"),
                            rs.getString("loaiThuCung"), rs.getBigDecimal("canNang")));
                    }
                }
            }
            return ketQua;
        }
    }
}
