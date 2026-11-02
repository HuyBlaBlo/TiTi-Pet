package com.titipet.entity;

import com.titipet.entity.enums.LoaiSanPham;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class SanPham {
    private String maSanPham;
    private String tenSanPham;
    private BigDecimal giaBan;
    private BigDecimal giaNhap;
    private int soLuongTonKho;
    private String maVach;
    private BigDecimal thueSuat;
    private LoaiSanPham loaiSanPham;
    private int nguongCanhBaoTon;

    public SanPham() {
    }

    public SanPham(String maSanPham, String tenSanPham, BigDecimal giaBan, BigDecimal giaNhap, int soLuongTonKho, String maVach, BigDecimal thueSuat, LoaiSanPham loaiSanPham, int nguongCanhBaoTon) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.giaBan = giaBan;
        this.giaNhap = giaNhap;
        this.soLuongTonKho = soLuongTonKho;
        this.maVach = maVach;
        this.thueSuat = thueSuat;
        this.loaiSanPham = loaiSanPham;
        this.nguongCanhBaoTon = nguongCanhBaoTon;
    }

    public int getSoLuongTonKho() {
        return soLuongTonKho;
    }

    public void setSoLuongTonKho(int soLuongTonKho) {
        this.soLuongTonKho = soLuongTonKho;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }

    public BigDecimal getGiaNhap() {
        return giaNhap;
    }

    public void setGiaNhap(BigDecimal giaNhap) {
        this.giaNhap = giaNhap;
    }

    public String getMaVach() {
        return maVach;
    }

    public void setMaVach(String maVach) {
        this.maVach = maVach;
    }

    public BigDecimal getThueSuat() {
        return thueSuat;
    }

    public void setThueSuat(BigDecimal thueSuat) {
        this.thueSuat = thueSuat;
    }

    public LoaiSanPham getLoaiSanPham() {
        return loaiSanPham;
    }

    public void setLoaiSanPham(LoaiSanPham loaiSanPham) {
        this.loaiSanPham = loaiSanPham;
    }

    public int getNguongCanhBaoTon() {
        return nguongCanhBaoTon;
    }

    public void setNguongCanhBaoTon(int nguongCanhBaoTon) {
        this.nguongCanhBaoTon = nguongCanhBaoTon;
    }

    // Kiểm tra số lượng tồn kho có đủ để bán không
    public boolean kiemTraTonKho(int soLuongYC) {
        return soLuongYC > 0 && soLuongTonKho >= soLuongYC;
    }

	@Override
	public int hashCode() {
		return Objects.hash(maSanPham);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SanPham other = (SanPham) obj;
		return Objects.equals(maSanPham, other.maSanPham);
	}
    
	@Override
    public String toString() {
        return tenSanPham + " (Tồn: " + soLuongTonKho + ")";
    }
}
