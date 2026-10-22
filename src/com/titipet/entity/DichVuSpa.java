package com.titipet.entity;

import com.titipet.entity.enums.LoaiThuCungApDung;

import java.math.BigDecimal;

public class DichVuSpa {
    private String maDichVu;
    private String tenDichVu;
    private BigDecimal donGiaCoBan;
    private BigDecimal thueSuat;
    private boolean giaDaBaoGomThue;
    private LoaiThuCungApDung loaiThuCungApDung;
    private boolean dangHoatDong;

    public DichVuSpa() {
    }

    public DichVuSpa(String maDichVu, String tenDichVu, BigDecimal donGiaCoBan, BigDecimal thueSuat, boolean giaDaBaoGomThue, LoaiThuCungApDung loaiThuCungApDung, boolean dangHoatDong) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.donGiaCoBan = donGiaCoBan;
        this.thueSuat = thueSuat;
        this.giaDaBaoGomThue = giaDaBaoGomThue;
        this.loaiThuCungApDung = loaiThuCungApDung;
        this.dangHoatDong = dangHoatDong;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public BigDecimal getDonGiaCoBan() {
        return donGiaCoBan;
    }

    public void setDonGiaCoBan(BigDecimal donGiaCoBan) {
        this.donGiaCoBan = donGiaCoBan;
    }

    public BigDecimal getThueSuat() {
        return thueSuat;
    }

    public void setThueSuat(BigDecimal thueSuat) {
        this.thueSuat = thueSuat;
    }

    public boolean isGiaDaBaoGomThue() {
        return giaDaBaoGomThue;
    }

    public void setGiaDaBaoGomThue(boolean giaDaBaoGomThue) {
        this.giaDaBaoGomThue = giaDaBaoGomThue;
    }

    public LoaiThuCungApDung getLoaiThuCungApDung() {
        return loaiThuCungApDung;
    }

    public void setLoaiThuCungApDung(LoaiThuCungApDung loaiThuCungApDung) {
        this.loaiThuCungApDung = loaiThuCungApDung;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public void setDangHoatDong(boolean dangHoatDong) {
        this.dangHoatDong = dangHoatDong;
    }
}
