package com.titipet.bus;

import com.titipet.dao.ThuCungDAO;
import com.titipet.entity.ThuCung;

import java.sql.SQLException;
import java.util.List;

public class ThuCungService {
	private final ThuCungDAO thuCungDAO;

    public ThuCungService() {
        this.thuCungDAO = new ThuCungDAO();
    }

    public List<ThuCung> layDanhSachCard(String tuKhoa) throws Exception {
        return thuCungDAO.layDanhSachCard(tuKhoa);
    }

    public void themThuCung(ThuCung tc) throws Exception {
        validateThuCung(tc);

        // Tự động cấp mã nếu chưa có
        if (tc.getMaThuCung() == null || tc.getMaThuCung().isBlank()) {
            tc.setMaThuCung(thuCungDAO.phatSinhMaThuCungTuDong());
        }

        boolean thanhCong = thuCungDAO.themThuCung(tc);
        if (!thanhCong) {
            throw new SQLException("Thêm hồ sơ thú cưng thất bại!");
        }
    }

    public void capNhatThuCung(ThuCung tc) throws Exception {
        if (tc == null || tc.getMaThuCung() == null || tc.getMaThuCung().isBlank()) {
            throw new IllegalArgumentException("Không xác định được thú cưng cần cập nhật!");
        }
        validateThuCung(tc);

        boolean ok = thuCungDAO.capNhatThuCung(tc);
        if (!ok) {
            throw new SQLException("Cập nhật hồ sơ thú cưng thất bại!");
        }
    }

    public String sinhMaMoi() throws Exception {
        return thuCungDAO.phatSinhMaThuCungTuDong();
    }

    private void validateThuCung(ThuCung tc) {
        if (tc == null) {
            throw new IllegalArgumentException("Dữ liệu thú cưng không hợp lệ!");
        }
        if (tc.getTenThuCung() == null || tc.getTenThuCung().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên thú cưng!");
        }
        if (tc.getCanNang() <= 0) {
            throw new IllegalArgumentException("Cân nặng phải lớn hơn 0 kg!");
        }
        if (tc.getChuNuoi() == null || tc.getChuNuoi().getMaKH() == null || tc.getChuNuoi().getMaKH().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn chủ nuôi!");
        }
    }
}
