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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO {

    public static class DichVuSpaItem {
        private String maDV;
        private String tenDV;
        private String loaiThuCung;
        private BigDecimal donGiaCoBan;
        private int thoiGianPhut;
        private String cachTinh;

        public DichVuSpaItem(String maDV, String tenDV, String loaiThuCung, BigDecimal donGiaCoBan, int thoiGianPhut, String cachTinh) {
            this.maDV = maDV;
            this.tenDV = tenDV;
            this.loaiThuCung = loaiThuCung;
            this.donGiaCoBan = donGiaCoBan;
            this.thoiGianPhut = thoiGianPhut;
            this.cachTinh = cachTinh;
        }

        public String getMaDV() { return maDV; }
        public String getTenDV() { return tenDV; }
        public String getLoaiThuCung() { return loaiThuCung; }
        public BigDecimal getDonGiaCoBan() { return donGiaCoBan; }
        public int getThoiGianPhut() { return thoiGianPhut; }
        public String getCachTinh() { return cachTinh; }
    }

    public List<SanPham> layDanhSachSanPham() throws SQLException {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT sp.maSanPham, sp.tenSanPham, sp.giaBan, sp.giaNhap, sp.soLuongTonKho, "
                   + "       sp.maVach, sp.nguongCanhBaoTon, sp.thueSuat, sp.loaiSanPham, "
                   + "       ta.hanSuDung, ta.trongLuong, "
                   + "       pk.kichThuoc, pk.mauSac, pk.chatLieu "
                   + "FROM dbo.SanPham sp "
                   + "LEFT JOIN dbo.ThucAn ta ON sp.maSanPham = ta.maSanPham "
                   + "LEFT JOIN dbo.PhuKien pk ON sp.maSanPham = pk.maSanPham "
                   + "WHERE sp.dangKinhDoanh = 1 "
                   + "ORDER BY sp.maSanPham ASC";

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getInstance().getConnection();
        }

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maSanPham");
                String ten = rs.getString("tenSanPham");
                BigDecimal giaBan = rs.getBigDecimal("giaBan");
                BigDecimal giaNhap = rs.getBigDecimal("giaNhap");
                int ton = rs.getInt("soLuongTonKho");
                String maVach = rs.getString("maVach");
                int nguong = rs.getInt("nguongCanhBaoTon");
                BigDecimal thue = rs.getBigDecimal("thueSuat");
                String loaiStr = rs.getString("loaiSanPham");

                if ("THUC_AN".equalsIgnoreCase(loaiStr)) {
                    ThucAn ta = new ThucAn();
                    ta.setMaSanPham(ma);
                    ta.setTenSanPham(ten);
                    ta.setGiaBan(giaBan);
                    ta.setGiaNhap(giaNhap);
                    ta.setSoLuongTonKho(ton);
                    ta.setMaVach(maVach);
                    ta.setNguongCanhBaoTon(nguong);
                    ta.setThueSuat(thue);
                    ta.setLoaiSanPham(LoaiSanPham.THUC_AN);

                    java.sql.Date d = rs.getDate("hanSuDung");
                    if (d != null) {
                        ta.setHanSuDung(d.toLocalDate());
                    }
                    ta.setTrongLuong(rs.getDouble("trongLuong"));
                    list.add(ta);
                } else {
                    PhuKien pk = new PhuKien();
                    pk.setMaSanPham(ma);
                    pk.setTenSanPham(ten);
                    pk.setGiaBan(giaBan);
                    pk.setGiaNhap(giaNhap);
                    pk.setSoLuongTonKho(ton);
                    pk.setMaVach(maVach);
                    pk.setNguongCanhBaoTon(nguong);
                    pk.setThueSuat(thue);
                    pk.setLoaiSanPham(LoaiSanPham.PHU_KIEN);
                    pk.setKichThuoc(rs.getString("kichThuoc"));
                    pk.setMauSac(rs.getString("mauSac"));
                    pk.setChatLieu(rs.getString("chatLieu"));
                    list.add(pk);
                }
            }
        }
        return list;
    }

    public List<DichVuSpaItem> layDanhSachDichVuSpa() throws SQLException {
        List<DichVuSpaItem> list = new ArrayList<>();
        String sql = "SELECT maDichVu, tenDichVu, loaiThuCungApDung, donGiaCoBan "
                   + "FROM dbo.DichVuSpa "
                   + "WHERE dangHoatDong = 1 "
                   + "ORDER BY maDichVu ASC";

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getInstance().getConnection();
        }

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maDichVu");
                String ten = rs.getString("tenDichVu");
                String loai = rs.getString("loaiThuCungApDung");
                BigDecimal gia = rs.getBigDecimal("donGiaCoBan");

                int thoiGian = 60;
                if (ten.toLowerCase().contains("cắt móng")) thoiGian = 30;
                else if (ten.toLowerCase().contains("cắt tỉa lông")) thoiGian = 45;
                else if (ten.toLowerCase().contains("tắm")) thoiGian = 60;

                String loaiHienThi = ("CA_HAI".equalsIgnoreCase(loai) || loai == null) ? "Tất cả" : loai;
                String cachTinh = "+50.000đ (5-10kg), +100.000đ (>10kg)";

                list.add(new DichVuSpaItem(ma, ten, loaiHienThi, gia, thoiGian, cachTinh));
            }
        }
        return list;
    }

    public String taoMaSanPhamMoi(LoaiSanPham loai) throws SQLException {
        String prefix = (loai == LoaiSanPham.THUC_AN) ? "TA" : "PK";
        String regex = "^" + prefix + "\\d+$";
        String sql = "SELECT maSanPham FROM dbo.SanPham WHERE loaiSanPham = ?";

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getInstance().getConnection();
        }

        int maxIndex = 0;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loai.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maSanPham");
                    if (ma != null && ma.matches(regex)) {
                        try {
                            int num = Integer.parseInt(ma.substring(2));
                            if (num > maxIndex) {
                                maxIndex = num;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return String.format("%s%03d", prefix, maxIndex + 1);
    }

    public boolean themSanPham(SanPham sp, LocalDateTime hanSuDung) throws SQLException {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getInstance().getConnection();
        }
        boolean autoCommitGoc = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            String sqlSP = "INSERT INTO dbo.SanPham (maSanPham, tenSanPham, giaBan, giaNhap, soLuongTonKho, maVach, nguongCanhBaoTon, loaiSanPham, dangKinhDoanh) "
                         + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
            try (PreparedStatement ps = con.prepareStatement(sqlSP)) {
                ps.setString(1, sp.getMaSanPham());
                ps.setString(2, sp.getTenSanPham());
                ps.setBigDecimal(3, sp.getGiaBan());
                ps.setBigDecimal(4, sp.getGiaNhap() != null ? sp.getGiaNhap() : BigDecimal.ZERO);
                ps.setInt(5, sp.getSoLuongTonKho());
                ps.setString(6, sp.getMaVach());
                ps.setInt(7, sp.getNguongCanhBaoTon());
                ps.setString(8, sp.getLoaiSanPham().name());
                ps.executeUpdate();
            }

            if (sp.getLoaiSanPham() == LoaiSanPham.THUC_AN) {
                String sqlTA = "INSERT INTO dbo.ThucAn (maSanPham, hanSuDung, trongLuong) VALUES (?, ?, ?)";
                try (PreparedStatement psTA = con.prepareStatement(sqlTA)) {
                    psTA.setString(1, sp.getMaSanPham());
                    LocalDate d = (hanSuDung != null) ? hanSuDung.toLocalDate() : LocalDate.now().plusMonths(12);
                    psTA.setDate(2, java.sql.Date.valueOf(d));
                    psTA.setDouble(3, 1.0);
                    psTA.executeUpdate();
                }
            } else {
                String sqlPK = "INSERT INTO dbo.PhuKien (maSanPham, kichThuoc, mauSac, chatLieu) VALUES (?, N'Tiêu chuẩn', N'Đa sắc', N'Vải/Nhựa')";
                try (PreparedStatement psPK = con.prepareStatement(sqlPK)) {
                    psPK.setString(1, sp.getMaSanPham());
                    psPK.executeUpdate();
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

    public boolean capNhatSanPham(SanPham sp, LocalDateTime hanSuDungMoi) throws SQLException {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getInstance().getConnection();
        }
        boolean autoCommitGoc = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            String sqlSP = "UPDATE dbo.SanPham SET tenSanPham = ?, giaBan = ?, maVach = ?, soLuongTonKho = ? WHERE maSanPham = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlSP)) {
                ps.setString(1, sp.getTenSanPham());
                ps.setBigDecimal(2, sp.getGiaBan());
                ps.setString(3, sp.getMaVach());
                ps.setInt(4, sp.getSoLuongTonKho());
                ps.setString(5, sp.getMaSanPham());
                ps.executeUpdate();
            }

            if (sp instanceof ThucAn && hanSuDungMoi != null) {
                String sqlTA = "UPDATE dbo.ThucAn SET hanSuDung = ? WHERE maSanPham = ?";
                try (PreparedStatement psTA = con.prepareStatement(sqlTA)) {
                    psTA.setDate(1, java.sql.Date.valueOf(hanSuDungMoi.toLocalDate()));
                    psTA.setString(2, sp.getMaSanPham());
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

    public static void main(String[] args) {
        try {
            System.out.println("=== TEST SanPhamDAO (SCHEMA CHÍNH THỨC) ===");
            ConnectDB.getInstance().connect();
            SanPhamDAO dao = new SanPhamDAO();

            String maTA = dao.taoMaSanPhamMoi(LoaiSanPham.THUC_AN);
            String maPK = dao.taoMaSanPhamMoi(LoaiSanPham.PHU_KIEN);
            System.out.println("Mã Thức ăn tiếp theo : " + maTA);
            System.out.println("Mã Phụ kiện tiếp theo: " + maPK);

            List<SanPham> ds = dao.layDanhSachSanPham();
            System.out.println("Danh sách sản phẩm hiện có: " + ds.size());
            for (SanPham sp : ds) {
                System.out.printf("  [%-5s] %-25s | Giá bán: %-10s | Tồn: %d\n",
                        sp.getMaSanPham(), sp.getTenSanPham(), sp.getGiaBan(), sp.getSoLuongTonKho());
            }

            System.out.println("=== TEST THÀNH CÔNG ===");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}