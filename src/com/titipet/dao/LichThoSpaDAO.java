package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Danh sách Spa lấy từ các chi tiết dịch vụ thuộc hóa đơn đã thanh toán. */
public class LichThoSpaDAO {
    private static Connection ketNoi() throws SQLException {
        Connection con = ConnectDB.getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }
        if (con == null || con.isClosed())
            throw new SQLException("Không có kết nối SQL Server.");
        return con;
    }

    public int[] layThongKe() throws SQLException { return layThongKe(null); }

    public int[] layThongKe(String maTho) throws SQLException {
        String sql = """
            SELECT
              COALESCE(SUM(CASE WHEN d.trangThaiThucHien = 'CHO_LAM' THEN 1 ELSE 0 END),0) AS choLam,
              COALESCE(SUM(CASE WHEN d.trangThaiThucHien = 'DANG_LAM' THEN 1 ELSE 0 END),0) AS dangLam,
              COALESCE(SUM(CASE WHEN d.trangThaiThucHien = 'HOAN_THANH'
                   AND d.trangThaiBanGiao = 'CHUA_BAN_GIAO' THEN 1 ELSE 0 END),0) AS hoanThanh,
              COALESCE(SUM(CASE WHEN d.trangThaiBanGiao = 'DA_BAN_GIAO' THEN 1 ELSE 0 END),0) AS daBanGiao
            FROM dbo.ChiTietHoaDon_DichVu d
            JOIN dbo.HoaDon h ON h.maHoaDon = d.maHoaDon
            WHERE h.trangThai = 'DA_THANH_TOAN'
              AND (? IS NULL OR d.maNVThoSpa = ?)
            """;
        synchronized (ConnectDB.class) {
            try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
                ps.setString(1, maTho);
                ps.setString(2, maTho);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return new int[4];
                    return new int[] {rs.getInt("choLam"), rs.getInt("dangLam"),
                            rs.getInt("hoanThanh"), rs.getInt("daBanGiao")};
                }
            }
        }
    }

    public List<Object[]> layDanhSachSpa() throws SQLException { return layDanhSachSpa(null); }

    public List<Object[]> layDanhSachSpa(String maTho) throws SQLException {
        String sql = """
            SELECT COALESCE(l.maLichHen, h.maHoaDon) AS maLichHen,
                t.tenThuCung, t.loaiThuCung, k.hoTen AS tenChu, k.soDienThoai,
                dv.tenDichVu, nv.hoTen AS tenTho,
                COALESCE(l.thoiGianHen, h.ngayLap) AS thoiGianHen,
                l.thoiGianKetThuc,
                d.trangThaiThucHien, d.trangThaiBanGiao,
                d.maChiTietDichVu, d.maNVThoSpa
            FROM dbo.ChiTietHoaDon_DichVu d
            JOIN dbo.HoaDon h ON h.maHoaDon = d.maHoaDon
            JOIN dbo.KhachHang k ON k.maKH = h.maKH
            JOIN dbo.ThuCung t ON t.maThuCung = d.maThuCung
            JOIN dbo.DichVuSpa dv ON dv.maDichVu = d.maDichVu
            LEFT JOIN dbo.NhanVien nv ON nv.maNV = d.maNVThoSpa
            LEFT JOIN dbo.LichHen l ON l.maLichHen = d.maLichHen
            WHERE h.trangThai = 'DA_THANH_TOAN'
              AND (? IS NULL OR d.maNVThoSpa = ?)
            ORDER BY COALESCE(l.thoiGianHen, h.ngayLap) DESC, d.maChiTietDichVu
            """;
        List<Object[]> ketQua = new ArrayList<>();
        synchronized (ConnectDB.class) {
            try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
                ps.setString(1, maTho);
                ps.setString(2, maTho);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ketQua.add(new Object[] {
                            rs.getString("maLichHen"), rs.getNString("tenThuCung"),
                            rs.getString("loaiThuCung"), rs.getNString("tenChu"),
                            rs.getString("soDienThoai"), rs.getNString("tenDichVu"),
                            rs.getNString("tenTho"), rs.getTimestamp("thoiGianHen"),
                            rs.getTimestamp("thoiGianKetThuc"),
                            rs.getString("trangThaiThucHien"),
                            rs.getString("trangThaiBanGiao"),
                            rs.getLong("maChiTietDichVu"), rs.getString("maNVThoSpa")
                        });
                    }
                }
            }
        }
        return ketQua;
    }

    /** Chỉ thợ được phân công mới đổi trạng thái, dùng điều kiện chống click hai lần. */
    public boolean chuyenTrangThai(long maChiTiet, String maTho,
            String tuTrangThai, String denTrangThai) throws SQLException {
        if (maTho == null || maTho.isBlank())
            throw new IllegalArgumentException("Chỉ thợ Spa được giao việc mới thao tác được.");
        boolean banGiao = "DA_BAN_GIAO".equals(denTrangThai);
        if (!(("CHO_LAM".equals(tuTrangThai) && "DANG_LAM".equals(denTrangThai))
              || ("DANG_LAM".equals(tuTrangThai) && "HOAN_THANH".equals(denTrangThai))
              || ("HOAN_THANH".equals(tuTrangThai) && banGiao)))
            throw new IllegalArgumentException("Chuyển trạng thái Spa không hợp lệ.");
        String sql = banGiao ? """
            UPDATE d SET trangThaiBanGiao = 'DA_BAN_GIAO'
            FROM dbo.ChiTietHoaDon_DichVu d
            JOIN dbo.HoaDon h ON h.maHoaDon = d.maHoaDon
            WHERE d.maChiTietDichVu = ? AND d.maNVThoSpa = ?
              AND h.trangThai = 'DA_THANH_TOAN'
              AND d.trangThaiThucHien = 'HOAN_THANH'
              AND d.trangThaiBanGiao = 'CHUA_BAN_GIAO'
            """ : """
            UPDATE d SET trangThaiThucHien = ?
            FROM dbo.ChiTietHoaDon_DichVu d
            JOIN dbo.HoaDon h ON h.maHoaDon = d.maHoaDon
            WHERE d.maChiTietDichVu = ? AND d.maNVThoSpa = ?
              AND h.trangThai = 'DA_THANH_TOAN' AND d.trangThaiThucHien = ?
            """;
        synchronized (ConnectDB.class) {
            try (PreparedStatement ps = ketNoi().prepareStatement(sql)) {
                if (banGiao) {
                    ps.setLong(1, maChiTiet);
                    ps.setString(2, maTho);
                } else {
                    ps.setString(1, denTrangThai);
                    ps.setLong(2, maChiTiet);
                    ps.setString(3, maTho);
                    ps.setString(4, tuTrangThai);
                }
                return ps.executeUpdate() == 1;
            }
        }
    }
}
