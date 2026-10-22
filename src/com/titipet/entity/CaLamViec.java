package com.titipet.entity;

import com.titipet.entity.enums.TrangThaiCa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CaLamViec {
    private String maCa;
    private NhanVien nhanVien;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private BigDecimal tienDauCa;
    private TrangThaiCa trangThai;
    private String ghiChu;

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public TrangThaiCa getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiCa trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getTienDauCa() {
        return tienDauCa;
    }

    public void setTienDauCa(BigDecimal tienDauCa) {
        this.tienDauCa = tienDauCa;
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getMaCa() {
        return maCa;
    }

    public void setMaCa(String maCa) {
        this.maCa = maCa;
    }

    public CaLamViec(String maCa, NhanVien nhanVien, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc, BigDecimal tienDauCa, TrangThaiCa trangThai, String ghiChu) {
        this.maCa = maCa;
        this.nhanVien = nhanVien;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.tienDauCa = tienDauCa;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public CaLamViec() {
    }
}
