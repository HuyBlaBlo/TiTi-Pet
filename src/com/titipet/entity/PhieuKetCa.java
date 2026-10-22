package com.titipet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PhieuKetCa {
    private NhanVien maNV;
    private CaLamViec maCa;
    private BigDecimal soTienBatDauCa;
    private BigDecimal tienMat;
    private BigDecimal tienCK;
    private LocalDateTime ngayLapPhieu;

    public PhieuKetCa(NhanVien maNV, CaLamViec maCa, BigDecimal soTienBatDauCa, BigDecimal tienMat, BigDecimal tienCK, LocalDateTime ngayLapPhieu) {
        this.maNV = maNV;
        this.maCa = maCa;
        this.soTienBatDauCa = soTienBatDauCa;
        this.tienMat = tienMat;
        this.tienCK = tienCK;
        this.ngayLapPhieu = ngayLapPhieu;
    }

    public PhieuKetCa() {
    }

    public NhanVien getMaNV() {
        return maNV;
    }

    public void setMaNV(NhanVien maNV) {
        this.maNV = maNV;
    }

    public CaLamViec getMaCa() {
        return maCa;
    }

    public void setMaCa(CaLamViec maCa) {
        this.maCa = maCa;
    }

    public BigDecimal getSoTienBatDauCa() {
        return soTienBatDauCa;
    }

    public void setSoTienBatDauCa(BigDecimal soTienBatDauCa) {
        this.soTienBatDauCa = soTienBatDauCa;
    }

    public BigDecimal getTienMat() {
        return tienMat;
    }

    public void setTienMat(BigDecimal tienMat) {
        this.tienMat = tienMat;
    }

    public BigDecimal getTienCK() {
        return tienCK;
    }

    public void setTienCK(BigDecimal tienCK) {
        this.tienCK = tienCK;
    }

    public LocalDateTime getNgayLapPhieu() {
        return ngayLapPhieu;
    }

    public void setNgayLapPhieu(LocalDateTime ngayLapPhieu) {
        this.ngayLapPhieu = ngayLapPhieu;
    }

    // Tính tổng tiền kết ca
    public BigDecimal tinhTongTienKetCa() {
        return tienMat.add(tienCK);
    }
}
