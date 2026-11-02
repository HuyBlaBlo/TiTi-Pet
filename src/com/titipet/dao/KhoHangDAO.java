package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.PhuKien;
import com.titipet.entity.SanPham;
import com.titipet.entity.ThucAn;
import com.titipet.entity.enums.LoaiSanPham;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhoHangDAO {

    public List<SanPham> layDanhSachSanPhamKho(String keyword) throws SQLException {
        List<SanPham> danhSach = new ArrayList<>();
        String sql = "SELECT sp.maSanPham, sp.tenSanPham, sp.giaBan, sp.giaNhap, sp.soLuongTonKho, "
                   + "       sp.maVach, sp.nguongCanhBaoTon, sp.thueSuat, sp.loaiSanPham, "
                   + "       ta.hanSuDung, ta.trongLuong, "
                   + "       pk.kichThuoc, pk.mauSac, pk.chatLieu "
                   + "FROM dbo.SanPham sp "
                   + "LEFT JOIN dbo.ThucAn ta ON sp.maSanPham = ta.maSanPham "
                   + "LEFT JOIN dbo.PhuKien pk ON sp.maSanPham = pk.maSanPham "
                   + "WHERE sp.dangKinhDoanh = 1 ";

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND (sp.maSanPham LIKE ? OR sp.tenSanPham LIKE ? OR sp.maVach LIKE ?) ";
        }
        sql += " ORDER BY sp.maSanPham ASC";

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getInstance().getConnection();
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim() + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                ps.setString(3, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maSP = rs.getString("maSanPham");
                    String tenSP = rs.getString("tenSanPham");
                    BigDecimal giaBan = rs.getBigDecimal("giaBan");
                    BigDecimal giaNhap = rs.getBigDecimal("giaNhap");
                    int tonKho = rs.getInt("soLuongTonKho");
                    String maVach = rs.getString("maVach");
                    int nguong = rs.getInt("nguongCanhBaoTon");
                    BigDecimal thue = rs.getBigDecimal("thueSuat");
                    String strLoai = rs.getString("loaiSanPham");

                    LoaiSanPham loai = "THUC_AN".equalsIgnoreCase(strLoai) ? LoaiSanPham.THUC_AN : LoaiSanPham.PHU_KIEN;

                    if (loai == LoaiSanPham.THUC_AN) {
                        ThucAn ta = new ThucAn();
                        ta.setMaSanPham(maSP);
                        ta.setTenSanPham(tenSP);
                        ta.setGiaBan(giaBan);
                        ta.setGiaNhap(giaNhap);
                        ta.setSoLuongTonKho(tonKho);
                        ta.setMaVach(maVach);
                        ta.setNguongCanhBaoTon(nguong);
                        ta.setThueSuat(thue);
                        ta.setLoaiSanPham(LoaiSanPham.THUC_AN);

                        java.sql.Date d = rs.getDate("hanSuDung");
                        if (d != null) {
                            ta.setHanSuDung(d.toLocalDate());
                        }
                        ta.setTrongLuong(rs.getDouble("trongLuong"));
                        danhSach.add(ta);
                    } else {
                        PhuKien pk = new PhuKien();
                        pk.setMaSanPham(maSP);
                        pk.setTenSanPham(tenSP);
                        pk.setGiaBan(giaBan);
                        pk.setGiaNhap(giaNhap);
                        pk.setSoLuongTonKho(tonKho);
                        pk.setMaVach(maVach);
                        pk.setNguongCanhBaoTon(nguong);
                        pk.setThueSuat(thue);
                        pk.setLoaiSanPham(LoaiSanPham.PHU_KIEN);
                        pk.setKichThuoc(rs.getString("kichThuoc"));
                        pk.setMauSac(rs.getString("mauSac"));
                        pk.setChatLieu(rs.getString("chatLieu"));
                        danhSach.add(pk);
                    }
                }
            }
        }
        return danhSach;
    }

    public boolean nhapKhoSanPham(String maSP, int soLuongNhapThem, BigDecimal giaNhapMoi, LocalDate hsdMoi) throws SQLException {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getInstance().getConnection();
        }

        boolean autoCommitGoc = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            // 1. Tăng số lượng tồn kho và cập nhật đơn giá nhập mới nhất
            String sqlSP = "UPDATE dbo.SanPham SET soLuongTonKho = soLuongTonKho + ?, giaNhap = ? WHERE maSanPham = ?";
            try (PreparedStatement psSP = con.prepareStatement(sqlSP)) {
                psSP.setInt(1, soLuongNhapThem);
                psSP.setBigDecimal(2, giaNhapMoi != null ? giaNhapMoi : BigDecimal.ZERO);
                psSP.setString(3, maSP);
                psSP.executeUpdate();
            }

            // 2. Nếu có HSD mới và sản phẩm là Thức ăn thì cập nhật bảng ThucAn
            if (hsdMoi != null) {
                String sqlTA = "UPDATE dbo.ThucAn SET hanSuDung = ? WHERE maSanPham = ?";
                try (PreparedStatement psTA = con.prepareStatement(sqlTA)) {
                    psTA.setDate(1, java.sql.Date.valueOf(hsdMoi));
                    psTA.setString(2, maSP);
                    psTA.executeUpdate();
                }
            }

            con.commit();
            return true;
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(autoCommitGoc);
        }
    }
}