package com.titipet.dao;

import java.sql.Statement;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.titipet.entity.enums.LoaiThuCungApDung;
import java.text.Normalizer;
import java.util.Locale;
import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.KhachHang;
import com.titipet.entity.ThuCung;

public class BoSungThuCungDAO {
	public ThuCung themThuCung(
	        String tenThuCung,
	        String loaiThuCung,
	        BigDecimal canNang,
	        String tinhTrangLong,
	        String ghiChu,
	        KhachHang chuNuoi
	) throws SQLException {

	    String maThuCung = taoMaThuCung();

	    String sql = """
	            INSERT INTO ThuCung
	                (maThuCung, maKH, tenThuCung, loaiThuCung,
	                 canNang, tinhTrangLong, ghiChu)
	            VALUES (?, ?, ?, ?, ?, ?, ?)
	            """;

	    Connection con = ConnectDB.getConnection();

	    try (PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setString(1, maThuCung);
	        ps.setString(2, chuNuoi.getMaKH());
	        ps.setString(3, tenThuCung);
	        ps.setString(4, loaiThuCung);
	        ps.setBigDecimal(5, canNang);
	        ps.setString(6, tinhTrangLong);
	        ps.setString(7, ghiChu);

	        ps.executeUpdate();

	        return new ThuCung(
	                maThuCung,
	                tenThuCung,
	                chuyenLoaiThuCung(loaiThuCung),
	                canNang.doubleValue(),
	                tinhTrangLong,
	                ghiChu,
	                chuNuoi
	        );
	    }
	}
	private String taoMaThuCung() throws SQLException {

	    String sql = """
	            SELECT MAX(
	                TRY_CAST(RIGHT(maThuCung, 2) AS INT)
	            )
	            FROM ThuCung
	            WHERE maThuCung LIKE 'TC_TEST_%'
	            """;

	    Connection con = ConnectDB.getConnection();

	    try (PreparedStatement ps = con.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {

	        if (rs.next()) {
	            int so = rs.getInt(1) + 1;
	            return String.format("TC_TEST_%02d", so);
	        }
	    }

	    return "TC_TEST_01";
	}
private static LoaiThuCungApDung chuyenLoaiThuCung(String giaTri) {
    if (giaTri == null || giaTri.isBlank()) {
        throw new IllegalArgumentException("Loại thú cưng không được để trống.");
    }

    String loai = Normalizer.normalize(
            giaTri.trim(), Normalizer.Form.NFD
    ).replaceAll("\\p{M}", "")
     .toUpperCase(Locale.ROOT);

    return switch (loai) {
        case "CHO" -> LoaiThuCungApDung.CHO;
        case "MEO" -> LoaiThuCungApDung.MEO;
        default -> throw new IllegalArgumentException(
                "Loại thú cưng không hợp lệ: " + giaTri
        );
    };
}
}


