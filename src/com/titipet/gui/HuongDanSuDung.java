package com.titipet.gui;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Mở hướng dẫn TiTi Pet bằng trình duyệt mặc định.
 * Không truy cập cơ sở dữ liệu, không thay đổi phiên đăng nhập.
 *
 * Trang mặc định là resources/huong-dan/index.html (dùng offline).
 * Có thể đặt biến TITIPET_HELP_URL thành URL https://... khi xuất bản lên web.
 */
public final class HuongDanSuDung {
    private static final String BIEN_URL = "TITIPET_HELP_URL";
    private static final Path TEP_HTML = Path.of("resources", "huong-dan", "index.html");

    private HuongDanSuDung() { }

    public static void moTrang(Component parent) {
        moTrang(parent, null);
    }

    /**
     * @param muc mã mục HTML như "ban-hang", "kho", "chot-ca";
     *            null hoặc rỗng sẽ mở trang tổng quan.
     */
    public static void moTrang(Component parent, String muc) {
        try {
            URI uri = diaChiHuongDan(muc);
            if (!Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                throw new IOException("Máy tính không hỗ trợ tự mở trình duyệt.");
            }
            Desktop.getDesktop().browse(uri);
        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(parent,
                    "Không thể mở trang Hướng dẫn sử dụng.\n" + ex.getMessage()
                    + "\nKiểm tra resources/huong-dan/index.html hoặc trình duyệt mặc định.",
                    "TiTi Pet - Hướng dẫn sử dụng", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static URI diaChiHuongDan(String muc) throws IOException {
        String urlNgoai = System.getenv(BIEN_URL);
        URI base;
        if (urlNgoai != null && !urlNgoai.isBlank()) {
            base = URI.create(urlNgoai.strip());
            String scheme = base.getScheme();
            if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
                throw new IOException(BIEN_URL + " chỉ chấp nhận URL http/https.");
            }
        } else {
            Path path = timTepHuongDan();
            if (path == null) {
                throw new IOException("Không tìm thấy tệp " + TEP_HTML + ".");
            }
            base = path.toUri();
        }
        String anchor = muc == null ? "" : muc.strip();
        // Chỉ chấp nhận ID nội bộ để tránh tạo địa chỉ không hợp lệ.
        if (!anchor.matches("[a-z0-9-]{0,50}")) {
            anchor = "";
        }
        return anchor.isEmpty() ? base : URI.create(base.toString().split("#", 2)[0] + "#" + anchor);
    }

    private static Path timTepHuongDan() {
        // Khi chạy từ terminal hoặc VS Code, user.dir thường là thư mục gốc project.
        Path folder = Path.of("").toAbsolutePath().normalize();
        for (int i = 0; i < 4 && folder != null; i++, folder = folder.getParent()) {
            Path candidate = folder.resolve(TEP_HTML).normalize();
            if (Files.isRegularFile(candidate)) return candidate;
        }
        return null;
    }
}
