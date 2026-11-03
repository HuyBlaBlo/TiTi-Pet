package com.titipet.entity;

import com.titipet.entity.enums.HangTV;

import java.math.BigDecimal;

public class KhachHang {
    private String maKH;
    private String hoTen;
    private String soDienThoai;
    private HangTV hangTV;
    private int diemTichLuy;

    public KhachHang() {
    }

    public KhachHang(String maKH, String hoTen, String soDienThoai, HangTV hangTV, int diemTichLuy) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.hangTV = hangTV;
        this.diemTichLuy = diemTichLuy;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public HangTV getHangTV() {
        return hangTV;
    }

    public void setHangTV(HangTV hangTV) {
        this.hangTV = hangTV;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    // Cộng điểm
    public void congDiem(int diemThem) {
        if (diemThem > 0) {
            this.diemTichLuy += diemThem;
        }
    }

    // Trừ điểm
    public boolean truDiem(int diemTru) {
        if (diemTru <= 0 || diemTru > this.diemTichLuy) {
            return false;
        }
        this.diemTichLuy -= diemTru;
        return true;
    }
}
