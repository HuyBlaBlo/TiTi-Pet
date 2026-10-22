package com.titipet.entity;

import com.titipet.entity.enums.TrangThaiThucHien;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ChiTietHoaDon_DichVu {
    private long maChiTietDichVu;
    private HoaDon hoaDon;
    private DichVuSpa dichVu;
    private ThuCung thuCung;
    private NhanVien thoSpa;
    private TrangThaiThucHien trangThaiThucHien;
    private BigDecimal donGia;
    private BigDecimal thueSuat;
    private boolean giaDaBaoGomThue;
    private BigDecimal tienGiamPhanBo;
    private BigDecimal tienTruocThue;
    private BigDecimal tienThue;

    public ChiTietHoaDon_DichVu() {
    }

    public ChiTietHoaDon_DichVu(long maChiTietDichVu, HoaDon hoaDon, DichVuSpa dichVu, ThuCung thuCung, NhanVien thoSpa, TrangThaiThucHien trangThaiThucHien, BigDecimal donGia, BigDecimal thueSuat, boolean giaDaBaoGomThue, BigDecimal tienGiamPhanBo, BigDecimal tienTruocThue, BigDecimal tienThue) {
        this.maChiTietDichVu = maChiTietDichVu;
        this.hoaDon = hoaDon;
        this.dichVu = dichVu;
        this.thuCung = thuCung;
        this.thoSpa = thoSpa;
        this.trangThaiThucHien = trangThaiThucHien;
        this.donGia = donGia;
        this.thueSuat = thueSuat;
        this.giaDaBaoGomThue = giaDaBaoGomThue;
        this.tienGiamPhanBo = tienGiamPhanBo;
        this.tienTruocThue = tienTruocThue;
        this.tienThue = tienThue;
    }

    public long getMaChiTietDichVu() {
        return maChiTietDichVu;
    }

    public void setMaChiTietDichVu(long maChiTietDichVu) {
        this.maChiTietDichVu = maChiTietDichVu;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public DichVuSpa getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVuSpa dichVu) {
        this.dichVu = dichVu;
    }

    public ThuCung getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
    }

    public NhanVien getThoSpa() {
        return thoSpa;
    }

    public void setThoSpa(NhanVien thoSpa) {
        this.thoSpa = thoSpa;
    }

    public TrangThaiThucHien getTrangThaiThucHien() {
        return trangThaiThucHien;
    }

    public void setTrangThaiThucHien(TrangThaiThucHien trangThaiThucHien) {
        this.trangThaiThucHien = trangThaiThucHien;
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


    // 1. Tính tiền trước thuế, chưa giảm giá
    public BigDecimal tinhTienTruocThue() {
        if (!giaDaBaoGomThue) {
            return donGia;
        }

        BigDecimal heSoThue = BigDecimal.ONE.add(
                thueSuat.divide(BigDecimal.valueOf(100))
        );

        return donGia.divide(
                heSoThue, 2, RoundingMode.HALF_UP
        );
    }

    // 2. Tính giá trị sau giảm, trước thuế
    public BigDecimal tinhGiaTriSauGiam() {
        BigDecimal ketQua = tinhTienTruocThue()
                .subtract(tienGiamPhanBo);

        return ketQua.max(BigDecimal.ZERO);
    }

    // 3. Tính tiền thuế
    public BigDecimal tinhTienThue() {
        return tinhGiaTriSauGiam()
                .multiply(thueSuat)
                .divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP);
    }

    // 4. Tính thành tiền sau thuế
    public BigDecimal tinhThanhTien() {
        return tinhGiaTriSauGiam()
                .add(tinhTienThue());
    }
}
