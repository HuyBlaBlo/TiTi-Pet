package com.titipet.entity;

/** DTO khách hàng cho cửa sổ POS; không thay thế entity KhachHang của module khác. */
public record KhachHangBanHang(
        String maKH,
        String hoTen,
        String soDienThoai,
        String hangTV,
        int diemTichLuy
) {
    @Override public String toString() {
        return hoTen + " · " + soDienThoai + " (" + maKH + ")";
    }
}
