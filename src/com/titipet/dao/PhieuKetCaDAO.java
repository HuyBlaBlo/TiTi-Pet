package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Ghi phiếu, chuyển trạng thái và duyệt phiếu trong transaction. */
public class PhieuKetCaDAO {
    public record PhieuChoDuyet(
            String maPhieu, String maCa, String maNV,
            BigDecimal tienMat, BigDecimal tienCK,
            LocalDateTime ngayLap) {
        @Override public String toString() {
            return maPhieu + " | Ca " + maCa + " | NV " + maNV;
        }
    }

    public String guiPhieu(String maNV, BigDecimal tienMatThucTe)
            throws SQLException {
        Connection con = CaLamViecDAO.ketNoi();
        synchronized (ConnectDB.class) {
            boolean oldAuto = con.getAutoCommit();
            int oldLevel = con.getTransactionIsolation();
            try {
                con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                con.setAutoCommit(false);
                // Chỉ Thu ngân đang hoạt động được lập phiếu.
                try (PreparedStatement ps = con.prepareStatement("""
                        SELECT 1 FROM dbo.NhanVien
                        WHERE maNV=? AND vaiTro='THU_NGAN' AND dangHoatDong=1
                        """)) {
                    ps.setString(1, maNV);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Chỉ Thu ngân đang hoạt động được lập phiếu.");
                    }
                }
                String maCa;
                try (PreparedStatement ps = con.prepareStatement("""
                        SELECT TOP 1 maCa FROM dbo.CaLamViec WITH (UPDLOCK, HOLDLOCK)
                        WHERE maNV = ? AND trangThai = 'DANG_MO'
                        ORDER BY thoiGianBatDau DESC
                        """)) {
                    ps.setString(1, maNV);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không có ca đang mở.");
                        maCa = rs.getString(1);
                    }
                }

                var tong = CaLamViecDAO.docTongHop(con, maCa);
                if (tong == null || tong.soHoaDon() == 0)
                    throw new SQLException("Ca chưa có hóa đơn đã thanh toán.");
                if (tong.hoaDonChuaPhanLoai() > 0)
                    throw new SQLException("Có hóa đơn chưa xác định phương thức thanh toán.");
                if (tong.doanhThuCKThuan().signum() < 0)
                    throw new SQLException("Hoàn chuyển khoản vượt doanh thu chuyển khoản của ca. "
                        + "Phiếu kết ca hiện không hỗ trợ số CK âm; cần Quản lý đối soát riêng.");
                if (tong.doanhThuQuaSan().signum() != 0)
                    throw new SQLException("Có doanh thu QUA_SAN: phiếu hiện tại chưa có trường đối soát qua sàn. Cần nhóm thống nhất cấu trúc trước khi chốt.");
                if (tong.tienMatDuKien().compareTo(tienMatThucTe) != 0)
                    throw new SQLException("Tiền mặt thực tế không khớp. Chênh lệch: "
                        + tienMatThucTe.subtract(tong.tienMatDuKien()) + " đ. Hãy kiểm tra hóa đơn.");

                // Mỗi ca chỉ có một phiếu (UQ_PhieuKetCa_MaCa).
                // Khi quản lý từ chối, cập nhật phiếu cũ để gửi lại.
                String maPhieu = null;
                String trangThai = null;
                try (PreparedStatement ps = con.prepareStatement("""
                        SELECT maPhieu, trangThaiDuyet
                        FROM dbo.PhieuKetCa WITH (UPDLOCK, HOLDLOCK)
                        WHERE maCa = ?
                        """)) {
                    ps.setString(1, maCa);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            maPhieu = rs.getString(1);
                            trangThai = rs.getString(2);
                        }
                    }
                }
                if (maPhieu == null) {
                    maPhieu = "KC" + UUID.randomUUID().toString()
                            .replace("-", "").substring(0, 18).toUpperCase();
                    try (PreparedStatement ps = con.prepareStatement("""
                            INSERT INTO dbo.PhieuKetCa
                            (maPhieu, maNV, maCa, soTienBanDauCa,
                             tienMat, tienCK, tongTienKetCa, trangThaiDuyet)
                            VALUES (?, ?, ?, ?, ?, ?, ?, 'CHO_DUYET')
                            """)) {
                        ps.setString(1, maPhieu);
                        ps.setString(2, maNV);
                        ps.setString(3, maCa);
                        ps.setBigDecimal(4, tong.tienDauCa());
                        ps.setBigDecimal(5, tienMatThucTe);
                        ps.setBigDecimal(6, tong.doanhThuCKThuan());
                        ps.setBigDecimal(7, tienMatThucTe.add(tong.doanhThuCKThuan()));
                        ps.executeUpdate();
                    }
                } else {
                    if (!"TU_CHOI".equals(trangThai))
                        throw new SQLException("Phiếu đang chờ duyệt hoặc đã được duyệt.");
                    try (PreparedStatement ps = con.prepareStatement("""
                            UPDATE dbo.PhieuKetCa SET
                              soTienBanDauCa=?, tienMat=?, tienCK=?,
                              tongTienKetCa=?, ngayLapPhieu=SYSDATETIME(),
                              trangThaiDuyet='CHO_DUYET',
                              maNVQuanLyDuyet=NULL, ngayDuyet=NULL
                            WHERE maPhieu=? AND trangThaiDuyet='TU_CHOI'
                            """)) {
                        ps.setBigDecimal(1, tong.tienDauCa());
                        ps.setBigDecimal(2, tienMatThucTe);
                        ps.setBigDecimal(3, tong.doanhThuCKThuan());
                        ps.setBigDecimal(4, tienMatThucTe.add(tong.doanhThuCKThuan()));
                        ps.setString(5, maPhieu);
                        if (ps.executeUpdate() != 1)
                            throw new SQLException("Phiếu kết ca đã thay đổi.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE dbo.CaLamViec SET
                        trangThai='CHO_XAC_NHAN', thoiGianKetThuc=SYSDATETIME()
                        WHERE maCa=? AND trangThai='DANG_MO'
                        """)) {
                    ps.setString(1, maCa);
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Trạng thái ca đã thay đổi.");
                }
                con.commit();
                return maPhieu;
            } catch (Exception e) {
                con.rollback();
                if (e instanceof SQLException se) throw se;
                throw new SQLException("Gửi phiếu thất bại.", e);
            } finally {
                con.setAutoCommit(oldAuto);
                con.setTransactionIsolation(oldLevel);
            }
        }
    }

    public List<PhieuChoDuyet> layPhieuChoDuyet() throws SQLException {
        Connection con = CaLamViecDAO.ketNoi();
        List<PhieuChoDuyet> result = new ArrayList<>();
        synchronized (ConnectDB.class) {
            try (PreparedStatement ps = con.prepareStatement("""
                    SELECT p.maPhieu, p.maCa, p.maNV, p.tienMat,
                           p.tienCK, p.ngayLapPhieu
                    FROM dbo.PhieuKetCa p
                    JOIN dbo.CaLamViec c ON c.maCa = p.maCa
                    WHERE p.trangThaiDuyet = 'CHO_DUYET'
                      AND c.trangThai = 'CHO_XAC_NHAN'
                    ORDER BY p.ngayLapPhieu DESC
                    """);
                ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new PhieuChoDuyet(
                            rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getBigDecimal(4), rs.getBigDecimal(5),
                            rs.getTimestamp(6).toLocalDateTime()));
                }
            }
        }
        return result;
    }

    public void xuLyDuyet(String maPhieu, String maQuanLy, boolean dongY, String lyDo)
            throws SQLException {
        Connection con = CaLamViecDAO.ketNoi();
        synchronized (ConnectDB.class) {
            boolean oldAuto = con.getAutoCommit();
            int oldLevel = con.getTransactionIsolation();
            try {
                con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                con.setAutoCommit(false);
                try (PreparedStatement ps = con.prepareStatement("""
                        SELECT 1 FROM dbo.NhanVien
                        WHERE maNV=? AND vaiTro='QUAN_LY' AND dangHoatDong=1
                        """)) {
                    ps.setString(1, maQuanLy);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Chỉ Quản lý được duyệt phiếu.");
                    }
                }
                String maCa;
                try (PreparedStatement ps = con.prepareStatement("""
                        SELECT p.maCa FROM dbo.PhieuKetCa p WITH (UPDLOCK, HOLDLOCK)
                        JOIN dbo.CaLamViec c WITH (UPDLOCK, HOLDLOCK) ON c.maCa=p.maCa
                        WHERE p.maPhieu=? AND p.trangThaiDuyet='CHO_DUYET'
                          AND c.trangThai='CHO_XAC_NHAN'
                        """)) {
                    ps.setString(1, maPhieu);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Phiếu không còn chờ duyệt.");
                        maCa = rs.getString(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE dbo.PhieuKetCa SET
                          trangThaiDuyet=?, maNVQuanLyDuyet=?, ngayDuyet=SYSDATETIME()
                        WHERE maPhieu=? AND trangThaiDuyet='CHO_DUYET'
                        """)) {
                    ps.setString(1, dongY ? "DA_DUYET" : "TU_CHOI");
                    ps.setString(2, maQuanLy);
                    ps.setString(3, maPhieu);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không cập nhật được phiếu.");
                }
                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE dbo.CaLamViec SET trangThai=?,
                        thoiGianKetThuc=CASE WHEN ?=1 THEN thoiGianKetThuc ELSE NULL END,
                        ghiChu=CASE WHEN ?=1 THEN ghiChu ELSE
                          LEFT(CONCAT(COALESCE(ghiChu,N''), N' | Tu choi ket ca: ', ?), 1000) END
                        WHERE maCa=? AND trangThai='CHO_XAC_NHAN'
                        """)) {
                    ps.setString(1, dongY ? "DA_CHOT" : "DANG_MO");
                    ps.setInt(2, dongY ? 1 : 0);
                    ps.setInt(3, dongY ? 1 : 0);
                    ps.setString(4, lyDo == null ? "" : lyDo);
                    ps.setString(5, maCa);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không cập nhật được ca.");
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                if (e instanceof SQLException se) throw se;
                throw new SQLException("Duyệt phiếu thất bại.", e);
            } finally {
                con.setAutoCommit(oldAuto);
                con.setTransactionIsolation(oldLevel);
            }
        }
    }
}
