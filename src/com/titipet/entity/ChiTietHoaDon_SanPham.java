package com.titipet.entity;

public class ChiTietHoaDon_SanPham {
    private HoaDon hoaDon;
    private DichVuSpa dichVuSpa;
    private int soLuong;
    private double donGia;

    public ChiTietHoaDon_SanPham(HoaDon hoaDon, DichVuSpa dichVuSpa, int soLuong, double donGia) {
        this.hoaDon = hoaDon;
        this.dichVuSpa = dichVuSpa;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public ChiTietHoaDon_SanPham() {
    }

}
