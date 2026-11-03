package com.titipet.bus;

import com.titipet.dao.HoaDonDAO;
import com.titipet.dao.HoaDonDAO.HoaDonChiTiet;
import com.titipet.dao.HoaDonDAO.HoaDonTomTat;
import com.titipet.dao.HoaDonDAO.DongChiTiet;
import com.titipet.entity.NhanVien;
import com.titipet.entity.enums.VaiTro;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Nghiệp vụ đọc, tìm kiếm, xuất CSV và in hóa đơn; chỉ đọc dữ liệu đã lưu. */
public final class HoaDonService {
    private static final int GIOI_HAN = 200;
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat TIENTE = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final HoaDonDAO dao;
    private final NhanVien nguoiDangNhap;

    public HoaDonService(NhanVien nguoiDangNhap) {
        this(nguoiDangNhap, new HoaDonDAO());
    }

    public HoaDonService(NhanVien nguoiDangNhap, HoaDonDAO dao) {
        this.nguoiDangNhap = nguoiDangNhap;
        this.dao = java.util.Objects.requireNonNull(dao);
    }

    private void kiemTraQuyen() {
        VaiTro vaiTro = nguoiDangNhap == null ? null : nguoiDangNhap.getVaiTro();
        if (vaiTro != VaiTro.THU_NGAN && vaiTro != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Thu ngân hoặc Quản lý được xem hóa đơn, giao dịch.");
        }
    }

    public List<HoaDonTomTat> timKiem(String tuKhoa, String trangThai) throws SQLException {
        kiemTraQuyen();
        String key = tuKhoa == null ? "" : tuKhoa.strip();
        if (key.length() > 120) throw new IllegalArgumentException("Từ khóa không quá 120 ký tự.");
        String status = trangThai == null ? "" : trangThai;
        if (!List.of("", "CHO_THANH_TOAN", "DA_THANH_TOAN", "DANG_DOI_SOAT", "HUY").contains(status)) {
            throw new IllegalArgumentException("Bộ lọc trạng thái không hợp lệ.");
        }
        return dao.timHoaDon(key, status, GIOI_HAN);
    }

    public HoaDonChiTiet chiTiet(String maHoaDon) throws SQLException {
        kiemTraQuyen();
        if (maHoaDon == null || maHoaDon.isBlank() || maHoaDon.length() > 30) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }
        return dao.chiTiet(maHoaDon).orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy hóa đơn: " + maHoaDon));
    }

    public void xuatCsv(List<HoaDonTomTat> rows, Path dich) throws IOException {
        kiemTraQuyen();
        if (rows == null || dich == null) throw new IllegalArgumentException("Thiếu danh sách hoặc nơi lưu CSV.");
        // Ghi UTF-8 BOM để Excel Windows hiển thị đúng tiếng Việt.
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Mã hóa đơn,Thời gian,Khách hàng,Số điện thoại,Nguồn đơn,Thanh toán,Tổng tiền,Trạng thái\r\n");
        for (HoaDonTomTat h : rows) {
            csv.append(csv(h.maHoaDon())).append(',')
               .append(csv(h.ngayLap() == null ? "" : NGAY.format(h.ngayLap()))).append(',')
               .append(csv(h.tenKhach())).append(',')
               .append(csv(h.soDienThoai())).append(',')
               .append(csv(h.nguonDon())).append(',')
               .append(csv(h.phuongThuc())).append(',')
               .append(csv(h.tienPhaiThu() == null ? "" : h.tienPhaiThu().toPlainString())).append(',')
               .append(csv(h.trangThai())).append("\r\n");
        }
        Files.writeString(dich, csv.toString(), StandardCharsets.UTF_8);
    }

    /** Nội dung hóa đơn chỉ đọc, dùng để xem/in bằng Java Print Service. */
    public String noiDungHoaDon(HoaDonChiTiet h) {
        kiemTraQuyen();
        StringBuilder s = new StringBuilder();
        s.append("                        TITI PET\n");
        s.append("            PET SHOP & SPA · HÓA ĐƠN\n");
        s.append("------------------------------------------------------------\n");
        s.append("Mã hóa đơn: ").append(h.maHoaDon()).append('\n');
        s.append("Ngày lập: ").append(h.ngayLap() == null ? "" : NGAY.format(h.ngayLap())).append('\n');
        s.append("Khách hàng: ").append(h.tenKhach()).append(" (").append(h.soDienThoai()).append(")\n");
        s.append("Nhân viên: ").append(h.tenNhanVien()).append('\n');
        s.append("Kênh: ").append(tenNguon(h.nguonDon())).append(" · Thanh toán: ")
                .append(tenThanhToan(h.phuongThuc())).append('\n');
        s.append("Trạng thái: ").append(tenTrangThai(h.trangThai())).append('\n');
        s.append("------------------------------------------------------------\n");
        for (DongChiTiet d : h.cacDong()) {
            s.append('[').append(d.loai()).append("] ").append(d.ten()).append('\n');
            if (d.moTa() != null && !d.moTa().isBlank()) s.append("  ").append(d.moTa()).append('\n');
            s.append("  ").append(d.soLuong()).append(" x ").append(tien(d.donGia()))
                    .append(" = ").append(tien(d.thanhTien())).append(" đ\n");
            if (d.giamGia() != null && d.giamGia().signum() > 0) {
                s.append("  Giảm theo dòng: ").append(tien(d.giamGia())).append(" đ\n");
            }
        }
        s.append("------------------------------------------------------------\n");
        s.append("Tổng sản phẩm: ").append(tien(h.tongTienHang())).append(" đ\n");
        s.append("Tổng Spa: ").append(tien(h.tongTienSpa())).append(" đ\n");
        if (h.tenKhuyenMai() != null) s.append("Khuyến mãi: ").append(h.tenKhuyenMai()).append('\n');
        s.append("Giảm KM: ").append(tien(h.tienGiamKM())).append(" đ\n");
        s.append("Giảm hạng TV: ").append(tien(h.tienGiamHangTV())).append(" đ\n");
        s.append("Đã sử dụng: ").append(h.soDiemSuDung()).append(" điểm (giảm ")
                .append(tien(h.tienGiamDiem())).append(" đ)\n");
        s.append("Thuế: ").append(tien(h.tongTienThue())).append(" đ\n");
        s.append("PHẢI THU: ").append(tien(h.tienPhaiThu())).append(" đ\n");
        s.append("Điểm được tích: +").append(h.soDiemDuocTich()).append('\n');
        s.append("------------------------------------------------------------\n");
        s.append("Cảm ơn quý khách đã ủng hộ TiTi Pet!\n");
        return s.toString();
    }

    private static String csv(String raw) {
        String value = raw == null ? "" : raw;
        // Ngăn Excel diễn dịch dữ liệu nhập bởi người dùng thành công thức.
        if (!value.isEmpty() && "=+-@\t\r".indexOf(value.charAt(0)) >= 0) value = "'" + value;
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    public static String tien(BigDecimal v) {
        return TIENTE.format(v == null ? BigDecimal.ZERO : v);
    }

    public static String tenNguon(String ma) {
        if (ma == null) return "—";
        return switch (ma) {
            case "TAI_QUAY" -> "Tại quầy";
            case "SHOPEE" -> "Shopee";
            case "TIKTOK" -> "TikTok";
            default -> ma;
        };
    }

    public static String tenThanhToan(String ma) {
        if (ma == null) return "—";
        return switch (ma) {
            case "TIEN_MAT" -> "Tiền mặt";
            case "CHUYEN_KHOAN" -> "Chuyển khoản";
            case "QUA_SAN" -> "Qua sàn";
            default -> ma;
        };
    }

    public static String tenTrangThai(String ma) {
        if (ma == null) return "—";
        return switch (ma) {
            case "DA_THANH_TOAN" -> "Đã thanh toán";
            case "CHO_THANH_TOAN" -> "Chờ thanh toán";
            case "DANG_DOI_SOAT" -> "Đang đối soát";
            case "HUY" -> "Đã hủy";
            default -> ma;
        };
    }
}
