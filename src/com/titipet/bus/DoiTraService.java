
package com.titipet.bus;

import com.titipet.dao.DoiTraDAO;

import java.sql.SQLException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * BUS - UC002: Doi/tra hang.
 * Kiem tra dieu kien theo nghiep vu TiTi Pet.
 */
public class DoiTraService {

    private final DoiTraDAO dao = new DoiTraDAO();
    private final com.titipet.dao.DoiTraGiaoDichDAO giaoDichDao = new com.titipet.dao.DoiTraGiaoDichDAO();

    public record KetQua(
            boolean dat,
            String thongBao) {
    }

    public record DuLieu(
            DoiTraDAO.HoaDonGoc hoaDon,
            List<DoiTraDAO.LichSu> lichSu) {
    }

    public DuLieu traCuu(String maHD) {
        if (maHD == null || maHD.isBlank()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập mã hóa đơn."
            );
        }

        try {
            String ma = maHD.strip();

            return new DuLieu(
                    dao.timHoaDon(ma),
                    dao.lichSu(ma)
            );

        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Không tra cứu được hóa đơn: "
                            + ex.getMessage(),
                    ex
            );
        }
    }

    /** Danh sach hang thay the duoc lay tu ton kho thuc te. */
    public List<DoiTraDAO.SanPhamThayThe> sanPhamThayThe(
            String maSanPhamCu) {
        if (maSanPhamCu == null || maSanPhamCu.isBlank()) {
            throw new IllegalArgumentException("Chưa chọn sản phẩm cũ.");
        }
        try {
            return dao.laySanPhamThayThe(maSanPhamCu);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không tải được sản phẩm thay thế: " + e.getMessage(), e);
        }
    }

    /** Chot giao dich: DAO se doi chieu lai tinh hop le, gia va ton kho trong transaction. */
    public com.titipet.dao.DoiTraGiaoDichDAO.KetQuaLuu xacNhanDoiHang(
            String maNV, DoiTraDAO.HoaDonGoc hd,
            DoiTraDAO.DongHang cu, DoiTraDAO.SanPhamThayThe moi,
            boolean tinhTrangDat, boolean khongThuocNhomCam,
            boolean daThuTien, String phuongThuc,
            BigDecimal chenhLechDaBaoGia) {
        if (maNV == null || maNV.isBlank()) {
            throw new IllegalArgumentException("Chưa đăng nhập tài khoản Thu ngân.");
        }
        BaoGiaDoi bg = baoGiaDoi(hd, cu, moi, tinhTrangDat, khongThuocNhomCam);
        if (!bg.hopLe()) throw new IllegalArgumentException(bg.thongBao());
        if (chenhLechDaBaoGia == null ||
                bg.tienChenhLech().compareTo(chenhLechDaBaoGia) != 0) {
            throw new IllegalArgumentException("Vui lòng tính lại báo giá.");
        }
        try {
            return giaoDichDao.luuDoiHang(
                    hd.maHD(), cu.maSP(), moi.maSP(), maNV,
                    chenhLechDaBaoGia, phuongThuc,
                    daThuTien, tinhTrangDat, khongThuocNhomCam);
        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public List<DoiTraDAO.HoaDonTomTat> timTheoSoDienThoai(String sdt) {
        if (sdt == null || !sdt.trim().matches("0[0-9]{8,10}"))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        try { return dao.timTheoSoDienThoai(sdt.trim()); }
        catch (SQLException ex) { throw new IllegalStateException("Lỗi tra cứu: " + ex.getMessage(),ex); }
    }

    public record BaoGiaTra(boolean hopLe, BigDecimal tienHoan, String thongBao) {}

    public BaoGiaTra baoGiaTra(DoiTraDAO.HoaDonGoc hd, DoiTraDAO.DongHang cu,
            boolean tinhTrang, boolean nhomHangDat) {
        KetQua k = kiemTra(hd,cu,tinhTrang,nhomHangDat);
        if(!k.dat()) return new BaoGiaTra(false,BigDecimal.ZERO,k.thongBao());
        return new BaoGiaTra(true,cu.donGia(),"Được hoàn theo giá gốc; DAO sẽ kiểm tra lại tiền thực trả.");
    }

    public com.titipet.dao.DoiTraGiaoDichDAO.KetQuaTra xacNhanTraHang(
            String nv, DoiTraDAO.HoaDonGoc hd, DoiTraDAO.DongHang cu,
            BigDecimal tienDuKien, String phuongThucHoan, boolean daHoan,
            boolean tinhTrang, boolean nhomHangDat) {
        BaoGiaTra bg = baoGiaTra(hd,cu,tinhTrang,nhomHangDat);
        if (!bg.hopLe()) throw new IllegalArgumentException(bg.thongBao());
        if (tienDuKien==null || bg.tienHoan().compareTo(tienDuKien)!=0)
            throw new IllegalArgumentException("Tiền hoàn đã thay đổi, cần báo giá lại.");
        try {
            return giaoDichDao.luuTraHang(hd.maHD(),cu.maSP(),nv,tienDuKien,
                    phuongThucHoan,daHoan,tinhTrang,nhomHangDat);
        } catch(SQLException ex) { throw new IllegalStateException(ex.getMessage(),ex); }
    }

    public record BaoGiaDoi(
            boolean hopLe,
            BigDecimal tienChenhLech,
            String thongBao) { }

    /** Chi tinh gia, KHONG ghi giao dich hoac tru kho. */
    public BaoGiaDoi baoGiaDoi(
            DoiTraDAO.HoaDonGoc hoaDon,
            DoiTraDAO.DongHang sanPhamCu,
            DoiTraDAO.SanPhamThayThe sanPhamMoi,
            boolean tinhTrangDat,
            boolean xacNhanKhongCam) {

        KetQua k = kiemTra(
                hoaDon, sanPhamCu, tinhTrangDat, xacNhanKhongCam);
        if (!k.dat()) {
            return new BaoGiaDoi(false, BigDecimal.ZERO, k.thongBao());
        }
        if (sanPhamMoi == null) {
            return new BaoGiaDoi(false, BigDecimal.ZERO,
                    "Vui lòng chọn sản phẩm thay thế.");
        }
        if (sanPhamCu.maSP().equals(sanPhamMoi.maSP())) {
            return new BaoGiaDoi(false, BigDecimal.ZERO,
                    "Sản phẩm mới phải khác sản phẩm cũ.");
        }
        if (sanPhamMoi.tonKho() < 1) {
            return new BaoGiaDoi(false, BigDecimal.ZERO,
                    "Sản phẩm mới đã hết hàng.");
        }
        BigDecimal chenh = sanPhamMoi.giaBan()
                .subtract(sanPhamCu.donGia());
        if (chenh.signum() < 0) {
            return new BaoGiaDoi(false, BigDecimal.ZERO,
                    "Hàng thay thế phải có giá trị bằng hoặc cao hơn hàng cũ.");
        }
        return new BaoGiaDoi(true, chenh,
                "Báo giá hợp lệ. Chưa thu tiền, chưa ghi phiếu đổi và chưa trừ kho.");
    }

    public KetQua kiemTra(
            DoiTraDAO.HoaDonGoc hd,
            DoiTraDAO.DongHang sp,
            boolean tinhTrangDat,
            boolean khongThuocNhomCam) {

        if (hd == null || sp == null) {
            return new KetQua(
                    false,
                    "Chọn hóa đơn và sản phẩm cần đổi/trả."
            );
        }

        if (!"DA_THANH_TOAN".equals(hd.trangThai())) {
            return new KetQua(
                    false,
                    "Chỉ xử lý hóa đơn đã thanh toán."
            );
        }

        if (sp.soLanDaDoi() >= 1) {
            return new KetQua(
                    false,
                    "Sản phẩm đã được đổi một lần."
            );
        }

        if (sp.soLuong() != 1) {
            return new KetQua(
                    false,
                    "Dòng có nhiều sản phẩm: cần hỗ trợ "
                            + "đổi theo số lượng trong CSDL."
            );
        }

        if ("THUC_AN".equals(sp.loaiSP())) {
            return new KetQua(
                    false,
                    "Thức ăn không thuộc nhóm được đổi/trả."
            );
        }

        if (!"PHU_KIEN".equals(sp.loaiSP())) {
            return new KetQua(
                    false,
                    "Loại sản phẩm chưa được hỗ trợ."
            );
        }

        // Quy dinh lien he sau 12 gio trua
        if (LocalTime.now().isBefore(LocalTime.NOON)) {
            return new KetQua(
                    false,
                    "Chỉ liên hệ đổi hàng sau 12 giờ trưa."
            );
        }

        // Moc 24 gio duoc ghi tren hoa don mau/prototype
        long phut = Duration.between(
                hd.ngayLap(),
                LocalDateTime.now()
        ).toMinutes();

        if (phut < 0 || phut > 24 * 60) {
            return new KetQua(
                    false,
                    "Đã quá mốc 24 giờ ghi trên hóa đơn mẫu."
            );
        }

        // Kiem tra thu cong cac nhom khong duoc doi tra
        if (!khongThuocNhomCam) {
            return new KetQua(
                    false,
                    "Cần xác nhận không thuộc tã/bỉm, "
                            + "vệ sinh pet, thuốc hoặc đồ hỗ trợ chuồng."
            );
        }

        // Kiem tra tinh trang san pham thuc te
        if (!tinhTrangDat) {
            return new KetQua(
                    false,
                    "Cần kiểm tra hàng sạch, chưa sử dụng, "
                            + "không dính lông/mùi."
            );
        }

        return new KetQua(
                true,
                "Đạt kiểm tra sơ bộ. "
                        + "Chưa thực hiện đổi/trả trong CSDL."
        );
    }
}
