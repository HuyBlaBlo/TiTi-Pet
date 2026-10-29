
package com.titipet.bus;

import com.titipet.entity.KhachHangBanHang;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * UC008/UC019 - Tính tiền tại POS.
 *
 * Chỉ tính toán, chưa:
 * - Lưu hóa đơn
 * - Trừ tồn kho
 * - Cập nhật điểm khách hàng
 */
public class BanHangTinhTienService {

    private static final BigDecimal MOT_DIEM =
            new BigDecimal("1000");

    private static final BigDecimal MUC_TICH_DIEM =
            new BigDecimal("50000");

    private static final BigDecimal MOT_TRAM =
            new BigDecimal("100");

    // Kết quả tính tiền thành viên
    public record KetQua(
            BigDecimal tamTinh,
            BigDecimal giamHang,
            BigDecimal giamDiem,
            BigDecimal phaiThu,
            int diemSuDung,
            int phanTramHang
    ) {}

    // Kết quả tính theo một khuyến mãi
    public record KetQuaKhuyenMai(
            BigDecimal tamTinh,
            BigDecimal giamKhuyenMai,
            BigDecimal phaiThu
    ) {}

    /**
     * 1. Ước tính khi người dùng đang nhập điểm.
     * Tự giới hạn về số điểm có thể sử dụng.
     */
    public KetQua uocTinh(
            BigDecimal tamTinh,
            KhachHangBanHang khach,
            int diemYeuCau
    ) {
        return tinhThanhVien(
                tamTinh, khach, diemYeuCau, false);
    }

    /**
     * 2. Kiểm tra chính xác khi nhấn Tính tiền.
     * Không cho sử dụng điểm vượt giới hạn.
     */
    public KetQua tinhChinhXac(
            BigDecimal tamTinh,
            KhachHangBanHang khach,
            int diemYeuCau
    ) {
        return tinhThanhVien(
                tamTinh, khach, diemYeuCau, true);
    }

    private KetQua tinhThanhVien(
            BigDecimal tamTinh,
            KhachHangBanHang khach,
            int diemYeuCau,
            boolean kiemTraChat
    ) {
        kiemTraSoTien(tamTinh);

        // Xác định mức giảm theo hạng
        int tyLe = phanTramHang(khach);

        BigDecimal giamHang = tamTinh
                .multiply(BigDecimal.valueOf(tyLe))
                .divide(MOT_TRAM, 0, RoundingMode.HALF_UP);

        BigDecimal conLai = tamTinh.subtract(giamHang);

        // Số điểm khách hiện có
        int diemDangCo = khach == null
                ? 0
                : Math.max(0, khach.diemTichLuy());

        // Số điểm tối đa có thể đổi theo số tiền
        int diemTheoTien = conLai
                .divideToIntegralValue(MOT_DIEM)
                .min(BigDecimal.valueOf(Integer.MAX_VALUE))
                .intValueExact();

        if (kiemTraChat) {

            if (diemYeuCau < 0) {
                throw new IllegalArgumentException(
                        "Số điểm sử dụng không được âm.");
            }

            if (diemYeuCau > 0 && khach == null) {
                throw new IllegalArgumentException(
                        "Vui lòng chọn khách hàng thành viên.");
            }

            if (diemYeuCau > diemDangCo) {
                throw new IllegalArgumentException(
                        "Số điểm không đủ. Khách hiện có "
                                + diemDangCo + " điểm.");
            }

            if (diemYeuCau > diemTheoTien) {
                throw new IllegalArgumentException(
                        "Số điểm sử dụng vượt số tiền "
                                + "còn phải thu.");
            }
        }

        int diemSuDung = Math.min(
                Math.max(0, diemYeuCau),
                Math.min(diemDangCo, diemTheoTien)
        );

        BigDecimal giamDiem = MOT_DIEM
                .multiply(BigDecimal.valueOf(diemSuDung));

        BigDecimal phaiThu = conLai.subtract(giamDiem);

        return new KetQua(
                tamTinh,
                giamHang,
                giamDiem,
                phaiThu,
                diemSuDung,
                tyLe
        );
    }

    /**
     * 3. Tính tiền khi có một khuyến mãi hợp lệ.
     *
     * Số tiền giảm phải được kiểm tra bởi module
     * Khuyến mãi trước khi truyền vào.
     *
     * Chưa cộng dồn giảm hạng hoặc đổi điểm.
     */
    public KetQuaKhuyenMai tinhTheoKhuyenMai(
            BigDecimal tamTinh,
            BigDecimal tienGiamKM
    ) {
        kiemTraSoTien(tamTinh);

        if (tienGiamKM == null
                || tienGiamKM.signum() < 0
                || tienGiamKM.compareTo(tamTinh) > 0) {
            throw new IllegalArgumentException(
                    "Tiền khuyến mãi không hợp lệ.");
        }

        return new KetQuaKhuyenMai(
                tamTinh,
                tienGiamKM,
                tamTinh.subtract(tienGiamKM)
        );
    }

    /**
     * 4. Tính điểm được tích.
     *
     * giaTriDuDieuKien phải do module Hóa đơn
     * tính sau khi xử lý ưu đãi và thuế.
     */
    public int tinhDiemDuocTich(
            BigDecimal giaTriDuDieuKien,
            KhachHangBanHang khach,
            int diemSuDung,
            boolean coKhuyenMai
    ) {
        kiemTraSoTien(giaTriDuDieuKien);

        if (diemSuDung < 0) {
            throw new IllegalArgumentException(
                    "Số điểm sử dụng không hợp lệ.");
        }

        // Khách lẻ không có tài khoản tích điểm.
        // Đã dùng điểm hoặc KM thì không tích thêm.
        if (khach == null
                || khach.maKH() == null
                || khach.maKH().isBlank()
                || diemSuDung > 0
                || coKhuyenMai) {
            return 0;
        }

        return giaTriDuDieuKien
                .divideToIntegralValue(MUC_TICH_DIEM)
                .min(BigDecimal.valueOf(Integer.MAX_VALUE))
                .intValueExact();
    }

    /**
     * 5. Tính tiền thừa cho thanh toán tiền mặt.
     */
    public BigDecimal tinhTienThua(
            BigDecimal phaiThu,
            String tienKhachDua
    ) {
        kiemTraSoTien(phaiThu);

        String text = tienKhachDua == null
                ? ""
                : tienKhachDua.strip()
                        .replace(".", "")
                        .replace(" ", "");

        if (!text.matches("[0-9]+")) {
            throw new IllegalArgumentException(
                    "Tiền khách đưa phải là số nguyên "
                            + "không âm.");
        }

        return new BigDecimal(text).subtract(phaiThu);
    }

    /**
     * 6. Kiểm tra số tiền.
     */
    private void kiemTraSoTien(BigDecimal tien) {
        if (tien == null || tien.signum() < 0) {
            throw new IllegalArgumentException(
                    "Số tiền không hợp lệ.");
        }
    }

    /**
     * 7. Tỷ lệ giảm theo hạng thành viên.
     */
    public int phanTramHang(KhachHangBanHang khach) {
        if (khach == null || khach.hangTV() == null) {
            return 0;
        }

        return switch (
                khach.hangTV().strip()
                        .toUpperCase(Locale.ROOT)
        ) {
            case "HANG_1", "BAC", "BẠC" -> 3;
            case "HANG_2", "VANG", "VÀNG" -> 4;
            case "HANG_3", "KIM_CUONG",
                 "KIM CƯƠNG" -> 5;
            default -> 0;
        };
    }

    /**
     * 8. Hiển thị tên hạng trên giao diện POS.
     */
    public String tenHang(KhachHangBanHang khach) {
        if (khach == null || khach.hangTV() == null) {
            return "Khách lẻ";
        }

        return switch (phanTramHang(khach)) {
            case 3 -> "Bạc -3%";
            case 4 -> "Vàng -4%";
            case 5 -> "Kim cương -5%";
            default -> "Thường";
        };
    }
}
