package com.titipet.entity.enums;

public enum LoaiThuCungApDung {
	CHO("Chó"),
    MEO("Mèo");

    private final String tenHienThi;

    LoaiThuCungApDung(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    @Override
    public String toString() {
        return tenHienThi; // Khi đưa vào JComboBox sẽ tự động hiện "Chó" / "Mèo"
    }
}
