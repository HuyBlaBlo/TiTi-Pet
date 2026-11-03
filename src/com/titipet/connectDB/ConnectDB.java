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
     * Mỗi DAO có thể tự đóng kết nối này mà không ảnh hưởng kết nối dùng chung.
     * Cùng cấu hình với connect() để Trang chủ đọc được dữ liệu sau đăng nhập.
     */
    public Connection moKetNoiMoi() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=TiTiPetDB;"
                + "encrypt=true;trustServerCertificate=true";
        String user = System.getenv("TITIPET_DB_USERNAME");
        String password = System.getenv("TITIPET_DB_PASSWORD");

        // Cả đăng nhập và Dashboard cần cùng một cấu hình kết nối.
        if (user == null || user.isBlank() || password == null || password.isBlank()) {
            throw new SQLException("Cần cấu hình TITIPET_DB_USERNAME và TITIPET_DB_PASSWORD.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
