package com.titipet.gui.panel;

import com.titipet.bus.AuthService;
import com.titipet.bus.SanPhamService;
import com.titipet.entity.DichVuSpa;
import com.titipet.entity.SanPham;
import com.titipet.entity.ThucAn;
import com.titipet.entity.enums.LoaiSanPham;
import com.titipet.entity.enums.VaiTro;
import com.titipet.gui.dialog.CapNhatSanPhamDialog;
import com.titipet.gui.dialog.ThemSanPhamDialog;
import com.titipet.gui.dialog.ThemDichVuSpaDialog;
import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SanPhamPanel extends JPanel {
	
    // KHAI BÁO SERVICE VÀ PHÂN QUYỀN
    private final SanPhamService sanPhamService;
    private AuthService authService;
    private VaiTro vaiTroHienTai;

    // CÁC NÚT TÁC VỤ PHÂN QUYỀN
    private JButton btnTaoDichVu;
    private JButton btnThemSanPham;

    // BẢNG VÀ MÔ HÌNH DỮ LIỆU SPA
    private JTable tblDichVuSpa;
    private DefaultTableModel modelSpa;

    // CONTAINER CARD SẢN PHẨM
    private JPanel pnlProductCards;

    // BẢNG MÀU THIẾT KẾ CỐ ĐỊNH
    private static final Color COLOR_BG = new Color(245, 247, 251);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);

    private final DecimalFormat dfTien = new DecimalFormat("#,##0 đ");
    private static final DateTimeFormatter DF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public SanPhamPanel(AuthService authService) {
        this.authService = authService;
        this.sanPhamService = new SanPhamService();

        if (authService != null && authService.getNhanVienDangNhap().isPresent()) {
            this.vaiTroHienTai = authService.getNhanVienDangNhap().get().getVaiTro();
        } else {
            this.vaiTroHienTai = VaiTro.QUAN_LY;
        }

        initComponents();
        taiDuLieuSpa();
        taiDuLieuSanPham();
        apDungPhanQuyen();
    }

    public SanPhamPanel(VaiTro vaiTro) {
        this.vaiTroHienTai = (vaiTro != null) ? vaiTro : VaiTro.QUAN_LY;
        this.sanPhamService = new SanPhamService();

        initComponents();
        taiDuLieuSpa();
        taiDuLieuSanPham();
        apDungPhanQuyen();
    }

    public SanPhamPanel() {
        this(VaiTro.QUAN_LY);
    }

    /**
     * Bố cục các thành phần tổng thể của Panel
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // NẾU LÀ THỢ SPA: KHÓA TRUY CẬP HOÀN TOÀN
        if (vaiTroHienTai == VaiTro.THO_SPA) {
            JPanel lockPanel = new JPanel(new GridBagLayout());
            lockPanel.setBackground(COLOR_BG);
            JLabel lblLock = new JLabel("Bạn không có quyền truy cập module Danh mục & Bảng giá!");
            lblLock.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblLock.setForeground(new Color(220, 38, 38));
            lockPanel.add(lblLock);
            add(lockPanel, BorderLayout.CENTER);
            return;
        }

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(COLOR_BG);
        mainContent.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 1. Header trên cùng
        mainContent.add(createHeaderSection());
        mainContent.add(Box.createVerticalStrut(18));

        // 2. Section Bảng giá Dịch vụ Spa
        mainContent.add(createSpaSection());
        mainContent.add(Box.createVerticalStrut(24));

        // 3. Section Danh mục Sản phẩm
        mainContent.add(createProductSection());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    // 1. PHẦN HEADER SECTION
    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblSub = new JLabel("TI TI PET - WORKSPACE");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSub.setForeground(COLOR_TEXT_MUTED);

        JLabel lblMain = new JLabel("Danh mục & bảng giá");
        lblMain.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblMain.setForeground(COLOR_TEXT_DARK);

        JLabel lblDesc = new JLabel("Quản lý sản phẩm và bảng giá dịch vụ theo cân nặng");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(COLOR_TEXT_MUTED);

        titlePanel.add(lblSub);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblMain);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblDesc);

        // Huy hiệu góc phải
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        RoundedPannel dateBadge = new RoundedPannel(Color.WHITE, 12);
        dateBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 6));
        JLabel lblDate = new JLabel(LocalDate.now().format(DF_DATE));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDate.setForeground(COLOR_TEXT_DARK);
        dateBadge.add(lblDate);

        RoundedPannel statusBadge = new RoundedPannel(new Color(254, 243, 199), 12);
        statusBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 6));
        JLabel lblStatus = new JLabel("● Chưa mở ca");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(new Color(217, 119, 6));
        statusBadge.add(lblStatus);

        rightActions.add(dateBadge);
        rightActions.add(statusBadge);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(rightActions, BorderLayout.EAST);
        return panel;
    }

    // 2. KHỐI BẢNG GIÁ DỊCH VỤ SPA
    private JPanel createSpaSection() {
        RoundedPannel card = new RoundedPannel(COLOR_CARD_BG, 16);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTitle.setOpaque(false);
        JLabel lblIcon = new JLabel("✂");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblIcon.setForeground(new Color(239, 68, 68));
        JLabel lblTitle = new JLabel("Bảng giá dịch vụ Spa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(COLOR_TEXT_DARK);

        leftTitle.add(lblIcon);
        leftTitle.add(lblTitle);

        btnTaoDichVu = createSolidButton("+ Tạo dịch vụ", COLOR_PRIMARY_BLUE, Color.WHITE);
//        btnTaoDichVu.addActionListener(e -> moDialogThemDichVuSpa());

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(btnTaoDichVu, BorderLayout.EAST);

        // Bảng dữ liệu dịch vụ Spa
        String[] cols = {"MÃ DV", "TÊN DỊCH VỤ", "LOÀI", "GIÁ CƠ BẢN MINH HỌA (≤5 KG)", "THỜI GIAN", "CÁCH TÍNH", "THAO TÁC"};
        modelSpa = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return laQuanLy() && c == 6; // Chỉ Quản lý mới được click cột Sửa
            }
        };

        tblDichVuSpa = new JTable(modelSpa);
        tblDichVuSpa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblDichVuSpa.setRowHeight(46);
        tblDichVuSpa.setShowVerticalLines(false);
        tblDichVuSpa.setGridColor(new Color(241, 245, 249));
        tblDichVuSpa.setSelectionBackground(new Color(248, 250, 252));
        tblDichVuSpa.setSelectionForeground(COLOR_TEXT_DARK);

        tblDichVuSpa.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblDichVuSpa.getTableHeader().setForeground(COLOR_TEXT_MUTED);
        tblDichVuSpa.getTableHeader().setBackground(COLOR_CARD_BG);
        tblDichVuSpa.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tblDichVuSpa.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));

        for (int i = 0; i < 6; i++) {
            tblDichVuSpa.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }

        // CỘT THAO TÁC (SỬA)
        tblDichVuSpa.getColumnModel().getColumn(6).setCellRenderer(new SpaActionRenderer());
        tblDichVuSpa.getColumnModel().getColumn(6).setCellEditor(new SpaActionEditor(new JCheckBox()));
        tblDichVuSpa.getColumnModel().getColumn(6).setPreferredWidth(70);

        JScrollPane tableScroll = new JScrollPane(tblDichVuSpa);
        tableScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDER));
        tableScroll.getViewport().setBackground(COLOR_CARD_BG);

        // Banner thông tin
        RoundedPannel bannerInfo = new RoundedPannel(new Color(239, 246, 255), 10);
        bannerInfo.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        JLabel lblNotice = new JLabel("Bảng giá trong bảng mẫu dùng cơ sở ≤5kg cho từng dịch vụ tương ứng, số liệu không đồng bộ đầy đủ nên không được coi là đây là biểu giá chính thức.");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNotice.setForeground(new Color(30, 64, 175));
        bannerInfo.add(lblNotice);

        card.add(topBar, BorderLayout.NORTH);
        card.add(tableScroll, BorderLayout.CENTER);
        card.add(bannerInfo, BorderLayout.SOUTH);
        return card;
    }

    // 3. KHỐI DANH MỤC SẢN PHẨM (CARD GRID)
    private JTextField txtTimKiemSanPham;

    // Trong createProductSection():
    private JPanel createProductSection() {
        RoundedPannel card = new RoundedPannel(COLOR_CARD_BG, 16);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTitle.setOpaque(false);
        JLabel lblIcon = new JLabel("🥫");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        JLabel lblTitle = new JLabel("Danh mục sản phẩm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(COLOR_TEXT_DARK);
        leftTitle.add(lblIcon);
        leftTitle.add(lblTitle);

        // Khu vực tìm kiếm và nút Thêm sản phẩm
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        // Ô tra cứu sản phẩm (Live search)
        txtTimKiemSanPham = new JTextField(15);
        txtTimKiemSanPham.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTimKiemSanPham.setPreferredSize(new Dimension(180, 36));
        txtTimKiemSanPham.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(0, 10, 0, 10)
        ));
        txtTimKiemSanPham.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { timKiem(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { timKiem(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { timKiem(); }
            private void timKiem() {
                taiDuLieuSanPham(txtTimKiemSanPham.getText().trim());
            }
        });

        btnThemSanPham = createSolidButton("+ Sản phẩm", COLOR_PRIMARY_BLUE, Color.WHITE);
        btnThemSanPham.addActionListener(e -> moDialogThemSanPham());

        rightActions.add(txtTimKiemSanPham);
        rightActions.add(btnThemSanPham);

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(rightActions, BorderLayout.EAST);

        pnlProductCards = new JPanel(new GridLayout(0, 4, 16, 16));
        pnlProductCards.setOpaque(false);

        card.add(topBar, BorderLayout.NORTH);
        card.add(pnlProductCards, BorderLayout.CENTER);
        return card;
    }

    // 4. PHÂN QUYỀN VAI TRÒ (RBAC)
    private boolean laQuanLy() {
        if (authService != null) {
            return authService.coVaiTro(VaiTro.QUAN_LY);
        }
        return vaiTroHienTai == VaiTro.QUAN_LY;
    }

    private void apDungPhanQuyen() {
        boolean quanLy = laQuanLy();

        // 1. Chỉ Quản lý mới thấy nút tạo dịch vụ và thêm sản phẩm
        if (btnTaoDichVu != null) btnTaoDichVu.setVisible(quanLy);
        if (btnThemSanPham != null) btnThemSanPham.setVisible(quanLy);

        // 2. Nếu là Thu ngân: Ẩn cột "Sửa" trên bảng dịch vụ Spa
        if (tblDichVuSpa != null && !quanLy) {
            int colSua = -1;
            for (int i = 0; i < tblDichVuSpa.getColumnCount(); i++) {
                if (" ".equals(tblDichVuSpa.getColumnName(i))) {
                    colSua = i;
                    break;
                }
            }
            if (colSua != -1) {
                tblDichVuSpa.removeColumn(tblDichVuSpa.getColumnModel().getColumn(colSua));
            }
        }
    }

    // 5. TẢI DỮ LIỆU TỪ BUS
    private void taiDuLieuSpa() {
        if (modelSpa == null) return;
        modelSpa.setRowCount(0);

        try {
            List<DichVuSpa> list = sanPhamService.layDanhSachDichVuSpa();
            for (DichVuSpa dv : list) {
                String loaiStr = (dv.getLoaiThuCungApDung() != null) ? dv.getLoaiThuCungApDung().name() : "Tất cả";
                if ("CA_HAI".equalsIgnoreCase(loaiStr)) loaiStr = "Tất cả";

                String cachTinh = "+60.000đ (5-10kg), +120.000đ (>10kg)";

                modelSpa.addRow(new Object[]{
                        dv.getMaDichVu(),
                        dv.getTenDichVu(),
                        loaiStr,
                        dfTien.format(dv.getDonGiaCoBan()),
                        dv.getThoiLuongPhut() + " phút",
                        cachTinh,
                        dv.getMaDichVu()
                });
            }
        } catch (Exception ex) {
            System.err.println("[LỖI TẢI DỊCH VỤ SPA]: " + ex.getMessage());
        }
    }
    
    public void taiDuLieuSanPham() {
        taiDuLieuSanPham("");
    }

    public void taiDuLieuSanPham(String keyword) {
        if (pnlProductCards == null) return;
        pnlProductCards.removeAll();

        try {
            List<SanPham> list = (keyword.isEmpty()) 
                    ? sanPhamService.layDanhSachSanPham() 
                    : sanPhamService.timKiemSanPham(keyword);

            for (SanPham sp : list) {
                // Nếu là Thợ Spa thì chỉ xem phụ kiện/vật tư tiêu hao (Giới hạn theo RBAC)
                if (vaiTroHienTai == VaiTro.THO_SPA && (sp instanceof ThucAn)) {
                    continue;
                }
                pnlProductCards.add(createSingleProductCard(sp));
            }
        } catch (Exception ex) {
            System.err.println("[LỖI TẢI SẢN PHẨM]: " + ex.getMessage());
        }

        pnlProductCards.revalidate();
        pnlProductCards.repaint();
    }

    /**
     * Tạo Card sản phẩm không hover
     */
    private JPanel createSingleProductCard(SanPham sp) {
        RoundedPannel card = new RoundedPannel(Color.WHITE, 14);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(14, 14, 14, 14)
        ));

        // 1. Icon minh họa
        JPanel iconWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        iconWrap.setOpaque(false);

        RoundedPannel iconBox = new RoundedPannel(new Color(248, 250, 252), 12);
        iconBox.setPreferredSize(new Dimension(56, 56));
        iconBox.setLayout(new GridBagLayout());

        String iconStr = "🥫";
        String ten = sp.getTenSanPham().toLowerCase();
        if (ten.contains("pate") || ten.contains("thức ăn") || ten.contains("cá hồi") || (sp instanceof ThucAn)) {
            iconStr = "🥫";
        } else if (ten.contains("dây dắt") || ten.contains("vòng cổ") || ten.contains("đồ chơi")) {
            iconStr = "🦮";
        } else if (ten.contains("topping") || ten.contains("bánh")) {
            iconStr = "🍘";
        } else {
            iconStr = "🎀";
        }

        JLabel lblIco = new JLabel(iconStr);
        lblIco.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconBox.add(lblIco);
        iconWrap.add(iconBox);

        // 2. Nội dung text
        JPanel centerInfo = new JPanel();
        centerInfo.setLayout(new BoxLayout(centerInfo, BoxLayout.Y_AXIS));
        centerInfo.setOpaque(false);

        JLabel lblName = new JLabel(sp.getTenSanPham());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(COLOR_TEXT_DARK);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        String danhMuc = (sp instanceof ThucAn || sp.getLoaiSanPham() == LoaiSanPham.THUC_AN) ? "Thức ăn" : "Phụ kiện";
        JLabel lblCategory = new JLabel(danhMuc + " - " + sp.getSoLuongTonKho() + " sản phẩm");
        lblCategory.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCategory.setForeground(COLOR_TEXT_MUTED);
        lblCategory.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPrice = new JLabel(dfTien.format(sp.getGiaBan()));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrice.setForeground(COLOR_TEXT_DARK);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerInfo.add(Box.createVerticalStrut(4));
        centerInfo.add(lblName);
        centerInfo.add(Box.createVerticalStrut(3));
        centerInfo.add(lblCategory);
        centerInfo.add(Box.createVerticalStrut(6));
        centerInfo.add(lblPrice);

        // 3. Nút chỉnh sửa 
        JButton btnSua = new JButton("Chỉnh sửa sản phẩm");
        btnSua.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSua.setForeground(COLOR_TEXT_MUTED);
        btnSua.setContentAreaFilled(false);
        btnSua.setBorderPainted(false);
        btnSua.setFocusPainted(false);
        btnSua.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSua.setVisible(laQuanLy()); // Thu ngân sẽ không nhìn thấy
        btnSua.addActionListener(e -> moDialogCapNhatSanPham(sp));

        card.add(iconWrap, BorderLayout.NORTH);
        card.add(centerInfo, BorderLayout.CENTER);
        card.add(btnSua, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Tạo nút phẳng tĩnh
     */
    private JButton createSolidButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    // 6. ĐIỀU HƯỚNG DIALOG
//    private void moDialogThemDichVuSpa() {
//        if (!laQuanLy()) {
//            JOptionPane.showMessageDialog(this, "Chỉ Quản lý mới có quyền tạo dịch vụ Spa!", "Từ chối", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        try {
//            BanHangSpaDAO spaDao = new BanHangSpaDAO();
//            BanHangSpaService spaBus = new BanHangSpaService();
//
//            List<BanHangSpaDAO.DichVu> dsDichVu = spaDao.layTatCaDichVu();
//            List<BanHangSpaDAO.ThuCung> dsThuCung = spaDao.layTatCaThuCung();
//
//            if (dsDichVu.isEmpty()) {
//                JOptionPane.showMessageDialog(this, "Chưa có danh mục dịch vụ Spa trong cơ sở dữ liệu!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
//                return;
//            }
//
//            // Gọi phương thức tĩnh hienThi của ThemDichVuSpaDialog
//            ThemDichVuSpaDialog.hienThi(this, dsDichVu, dsThuCung, spaBus);[cite: 8]
//            taiDuLieuSpa();
//
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(this,
//                    "Không thể mở cửa sổ Dịch vụ Spa: " + ex.getMessage(),
//                    "Thông báo", JOptionPane.WARNING_MESSAGE);
//        }
//    }
    
    private void moDialogThemSanPham() {
        if (!laQuanLy()) {
            JOptionPane.showMessageDialog(this, "Chỉ Quản lý mới có quyền tạo mặt hàng mới!", "Từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        ThemSanPhamDialog dlg = new ThemSanPhamDialog(parent, sanPhamService);
        dlg.setVisible(true);
        if (dlg.isDaLuuThanhCong()) {
            taiDuLieuSanPham();
        }
    }

    private void moDialogCapNhatSanPham(SanPham sp) {
        if (!laQuanLy()) {
            JOptionPane.showMessageDialog(this, "Chỉ Quản lý mới có quyền chỉnh sửa mặt hàng!", "Từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        CapNhatSanPhamDialog dlg = new CapNhatSanPhamDialog(parent, sanPhamService, sp);
        dlg.setVisible(true);
        if (dlg.isDaCapNhatThanhCong()) {
            taiDuLieuSanPham();
        }
    }

    // 7. RENDERER & CELL EDITOR CHO NÚT SỬA DỊCH VỤ SPA
    private static class SpaActionRenderer extends JButton implements TableCellRenderer {
        public SpaActionRenderer() {
            setText("Sửa");
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(COLOR_PRIMARY_BLUE);
            setBackground(new Color(239, 246, 255));
            setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254), 1));
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class SpaActionEditor extends DefaultCellEditor {
        private final JButton btn;
        private String maDV;

        public SpaActionEditor(JCheckBox checkBox) {
            super(checkBox);
            btn = new JButton("Sửa");
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setForeground(COLOR_PRIMARY_BLUE);
            btn.setBackground(new Color(239, 246, 255));
            btn.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254), 1));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                fireEditingStopped();
                if (!laQuanLy()) {
                    JOptionPane.showMessageDialog(SanPhamPanel.this,
                            "Chỉ Quản lý mới có quyền chỉnh sửa bảng giá dịch vụ!",
                            "Từ chối", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(SanPhamPanel.this,
                        "Mở chỉnh sửa đơn giá và thông tin dịch vụ Spa: " + maDV,
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.maDV = (value != null) ? value.toString() : "";
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return maDV;
        }
    }

}