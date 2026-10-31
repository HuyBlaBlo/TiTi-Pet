package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** SQL cho danh sách và kiểm tra khuyến mãi UC008/UC019. */
public class BanHangKhuyenMaiDAO {
    public record KhuyenMaiPOS(
            String maKhuyenMai, String tenChuongTrinh, BigDecimal giamTram,
            String phamVi, String maSanPhamApDung, String maDichVuApDung,
            LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        @Override public String toString() {
            return tenChuongTrinh + " (" + giamTram.stripTrailingZeros().toPlainString() + "%)";
        }
    }

    public List<KhuyenMaiPOS> layDangHieuLuc() throws SQLException {
        synchronized (ConnectDB.class) {
            Connection con = ConnectDB.getConnection();
            if (con == null || con.isClosed()) {
                ConnectDB.getInstance().connect();
                con = ConnectDB.getConnection();
            }
            if (con == null || con.isClosed())
                throw new SQLException("Chưa kết nối SQL Server.");
            String sql = """
                SELECT maKhuyenMai, tenChuongTrinh, giamTram, phamVi,
                       maSanPhamApDung, maDichVuApDung, ngayBatDau, ngayKetThuc
                FROM dbo.KhuyenMai
                WHERE trangThai = 1 AND ngayBatDau <= SYSDATETIME()
                      AND ngayKetThuc >= SYSDATETIME()
                ORDER BY tenChuongTrinh, maKhuyenMai
                """;
            List<KhuyenMaiPOS> ds = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
            return ds;
        }
    }

    /** Gọi TRONG transaction thanh toán để kiểm tra lại quyền áp dụng. */
    public KhuyenMaiPOS layChoThanhToan(Connection con, String maKM) throws SQLException {
        if (maKM == null || maKM.isBlank()) return null;
        String sql = """
            SELECT maKhuyenMai, tenChuongTrinh, giamTram, phamVi,
                   maSanPhamApDung, maDichVuApDung, ngayBatDau, ngayKetThuc
            FROM dbo.KhuyenMai WITH (HOLDLOCK)
            WHERE maKhuyenMai = ? AND trangThai = 1
                  AND ngayBatDau <= SYSDATETIME()
                  AND ngayKetThuc >= SYSDATETIME()
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException(
                        "Khuyến mãi đã hết hiệu lực hoặc bị ngừng. Vui lòng chọn lại.");
                return doc(rs);
            }
        }
    }

    private KhuyenMaiPOS doc(ResultSet rs) throws SQLException {
        return new KhuyenMaiPOS(
            rs.getString("maKhuyenMai"), rs.getString("tenChuongTrinh"),
            rs.getBigDecimal("giamTram"), rs.getString("phamVi"),
            rs.getString("maSanPhamApDung"), rs.getString("maDichVuApDung"),
            rs.getTimestamp("ngayBatDau").toLocalDateTime(),
            rs.getTimestamp("ngayKetThuc").toLocalDateTime());
    }
}
