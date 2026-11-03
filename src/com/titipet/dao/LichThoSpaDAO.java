
package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import java.sql.*;
import java.util.*;

public class LichThoSpaDAO {

    // Lấy thống kê 4 trạng thái
    public int[] layThongKe() {
        int[] kq = new int[4];

        String sql = """
            SELECT
                SUM(CASE WHEN trangThaiThucHien = 'CHO_LAM'
                    THEN 1 ELSE 0 END) AS choLam,

                SUM(CASE WHEN trangThaiThucHien = 'DANG_LAM'
                    THEN 1 ELSE 0 END) AS dangLam,

                SUM(CASE WHEN trangThaiThucHien = 'HOAN_THANH'
                    AND trangThaiBanGiao = 'CHUA_BAN_GIAO'
                    THEN 1 ELSE 0 END) AS hoanThanh,

                SUM(CASE WHEN trangThaiBanGiao = 'DA_BAN_GIAO'
                    THEN 1 ELSE 0 END) AS daBanGiao

            FROM ChiTietHoaDon_DichVu
            """;

        Connection conn = ConnectDB.getConnection();

        if (conn == null) {
            System.out.println("Không kết nối được SQL Server!");
            return kq;
        }

        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                kq[0] = rs.getInt("choLam");
                kq[1] = rs.getInt("dangLam");
                kq[2] = rs.getInt("hoanThanh");
                kq[3] = rs.getInt("daBanGiao");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return kq;
    }

    // Lấy danh sách dịch vụ Spa để điều phối
    public List<Object[]> layDanhSachSpa() {
        List<Object[]> ds = new ArrayList<>();

        String sql = """
            SELECT
                lh.maLichHen,
                tc.tenThuCung,
                tc.loaiThuCung,
                kh.hoTen AS tenChu,
                kh.soDienThoai,
                dv.tenDichVu,
                nv.hoTen AS tenTho,
                lh.thoiGianHen,
                lh.thoiGianKetThuc,
                ctdv.trangThaiThucHien,
                ctdv.trangThaiBanGiao,
                ctdv.maChiTietDichVu
            FROM ChiTietHoaDon_DichVu ctdv
            JOIN HoaDon hd ON ctdv.maHoaDon = hd.maHoaDon
            JOIN LichHen lh ON hd.maLichHen = lh.maLichHen
            JOIN KhachHang kh ON lh.maKH = kh.maKH
            JOIN ThuCung tc ON lh.maThuCung = tc.maThuCung
            JOIN DichVuSpa dv ON ctdv.maDichVu = dv.maDichVu
            LEFT JOIN NhanVien nv ON ctdv.maNVThoSpa = nv.maNV
            ORDER BY lh.thoiGianHen
            """;

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
            	ds.add(new Object[]{
            		rs.getString("maLichHen"),
            		rs.getString("tenThuCung"),
            		rs.getString("loaiThuCung"),
            		rs.getString("tenChu"),
            		rs.getString("soDienThoai"),
            		rs.getString("tenDichVu"),
            		rs.getString("tenTho"),
                    rs.getTimestamp("thoiGianHen"),
                    rs.getTimestamp("thoiGianKetThuc"),
                    rs.getString("trangThaiThucHien"),
                    rs.getString("trangThaiBanGiao"),
                    rs.getInt("maChiTietDichVu")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ds;
    }
}
