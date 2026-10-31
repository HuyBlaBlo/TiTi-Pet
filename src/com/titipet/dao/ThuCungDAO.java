package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.KhachHang;
import com.titipet.entity.ThuCung;
import com.titipet.entity.enums.LoaiThuCungApDung;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThuCungDAO {
	/**
     * Lấy danh sách thú cưng hiển thị Card Grid kèm tìm kiếm Live Search
     * Lọc theo: Tên pet, tình trạng lông, tên chủ nuôi, số điện thoại
     */
    public List<ThuCung> layDanhSachCard(String tuKhoa) throws SQLException {
        List<ThuCung> list = new ArrayList<>();
        String sql = """
            SELECT tc.maThuCung, tc.tenThuCung, tc.loaiThuCung, tc.canNang, 
                   tc.tinhTrangLong, tc.ghiChu,
                   kh.maKH, kh.hoTen, kh.soDienThoai
            FROM dbo.ThuCung tc
            LEFT JOIN dbo.KhachHang kh ON tc.maKH = kh.maKH
            WHERE (? IS NULL OR ? = ''
                   OR tc.tenThuCung LIKE ?
                   OR tc.tinhTrangLong LIKE ?
                   OR kh.hoTen LIKE ?
                   OR kh.soDienThoai LIKE ?)
            ORDER BY tc.maThuCung ASC
        """;

        Connection con = getActiveConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            String pattern = "%" + (tuKhoa != null ? tuKhoa.trim() : "") + "%";
            stmt.setString(1, tuKhoa);
            stmt.setString(2, tuKhoa);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            stmt.setString(5, pattern);
            stmt.setString(6, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapThuCung(rs));
                }
            }
        }
        return list;
    }
    
    /**
     * Tìm kiếm thú cưng theo mã
     */
    public Optional<ThuCung> timTheoMa(String maThuCung) throws SQLException {
        String sql = """
            SELECT tc.maThuCung, tc.tenThuCung, tc.loaiThuCung, tc.canNang, 
                   tc.tinhTrangLong, tc.ghiChu,
                   kh.maKH, kh.hoTen, kh.soDienThoai
            FROM dbo.ThuCung tc
            LEFT JOIN dbo.KhachHang kh ON tc.maKH = kh.maKH
            WHERE tc.maThuCung = ?
        """;

        Connection con = getActiveConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maThuCung);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapThuCung(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Thêm mới thú cưng vào cơ sở dữ liệu
     */
    public boolean themThuCung(ThuCung tc) throws SQLException {
        String sql = """
            INSERT INTO dbo.ThuCung (maThuCung, maKH, tenThuCung, loaiThuCung, canNang, tinhTrangLong, ghiChu)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        Connection con = getActiveConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tc.getMaThuCung());
            stmt.setString(2, tc.getChuNuoi() != null ? tc.getChuNuoi().getMaKH() : null);
            stmt.setString(3, tc.getTenThuCung());
            stmt.setString(4, tc.getLoaiThuCung() != null ? tc.getLoaiThuCung().name() : LoaiThuCungApDung.CHO.name());
            stmt.setDouble(5, tc.getCanNang());
            stmt.setString(6, tc.getTinhTrangLong());
            stmt.setString(7, tc.getGhiChu());

            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Cập nhật hồ sơ thông tin thú cưng
     */
    public boolean capNhatThuCung(ThuCung tc) throws SQLException {
        String sql = """
            UPDATE dbo.ThuCung
            SET maKH = ?, tenThuCung = ?, loaiThuCung = ?, canNang = ?, tinhTrangLong = ?, ghiChu = ?
            WHERE maThuCung = ?
        """;

        Connection con = getActiveConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tc.getChuNuoi() != null ? tc.getChuNuoi().getMaKH() : null);
            stmt.setString(2, tc.getTenThuCung());
            stmt.setString(3, tc.getLoaiThuCung() != null ? tc.getLoaiThuCung().name() : LoaiThuCungApDung.CHO.name());
            stmt.setDouble(4, tc.getCanNang());
            stmt.setString(5, tc.getTinhTrangLong());
            stmt.setString(6, tc.getGhiChu());
            stmt.setString(7, tc.getMaThuCung());

            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Tự động sinh mã kế tiếp: PET001, PET002...
     */
    public String phatSinhMaThuCungTuDong() throws SQLException {
        String sql = "SELECT TOP 1 maThuCung FROM dbo.ThuCung ORDER BY maThuCung DESC";
        Connection con = getActiveConnection();
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String maMax = rs.getString("maThuCung");
                if (maMax != null && maMax.matches(".*\\d+.*")) {
                    int so = Integer.parseInt(maMax.replaceAll("\\D+", "")) + 1;
                    return String.format("PET%03d", so);
                }
            }
        }
        return "PET001";
    }
    
    /**
     * Lấy toàn bộ danh sách khách hàng (đang hoạt động) để nạp vào ComboBox
     */
    public List<KhachHang> layTatCaKhachHang() throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT maKH, hoTen, soDienThoai FROM dbo.KhachHang ORDER BY hoTen ASC";

        Connection con = ConnectDB.getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                KhachHang kh = new KhachHang();
                kh.setMaKH(rs.getString("maKH"));
                kh.setHoTen(rs.getString("hoTen"));
                kh.setSoDienThoai(rs.getString("soDienThoai"));
                list.add(kh);
            }
        }
        return list;
    }
    
    // --- HÀM TRỢ GIÚP ÁNH XẠ VÀ KẾT NỐI ---
    private ThuCung mapThuCung(ResultSet rs) throws SQLException {
        KhachHang chuNuoi = new KhachHang();
        chuNuoi.setMaKH(rs.getString("maKH"));
        chuNuoi.setHoTen(rs.getString("hoTen"));
        chuNuoi.setSoDienThoai(rs.getString("soDienThoai"));

        String rawLoai = rs.getString("loaiThuCung");
        LoaiThuCungApDung loai = "MEO".equalsIgnoreCase(rawLoai) ? LoaiThuCungApDung.MEO : LoaiThuCungApDung.CHO;

        return new ThuCung(
                rs.getString("maThuCung"),
                rs.getString("tenThuCung"),
                loai,
                rs.getDouble("canNang"),
                rs.getString("tinhTrangLong"),
                rs.getString("ghiChu"),
                chuNuoi
        );
    }
    
    private Connection getActiveConnection() throws SQLException {
        Connection con = ConnectDB.getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }
        return con;
    }
}
