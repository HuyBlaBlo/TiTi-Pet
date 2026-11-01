
package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

/**
 * DAO quản lý ca làm việc.
 * Tổng hợp doanh thu từ UC008 và tiền hoàn từ UC002.
 * Không xử lý giao diện hoặc nghiệp vụ tại đây.
 */
public class CaLamViecDAO {

    public record TongHopCa(
            String maCa,
            String maNV,
            String trangThai,
            BigDecimal tienDauCa,
            BigDecimal doanhThuTienMat,
            BigDecimal doanhThuCK,
            BigDecimal doanhThuQuaSan,
            long soHoaDon,
            long hoaDonChuaPhanLoai,
            BigDecimal hoanTienMat,
            BigDecimal hoanTienCK) {

        // Tiền mặt dự kiến sau khi trừ tiền hoàn UC002
        public BigDecimal tienMatDuKien() {
            return tienDauCa
                    .add(doanhThuTienMat)
                    .subtract(hoanTienMat);
        }

        // Doanh thu chuyển khoản sau hoàn tiền
        public BigDecimal doanhThuCKThuan() {
            return doanhThuCK.subtract(hoanTienCK);
        }
    }

    public Optional<TongHopCa> timCaHienTai(String maNV)
            throws SQLException {

        Connection con = ketNoi();

        synchronized (ConnectDB.class) {

            String sql = """
                SELECT TOP 1 maCa
                FROM dbo.CaLamViec
                WHERE maNV = ?
                  AND trangThai IN ('DANG_MO','CHO_XAC_NHAN')
                ORDER BY thoiGianBatDau DESC
                """;

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setString(1, maNV);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    return Optional.ofNullable(
                            docTongHop(con, rs.getString(1))
                    );
                }
            }
        }
    }

    static TongHopCa docTongHop(
            Connection con,
            String maCa) throws SQLException {

        String sql = """
            SELECT
                c.maCa,
                c.maNV,
                c.trangThai,
                c.tienDauCa,

                COUNT_BIG(h.maHoaDon) AS soHoaDon,

                COALESCE(SUM(
                    CASE
                        WHEN h.phuongThucThanhToan = 'TIEN_MAT'
                        THEN h.tienPhaiThu
                        ELSE 0
                    END
                ), 0) AS tienMat,

                COALESCE(SUM(
                    CASE
                        WHEN h.phuongThucThanhToan = 'CHUYEN_KHOAN'
                        THEN h.tienPhaiThu
                        ELSE 0
                    END
                ), 0) AS tienCK,

                COALESCE(SUM(
                    CASE
                        WHEN h.phuongThucThanhToan = 'QUA_SAN'
                        THEN h.tienPhaiThu
                        ELSE 0
                    END
                ), 0) AS tienQuaSan,

                COALESCE(SUM(
                    CASE
                        WHEN h.maHoaDon IS NOT NULL
                         AND (
                             h.phuongThucThanhToan IS NULL
                             OR h.phuongThucThanhToan NOT IN
                                 ('TIEN_MAT',
                                  'CHUYEN_KHOAN',
                                  'QUA_SAN')
                         )
                        THEN 1
                        ELSE 0
                    END
                ), 0) AS chuaPhanLoai,

                COALESCE((
                    SELECT SUM(d.tienHoan)
                    FROM dbo.DoiTra d WITH (HOLDLOCK)
                    WHERE d.maCa = c.maCa
                      AND d.loaiXuLy = 'TRA'
                      AND d.phuongThucXuLy = 'TIEN_MAT'
                ), 0) AS hoanTienMat,

                COALESCE((
                    SELECT SUM(d.tienHoan)
                    FROM dbo.DoiTra d WITH (HOLDLOCK)
                    WHERE d.maCa = c.maCa
                      AND d.loaiXuLy = 'TRA'
                      AND d.phuongThucXuLy = 'CHUYEN_KHOAN'
                ), 0) AS hoanTienCK

            FROM dbo.CaLamViec c

            LEFT JOIN dbo.HoaDon h WITH (HOLDLOCK)
                ON h.maCa = c.maCa
               AND h.trangThai = 'DA_THANH_TOAN'

            WHERE c.maCa = ?

            GROUP BY
                c.maCa,
                c.maNV,
                c.trangThai,
                c.tienDauCa
            """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, maCa);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return new TongHopCa(
                        rs.getString("maCa"),
                        rs.getString("maNV"),
                        rs.getString("trangThai"),
                        rs.getBigDecimal("tienDauCa"),
                        rs.getBigDecimal("tienMat"),
                        rs.getBigDecimal("tienCK"),
                        rs.getBigDecimal("tienQuaSan"),
                        rs.getLong("soHoaDon"),
                        rs.getLong("chuaPhanLoai"),
                        rs.getBigDecimal("hoanTienMat"),
                        rs.getBigDecimal("hoanTienCK")
                );
            }
        }
    }

    static Connection ketNoi() throws SQLException {

        Connection con = ConnectDB.getConnection();

        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }

        if (con == null || con.isClosed()) {
            throw new SQLException(
                    "Chưa kết nối SQL Server."
            );
        }

        return con;
    }
}
