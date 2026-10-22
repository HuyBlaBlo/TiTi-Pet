package com.titipet.entity;

import com.titipet.entity.enums.LoaiSanPham;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class PhuKien extends SanPham{
    private String kichThuoc;
    private String mauSac;
    private String chatLieu;

    public PhuKien(String kichThuoc, String mauSac, String chatLieu) {
        this.kichThuoc = kichThuoc;
        this.mauSac = mauSac;
        this.chatLieu = chatLieu;
    }

    public PhuKien(String maSanPham, String tenSanPham, BigDecimal giaBan, BigDecimal giaNhap, int soLuongTonKho, String maVach, BigDecimal thueSuat, LoaiSanPham loaiSanPham, int nguongCanhBaoTon, String kichThuoc, String mauSac, String chatLieu) {
        super(maSanPham, tenSanPham, giaBan, giaNhap, soLuongTonKho, maVach, thueSuat, loaiSanPham, nguongCanhBaoTon);
        this.kichThuoc = kichThuoc;
        this.mauSac = mauSac;
        this.chatLieu = chatLieu;
    }

    public PhuKien() {
    }

    public String getKichThuoc() {
        return kichThuoc;
    }

    public void setKichThuoc(String kichThuoc) {
        this.kichThuoc = kichThuoc;
    }

    public String getMauSac() {
        return mauSac;
    }

    public void setMauSac(String mauSac) {
        this.mauSac = mauSac;
    }

    public String getChatLieu() {
        return chatLieu;
    }

    public void setChatLieu(String chatLieu) {
        this.chatLieu = chatLieu;
    }
}
