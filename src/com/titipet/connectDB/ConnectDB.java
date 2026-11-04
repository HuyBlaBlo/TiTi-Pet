package com.titipet.connectDB;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Một nguồn cấu hình kết nối duy nhất cho tất cả DAO của TiTi Pet.
 * Không tạo database mới: mọi kết nối đều mở đúng TiTiPetDB.
 * Cấu hình không chứa mật khẩu trong mã nguồn.
 */
public final class ConnectDB {
    public static Connection con = null;

    private static final ConnectDB instance = new ConnectDB();
    private static final String DATABASE = "TiTiPetDB";
    private static final Path LOCAL_CONFIG = Path.of("config", "db.properties");

    private ConnectDB() { }

    public static ConnectDB getInstance() { return instance; }

    /** Dùng kết nối chia sẻ cho các DAO cũ. Không được đóng từ bên trong DAO. */
    public synchronized void connect() throws SQLException {
        if (con == null || con.isClosed()) {
            con = taoKetNoi();
        }
    }

    /** Mở kết nối độc lập cùng database; DAO Trang chủ có thể tự đóng. */
    public Connection moKetNoiMoi() throws SQLException {
        return taoKetNoi();
    }

    private Connection taoKetNoi() throws SQLException {
        Properties file = new Properties();
        if (Files.isRegularFile(LOCAL_CONFIG)) {
            try (Reader reader = Files.newBufferedReader(LOCAL_CONFIG, StandardCharsets.UTF_8)) {
                file.load(reader);
            } catch (IOException ex) {
                throw new SQLException("Không đọc được config/db.properties.", ex);
            }
        }

        String host = layCauHinh("TITIPET_DB_HOST", file, "host", "localhost");
        String port = layCauHinh("TITIPET_DB_PORT", file, "port", "14333");
        String user = layCauHinh("TITIPET_DB_USERNAME", file, "username", "Titi_pet");
        String password = layCauHinh("TITIPET_DB_PASSWORD", file, "password", "Khang1111.");

        if (user.isBlank() || password.isBlank()) {
            throw new SQLException("Thiếu thông tin SQL Server. Cấu hình TITIPET_DB_USERNAME và "
                    + "TITIPET_DB_PASSWORD hoặc config/db.properties (không commit).");
        }
        if (!host.matches("[A-Za-z0-9_.-]+") || !port.matches("[0-9]{1,5}")) {
            throw new SQLException("Host hoặc cổng SQL Server không hợp lệ.");
        }
        try {
            int portNumber = Integer.parseInt(port);
            if (portNumber < 1 || portNumber > 65535) {
                throw new SQLException("Cổng SQL Server phải trong khoảng 1..65535.");
            }
        } catch (NumberFormatException ex) {
            throw new SQLException("Cổng SQL Server không hợp lệ.", ex);
        }

        String url = "jdbc:sqlserver://" + host + ":" + port
                + ";databaseName=" + DATABASE
                + ";encrypt=true;trustServerCertificate=true;loginTimeout=6";
        Connection result = DriverManager.getConnection(url, user, password);
        if (!DATABASE.equalsIgnoreCase(result.getCatalog())) {
            try { result.close(); } catch (SQLException ignored) { }
            throw new SQLException("Kết nối nhầm database. Chỉ cho phép " + DATABASE + ".");
        }
        return result;
    }

    private static String layCauHinh(String envName, Properties file, String propertyName, String fallback) {
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) return env.trim();
        String value = file.getProperty(propertyName);
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }

    public synchronized void disconnect() {
        if (con != null) {
            try { con.close(); } catch (SQLException ignored) { }
            finally { con = null; }
        }
    }

    public static Connection getConnection() { return con; }
}
