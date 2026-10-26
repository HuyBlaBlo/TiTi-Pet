package com.titipet.gui.component;

import com.titipet.gui.MainFrame;
import com.titipet.util.RoundedPannel;
import com.titipet.util.RoundedPannelLinear;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SidebarPanel extends RoundedPannelLinear {
    private static final Color TEXT = new Color(211, 226, 248);
    private static final Color MUTED = new Color(156, 185, 225);
    private final Map<String, JToggleButton> menuButtons = new LinkedHashMap<>();
    private final Map<String, String> titles = new LinkedHashMap<>();
    private final ButtonGroup menuGroup = new ButtonGroup();
    private final Consumer<String> onMenuSelected;

    public SidebarPanel(String tenNhanVien, String vaiTro,
                        Consumer<String> onMenuSelected, Runnable onLogout) {
        super(new Color(34, 82, 153), new Color(22, 53, 99), 0, Direction.VERTICAL);
        this.onMenuSelected = onMenuSelected;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(260, 0));
        add(taoThuongHieu(), BorderLayout.NORTH);
        add(taoMenu(), BorderLayout.CENTER);
        add(taoChanSidebar(tenNhanVien, vaiTro, onLogout), BorderLayout.SOUTH);
    }

    private JPanel taoThuongHieu() {
        JPanel header = panelTrong(new BorderLayout(12, 0));
        header.setBorder(new EmptyBorder(28, 20, 28, 20));
        RoundedPannel logo = new RoundedPannel(Color.WHITE, 24);
        logo.setLayout(new BorderLayout());
        logo.add(new JLabel(taiIcon("🐾.png", 24, null), SwingConstants.CENTER));
        logo.setPreferredSize(new Dimension(40, 40));
        JPanel text = panelTrong(new GridLayout(2, 1, 0, 2));
        text.add(label("TiTi Pet", 23, Font.BOLD, Color.WHITE));
        text.add(label("MANAGEMENT SYSTEM", 10, Font.PLAIN, MUTED));
        header.add(logo, BorderLayout.WEST);
        header.add(text, BorderLayout.CENTER);
        return header;
    }

    private JScrollPane taoMenu() {
        JPanel menu = panelTrong(null);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(6, 14, 12, 14));
        themNhom(menu, "TỔNG QUAN");
        themMuc(menu, MainFrame.TRANG_CHU, "Trang chủ", "Tổng quan cửa hàng", "▦.png");
        themMuc(menu, MainFrame.CHOT_CA, "Mở / chốt ca", "Mở / chốt ca", "◷.png");
        themNhom(menu, "VẬN HÀNH");
        themMuc(menu, MainFrame.LICH_HEN, "Đặt lịch hẹn Spa", "Đặt lịch hẹn Spa", "▦(1).png");
        themMuc(menu, MainFrame.TIEP_NHAN_SPA, "Tiếp nhận thú cưng", "Tiếp nhận thú cưng", "🐾.png");
        themMuc(menu, MainFrame.DIEU_PHOI_SPA, "Điều phối Spa", "Điều phối Spa", "✂.png");
        themMuc(menu, MainFrame.BAN_HANG, "Bán hàng POS", "Bán hàng POS", "▣.png");
        themMuc(menu, MainFrame.DOI_TRA, "Đổi trả hàng", "Đổi trả hàng", "⇄.png");
        themMuc(menu, MainFrame.HOA_DON, "Hóa đơn / giao dịch", "Hóa đơn / giao dịch", "▤.png");
        themNhom(menu, "KHÁCH HÀNG & KHO");
        themMuc(menu, MainFrame.KHACH_HANG, "Khách hàng & VIP", "Khách hàng & VIP", "♙.png");
        themMuc(menu, MainFrame.THU_CUNG, "Hồ sơ thú cưng", "Hồ sơ thú cưng", "♡.png");
        themMuc(menu, MainFrame.KHO, "Kho & nhập hàng", "Kho & nhập hàng", "▤(1).png");
        themMuc(menu, MainFrame.DICH_VU_SAN_PHAM, "Dịch vụ / sản phẩm", "Dịch vụ / sản phẩm", "◈.png");
        themMuc(menu, MainFrame.KHUYEN_MAI, "Khuyến mãi", "Khuyến mãi", "_.png");
        themNhom(menu, "QUẢN TRỊ & BÀN GIAO");
        themMuc(menu, MainFrame.THONG_KE, "Báo cáo thống kê", "Báo cáo thống kê", "▥.png");

        JScrollPane scroll = new JScrollPane(menu);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        return scroll;
    }

    private void themNhom(JPanel menu, String title) {
        JLabel heading = label(title, 10, Font.BOLD, MUTED);
        heading.setBorder(new EmptyBorder(18, 10, 8, 0));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(heading);
    }

    private void themMuc(JPanel menu, String key, String text, String title, String iconFile) {
        JToggleButton button = new JToggleButton(text, taiIcon(iconFile, 18, TEXT));
        button.setName(key);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(14);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setForeground(TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setPreferredSize(new Dimension(230, 40));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.putClientProperty("FlatLaf.style",
                "arc: 12; borderWidth: 0; focusWidth: 0; margin: 8,12,8,12;"
                        + " background: #00000000; hoverBackground: #315D99;"
                        + " selectedBackground: #3C67A4; selectedForeground: #FFFFFF");
        button.addActionListener(event -> onMenuSelected.accept(key));
        menuButtons.put(key, button);
        titles.put(key, title);
        menuGroup.add(button);
        menu.add(button);
        menu.add(Box.createVerticalStrut(3));
    }

    private JPanel taoChanSidebar(String name, String role, Runnable onLogout) {
        JPanel footer = panelTrong(new BorderLayout(0, 12));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(61, 94, 141)),
                new EmptyBorder(18, 18, 22, 18)));
        JPanel user = panelTrong(new BorderLayout(10, 0));
        user.add(new JLabel(taiIcon("span.side-avatar.png", 34, null)), BorderLayout.WEST);
        JPanel details = panelTrong(new GridLayout(2, 1, 0, 2));
        details.add(label(name, 12, Font.BOLD, Color.WHITE));
        details.add(label(role, 11, Font.PLAIN, MUTED));
        user.add(details, BorderLayout.CENTER);
        footer.add(user, BorderLayout.NORTH);
        footer.add(label("TiTi Pet Management", 10, Font.PLAIN, MUTED), BorderLayout.CENTER);

        JButton logout = new JButton("↪  Đăng xuất");
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        logout.setForeground(TEXT);
        logout.setBorder(BorderFactory.createEmptyBorder());
        logout.setContentAreaFilled(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(event -> onLogout.run());
        footer.add(logout, BorderLayout.SOUTH);
        return footer;
    }

    public void chonManHinh(String key) {
        JToggleButton button = menuButtons.get(key);
        if (button != null) {
            button.setSelected(true);
        }
    }

    public String layTieuDe(String key) {
        return titles.get(key);
    }

    private static JPanel panelTrong(LayoutManager layout) {
        JPanel panel = layout == null ? new JPanel() : new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, style, size));
        label.setForeground(color);
        return label;
    }

    // hàm tải ảnh dùng cho sidebar và top bar
    static ImageIcon taiIcon(String fileName, int size, Color tint) {
        String resourcePath = "icons/sidebar/" + fileName;
        try {
            URL url = SidebarPanel.class.getResource("/" + resourcePath);
            if (url == null) {
                url = SidebarPanel.class.getResource("/resources/" + resourcePath);
            }
            Path path = Path.of("resources", resourcePath);
            BufferedImage image = url != null ? ImageIO.read(url)
                    : Files.isRegularFile(path) ? ImageIO.read(path.toFile()) : null;
            if (image == null) {
                System.err.println("Không tìm thấy icon: " + resourcePath);
                return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
            }
            BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = result.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                double scale = Math.min(size / (double) image.getWidth(), size / (double) image.getHeight());
                int width = (int) Math.round(image.getWidth() * scale);
                int height = (int) Math.round(image.getHeight() * scale);
                g.drawImage(image, (size - width) / 2, (size - height) / 2, width, height, null);
                if (tint != null) {
                    g.setComposite(AlphaComposite.SrcIn);
                    g.setColor(tint);
                    g.fillRect(0, 0, size, size);
                }
            } finally {
                g.dispose();
            }
            return new ImageIcon(result);
        } catch (IOException exception) {
            throw new IllegalStateException("Không tải được icon: " + resourcePath, exception);
        }
    }
}
