package com.titipet.bus;

import com.titipet.entity.KhachHangBanHang;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Một nguồn tính VAT 8% dùng chung cho POS và giao dịch SQL. */
public final class BanHangThueService {
    public static final BigDecimal THUE_SUAT = new BigDecimal("8.00");
    private static final BigDecimal TY_LE = new BigDecimal("0.08");
    private static final BigDecimal MOT_CONG_THUE = new BigDecimal("1.08");
    private final BanHangTinhTienService uuDai = new BanHangTinhTienService();

    public record ThanhTien(BigDecimal truocThue, BigDecimal tienThue, BigDecimal sauThue) { }
    public record HoaDonThanhVien(BigDecimal tamTinh, BigDecimal giamHang,
            BigDecimal giamDiem, BigDecimal truocThue, BigDecimal tienThue,
            BigDecimal phaiThu) { }

    public ThanhTien sanPham(BigDecimal giaSauGiam) {
        kiemTra(giaSauGiam);
        BigDecimal vat = giaSauGiam.multiply(TY_LE).setScale(2, RoundingMode.HALF_UP);
        return new ThanhTien(giaSauGiam, vat, giaSauGiam.add(vat));
    }

    public ThanhTien spa(BigDecimal giaSauGiam, boolean daGomThue) {
        kiemTra(giaSauGiam);
        if (!daGomThue) return sanPham(giaSauGiam);
        BigDecimal net = giaSauGiam.divide(MOT_CONG_THUE, 2, RoundingMode.HALF_UP);
        return new ThanhTien(net, giaSauGiam.subtract(net), giaSauGiam);
    }

    /** Điểm là hình thức thanh toán, trừ sau khi tính VAT. */
    public HoaDonThanhVien thanhVien(List<BanHangService.DongGioHang> hang,
            List<BanHangService.DongSpa> spa, KhachHangBanHang khach,
            int diem, boolean chinhXac) {
        BigDecimal base = BigDecimal.ZERO;
        for (var dong : hang) base = base.add(sanPham(dong.thanhTien()).truocThue());
        for (var dong : spa) base = base.add(spa(dong.donGiaDaChot(), dong.giaDaBaoGomThue()).truocThue());
        var giam = chinhXac ? uuDai.tinhChinhXac(base, khach, diem)
                : uuDai.uocTinh(base, khach, diem);
        BigDecimal net = base.subtract(giam.giamHang());
        BigDecimal vat = net.multiply(TY_LE).setScale(2, RoundingMode.HALF_UP);
        return new HoaDonThanhVien(base, giam.giamHang(), giam.giamDiem(),
                net, vat, net.add(vat).subtract(giam.giamDiem()));
    }

    private static void kiemTra(BigDecimal value) {
        if (value == null || value.signum() < 0)
            throw new IllegalArgumentException("Tiền trước thuế không hợp lệ.");
    }
}
