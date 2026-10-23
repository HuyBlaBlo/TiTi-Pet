package com.titipet.entity;

import java.math.BigDecimal;

/**
 * DTO chứa dữ liệu sản phẩm phục vụ màn hình bán hàng.
 * Không thay đổi lớp SanPham của module Kho.
 */
public record SanPhamBanHang(
        String maSanPham,
        String tenSanPham,
        String maVach,
        BigDecimal giaBan,
        int soLuongTonKho
) {
}