package com.titipet.entity;

import java.math.BigDecimal;

public class ChiTietHoaDon_SanPham {
    private HoaDon hoaDon;
    private SanPham sanPham;
    private int soLuong;
    private BigDecimal donGia;
    private BigDecimal thueSuat;
    private boolean giaDaBaoGomThue;
    private BigDecimal tienGiamPhanBo;
    private BigDecimal tienTruocThue;
    private BigDecimal tienThue;

    public ChiTietHoaDon_SanPham() {
    }

    public ChiTietHoaDon_SanPham(HoaDon hoaDon, SanPham sanPham, int soLuong, BigDecimal donGia, BigDecimal thueSuat, boolean giaDaBaoGomThue, BigDecimal tienGiamPhanBo, BigDecimal tienTruocThue, BigDecimal tienThue) {
        this.hoaDon = hoaDon;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thueSuat = thueSuat;
        this.giaDaBaoGomThue = giaDaBaoGomThue;
        this.tienGiamPhanBo = tienGiamPhanBo;
        this.tienTruocThue = tienTruocThue;
        this.tienThue = tienThue;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
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

    public BigDecimal getTienGiamPhanBo() {
        return tienGiamPhanBo;
    }

    public void setTienGiamPhanBo(BigDecimal tienGiamPhanBo) {
        this.tienGiamPhanBo = tienGiamPhanBo;
    }

    public BigDecimal getTienTruocThue() {
        return tienTruocThue;
    }

    public void setTienTruocThue(BigDecimal tienTruocThue) {
        this.tienTruocThue = tienTruocThue;
    }

    public BigDecimal getTienThue() {
        return tienThue;
    }

    public void setTienThue(BigDecimal tienThue) {
        this.tienThue = tienThue;
    }
}
