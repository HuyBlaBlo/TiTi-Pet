
package com.titipet.bus;

import java.sql.SQLException;

import com.titipet.dao.LichHenDAO;

public class LichHenService {

    private final LichHenDAO lichHenDAO = new LichHenDAO();

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
