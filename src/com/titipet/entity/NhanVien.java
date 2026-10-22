package com.titipet.entity;

import com.titipet.entity.enums.VaiTro;

public class NhanVien {
    private String maNV;
    private String matKhau;
    private String hoTen;
    private VaiTro vaiTro;
    private String tenDangNhap;

    public NhanVien() {
    }

    public NhanVien(String maNV, String matKhau, String hoTen, VaiTro vaiTro, String tenDangNhap) {
        this.maNV = maNV;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
        this.tenDangNhap = tenDangNhap;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(VaiTro vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

}
