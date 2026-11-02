package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.KhuyenMai;
import com.titipet.entity.enums.PhamVi;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {

    private Connection getActiveConnection() throws SQLException {
        Connection con = ConnectDB.getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }
        return con;
    }

    /**
     * Lấy toàn bộ danh sách chương trình khuyến mãi
     */
    public List<KhuyenMai> layTatCaKhuyenMai() throws SQLException {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT maKhuyenMai, tenChuongTrinh, ngayBatDau, ngayKetThuc, giamTram, trangThai, phamVi, maSanPhamApDung, maDichVuApDung "
                   + "FROM dbo.KhuyenMai ORDER BY maKhuyenMai DESC";

        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Thêm mới một chương trình khuyến mãi (sử dụng cột giamTram)
     */
    public boolean themKhuyenMai(KhuyenMai km) throws SQLException {
        String sql = "INSERT INTO dbo.KhuyenMai (maKhuyenMai, tenChuongTrinh, ngayBatDau, ngayKetThuc, giamTram, trangThai, phamVi, maSanPhamApDung, maDichVuApDung) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
            ps.setString(1, km.getMaKhuyenMai());
            ps.setString(2, km.getTenChuongTrinh());
            ps.setTimestamp(3, km.getNgayBatDau() != null ? Timestamp.valueOf(km.getNgayBatDau()) : null);
            ps.setTimestamp(4, km.getNgayKetThuc() != null ? Timestamp.valueOf(km.getNgayKetThuc()) : null);
            ps.setBigDecimal(5, km.getGiamTram());
            ps.setBoolean(6, km.isTrangThai());
            ps.setString(7, km.getPhamVi() != null ? km.getPhamVi().name() : PhamVi.TOAN_DON.name());
            ps.setString(8, km.getSanPhamApDung() != null ? km.getSanPhamApDung().getMaSanPham() : null);
            ps.setString(9, km.getDichVuApDung() != null ? km.getDichVuApDung().getMaDichVu() : null);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin chương trình khuyến mãi
     */
    public boolean capNhatKhuyenMai(KhuyenMai km) throws SQLException {
        String sql = "UPDATE dbo.KhuyenMai SET tenChuongTrinh = ?, ngayBatDau = ?, ngayKetThuc = ?, "
                   + "giamTram = ?, trangThai = ?, phamVi = ?, maSanPhamApDung = ?, maDichVuApDung = ? "
                   + "WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
            ps.setString(1, km.getTenChuongTrinh());
            ps.setTimestamp(2, km.getNgayBatDau() != null ? Timestamp.valueOf(km.getNgayBatDau()) : null);
            ps.setTimestamp(3, km.getNgayKetThuc() != null ? Timestamp.valueOf(km.getNgayKetThuc()) : null);
            ps.setBigDecimal(4, km.getGiamTram());
            ps.setBoolean(5, km.isTrangThai());
            ps.setString(6, km.getPhamVi() != null ? km.getPhamVi().name() : PhamVi.TOAN_DON.name());
            ps.setString(7, km.getSanPhamApDung() != null ? km.getSanPhamApDung().getMaSanPham() : null);
            ps.setString(8, km.getDichVuApDung() != null ? km.getDichVuApDung().getMaDichVu() : null);
            ps.setString(9, km.getMaKhuyenMai());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Chuyển đổi trạng thái Bật / Tắt khuyến mãi
     */
    public boolean chuyenTrangThai(String maKM, boolean trangThaiMoi) throws SQLException {
        String sql = "UPDATE dbo.KhuyenMai SET trangThai = ? WHERE maKhuyenMai = ?";
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
            ps.setBoolean(1, trangThaiMoi);
            ps.setString(2, maKM);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Tự động sinh mã mới dạng KMxxx (hoặc KM_TEST_xxx nếu có)
     */
    public String sinhMaKhuyenMaiMoi() throws SQLException {
        String sql = "SELECT TOP 1 maKhuyenMai FROM dbo.KhuyenMai WHERE maKhuyenMai LIKE 'KM%' ORDER BY maKhuyenMai DESC";
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maMax = rs.getString(1);
                try {
                    // Xử lý cả dạng KM001 lẫn KM_TEST_xxx
                    String so = maMax.replaceAll("\\D+", "");
                    if (!so.isEmpty()) {
                        int stt = Integer.parseInt(so);
                        return String.format("KM%03d", stt + 1);
                    }
                } catch (Exception ignored) {}
            }
        }
        return "KM001";
    }

    private KhuyenMai mapResultSet(ResultSet rs) throws SQLException {
        KhuyenMai km = new KhuyenMai();
        km.setMaKhuyenMai(rs.getString("maKhuyenMai")); 
        km.setTenChuongTrinh(rs.getString("tenChuongTrinh")); 

        Timestamp tsBD = rs.getTimestamp("ngayBatDau");
        if (tsBD != null) km.setNgayBatDau(tsBD.toLocalDateTime());

        Timestamp tsKT = rs.getTimestamp("ngayKetThuc");
        if (tsKT != null) km.setNgayKetThuc(tsKT.toLocalDateTime());

        km.setGiamTram(rs.getBigDecimal("giamTram")); 
        km.setTrangThai(rs.getBoolean("trangThai"));

        String strPhamVi = rs.getString("phamVi");
        if (strPhamVi != null) {
            try {
                km.setPhamVi(PhamVi.valueOf(strPhamVi.trim()));
            } catch (Exception e) {
                km.setPhamVi(PhamVi.TOAN_DON);
            }
        } else {
            km.setPhamVi(PhamVi.TOAN_DON);
        }

        return km;
    } 

}