
package com.titipet.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.CaLamViecSpa;

public class CaLamViecSpaDAO {

    // Lấy tất cả ca của một thợ Spa
    public List<CaLamViecSpa> layCaTheoThoSpa(String maNV) {

        List<CaLamViecSpa> ds = new ArrayList<>();

        String sql =
            "SELECT " +
            "ctdv.maChiTietDichVu, " +
            "lh.maLichHen, " +
            "lh.thoiGianHen, " +
            "lh.thoiGianKetThuc, " +

            "tc.maThuCung, " +
            "tc.tenThuCung, " +
            "tc.loaiThuCung, " +
            "tc.canNang, " +
            "tc.tinhTrangLong, " +
            "tc.ghiChu AS ghiChuThuCung, " +

            "kh.maKH, " +
            "kh.hoTen AS tenKH, " +
            "kh.soDienThoai AS sdtKH, " +

            "dv.maDichVu, " +
            "dv.tenDichVu, " +

            "COALESCE(ctdv.maNVThoSpa, lh.maNVThoSpa) AS maNVThoSpa, " +
            "nv.hoTen AS tenNVThoSpa, " +

            "ctdv.trangThaiThucHien, " +
            "ctdv.trangThaiBanGiao, " +
            "lh.ghiChu AS ghiChuLichHen " +

            "FROM dbo.ChiTietHoaDon_DichVu ctdv " +

            "INNER JOIN dbo.HoaDon hd " +
            "ON hd.maHoaDon = ctdv.maHoaDon " +

            "INNER JOIN dbo.LichHen lh " +
            "ON lh.maLichHen = ctdv.maLichHen " +

            "INNER JOIN dbo.ThuCung tc " +
            "ON tc.maThuCung = lh.maThuCung " +
            "AND tc.maKH = lh.maKH " +

            "INNER JOIN dbo.KhachHang kh " +
            "ON kh.maKH = lh.maKH " +

            "INNER JOIN dbo.DichVuSpa dv " +
            "ON dv.maDichVu = ctdv.maDichVu " +

            "LEFT JOIN dbo.NhanVien nv " +
            "ON nv.maNV = COALESCE(ctdv.maNVThoSpa, lh.maNVThoSpa) " +

            "WHERE COALESCE(ctdv.maNVThoSpa, lh.maNVThoSpa) = ? " +
            "AND hd.trangThai = 'DA_THANH_TOAN' " +
            "AND lh.trangThai = 'DA_TIEP_NHAN' " +

            "ORDER BY lh.thoiGianHen ASC";

        try {
            
            Connection con = ConnectDB.getInstance().getConnection();

            if (con == null) {
                System.out.println("❌ KẾT NỐI DATABASE ĐANG NULL!");
                return ds;
            }

            System.out.println("✅ DATABASE ĐÃ KẾT NỐI!");

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maNV);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                CaLamViecSpa ca = new CaLamViecSpa();

                ca.setMaChiTietDichVu(
                    rs.getLong("maChiTietDichVu"));

                ca.setMaLichHen(
                    rs.getString("maLichHen"));

                Timestamp tgHen = rs.getTimestamp("thoiGianHen");
                if (tgHen != null) {
                    ca.setThoiGianHen(
                        tgHen.toLocalDateTime());
                }

                Timestamp tgKetThuc =
                    rs.getTimestamp("thoiGianKetThuc");

                if (tgKetThuc != null) {
                    ca.setThoiGianKetThuc(
                        tgKetThuc.toLocalDateTime());
                }

                ca.setMaThuCung(rs.getString("maThuCung"));
                ca.setTenThuCung(rs.getString("tenThuCung"));
                ca.setLoaiThuCung(rs.getString("loaiThuCung"));
                ca.setCanNang(rs.getBigDecimal("canNang"));
                ca.setTinhTrangLong(rs.getString("tinhTrangLong"));
                ca.setGhiChuThuCung(rs.getString("ghiChuThuCung"));

                ca.setMaKH(rs.getString("maKH"));
                ca.setTenKH(rs.getString("tenKH"));
                ca.setSdtKH(rs.getString("sdtKH"));

                ca.setMaDichVu(rs.getString("maDichVu"));
                ca.setTenDichVu(rs.getString("tenDichVu"));

                ca.setMaNVThoSpa(rs.getString("maNVThoSpa"));
                ca.setTenNVThoSpa(rs.getString("tenNVThoSpa"));

                ca.setTrangThaiThucHien(
                    rs.getString("trangThaiThucHien"));

                ca.setTrangThaiBanGiao(
                    rs.getString("trangThaiBanGiao"));

                ca.setGhiChuLichHen(
                    rs.getString("ghiChuLichHen"));

                ds.add(ca);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ds;
    }

    // Bắt đầu thực hiện ca
    public boolean batDauCa(long maChiTietDichVu, String maNV) {

        String sql =
            "UPDATE dbo.ChiTietHoaDon_DichVu " +
            "SET trangThaiThucHien = 'DANG_LAM' " +
            "WHERE maChiTietDichVu = ? " +
            "AND maNVThoSpa = ? " +
            "AND trangThaiThucHien = 'CHO_LAM' " +
            "AND trangThaiBanGiao = 'CHUA_BAN_GIAO'";

        return capNhatTrangThai(sql, maChiTietDichVu, maNV);
    }

    // Hoàn thành ca
    public boolean hoanThanhCa(long maChiTietDichVu, String maNV) {

        String sql =
            "UPDATE dbo.ChiTietHoaDon_DichVu " +
            "SET trangThaiThucHien = 'HOAN_THANH' " +
            "WHERE maChiTietDichVu = ? " +
            "AND maNVThoSpa = ? " +
            "AND trangThaiThucHien = 'DANG_LAM' " +
            "AND trangThaiBanGiao = 'CHUA_BAN_GIAO'";

        return capNhatTrangThai(sql, maChiTietDichVu, maNV);
    }

    // Bàn giao ca
    public boolean banGiaoCa(long maChiTietDichVu, String maNV) {

        String sql =
            "UPDATE dbo.ChiTietHoaDon_DichVu " +
            "SET trangThaiBanGiao = 'DA_BAN_GIAO' " +
            "WHERE maChiTietDichVu = ? " +
            "AND maNVThoSpa = ? " +
            "AND trangThaiThucHien = 'HOAN_THANH' " +
            "AND trangThaiBanGiao = 'CHUA_BAN_GIAO'";

        return capNhatTrangThai(sql, maChiTietDichVu, maNV);
    }

    private boolean capNhatTrangThai(
            String sql,
            long maChiTietDichVu,
            String maNV) {

        try {
            Connection con = ConnectDB.getInstance().getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setLong(1, maChiTietDichVu);
            ps.setString(2, maNV);

            int rows = ps.executeUpdate();

            ps.close();

            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
