package com.titipet.gui;

import com.titipet.bus.AuthService;
import com.titipet.bus.HoaDonService;
import com.titipet.entity.NhanVien;
import com.titipet.entity.enums.VaiTro;
import com.titipet.gui.component.SidebarPanel;
import com.titipet.gui.component.TopBarPanel;
import com.titipet.gui.panel.BanHangPanel;
import com.titipet.gui.panel.ChotCaPanel;
import com.titipet.gui.panel.DoiTraPanel;
import com.titipet.gui.panel.HoaDonPanel;
import com.titipet.gui.panel.KhachHangPanel;
import com.titipet.gui.panel.KhoPanel;
import com.titipet.gui.panel.KhuyenMaiPanel;
import com.titipet.gui.panel.LichHenPanel;
import com.titipet.gui.panel.LichThoSpaPanel;
import com.titipet.gui.panel.SanPhamPanel;
import com.titipet.gui.panel.TiepNhanSpaPanel;
import com.titipet.gui.panel.ThuCungPanel;
import com.titipet.gui.panel.TrangChuPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainFrame extends JFrame {
    // Mã màn hình dùng chung với các mục trong SidebarPanel.
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
        dangKyCacPanel();
        hienThiManHinh(TRANG_CHU);
        cauHinhCuaSo();
    }

    /** Gắn các panel đã có giao diện vào đúng mã menu của SidebarPanel. */
    private void dangKyCacPanel() {
        NhanVien nhanVien = authService.getNhanVienDangNhap().orElse(null);
        String maNV = nhanVien == null ? null : nhanVien.getMaNV();
        VaiTro vaiTro = nhanVien == null ? null : nhanVien.getVaiTro();

        dangKyTrangChu(maNV, nhanVien == null ? "TiTi Pet" : nhanVien.getHoTen());
        dangKyChotCa(maNV, vaiTro);
        dangKyLichHen();
        dangKyBanHang(maNV);
        dangKyDoiTra(maNV);
        dangKyKhachHang();
        dangKyThuCung(nhanVien);
    }

    /** Trang chủ nhận mã nhân viên để hiện trạng thái ca và callback để mở menu. */
    private void dangKyTrangChu(String maNV, String tenNhanVien) {
        dangKyPanel(TRANG_CHU, new TrangChuPanel(maNV, tenNhanVien, this::hienThiManHinh));
    }

    /** Mở/chốt ca cần mã nhân viên và vai trò để hiện đúng chức năng. */
    private void dangKyChotCa(String maNV, VaiTro vaiTro) {
        dangKyPanel(CHOT_CA, new ChotCaPanel(maNV, vaiTro));
    }

    /** Mục Đặt lịch hẹn Spa. */
    private void dangKyLichHen() {
        dangKyPanel(LICH_HEN, new LichHenPanel());
    }

    /** POS cần mã nhân viên đăng nhập khi tạo giao dịch. */
    private void dangKyBanHang(String maNV) {
        BanHangPanel panel = new BanHangPanel();
        panel.setMaNhanVienDangNhap(maNV);
        dangKyPanel(BAN_HANG, panel);
    }

    /** Đổi trả cần mã nhân viên để xác nhận giao dịch. */
    private void dangKyDoiTra(String maNV) {
        dangKyPanel(DOI_TRA, new DoiTraPanel(maNV));
    }

    /** Khách hàng & VIP dùng AuthService để kiểm tra quyền trên màn hình. */
    private void dangKyKhachHang() {
        dangKyPanel(KHACH_HANG, new KhachHangPanel(authService));
    }

    /** Mục Hồ sơ thú cưng nhận nhân viên đăng nhập để kiểm tra quyền thao tác. */
    private void dangKyThuCung(NhanVien nhanVien) {
        dangKyPanel(THU_CUNG, new ThuCungPanel(nhanVien));
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

    /** Chuyển menu, cập nhật tiêu đề và hiển thị panel tương ứng. */
    public void hienThiManHinh(String maManHinh) {
        kiemTraMaManHinh(maManHinh);
        if (!duocMoPanelMoi(maManHinh)) {
            // Nút menu đã được Swing chọn trước khi gọi callback; chọn lại màn hình cũ.
            sidebar.chonManHinh(manHinhHienTai);
            JOptionPane.showMessageDialog(this, "Màn hình này chưa cho phép tài khoản hiện tại truy cập.");
            return;
        }
        dangKyPanelKhiMoLanDau(maManHinh);
        manHinhHienTai = maManHinh;
        sidebar.chonManHinh(maManHinh);
        topBar.setTieuDe(sidebar.layTieuDe(maManHinh));
        cardLayout.show(contentPanel, panels.containsKey(maManHinh) ? maManHinh : PANEL_TRONG);
        if (HOA_DON.equals(maManHinh)) {
            ((HoaDonPanel) panels.get(HOA_DON)).taiDuLieu();
        }
        if (TRANG_CHU.equals(maManHinh)) {
            ((TrangChuPanel) panels.get(TRANG_CHU)).taiDuLieu();
        }
    }

    /**
     * Chặn trước khi tạo panel, vì constructor của các màn hình này đã đọc CSDL.
     * Điều phối Spa hiện lấy dữ liệu của mọi thợ, nên chỉ mở cho quản lý cho tới
     * khi truy vấn được giới hạn theo nhân viên được phân công.
     */
    private boolean duocMoPanelMoi(String maManHinh) {
        VaiTro vaiTro = authService.getNhanVienDangNhap()
                .map(NhanVien::getVaiTro).orElse(null);
        if (HOA_DON.equals(maManHinh)) {
            return vaiTro == VaiTro.QUAN_LY || vaiTro == VaiTro.THU_NGAN;
        }
        if (DIEU_PHOI_SPA.equals(maManHinh)) {
            return vaiTro == VaiTro.QUAN_LY;
        }
        if (KHO.equals(maManHinh) || DICH_VU_SAN_PHAM.equals(maManHinh)
                || KHUYEN_MAI.equals(maManHinh)) {
            return vaiTro == VaiTro.QUAN_LY || vaiTro == VaiTro.THU_NGAN;
        }
        return true;
    }

    /**
     * Các panel này đọc CSDL ngay trong constructor, nên chỉ tạo khi người dùng mở menu.
     * Truyền AuthService cho các module đã có kiểm tra vai trò riêng; không dùng
     * constructor mặc định của chúng vì constructor đó mặc định là quản lý.
     */
    private void dangKyPanelKhiMoLanDau(String maManHinh) {
        if (panels.containsKey(maManHinh)) {
            return;
        }

        switch (maManHinh) {
            case HOA_DON -> dangKyPanel(HOA_DON, new HoaDonPanel(
                    new HoaDonService(authService.getNhanVienDangNhap().orElse(null))));
            case TIEP_NHAN_SPA -> dangKyPanel(TIEP_NHAN_SPA, new TiepNhanSpaPanel());
            case DIEU_PHOI_SPA -> dangKyPanel(DIEU_PHOI_SPA, new LichThoSpaPanel());
            case KHO -> dangKyPanel(KHO, new KhoPanel(authService));
            case DICH_VU_SAN_PHAM -> dangKyPanel(DICH_VU_SAN_PHAM, new SanPhamPanel(authService));
            case KHUYEN_MAI -> dangKyPanel(KHUYEN_MAI, new KhuyenMaiPanel(authService));
            default -> {
                // Các panel còn lại đã đăng ký lúc khởi tạo hoặc chưa có giao diện.
            }
        }
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
