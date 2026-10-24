package com.titipet.bus;

import com.titipet.entity.KhachHangBanHang;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Ước tính số tiền hiển thị tại POS. Không ghi hóa đơn, trừ điểm hoặc tồn kho.
 * Mức hạng HANG_1/2/3 = 3/4/5%; mỗi điểm đổi 1.000đ.
 * VAT, khuyến mãi và nghiệp vụ xác nhận thanh toán do module Hóa đơn triển khai.
 */
public class BanHangTinhTienService {
    private static final BigDecimal MOT_DIEM = BigDecimal.valueOf(1000);

    public record KetQua(BigDecimal tamTinh, BigDecimal giamHang, BigDecimal giamDiem,
                         BigDecimal phaiThu, int diemSuDung, int phanTramHang) { }

    public KetQua uocTinh(BigDecimal tamTinh, KhachHangBanHang khach, int diemYeuCau) {
        if (tamTinh == null || tamTinh.signum() < 0) throw new IllegalArgumentException("Tạm tính không hợp lệ.");
        int tyLe = phanTramHang(khach);
        BigDecimal giamHang = tamTinh.multiply(BigDecimal.valueOf(tyLe))
                                  .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        BigDecimal conLai = tamTinh.subtract(giamHang);
        int diemKhachCo = khach == null ? 0 : Math.max(0, khach.diemTichLuy());
        int diemToiDaTheoTien = conLai.divideToIntegralValue(MOT_DIEM).intValue();
        int diem = Math.min(Math.max(0, diemYeuCau), Math.min(diemKhachCo, diemToiDaTheoTien));
        BigDecimal giamDiem = MOT_DIEM.multiply(BigDecimal.valueOf(diem));
        return new KetQua(tamTinh, giamHang, giamDiem, conLai.subtract(giamDiem), diem, tyLe);
    }

    /** Return money to give back, or a negative value for an insufficient cash amount. */
    public BigDecimal tinhTienThua(BigDecimal phaiThu, String tienKhachDua) {
        if (phaiThu == null || phaiThu.signum() < 0) {
            throw new IllegalArgumentException("Số tiền phải thu không hợp lệ.");
        }
        String normalized = tienKhachDua == null ? "" :
                tienKhachDua.strip().replace(".", "").replace(" ", "");
        if (!normalized.matches("[0-9]+")) {
            throw new IllegalArgumentException("Tiền khách đưa phải là số nguyên không âm.");
        }
        return new BigDecimal(normalized).subtract(phaiThu);
    }

    public int phanTramHang(KhachHangBanHang khach) {
        if (khach == null || khach.hangTV() == null) return 0;
        return switch (khach.hangTV().strip().toUpperCase()) {
            case "HANG_1", "BAC", "BẠC" -> 3;
            case "HANG_2", "VANG", "VÀNG" -> 4;
            case "HANG_3", "KIM_CUONG", "KIM CƯƠNG" -> 5;
            default -> 0;
        };
    }

    public String tenHang(KhachHangBanHang khach) {
        if (khach == null || khach.hangTV() == null) return "Khách lẻ";
        return switch (phanTramHang(khach)) {
            case 3 -> "Bạc -3%";
            case 4 -> "Vàng -4%";
            case 5 -> "Kim cương -5%";
            default -> "Thường";
        };
    }
}
