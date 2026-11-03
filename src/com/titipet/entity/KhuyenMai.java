package com.titipet.entity;

import com.titipet.entity.enums.PhamVi;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KhuyenMai {
    private String maKhuyenMai;
    private String tenChuongTrinh;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private BigDecimal giaGiam;
    private boolean trangThai;
    private PhamVi phamVi;
    private SanPham sanPhamApDung;
    private DichVuSpa dichVuApDung;

    public KhuyenMai() {
    }

    public KhuyenMai(String maKhuyenMai, String tenChuongTrinh, LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc, BigDecimal giaGiam, boolean trangThai, PhamVi phamVi, SanPham sanPhamApDung, DichVuSpa dichVuApDung) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenChuongTrinh = tenChuongTrinh;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.giaGiam = giaGiam;
        this.trangThai = trangThai;
        this.phamVi = phamVi;
        this.sanPhamApDung = sanPhamApDung;
        this.dichVuApDung = dichVuApDung;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public String getTenChuongTrinh() {
        return tenChuongTrinh;
    }

    public void setTenChuongTrinh(String tenChuongTrinh) {
        this.tenChuongTrinh = tenChuongTrinh;
    }

    public LocalDateTime getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDateTime ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDateTime getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDateTime ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public BigDecimal getGiaGiam() {
        return giaGiam;
    }

    public void setGiaGiam(BigDecimal giaGiam) {
        this.giaGiam = giaGiam;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public PhamVi getPhamVi() {
        return phamVi;
    }

    public void setPhamVi(PhamVi phamVi) {
        this.phamVi = phamVi;
    }

    public SanPham getSanPhamApDung() {
        return sanPhamApDung;
    }

    public void setSanPhamApDung(SanPham sanPhamApDung) {
        this.sanPhamApDung = sanPhamApDung;
    }

    public DichVuSpa getDichVuApDung() {
        return dichVuApDung;
    }

    public void setDichVuApDung(DichVuSpa dichVuApDung) {
        this.dichVuApDung = dichVuApDung;
    }

    //Kiểm tra khuyến mãi có hiệu lực hay không
    public boolean kiemTraHieuLuc(LocalDateTime thoiDiem) {
        if (!trangThai || thoiDiem == null || ngayBatDau == null || ngayKetThuc == null) {
            return false;
        }
        return !thoiDiem.isBefore(ngayBatDau) && !thoiDiem.isAfter(ngayKetThuc);
    }

    //Tính số tiền được giảm
    public BigDecimal tinhTienGiam(BigDecimal giaTriDuDieuKien) {
        if (giaTriDuDieuKien == null || giaTriDuDieuKien.signum() <= 0 || giaGiam == null || giaGiam.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return giaGiam.min(giaTriDuDieuKien);
    }

}
