package com.titipet.entity;

import com.titipet.entity.enums.NguonDon;
import com.titipet.entity.enums.PhuongThucThanhToan;
import com.titipet.entity.enums.TrangThaiHoaDon;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HoaDon {
    private String maHoaDon;
    private LocalDateTime ngayLap;
    private KhachHang khachHang;
    private NhanVien nhanVien;
    private CaLamViec caLamViec;
    private KhuyenMai khuyenMai;
    private NguonDon nguonDon;
    private String maDonKenh;
    private PhuongThucThanhToan phuongThucThanhToan;
    private TrangThaiHoaDon trangThai;
    private BigDecimal tongTienHang;
    private BigDecimal tongTienSpa;
    private BigDecimal tienGiamKM;
    private BigDecimal tienGiamHangTV;
    private int soDiemSuDung;
    private BigDecimal tienGiamDiem;
    private BigDecimal tienPhaiThu;
    private int soDiemDuocTich;
    private BigDecimal tongTienTruocThue;
    private BigDecimal tongTienThue;
    private BigDecimal tongTien;

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, LocalDateTime ngayLap, KhachHang khachHang, NhanVien nhanVien, CaLamViec caLamViec, KhuyenMai khuyenMai, NguonDon nguonDon, String maDonKenh, PhuongThucThanhToan phuongThucThanhToan, TrangThaiHoaDon trangThai, BigDecimal tongTienHang, BigDecimal tongTienSpa, BigDecimal tienGiamKM, BigDecimal tienGiamHangTV, int soDiemSuDung, BigDecimal tienGiamDiem, BigDecimal tienPhaiThu, int soDiemDuocTich, BigDecimal tongTienTruocThue, BigDecimal tongTienThue, BigDecimal tongTien) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.caLamViec = caLamViec;
        this.khuyenMai = khuyenMai;
        this.nguonDon = nguonDon;
        this.maDonKenh = maDonKenh;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.trangThai = trangThai;
        this.tongTienHang = tongTienHang;
        this.tongTienSpa = tongTienSpa;
        this.tienGiamKM = tienGiamKM;
        this.tienGiamHangTV = tienGiamHangTV;
        this.soDiemSuDung = soDiemSuDung;
        this.tienGiamDiem = tienGiamDiem;
        this.tienPhaiThu = tienPhaiThu;
        this.soDiemDuocTich = soDiemDuocTich;
        this.tongTienTruocThue = tongTienTruocThue;
        this.tongTienThue = tongTienThue;
        this.tongTien = tongTien;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public CaLamViec getCaLamViec() {
        return caLamViec;
    }

    public void setCaLamViec(CaLamViec caLamViec) {
        this.caLamViec = caLamViec;
    }

    public KhuyenMai getKhuyenMai() {
        return khuyenMai;
    }

    public void setKhuyenMai(KhuyenMai khuyenMai) {
        this.khuyenMai = khuyenMai;
    }

    public NguonDon getNguonDon() {
        return nguonDon;
    }

    public void setNguonDon(NguonDon nguonDon) {
        this.nguonDon = nguonDon;
    }

    public String getMaDonKenh() {
        return maDonKenh;
    }

    public void setMaDonKenh(String maDonKenh) {
        this.maDonKenh = maDonKenh;
    }

    public PhuongThucThanhToan getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(PhuongThucThanhToan phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public TrangThaiHoaDon getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiHoaDon trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getTongTienHang() {
        return tongTienHang;
    }

    public void setTongTienHang(BigDecimal tongTienHang) {
        this.tongTienHang = tongTienHang;
    }

    public BigDecimal getTongTienSpa() {
        return tongTienSpa;
    }

    public void setTongTienSpa(BigDecimal tongTienSpa) {
        this.tongTienSpa = tongTienSpa;
    }

    public BigDecimal getTienGiamKM() {
        return tienGiamKM;
    }

    public void setTienGiamKM(BigDecimal tienGiamKM) {
        this.tienGiamKM = tienGiamKM;
    }

    public BigDecimal getTienGiamHangTV() {
        return tienGiamHangTV;
    }

    public void setTienGiamHangTV(BigDecimal tienGiamHangTV) {
        this.tienGiamHangTV = tienGiamHangTV;
    }

    public int getSoDiemSuDung() {
        return soDiemSuDung;
    }

    public void setSoDiemSuDung(int soDiemSuDung) {
        this.soDiemSuDung = soDiemSuDung;
    }

    public BigDecimal getTienGiamDiem() {
        return tienGiamDiem;
    }

    public void setTienGiamDiem(BigDecimal tienGiamDiem) {
        this.tienGiamDiem = tienGiamDiem;
    }

    public BigDecimal getTienPhaiThu() {
        return tienPhaiThu;
    }

    public void setTienPhaiThu(BigDecimal tienPhaiThu) {
        this.tienPhaiThu = tienPhaiThu;
    }

    public int getSoDiemDuocTich() {
        return soDiemDuocTich;
    }

    public void setSoDiemDuocTich(int soDiemDuocTich) {
        this.soDiemDuocTich = soDiemDuocTich;
    }

    public BigDecimal getTongTienTruocThue() {
        return tongTienTruocThue;
    }

    public void setTongTienTruocThue(BigDecimal tongTienTruocThue) {
        this.tongTienTruocThue = tongTienTruocThue;
    }

    public BigDecimal getTongTienThue() {
        return tongTienThue;
    }

    public void setTongTienThue(BigDecimal tongTienThue) {
        this.tongTienThue = tongTienThue;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }
}
