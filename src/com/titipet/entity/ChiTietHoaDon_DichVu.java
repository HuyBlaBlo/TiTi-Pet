package com.titipet.entity;

public class ChiTietHoaDon_DichVu {
    private HoaDon haoDon;
    private DichVuSpa dichVuSpa;
    private int soLuong;
    private double donGia;

    public ChiTietHoaDon_DichVu(HoaDon haoDon, DichVuSpa dichVuSpa, int soLuong, double donGia) {
        this.haoDon = haoDon;
        this.dichVuSpa = dichVuSpa;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public ChiTietHoaDon_DichVu() {
    }

}
