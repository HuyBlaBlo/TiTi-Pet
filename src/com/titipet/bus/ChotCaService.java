package com.titipet.bus;

import com.titipet.dao.CaLamViecDAO;
import com.titipet.dao.PhieuKetCaDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Kiểm tra nghiệp vụ và điều phối DAO, không dùng Swing hay SQL. */
public class ChotCaService {
    private final CaLamViecDAO caDao = new CaLamViecDAO();
    private final PhieuKetCaDAO phieuDao = new PhieuKetCaDAO();

    public Optional<CaLamViecDAO.TongHopCa> layCaHienTai(String maNV) {
        kiemTraMa(maNV, "Chưa có nhân viên đăng nhập.");
        try { return caDao.timCaHienTai(maNV); }
        catch (SQLException e) { throw new IllegalStateException("Không tải được ca: " + e.getMessage(), e); }
    }

    public String guiPhieu(String maNV, BigDecimal tienMatThucTe) {
        kiemTraMa(maNV, "Chưa có nhân viên đăng nhập.");
        if (tienMatThucTe == null || tienMatThucTe.signum() < 0)
            throw new IllegalArgumentException("Tiền mặt thực tế phải >= 0.");
        try { return phieuDao.guiPhieu(maNV, tienMatThucTe); }
        catch (SQLException e) { throw new IllegalStateException(e.getMessage(), e); }
    }

    public List<PhieuKetCaDAO.PhieuChoDuyet> layPhieuChoDuyet() {
        try { return phieuDao.layPhieuChoDuyet(); }
        catch (SQLException e) { throw new IllegalStateException("Không tải được phiếu: " + e.getMessage(), e); }
    }

    public void duyet(String maPhieu, String maQuanLy, boolean dongY, String lyDo) {
        kiemTraMa(maPhieu, "Chưa chọn phiếu.");
        kiemTraMa(maQuanLy, "Chưa có mã quản lý đăng nhập.");
        if (!dongY && (lyDo == null || lyDo.isBlank()))
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối.");
        try { phieuDao.xuLyDuyet(maPhieu, maQuanLy, dongY, lyDo); }
        catch (SQLException e) { throw new IllegalStateException(e.getMessage(), e); }
    }

    private static void kiemTraMa(String ma, String loi) {
        if (ma == null || ma.isBlank()) throw new IllegalArgumentException(loi);
    }
}
