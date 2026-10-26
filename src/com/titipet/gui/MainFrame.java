package com.titipet.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.titipet.bus.AuthService;
import com.titipet.entity.enums.VaiTro;
import com.titipet.gui.component.SidebarPanel;
import com.titipet.gui.component.TopBarPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainFrame extends JFrame {
    // Dùng các mã này khi gắn panel của thành viên vào khung chính.
    public static final String TRANG_CHU = "trang-chu";
    public static final String CHOT_CA = "chot-ca";
    public static final String LICH_HEN = "lich-hen";
    public static final String TIEP_NHAN_SPA = "tiep-nhan-spa";
    public static final String DIEU_PHOI_SPA = "dieu-phoi-spa";
    public static final String BAN_HANG = "ban-hang";
    public static final String DOI_TRA = "doi-tra";
    public static final String HOA_DON = "hoa-don";
    public static final String KHACH_HANG = "khach-hang";
    public static final String THU_CUNG = "thu-cung";
    public static final String KHO = "kho";
    public static final String DICH_VU_SAN_PHAM = "dich-vu-san-pham";
    public static final String KHUYEN_MAI = "khuyen-mai";
    public static final String THONG_KE = "thong-ke";

    private static final String PANEL_TRONG = "panel-trong";
    private static final Color CONTENT_BACKGROUND = new Color(245, 248, 254);

    private final AuthService authService;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JPanel> panels = new HashMap<>();
    private final SidebarPanel sidebar;
    private final TopBarPanel topBar;
    private String manHinhHienTai = TRANG_CHU;

    public MainFrame(AuthService authService) {
        super("TiTiPet - Quản lý cửa hàng");
        this.authService = Objects.requireNonNull(authService);
        String tenNhanVien = authService.getNhanVienDangNhap()
                .map(nhanVien -> nhanVien.getHoTen())
                .orElse("TiTi Pet");
        String vaiTro = layTenVaiTro();

        sidebar = new SidebarPanel(tenNhanVien, vaiTro, this::hienThiManHinh, this::dangXuat);
        topBar = new TopBarPanel(tenNhanVien, vaiTro, this::dangXuat);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        taoGiaoDien();
        hienThiManHinh(TRANG_CHU);
        cauHinhCuaSo();
    }

    private void taoGiaoDien() {
        JPanel root = new JPanel(new BorderLayout());
        JPanel workspace = new JPanel(new BorderLayout());
        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(CONTENT_BACKGROUND);
        contentPanel.setBackground(CONTENT_BACKGROUND);
        contentPanel.add(emptyPanel, PANEL_TRONG);

        workspace.add(topBar, BorderLayout.NORTH);
        workspace.add(contentPanel, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);
        root.add(workspace, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void cauHinhCuaSo() {
        GraphicsConfiguration configuration = getGraphicsConfiguration();
        Rectangle screen = configuration.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
        Rectangle workArea = new Rectangle(
                screen.x + insets.left, screen.y + insets.top,
                screen.width - insets.left - insets.right,
                screen.height - insets.top - insets.bottom
        );
        setPreferredSize(workArea.getSize());
        setMinimumSize(new Dimension(Math.min(900, workArea.width), Math.min(600, workArea.height)));
        setMaximizedBounds(workArea);
        pack();
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);
    }

    /**
     * gọi các panel sau
     * frame.dangKyPanel(MainFrame.BAN_HANG,new BangHangPanel())
     */
    public void dangKyPanel(String maManHinh, JPanel panel) {
        kiemTraMaManHinh(maManHinh);
        Objects.requireNonNull(panel, "Panel không được null.");
        JPanel oldPanel = panels.get(maManHinh);
        if (oldPanel == panel) {
            return;
        }
        if (panels.containsValue(panel)) {
            throw new IllegalArgumentException("Mỗi màn hình cần một đối tượng panel riêng.");
        }
        if (oldPanel != null) {
            contentPanel.remove(oldPanel);
        }
        panels.put(maManHinh, panel);
        contentPanel.add(panel, maManHinh);
        if (maManHinh.equals(manHinhHienTai)) {
            cardLayout.show(contentPanel, maManHinh);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /** ddổi menu, tiêu đề và panel
     * màn hình chưa gắn panel sẽ có nền trống */
    public void hienThiManHinh(String maManHinh) {
        kiemTraMaManHinh(maManHinh);
        manHinhHienTai = maManHinh;
        sidebar.chonManHinh(maManHinh);
        topBar.setTieuDe(sidebar.layTieuDe(maManHinh));
        cardLayout.show(contentPanel, panels.containsKey(maManHinh) ? maManHinh : PANEL_TRONG);
    }

    private void kiemTraMaManHinh(String maManHinh) {
        if (sidebar.layTieuDe(maManHinh) == null) {
            throw new IllegalArgumentException("Mã màn hình không hợp lệ: " + maManHinh);
        }
    }

    private String layTenVaiTro() {
        VaiTro vaiTro = authService.getNhanVienDangNhap()
                .map(nhanVien -> nhanVien.getVaiTro()).orElse(null);
        if (vaiTro == null) {
            return "Chưa đăng nhập";
        }
        return switch (vaiTro) {
            case QUAN_LY -> "Quản lý";
            case THU_NGAN -> "Thu ngân";
            case THO_SPA -> "Thợ Spa";
        };
    }

    private void dangXuat() {
        authService.dangXuat();
        new LoginFrame(authService).setVisible(true);
        dispose();
    }

}
