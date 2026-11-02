package com.titipet.entity;

import com.titipet.entity.enums.LoaiSanPham;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ThucAn extends SanPham{
    private LocalDateTime hanSuDung;
    private double trongLuong;

    public ThucAn() {
        super();
        setLoaiSanPham(LoaiSanPham.THUC_AN);
    }

    public ThucAn(String maSanPham, String tenSanPham, BigDecimal giaBan, BigDecimal giaNhap, int soLuongTonKho, String maVach, BigDecimal thueSuat, LoaiSanPham loaiSanPham, int nguongCanhBaoTon, LocalDateTime hanSuDung, double trongLuong) {
        super(maSanPham, tenSanPham, giaBan, giaNhap, soLuongTonKho, maVach, thueSuat, loaiSanPham, nguongCanhBaoTon);
        this.hanSuDung = hanSuDung;
        this.trongLuong = trongLuong;
    }

    public LocalDateTime getHanSuDung() {
		return hanSuDung;
	}

	public void setHanSuDung(LocalDateTime hanSuDung) {
		this.hanSuDung = hanSuDung;
	}

	public double getTrongLuong() {
		return trongLuong;
	}

	public void setTrongLuong(double trongLuong) {
		this.trongLuong = trongLuong;
	}

	// Kiểm tra thức ăn sắp hết hạn hoặc đã hết hạn
    public boolean kiemTraCanDate(int soNgayCanhBao) {

        if (hanSuDung == null || soNgayCanhBao < 0) {
            return false;
        }

        long soNgayConLai = ChronoUnit.DAYS.between(
                LocalDateTime.now(),
                hanSuDung
        );

        return soNgayConLai <= soNgayCanhBao;
    }
}
