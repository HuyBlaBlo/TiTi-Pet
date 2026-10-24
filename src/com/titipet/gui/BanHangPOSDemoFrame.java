package com.titipet.gui;

import com.titipet.bus.BanHangKhachHangService;
import com.titipet.bus.BanHangService;
import com.titipet.dao.BanHangSanPhamDAO;
import com.titipet.dao.BanHangKhachHangDAO;
import com.titipet.entity.KhachHangBanHang;
import com.titipet.entity.SanPhamBanHang;
import com.titipet.gui.panel.BanHangPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Frame độc lập tái hiện sidebar + topbar trong POS.png.
 * Khi ghép hệ thống của nhóm, chỉ nhúng BanHangPanel vào MainFrame có sẵn.
 */
public class BanHangPOSDemoFrame extends JFrame {
    private final BanHangPanel pos;
    private JLabel brandLogo;

    public BanHangPOSDemoFrame(boolean demo) {
        super("TiTi Pet Management System — Bán hàng tại quầy POS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 720));
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PosUI.BG);
        root.add(createSidebar(), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(PosUI.BG);
        main.add(createTopbar(), BorderLayout.NORTH);
        if (demo) {
            BanHangSanPhamDAO fake = new BanHangSanPhamDAO() {
                @Override public List<SanPhamBanHang> timSanPham(String key) throws SQLException {
                    String search = key == null ? "" : key.strip().toLowerCase();
                    return previewProducts().stream()
                        .filter(p -> p.tenSanPham().toLowerCase().contains(search) ||
                                     p.maSanPham().equalsIgnoreCase(search) ||
                                     (p.maVach() != null && p.maVach().equals(search)))
                        .toList();
                }
            };
            KhachHangBanHang sampleCustomer = new KhachHangBanHang("KH001", "Hồng Nhiên", "0332353566", "HANG_1", 12);
            BanHangKhachHangDAO fakeCustomers = new BanHangKhachHangDAO() {
                @Override public List<KhachHangBanHang> timKhachHang(String key) {
                    if (key == null || key.isBlank() || sampleCustomer.hoTen().toLowerCase().contains(key.toLowerCase())
                        || sampleCustomer.soDienThoai().contains(key)) return List.of(sampleCustomer);
                    return List.of();
                }
            };
            pos = new BanHangPanel(new BanHangService(fake), new BanHangKhachHangService(fakeCustomers), true);
            pos.showPreviewCustomer(sampleCustomer);
        } else {
            pos = new BanHangPanel();
        }
        main.add(pos, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        updateAppLogo();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension(Math.min(1850, Math.max(1050, screen.width - 50)),
                                       Math.min(1134, Math.max(700, screen.height - 60))));
        pack();
        setLocationRelativeTo(null);
    }

    private JComponent createTopbar() {
        JPanel top = new JPanel(new BorderLayout(20, 0));
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, PosUI.BORDER),
            BorderFactory.createEmptyBorder(15, 27, 15, 29)));
        top.setPreferredSize(new Dimension(100, 80));
        JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        breadcrumb.setOpaque(false);
        breadcrumb.add(PosUI.text("Quản lý cửa hàng", 12, false, PosUI.MUTED));
        breadcrumb.add(PosUI.text("›", 13, true, PosUI.MUTED));
        breadcrumb.add(PosUI.text("Bán hàng tại quầy — POS", 12, true, PosUI.NAVY));
        top.add(breadcrumb, BorderLayout.WEST);
        JTextField global = PosUI.field("Tìm khách hàng, thú cưng, hóa đơn...");
        global.setBackground(new Color(245, 248, 253));
        global.setPreferredSize(new Dimension(360, 41));
        global.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Tìm kiếm toàn hệ thống sẽ hoạt động khi MainFrame của nhóm được tích hợp.", "TiTi Pet", JOptionPane.INFORMATION_MESSAGE));
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.setOpaque(false);
        center.add(global);
        top.add(center, BorderLayout.CENTER);
        JPanel user = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        user.setOpaque(false);
        PosUI.StyledButton bell = PosUI.button("", PosUI.NAVY, new Color(245, 248, 253), null);
        bell.setIcon(PosIcons.of("bell", 17, PosUI.NAVY));
        bell.setToolTipText("Thông báo");
        bell.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chưa có thông báo mới."));
        user.add(bell);
        PosUI.StyledButton avatar = PosUI.button("", PosUI.NAVY, new Color(235, 243, 254), null);
        avatar.setIcon(PosIcons.of("users", 17, PosUI.NAVY));
        avatar.setToolTipText("Tài khoản đang đăng nhập");
        user.add(avatar);
        JPanel userText = PosUI.vertical(0);
        userText.add(PosUI.text("Quản lý TiTi Pet", 11, true, PosUI.NAVY));
        userText.add(PosUI.text("Quản lý   ⌄", 10, false, PosUI.MUTED));
        user.add(userText);
        top.add(user, BorderLayout.EAST);
        return top;
    }

    private JComponent createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(275, 100));
        sidebar.setOpaque(false);
        GradientSide gradient = new GradientSide();
        gradient.setLayout(new BorderLayout());
        sidebar.add(gradient);

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255,255,255,23)),
            BorderFactory.createEmptyBorder(26, 17, 23, 10)));
        brandLogo = PosUI.text("", 24, true, PosUI.NAVY);
        brandLogo.setHorizontalAlignment(SwingConstants.CENTER);
        brandLogo.setPreferredSize(new Dimension(48, 48));
        brandLogo.setOpaque(true);
        brandLogo.setBackground(Color.WHITE);
        brandLogo.setToolTipText("Nhấn để chọn logo ứng dụng (PNG/JPG)");
        brandLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        brandLogo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { chooseLogo(); }
        });
        brand.add(brandLogo);
        JPanel name = PosUI.vertical(0);
        name.add(PosUI.text("TiTiPet", 21, true, Color.WHITE));
        name.add(PosUI.text("M A N A G E M E N T   S Y S T E M", 8, true, new Color(177, 206, 246)));
        brand.add(name);
        gradient.add(brand, BorderLayout.NORTH);

        JPanel nav = PosUI.vertical(0);
        nav.setBorder(BorderFactory.createEmptyBorder(17, 13, 10, 13));
        section(nav, "TỔNG QUAN");
        item(nav, "home", "Trang chủ", false);
        item(nav, "clock", "Mở / chốt ca", false);
        section(nav, "VẬN HÀNH");
        item(nav, "calendar", "Đặt lịch hẹn Spa", false);
        item(nav, "paw", "Tiếp nhận thú cưng", false);
        item(nav, "spa", "Điều phối Spa", false);
        item(nav, "cart", "Bán hàng POS", true);
        item(nav, "exchange", "Đổi trả hàng", false);
        item(nav, "receipt", "Hóa đơn / giao dịch", false);
        section(nav, "KHÁCH HÀNG & KHO");
        item(nav, "users", "Khách hàng & VIP", false);
        item(nav, "heart", "Hồ sơ thú cưng", false);
        item(nav, "box", "Kho & nhập hàng", false);
        item(nav, "tag", "Dịch vụ / sản phẩm", false);
        item(nav, "gift", "Khuyến mãi", false);
        section(nav, "QUẢN TRỊ & BÀN GIAO");
        item(nav, "chart", "Báo cáo thống kê", false);
        JScrollPane scroll = new JScrollPane(nav);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        gradient.add(scroll, BorderLayout.CENTER);

        JPanel bottom = PosUI.vertical(0);
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255,255,255,23)),
            BorderFactory.createEmptyBorder(14, 16, 18, 10)));
        bottom.add(PosUI.text("Quản lý TiTi Pet", 12, true, Color.WHITE));
        bottom.add(PosUI.space(5));
        bottom.add(PosUI.text("TiTi Pet Management             v2.6 UI", 10, false, new Color(171, 203, 244)));
        bottom.add(PosUI.space(13));
        PosUI.StyledButton logout = PosUI.button("Đăng xuất / Đổi vai trò", new Color(205,226,252), new Color(24, 62, 118), null);
        logout.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Đăng xuất / đổi vai trò sẽ hoạt động khi tích hợp MainFrame.", "TiTi Pet", JOptionPane.INFORMATION_MESSAGE));
        bottom.add(logout);
        gradient.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private void updateAppLogo() {
        ImageIcon mark = PosUI.logoIcon(43, 43);
        if (brandLogo != null) {
            brandLogo.setIcon(mark != null ? mark : PosIcons.of("paw", 29, PosUI.NAVY));
            brandLogo.setText("");
        }
        if (mark != null) setIconImage(mark.getImage());
    }

    private void chooseLogo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn logo TiTi Pet (PNG, JPG)");
        chooser.setFileFilter(new FileNameExtensionFilter("Ảnh PNG / JPG", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            File selected = chooser.getSelectedFile();
            java.awt.image.BufferedImage image = ImageIO.read(selected);
            if (image == null) throw new IllegalArgumentException("Không phải ảnh PNG/JPG hợp lệ.");
            Path folder = Path.of("assets", "branding");
            Files.createDirectories(folder);
            // Save as PNG even if user selected JPG; ImageIO handles conversion.
            Path target = folder.resolve("logo.png");
            if (!ImageIO.write(image, "png", target.toFile())) throw new java.io.IOException("Không ghi được PNG.");
            updateAppLogo();
            JOptionPane.showMessageDialog(this, "Đã đặt logo: " + target.toAbsolutePath(), "TiTi Pet", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không gắn được logo: " + ex.getMessage(), "Lỗi ảnh", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void section(JPanel nav, String value) {
        nav.add(PosUI.space(13));
        JLabel title = PosUI.text("   " + value, 10, true, new Color(155, 189, 235));
        nav.add(title);
        nav.add(PosUI.space(8));
    }

    private void item(JPanel nav, String ico, String title, boolean selected) {
        JPanel item = new JPanel(new BorderLayout(12, 0));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(9, 11, 9, 10));
        if (selected) {
            item.setOpaque(true);
            item.setBackground(new Color(62, 112, 179));
            item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(130, 203, 255)),
                BorderFactory.createEmptyBorder(9, 9, 9, 10)));
        }
        JLabel symbol = PosUI.text("", 14, true, selected ? Color.WHITE : new Color(176, 205, 243));
        symbol.setIcon(PosIcons.of(ico, 17, selected ? Color.WHITE : new Color(176, 205, 243)));
        symbol.setPreferredSize(new Dimension(19, 19));
        item.add(symbol, BorderLayout.WEST);
        item.add(PosUI.text(title, 12, true, selected ? Color.WHITE : new Color(193, 218, 249)), BorderLayout.CENTER);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (!selected) item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(BanHangPOSDemoFrame.this,
                    "Mục " + title + " do MainFrame/module tương ứng của nhóm quản lý.",
                    "TiTi Pet", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        nav.add(item);
        nav.add(PosUI.space(2));
    }

    private static final class GradientSide extends JPanel {
        GradientSide() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setPaint(new GradientPaint(0,0,new Color(35, 87, 159), 0,getHeight(),new Color(20, 57, 111)));
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.dispose(); super.paintComponent(g);
        }
    }

    private static List<SanPhamBanHang> previewProducts() {
        // Dữ liệu MẪU chỉ dùng với --demo; không ghi vào SQL Server.
        return List.of(
            sample("SP01", "Snappy Tom pate Trái Cây 70g", "THUC_AN", 20000, 26, "2026-11-21"),
            sample("SP02", "Miuna Mix Topping 1kg", "THUC_AN", 122000, 8, "2026-10-26"),
            sample("SP03", "Dây dắt thú cưng cao cấp", "PHU_KIEN", 95000, 13, null),
            sample("SP04", "Vòng cổ điệu chỉnh", "PHU_KIEN", 80000, 3, null),
            sample("SP05", "Đồ chơi bóng cao su", "PHU_KIEN", 65000, 17, null),
            sample("SP06", "Tã bỉm thú cưng", "PHU_KIEN", 70000, 9, "2027-08-06"),
            sample("SP07", "Áo thú cưng pastel", "PHU_KIEN", 130000, 7, null)
        );
    }
    private static SanPhamBanHang sample(String code, String name, String type, int price, int stock, String date) {
        return new SanPhamBanHang(code, name, "893" + code, BigDecimal.valueOf(price), stock,
                type, date == null ? null : LocalDate.parse(date));
    }

    public static void main(String[] args) {
        boolean demo = java.util.Arrays.asList(args).contains("--demo");
        String snapshot = null;
        for (String a : args) if (a.startsWith("--snapshot=")) snapshot = a.substring("--snapshot=".length());
        final String snapshotFile = snapshot;
        try { UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf"); }
        catch (Exception ex) { /* Chạy với Look & Feel mặc định nếu FlatLaf không có. */ }
        SwingUtilities.invokeLater(() -> {
            BanHangPOSDemoFrame frame = new BanHangPOSDemoFrame(demo);
            if (snapshotFile != null) frame.setLocation(0, 0);
            frame.setVisible(true);
            if (snapshotFile != null) {
                Timer timer = new Timer(1700, e -> {
                    try {
                        Point p = frame.getLocationOnScreen();
                        BufferedImage image = new Robot().createScreenCapture(
                            new Rectangle(p.x, p.y, frame.getWidth(), frame.getHeight()));
                        ImageIO.write(image, "png", new File(snapshotFile));
                        System.out.println("Da luu anh demo: " + snapshotFile);
                    } catch (Exception ex) { ex.printStackTrace(); }
                    frame.dispose();
                });
                timer.setRepeats(false); timer.start();
            }
        });
    }
}
