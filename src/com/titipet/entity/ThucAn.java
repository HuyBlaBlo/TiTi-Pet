package com.titipet.entity;

import com.titipet.entity.enums.LoaiSanPham;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ThucAn extends SanPham{
    private LocalDate hanSuDung;
    private double trongLuong;

    public ThucAn() {
        super();
        setLoaiSanPham(LoaiSanPham.THUC_AN);
    }

    public ThucAn(String maSanPham, String tenSanPham, BigDecimal giaBan, BigDecimal giaNhap, int soLuongTonKho, String maVach, BigDecimal thueSuat, LoaiSanPham loaiSanPham, int nguongCanhBaoTon, LocalDate hanSuDung, double trongLuong) {
        super(maSanPham, tenSanPham, giaBan, giaNhap, soLuongTonKho, maVach, thueSuat, loaiSanPham, nguongCanhBaoTon);
        this.hanSuDung = hanSuDung;
        this.trongLuong = trongLuong;
    }

    public LocalDate getHanSuDung() {
		return hanSuDung;
	}

	public void setHanSuDung(LocalDate hanSuDung) {
		this.hanSuDung = hanSuDung;
	}

	public double getTrongLuong() {
		return trongLuong;
	}

	public void setTrongLuong(double trongLuong) {
		this.trongLuong = trongLuong;
	}

	// Kiểm tra thức ăn cận date (mặc định <= 30 ngày)
    public boolean kiemTraCanDate(int soNgayCanhBao) {
        if (hanSuDung == null || soNgayCanhBao < 0) {
            return false;
        }
        long soNgayConLai = ChronoUnit.DAYS.between(
        		LocalDate.now(), hanSuDung);
        return soNgayConLai >= 0 && soNgayConLai <= soNgayCanhBao;
    }

    // Kiểm tra sản phẩm đã quá hạn sử dụng chưa
    public boolean daHetHan() {
        if (hanSuDung == null) return false;
        return hanSuDung.isBefore(LocalDate.now());
    }
}
