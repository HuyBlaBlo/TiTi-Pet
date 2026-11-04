package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ThongKeDAO {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public record TongQuan(long soHoaDon, BigDecimal thuHoaDon,
            BigDecimal tienHoan, BigDecimal doanhThuThuan,
            BigDecimal doanhSoHang, BigDecimal doanhSoSpa,
            BigDecimal tienMatThuan, BigDecimal tongUuDai,
            long hoanChuaPhanLoai) {}

    public record NgayDoanhThu(LocalDate ngay, BigDecimal thu, BigDecimal hoan) {
        public BigDecimal thuan() { return thu.subtract(hoan); }
    }

    public record Kenh(String maKenh, String tenKenh, BigDecimal thu,
            BigDecimal hoan) {
        public BigDecimal thuan() { return thu.subtract(hoan); }
    }

    public record SanPhamBanChay(String ma, String ten, long soLuong, BigDecimal doanhSo) {}
    public record SpaBanChay(String ma, String ten, long soLan, BigDecimal doanhSo) {}
    public record CanhBaoTon(String ma, String ten, int soLuong, int nguong) {}
    public record HanThucAn(String ma, String ten, LocalDate han, int soLuong) {}

    public record PhieuCa(String maCa, String thuNgan, LocalDateTime ngayLap,
            BigDecimal quyDauCa, BigDecimal tienMatThucDem,
            BigDecimal tienMatDuKien, BigDecimal chenhLech, String trangThai) {}

    public record VanHanh(long spaChoLam, long tongLichHen, long lichHuy) {}

    public record DuLieu(
            TongQuan tongQuan, List<NgayDoanhThu> bayNgay,
            List<Kenh> theoKenh, List<PhieuCa> phieuCa,
            List<SanPhamBanChay> sanPhamBanChay,
            List<SpaBanChay> spaBanChay,
            List<CanhBaoTon> canhBaoTon,
            List<HanThucAn> hanThucAn,
            VanHanh vanHanh) {}

    /** Dùng connection đã được Main đăng nhập SQL tạo ra; KHÔNG đóng connection dùng chung. */
    public DuLieu docBaoCao(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        Connection con = ConnectDB.getConnection();
        if (con == null || con.isClosed()) {
            throw new SQLException("Chưa kết nối SQL Server. Hãy đăng nhập lại TiTi Pet.");
        }
        // PreparedStatement / ResultSet đều được đóng trong từng phương thức.
        return new DuLieu(
                tongQuan(con, tuNgay, denNgay),
                bayNgay(con, tuNgay, denNgay),
                kenh(con, tuNgay, denNgay),
                phieuCa(con, tuNgay, denNgay),
                sanPham(con, tuNgay, denNgay),
                spa(con, tuNgay, denNgay),
                canhBaoTon(con),
                hanThucAn(con),
                vanHanh(con, tuNgay, denNgay));
    }

    private static Timestamp dauNgay(LocalDate ngay) {
        return Timestamp.valueOf(ngay.atStartOfDay());
    }

    private static void ngay(PreparedStatement ps, int viTri,
            LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        ps.setTimestamp(viTri, dauNgay(tuNgay));
        ps.setTimestamp(viTri + 1, dauNgay(denNgay.plusDays(1)));
    }

    private static BigDecimal tien(ResultSet rs, String cot) throws SQLException {
        BigDecimal giaTri = rs.getBigDecimal(cot);
        return giaTri == null ? ZERO : giaTri;
    }

    private TongQuan tongQuan(Connection con, LocalDate tu, LocalDate den) throws SQLException {
        String sqlHoaDon = """
                SELECT COUNT_BIG(*) AS soHoaDon,
                       COALESCE(SUM(h.tienPhaiThu),0) AS thu,
                       COALESCE(SUM(h.tongTienHang),0) AS hang,
                       COALESCE(SUM(h.tongTienSpa),0) AS spa,
                       COALESCE(SUM(h.tienGiamKM+h.tienGiamHangTV+h.tienGiamDiem),0) AS uuDai,
                       COALESCE(SUM(CASE WHEN h.phuongThucThanhToan='TIEN_MAT'
                           THEN h.tienPhaiThu ELSE 0 END),0) AS tienMat
                FROM dbo.HoaDon h
                WHERE h.trangThai='DA_THANH_TOAN' AND h.ngayLap >= ? AND h.ngayLap < ?
                """;
        long soHoaDon;
        BigDecimal thu, hang, spa, uuDai, tienMat;
        try (PreparedStatement ps = con.prepareStatement(sqlHoaDon)) {
            ngay(ps, 1, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                soHoaDon = rs.getLong("soHoaDon");
                thu = tien(rs, "thu");
                hang = tien(rs, "hang");
                spa = tien(rs, "spa");
                uuDai = tien(rs, "uuDai");
                tienMat = tien(rs, "tienMat");
            }
        }
        // Hoàn tiền lấy theo NGÀY XỬ LÝ, không theo ngày lập hóa đơn gốc.
        String sqlHoan = """
                SELECT COALESCE(SUM(d.tienHoan),0) AS hoan,
                       COALESCE(SUM(CASE WHEN d.phuongThucXuLy='TIEN_MAT'
                           THEN d.tienHoan ELSE 0 END),0) AS hoanMat,
                       COUNT_BIG(CASE WHEN d.phuongThucXuLy IS NULL THEN 1 END) AS chuaPhanLoai
                FROM dbo.DoiTra d
                WHERE d.loaiXuLy='TRA' AND d.ngayDoi >= ? AND d.ngayDoi < ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sqlHoan)) {
            ngay(ps, 1, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                BigDecimal hoan = tien(rs, "hoan");
                BigDecimal hoanMat = tien(rs, "hoanMat");
                return new TongQuan(soHoaDon, thu, hoan,
                        thu.subtract(hoan), hang, spa,
                        tienMat.subtract(hoanMat), uuDai,
                        rs.getLong("chuaPhanLoai"));
            }
        }
    }

    /** Chính xác tối đa 7 ngày cuối của khoảng lọc, luôn điền ngày không có doanh thu bằng 0. */
    private List<NgayDoanhThu> bayNgay(Connection con, LocalDate tu, LocalDate den) throws SQLException {
        LocalDate batDau = den.minusDays(6).isBefore(tu) ? tu : den.minusDays(6);
        Map<LocalDate, BigDecimal[]> map = new LinkedHashMap<>();
        for (LocalDate d = batDau; !d.isAfter(den); d = d.plusDays(1)) {
            map.put(d, new BigDecimal[]{ZERO, ZERO});
        }
        String sql = """
                SELECT CAST(ngayLap AS DATE) AS ngay, COALESCE(SUM(tienPhaiThu),0) AS tong
                FROM dbo.HoaDon
                WHERE trangThai='DA_THANH_TOAN' AND ngayLap >= ? AND ngayLap < ?
                GROUP BY CAST(ngayLap AS DATE)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ngay(ps, 1, batDau, den);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal[] row = map.get(rs.getDate("ngay").toLocalDate());
                    if (row != null) row[0] = tien(rs, "tong");
                }
            }
        }
        String sqlTra = """
                SELECT CAST(ngayDoi AS DATE) AS ngay, COALESCE(SUM(tienHoan),0) AS tong
                FROM dbo.DoiTra
                WHERE loaiXuLy='TRA' AND ngayDoi >= ? AND ngayDoi < ?
                GROUP BY CAST(ngayDoi AS DATE)
                """;
        try (PreparedStatement ps = con.prepareStatement(sqlTra)) {
            ngay(ps, 1, batDau, den);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal[] row = map.get(rs.getDate("ngay").toLocalDate());
                    if (row != null) row[1] = tien(rs, "tong");
                }
            }
        }
        List<NgayDoanhThu> list = new ArrayList<>();
        map.forEach((d, amounts) -> list.add(new NgayDoanhThu(d, amounts[0], amounts[1])));
        return List.copyOf(list);
    }

    private List<Kenh> kenh(Connection con, LocalDate tu, LocalDate den) throws SQLException {
        // Theo đúng ba nguồn đơn của bảng HoaDon. Hoàn tiền phân về kênh hóa đơn gốc.
        Map<String, BigDecimal[]> map = new LinkedHashMap<>();
        for (String k : List.of("TAI_QUAY", "SHOPEE", "TIKTOK")) {
            map.put(k, new BigDecimal[]{ZERO, ZERO});
        }
        String sql = """
                SELECT nguonDon AS kenh, COALESCE(SUM(tienPhaiThu),0) AS tong
                FROM dbo.HoaDon
                WHERE trangThai='DA_THANH_TOAN' AND ngayLap >= ? AND ngayLap < ?
                GROUP BY nguonDon
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ngay(ps, 1, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal[] row = map.get(rs.getString("kenh"));
                    if (row != null) row[0] = tien(rs, "tong");
                }
            }
        }
        String sqlHoan = """
                SELECT h.nguonDon AS kenh, COALESCE(SUM(d.tienHoan),0) AS tong
                FROM dbo.DoiTra d
                JOIN dbo.HoaDon h ON h.maHoaDon=d.maHoaDon
                WHERE d.loaiXuLy='TRA' AND d.ngayDoi >= ? AND d.ngayDoi < ?
                GROUP BY h.nguonDon
                """;
        try (PreparedStatement ps = con.prepareStatement(sqlHoan)) {
            ngay(ps, 1, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal[] row = map.get(rs.getString("kenh"));
                    if (row != null) row[1] = tien(rs, "tong");
                }
            }
        }
        List<Kenh> list = new ArrayList<>();
        String[] labels = {"Tại quầy", "Shopee", "TikTok"};
        int i = 0;
        for (Map.Entry<String, BigDecimal[]> e : map.entrySet()) {
            list.add(new Kenh(e.getKey(), labels[i++], e.getValue()[0], e.getValue()[1]));
        }
        return List.copyOf(list);
    }

    private List<PhieuCa> phieuCa(Connection con, LocalDate tu, LocalDate den) throws SQLException {
        String sql = """
                SELECT TOP (100) p.maCa, n.hoTen, p.ngayLapPhieu,
                       c.tienDauCa, p.tienMat, p.trangThaiDuyet,
                       c.tienDauCa
                       + COALESCE((SELECT SUM(h.tienPhaiThu)
                             FROM dbo.HoaDon h WHERE h.maCa=p.maCa
                             AND h.trangThai='DA_THANH_TOAN'
                             AND h.phuongThucThanhToan='TIEN_MAT'),0)
                       - COALESCE((SELECT SUM(d.tienHoan)
                             FROM dbo.DoiTra d WHERE d.maCa=p.maCa
                             AND d.loaiXuLy='TRA' AND d.phuongThucXuLy='TIEN_MAT'),0)
                       AS tienMatDuKien
                FROM dbo.PhieuKetCa p
                JOIN dbo.CaLamViec c ON c.maCa=p.maCa
                JOIN dbo.NhanVien n ON n.maNV=p.maNV
                WHERE p.ngayLapPhieu >= ? AND p.ngayLapPhieu < ?
                ORDER BY p.ngayLapPhieu DESC, p.maCa DESC
                """;
        List<PhieuCa> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ngay(ps, 1, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal dau = tien(rs, "tienDauCa");
                    BigDecimal dem = tien(rs, "tienMat");
                    BigDecimal duKien = tien(rs, "tienMatDuKien");
                    list.add(new PhieuCa(rs.getString("maCa"), rs.getString("hoTen"),
                            rs.getTimestamp("ngayLapPhieu").toLocalDateTime(), dau,
                            dem, duKien, dem.subtract(duKien), rs.getString("trangThaiDuyet")));
                }
            }
        }
        return List.copyOf(list);
    }

    private List<SanPhamBanChay> sanPham(Connection con, LocalDate tu, LocalDate den)
            throws SQLException {
        String sql = """
                SELECT TOP (5) sp.maSanPham, sp.tenSanPham,
                       SUM(CAST(ct.soLuong AS BIGINT)) AS soLuong,
                       COALESCE(SUM(ct.thanhTien),0) AS doanhSo
                FROM dbo.ChiTietHoaDon_SanPham ct
                JOIN dbo.HoaDon h ON h.maHoaDon=ct.maHoaDon
                JOIN dbo.SanPham sp ON sp.maSanPham=ct.maSanPham
                WHERE h.trangThai='DA_THANH_TOAN' AND h.ngayLap >= ? AND h.ngayLap < ?
                GROUP BY sp.maSanPham, sp.tenSanPham
                ORDER BY soLuong DESC, sp.maSanPham
                """;
        List<SanPhamBanChay> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ngay(ps, 1, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new SanPhamBanChay(
                        rs.getString("maSanPham"), rs.getString("tenSanPham"),
                        rs.getLong("soLuong"), tien(rs, "doanhSo")));
            }
        }
        return List.copyOf(list);
    }

    private List<SpaBanChay> spa(Connection con, LocalDate tu, LocalDate den)
            throws SQLException {
        String sql = """
                SELECT TOP (5) dv.maDichVu, dv.tenDichVu, COUNT_BIG(*) AS soLan,
                       COALESCE(SUM(ct.donGia),0) AS doanhSo
                FROM dbo.ChiTietHoaDon_DichVu ct
                JOIN dbo.HoaDon h ON h.maHoaDon=ct.maHoaDon
                JOIN dbo.DichVuSpa dv ON dv.maDichVu=ct.maDichVu
                WHERE h.trangThai='DA_THANH_TOAN' AND h.ngayLap >= ? AND h.ngayLap < ?
                GROUP BY dv.maDichVu, dv.tenDichVu
                ORDER BY soLan DESC, dv.maDichVu
                """;
        List<SpaBanChay> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ngay(ps, 1, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new SpaBanChay(
                        rs.getString("maDichVu"), rs.getString("tenDichVu"),
                        rs.getLong("soLan"), tien(rs, "doanhSo")));
            }
        }
        return List.copyOf(list);
    }

    private List<CanhBaoTon> canhBaoTon(Connection con) throws SQLException {
        String sql = """
                SELECT maSanPham, tenSanPham, soLuongTonKho, nguongCanhBaoTon
                FROM dbo.SanPham
                WHERE dangKinhDoanh=1 AND soLuongTonKho <= nguongCanhBaoTon
                ORDER BY soLuongTonKho, maSanPham
                """;
        List<CanhBaoTon> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new CanhBaoTon(rs.getString("maSanPham"),
                    rs.getString("tenSanPham"), rs.getInt("soLuongTonKho"),
                    rs.getInt("nguongCanhBaoTon")));
        }
        return List.copyOf(list);
    }

    private List<HanThucAn> hanThucAn(Connection con) throws SQLException {
        String sql = """
                SELECT sp.maSanPham, sp.tenSanPham, ta.hanSuDung, sp.soLuongTonKho
                FROM dbo.ThucAn ta
                JOIN dbo.SanPham sp ON sp.maSanPham=ta.maSanPham
                WHERE sp.dangKinhDoanh=1 AND sp.soLuongTonKho>0
                    AND ta.hanSuDung <= DATEADD(DAY,30,CONVERT(DATE,GETDATE()))
                ORDER BY ta.hanSuDung, sp.maSanPham
                """;
        List<HanThucAn> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new HanThucAn(rs.getString("maSanPham"),
                    rs.getString("tenSanPham"), rs.getDate("hanSuDung").toLocalDate(),
                    rs.getInt("soLuongTonKho")));
        }
        return List.copyOf(list);
    }

    private VanHanh vanHanh(Connection con, LocalDate tu, LocalDate den) throws SQLException {
        String sql = """
                SELECT (SELECT COUNT_BIG(*) FROM dbo.ChiTietHoaDon_DichVu dv
                        JOIN dbo.HoaDon h ON h.maHoaDon=dv.maHoaDon
                        WHERE dv.trangThaiThucHien='CHO_LAM'
                           AND h.trangThai='DA_THANH_TOAN') AS choLam,
                       (SELECT COUNT_BIG(*) FROM dbo.LichHen
                        WHERE thoiGianHen >= ? AND thoiGianHen < ?) AS tongLich,
                       (SELECT COUNT_BIG(*) FROM dbo.LichHen
                        WHERE trangThai='HUY' AND thoiGianHen >= ? AND thoiGianHen < ?) AS lichHuy
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ngay(ps, 1, tu, den);
            ngay(ps, 3, tu, den);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new VanHanh(rs.getLong("choLam"), rs.getLong("tongLich"),
                        rs.getLong("lichHuy"));
            }
        }
    }
}
