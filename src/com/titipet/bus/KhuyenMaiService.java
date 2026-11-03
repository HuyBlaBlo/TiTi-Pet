package com.titipet.bus;

import com.titipet.dao.KhuyenMaiDAO;
import com.titipet.entity.KhuyenMai;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class KhuyenMaiService {

    private final KhuyenMaiDAO khuyenMaiDAO;

    public KhuyenMaiService() {
        this.khuyenMaiDAO = new KhuyenMaiDAO();
    }

    public KhuyenMaiService(KhuyenMaiDAO khuyenMaiDAO) {
        this.khuyenMaiDAO = khuyenMaiDAO;
    }

    public List<KhuyenMai> layDanhSachKhuyenMai() throws SQLException {
        return khuyenMaiDAO.layTatCaKhuyenMai();
    }

    public String taoMaKhuyenMaiMoi() throws SQLException {
        return khuyenMaiDAO.sinhMaKhuyenMaiMoi();
    }

    public boolean themKhuyenMai(KhuyenMai km) throws SQLException {
        kiemTraHopLe(km);
        if (km.getMaKhuyenMai() == null || km.getMaKhuyenMai().isBlank()) {
            km.setMaKhuyenMai(khuyenMaiDAO.sinhMaKhuyenMaiMoi());
        }
        return khuyenMaiDAO.themKhuyenMai(km);
    }

    public boolean capNhatKhuyenMai(KhuyenMai km) throws SQLException {
        if (km.getMaKhuyenMai() == null || km.getMaKhuyenMai().isBlank()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống!");
        }
        kiemTraHopLe(km);
        return khuyenMaiDAO.capNhatKhuyenMai(km);
    }

    public boolean batTatTrangThai(String maKM, boolean trangThaiMoi) throws SQLException {
        if (maKM == null || maKM.isBlank()) {
            throw new IllegalArgumentException("Mã khuyến mãi không hợp lệ!");
        }
        return khuyenMaiDAO.chuyenTrangThai(maKM, trangThaiMoi);
    }

    private void kiemTraHopLe(KhuyenMai km) {
        if (km == null) {
            throw new IllegalArgumentException("Dữ liệu khuyến mãi không được rỗng!");
        }
        if (km.getTenChuongTrinh() == null || km.getTenChuongTrinh().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên chương trình khuyến mãi không được để trống!");
        }
        if (km.getGiamTram() == null || km.getGiamTram().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Tỷ lệ giảm phần trăm phải lớn hơn 0%!");
        }
        if (km.getGiamTram().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Tỷ lệ giảm phần trăm không được vượt quá 100%!");
        }
        if (km.getNgayBatDau() != null && km.getNgayKetThuc() != null) {
            if (km.getNgayKetThuc().isBefore(km.getNgayBatDau())) {
                throw new IllegalArgumentException("Ngày kết thúc phải diễn ra sau hoặc bằng ngày bắt đầu!");
            }
        }
    }
    
}