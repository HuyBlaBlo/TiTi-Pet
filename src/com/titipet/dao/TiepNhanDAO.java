
package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.DichVuSpa;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

public class TiepNhanDAO {

    // Thêm phiếu tiếp nhận và chi tiết dịch vụ
    public boolean themPhieuTiepNhan(
            String maPhieuTiepNhan,
            String maLichHen,
            String maKH,
            String maThuCung,
            BigDecimal canNangThucTe,
            String tinhTrangLong,
            String ghiChu,
            LocalDateTime thoiGianTraDuKien,
            List<DichVuSpa> danhSachDichVu) {

        String sqlPhieu = """
                INSERT INTO dbo.PhieuTiepNhan
                (
                    maPhieuTiepNhan,
                    maLichHen,
                    maKH,
                    maThuCung,
                    canNangThucTe,
                    tinhTrangLong,
                    ghiChu,
                    thoiGianTraDuKien,
                    trangThai
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String sqlChiTiet = """
                INSERT INTO dbo.ChiTietTiepNhan
                (
                    maPhieuTiepNhan,
                    maDichVu,
                    donGia,
                    thoiLuongPhut
                )
                VALUES (?, ?, ?, ?)
                """;

        Connection con = null;

        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // 1. Thêm phiếu tiếp nhận
            try (PreparedStatement ps = con.prepareStatement(sqlPhieu)) {

                ps.setString(1, maPhieuTiepNhan);

                if (maLichHen == null) {
                    ps.setNull(2, Types.VARCHAR);
                } else {
                    ps.setString(2, maLichHen);
                }

                ps.setString(3, maKH);
                ps.setString(4, maThuCung);
                ps.setBigDecimal(5, canNangThucTe);
                ps.setString(6, tinhTrangLong);
                ps.setString(7, ghiChu);

                if (thoiGianTraDuKien == null) {
                    ps.setNull(8, Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(
                        8,
                        java.sql.Timestamp.valueOf(thoiGianTraDuKien)
                    );
                }

                ps.setString(9, "DANG_TIEP_NHAN");

                ps.executeUpdate();
            }

            // 2. Thêm các dịch vụ xác nhận
            try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {

                for (DichVuSpa dv : danhSachDichVu) {

                    ps.setString(1, maPhieuTiepNhan);
                    ps.setString(2, dv.getMaDichVu());
                    ps.setBigDecimal(3, dv.getDonGiaCoBan());
                    ps.setInt(4, dv.getThoiLuongPhut());

                    ps.addBatch();
                }

                ps.executeBatch();
            }
         // Cập nhật trạng thái lịch hẹn sau khi lưu chi tiết tiếp nhận
            String sqlUpdateLichHen = """
                UPDATE dbo.LichHen
                SET trangThai = 'DA_TIEP_NHAN'
                WHERE maLichHen = ?
                  AND trangThai = 'CHO_TIEP_NHAN'
                """;

            try (PreparedStatement psLichHen = con.prepareStatement(sqlUpdateLichHen)) {
                psLichHen.setString(1, maLichHen);

                int rows = psLichHen.executeUpdate();

                if (rows != 1) {
                    throw new SQLException(
                        "Không thể cập nhật trạng thái lịch hẹn. "
                        + "Lịch hẹn không tồn tại hoặc đã được tiếp nhận."
                    );
                }
            }

            // Chỉ commit khi cả phiếu, chi tiết và lịch hẹn đều thành công
            con.commit();
            return true;

        } catch (SQLException e) {

            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            return false;

        } finally {

            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
