package com.titipet.entity;

import com.titipet.entity.enums.NguonDon;
import com.titipet.entity.enums.PhuongThucThanhToan;
import com.titipet.entity.enums.TrangThaiHoaDon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    private String maHoaDon;
    private LocalDateTime ngayLap;
    private KhachHang khachHang;
    private NhanVien nhanVien;
    private KhuyenMai khuyenMai;
    private CaLamViec caLamViec;
    private NguonDon nguonDon;
    private String maDonKenh;
    private PhuongThucThanhToan phuongThucThanhToan;
    private TrangThaiHoaDon trangThai;
    private int soDiemSuDung;

    private List<ChiTietHoaDon_SanPham> chiTietSanPhams;
    private List<ChiTietHoaDon_DichVu> chiTietDichVus;

    public HoaDon(String maHoaDon, LocalDateTime ngayLap, KhachHang khachHang, NhanVien nhanVien, KhuyenMai khuyenMai, CaLamViec caLamViec, NguonDon nguonDon, String maDonKenh, PhuongThucThanhToan phuongThucThanhToan, TrangThaiHoaDon trangThai, int soDiemSuDung, List<ChiTietHoaDon_SanPham> chiTietSanPhams, List<ChiTietHoaDon_DichVu> chiTietDichVus) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.khuyenMai = khuyenMai;
        this.caLamViec = caLamViec;
        this.nguonDon = nguonDon;
        this.maDonKenh = maDonKenh;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.trangThai = trangThai;
        this.soDiemSuDung = soDiemSuDung;
        this.chiTietSanPhams = chiTietSanPhams;
        this.chiTietDichVus = chiTietDichVus;
    }

    public HoaDon() {
        this.ngayLap = LocalDateTime.now();
        this.chiTietSanPhams = new ArrayList<>();
        this.chiTietDichVus = new ArrayList<>();
    }

    // 1. Tổng tiền sản phẩm
    public BigDecimal tinhTongTienHang() {
        BigDecimal tong = BigDecimal.ZERO;

        for (ChiTietHoaDon_SanPham ct : chiTietSanPhams) {
            tong = tong.add(ct.tinhThanhTien());
        }

        return tong;
    }

    // 2. Tổng tiền dịch vụ Spa
    public BigDecimal tinhTongTienSpa() {
        BigDecimal tong = BigDecimal.ZERO;

        for (ChiTietHoaDon_DichVu ct : chiTietDichVus) {
            tong = tong.add(ct.tinhThanhTien());
        }

        return tong;
    }

    // 3. Tổng tiền trước thuế
    public BigDecimal tinhTongTienTruocThue() {
        BigDecimal tong = BigDecimal.ZERO;

        for (ChiTietHoaDon_SanPham ct : chiTietSanPhams) {
            tong = tong.add(ct.tinhGiaTriSauGiam());
        }

        for (ChiTietHoaDon_DichVu ct : chiTietDichVus) {
            tong = tong.add(ct.tinhGiaTriSauGiam());
        }

        return tong;
    }

    // 4. Tổng tiền thuế
    public BigDecimal tinhTongTienThue() {
        BigDecimal tong = BigDecimal.ZERO;

        for (ChiTietHoaDon_SanPham ct : chiTietSanPhams) {
            tong = tong.add(ct.tinhTienThue());
        }

        for (ChiTietHoaDon_DichVu ct : chiTietDichVus) {
            tong = tong.add(ct.tinhTienThue());
        }

        return tong;
    }

    // 5. Tổng tiền thanh toán sau giảm giá và thuế
    public BigDecimal tinhTongTien() {
        return tinhTongTienHang().add(tinhTongTienSpa());
    }

    // 6. Tiền giảm giá
    public BigDecimal tinhTienGiam() {
        BigDecimal tongGiam = BigDecimal.ZERO;

        for (ChiTietHoaDon_SanPham ct : chiTietSanPhams) {
            tongGiam = tongGiam.add(ct.getTienGiamPhanBo());
        }

        for (ChiTietHoaDon_DichVu ct : chiTietDichVus) {
            tongGiam = tongGiam.add(ct.getTienGiamPhanBo());
        }

        return tongGiam;
    }

    // 7. Quy đổi điểm thành tiền
    public BigDecimal quyDoiDiem(int diem) {
        if (diem < 0) {
            throw new IllegalArgumentException("Điểm không hợp lệ");
        }

        // Tỷ lệ minh họa: 1 điểm = 1.000đ
        // Cần thay bằng quy định của TiTiPet.
        return BigDecimal.valueOf(diem)
                .multiply(BigDecimal.valueOf(1000));
    }

    // 8. Tiền phải thu
    public BigDecimal tinhTienPhaiThu() {
        return tinhTongTien();
    }

    // 9. Điểm được tích
    public int tinhDiemDuocTich() {
        // Chưa có quy tắc tích điểm được chốt ở đây.
        return 0;
    }

    // 10. Xác nhận thanh toán
    public void xacNhanThanhToan() {
        this.trangThai = TrangThaiHoaDon.DA_THANH_TOAN;
    }
}
