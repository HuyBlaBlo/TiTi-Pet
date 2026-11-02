package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.DichVuSpa;
import com.titipet.entity.PhuKien;
import com.titipet.entity.SanPham;
import com.titipet.entity.ThucAn;
import com.titipet.entity.enums.LoaiSanPham;
import com.titipet.entity.enums.LoaiThuCungApDung;

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
	/**
     * Lấy danh sách toàn bộ sản phẩm đang kinh doanh kèm thông tin bảng con
     */
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

    /**
     * Lấy danh sách Dịch vụ Spa trực tiếp qua Entity DichVuSpa
     */
    public List<DichVuSpa> layDanhSachDichVuSpa() throws SQLException {
        List<DichVuSpa> list = new ArrayList<>();
        String sql = "SELECT maDichVu, tenDichVu, donGiaCoBan, thueSuat, giaDaBaoGomThue, loaiThuCungApDung, dangHoatDong "
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
                DichVuSpa dv = new DichVuSpa();
                dv.setMaDichVu(rs.getString("maDichVu"));
                dv.setTenDichVu(rs.getString("tenDichVu"));
                dv.setDonGiaCoBan(rs.getBigDecimal("donGiaCoBan"));
                dv.setThueSuat(rs.getBigDecimal("thueSuat"));
                dv.setGiaDaBaoGomThue(rs.getBoolean("giaDaBaoGomThue"));
                dv.setDangHoatDong(rs.getBoolean("dangHoatDong"));

                String loaiStr = rs.getString("loaiThuCungApDung");
                try {
                    dv.setLoaiThuCungApDung(LoaiThuCungApDung.valueOf(loaiStr));
                } catch (Exception e) {
                    dv.setLoaiThuCungApDung(LoaiThuCungApDung.CA_HAI);
                }

                // Gán thời lượng mặc định hiển thị theo tên dịch vụ
                int thoiGian = 60;
                String tenLower = dv.getTenDichVu().toLowerCase();
                if (tenLower.contains("cắt móng")) thoiGian = 30;
                else if (tenLower.contains("cắt tỉa")) thoiGian = 45;
                else if (tenLower.contains("tắm sấy")) thoiGian = 90;
                dv.setThoiLuongPhut(thoiGian);

                list.add(dv);
            }
        }
        return list;
    }

    /**
     * Tự động sinh mã sản phẩm tiếp theo: TAxxx cho Thức ăn, PKxxx cho Phụ kiện
     */
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

    /**
     * Thêm mới sản phẩm vào CSDL kèm bảng con
     */
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

    /**
     * Cập nhật thông tin sản phẩm
     */
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
            System.out.println("=== TEST SanPhamDAO (DUNG DichVuSpa ENTITY) ===");
            ConnectDB.getInstance().connect();
            SanPhamDAO dao = new SanPhamDAO();

            List<DichVuSpa> dsSpa = dao.layDanhSachDichVuSpa();
            System.out.println("Tổng số dịch vụ Spa: " + dsSpa.size());
            for (DichVuSpa dv : dsSpa) {
                System.out.printf("  [%s] %-25s | Đơn giá: %-10s | Áp dụng: %s | Thời lượng: %d phút\n",
                        dv.getMaDichVu(), dv.getTenDichVu(), dv.getDonGiaCoBan(), dv.getLoaiThuCungApDung(), dv.getThoiLuongPhut());
            }

            System.out.println("=== TEST THÀNH CÔNG ===");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}