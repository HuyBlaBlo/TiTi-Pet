package com.titipet.entity.enums;

public enum LoaiSanPham {
	THUC_AN("Thức ăn"),
    PHU_KIEN("Phụ kiện");

    private final String tenHienThi;

    LoaiSanPham(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    @Override
    public String toString() {
        return tenHienThi;
    }
}
