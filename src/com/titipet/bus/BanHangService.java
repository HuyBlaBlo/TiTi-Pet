package com.titipet.bus;

import com.titipet.dao.BanHangSanPhamDAO;
import com.titipet.entity.SanPhamBanHang;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Nghiệp vụ giỏ hàng POS (chưa xác nhận thanh toán). */
public class BanHangService {
    public record DongGioHang(SanPhamBanHang sanPham, int soLuong) {
        public BigDecimal thanhTien() {
            return sanPham.giaBan().multiply(BigDecimal.valueOf(soLuong));
        }
    }

    private final BanHangSanPhamDAO sanPhamDAO;
    private final Map<String, DongGioHang> gioHang = new LinkedHashMap<>();

    public BanHangService() {
        this(new BanHangSanPhamDAO());
    }

    public BanHangService(BanHangSanPhamDAO sanPhamDAO) {
        this.sanPhamDAO = Objects.requireNonNull(sanPhamDAO);
    }

    public List<SanPhamBanHang> timSanPham(String tuKhoa) {
        try {
            return sanPhamDAO.timSanPham(tuKhoa);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể tải sản phẩm. Vui lòng kiểm tra kết nối SQL Server.", e);
        }
    }

    public void themVaoGio(SanPhamBanHang sanPham, int soLuongThem) {
        if (sanPham == null || sanPham.maSanPham() == null
                || sanPham.maSanPham().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm hợp lệ.");
        }
        if (soLuongThem <= 0) {
            throw new IllegalArgumentException("Số lượng mua phải lớn hơn 0.");
        }
        if (sanPham.giaBan() == null || sanPham.giaBan().signum() < 0) {
            throw new IllegalArgumentException("Đơn giá sản phẩm không hợp lệ.");
        }
        DongGioHang dongCu = gioHang.get(sanPham.maSanPham());
        long tongSoLuong = (long) soLuongThem + (dongCu == null ? 0 : dongCu.soLuong());
        if (tongSoLuong > sanPham.soLuongTonKho()) {
            throw new IllegalArgumentException("Sản phẩm không đủ số lượng tồn kho.");
        }
        gioHang.put(sanPham.maSanPham(), new DongGioHang(sanPham, (int) tongSoLuong));
    }

    public void capNhatSoLuong(String maSanPham, int soLuongMoi) {
        DongGioHang dong = gioHang.get(maSanPham);
        if (dong == null) {
            throw new IllegalArgumentException("Sản phẩm không có trong giỏ hàng.");
        }
        if (soLuongMoi <= 0) {
            throw new IllegalArgumentException("Số lượng mua phải lớn hơn 0.");
        }
        if (soLuongMoi > dong.sanPham().soLuongTonKho()) {
            throw new IllegalArgumentException("Sản phẩm không đủ số lượng tồn kho.");
        }
        gioHang.put(maSanPham, new DongGioHang(dong.sanPham(), soLuongMoi));
    }

    public void xoaKhoiGio(String maSanPham) {
        gioHang.remove(maSanPham);
    }

    public void xoaToanBoGio() {
        gioHang.clear();
    }

    public List<DongGioHang> layGioHang() {
        return new ArrayList<>(gioHang.values());
    }

    /** Tổng tiền hàng trước khi xử lý VAT/ưu đãi/điểm và thanh toán. */
    public BigDecimal tinhTongTienHang() {
        return gioHang.values().stream()
                .map(DongGioHang::thanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
