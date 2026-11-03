package com.titipet.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO cho màn hình POS; không thay thế entity SanPham của module Kho. */
public record SanPhamBanHang(
        String maSanPham,
        String tenSanPham,
        String maVach,
        BigDecimal giaBan,
        int soLuongTonKho,
        String loaiSanPham,
        LocalDate hanSuDung
) {
    /** Tương thích với code POS bước 1 và các bài kiểm thử đã có. */
    public SanPhamBanHang(String maSanPham, String tenSanPham, String maVach,
                          BigDecimal giaBan, int soLuongTonKho) {
        this(maSanPham, tenSanPham, maVach, giaBan, soLuongTonKho, "PHU_KIEN", null);
    }
}
