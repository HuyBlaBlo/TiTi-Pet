
package com.titipet.dao;

import com.titipet.bus.BanHangService;
import com.titipet.bus.BanHangKhuyenMaiService;
import com.titipet.bus.BanHangTinhTienService;
import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.KhachHangBanHang;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        return luuGiaoDich(maKH, maNV, nguonDon, phuongThuc,
                diemSuDung, gio, List.of(), tienKhachDua, null);
    }

    public String luuGiaoDich(
            String maKH, String maNV,
            String nguonDon, String phuongThuc,
            int diemSuDung,
            List<BanHangService.DongGioHang> gio,
            List<BanHangService.DongSpa> gioSpa,
            BigDecimal tienKhachDua) throws SQLException {
        return luuGiaoDich(maKH, maNV, nguonDon, phuongThuc,
                diemSuDung, gio, gioSpa, tienKhachDua, null);
    }

    /** Mã KM được xác nhận lại khi ghi hóa đơn trong transaction. */
    public String luuGiaoDich(
            String maKH, String maNV,
            String nguonDon, String phuongThuc,
            int diemSuDung,
            List<BanHangService.DongGioHang> gio,
            List<BanHangService.DongSpa> gioSpa,
            BigDecimal tienKhachDua, String maKhuyenMai) throws SQLException {

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

                BigDecimal tienHang = BigDecimal.ZERO;
                BigDecimal tienSpa = BigDecimal.ZERO;
                BigDecimal tienThueSpa = BigDecimal.ZERO;

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

                            tienHang = tienHang.add(
                                giaDB.multiply(BigDecimal.valueOf(soLuong)));
                        }
                    }
                }

                // Kiem tra lai toan bo dich vu va thu cung trong CSDL
                // ngay trong transaction truoc khi lap hoa don.
                String sqlSpa = """
                    SELECT d.dangHoatDong, d.loaiThuCungApDung,
                           d.thueSuat, d.giaDaBaoGomThue,
                           t.loaiThuCung, t.canNang
                    FROM dbo.DichVuSpa d WITH (HOLDLOCK)
                    JOIN dbo.ThuCung t WITH (HOLDLOCK)
                      ON t.maThuCung = ? AND t.maKH = ?
                    WHERE d.maDichVu = ?
                    """;

                for (BanHangService.DongSpa dong : gioSpa) {
                    if (dong == null || dong.donGiaDaChot() == null
                            || dong.donGiaDaChot().signum() < 0)
                        throw new SQLException("Gia dich vu Spa khong hop le.");
                    try (PreparedStatement ps = con.prepareStatement(sqlSpa)) {
                        ps.setString(1, dong.maThuCung());
                        ps.setString(2, maKH);
                        ps.setString(3, dong.maDichVu());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next() || !rs.getBoolean("dangHoatDong"))
                                throw new SQLException("Dich vu Spa/thu cung khong hop le hoac khac chu.");
                            String loai = rs.getString("loaiThuCungApDung");
                            String loaiPet = rs.getString("loaiThuCung");
                            if (!"CA_HAI".equals(loai) && !loai.equals(loaiPet))
                                throw new SQLException("Loai thu cung khong dung voi dich vu Spa.");
                            if (rs.getBigDecimal("canNang").signum() <= 0)
                                throw new SQLException("Can nang thu cung khong hop le.");
                            if (dong.thueSuat().compareTo(rs.getBigDecimal("thueSuat")) != 0
                                    || dong.giaDaBaoGomThue() != rs.getBoolean("giaDaBaoGomThue"))
                                throw new SQLException("Thue dich vu da thay doi, hay chon dich vu lai.");
                        }
                    }
                    tienSpa = tienSpa.add(dong.donGiaDaChot());
                    tienThueSpa = tienThueSpa.add(dong.tienThue());
                }
                BigDecimal tamTinh = tienHang.add(tienSpa)
                        .add(gioSpa.stream()
                                .filter(d -> !d.giaDaBaoGomThue())
                                .map(BanHangService.DongSpa::tienThue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));

                BanHangTinhTienService tinhTien = new BanHangTinhTienService();
                BanHangKhuyenMaiService kmService = new BanHangKhuyenMaiService();
                BanHangKhuyenMaiService.KetQuaKM ketQuaKM = null;
                BigDecimal giamHang, giamDiem, giamKM, phaiThu;
                BigDecimal tongTruocThue, tongThue;
                int diemTich;

                if (maKhuyenMai != null && !maKhuyenMai.isBlank()) {
                    if (diemSuDung != 0)
                        throw new SQLException("Không thể vừa sử dụng điểm vừa áp dụng khuyến mãi.");
                    var km = new BanHangKhuyenMaiDAO().layChoThanhToan(con, maKhuyenMai);
                    ketQuaKM = kmService.tinh(km, gio, gioSpa);
                    giamHang = BigDecimal.ZERO;  // SQL CK_HoaDon_KM_HangTV
                    giamDiem = BigDecimal.ZERO;
                    giamKM = ketQuaKM.tienGiamKM();
                    phaiThu = ketQuaKM.phaiThu();
                    tongTruocThue = ketQuaKM.tienTruocThue();
                    tongThue = ketQuaKM.tienThue();
                    diemTich = 0;
                } else {
                    maKhuyenMai = null;
                    var kq = tinhTien.tinhChinhXac(tamTinh, khach, diemSuDung);
                    giamHang = kq.giamHang();
                    giamDiem = kq.giamDiem();
                    giamKM = BigDecimal.ZERO;
                    phaiThu = kq.phaiThu();
                    tongTruocThue = tamTinh.subtract(tienThueSpa);
                    tongThue = tienThueSpa;
                    diemTich = tinhTien.tinhDiemDuocTich(
                        phaiThu, khach, diemSuDung, false);
                }

                if ("TIEN_MAT".equals(phuongThuc)) {
                    if (tienKhachDua == null || tienKhachDua.compareTo(phaiThu) < 0) {
                        throw new SQLException("Số tiền khách đưa chưa đủ thanh toán.");
                    }
                }

                String maHD = "HD" + UUID.randomUUID()
                        .toString().replace("-", "")
                        .substring(0, 24).toUpperCase();

                String maCa = layCaDangMo(con, maNV);

                String sqlHD = """
                    INSERT INTO dbo.HoaDon (
                        maHoaDon, maKH, maNV, maCa, maKhuyenMai,
                        nguonDon, phuongThucThanhToan,
                        trangThai, tongTienHang, tongTienSpa,
                        tienGiamKM, tienGiamHangTV,
                        soDiemSuDung, tienGiamDiem,
                        tienPhaiThu, soDiemDuocTich,
                        tongTienTruocThue, tongTienThue, tongTien
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?,
                            'DA_THANH_TOAN', ?, ?,
                            ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

                try (PreparedStatement ps = con.prepareStatement(sqlHD)) {
                    ps.setString(1, maHD);
                    ps.setString(2, maKH);
                    ps.setString(3, maNV);
                    ps.setString(4, maCa);
                    if (maKhuyenMai == null) ps.setNull(5, Types.VARCHAR);
                    else ps.setString(5, maKhuyenMai);
                    ps.setString(6, nguonDon);
                    ps.setString(7, phuongThuc);
                    ps.setBigDecimal(8, tienHang);
                    ps.setBigDecimal(9, tienSpa);
                    ps.setBigDecimal(10, giamKM);
                    ps.setBigDecimal(11, giamHang);
                    ps.setInt(12, diemSuDung);
                    ps.setBigDecimal(13, giamDiem);
                    ps.setBigDecimal(14, phaiThu);
                    ps.setInt(15, diemTich);
                    ps.setBigDecimal(16, tongTruocThue);
                    ps.setBigDecimal(17, tongThue);
                    ps.setBigDecimal(18, phaiThu);
                    ps.executeUpdate();
                }

                String sqlCT = """
                    INSERT INTO dbo.ChiTietHoaDon_SanPham
                    (maHoaDon, maSanPham, soLuong, donGia,
                     thueSuat, giaDaBaoGomThue,
                     tienGiamPhanBo, tienTruocThue, tienThue)
                    VALUES (?, ?, ?, ?, 0, 0, ?, ?, 0)
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
                        BigDecimal giamSp = ketQuaKM == null ? BigDecimal.ZERO :
                                ketQuaKM.giamSanPham().getOrDefault(sp.maSanPham(), BigDecimal.ZERO);
                        ct.setBigDecimal(5, giamSp);
                        ct.setBigDecimal(6, thanhTien.subtract(giamSp));
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

                String sqlCtSpa = """
                    INSERT INTO dbo.ChiTietHoaDon_DichVu
                      (maHoaDon, maDichVu, maThuCung,
                       trangThaiThucHien, soLuong, donGia,
                       thueSuat, giaDaBaoGomThue,
                       tienGiamPhanBo, tienTruocThue, tienThue)
                    VALUES (?, ?, ?, 'CHO_LAM', 1, ?, ?, ?, ?, ?, ?)
                    """;
                try (PreparedStatement ps = con.prepareStatement(sqlCtSpa)) {
                    for (int i = 0; i < gioSpa.size(); i++) {
                        BanHangService.DongSpa dong = gioSpa.get(i);
                        BigDecimal giamSpa = ketQuaKM == null ? BigDecimal.ZERO :
                                ketQuaKM.spaSauGiam().get(i).giamPhanBo();
                        BigDecimal truoc = ketQuaKM == null ? dong.tienTruocThue() :
                                ketQuaKM.spaSauGiam().get(i).tienTruocThue();
                        BigDecimal thue = ketQuaKM == null ? dong.tienThue() :
                                ketQuaKM.spaSauGiam().get(i).tienThue();
                        ps.setString(1, maHD);
                        ps.setString(2, dong.maDichVu());
                        ps.setString(3, dong.maThuCung());
                        ps.setBigDecimal(4, dong.donGiaDaChot());
                        ps.setBigDecimal(5, dong.thueSuat());
                        ps.setBoolean(6, dong.giaDaBaoGomThue());
                        ps.setBigDecimal(7, giamSpa);
                        ps.setBigDecimal(8, truoc);
                        ps.setBigDecimal(9, thue);
                        ps.executeUpdate();
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
