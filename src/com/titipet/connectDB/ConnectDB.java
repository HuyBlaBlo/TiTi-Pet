package com.titipet.connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Tạo kết nối đến SQL Server cho các lớp DAO
 * {@code TITIPET_DB_USERNAME} và {@code TITIPET_DB_PASSWORD}
 */
public final class ConnectDB {
    public static Connection con = null;
    private static ConnectDB instance = new ConnectDB();

    public static ConnectDB getInstance() {
        return instance;
    }

    /** Mở kết nối dùng chung cho các DAO hiện có. */
    public void connect() throws SQLException {
        con = moKetNoiMoi();
    }

    public void disconnect() {
        if(con!=null) {
            try {
                con.close();
            }catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() {
        return con;
    }

    /**
     * Tạo kết nối riêng cho DAO Trang chủ.
     * DAO có thể đóng kết nối này mà không làm đóng kết nối dùng chung.
     */
    public Connection moKetNoiMoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=TiTiPetDB;"
                + "encrypt=true;trustServerCertificate=true";
        String user = System.getenv("TITIPET_DB_USERNAME");
        String password = System.getenv("TITIPET_DB_PASSWORD");

        // Cả đăng nhập và Trang chủ dùng chung cấu hình, không lưu mật khẩu trong Git.
        if (user == null || user.isBlank() || password == null || password.isBlank()) {
            throw new SQLException("Cần cấu hình TITIPET_DB_USERNAME và TITIPET_DB_PASSWORD.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
