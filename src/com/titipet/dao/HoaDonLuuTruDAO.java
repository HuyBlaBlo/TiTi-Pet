package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import java.sql.*;

/** Bản chụp hóa đơn đã mã hóa, một BLOB cho mỗi hóa đơn. */
public final class HoaDonLuuTruDAO {
    public void luu(Connection con, String maHoaDon, byte[] tepMaHoa) throws SQLException {
        String sql = "INSERT INTO dbo.HoaDonLuuTru (maHoaDon, duLieuMaHoa) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            ps.setBytes(2, tepMaHoa);
            ps.executeUpdate();
        }
    }

    /** Bổ sung bản lưu cho hóa đơn lập trước khi chạy bản vá (không ghi đè). */
    public void luuNeuChuaCo(String maHoaDon, byte[] tepMaHoa) throws SQLException {
        synchronized (ConnectDB.class) {
            Connection con = ConnectDB.getConnection();
            if (con == null || con.isClosed()) {
                ConnectDB.getInstance().connect();
                con = ConnectDB.getConnection();
            }
            if (con == null || con.isClosed()) throw new SQLException("Chưa kết nối SQL Server.");
            String sql = """
                INSERT INTO dbo.HoaDonLuuTru(maHoaDon, duLieuMaHoa)
                SELECT ?, ? WHERE NOT EXISTS (
                    SELECT 1 FROM dbo.HoaDonLuuTru WITH (UPDLOCK, HOLDLOCK)
                    WHERE maHoaDon = ?)
                """;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHoaDon);
                ps.setBytes(2, tepMaHoa);
                ps.setString(3, maHoaDon);
                ps.executeUpdate();
            }
        }
    }

    public byte[] doc(String maHoaDon) throws SQLException {
        synchronized (ConnectDB.class) {
            Connection con = ConnectDB.getConnection();
            if (con == null || con.isClosed()) {
                ConnectDB.getInstance().connect();
                con = ConnectDB.getConnection();
            }
            if (con == null || con.isClosed()) throw new SQLException("Chưa kết nối SQL Server.");
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT duLieuMaHoa FROM dbo.HoaDonLuuTru WHERE maHoaDon = ?")) {
                ps.setString(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getBytes(1) : null;
                }
            }
        }
    }
}
