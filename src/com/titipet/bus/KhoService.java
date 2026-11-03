package com.titipet.bus;

import com.titipet.dao.KhoHangDAO;
import com.titipet.entity.SanPham;
import com.titipet.entity.ThucAn;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class KhoService {
    private final KhoHangDAO khoHangDAO;

    public KhoService() {
        this.khoHangDAO = new KhoHangDAO();
    }

    public List<SanPham> layDanhSachSanPhamKho(String keyword) throws SQLException {
        return khoHangDAO.layDanhSachSanPhamKho(keyword);
    }

    public String xacDinhTrangThai(SanPham sp) {
        if (sp instanceof ThucAn ta) {
            if (ta.daHetHan()) {
                return "Hết hạn";
            }
            if (ta.kiemTraCanDate(30)) {
                return "Sắp hết hạn";
            }
        }

        if (sp.getSoLuongTonKho() <= sp.getNguongCanhBaoTon()) {
            return "Sắp hết hàng";
        }

        return "Bình thường";
    }

    public void xuLyNhapKho(String maSP, int soLuongNhap, BigDecimal giaNhapMoi, LocalDate hsdMoi, String ghiChu) throws Exception {
        if (maSP == null || maSP.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần nhập!");
        }
        if (soLuongNhap <= 0) {
            throw new IllegalArgumentException("Số lượng nhập thêm phải lớn hơn 0!");
        }
        if (giaNhapMoi != null && giaNhapMoi.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá nhập không được nhỏ hơn 0!");
        }
        khoHangDAO.nhapKhoSanPham(maSP, soLuongNhap, giaNhapMoi, hsdMoi, ghiChu);
    }

    public List<KhoHangDAO.LichSuNhapItem> layDanhSachLichSuNhapKho() throws SQLException {
        return khoHangDAO.layDanhSachLichSuNhapKho();
    }

}