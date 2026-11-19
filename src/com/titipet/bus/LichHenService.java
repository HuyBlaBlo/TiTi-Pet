
package com.titipet.bus;

import java.sql.SQLException;

import com.titipet.dao.LichHenDAO;

public class LichHenService {

    private final LichHenDAO lichHenDAO = new LichHenDAO();

    public java.util.List<com.titipet.entity.LichHen> traCuuTheoSoDienThoai(String soDienThoai)
            throws SQLException {
        return lichHenDAO.timLichTheoSoDienThoai(chuanHoaSoDienThoai(soDienThoai));
    }

    /** Quy tắc chung: số điện thoại Việt Nam phải có 10 số và bắt đầu bằng 0. */
    public static String chuanHoaSoDienThoai(String input) {
        String phone = input == null ? "" : input.strip();
        if (!phone.matches("0[0-9]{9}")) {
            throw new IllegalArgumentException(
                    "Số điện thoại phải có đúng 10 chữ số và bắt đầu bằng 0.");
        }
        return phone;
    }

    // Nghiệp vụ tiếp nhận lịch hẹn
    public boolean tiepNhanLichHen(String maLichHen) throws SQLException {

        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            return false;
        }

        return lichHenDAO.tiepNhanLichHen(maLichHen);
    }

    // Nghiệp vụ hủy lịch hẹn
    public boolean huyLichHen(String maLichHen) throws SQLException {

        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            return false;
        }

        return lichHenDAO.huyLichHen(maLichHen);
    }
}
