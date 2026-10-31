
package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DAO UC002 - Tra cuu hoa don va lich su doi tra.
 * Chua thuc hien thay doi ton kho.
 */
public class DoiTraDAO {

    public record DongHang(
            String maSP,
            String tenSP,
            String loaiSP,
            int soLuong,
            BigDecimal donGia,
            int soLanDaDoi) {

        @Override
        public String toString() {
            return tenSP + "  |  SL: " + soLuong
                    + "  |  " + donGia + " đ";
        }
    }

    public record HoaDonGoc(
            String maHD,
            String tenKH,
            String sdt,
            LocalDateTime ngayLap,
            String trangThai,
            List<DongHang> sanPham) {
    }

    public record LichSu(
            String maDoiTra,
            String maSPCu,
            String maSPMoi,
            BigDecimal chenhLech,
            LocalDateTime ngayDoi,
            String loaiXuLy,
            BigDecimal tienHoan) {

        @Override
        public String toString() {
            return maDoiTra + " | " + ("TRA".equals(loaiXuLy) ? "Trả" : "Đổi")
                    + " | " + maSPCu + (maSPMoi == null ? "" : " → " + maSPMoi)
                    + ("TRA".equals(loaiXuLy) ? " | Hoàn " + tienHoan + " đ" : "");
        }
    }

    /** San pham phu kien con ban va con ton de khach chon thay the. */
    public record SanPhamThayThe(
            String maSP,
            String tenSP,
            BigDecimal giaBan,
            int tonKho) {

        @Override
        public String toString() {
            return tenSP + " | " + giaBan.toPlainString()
                    + " đ | Còn " + tonKho;
        }
    }

    public List<SanPhamThayThe> laySanPhamThayThe(String maSPCu)
            throws SQLException {
        synchronized (ConnectDB.class) {
            Connection c = ketNoi();
            String sql = """
                SELECT maSanPham, tenSanPham, giaBan, soLuongTonKho
                FROM dbo.SanPham
                WHERE loaiSanPham = 'PHU_KIEN'
                  AND dangKinhDoanh = 1
                  AND soLuongTonKho > 0
                  AND maSanPham <> ?
                ORDER BY tenSanPham
                """;
            List<SanPhamThayThe> ds = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, maSPCu);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ds.add(new SanPhamThayThe(
                                rs.getString("maSanPham"),
                                rs.getString("tenSanPham"),
                                rs.getBigDecimal("giaBan"),
                                rs.getInt("soLuongTonKho")
                        ));
                    }
                }
            }
            return List.copyOf(ds);
        }
    }

    /** Tra cuu bang SDT, cho nguoi dung chon hoa don neu co nhieu. */
    public record HoaDonTomTat(String maHD, LocalDateTime ngayLap,
                               String trangThai) {
        @Override public String toString() {
            return maHD + " | " + ngayLap + " | " + trangThai;
        }
    }

    public List<HoaDonTomTat> timTheoSoDienThoai(String sdt) throws SQLException {
        synchronized (ConnectDB.class) {
            Connection c = ketNoi();
            List<HoaDonTomTat> items = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("""
                SELECT TOP 100 h.maHoaDon,h.ngayLap,h.trangThai
                FROM dbo.HoaDon h JOIN dbo.KhachHang k ON k.maKH=h.maKH
                WHERE k.soDienThoai=? AND h.trangThai='DA_THANH_TOAN'
                ORDER BY h.ngayLap DESC
                """)) {
                ps.setString(1,sdt);
                try (ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) items.add(new HoaDonTomTat(
                        rs.getString(1),rs.getTimestamp(2).toLocalDateTime(),rs.getString(3)));
                }
            }
            return List.copyOf(items);
        }
    }

    private Connection ketNoi() throws SQLException {
        Connection c = ConnectDB.getConnection();

        if (c == null || c.isClosed()) {
            ConnectDB.getInstance().connect();
            c = ConnectDB.getConnection();
        }

        if (c == null || c.isClosed()) {
            throw new SQLException("Chưa kết nối SQL Server.");
        }

        return c;
    }

    public HoaDonGoc timHoaDon(String maHD)
            throws SQLException {

        synchronized (ConnectDB.class) {
            Connection c = ketNoi();

            String sql = """
                SELECT h.maHoaDon, h.ngayLap, h.trangThai,
                       k.hoTen, k.soDienThoai
                FROM dbo.HoaDon h
                JOIN dbo.KhachHang k ON k.maKH = h.maKH
                WHERE h.maHoaDon = ?
                """;

            String tenKH;
            String sdt;
            String trangThai;
            LocalDateTime ngayLap;

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, maHD);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    tenKH = rs.getString("hoTen");
                    sdt = rs.getString("soDienThoai");
                    trangThai = rs.getString("trangThai");
                    ngayLap = rs.getTimestamp("ngayLap")
                            .toLocalDateTime();
                }
            }

            String sqlCT = """
                SELECT ct.maSanPham, s.tenSanPham,
                       s.loaiSanPham, ct.soLuong, ct.donGia,
                       (SELECT COUNT(*)
                        FROM dbo.DoiTra d
                        WHERE d.maHoaDon = ct.maHoaDon
                          AND d.maSanPhamCu = ct.maSanPham
                       ) AS soLanDaDoi
                FROM dbo.ChiTietHoaDon_SanPham ct
                JOIN dbo.SanPham s
                  ON s.maSanPham = ct.maSanPham
                WHERE ct.maHoaDon = ?
                ORDER BY s.tenSanPham
                """;

            List<DongHang> hang = new ArrayList<>();

            try (PreparedStatement ps = c.prepareStatement(sqlCT)) {
                ps.setString(1, maHD);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        hang.add(new DongHang(
                                rs.getString("maSanPham"),
                                rs.getString("tenSanPham"),
                                rs.getString("loaiSanPham"),
                                rs.getInt("soLuong"),
                                rs.getBigDecimal("donGia"),
                                rs.getInt("soLanDaDoi")
                        ));
                    }
                }
            }

            return new HoaDonGoc(
                    maHD,
                    tenKH,
                    sdt,
                    ngayLap,
                    trangThai,
                    List.copyOf(hang)
            );
        }
    }

    public List<LichSu> lichSu(String maHD)
            throws SQLException {

        synchronized (ConnectDB.class) {
            Connection c = ketNoi();

            List<LichSu> result = new ArrayList<>();

            String sql = """
                SELECT maDoiTra, maSanPhamCu,
                       maSanPhamMoi, chenhLech, ngayDoi,
                       loaiXuLy, tienHoan
                FROM dbo.DoiTra
                WHERE maHoaDon = ?
                ORDER BY ngayDoi DESC
                """;

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, maHD);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new LichSu(
                                rs.getString("maDoiTra"),
                                rs.getString("maSanPhamCu"),
                                rs.getString("maSanPhamMoi"),
                                rs.getBigDecimal("chenhLech"),
                                rs.getTimestamp("ngayDoi").toLocalDateTime(),
                                rs.getString("loaiXuLy"),
                                rs.getBigDecimal("tienHoan")
                        ));
                    }
                }
            }

            return result;
        }
    }
}
