package com.titipet.bus;

import com.titipet.dao.TrangChuDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Chuẩn bị dữ liệu cho màn hình Trang chủ.
 * DAO đọc cơ sở dữ liệu; Service chọn ngày, số dòng và gom kết quả;
 * Panel chỉ nhận dữ liệu để hiển thị.
 */
public class TrangChuService {

    // Số dòng tối đa trong từng khung nhỏ của dashboard.
    private static final int SO_LICH_HIEN_THI = 3;
    private static final int SO_CANH_BAO_HIEN_THI = 3;
    private static final int SO_HOA_DON_HIEN_THI = 5;
    private static final int SO_NGAY_CANH_BAO_HAN = 7;

    private final TrangChuDAO trangChuDAO = new TrangChuDAO();

    /**
     * Một lần tải đầy đủ dữ liệu mà TrangChuPanel cần.
     * Các danh sách có thể rỗng nếu chưa có lịch, cảnh báo hoặc hóa đơn.
     * caHienTai rỗng khi nhân viên chưa mở ca.
     */
    public record DuLieuTrangChu(
            LocalDate ngay,
            Optional<TrangChuDAO.CaHienTai> caHienTai,
            TrangChuDAO.SoLieu soLieu,
            List<TrangChuDAO.LichPhucVu> lichPhucVu,
            List<TrangChuDAO.CanhBaoKho> canhBaoKho,
            List<TrangChuDAO.HoaDonGanDay> hoaDonGanDay) { }

    /**
     * Lấy dữ liệu mới nhất cho Trang chủ của nhân viên đang đăng nhập.
     * Dùng cùng một ngày cho các số liệu và lịch để tránh lệch ngày lúc qua nửa đêm.
     * Lỗi kết nối được báo ra cho Panel hiển thị, không thay bằng số 0 gây hiểu nhầm.
     */
    public DuLieuTrangChu layDuLieuTrangChu(String maNV) {
        if (maNV == null || maNV.isBlank()) {
            throw new IllegalArgumentException("Chưa có nhân viên đăng nhập.");
        }

        LocalDate homNay = LocalDate.now();
        try {
            Optional<TrangChuDAO.CaHienTai> ca = trangChuDAO.timCaHienTai(maNV);
            TrangChuDAO.SoLieu soLieu = trangChuDAO.laySoLieu(homNay);
            List<TrangChuDAO.LichPhucVu> lich =
                    trangChuDAO.layLichPhucVu(homNay, SO_LICH_HIEN_THI);
            List<TrangChuDAO.CanhBaoKho> canhBao =
                    trangChuDAO.layCanhBaoKho(homNay, SO_NGAY_CANH_BAO_HAN, SO_CANH_BAO_HIEN_THI);
            List<TrangChuDAO.HoaDonGanDay> hoaDon =
                    trangChuDAO.layHoaDonGanDay(SO_HOA_DON_HIEN_THI);

            return new DuLieuTrangChu(homNay, ca, soLieu,
                    List.copyOf(lich), List.copyOf(canhBao), List.copyOf(hoaDon));
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được dữ liệu Trang chủ.", e);
        }
    }
}
