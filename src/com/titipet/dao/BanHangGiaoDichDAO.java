
package com.titipet.dao;

import com.titipet.bus.BanHangService;
import com.titipet.bus.BanHangTinhTienService;
import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.KhachHangBanHang;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BanHangGiaoDichDAO {

    public String luuGiaoDich(
            String maKH, String maNV,
            String nguonDon, String phuongThuc,
            int diemSuDung,
            List<BanHangService.DongGioHang> gio,
            BigDecimal tienKhachDua) throws SQLException {

        Connection con = ConnectDB.getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }

        if (con == null || con.isClosed()) {
            throw new SQLException("Chưa kết nối SQL Server.");
        }

        // Sử dụng transaction chung. Không đóng kết nối
        // do ConnectDB đang quản lý.
        synchronized (ConnectDB.class) {
            boolean autoCommitCu = con.getAutoCommit();

            try {
                con.setAutoCommit(false);
                con.setTransactionIsolation(
                    Connection.TRANSACTION_READ_COMMITTED);

                KhachHangBanHang khach = layKhachHang(con, maKH);
                kiemTraNhanVien(con, maNV);

                BigDecimal tamTinh = BigDecimal.ZERO;

                // Khóa bản ghi sản phẩm để hạn chế bán vượt tồn.
                for (BanHangService.DongGioHang dong : gio) {
                    String maSP = dong.sanPham().maSanPham();
                    int soLuong = dong.soLuong();

                    if (soLuong <= 0) {
                        throw new SQLException("Số lượng mua không hợp lệ.");
                    }

                    String sql = """
                        SELECT s.giaBan, s.soLuongTonKho,
                               s.dangKinhDoanh, ta.hanSuDung
                        FROM dbo.SanPham s WITH (UPDLOCK, HOLDLOCK)
                        LEFT JOIN dbo.ThucAn ta
                          ON ta.maSanPham = s.maSanPham
                        WHERE s.maSanPham = ?
                        """;

                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, maSP);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException(
                                    "Không tìm thấy sản phẩm: " + maSP);
                            }

                            if (!rs.getBoolean("dangKinhDoanh")) {
                                throw new SQLException(
                                    "Sản phẩm đã ngừng kinh doanh: " + maSP);
                            }

                            if (rs.getInt("soLuongTonKho") < soLuong) {
                                throw new SQLException(
                                    "Không đủ tồn kho: " + maSP);
                            }

                            Date hsd = rs.getDate("hanSuDung");
                            if (hsd != null &&
                                    hsd.toLocalDate().isBefore(LocalDate.now())) {
                                throw new SQLException(
                                    "Sản phẩm đã hết hạn: " + maSP);
                            }

                            BigDecimal giaDB = rs.getBigDecimal("giaBan");

                            if (giaDB.compareTo(dong.sanPham().giaBan()) != 0) {
                                throw new SQLException(
                                    "Giá sản phẩm đã thay đổi. Vui lòng tải lại giỏ hàng.");
                            }

                            tamTinh = tamTinh.add(
                                giaDB.multiply(BigDecimal.valueOf(soLuong)));
                        }
                    }
                }

                BanHangTinhTienService tinhTien =
                    new BanHangTinhTienService();

                BanHangTinhTienService.KetQua ketQua =
                    tinhTien.tinhChinhXac(
                        tamTinh, khach, diemSuDung);

                if ("TIEN_MAT".equals(phuongThuc)) {
                    if (tienKhachDua == null ||
                            tienKhachDua.compareTo(ketQua.phaiThu()) < 0) {
                        throw new SQLException(
                            "Số tiền khách đưa chưa đủ thanh toán.");
                    }
                }

                int diemTich = tinhTien.tinhDiemDuocTich(
                    ketQua.phaiThu(),
                    khach,
                    diemSuDung,
                    false
                );

                String maHD = "HD" + UUID.randomUUID()
                        .toString().replace("-", "")
                        .substring(0, 24).toUpperCase();

                String maCa = layCaDangMo(con, maNV);

                String sqlHD = """
                    INSERT INTO dbo.HoaDon (
                        maHoaDon, maKH, maNV, maCa,
                        nguonDon, phuongThucThanhToan,
                        trangThai, tongTienHang, tongTienSpa,
                        tienGiamKM, tienGiamHangTV,
                        soDiemSuDung, tienGiamDiem,
                        tienPhaiThu, soDiemDuocTich,
                        tongTienTruocThue, tongTienThue, tongTien
                    )
                    VALUES (?, ?, ?, ?, ?, ?,
                            'DA_THANH_TOAN', ?, 0,
                            0, ?, ?, ?, ?, ?, ?, 0, ?)
                    """;

                try (PreparedStatement ps = con.prepareStatement(sqlHD)) {
                    ps.setString(1, maHD);
                    ps.setString(2, maKH);
                    ps.setString(3, maNV);
                    ps.setString(4, maCa);
                    ps.setString(5, nguonDon);
                    ps.setString(6, phuongThuc);
                    ps.setBigDecimal(7, tamTinh);
                    ps.setBigDecimal(8, ketQua.giamHang());
                    ps.setInt(9, diemSuDung);
                    ps.setBigDecimal(10, ketQua.giamDiem());
                    ps.setBigDecimal(11, ketQua.phaiThu());
                    ps.setInt(12, diemTich);
                    ps.setBigDecimal(13, tamTinh);
                    ps.setBigDecimal(14, ketQua.phaiThu());
                    ps.executeUpdate();
                }

                String sqlCT = """
                    INSERT INTO dbo.ChiTietHoaDon_SanPham
                    (maHoaDon, maSanPham, soLuong, donGia,
                     thueSuat, giaDaBaoGomThue,
                     tienGiamPhanBo, tienTruocThue, tienThue)
                    VALUES (?, ?, ?, ?, 0, 0, 0, ?, 0)
                    """;

                String sqlKho = """
                    UPDATE dbo.SanPham
                    SET soLuongTonKho = soLuongTonKho - ?
                    WHERE maSanPham = ? AND soLuongTonKho >= ?
                    """;

                try (PreparedStatement ct = con.prepareStatement(sqlCT);
                     PreparedStatement kho = con.prepareStatement(sqlKho)) {

                    for (BanHangService.DongGioHang dong : gio) {
                        var sp = dong.sanPham();

                        BigDecimal thanhTien = sp.giaBan().multiply(
                            BigDecimal.valueOf(dong.soLuong()));

                        ct.setString(1, maHD);
                        ct.setString(2, sp.maSanPham());
                        ct.setInt(3, dong.soLuong());
                        ct.setBigDecimal(4, sp.giaBan());
                        ct.setBigDecimal(5, thanhTien);
                        ct.executeUpdate();

                        kho.setInt(1, dong.soLuong());
                        kho.setString(2, sp.maSanPham());
                        kho.setInt(3, dong.soLuong());

                        if (kho.executeUpdate() != 1) {
                            throw new SQLException(
                                "Không thể cập nhật tồn kho: "
                                + sp.maSanPham());
                        }
                    }
                }

                if (diemSuDung > 0 || diemTich > 0) {
                    String sqlDiem = """
                        UPDATE dbo.KhachHang
                        SET diemTichLuy = diemTichLuy - ? + ?
                        WHERE maKH = ? AND diemTichLuy >= ?
                        """;

                    try (PreparedStatement ps = con.prepareStatement(sqlDiem)) {
                        ps.setInt(1, diemSuDung);
                        ps.setInt(2, diemTich);
                        ps.setString(3, maKH);
                        ps.setInt(4, diemSuDung);

                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                "Không thể cập nhật điểm khách hàng.");
                        }
                    }
                }

                con.commit();
                return maHD;

            } catch (Exception e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                if (e instanceof SQLException sqlError) {
                    throw sqlError;
                }
                throw new SQLException(
                    "Thanh toán thất bại: " + e.getMessage(), e);
            } finally {
                con.setAutoCommit(autoCommitCu);
            }
        }
    }

    private KhachHangBanHang layKhachHang(
            Connection con, String maKH) throws SQLException {

        String sql = """
            SELECT maKH, hoTen, soDienThoai,
                   hangTV, diemTichLuy
            FROM dbo.KhachHang WITH (UPDLOCK, HOLDLOCK)
            WHERE maKH = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                        "Không tìm thấy khách hàng: " + maKH);
                }

                return new KhachHangBanHang(
                    rs.getString("maKH"),
                    rs.getString("hoTen"),
                    rs.getString("soDienThoai"),
                    rs.getString("hangTV"),
                    rs.getInt("diemTichLuy")
                );
            }
        }
    }

    private void kiemTraNhanVien(
            Connection con, String maNV) throws SQLException {

        String sql = """
            SELECT 1 FROM dbo.NhanVien
            WHERE maNV = ? AND dangHoatDong = 1
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                        "Nhân viên không tồn tại hoặc đã ngừng hoạt động.");
                }
            }
        }
    }

    private String layCaDangMo(
            Connection con, String maNV) throws SQLException {

        String sql = """
            SELECT TOP 1 maCa
            FROM dbo.CaLamViec
            WHERE maNV = ? AND trangThai = 'DANG_MO'
            ORDER BY thoiGianBatDau DESC
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                        "Nhân viên chưa có ca làm việc đang mở.");
                }
                return rs.getString("maCa");
            }
        }
    }
}
