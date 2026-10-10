package com.titipet.entity;

import java.time.LocalDateTime;

public class CaLamViec {
    private String maCa;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private NhanVien nhanVienTruc;
    private String ghiChu;


    public CaLamViec(String maCa, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc, NhanVien nhanVienTruc, String ghiChu) {
        this.maCa = maCa;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.nhanVienTruc = nhanVienTruc;
        this.ghiChu = ghiChu;
    }

    public CaLamViec() {
    }


}
