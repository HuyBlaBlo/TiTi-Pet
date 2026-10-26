package com.titipet.connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

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
        String url = "jdbc:sqlserver://localhost:1433;databasename=TiTiPetDB;trustservercertificate=true;Encrypt=true";
        String user = "sa";
        String password = "123456";

        con = DriverManager.getConnection(url,user,password);
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
}
