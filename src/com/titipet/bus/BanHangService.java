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

/**
 * BUS xử lý giỏ hàng bán lẻ.
 * Chưa thực hiện thanh toán và trừ tồn kho.
 */
public class BanHangService {

    /**
     * Một dòng sản phẩm trong giỏ hàng.
     */
    public record DongGioHang(
            SanPhamBanHang sanPham,
            int soLuong
    ) {

        public BigDecimal thanhTien() {

            return sanPham.giaBan()
                    .multiply(BigDecimal.valueOf(soLuong));
        }
    }

    private final BanHangSanPhamDAO sanPhamDAO;

    private final Map<String, DongGioHang> gioHang =
            new LinkedHashMap<>();

    /**
     * Constructor mặc định.
     */
    public BanHangService() {
        this(new BanHangSanPhamDAO());
    }

    /**
     * Cho phép truyền DAO để kiểm thử.
     */
    public BanHangService(BanHangSanPhamDAO sanPhamDAO) {

        this.sanPhamDAO =
                Objects.requireNonNull(sanPhamDAO);
    }

    /**
     * Tra cứu sản phẩm.
     */
    public List<SanPhamBanHang> timSanPham(String tuKhoa) {

        try {

            return sanPhamDAO.timSanPham(tuKhoa);

        } catch (SQLException e) {

            throw new IllegalStateException(
                    "Không thể tải sản phẩm. "
                    + "Vui lòng kiểm tra kết nối SQL Server.",
                    e
            );
        }
    }

    /**
     * Thêm sản phẩm vào giỏ hàng.
     * Nếu trùng mã sản phẩm thì cộng dồn số lượng.
     */
    public void themVaoGio(
            SanPhamBanHang sanPham,
            int soLuongThem
    ) {

        if (sanPham == null
                || sanPham.maSanPham() == null
                || sanPham.maSanPham().isBlank()) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn sản phẩm hợp lệ."
            );
        }

        if (soLuongThem <= 0) {

            throw new IllegalArgumentException(
                    "Số lượng mua phải lớn hơn 0."
            );
        }

        if (sanPham.giaBan() == null
                || sanPham.giaBan().signum() < 0) {

            throw new IllegalArgumentException(
                    "Đơn giá sản phẩm không hợp lệ."
            );
        }

        DongGioHang dongCu =
                gioHang.get(sanPham.maSanPham());

        long tongSoLuong =
                (long) soLuongThem
                + (dongCu == null ? 0 : dongCu.soLuong());

        if (tongSoLuong > sanPham.soLuongTonKho()) {

            throw new IllegalArgumentException(
                    "Sản phẩm không đủ số lượng tồn kho."
            );
        }

        gioHang.put(
                sanPham.maSanPham(),
                new DongGioHang(
                        sanPham,
                        (int) tongSoLuong
                )
        );
    }

    /**
     * Thay đổi số lượng sản phẩm đã có trong giỏ.
     */
    public void capNhatSoLuong(
            String maSanPham,
            int soLuongMoi
    ) {

        DongGioHang dong =
                gioHang.get(maSanPham);

        if (dong == null) {

            throw new IllegalArgumentException(
                    "Sản phẩm không có trong giỏ hàng."
            );
        }

        if (soLuongMoi <= 0) {

            throw new IllegalArgumentException(
                    "Số lượng mua phải lớn hơn 0."
            );
        }

        if (soLuongMoi > dong.sanPham().soLuongTonKho()) {

            throw new IllegalArgumentException(
                    "Sản phẩm không đủ số lượng tồn kho."
            );
        }

        gioHang.put(
                maSanPham,
                new DongGioHang(
                        dong.sanPham(),
                        soLuongMoi
                )
        );
    }

    /**
     * Xóa một sản phẩm khỏi giỏ.
     */
    public void xoaKhoiGio(String maSanPham) {

        gioHang.remove(maSanPham);
    }

    /**
     * Xóa toàn bộ giỏ hàng.
     */
    public void xoaToanBoGio() {

        gioHang.clear();
    }

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng.
     */
    public List<DongGioHang> layGioHang() {

        return new ArrayList<>(gioHang.values());
    }

    /**
     * Tổng tiền hàng trước VAT, ưu đãi
     * và các bước xử lý thanh toán.
     */
    public BigDecimal tinhTongTienHang() {

        return gioHang.values()
                .stream()
                .map(DongGioHang::thanhTien)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}