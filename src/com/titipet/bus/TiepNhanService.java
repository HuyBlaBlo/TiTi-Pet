
package com.titipet.bus;

import com.titipet.dao.TiepNhanDAO;
import com.titipet.entity.DichVuSpa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TiepNhanService {

    private final TiepNhanDAO tiepNhanDAO = new TiepNhanDAO();

    // Nghiệp vụ tạo phiếu tiếp nhận
    public boolean taoPhieuTiepNhan(
            String maPhieuTiepNhan,
            String maLichHen,
            String maKH,
            String maThuCung,
            BigDecimal canNangThucTe,
            String tinhTrangLong,
            String ghiChu,
            LocalDateTime thoiGianTraDuKien,
            List<DichVuSpa> danhSachDichVu) {

        // 1. Kiểm tra mã phiếu
        if (maPhieuTiepNhan == null
                || maPhieuTiepNhan.trim().isEmpty()) {
            return false;
        }

        // 2. Kiểm tra khách hàng
        if (maKH == null || maKH.trim().isEmpty()) {
            return false;
        }

        // 3. Kiểm tra thú cưng
        if (maThuCung == null || maThuCung.trim().isEmpty()) {
            return false;
        }

        // 4. Kiểm tra cân nặng
        if (canNangThucTe == null
                || canNangThucTe.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // 5. Kiểm tra tình trạng lông
        if (tinhTrangLong == null
                || tinhTrangLong.trim().isEmpty()) {
            return false;
        }

        // 6. Kiểm tra danh sách dịch vụ
        if (danhSachDichVu == null
                || danhSachDichVu.isEmpty()) {
            return false;
        }

        // 7. Kiểm tra thời gian trả dự kiến
        if (thoiGianTraDuKien == null
                || thoiGianTraDuKien.isBefore(LocalDateTime.now())) {
            return false;
        }

        // 8. Lưu phiếu tiếp nhận và chi tiết dịch vụ
        return tiepNhanDAO.themPhieuTiepNhan(
                maPhieuTiepNhan,
                maLichHen,
                maKH,
                maThuCung,
                canNangThucTe,
                tinhTrangLong,
                ghiChu,
                thoiGianTraDuKien,
                danhSachDichVu
        );
    }
}
