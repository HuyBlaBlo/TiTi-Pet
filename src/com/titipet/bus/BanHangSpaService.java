package com.titipet.bus;

import com.titipet.dao.BanHangSpaDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

/** Kiem tra nghiep vu chon dich vu Spa truoc khi them vao gio POS. */
public class BanHangSpaService {
    private final BanHangSpaDAO dao = new BanHangSpaDAO();

    public List<BanHangSpaDAO.DichVu> layDichVu() throws SQLException {
        return dao.danhSachDichVu();
    }

    public List<BanHangSpaDAO.ThuCung> layThuCung(String maKH) throws SQLException {
        if (maKH == null || maKH.isBlank())
            throw new IllegalArgumentException("Vui long chon khach hang truoc.");
        return dao.danhSachThuCung(maKH);
    }

    public BanHangService.DongSpa chonDichVu(
            BanHangSpaDAO.DichVu dichVu,
            BanHangSpaDAO.ThuCung thuCung,
            BigDecimal giaDaXacNhan) {
        if (dichVu == null || thuCung == null)
            throw new IllegalArgumentException("Hay chon dich vu va thu cung.");
        if (!"CA_HAI".equals(dichVu.loaiApDung())
                && !dichVu.loaiApDung().equals(thuCung.loai()))
            throw new IllegalArgumentException("Dich vu khong ap dung cho loai thu cung nay.");
        if (thuCung.canNang() == null || thuCung.canNang().signum() <= 0)
            throw new IllegalArgumentException("Can nang thu cung chua hop le.");
        if (giaDaXacNhan == null || giaDaXacNhan.signum() < 0
                || giaDaXacNhan.compareTo(new BigDecimal("9999999999999999.99")) > 0)
            throw new IllegalArgumentException("Gia Spa da xac nhan khong hop le.");
        if (dichVu.thueSuat() == null || dichVu.thueSuat().signum() < 0
                || dichVu.thueSuat().compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Thue suat dich vu khong hop le.");
        return new BanHangService.DongSpa(
            dichVu.ma(), dichVu.ten(), thuCung.ma(), thuCung.ten(),
            giaDaXacNhan.setScale(2, RoundingMode.HALF_UP),
            dichVu.thueSuat(), dichVu.giaDaBaoGomThue());
    }
}
