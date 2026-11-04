package com.titipet.bus;

import com.titipet.dao.LichHenDAO;
import com.titipet.entity.LichHen;
import com.titipet.entity.enums.TrangThaiLichHen;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Kiểm tra lịch đã đặt trước khi chuyển sang tiếp nhận thú cưng.
 * Không ghi trạng thái tại bước này: hóa đơn và lịch chỉ được chốt trong
 * transaction thanh toán tại BanHangGiaoDichDAO.
 */
public final class ChuyenLichHenTiepNhanService {
    private final LichHenDAO lichHenDAO;

    public ChuyenLichHenTiepNhanService() {
        this.lichHenDAO = new LichHenDAO();
    }

    /** Đọc lại lịch từ CSDL để không sử dụng trạng thái cũ đang hiển thị trên UI. */
    public LichHen layLichChoTiepNhan(String maLichHen) throws SQLException {
        if (maLichHen == null || maLichHen.isBlank())
            throw new IllegalArgumentException("Vui lòng chọn lịch hẹn cần tiếp nhận.");
        LichHen lich = lichHenDAO.timTheoMa(maLichHen);
        kiemTraLichChoTiepNhan(lich, LocalDate.now());
        return lich;
    }

    /** Quy tắc thuần, có thể kiểm thử không cần SQL Server. */
    public static void kiemTraLichChoTiepNhan(LichHen lich, LocalDate ngayHienTai) {
        Objects.requireNonNull(ngayHienTai, "Ngày kiểm tra không được để trống.");
        if (lich == null)
            throw new IllegalArgumentException("Lịch hẹn không còn tồn tại. Vui lòng làm mới.");
        if (lich.getTrangThai() != TrangThaiLichHen.CHO_TIEP_NHAN)
            throw new IllegalArgumentException("Lịch đã tiếp nhận, thanh toán hoặc hủy. Vui lòng làm mới.");
        if (lich.getThoiGianHen() == null || lich.getThoiGianHen().toLocalDate().isAfter(ngayHienTai))
            throw new IllegalArgumentException("Chưa đến ngày hẹn Spa. Chỉ tiếp nhận khi khách đã đến cửa hàng.");
        if (lich.getKhachHang() == null || lich.getThuCung() == null
                || lich.getDichVu() == null || lich.getThoSpa() == null)
            throw new IllegalArgumentException("Lịch thiếu thông tin khách, thú cưng, dịch vụ hoặc thợ Spa.");
        if (lich.getKhachHang().getMaKH() == null || lich.getThuCung().getMaThuCung() == null
                || lich.getDichVu().getMaDichVu() == null || lich.getThoSpa().getMaNV() == null)
            throw new IllegalArgumentException("Thông tin lịch không đầy đủ. Vui lòng kiểm tra lại.");
        if (lich.getThuCung().getChuNuoi() != null
                && !lich.getKhachHang().getMaKH().equals(lich.getThuCung().getChuNuoi().getMaKH()))
            throw new IllegalArgumentException("Thú cưng không thuộc khách hàng của lịch hẹn.");
    }
}
