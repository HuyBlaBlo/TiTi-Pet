package com.titipet.bus;

import com.titipet.dao.DichVuSpaDAO;
import com.titipet.dao.SanPhamDAO;
import com.titipet.entity.DichVuSpa;
import com.titipet.entity.SanPham;
import com.titipet.entity.ThucAn;
import com.titipet.entity.enums.LoaiSanPham;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SanPhamService {

    private final SanPhamDAO sanPhamDAO;
    private final DichVuSpaDAO dichVuSpaDAO;

    public SanPhamService() {
        this.sanPhamDAO = new SanPhamDAO();
        this.dichVuSpaDAO = new DichVuSpaDAO();
    }
    
    // 1. TRUY VẤN DỮ LIỆU
    public List<SanPham> layDanhSachSanPham() throws SQLException {
        return sanPhamDAO.layDanhSachSanPham();
    }

    public List<SanPham> timKiemSanPham(String keyword) throws SQLException {
        List<SanPham> all = sanPhamDAO.layDanhSachSanPham();
        if (keyword == null || keyword.isBlank()) {
            return all;
        }

        String kw = keyword.trim().toLowerCase();
        List<SanPham> filtered = new ArrayList<>();
        for (SanPham sp : all) {
            boolean matchMa = sp.getMaSanPham() != null && sp.getMaSanPham().toLowerCase().contains(kw);
            boolean matchTen = sp.getTenSanPham() != null && sp.getTenSanPham().toLowerCase().contains(kw);
            boolean matchVach = sp.getMaVach() != null && sp.getMaVach().toLowerCase().contains(kw);

            if (matchMa || matchTen || matchVach) {
                filtered.add(sp);
            }
        }
        return filtered;
    }

    public List<DichVuSpa> layDanhSachDichVuSpa() throws SQLException {
        return dichVuSpaDAO.layDanhSachDichVuSpa();
    }

    public String taoMaSanPhamMoi(LoaiSanPham loai) throws SQLException {
        if (loai == null) {
            loai = LoaiSanPham.PHU_KIEN;
        }
        return sanPhamDAO.taoMaSanPhamMoi(loai);
    }

    // 2. THÊM MỚI SẢN PHẨM
    public void themSanPham(SanPham sp, LocalDate hanSuDung) throws Exception {
        LocalDateTime hsdTime = (hanSuDung != null) ? hanSuDung.atTime(23, 59, 59) : null;
        themSanPham(sp, hsdTime);
    }

    public void themSanPham(SanPham sp, LocalDateTime hanSuDung) throws Exception {
        kiemTraHopLeSanPham(sp);

        if (sp.getLoaiSanPham() == LoaiSanPham.THUC_AN) {
            if (hanSuDung == null) {
                throw new IllegalArgumentException("Vui lòng nhập hạn sử dụng cho mặt hàng Thức ăn!");
            }
            if (hanSuDung.toLocalDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Hạn sử dụng không thể là ngày trong quá khứ!");
            }
        }

        if (sp.getMaSanPham() == null || sp.getMaSanPham().isBlank()) {
            sp.setMaSanPham(sanPhamDAO.taoMaSanPhamMoi(sp.getLoaiSanPham()));
        }

        if (sp.getGiaNhap() == null || sp.getGiaNhap().compareTo(BigDecimal.ZERO) <= 0) {
            sp.setGiaNhap(sp.getGiaBan().multiply(new BigDecimal("0.70")));
        }

        if (sp.getNguongCanhBaoTon() <= 0) {
            sp.setNguongCanhBaoTon(3);
        }

        sanPhamDAO.themSanPham(sp, hanSuDung);
    }

    // 3. CẬP NHẬT SẢN PHẨM
    public void capNhatSanPham(SanPham sp, LocalDate hanSuDung) throws Exception {
        LocalDateTime hsdTime = (hanSuDung != null) ? hanSuDung.atTime(23, 59, 59) : null;
        capNhatSanPham(sp, hsdTime);
    }

    public void capNhatSanPham(SanPham sp, LocalDateTime hanSuDung) throws Exception {
        if (sp == null || sp.getMaSanPham() == null || sp.getMaSanPham().isBlank()) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ!");
        }

        kiemTraHopLeSanPham(sp);

        if (sp instanceof ThucAn && hanSuDung != null) {
            if (hanSuDung.toLocalDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Hạn sử dụng mới không thể là ngày trong quá khứ!");
            }
        }

        sanPhamDAO.capNhatSanPham(sp, hanSuDung);
    }
    
    // 4. NGỪNG KINH DOANH 
    public boolean ngungKinhDoanhSanPham(String maSP) throws SQLException {
        if (maSP == null || maSP.isBlank()) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ!");
        }
        return sanPhamDAO.ngungKinhDoanhSanPham(maSP);
    }

    // 5. VALIDATION
    private void kiemTraHopLeSanPham(SanPham sp) {
        if (sp == null) {
            throw new IllegalArgumentException("Dữ liệu sản phẩm không được rỗng!");
        }
        if (sp.getTenSanPham() == null || sp.getTenSanPham().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên mặt hàng không được để trống!");
        }
        if (sp.getTenSanPham().trim().length() < 2) {
            throw new IllegalArgumentException("Tên mặt hàng phải có ít nhất 2 ký tự!");
        }
        if (sp.getMaVach() == null || sp.getMaVach().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã vạch không được để trống!");
        }
        if (sp.getGiaBan() == null || sp.getGiaBan().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá bán không được nhỏ hơn 0!");
        }
        if (sp.getSoLuongTonKho() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được nhỏ hơn 0!");
        }
    }

}