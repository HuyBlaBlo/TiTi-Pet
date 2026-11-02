package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Đọc dữ liệu từ SQL Server cho màn hình Trang chủ */
public class TrangChuDAO {

    public record CaHienTai(
            String maCa, String trangThai, LocalDateTime batDau
    ) {}

    public record SoLieu(long tongThuCung, long lichHenHomNay, long lichChoTiepNhan,
                         long hoaDonDaThanhToan, BigDecimal doanhThuHomNay,
                         long dichVuDangLam, long dichVuChoLam
                        ) {}

    public record LichPhucVu(String maLichHen, LocalDateTime gioHen, String tenThuCung,
                             String tenKhachHang, String tenDichVu, String trangThai) {}

    public record CanhBaoKho(String maSanPham, String tenSanPham, int soLuongTon,
                             int nguongCanhBao, LocalDate hanSuDung) {}

    public record HoaDonGanDay(String maHoaDon, LocalDateTime ngayLap,
                               String tenKhachHang, BigDecimal tienPhaiThu) {}

    /** Tìm ca còn mở hoặc đang chờ xác nhận của nhân viên đăng nhập
      */
    public Optional<CaHienTai> timCaHienTai(String maNV) throws SQLException {
        if (maNV == null || maNV.isBlank()) return Optional.empty();
        String sql = "SELECT TOP 1 maCa, trangThai, thoiGianBatDau " +
                "FROM CaLamViec " +
                "WHERE maNV = ? AND trangThai IN ('DANG_MO', 'CHO_XAC_NHAN') " +
                "ORDER BY thoiGianBatDau DESC, maCa DESC ";
        try (Connection con = moKetNoi();
             PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maNV);
                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next())
                        return Optional.empty();

                    return Optional.of(new CaHienTai(
                                            rs.getString("maCa"),
                                            rs.getString("trangThai"),
                                            rs.getTimestamp("thoiGianBatDau").toLocalDateTime()
                                        )
                    );
                }
        }
    }

    /**
     * Số liệu các thẻ trong ngày: lịch bỏ lịch hủy; hóa đơn chỉ tính đã thanh toán
     * Doanh thu là tổng tienPhaiThu của hóa đơn trong ngày, chưa trừ giao dịch hoàn tiền
     * Hai số liệu Spa đếm dòng dịch vụ DANG_LAM và CHO_LAM trên toàn cửa hàng
     *
     *  Các số liệu trả về: Tổng thú cưng, số lịch hẹn hôm nay,số lichj chờ tiếp nhận, số Hóa đơn đã thanh toán, tổng doanh thu hôm nay, số dịch vụ đang lam, và số dịch vụ chờ làm
     */
    public SoLieu laySoLieu(LocalDate ngay) throws SQLException {
        kiemTraNgay(ngay);
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM dbo.ThuCung) AS tongThuCung,
                    (SELECT COUNT(*) FROM dbo.LichHen
                        WHERE thoiGianHen >= ? AND thoiGianHen < ? AND trangThai <> 'HUY') AS lichHen,
                    (SELECT COUNT(*) FROM dbo.LichHen
                        WHERE thoiGianHen >= ? AND thoiGianHen < ?
                        AND trangThai = 'CHO_TIEP_NHAN') AS lichCho,
                    (SELECT COUNT(*) FROM dbo.HoaDon
                        WHERE ngayLap >= ? AND ngayLap < ?
                        AND trangThai = 'DA_THANH_TOAN') AS hoaDon,
                    (SELECT COALESCE(SUM(tienPhaiThu), 0) FROM dbo.HoaDon
                        WHERE ngayLap >= ? AND ngayLap < ?
                        AND trangThai = 'DA_THANH_TOAN') AS doanhThu,
                    (SELECT COUNT(*) FROM dbo.ChiTietHoaDon_DichVu
                        WHERE trangThaiThucHien = 'DANG_LAM') AS dichVuDangLam,
                    (SELECT COUNT(*) FROM dbo.ChiTietHoaDon_DichVu
                        WHERE trangThaiThucHien = 'CHO_LAM') AS dichVuChoLam
                """;
        try (Connection con = moKetNoi();
             PreparedStatement ps = con.prepareStatement(sql)) {
                for (int viTri = 1; viTri <= 7; viTri += 2) datKhoangNgay(ps, viTri, ngay);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return new SoLieu(rs.getLong("tongThuCung"), rs.getLong("lichHen"),
                            rs.getLong("lichCho"), rs.getLong("hoaDon"),
                            rs.getBigDecimal("doanhThu"), rs.getLong("dichVuDangLam"),
                            rs.getLong("dichVuChoLam"));
                }
        }
    }

    /** Lịch phục vụ hôm nay, theo giờ tăng dần; không hiện lịch đã hủy. */
    public List<LichPhucVu> layLichPhucVu(LocalDate ngay, int gioiHan) throws SQLException {
        kiemTraNgay(ngay);
        kiemTraGioiHan(gioiHan);
        String sql = """
                SELECT TOP (?) lh.maLichHen, lh.thoiGianHen, tc.tenThuCung,
                       kh.hoTen, dv.tenDichVu, lh.trangThai
                FROM dbo.LichHen lh
                JOIN dbo.ThuCung tc ON tc.maThuCung = lh.maThuCung AND tc.maKH = lh.maKH
                JOIN dbo.KhachHang kh ON kh.maKH = lh.maKH
                JOIN dbo.DichVuSpa dv ON dv.maDichVu = lh.maDichVu
                WHERE lh.thoiGianHen >= ? AND lh.thoiGianHen < ? AND lh.trangThai <> 'HUY'
                ORDER BY lh.thoiGianHen, lh.maLichHen
                """;
        List<LichPhucVu> ketQua = new ArrayList<>();
        try (Connection con = moKetNoi();
             PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, gioiHan);
                datKhoangNgay(ps, 2, ngay);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ketQua.add(new LichPhucVu(rs.getString("maLichHen"),
                                rs.getTimestamp("thoiGianHen").toLocalDateTime(),
                                rs.getString("tenThuCung"), rs.getString("hoTen"),
                                rs.getString("tenDichVu"), rs.getString("trangThai")));
                    }
                }
        }
        return ketQua;
    }

    /**
     * Cảnh báo sản phẩm đang kinh doanh: tồn kho ở ngưỡng cảnh báo, hoặc thức ăn
     * có hạn sử dụng trước hoặc đúng ngày giới hạn (gồm cả hàng đã quá hạn).
     */
    public List<CanhBaoKho> layCanhBaoKho(LocalDate ngay, int soNgayCanhBao,
                                          int gioiHan) throws SQLException {
        kiemTraNgay(ngay);
        if (soNgayCanhBao < 0) throw new IllegalArgumentException("Số ngày cảnh báo không được âm.");
        kiemTraGioiHan(gioiHan);
        String sql = """
                SELECT TOP (?) sp.maSanPham, sp.tenSanPham, sp.soLuongTonKho,
                       sp.nguongCanhBaoTon, ta.hanSuDung
                FROM dbo.SanPham sp
                LEFT JOIN dbo.ThucAn ta ON ta.maSanPham = sp.maSanPham
                WHERE sp.dangKinhDoanh = 1
                  AND (sp.soLuongTonKho <= sp.nguongCanhBaoTon OR ta.hanSuDung <= ?)
                ORDER BY CASE WHEN ta.hanSuDung <= ? THEN 0 ELSE 1 END,
                         ta.hanSuDung, sp.soLuongTonKho, sp.maSanPham
                """;
        List<CanhBaoKho> ketQua = new ArrayList<>();
        try (Connection con = moKetNoi();
             PreparedStatement ps = con.prepareStatement(sql)) {
                Date ngayGioiHan = Date.valueOf(ngay.plusDays(soNgayCanhBao));
                ps.setInt(1, gioiHan);
                ps.setDate(2, ngayGioiHan);
                ps.setDate(3, ngayGioiHan);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Date han = rs.getDate("hanSuDung");
                        ketQua.add(new CanhBaoKho(rs.getString("maSanPham"),
                                rs.getString("tenSanPham"), rs.getInt("soLuongTonKho"),
                                rs.getInt("nguongCanhBaoTon"),
                                han == null ? null : han.toLocalDate()));
                    }
                }
        }
        return ketQua;
    }

    /** Hoạt động gần đây: các hóa đơn thanh toán mới nhất trong toàn cửa hàng. */
    public List<HoaDonGanDay> layHoaDonGanDay(int gioiHan) throws SQLException {
        kiemTraGioiHan(gioiHan);
        String sql = """
                SELECT TOP (?) h.maHoaDon, h.ngayLap, kh.hoTen, h.tienPhaiThu
                FROM dbo.HoaDon h
                JOIN dbo.KhachHang kh ON kh.maKH = h.maKH
                WHERE h.trangThai = 'DA_THANH_TOAN'
                ORDER BY h.ngayLap DESC, h.maHoaDon DESC
                """;
        List<HoaDonGanDay> ketQua = new ArrayList<>();
        try (Connection con = moKetNoi();
             PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, gioiHan);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ketQua.add(new HoaDonGanDay(rs.getString("maHoaDon"),
                                rs.getTimestamp("ngayLap").toLocalDateTime(),
                                rs.getString("hoTen"), rs.getBigDecimal("tienPhaiThu")));
                    }
                }
        }
        return ketQua;
    }

    /** Mỗi lần đọc dùng kết nối riêng; try-with-resources sẽ tự đóng kết nối. */
    private Connection moKetNoi() throws SQLException {
        return ConnectDB.getInstance().moKetNoiMoi();
    }

    private static void datKhoangNgay(PreparedStatement ps, int viTri, LocalDate ngay)
            throws SQLException {
        ps.setTimestamp(viTri, Timestamp.valueOf(ngay.atStartOfDay()));
        ps.setTimestamp(viTri + 1, Timestamp.valueOf(ngay.plusDays(1).atStartOfDay()));
    }

    private static void kiemTraNgay(LocalDate ngay) {
        if (ngay == null) throw new IllegalArgumentException("Ngày xem dashboard không được để trống.");
    }

    private static void kiemTraGioiHan(int gioiHan) {
        if (gioiHan < 1 || gioiHan > 100)
            throw new IllegalArgumentException("Giới hạn danh sách phải từ 1 đến 100.");
    }
}
