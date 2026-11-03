package com.titipet.bus;

import com.titipet.dao.TrangChuDAO;
import com.titipet.entity.NhanVien;
import com.titipet.entity.enums.VaiTro;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Chọn dữ liệu Trang chủ theo nhân viên đang đăng nhập
 * Mỗi vai trò gọi truy vấn phù hợp với mình
 */
public class TrangChuService {
    private static final int SO_LICH_HIEN_THI = 3;
    private static final int SO_CANH_BAO_HIEN_THI = 3;
    private static final int SO_HOA_DON_HIEN_THI = 5;
    private static final int SO_NGAY_CANH_BAO_HAN = 7;

    private final AuthService authService;
    private final TrangChuDAO trangChuDAO;

    public TrangChuService(AuthService authService) {
        this(authService, new TrangChuDAO());
    }

    TrangChuService(AuthService authService, TrangChuDAO trangChuDAO) {
        this.authService = Objects.requireNonNull(authService);
        this.trangChuDAO = Objects.requireNonNull(trangChuDAO);
    }

    /**
     * Dữ liệu trả về đã được giới hạn theo vai trò
     * */
    public record DuLieuTrangChu(
            VaiTro vaiTro,
            LocalDate ngay,
            Optional<TrangChuDAO.CaHienTai> caHienTai,
            TrangChuDAO.SoLieu soLieu,
            List<TrangChuDAO.LichPhucVu> lichPhucVu,
            List<TrangChuDAO.CanhBaoKho> canhBaoKho,
            List<TrangChuDAO.HoaDonGanDay> hoaDonGanDay) { }

    /**
     * Lấy mã và vai trò từ phiên đăng nhập
     * Tài khoản chưa đăng nhập hay thiếu vai trò sẽ không được tải Dashboard
     */
    public DuLieuTrangChu layDuLieuTrangChu() {
        NhanVien nhanVien = authService.getNhanVienDangNhap()
                .orElseThrow(() -> new IllegalStateException("Chưa có nhân viên đăng nhập."));
        if (nhanVien.getMaNV() == null || nhanVien.getMaNV().isBlank()
                || nhanVien.getVaiTro() == null) {
            throw new IllegalStateException("Thông tin nhân viên đăng nhập không hợp lệ.");
        }

        LocalDate homNay = LocalDate.now();
        try {
            return switch (nhanVien.getVaiTro()) {
                case QUAN_LY -> layChoQuanLy(homNay, nhanVien.getMaNV());
                case THU_NGAN -> layChoThuNgan(homNay, nhanVien.getMaNV());
                case THO_SPA -> layChoThoSpa(homNay, nhanVien.getMaNV());
            };
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được dữ liệu Trang chủ.", e);
        }
    }

    /** Quản lý được xem số liệu và hoạt động của toàn cửa hàng */
    private DuLieuTrangChu layChoQuanLy(LocalDate ngay, String maNV) throws SQLException {
        return new DuLieuTrangChu(VaiTro.QUAN_LY, ngay,
                trangChuDAO.timCaHienTai(maNV),
                trangChuDAO.laySoLieu(ngay),
                List.copyOf(trangChuDAO.layLichPhucVu(ngay, SO_LICH_HIEN_THI)),
                List.copyOf(trangChuDAO.layCanhBaoKho(
                        ngay, SO_NGAY_CANH_BAO_HAN, SO_CANH_BAO_HIEN_THI)),
                List.copyOf(trangChuDAO.layHoaDonGanDay(SO_HOA_DON_HIEN_THI)));
    }

    /**
     * Thu ngân xem lịch/kho phục vụ công việc, tiền và hóa đơn chỉ thuộc ca của mình.
     * Nếu chưa mở ca, số liệu ca bằng 0 và không truy vấn hóa đơn toàn cửa hàng
     */
    private DuLieuTrangChu layChoThuNgan(LocalDate ngay, String maNV) throws SQLException {
        Optional<TrangChuDAO.CaHienTai> ca = trangChuDAO.timCaHienTai(maNV);
        TrangChuDAO.SoLieuVanHanh vanHanh = trangChuDAO.laySoLieuVanHanh(ngay);
        TrangChuDAO.SoLieuCa soLieuCa = ca.isPresent()
                ?trangChuDAO.laySoLieuCa(ca.get().maCa())
                :new TrangChuDAO.SoLieuCa(0, BigDecimal.ZERO);

        TrangChuDAO.SoLieu soLieu = new TrangChuDAO.SoLieu(
                vanHanh.tongThuCung(), vanHanh.lichHenHomNay(),
                vanHanh.lichChoTiepNhan(), soLieuCa.soHoaDon(),
                soLieuCa.doanhThu(), vanHanh.dichVuDangLam(),
                vanHanh.dichVuChoLam());

        List<TrangChuDAO.HoaDonGanDay> hoaDon = ca.isPresent()
                ?trangChuDAO.layHoaDonGanDayCuaCa(ca.get().maCa(), SO_HOA_DON_HIEN_THI)
                :List.of();
        return new DuLieuTrangChu(VaiTro.THU_NGAN, ngay, ca, soLieu,
                List.copyOf(trangChuDAO.layLichPhucVu(ngay, SO_LICH_HIEN_THI)),
                List.copyOf(trangChuDAO.layCanhBaoKho(
                        ngay, SO_NGAY_CANH_BAO_HAN, SO_CANH_BAO_HIEN_THI)),
                List.copyOf(hoaDon));
    }

    /**
     * Thợ Spa chỉ nhận lịch và tiến độ được gắn mã nhân viên của mình
     * Không gọi truy vấn tổng doanh thu, hóa đơn hay kho
     */
    private DuLieuTrangChu layChoThoSpa(LocalDate ngay, String maNV) throws SQLException {
        TrangChuDAO.SoLieuThoSpa viec = trangChuDAO.laySoLieuThoSpa(ngay, maNV);
        TrangChuDAO.SoLieu soLieu = new TrangChuDAO.SoLieu(
                0, viec.lichHomNay(), 0, 0, BigDecimal.ZERO,
                viec.dangLam(), viec.choLam());
        return new DuLieuTrangChu(VaiTro.THO_SPA, ngay, Optional.empty(), soLieu,
                List.copyOf(trangChuDAO.layLichPhucVuThoSpa(
                        ngay, maNV, SO_LICH_HIEN_THI)), List.of(), List.of());
    }
}
