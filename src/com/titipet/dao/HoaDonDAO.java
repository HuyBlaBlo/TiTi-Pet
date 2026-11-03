package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Truy vấn đọc hóa đơn từ SQL Server; KHÔNG sửa hóa đơn, tồn kho hay điểm. */
public class HoaDonDAO {

    public record HoaDonTomTat(String maHoaDon, LocalDateTime ngayLap,
            String maKH, String tenKhach, String soDienThoai,
            String nguonDon, String phuongThuc, BigDecimal tienPhaiThu,
            String trangThai) { }

    public record HoaDonChiTiet(String maHoaDon, LocalDateTime ngayLap,
            String maKH, String tenKhach, String soDienThoai,
            String maNV, String tenNhanVien, String maCa,
            String nguonDon, String maDonKenh, String phuongThuc,
            String trangThai, String tenKhuyenMai,
            BigDecimal tongTienHang, BigDecimal tongTienSpa,
            BigDecimal tienGiamKM, BigDecimal tienGiamHangTV,
            int soDiemSuDung, BigDecimal tienGiamDiem, int soDiemDuocTich,
            BigDecimal tongTienTruocThue, BigDecimal tongTienThue,
            BigDecimal tienPhaiThu, List<DongChiTiet> cacDong) { }

    public record DongChiTiet(String loai, String ma, String ten,
            String moTa, int soLuong, BigDecimal donGia,
            BigDecimal thanhTien, BigDecimal giamGia,
            BigDecimal tienTruocThue, BigDecimal tienThue,
            BigDecimal tienSauThue) { }

    public List<HoaDonTomTat> timHoaDon(String tuKhoa, String trangThai,
                                        int gioiHan) throws SQLException {
        String sql = """
                SELECT TOP (?) h.maHoaDon, h.ngayLap, h.maKH,
                       kh.hoTen, kh.soDienThoai, h.nguonDon,
                       h.phuongThucThanhToan, h.tienPhaiThu, h.trangThai
                FROM dbo.HoaDon h
                INNER JOIN dbo.KhachHang kh ON kh.maKH = h.maKH
                WHERE (? = '' OR CHARINDEX(?, h.maHoaDon) > 0
                    OR CHARINDEX(?, kh.hoTen) > 0
                    OR CHARINDEX(?, kh.soDienThoai) > 0
                    OR CHARINDEX(?, kh.maKH) > 0
                    OR CHARINDEX(?, CONVERT(VARCHAR(10), h.ngayLap, 103)) > 0)
                  AND (? = '' OR h.trangThai = ?)
                ORDER BY h.ngayLap DESC, h.maHoaDon DESC
                """;
        List<HoaDonTomTat> rows = new ArrayList<>();
        synchronized (ConnectDB.class) {
        Connection con = ketNoiChung();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, gioiHan);
            String key = tuKhoa == null ? "" : tuKhoa.strip();
            ps.setString(2, key);
            for (int i = 3; i <= 7; i++) ps.setNString(i, key);
            String filter = trangThai == null ? "" : trangThai;
            ps.setString(8, filter);
            ps.setString(9, filter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new HoaDonTomTat(
                            rs.getString("maHoaDon"), docThoiGian(rs, "ngayLap"),
                            rs.getString("maKH"), rs.getString("hoTen"),
                            rs.getString("soDienThoai"), rs.getString("nguonDon"),
                            rs.getString("phuongThucThanhToan"),
                            rs.getBigDecimal("tienPhaiThu"), rs.getString("trangThai")));
                }
            }
        }
        return rows;
        }
    }

    public Optional<HoaDonChiTiet> chiTiet(String maHoaDon) throws SQLException {
        String sql = """
                SELECT h.*, kh.hoTen AS tenKhach, kh.soDienThoai,
                       nv.hoTen AS tenNhanVien, km.tenChuongTrinh
                FROM dbo.HoaDon h
                INNER JOIN dbo.KhachHang kh ON kh.maKH = h.maKH
                INNER JOIN dbo.NhanVien nv ON nv.maNV = h.maNV
                LEFT JOIN dbo.KhuyenMai km ON km.maKhuyenMai = h.maKhuyenMai
                WHERE h.maHoaDon = ?
                """;
        synchronized (ConnectDB.class) {
            Connection con = ketNoiChung();
            HoaDonChiTiet header;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    // Đọc header xong đóng ResultSet trước khi truy vấn chi tiết (không yêu cầu MARS).
                    header = new HoaDonChiTiet(
                            rs.getString("maHoaDon"), docThoiGian(rs, "ngayLap"),
                            rs.getString("maKH"), rs.getString("tenKhach"),
                            rs.getString("soDienThoai"), rs.getString("maNV"),
                            rs.getString("tenNhanVien"), rs.getString("maCa"),
                            rs.getString("nguonDon"), rs.getString("maDonKenh"),
                            rs.getString("phuongThucThanhToan"), rs.getString("trangThai"),
                            rs.getString("tenChuongTrinh"), rs.getBigDecimal("tongTienHang"),
                            rs.getBigDecimal("tongTienSpa"), rs.getBigDecimal("tienGiamKM"),
                            rs.getBigDecimal("tienGiamHangTV"), rs.getInt("soDiemSuDung"),
                            rs.getBigDecimal("tienGiamDiem"), rs.getInt("soDiemDuocTich"),
                            rs.getBigDecimal("tongTienTruocThue"),
                            rs.getBigDecimal("tongTienThue"),
                            rs.getBigDecimal("tienPhaiThu"), List.of());
                }
            }
            List<DongChiTiet> rows = new ArrayList<>();
            rows.addAll(docSanPham(con, maHoaDon));
            rows.addAll(docDichVu(con, maHoaDon));
            return Optional.of(new HoaDonChiTiet(header.maHoaDon(), header.ngayLap(),
                    header.maKH(), header.tenKhach(), header.soDienThoai(),
                    header.maNV(), header.tenNhanVien(), header.maCa(),
                    header.nguonDon(), header.maDonKenh(), header.phuongThuc(),
                    header.trangThai(), header.tenKhuyenMai(), header.tongTienHang(),
                    header.tongTienSpa(), header.tienGiamKM(), header.tienGiamHangTV(),
                    header.soDiemSuDung(), header.tienGiamDiem(), header.soDiemDuocTich(),
                    header.tongTienTruocThue(), header.tongTienThue(), header.tienPhaiThu(),
                    List.copyOf(rows)));
        }
    }

    private List<DongChiTiet> docSanPham(Connection con, String maHD) throws SQLException {
        String sql = """
                SELECT ct.maSanPham, sp.tenSanPham, ct.soLuong, ct.donGia,
                       ct.thanhTien, ct.tienGiamPhanBo, ct.tienTruocThue,
                       ct.tienThue, ct.tienSauThue
                FROM dbo.ChiTietHoaDon_SanPham ct
                LEFT JOIN dbo.SanPham sp ON sp.maSanPham = ct.maSanPham
                WHERE ct.maHoaDon = ?
                ORDER BY ct.maSanPham
                """;
        List<DongChiTiet> result = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maSanPham");
                    result.add(new DongChiTiet("SẢN PHẨM", ma,
                            coTen(rs.getString("tenSanPham"), ma), "", rs.getInt("soLuong"),
                            rs.getBigDecimal("donGia"), rs.getBigDecimal("thanhTien"),
                            rs.getBigDecimal("tienGiamPhanBo"), rs.getBigDecimal("tienTruocThue"),
                            rs.getBigDecimal("tienThue"), rs.getBigDecimal("tienSauThue")));
                }
            }
        }
        return result;
    }

    private List<DongChiTiet> docDichVu(Connection con, String maHD) throws SQLException {
        String sql = """
                SELECT ct.maDichVu, dv.tenDichVu, tc.tenThuCung,
                       ct.trangThaiThucHien, ct.soLuong, ct.donGia,
                       ct.thanhTien, ct.tienGiamPhanBo, ct.tienTruocThue,
                       ct.tienThue, ct.tienSauThue
                FROM dbo.ChiTietHoaDon_DichVu ct
                LEFT JOIN dbo.DichVuSpa dv ON dv.maDichVu = ct.maDichVu
                LEFT JOIN dbo.ThuCung tc ON tc.maThuCung = ct.maThuCung
                WHERE ct.maHoaDon = ?
                ORDER BY ct.maChiTietDichVu
                """;
        List<DongChiTiet> result = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maDichVu");
                    String tenPet = rs.getString("tenThuCung");
                    String moTa = (tenPet == null ? "Thú cưng chưa xác định" : tenPet)
                            + " • " + rs.getString("trangThaiThucHien");
                    result.add(new DongChiTiet("SPA", ma,
                            coTen(rs.getString("tenDichVu"), ma), moTa,
                            rs.getInt("soLuong"), rs.getBigDecimal("donGia"),
                            rs.getBigDecimal("thanhTien"), rs.getBigDecimal("tienGiamPhanBo"),
                            rs.getBigDecimal("tienTruocThue"), rs.getBigDecimal("tienThue"),
                            rs.getBigDecimal("tienSauThue")));
                }
            }
        }
        return result;
    }

    private Connection ketNoiChung() throws SQLException {
        // Dùng cùng cấu hình ConnectDB.connect() để tránh lệch cổng/DB giữa các module.
        // Người gọi đồng bộ theo ConnectDB.class và CHỈ đóng Statement/ResultSet.
        Connection con = ConnectDB.getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }
        if (con == null || con.isClosed()) {
            throw new SQLException("Chưa kết nối SQL Server.");
        }
        return con;
    }

    private static String coTen(String ten, String fallback) {
        return ten == null || ten.isBlank() ? fallback : ten;
    }

    private static LocalDateTime docThoiGian(ResultSet rs, String col) throws SQLException {
        Timestamp value = rs.getTimestamp(col);
        return value == null ? null : value.toLocalDateTime();
    }
}
