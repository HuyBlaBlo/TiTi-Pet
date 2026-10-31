package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/** UC002: transaction exchange or refund; SQL Server 2012-compatible JDBC. */
public final class DoiTraGiaoDichDAO {
    public record KetQuaLuu(String maDoiTra, String maHoaDonPhuThu,
                             BigDecimal tienTraThem) {}
    public record KetQuaTra(String maDoiTra, BigDecimal tienHoan) {}

    private record Goc(String maKH, BigDecimal giaCu, String maCaGoc) {}

    public KetQuaLuu luuDoiHang(String maHD, String maSPCu, String maSPMoi,
            String maNV, BigDecimal chenhDuKien, String phuongThuc,
            boolean daThu, boolean tinhTrangDat, boolean nhomDat) throws SQLException {
        return (KetQuaLuu) giaoDich(false, maHD, maSPCu, maSPMoi, maNV,
                chenhDuKien, phuongThuc, daThu, tinhTrangDat, nhomDat);
    }

    public KetQuaTra luuTraHang(String maHD, String maSPCu, String maNV,
            BigDecimal tienHoanDuKien, String phuongThucHoan, boolean daHoan,
            boolean tinhTrangDat, boolean nhomDat) throws SQLException {
        return (KetQuaTra) giaoDich(true, maHD, maSPCu, null, maNV,
                tienHoanDuKien, phuongThucHoan, daHoan, tinhTrangDat, nhomDat);
    }

    private Object giaoDich(boolean tra, String maHD, String cu, String moi,
            String nv, BigDecimal duKien, String phuongThuc, boolean daXuLy,
            boolean tinhTrang, boolean nhomDat) throws SQLException {
        // Never allow commit accidentally. Only an explicit local TEST switch can enable it.
        boolean dryRun = Boolean.getBoolean("titipet.uc002.rollbackOnly");
        
        if (!tinhTrang || !nhomDat)
            throw new SQLException("Chưa xác nhận tình trạng và nhóm sản phẩm.");
        if (maHD == null || cu == null || nv == null || maHD.isBlank()
                || cu.isBlank() || nv.isBlank())
            throw new SQLException("Thiếu mã hóa đơn, sản phẩm hoặc nhân viên.");
        if (!tra && (moi == null || moi.isBlank() || cu.equals(moi)))
            throw new SQLException("Phải chọn hàng thay thế khác hàng cũ.");
        if (!"TIEN_MAT".equals(phuongThuc) && !"CHUYEN_KHOAN".equals(phuongThuc))
            throw new SQLException("Phương thức thanh toán phải là tiền mặt hoặc chuyển khoản.");
        if (LocalTime.now().isBefore(LocalTime.NOON))
            throw new SQLException("Chỉ tiếp nhận đổi/trả sau 12 giờ trưa.");
        synchronized (ConnectDB.class) {
            Connection c = ConnectDB.getConnection();
            if (c == null || c.isClosed()) {
                ConnectDB.getInstance().connect();
                c = ConnectDB.getConnection();
            }
            if (c == null || c.isClosed()) throw new SQLException("Chưa kết nối SQL Server.");
            
String tenDatabase;

try (Statement st = c.createStatement();
     ResultSet rs = st.executeQuery("SELECT DB_NAME()")) {

    if (!rs.next()) {
        throw new SQLException(
                "Không xác định được database JDBC."
        );
    }

    tenDatabase = rs.getString(1);
}

System.out.println(
        "UC002 - JDBC Database: " + tenDatabase
);



            boolean oldAuto = c.getAutoCommit();
            int oldIsolation = c.getTransactionIsolation();
            try {
                c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                c.setAutoCommit(false);
                String maCa = caHienTai(c, nv);
                Goc goc = kiemTraGoc(c, maHD, cu);
                if (daDoiTra(c, maHD, cu))
                    throw new SQLException("Dòng sản phẩm này đã được đổi/trả một lần.");
                BigDecimal chenh = BigDecimal.ZERO;
                BigDecimal hoan = BigDecimal.ZERO;
                if (tra) {
                    hoan = goc.giaCu();
                    if (!daXuLy && hoan.signum() > 0)
                        throw new SQLException("Chưa xác nhận đã hoàn tiền cho khách.");
                } else {
                    BigDecimal giaMoi = giaMoiTrongKho(c, moi);
                    chenh = giaMoi.subtract(goc.giaCu());
                    if (chenh.signum() < 0)
                        throw new SQLException("Giá hàng thay thế phải bằng hoặc cao hơn hàng cũ.");
                    if (!daXuLy && chenh.signum() > 0)
                        throw new SQLException("Chưa xác nhận đã thu phần chênh lệch.");
                }
                BigDecimal tien = tra ? hoan : chenh;
                if (duKien == null || tien.compareTo(duKien) != 0)
                    throw new SQLException("Số tiền đã đổi. Vui lòng tra cứu và báo giá lại.");

                // The original item is physically accepted back in saleable condition.
                int count = execute(c, "UPDATE dbo.SanPham SET soLuongTonKho=soLuongTonKho+1 WHERE maSanPham=?", cu);
                if (count != 1) throw new SQLException("Không thể nhận lại hàng cũ.");
                if (!tra) {
                    count = execute(c, "UPDATE dbo.SanPham SET soLuongTonKho=soLuongTonKho-1 WHERE maSanPham=? AND soLuongTonKho>0", moi);
                    if (count != 1) throw new SQLException("Sản phẩm mới không còn tồn kho.");
                }

                String maHDDC = null;
                if (!tra && chenh.signum() > 0) {
                    maHDDC = "HDD" + maMoi(24);
                    // Only the supplementary amount is reported in this invoice.
                    // A linked line and DoiTra.maHoaDonDieuChinh provide audit traceability.
                    try (PreparedStatement ps = c.prepareStatement("""
                        INSERT INTO dbo.HoaDon
                        (maHoaDon, maKH, maNV, maCa, nguonDon,
                         phuongThucThanhToan, trangThai, tongTienHang,
                         tienPhaiThu, tongTienTruocThue, tongTien)
                        VALUES (?,?,?,?, 'TAI_QUAY',?, 'DA_THANH_TOAN',?,?,?,?)
                        """)) {
                        ps.setString(1, maHDDC);
                        ps.setString(2, goc.maKH());
                        ps.setString(3, nv);
                        ps.setString(4, maCa);
                        ps.setString(5, phuongThuc);
                        for (int i = 6; i <= 9; i++) ps.setBigDecimal(i, chenh);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = c.prepareStatement("""
                        INSERT INTO dbo.ChiTietHoaDon_SanPham
                        (maHoaDon, maSanPham, soLuong, donGia,
                         thueSuat, giaDaBaoGomThue, tienTruocThue, tienThue)
                        VALUES (?,?,1,?,0,0,?,0)
                        """)) {
                        ps.setString(1, maHDDC);
                        ps.setString(2, moi);
                        ps.setBigDecimal(3, chenh);
                        ps.setBigDecimal(4, chenh);
                        ps.executeUpdate();
                    }
                }

                String maDoiTra = "DT" + maMoi(18);
                try (PreparedStatement ps = c.prepareStatement("""
                    INSERT INTO dbo.DoiTra
                    (maDoiTra, maHoaDon, maSanPhamCu, maSanPhamMoi,
                     maNV, chenhLech, ghiChu, loaiXuLy, tienHoan,
                     maCa, maHoaDonDieuChinh, phuongThucXuLy, soLuongXuLy)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,1)
                    """)) {
                    ps.setString(1, maDoiTra);
                    ps.setString(2, maHD);
                    ps.setString(3, cu);
                    ps.setString(4, moi);
                    ps.setString(5, nv);
                    ps.setBigDecimal(6, chenh);
                    ps.setString(7, tra ? "Trả hàng, đã hoàn tiền theo xác nhận Thu ngân"
                        : "Đổi hàng, đã nhận chênh lệch theo xác nhận Thu ngân");
                    ps.setString(8, tra ? "TRA" : "DOI");
                    ps.setBigDecimal(9, hoan);
                    ps.setString(10, maCa);
                    ps.setString(11, maHDDC);
                    ps.setString(12, phuongThuc);
                    ps.executeUpdate();
                }

                if (dryRun) c.rollback();
                else c.commit();
                return tra ? new KetQuaTra(dryRun ? "DRYRUN_" + maDoiTra : maDoiTra, hoan)
                     : new KetQuaLuu(dryRun ? "DRYRUN_" + maDoiTra : maDoiTra, maHDDC, chenh);
            } catch (Exception e) {
                try { c.rollback(); } catch (SQLException rb) { e.addSuppressed(rb); }
                if (e instanceof SQLException se) throw se;
                throw new SQLException("UC002 thất bại: " + e.getMessage(), e);
            } finally {
                try { c.setAutoCommit(oldAuto); }
                finally { c.setTransactionIsolation(oldIsolation); }
            }
        }
    }

    private static String caHienTai(Connection c, String nv) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
            SELECT TOP 1 c.maCa
            FROM dbo.CaLamViec c WITH (UPDLOCK,HOLDLOCK)
            JOIN dbo.NhanVien n ON n.maNV=c.maNV
            WHERE n.maNV=? AND n.vaiTro='THU_NGAN'
              AND n.dangHoatDong=1 AND c.trangThai='DANG_MO'
            ORDER BY c.thoiGianBatDau DESC
            """)) {
            ps.setString(1,nv);
            try (ResultSet rs=ps.executeQuery()) {
                if(!rs.next()) throw new SQLException("Thu ngân không có ca đang mở hoặc không có quyền.");
                return rs.getString(1);
            }
        }
    }

    private static Goc kiemTraGoc(Connection c, String hd, String cu) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
            SELECT h.maKH,h.ngayLap,h.nguonDon,h.maCa,ct.soLuong,ct.donGia,
              ct.tienGiamPhanBo,ct.tienThue,h.tongTienThue,h.tienGiamKM,
              h.tienGiamHangTV,h.tienGiamDiem,h.maKhuyenMai,s.loaiSanPham
            FROM dbo.HoaDon h WITH (UPDLOCK,HOLDLOCK)
            JOIN dbo.ChiTietHoaDon_SanPham ct WITH (UPDLOCK,HOLDLOCK)
              ON ct.maHoaDon=h.maHoaDon
            JOIN dbo.SanPham s ON s.maSanPham=ct.maSanPham
            WHERE h.maHoaDon=? AND h.trangThai='DA_THANH_TOAN' AND ct.maSanPham=?
            """)) {
            ps.setString(1,hd); ps.setString(2,cu);
            try (ResultSet rs=ps.executeQuery()) {
                if(!rs.next()) throw new SQLException("Hóa đơn hoặc sản phẩm gốc không hợp lệ.");
                if (rs.getString("maCa")==null || !"TAI_QUAY".equals(rs.getString("nguonDon")))
                    throw new SQLException("UC002 hiện chỉ xử lý hóa đơn POS tại quầy có mã ca.");
                if (rs.getInt("soLuong") != 1 || !"PHU_KIEN".equals(rs.getString("loaiSanPham")))
                    throw new SQLException("Chỉ nhận đổi/trả một phụ kiện mỗi dòng hóa đơn.");
                if (rs.getBigDecimal("tienGiamPhanBo").signum()!=0
                    || rs.getBigDecimal("tienThue").signum()!=0
                    || rs.getBigDecimal("tongTienThue").signum()!=0
                    || rs.getBigDecimal("tienGiamKM").signum()!=0
                    || rs.getBigDecimal("tienGiamHangTV").signum()!=0
                    || rs.getBigDecimal("tienGiamDiem").signum()!=0
                    || rs.getString("maKhuyenMai")!=null)
                    throw new SQLException("Hóa đơn có thuế/giảm giá cần phân bổ tiền hoàn; bản này chưa hỗ trợ.");
                LocalDateTime ngay=rs.getTimestamp("ngayLap").toLocalDateTime();
                if (ngay.isAfter(LocalDateTime.now()) || ngay.isBefore(LocalDateTime.now().minusHours(24)))
                    throw new SQLException("Đã quá 24 giờ theo điều kiện ghi trên hóa đơn mẫu.");
                return new Goc(rs.getString("maKH"),rs.getBigDecimal("donGia"),rs.getString("maCa"));
            }
        }
    }

    private static boolean daDoiTra(Connection c, String hd, String cu) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
            SELECT 1 FROM dbo.DoiTra WITH (UPDLOCK,HOLDLOCK)
            WHERE maHoaDon=? AND maSanPhamCu=?
            """)) {
            ps.setString(1,hd); ps.setString(2,cu);
            try (ResultSet rs=ps.executeQuery()) { return rs.next(); }
        }
    }

    private static BigDecimal giaMoiTrongKho(Connection c,String maSP) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("""
            SELECT giaBan,loaiSanPham,soLuongTonKho,dangKinhDoanh
            FROM dbo.SanPham WITH (UPDLOCK,HOLDLOCK) WHERE maSanPham=?
            """)) {
            ps.setString(1,maSP);
            try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next() || !"PHU_KIEN".equals(rs.getString("loaiSanPham"))
                    || !rs.getBoolean("dangKinhDoanh") || rs.getInt("soLuongTonKho")<1)
                    throw new SQLException("Sản phẩm thay thế không đủ điều kiện hoặc hết kho.");
                return rs.getBigDecimal("giaBan");
            }
        }
    }

    private static int execute(Connection c,String sql,String id) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,id);return ps.executeUpdate();
        }
    }
    private static String maMoi(int len) {
        return UUID.randomUUID().toString().replace("-","")
            .substring(0,len).toUpperCase();
    }
}
