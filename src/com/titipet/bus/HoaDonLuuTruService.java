package com.titipet.bus;

import com.titipet.dao.HoaDonDAO.HoaDonChiTiet;
import com.titipet.dao.HoaDonDAO.DongChiTiet;
import com.titipet.dao.HoaDonLuuTruDAO;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Snapshot hóa đơn: GZIP rồi AES-256-GCM, lưu BLOB SQL Server và xuất .tpet. */
public final class HoaDonLuuTruService {
    private static final byte[] MAGIC = {'T', 'P', 'E', 'T', 1};
    private static final SecureRandom RANDOM = new SecureRandom();
    private final HoaDonLuuTruDAO dao = new HoaDonLuuTruDAO();

    public byte[] maHoa(HoaDonChiTiet h) throws IOException {
        Properties p = new Properties();
        dat(p, "maHoaDon", h.maHoaDon());
        dat(p, "ngayLap", h.ngayLap());
        dat(p, "maKH", h.maKH());
        dat(p, "tenKhach", h.tenKhach());
        dat(p, "soDienThoai", h.soDienThoai());
        dat(p, "maNV", h.maNV());
        dat(p, "tenNhanVien", h.tenNhanVien());
        dat(p, "maCa", h.maCa());
        dat(p, "nguonDon", h.nguonDon());
        dat(p, "maDonKenh", h.maDonKenh());
        dat(p, "phuongThuc", h.phuongThuc());
        dat(p, "trangThai", h.trangThai());
        dat(p, "tenKhuyenMai", h.tenKhuyenMai());
        dat(p, "tongTienHang", h.tongTienHang());
        dat(p, "tongTienSpa", h.tongTienSpa());
        dat(p, "tienGiamKM", h.tienGiamKM());
        dat(p, "tienGiamHangTV", h.tienGiamHangTV());
        dat(p, "soDiemSuDung", h.soDiemSuDung());
        dat(p, "tienGiamDiem", h.tienGiamDiem());
        dat(p, "soDiemDuocTich", h.soDiemDuocTich());
        dat(p, "tongTienTruocThue", h.tongTienTruocThue());
        dat(p, "tongTienThue", h.tongTienThue());
        dat(p, "tienPhaiThu", h.tienPhaiThu());
        p.setProperty("dong.count", Integer.toString(h.cacDong().size()));
        for (int i = 0; i < h.cacDong().size(); i++) {
            DongChiTiet d = h.cacDong().get(i);
            String k = "dong." + i + ".";
            dat(p, k + "loai", d.loai()); dat(p, k + "ma", d.ma());
            dat(p, k + "ten", d.ten()); dat(p, k + "moTa", d.moTa());
            dat(p, k + "soLuong", d.soLuong()); dat(p, k + "donGia", d.donGia());
            dat(p, k + "thanhTien", d.thanhTien()); dat(p, k + "giamGia", d.giamGia());
            dat(p, k + "tienTruocThue", d.tienTruocThue());
            dat(p, k + "tienThue", d.tienThue()); dat(p, k + "tienSauThue", d.tienSauThue());
        }
        ByteArrayOutputStream zipped = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(zipped)) {
            p.store(gzip, "TiTi Pet invoice snapshot version 1");
        }
        byte[] iv = new byte[12];
        RANDOM.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            cipher.updateAAD(MAGIC);
            byte[] encrypted = cipher.doFinal(zipped.toByteArray());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            result.write(MAGIC); result.write(iv); result.write(encrypted);
            return result.toByteArray();
        } catch (GeneralSecurityException ex) {
            throw new IOException("Không thể mã hóa hóa đơn.", ex);
        }
    }

    /** Kiểm tra tag xác thực và giải nén được; sai khóa hoặc tệp bị sửa sẽ thất bại. */
    public Properties giaiMa(byte[] raw) throws IOException {
        if (raw == null || raw.length < MAGIC.length + 12 + 16)
            throw new IOException("Tệp hóa đơn không hợp lệ.");
        for (int i = 0; i < MAGIC.length; i++) {
            if (raw[i] != MAGIC[i]) throw new IOException("Không đúng định dạng TiTi Pet.");
        }
        byte[] iv = java.util.Arrays.copyOfRange(raw, MAGIC.length, MAGIC.length + 12);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            cipher.updateAAD(MAGIC);
            byte[] zipped = cipher.doFinal(raw, MAGIC.length + iv.length,
                    raw.length - MAGIC.length - iv.length);
            Properties data = new Properties();
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(zipped))) {
                data.load(gzip);
            }
            if (data.getProperty("maHoaDon", "").isBlank())
                throw new IOException("Thiếu mã hóa đơn trong bản lưu.");
            return data;
        } catch (GeneralSecurityException ex) {
            throw new IOException("Không giải mã được hóa đơn: sai khóa hoặc tệp bị thay đổi.", ex);
        }
    }

    public void xuat(String maHoaDon, Path tep) throws SQLException, IOException {
        byte[] raw = dao.doc(maHoaDon);
        if (raw == null) {
            // Hóa đơn cũ: mã hóa bản chụp khi xuất lần đầu, không chỉnh sửa hóa đơn gốc.
            var banGoc = new com.titipet.dao.HoaDonDAO().chiTiet(maHoaDon)
                    .orElseThrow(() -> new SQLException("Không tìm thấy hóa đơn: " + maHoaDon));
            dao.luuNeuChuaCo(maHoaDon, maHoa(banGoc));
            raw = dao.doc(maHoaDon);
        }
        Properties verified = giaiMa(raw);
        if (!maHoaDon.equals(verified.getProperty("maHoaDon")))
            throw new IOException("Mã hóa đơn không khớp dữ liệu lưu.");
        Files.write(tep, raw);
    }

    private static void dat(Properties p, String key, Object value) {
        p.setProperty(key, value == null ? "" : value.toString());
    }

    private static SecretKeySpec key() throws IOException {
        String keyText = System.getenv("TITIPET_INVOICE_KEY");
        if (keyText == null || keyText.isBlank()) {
            Path path = Path.of("config", "invoice.key");
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                byte[] generated = new byte[32];
                RANDOM.nextBytes(generated);
                try {
                    Files.writeString(path, Base64.getEncoder().encodeToString(generated),
                            StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                    try {
                        Files.setPosixFilePermissions(path, java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
                    } catch (UnsupportedOperationException ignored) { /* NTFS/Windows */ }
                } catch (FileAlreadyExistsException ignored) { /* tạo bởi tiến trình khác */ }
            }
            keyText = Files.readString(path).strip();
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(keyText.strip());
            if (bytes.length != 32) throw new IllegalArgumentException("Khóa phải dài 256-bit.");
            return new SecretKeySpec(bytes, "AES");
        } catch (IllegalArgumentException ex) {
            throw new IOException("TITIPET_INVOICE_KEY/config/invoice.key không hợp lệ.", ex);
        }
    }
}
