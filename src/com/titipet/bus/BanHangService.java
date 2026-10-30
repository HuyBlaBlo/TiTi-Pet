
package com.titipet.bus;

import com.titipet.dao.BanHangSanPhamDAO;
import com.titipet.entity.SanPhamBanHang;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * UC008 - Bán hàng tại quầy.
 * Chịu trách nhiệm:
 * - Tìm kiếm sản phẩm
 * - Kiểm tra điều kiện bán
 * - Quản lý giỏ hàng
 * - Tính tổng tiền hàng
 *
 * Chưa ghi hóa đơn xuống SQL Server.
 */
public class BanHangService {

    /**
     * Đại diện một dòng trong giỏ hàng.
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

    public BanHangService() {
        this(new BanHangSanPhamDAO());
    }

    public BanHangService(BanHangSanPhamDAO sanPhamDAO) {
        this.sanPhamDAO = Objects.requireNonNull(sanPhamDAO);
    }

    /**
     * 1. Tìm sản phẩm từ SQL Server.
     */
    public List<SanPhamBanHang> timSanPham(String tuKhoa) {
        try {
            return sanPhamDAO.timSanPham(tuKhoa);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể tải sản phẩm từ SQL Server.", e);
        }
    }

    /**
     * 2. Kiểm tra điều kiện bán sản phẩm.
     */
    private void kiemTraSanPham(SanPhamBanHang sanPham) {

        if (sanPham == null
                || sanPham.maSanPham() == null
                || sanPham.maSanPham().isBlank()) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn sản phẩm hợp lệ.");
        }

        if (sanPham.giaBan() == null
                || sanPham.giaBan().signum() < 0) {

            throw new IllegalArgumentException(
                    "Đơn giá sản phẩm không hợp lệ.");
        }

        if (sanPham.soLuongTonKho() <= 0) {
            throw new IllegalArgumentException(
                    "Sản phẩm đã hết hàng.");
        }

        if (sanPham.hanSuDung() != null
                && sanPham.hanSuDung()
                        .isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Sản phẩm đã hết hạn sử dụng.");
        }
    }

    /**
     * 3. Thêm sản phẩm vào giỏ hàng.
     */
    public void themVaoGio(
            SanPhamBanHang sanPham,
            int soLuongThem
    ) {
        kiemTraSanPham(sanPham);

        if (soLuongThem <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng mua phải lớn hơn 0.");
        }

        DongGioHang dongCu =
                gioHang.get(sanPham.maSanPham());

        long soLuongMoi = (long) soLuongThem
                + (dongCu == null ? 0 : dongCu.soLuong());

        if (soLuongMoi > sanPham.soLuongTonKho()) {
            throw new IllegalArgumentException(
                    "Sản phẩm không đủ số lượng tồn kho.");
        }

        gioHang.put(
                sanPham.maSanPham(),
                new DongGioHang(sanPham, (int) soLuongMoi)
        );
    }

    /**
     * 4. Cập nhật số lượng trong giỏ.
     */
    public void capNhatSoLuong(
            String maSanPham,
            int soLuongMoi
    ) {
        DongGioHang dong = gioHang.get(maSanPham);

        if (dong == null) {
            throw new IllegalArgumentException(
                    "Sản phẩm không có trong giỏ hàng.");
        }

        kiemTraSanPham(dong.sanPham());

        if (soLuongMoi <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng mua phải lớn hơn 0.");
        }

        if (soLuongMoi > dong.sanPham().soLuongTonKho()) {
            throw new IllegalArgumentException(
                    "Sản phẩm không đủ số lượng tồn kho.");
        }

        gioHang.put(
                maSanPham,
                new DongGioHang(dong.sanPham(), soLuongMoi)
        );
    }

    /**
     * 5. Xóa một sản phẩm khỏi giỏ.
     */
    public void xoaKhoiGio(String maSanPham) {
        gioHang.remove(maSanPham);
    }

    /**
     * 6. Xóa toàn bộ giỏ hàng.
     */
    public void xoaToanBoGio() {
        gioHang.clear();
    }

    /**
     * 7. Lấy danh sách các dòng trong giỏ.
     */
    public List<DongGioHang> layGioHang() {
        return new ArrayList<>(gioHang.values());
    }

    /**
     * 8. Tính tổng tiền hàng trước ưu đãi.
     */
    public BigDecimal tinhTongTienHang() {
        return gioHang.values().stream()
                .map(DongGioHang::thanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
