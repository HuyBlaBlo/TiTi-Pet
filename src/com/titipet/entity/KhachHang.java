package com.titipet.entity;

import com.titipet.entity.enums.HangTV;

import java.math.BigDecimal;
import java.util.Objects;

public class KhachHang {
    private String maKH;
    private String hoTen;
    private String soDienThoai;
    // Hạng thành viên: Enum HangTV ('THUONG','HANG_1','HANG_2','HANG_3')
    private HangTV hangTV;
    // Điểm tích lũy hiện có: INT >= 0
    private int diemTichLuy;
    
    // Constructor mặc định
    public KhachHang() {
    	this.hangTV = HangTV.THUONG;
    	this.diemTichLuy = 0;
    }

    // Constructor khởi tạo khi thêm mới tại quầy (mặc định điểm = 0)
    public KhachHang(String maKH, String hoTen, String soDienThoai, HangTV hangTV) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.hangTV = (hangTV != null) ? hangTV : HangTV.THUONG;;
        this.diemTichLuy = 0;
    }
    
    // Constructor đầy đủ tham số khi đọc từ CSDL lên
    public KhachHang(String maKH, String hoTen, String soDienThoai, HangTV hangTV, int diemTichLuy) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.hangTV = (hangTV != null) ? hangTV : HangTV.THUONG;
        this.diemTichLuy = Math.max(0, diemTichLuy);
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public HangTV getHangTV() {
        return hangTV;
    }

    public void setHangTV(HangTV hangTV) {
        this.hangTV = (hangTV != null) ? hangTV : HangTV.THUONG;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = Math.max(0, diemTichLuy);
    }

    /**
     * Cộng điểm tích lũy sau khi hoàn tất hóa đơn hợp lệ.
     * @param diemThem Số điểm được cộng (diemThem > 0)
     */
    public void congDiem(int diemThem) {
        if (diemThem > 0) {
            this.diemTichLuy += diemThem;
        }
    }

    /**
     * Trừ điểm tích lũy khi khách hàng dùng điểm để giảm trừ tiền.
     * @param diemTru Số điểm muốn sử dụng (0 < diemTru <= diemTichLuy)
     * @return true nếu trừ điểm thành công, ngược lại false
     */
    public boolean truDiem(int diemTru) {
        if (diemTru <= 0 || diemTru > this.diemTichLuy) {
            return false;
        }
        this.diemTichLuy -= diemTru;
        return true;
    }

	@Override
	public int hashCode() {
		return Objects.hash(maKH);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KhachHang other = (KhachHang) obj;
		return Objects.equals(maKH, other.maKH);
	}

	@Override
	public String toString() {
		return "KhachHang{" +
                "maKH='" + maKH + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", hangTV=" + hangTV +
                ", diemTichLuy=" + diemTichLuy +
                '}';
	}
    
	
    
}
