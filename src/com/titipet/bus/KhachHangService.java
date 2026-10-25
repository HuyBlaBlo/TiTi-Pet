package com.titipet.bus;

import com.titipet.dao.KhachHangDAO;
import com.titipet.entity.KhachHang;
import com.titipet.entity.enums.HangTV;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class KhachHangService {
	private final KhachHangDAO khachHangDAO;

    public KhachHangService() {
        this.khachHangDAO = new KhachHangDAO();
    }

    /**
     * Lấy dữ liệu danh sách khách hàng nạp thẳng lên JTable
     */
    public List<Object[]> layDanhSachHienThiBang(String keyword) throws Exception {
        try {
            return khachHangDAO.getDanhSachHienThiBang(keyword);
        } catch (SQLException e) {
            throw new Exception("Lỗi khi tải danh sách khách hàng từ CSDL: " + e.getMessage());
        }
    }
    
    /**
     * Tra cứu khách hàng theo mã hoặc số điện thoại
     */
    public KhachHang timKhachHangTheoMaHoacSDT(String tuKhoa) throws Exception {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            throw new Exception("Vui lòng nhập mã khách hàng hoặc số điện thoại để tra cứu!");
        }
        try {
            return khachHangDAO.findByIdOrPhone(tuKhoa.trim());
        } catch (SQLException e) {
            throw new Exception("Lỗi khi tìm kiếm khách hàng: " + e.getMessage());
        }
    }
    
    /**
     * Thêm mới một khách hàng với kiểm tra ràng buộc đầy đủ
     */
    public void themKhachHang(KhachHang kh) throws Exception {
        validateThongTinKhachHang(kh);

        try {
            // Kiểm tra số điện thoại duy nhất theo CSDL
            KhachHang daTonTai = khachHangDAO.findByIdOrPhone(kh.getSoDienThoai());
            if (daTonTai != null) {
                throw new Exception("Số điện thoại này đã được đăng ký bởi khách hàng: " + daTonTai.getHoTen());
            }

            if (kh.getHangTV() == null) {
                kh.setHangTV(HangTV.THUONG);
            }

            boolean kq = khachHangDAO.insertKhachHang(kh);
            if (!kq) {
                throw new Exception("Thêm khách hàng thất bại! Vui lòng kiểm tra lại kết nối.");
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi CSDL khi lưu khách hàng: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật thông tin khách hàng
     */
    public void capNhatKhachHang(KhachHang kh) throws Exception {
        if (kh.getMaKH() == null || kh.getMaKH().trim().isEmpty()) {
            throw new Exception("Mã khách hàng không được để trống khi thực hiện cập nhật!");
        }
        validateThongTinKhachHang(kh);

        try {
            boolean kq = khachHangDAO.updateKhachHang(kh);
            if (!kq) {
                throw new Exception("Cập nhật thông tin thất bại! Không tìm thấy mã khách hàng.");
            }
        } catch (SQLException e) {
            throw new Exception("Lỗi CSDL khi cập nhật khách hàng: " + e.getMessage());
        }
    }
    
    /**
     * Nghiệp vụ tính điểm: Cứ 50.000đ = 1 điểm tích lũy
     */
    public int tinhDiemTichLuy(BigDecimal tongTienThanhToan) {
        if (tongTienThanhToan == null || tongTienThanhToan.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return tongTienThanhToan.divideToIntegralValue(BigDecimal.valueOf(50000)).intValue();
    }
    
    /**
     * Nghiệp vụ quy đổi: 1 điểm = 1.000đ giảm giá
     */
    public BigDecimal quyDoiDiemThanhTien(int diemSuDung) {
        if (diemSuDung <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(diemSuDung * 1000L);
    }
    
    /**
     * Nghiệp vụ cộng điểm sau khi thanh toán: sử dụng hàm congDiem() sẵn có trong Entity
     */
    public void congDiemSauThanhToan(String maKH, int diemThem) throws Exception {
        KhachHang kh = timKhachHangTheoMaHoacSDT(maKH);
        if (kh == null) {
            throw new Exception("Không tìm thấy khách hàng để tích điểm!");
        }
        kh.congDiem(diemThem);
        try {
            khachHangDAO.updateDiemTichLuy(kh.getMaKH(), kh.getDiemTichLuy());
        } catch (SQLException e) {
            throw new Exception("Lỗi CSDL khi cập nhật điểm: " + e.getMessage());
        }
    }
    
    /**
     * Nghiệp vụ trừ điểm: sử dụng hàm truDiem() sẵn có trong Entity
     */
    public void truDiemTichLuy(String maKH, int diemTru) throws Exception {
        KhachHang kh = timKhachHangTheoMaHoacSDT(maKH);
        if (kh == null) {
            throw new Exception("Không tìm thấy thông tin khách hàng để trừ điểm!");
        }
        
        // Gọi hàm nghiệp vụ truDiem() trong chính Entity KhachHang
        boolean hopLe = kh.truDiem(diemTru);
        if (!hopLe) {
            throw new Exception("Số điểm yêu cầu trừ (" + diemTru + ") không hợp lệ hoặc vượt quá số điểm hiện có (" + kh.getDiemTichLuy() + ")!");
        }

        try {
            khachHangDAO.updateDiemTichLuy(maKH, kh.getDiemTichLuy());
        } catch (SQLException e) {
            throw new Exception("Lỗi khi cập nhật điểm: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra tính hợp lệ dữ liệu
     */
    private void validateThongTinKhachHang(KhachHang kh) throws Exception {
        if (kh.getHoTen() == null || kh.getHoTen().trim().isEmpty()) {
            throw new Exception("Họ và tên khách hàng không được để trống!");
        }
        if (kh.getSoDienThoai() == null || kh.getSoDienThoai().trim().isEmpty()) {
            throw new Exception("Số điện thoại không được để trống!");
        }
        if (!kh.getSoDienThoai().matches("^0[0-9]{9}$")) {
            throw new Exception("Số điện thoại không đúng định dạng (phải gồm 10 chữ số và bắt đầu bằng số 0)!");
        }
    }
    
}

