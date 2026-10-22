package com.titipet.entity;

import com.titipet.entity.enums.TrangThaiLichHen;

import java.time.LocalDateTime;
import java.util.List;

public class LichHen {
    private String maLichHen;
    private LocalDateTime thoiGianHen;
    private TrangThaiLichHen trangThai;
    private KhachHang khachHang;
    private DichVuSpa dichVu;
    private String ghiChu;
    private ThuCung thuCung;
    private NhanVien thoSpa;

    public LichHen() {
    }

    public LichHen(String maLichHen, LocalDateTime thoiGianHen, TrangThaiLichHen trangThai, KhachHang khachHang, DichVuSpa dichVu, String ghiChu, ThuCung thuCung, NhanVien thoSpa) {
        this.maLichHen = maLichHen;
        this.thoiGianHen = thoiGianHen;
        this.trangThai = trangThai;
        this.khachHang = khachHang;
        this.dichVu = dichVu;
        this.ghiChu = ghiChu;
        this.thuCung = thuCung;
        this.thoSpa = thoSpa;
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(LocalDateTime thoiGianHen) {
        this.thoiGianHen = thoiGianHen;
    }

    public TrangThaiLichHen getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiLichHen trangThai) {
        this.trangThai = trangThai;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public DichVuSpa getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVuSpa dichVu) {
        this.dichVu = dichVu;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
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

    // Kiểm tra lịch hẹn có bị trùng hay không
    public boolean kiemTraLichTrung(List<LichHen> dsLichHen) {
        if (thoiGianHen == null || thoSpa == null || dsLichHen == null) {
            return false;
        }
        for (LichHen lich : dsLichHen) {
            if (lich == null || lich == this) {
                continue;
            }
            // Không so sánh với lịch đã hủy
            if (lich.getTrangThai() == TrangThaiLichHen.DA_HUY) {
                continue;
            }
            // Cùng nhân viên Spa
            boolean cungTho = thoSpa.getMaNV()
                    .equals(lich.getThoSpa().getMaNV());
            // Cùng thời gian hẹn
            boolean cungThoiGian = thoiGianHen
                    .equals(lich.getThoiGianHen());
            if (cungTho && cungThoiGian) {
                return true;
            }
        }
        return false;
    }

}
