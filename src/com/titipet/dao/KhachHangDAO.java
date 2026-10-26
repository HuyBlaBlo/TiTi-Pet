package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;

import com.titipet.entity.KhachHang;
import com.titipet.entity.enums.HangTV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {
	/**
     * Lấy toàn bộ danh sách khách hàng dưới dạng Entity KhachHang.
     * Sử dụng enum HangTV để gán giá trị tương ứng.
     */
    public List<KhachHang> getAllKhachHang() throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT maKH, hoTen, soDienThoai, hangTV, diemTichLuy FROM dbo.KhachHang";

        Connection con = ConnectDB.getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Đọc chuỗi từ CSDL và chuyển đổi sang Enum HangTV
                String hangStr = rs.getString("hangTV");
                HangTV hang = (hangStr != null) ? HangTV.valueOf(hangStr) : HangTV.THUONG;

                KhachHang kh = new KhachHang(
                        rs.getString("maKH"),
                        rs.getString("hoTen"),
                        rs.getString("soDienThoai"),
                        hang,
                        rs.getInt("diemTichLuy")
                );
                list.add(kh);
            }
        }
        return list;
    }
    
    /**
     * Lấy dữ liệu tổng hợp để nạp trực tiếp lên JTable (gồm tên thú cưng và tổng chi tiêu).
     * Trả về List<Object[]> đại diện cho từng dòng của DefaultTableModel.
     */
    public List<Object[]> getDanhSachHienThiBang(String keyword) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        
        // Truy vấn kết hợp: bảng KhachHang + ThuCung + HoaDon đã thanh toán
        String sql = "SELECT kh.maKH, kh.hoTen, kh.soDienThoai, kh.hangTV, kh.diemTichLuy, " +
                     "       ISNULL(STUFF((SELECT ', ' + tc.tenThuCung FROM dbo.ThuCung tc WHERE tc.maKH = kh.maKH FOR XML PATH('')), 1, 2, ''), N'Chưa có') AS dsThuCung, " +
                     "       ISNULL(SUM(CASE WHEN hd.trangThai = 'DA_THANH_TOAN' THEN hd.tienPhaiThu ELSE 0 END), 0) AS tongChiTieu " +
                     "FROM dbo.KhachHang kh " +
                     "LEFT JOIN dbo.HoaDon hd ON kh.maKH = hd.maKH " +
                     "WHERE (? IS NULL OR ? = '' OR kh.hoTen LIKE ? OR kh.soDienThoai LIKE ?) " +
                     "GROUP BY kh.maKH, kh.hoTen, kh.soDienThoai, kh.hangTV, kh.diemTichLuy";

        Connection con = ConnectDB.getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            String pattern = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.trim() + "%" : "";
            stmt.setString(1, keyword);
            stmt.setString(2, keyword);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String hangStr = rs.getString("hangTV");
                    HangTV hang = (hangStr != null) ? HangTV.valueOf(hangStr) : HangTV.THUONG;

                    Object[] row = new Object[]{
                            rs.getString("hoTen"),           // Cột 0: Tên để renderer vẽ avatar
                            rs.getString("soDienThoai"),      // Cột 1: SĐT
                            rs.getString("dsThuCung"),        // Cột 2: Tên thú cưng
                            hang,                             // Cột 3: Enum HangTV
                            rs.getInt("diemTichLuy"),         // Cột 4: Điểm hiện có
                            rs.getBigDecimal("tongChiTieu"),  // Cột 5: Tổng chi tiêu
                            rs.getString("maKH")              // Cột 6: Mã KH làm khóa phụ
                    };
                    list.add(row);
                }
            }
        }
        return list;
    }
    
    /**
     * Tra cứu một khách hàng theo mã hoặc số điện thoại chính xác
     */
    public KhachHang findByIdOrPhone(String keyword) throws SQLException {
        String sql = "SELECT maKH, hoTen, soDienThoai, hangTV, diemTichLuy FROM dbo.KhachHang " +
                     "WHERE maKH = ? OR soDienThoai = ?";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, keyword);
            stmt.setString(2, keyword);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hangStr = rs.getString("hangTV");
                    HangTV hang = (hangStr != null) ? HangTV.valueOf(hangStr) : HangTV.THUONG;

                    return new KhachHang(
                            rs.getString("maKH"),
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
                            hang,
                            rs.getInt("diemTichLuy")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Thêm mới một khách hàng
     */
    public boolean insertKhachHang(KhachHang kh) throws SQLException {
        String sql = "INSERT INTO dbo.KhachHang (maKH, hoTen, soDienThoai, hangTV, diemTichLuy) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, kh.getMaKH());
            stmt.setString(2, kh.getHoTen());
            stmt.setString(3, kh.getSoDienThoai());
            // Lưu chuỗi tên của Enum vào cột VARCHAR(12)
            stmt.setString(4, kh.getHangTV() != null ? kh.getHangTV().name() : HangTV.THUONG.name());
            stmt.setInt(5, kh.getDiemTichLuy());
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Cập nhật thông tin khách hàng
     */
    public boolean updateKhachHang(KhachHang kh) throws SQLException {
        String sql = "UPDATE dbo.KhachHang SET hoTen = ?, soDienThoai = ?, hangTV = ?, diemTichLuy = ? " +
                     "WHERE maKH = ?";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, kh.getHoTen());
            stmt.setString(2, kh.getSoDienThoai());
            stmt.setString(3, kh.getHangTV() != null ? kh.getHangTV().name() : HangTV.THUONG.name());
            stmt.setInt(4, kh.getDiemTichLuy());
            stmt.setString(5, kh.getMaKH());
            return stmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Cập nhật điểm tích lũy
     */
    public boolean updateDiemTichLuy(String maKH, int diemMoi) throws SQLException {
        String sql = "UPDATE dbo.KhachHang SET diemTichLuy = ? WHERE maKH = ?";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, diemMoi);
            stmt.setString(2, maKH);
            return stmt.executeUpdate() > 0;
        }
    }
    
}
