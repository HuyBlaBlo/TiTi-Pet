
package com.titipet.bus;

import com.titipet.dao.BanHangGiaoDichDAO;
import java.math.BigDecimal;
import java.util.List;

public class BanHangThanhToanService {

    private final BanHangGiaoDichDAO dao;

    public BanHangThanhToanService() {
        this.dao = new BanHangGiaoDichDAO();
    }

    public String thanhToan(
            String maKH,
            String maNV,
            String nguonDon,
            String phuongThuc,
            int diemSuDung,
            List<BanHangService.DongGioHang> gioHang,
            BigDecimal tienKhachDua) {

        return thanhToan(maKH, maNV, nguonDon, phuongThuc, diemSuDung,
                gioHang, List.of(), tienKhachDua);
    }

    public String thanhToan(
            String maKH, String maNV, String nguonDon,
            String phuongThuc, int diemSuDung,
            List<BanHangService.DongGioHang> gioHang,
            List<BanHangService.DongSpa> gioSpa,
            BigDecimal tienKhachDua) {

        return thanhToan(maKH, maNV, nguonDon, phuongThuc, diemSuDung,
                gioHang, gioSpa, tienKhachDua, null);
    }

    public String thanhToan(
            String maKH, String maNV, String nguonDon,
            String phuongThuc, int diemSuDung,
            List<BanHangService.DongGioHang> gioHang,
            List<BanHangService.DongSpa> gioSpa,
            BigDecimal tienKhachDua, String maKhuyenMai) {

        if (maKhuyenMai != null && !maKhuyenMai.isBlank() && diemSuDung != 0) {
            throw new IllegalArgumentException(
                "Khuyến mãi không được kết hợp với sử dụng điểm.");
        }
        if (maKH == null || maKH.isBlank()) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn khách hàng trước khi thanh toán.");
        }

        if (maNV == null || maNV.isBlank()) {
            throw new IllegalArgumentException(
                    "Chưa xác định nhân viên lập hóa đơn.");
        }

        if ((gioHang == null || gioHang.isEmpty())
                && (gioSpa == null || gioSpa.isEmpty())) {
            throw new IllegalArgumentException(
                    "Giỏ hàng đang trống.");
        }

        if (diemSuDung < 0) {
            throw new IllegalArgumentException(
                    "Số điểm sử dụng không hợp lệ.");
        }

        if (!List.of("TAI_QUAY", "SHOPEE", "TIKTOK")
                .contains(nguonDon)) {
            throw new IllegalArgumentException(
                    "Nguồn đơn hàng không hợp lệ.");
        }

        if (!List.of("TIEN_MAT", "CHUYEN_KHOAN", "QUA_SAN")
                .contains(phuongThuc)) {
            throw new IllegalArgumentException(
                    "Phương thức thanh toán không hợp lệ.");
        }

        if (tienKhachDua != null &&
                tienKhachDua.signum() < 0) {
            throw new IllegalArgumentException(
                    "Tiền khách đưa không được âm.");
        }

        try {
            return dao.luuGiaoDich(
                    maKH, maNV, nguonDon,
                    phuongThuc, diemSuDung,
                    gioHang == null ? List.of() : List.copyOf(gioHang),
                    gioSpa == null ? List.of() : List.copyOf(gioSpa),
                    tienKhachDua, maKhuyenMai);
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException(
                    "Không thể lưu giao dịch: " + e.getMessage(), e);
        }
    }
}
