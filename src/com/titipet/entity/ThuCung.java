package com.titipet.entity;

import java.util.Objects;

import com.titipet.entity.enums.LoaiThuCungApDung;

public class ThuCung {
    private String maThuCung;		// VARCHAR(20) - Khóa chính
    private String tenThuCung;		// NVARCHAR(120)
    private LoaiThuCungApDung loaiThuCung;		// VARCHAR(10) - Chỉ nhận 'CHO' hoặc 'MEO'
    private double canNang;			// DECIMAL(8,2) - Lớn hơn 0
    private String tinhTrangLong;	// NVARCHAR(300)
    private String ghiChu;			// NVARCHAR(1000)
    private KhachHang chuNuoi;		// Khóa ngoại maKH VARCHAR(20)

    public ThuCung() {
    }

    public ThuCung(String maThuCung, String tenThuCung, LoaiThuCungApDung loaiThuCung, double canNang, String tinhTrangLong, String ghiChu, KhachHang chuNuoi) {
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
        this.maThuCung = (maThuCung != null) ? maThuCung.trim() : null;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = (tenThuCung != null) ? tenThuCung.trim() : "";
    }

    public LoaiThuCungApDung getLoaiThuCung() {
        return loaiThuCung;
    }

    public void setLoaiThuCung(LoaiThuCungApDung loaiThuCung) {
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

	@Override
	public int hashCode() {
		return Objects.hash(maThuCung);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThuCung other = (ThuCung) obj;
		return Objects.equals(maThuCung, other.maThuCung);
	}

	@Override
	public String toString() {
		return tenThuCung != null ? tenThuCung : "";
	}
    
}
