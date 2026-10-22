package com.titipet.entity;

public class ThuCung {
    private String maThuCung;
    private String tenThuCung;
    private String loaiThuCung;
    private double canNang;
    private String tinhTrangLong;
    private String ghiChu;
    private KhachHang chuNuoi;

    public ThuCung() {
    }

    public ThuCung(String maThuCung, String tenThuCung, String loaiThuCung, double canNang, String tinhTrangLong, String ghiChu, KhachHang chuNuoi) {
        this.maThuCung = maThuCung;
        this.tenThuCung = tenThuCung;
        this.loaiThuCung = loaiThuCung;
        this.canNang = canNang;
        this.tinhTrangLong = tinhTrangLong;
        this.ghiChu = ghiChu;
        this.chuNuoi = chuNuoi;
    }

    public String getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getLoaiThuCung() {
        return loaiThuCung;
    }

    public void setLoaiThuCung(String loaiThuCung) {
        this.loaiThuCung = loaiThuCung;
    }

    public double getCanNang() {
        return canNang;
    }

    public void setCanNang(double canNang) {
        this.canNang = canNang;
    }

    public String getTinhTrangLong() {
        return tinhTrangLong;
    }

    public void setTinhTrangLong(String tinhTrangLong) {
        this.tinhTrangLong = tinhTrangLong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public KhachHang getChuNuoi() {
        return chuNuoi;
    }

    public void setChuNuoi(KhachHang chuNuoi) {
        this.chuNuoi = chuNuoi;
    }

    // Cập nhật thông tin kiểm tra khi tiếp nhận Spa
    public void capNhatKiemTra(
            double canNang,
            String tinhTrangLong) {

        this.canNang = canNang;
        this.tinhTrangLong = tinhTrangLong;
    }
}
