
package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.DichVuSpa;
import com.titipet.entity.enums.LoaiThuCungApDung;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVuSpaDAO {

    private Connection getActiveConnection() throws SQLException {
        Connection con = ConnectDB.getConnection();

        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }

        return con;
    }

    // Danh sách dịch vụ Spa đang hoạt động
    public List<DichVuSpa> layDanhSachDichVuSpa() throws SQLException {

        String sql = """
            SELECT maDichVu, tenDichVu, donGiaCoBan, thueSuat,
                   giaDaBaoGomThue, loaiThuCungApDung,
                   thoiLuongPhut, dangHoatDong
            FROM dbo.DichVuSpa
            WHERE dangHoatDong = 1
            ORDER BY tenDichVu
            """;

        List<DichVuSpa> ds = new ArrayList<>();

        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String loai = rs.getString("loaiThuCungApDung");
                LoaiThuCungApDung loaiEnum = null;

                try {
                    loaiEnum = LoaiThuCungApDung.valueOf(loai);
                } catch (IllegalArgumentException ignored) {
                    // Enum chưa có giá trị tương ứng
                }

                DichVuSpa dv = new DichVuSpa(
                    rs.getString("maDichVu"),
                    rs.getString("tenDichVu"),
                    rs.getBigDecimal("donGiaCoBan"),
                    rs.getBigDecimal("thueSuat"),
                    rs.getBoolean("giaDaBaoGomThue"),
                    loaiEnum,
                    rs.getBoolean("dangHoatDong")
                );

                dv.setThoiLuongPhut(rs.getInt("thoiLuongPhut"));

                ds.add(dv);
            }
        }

        return ds;
    }
    
     // Lấy dịch vụ theo mã
    public DichVuSpa layDichVuTheoMa(String maDichVu) {
        String sql = """
                SELECT maDichVu, tenDichVu, donGiaCoBan,
                       thueSuat, giaDaBaoGomThue,
                       loaiThuCungApDung, thoiLuongPhut,
                       dangHoatDong
                FROM dbo.DichVuSpa
                WHERE maDichVu = ?
                """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maDichVu);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DichVuSpa dv = new DichVuSpa();

                    dv.setMaDichVu(rs.getString("maDichVu"));
                    dv.setTenDichVu(rs.getString("tenDichVu"));
                    dv.setDonGiaCoBan(
                        rs.getBigDecimal("donGiaCoBan")
                    );
                    dv.setThueSuat(
                        rs.getBigDecimal("thueSuat")
                    );
                    dv.setGiaDaBaoGomThue(
                        rs.getBoolean("giaDaBaoGomThue")
                    );

                    String loai = rs.getString("loaiThuCungApDung");
                    if (loai != null) {
                        dv.setLoaiThuCungApDung(
                            LoaiThuCungApDung.valueOf(loai)
                        );
                    }

                    dv.setThoiLuongPhut(
                        rs.getInt("thoiLuongPhut")
                    );
                    dv.setDangHoatDong(
                        rs.getBoolean("dangHoatDong")
                    );

                    return dv;
                }
            }

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm dịch vụ
    public boolean themDichVu(DichVuSpa dv) {
        String sql = """
                INSERT INTO dbo.DichVuSpa
                (maDichVu, tenDichVu, donGiaCoBan,
                 thueSuat, giaDaBaoGomThue,
                 loaiThuCungApDung, thoiLuongPhut,
                 dangHoatDong)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dv.getMaDichVu());
            ps.setString(2, dv.getTenDichVu());
            ps.setBigDecimal(3, dv.getDonGiaCoBan());
            ps.setBigDecimal(4, dv.getThueSuat());
            ps.setBoolean(5, dv.isGiaDaBaoGomThue());

            if (dv.getLoaiThuCungApDung() != null) {
                ps.setString(6, dv.getLoaiThuCungApDung().name());
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
            }

            ps.setInt(7, dv.getThoiLuongPhut());
            ps.setBoolean(8, dv.isDangHoatDong());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật dịch vụ
    public boolean capNhatDichVu(DichVuSpa dv) {
        String sql = """
                UPDATE dbo.DichVuSpa
                SET tenDichVu = ?,
                    donGiaCoBan = ?,
                    thueSuat = ?,
                    giaDaBaoGomThue = ?,
                    loaiThuCungApDung = ?,
                    thoiLuongPhut = ?,
                    dangHoatDong = ?
                WHERE maDichVu = ?
                """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dv.getTenDichVu());
            ps.setBigDecimal(2, dv.getDonGiaCoBan());
            ps.setBigDecimal(3, dv.getThueSuat());
            ps.setBoolean(4, dv.isGiaDaBaoGomThue());

            if (dv.getLoaiThuCungApDung() != null) {
                ps.setString(5, dv.getLoaiThuCungApDung().name());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }

            ps.setInt(6, dv.getThoiLuongPhut());
            ps.setBoolean(7, dv.isDangHoatDong());
            ps.setString(8, dv.getMaDichVu());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa dịch vụ
    public boolean xoaDichVu(String maDichVu) {
        String sql = """
                DELETE FROM dbo.DichVuSpa
                WHERE maDichVu = ?
                """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maDichVu);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Tìm kiếm dịch vụ theo tên
    public List<DichVuSpa> timDichVu(String ten) {
        List<DichVuSpa> ds = new ArrayList<>();

        String sql = """
                SELECT maDichVu, tenDichVu, donGiaCoBan,
                       thueSuat, giaDaBaoGomThue,
                       loaiThuCungApDung, thoiLuongPhut,
                       dangHoatDong
                FROM dbo.DichVuSpa
                WHERE tenDichVu LIKE ?
                ORDER BY tenDichVu
                """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + ten + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DichVuSpa dv = new DichVuSpa();

                    dv.setMaDichVu(rs.getString("maDichVu"));
                    dv.setTenDichVu(rs.getString("tenDichVu"));
                    dv.setDonGiaCoBan(
                        rs.getBigDecimal("donGiaCoBan")
                    );
                    dv.setThueSuat(
                        rs.getBigDecimal("thueSuat")
                    );
                    dv.setGiaDaBaoGomThue(
                        rs.getBoolean("giaDaBaoGomThue")
                    );

                    String loai = rs.getString("loaiThuCungApDung");
                    if (loai != null) {
                        dv.setLoaiThuCungApDung(
                            LoaiThuCungApDung.valueOf(loai)
                        );
                    }

                    dv.setThoiLuongPhut(
                        rs.getInt("thoiLuongPhut")
                    );
                    dv.setDangHoatDong(
                        rs.getBoolean("dangHoatDong")
                    );

                    ds.add(dv);
                }
            }

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return ds;
    }
}
