package com.titipet.bus;

import com.titipet.dao.BanHangKhuyenMaiDAO;
import com.titipet.dao.BanHangKhuyenMaiDAO.KhuyenMaiPOS;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Quy tắc giảm giá theo phạm vi; không phụ thuộc UI hoặc SQL. */
public class BanHangKhuyenMaiService {
    private final BanHangKhuyenMaiDAO dao = new BanHangKhuyenMaiDAO();

    public List<KhuyenMaiPOS> layKhuyenMai() {
        try { return dao.layDangHieuLuc(); }
        catch (SQLException ex) { throw new IllegalStateException("Không tải được khuyến mãi: " + ex.getMessage(), ex); }
    }

    public record DongSpaSauGiam(
            BigDecimal giamPhanBo, BigDecimal tienTruocThue,
            BigDecimal tienThue, BigDecimal tienSauThue) {}

    public record KetQuaKM(
            BigDecimal tamTinh, BigDecimal tienGiamKM,
            BigDecimal tienTruocThue, BigDecimal tienThue,
            BigDecimal phaiThu, Map<String, BigDecimal> giamSanPham,
            List<DongSpaSauGiam> spaSauGiam) {}

    public KetQuaKM tinh(
            KhuyenMaiPOS km, List<BanHangService.DongGioHang> gio,
            List<BanHangService.DongSpa> gioSpa) {
        if (km == null) throw new IllegalArgumentException("Chưa chọn khuyến mãi.");
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(km.ngayBatDau()) || now.isAfter(km.ngayKetThuc()))
            throw new IllegalArgumentException("Khuyến mãi đã hết hiệu lực.");
        if (km.giamTram() == null || km.giamTram().signum() < 0
                || km.giamTram().compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("Tỷ lệ khuyến mãi không hợp lệ.");
        if (gio == null) gio = List.of();
        if (gioSpa == null) gioSpa = List.of();
        BigDecimal tamTinh = BigDecimal.ZERO, giamKM = BigDecimal.ZERO;
        BigDecimal truocThue = BigDecimal.ZERO, thue = BigDecimal.ZERO;
        boolean coDongApDung = false;
        Map<String, BigDecimal> spGiam = new LinkedHashMap<>();
        List<DongSpaSauGiam> spaGiam = new ArrayList<>();
        BigDecimal rate = km.giamTram().movePointLeft(2);

        for (BanHangService.DongGioHang d : gio) {
            BigDecimal gross = d.thanhTien();
            if (gross.signum() < 0) throw new IllegalArgumentException("Giá trị hàng không hợp lệ.");
            boolean duDieuKien = hopLeSanPham(km, d);
            if (duDieuKien) coDongApDung = true;
            BigDecimal discount = duDieuKien ? gross.multiply(rate).setScale(2, RoundingMode.HALF_UP)
                                             : BigDecimal.ZERO;
            spGiam.put(d.sanPham().maSanPham(), discount);
            tamTinh = tamTinh.add(gross);
            giamKM = giamKM.add(discount);
            truocThue = truocThue.add(gross.subtract(discount)); // SP có thuế suất 0 trong POS hiện tại
        }
        for (BanHangService.DongSpa d : gioSpa) {
            if (d == null || d.donGiaDaChot() == null || d.thueSuat() == null)
                throw new IllegalArgumentException("Giá Spa hoặc thuế suất không hợp lệ.");
            boolean duDieuKien = hopLeSpa(km, d);
            if (duDieuKien) coDongApDung = true;
            BigDecimal giam = duDieuKien ? d.donGiaDaChot().multiply(rate).setScale(2, RoundingMode.HALF_UP)
                                         : BigDecimal.ZERO;
            BigDecimal giaSauGiam = d.donGiaDaChot().subtract(giam);
            BigDecimal soTruocThue, soThue;
            if (d.giaDaBaoGomThue()) {
                soTruocThue = giaSauGiam.divide(
                    BigDecimal.ONE.add(d.thueSuat().movePointLeft(2)), 2, RoundingMode.HALF_UP);
                soThue = giaSauGiam.subtract(soTruocThue);
            } else {
                soTruocThue = giaSauGiam;
                soThue = soTruocThue.multiply(d.thueSuat())
                        .movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
            }
            spaGiam.add(new DongSpaSauGiam(giam, soTruocThue, soThue,
                    soTruocThue.add(soThue)));
            tamTinh = tamTinh.add(d.thanhTien());
            giamKM = giamKM.add(giam);
            truocThue = truocThue.add(soTruocThue);
            thue = thue.add(soThue);
        }
        if (!coDongApDung)
            throw new IllegalArgumentException("Khuyến mãi không áp dụng cho mặt hàng hoặc dịch vụ đang chọn.");
        return new KetQuaKM(tamTinh, giamKM, truocThue, thue,
                truocThue.add(thue), Map.copyOf(spGiam), List.copyOf(spaGiam));
    }

    private boolean hopLeSanPham(KhuyenMaiPOS km, BanHangService.DongGioHang d) {
        String phamVi = km.phamVi();
        if ("TOAN_DON".equals(phamVi)) return true;
        if (!"THUC_AN".equals(phamVi) && !"PHU_KIEN".equals(phamVi)) return false;
        String loai = d.sanPham().loaiSanPham();
        if (!phamVi.equals(loai)) return false;
        String ma = km.maSanPhamApDung();
        return ma == null || ma.isBlank() || ma.equals(d.sanPham().maSanPham());
    }

    private boolean hopLeSpa(KhuyenMaiPOS km, BanHangService.DongSpa d) {
        if ("TOAN_DON".equals(km.phamVi())) return true;
        if (!"DICH_VU".equals(km.phamVi())) return false;
        String ma = km.maDichVuApDung();
        return ma == null || ma.isBlank() || ma.equals(d.maDichVu());
    }
}
