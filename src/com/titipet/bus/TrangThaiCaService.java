package com.titipet.bus;

import com.titipet.dao.CaLamViecDAO;
import com.titipet.entity.NhanVien;
import com.titipet.entity.enums.VaiTro;

import java.math.BigDecimal;
import java.sql.SQLException;

/** BUS khong biet Swing, chi doc ca mo cua nhan vien/dang quan ly. */
public final class TrangThaiCaService {
    private final CaLamViecDAO dao = new CaLamViecDAO();
    public boolean daMoCa(NhanVien nv) throws SQLException {
        if (nv == null) return false;
        // Trang thai chung cua cua hang cho quan ly va tho Spa.
        // Thu ngan chi xem ca do chinh minh mo.
        boolean xemCaCuaHang = nv.getVaiTro() != VaiTro.THU_NGAN;
        return dao.coCaDangMo(nv.getMaNV(), xemCaCuaHang);
    }
    public String moCa(
        NhanVien nv,
        BigDecimal tienDauCa
) {
    if (nv == null) {
        throw new IllegalArgumentException(
            "Vui lòng đăng nhập."
        );
    }

    try {
        return dao.moCa(
            nv.getMaNV(),
            tienDauCa
        );

    } catch (SQLException e) {
        throw new IllegalStateException(
            "Không thể mở ca: " + e.getMessage(), e
        );
    }
}
}
