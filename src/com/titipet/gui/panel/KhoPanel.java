package com.titipet.gui.panel;

import com.titipet.bus.AuthService;
import com.titipet.bus.KhoService;
import com.titipet.bus.SanPhamService;
import com.titipet.entity.SanPham;
import com.titipet.entity.ThucAn;
import com.titipet.entity.enums.VaiTro;
import com.titipet.gui.dialog.NhapKhoDialog;
import com.titipet.gui.dialog.ThemSanPhamDialog;
import com.titipet.gui.dialog.CapNhatSanPhamDialog;
import com.titipet.gui.dialog.LichSuNhapKhoDialog;
import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * KhoHangPanel: Màn hình Quản lý kho hàng, hạn sử dụng & cảnh báo tồn kho.
 * Phân quyền nghiêm ngặt theo ma trận TiTiPet:
 * - QUAN_LY: Toàn quyền nhập hàng, thêm mới, sửa giá, xuất báo cáo CSV.
 * - THU_NGAN: Chỉ được tra cứu tồn kho & cảnh báo (Ẩn nút nhập hàng, thêm mới, xuất CSV, cột Sửa).
 * - THO_SPA: Khóa toàn quyền truy cập.
 */
public class KhoPanel extends JPanel {

    private final KhoService khoHangService;
    private final SanPhamService sanPhamSerVice;
    private AuthService authService;
    private VaiTro vaiTroHienTai;

    // CÁC NÚT TÁC VỤ PHÂN QUYỀN
    private JButton btnNhapHang;
    private JButton btnMatHangMoi;
    private JButton btnXuatCsv;
    private JButton btnLichSuNhap;

    // TÌM KIẾM
    private JTextField txtTimKiem;
    private JLabel lblClearSearch;

    // BẢNG DỮ LIỆU & KPI
    private JTable tblKhoHang;
    private DefaultTableModel tableModel;
    private JLabel lblKpiTongMatHang;
    private JLabel lblKpiSapHet;
    private JLabel lblKpiCanHan;
    private JLabel lblKpiHetHan;

    // HUY HIỆU CA LÀM VIỆC
    private JLabel lblNgayHienTai;
    private JLabel lblTrangThaiCa;
    private RoundedPannel statusBadge;

    // BẢNG MÀU CHUẨN FLAT DESIGN
    private static final Color COLOR_BG = new Color(245, 247, 251);
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_SEARCH_BG = new Color(240, 242, 245);

    private final DecimalFormat dfTien = new DecimalFormat("#,##0 đ");
    private static final DateTimeFormatter DF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public KhoPanel(AuthService authService) {
        this.authService = authService;
        this.khoHangService = new KhoService();
        this.sanPhamSerVice = new SanPhamService();

        if (authService != null && authService.getNhanVienDangNhap().isPresent()) {
            this.vaiTroHienTai = authService.getNhanVienDangNhap().get().getVaiTro();
        } else {
            this.vaiTroHienTai = VaiTro.QUAN_LY;
        }
		

        initComponents();
        loadData("");
        capNhatKpiToanKho();
        apDungPhanQuyen();
    }

    public KhoPanel(VaiTro vaiTro) {
        this.vaiTroHienTai = (vaiTro != null) ? vaiTro : VaiTro.QUAN_LY;
        this.khoHangService = new KhoService();
		this.sanPhamSerVice = new SanPhamService();
		
        initComponents();
        loadData("");
        capNhatKpiToanKho();
        apDungPhanQuyen();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // NẾU LÀ THỢ SPA: KHÓA TRUY CẬP
        if (vaiTroHienTai == VaiTro.THO_SPA) {
            JPanel lockPanel = new JPanel(new GridBagLayout());
            lockPanel.setBackground(COLOR_BG);
            JLabel lblLock = new JLabel("Bạn không có quyền truy cập module Kho hàng & Hạn sử dụng!");
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

        mainContent.add(createHeaderSection());
        mainContent.add(Box.createVerticalStrut(18));
        mainContent.add(createMetricsSection());
        mainContent.add(Box.createVerticalStrut(20));
        mainContent.add(createTableSection());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    // 1. HEADER SECTION
    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblSub = new JLabel("TI TI PET - WORKSPACE");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSub.setForeground(COLOR_TEXT_MUTED);

        JLabel lblMain = new JLabel("Quản lý kho & hạn sử dụng");
        lblMain.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblMain.setForeground(COLOR_TEXT_DARK);

        JLabel lblDesc = new JLabel("Nhập hàng, số lượng tồn và cảnh báo cận hạn");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(COLOR_TEXT_MUTED);

        titlePanel.add(lblSub);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblMain);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblDesc);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        RoundedPannel dateBadge = new RoundedPannel(Color.WHITE, 12);
        dateBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 6));
        String ngayHomNay = java.time.LocalDate.now().format(DF_DATE);
        lblNgayHienTai = new JLabel(ngayHomNay);
        lblNgayHienTai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNgayHienTai.setForeground(COLOR_TEXT_DARK);
        dateBadge.add(lblNgayHienTai);

        statusBadge = new RoundedPannel(new Color(254, 243, 199), 12);
        statusBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 6));
        lblTrangThaiCa = new JLabel("● Chưa mở ca");
        lblTrangThaiCa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTrangThaiCa.setForeground(new Color(217, 119, 6));
        statusBadge.add(lblTrangThaiCa);

        rightActions.add(dateBadge);
        rightActions.add(statusBadge);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(rightActions, BorderLayout.EAST);
        return panel;
    }

    public void capNhatTrangThaiCa(boolean daMoCa) {
        if (statusBadge == null || lblTrangThaiCa == null) return;
        if (daMoCa) {
            statusBadge.setBackground(new Color(220, 252, 231));
            lblTrangThaiCa.setText("● Đang trong ca");
            lblTrangThaiCa.setForeground(new Color(22, 163, 74));
        } else {
            statusBadge.setBackground(new Color(254, 243, 199));
            lblTrangThaiCa.setText("● Chưa mở ca");
            lblTrangThaiCa.setForeground(new Color(217, 119, 6));
        }
        statusBadge.repaint();
    }

    // 2. BỐN THẺ KPI THỐNG KÊ (METRICS SECTION)
    private JPanel createMetricsSection() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        lblKpiTongMatHang = new JLabel("0");
        lblKpiSapHet = new JLabel("0");
        lblKpiCanHan = new JLabel("0");
        lblKpiHetHan = new JLabel("0");

        grid.add(createMetricCard("📦", lblKpiTongMatHang, "Mặt hàng", new Color(239, 246, 255)));
        grid.add(createMetricCard("⚠️", lblKpiSapHet, "Sắp hết", new Color(254, 243, 199)));
        grid.add(createMetricCard("⏳", lblKpiCanHan, "Cận hạn ≤30 ngày", new Color(243, 232, 255)));
        grid.add(createMetricCard("❌", lblKpiHetHan, "Hết hạn", new Color(254, 242, 242)));

        return grid;
    }

    private JPanel createMetricCard(String icon, JLabel lblVal, String title, Color iconBg) {
        RoundedPannel card = new RoundedPannel(COLOR_CARD_BG, 14);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 16));

        RoundedPannel iconBox = new RoundedPannel(iconBg, 12);
        iconBox.setPreferredSize(new Dimension(48, 48));
        iconBox.setLayout(new GridBagLayout());
        JLabel lblIco = new JLabel(icon);
        lblIco.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconBox.add(lblIco);

        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(COLOR_TEXT_DARK);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        textGroup.add(lblVal);
        textGroup.add(Box.createVerticalStrut(2));
        textGroup.add(lblTitle);

        card.add(iconBox);
        card.add(textGroup);
        return card;
    }
    
    // 3. BẢNG TỒN KHO & THANH TÁC VỤ
    private JPanel createTableSection() {
        RoundedPannel card = new RoundedPannel(COLOR_CARD_BG, 16);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // HEADER BẢNG
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel();
        leftTitle.setLayout(new BoxLayout(leftTitle, BoxLayout.Y_AXIS));
        leftTitle.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Kho hàng, hạn sử dụng & cảnh báo");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTableTitle.setForeground(COLOR_TEXT_DARK);

        JLabel lblTableSub = new JLabel("Chỉ quản lý có quyền chỉnh sửa đơn giá sản phẩm");
        lblTableSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTableSub.setForeground(COLOR_TEXT_MUTED);

        leftTitle.add(lblTableTitle);
        leftTitle.add(Box.createVerticalStrut(2));
        leftTitle.add(lblTableSub);

        JPanel actionButtonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtonGroup.setOpaque(false);

        btnXuatCsv = createOutlineButton("Xuất kho CSV");
        btnXuatCsv.addActionListener(e -> xuatFileCsv());

        btnMatHangMoi = createOutlineButton("+ Mặt hàng mới");
        btnMatHangMoi.addActionListener(e -> moDialogThemSP());

        btnNhapHang = new JButton("+ Nhập hàng");
        btnNhapHang.setBackground(COLOR_PRIMARY_BLUE);
        btnNhapHang.setForeground(Color.WHITE);
        btnNhapHang.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnNhapHang.setFocusPainted(false);
        btnNhapHang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNhapHang.setBorder(new EmptyBorder(10, 18, 10, 18));
        btnNhapHang.addActionListener(e -> moDialogNhapKho());
        
        btnLichSuNhap = createOutlineButton("Lịch sử nhập");
        btnLichSuNhap.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            LichSuNhapKhoDialog dlg = new LichSuNhapKhoDialog(parentFrame, khoHangService);
            dlg.setVisible(true);
        });
        
        actionButtonGroup.add(btnLichSuNhap);
        actionButtonGroup.add(btnXuatCsv);
        actionButtonGroup.add(btnMatHangMoi);
        actionButtonGroup.add(btnNhapHang);

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(actionButtonGroup, BorderLayout.EAST);

        // THANH TÌM KIẾM
        JPanel searchBarRow = new JPanel(new BorderLayout(12, 0));
        searchBarRow.setOpaque(false);

        RoundedPannel searchBox = new RoundedPannel(COLOR_SEARCH_BG, 20);
        searchBox.setPreferredSize(new Dimension(0, 42));
        searchBox.setLayout(new BorderLayout(8, 0));
        searchBox.setBorder(new EmptyBorder(0, 14, 0, 14));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblSearchIcon.setForeground(COLOR_TEXT_MUTED);

        txtTimKiem = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !(FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    g2.setColor(COLOR_TEXT_MUTED);
                    g2.drawString("Tìm sản phẩm", 5, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        txtTimKiem.setOpaque(false);
        txtTimKiem.setBorder(null);
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTimKiem.setForeground(COLOR_TEXT_DARK);

        lblClearSearch = new JLabel("X");
        lblClearSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblClearSearch.setForeground(COLOR_TEXT_MUTED);
        lblClearSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClearSearch.setVisible(false);
        lblClearSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                txtTimKiem.setText("");
                txtTimKiem.requestFocus();
            }
        });

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { xuLyTimKiem(); }
            @Override
            public void removeUpdate(DocumentEvent e) { xuLyTimKiem(); }
            @Override
            public void changedUpdate(DocumentEvent e) { xuLyTimKiem(); }

            private void xuLyTimKiem() {
                String kw = txtTimKiem.getText().trim();
                lblClearSearch.setVisible(!kw.isEmpty());
                loadData(kw);
            }
        });

        searchBox.add(lblSearchIcon, BorderLayout.WEST);
        searchBox.add(txtTimKiem, BorderLayout.CENTER);
        searchBox.add(lblClearSearch, BorderLayout.EAST);
        searchBarRow.add(searchBox, BorderLayout.CENTER);

        JPanel cardHeaderGroup = new JPanel();
        cardHeaderGroup.setLayout(new BoxLayout(cardHeaderGroup, BoxLayout.Y_AXIS));
        cardHeaderGroup.setOpaque(false);
        cardHeaderGroup.add(topBar);
        cardHeaderGroup.add(Box.createVerticalStrut(14));
        cardHeaderGroup.add(searchBarRow);

        card.add(cardHeaderGroup, BorderLayout.NORTH);

        // BẢNG DỮ LIỆU
        String[] columns = {
                "MÃ SP", "SẢN PHẨM", "DANH MỤC", "ĐƠN GIÁ BÁN", "TỒN KHO", "HSD", "TRẠNG THÁI", "THAO TÁC"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Chỉ Quản lý mới được click cột THAO TÁC (sửa thông tin/giá)
                if (laQuanLy()) {
                    return column == 7;
                }
                return false;
            }
        };

        tblKhoHang = new JTable(tableModel);
        tblKhoHang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblKhoHang.setRowHeight(50);
        tblKhoHang.setShowVerticalLines(false);
        tblKhoHang.setGridColor(new Color(241, 245, 249));
        tblKhoHang.setSelectionBackground(new Color(248, 250, 252));
        tblKhoHang.setSelectionForeground(COLOR_TEXT_DARK);

        tblKhoHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblKhoHang.getTableHeader().setForeground(COLOR_TEXT_MUTED);
        tblKhoHang.getTableHeader().setBackground(COLOR_CARD_BG);
        tblKhoHang.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tblKhoHang.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        tblKhoHang.getColumnModel().getColumn(0).setCellRenderer(new DefaultAlignRenderer(SwingConstants.LEFT));
        tblKhoHang.getColumnModel().getColumn(1).setCellRenderer(new ProductCellRenderer());
        tblKhoHang.getColumnModel().getColumn(2).setCellRenderer(new DefaultAlignRenderer(SwingConstants.LEFT));
        tblKhoHang.getColumnModel().getColumn(3).setCellRenderer(new DefaultAlignRenderer(SwingConstants.LEFT));
        tblKhoHang.getColumnModel().getColumn(4).setCellRenderer(new StockCellRenderer());
        tblKhoHang.getColumnModel().getColumn(5).setCellRenderer(new DefaultAlignRenderer(SwingConstants.LEFT));
        tblKhoHang.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
        tblKhoHang.getColumnModel().getColumn(7).setCellRenderer(new ActionButtonRenderer());
        tblKhoHang.getColumnModel().getColumn(7).setCellEditor(new ActionButtonEditor(new JCheckBox()));

        JScrollPane tableScroll = new JScrollPane(tblKhoHang);
        tableScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDER));
        tableScroll.getViewport().setBackground(COLOR_CARD_BG);

        RoundedPannel bannerWarning = new RoundedPannel(new Color(254, 243, 199), 10);
        bannerWarning.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        JLabel lblNotice = new JLabel("Khi bán hàng: luôn xuất theo lô cũ trước. Các mặt hàng cận hạn ≤ 30 ngày cần ưu đãi hoặc xuất thanh lý.");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNotice.setForeground(new Color(180, 83, 9));
        bannerWarning.add(lblNotice);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(bannerWarning, BorderLayout.SOUTH);

        card.add(centerPanel, BorderLayout.CENTER);
        return card;
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(COLOR_TEXT_DARK);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // 4. PHÂN QUYỀN VAI TRÒ
    private boolean laQuanLy() {
        if (authService != null) {
            return authService.coVaiTro(VaiTro.QUAN_LY);
        }
        return vaiTroHienTai == VaiTro.QUAN_LY;
    }

    private void apDungPhanQuyen() {
        boolean quanLy = laQuanLy();

        // 1. Chỉ QUẢN LÝ mới có quyền Nhập hàng, Mặt hàng mới, Xuất CSV
        if (btnNhapHang != null) btnNhapHang.setVisible(quanLy);
        if (btnMatHangMoi != null) btnMatHangMoi.setVisible(quanLy);
        if (btnXuatCsv != null) btnXuatCsv.setVisible(quanLy);
        if (btnLichSuNhap != null) btnLichSuNhap.setVisible(quanLy);

        // 2. Nếu là THU NGÂN: Ẩn cột THAO TÁC (không cho sửa giá / sửa thông tin)
        if (tblKhoHang != null && !quanLy) {
            int colThaoTac = -1;
            for (int i = 0; i < tblKhoHang.getColumnCount(); i++) {
                if ("THAO TÁC".equalsIgnoreCase(tblKhoHang.getColumnName(i))) {
                    colThaoTac = i;
                    break;
                }
            }
            if (colThaoTac != -1) {
                tblKhoHang.removeColumn(tblKhoHang.getColumnModel().getColumn(colThaoTac));
            }
        }
    }

    // 5. TẢI DỮ LIỆU TỒN KHO & TÍNH TOÁN KPI
    /**
     * Cập nhật số liệu 4 thẻ KPI dựa trên toàn bộ kho hàng
     */
    public void capNhatKpiToanKho() {
        try {
            List<SanPham> toanBoKho = khoHangService.layDanhSachSanPhamKho("");
            int countTong = 0;
            int countSapHet = 0;
            int countCanHan = 0;
            int countHetHan = 0;

            for (SanPham sp : toanBoKho) {
                countTong++;
                String trangThai = khoHangService.xacDinhTrangThai(sp);

                if ("Hết hạn".equals(trangThai)) {
                    countHetHan++;
                } else if ("Sắp hết hạn".equals(trangThai)) {
                    countCanHan++;
                } else if ("Sắp hết hàng".equals(trangThai)) {
                    countSapHet++;
                }
            }

            if (lblKpiTongMatHang != null) lblKpiTongMatHang.setText(String.valueOf(countTong));
            if (lblKpiSapHet != null) lblKpiSapHet.setText(String.valueOf(countSapHet));
            if (lblKpiCanHan != null) lblKpiCanHan.setText(String.valueOf(countCanHan));
            if (lblKpiHetHan != null) lblKpiHetHan.setText(String.valueOf(countHetHan));

        } catch (Exception ex) {
            System.err.println("[LỖI CẬP NHẬT KPI TOÀN KHO]: " + ex.getMessage());
        }
    }
    
    public void loadData(String keyword) {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        try {
            List<SanPham> danhSach = khoHangService.layDanhSachSanPhamKho(keyword);

            for (SanPham sp : danhSach) {
                String danhMuc = (sp instanceof ThucAn) ? "Thức ăn" : "Phụ kiện";
                String strHsd = "--";

                if (sp instanceof ThucAn ta && ta.getHanSuDung() != null) {
                    strHsd = ta.getHanSuDung().format(DF_DATE);
                }

                String trangThai = khoHangService.xacDinhTrangThai(sp);

                tableModel.addRow(new Object[]{
                        sp.getMaSanPham(),
                        sp.getTenSanPham(),
                        danhMuc,
                        dfTien.format(sp.getGiaBan()),
                        sp.getSoLuongTonKho(),
                        strHsd,
                        trangThai,
                        sp.getMaSanPham()
                });
            }

        } catch (Exception ex) {
            System.err.println("[LỖI LOAD KHO HÀNG]: " + ex.getMessage());
        }
    }

    private void moDialogNhapKho() {
        if (!laQuanLy()) {
            JOptionPane.showMessageDialog(this, "Chỉ Quản lý mới có quyền nhập hàng và điều chỉnh tồn!", "Từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        NhapKhoDialog dialog = new NhapKhoDialog(parentFrame, khoHangService);
        dialog.setVisible(true);
        loadData(txtTimKiem.getText());
        capNhatKpiToanKho();
    }

    private void moDialogThemSP() {
        if (!laQuanLy()) {
            JOptionPane.showMessageDialog(this, "Chỉ Quản lý mới có quyền thêm sản phẩm", "Từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        ThemSanPhamDialog dialog = new ThemSanPhamDialog(parentFrame, sanPhamSerVice);
        dialog.setVisible(true);
        loadData(txtTimKiem.getText());
        capNhatKpiToanKho();
    }
    
    private void moDialogCapNhatSP(String maSP) {
        if (!laQuanLy()) {
            JOptionPane.showMessageDialog(this, "Chỉ Quản lý mới có quyền thêm sản phẩm", "Từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // 1. Tìm thông tin sản phẩm đầy đủ từ Service
            SanPham sp = sanPhamSerVice.timSanPhamTheoMa(maSP);
            if (sp == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin sản phẩm: " + maSP, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Mở dialog chỉnh sửa
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            CapNhatSanPhamDialog dialog = new CapNhatSanPhamDialog(parentFrame, sanPhamSerVice, sp);
            dialog.setVisible(true);

            // 3. Tải lại dữ liệu sau khi sửa thành công
            if (dialog.isDaCapNhatThanhCong()) {
                loadData(txtTimKiem.getText().trim());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi mở cửa sổ cập nhật: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        loadData(txtTimKiem.getText());
        capNhatKpiToanKho();
    }
    
    private void xuatFileCsv() {
        if (!laQuanLy()) return;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file tồn kho CSV");
        fileChooser.setSelectedFile(new File("BaoCaoTonKho_" + java.time.LocalDate.now() + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(fileToSave, StandardCharsets.UTF_8)) {
                pw.write('\ufeff');
                pw.println("Mã SP,Tên sản phẩm,Danh mục,Đơn giá,Tồn kho,HSD,Trạng thái");

                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\"\n",
                            tableModel.getValueAt(r, 0),
                            tableModel.getValueAt(r, 1),
                            tableModel.getValueAt(r, 2),
                            tableModel.getValueAt(r, 3),
                            tableModel.getValueAt(r, 4),
                            tableModel.getValueAt(r, 5),
                            tableModel.getValueAt(r, 6));
                }
                JOptionPane.showMessageDialog(this, "Xuất dữ liệu CSV thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 6. CÁC LỚP RENDERER ĐỊNH DẠNG TÙY BIẾN
    private static class ProductCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblIcon = new JLabel("", SwingConstants.CENTER);
        private final JLabel lblName = new JLabel();

        public ProductCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
            setOpaque(true);

            lblIcon.setPreferredSize(new Dimension(34, 34));
            lblIcon.setOpaque(true);
            lblIcon.setBackground(new Color(241, 245, 249));
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

            lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblName.setForeground(COLOR_TEXT_DARK);

            add(lblIcon);
            add(lblName);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String name = (value != null) ? value.toString() : "";
            lblName.setText(name);

            if (name.toLowerCase().contains("pate") || name.toLowerCase().contains("thức ăn") || name.toLowerCase().contains("hạt")) {
                lblIcon.setText("🥫");
            } else if (name.toLowerCase().contains("đồ chơi") || name.toLowerCase().contains("cây cào")) {
                lblIcon.setText("🎾");
            } else {
                lblIcon.setText("📦");
            }

            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private static class StockCellRenderer extends DefaultTableCellRenderer {
        public StockCellRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            try {
                int ton = Integer.parseInt(value.toString());
                if (ton <= 3) {
                    c.setForeground(new Color(220, 38, 38));
                } else {
                    c.setForeground(COLOR_TEXT_DARK);
                }
            } catch (Exception ignored) {}
            return c;
        }
    }

    private static class StatusBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblBadge = new JLabel("", SwingConstants.CENTER);

        public StatusBadgeRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 10));
            setOpaque(true);

            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblBadge.setOpaque(true);
            lblBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
            add(lblBadge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (value != null) ? value.toString() : "Bình thường";
            lblBadge.setText(status);

            switch (status) {
                case "Hết hạn" -> {
                    lblBadge.setBackground(new Color(254, 242, 242));
                    lblBadge.setForeground(new Color(220, 38, 38));
                }
                case "Sắp hết hạn" -> {
                    lblBadge.setBackground(new Color(254, 243, 199));
                    lblBadge.setForeground(new Color(217, 119, 6));
                }
                case "Sắp hết hàng" -> {
                    lblBadge.setBackground(new Color(255, 237, 213));
                    lblBadge.setForeground(new Color(194, 65, 12));
                }
                default -> {
                    lblBadge.setBackground(new Color(220, 252, 231));
                    lblBadge.setForeground(new Color(22, 163, 74));
                }
            }

            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private static class DefaultAlignRenderer extends DefaultTableCellRenderer {
        public DefaultAlignRenderer(int alignment) {
            setHorizontalAlignment(alignment);
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }
    }

    private static class ActionButtonRenderer extends JButton implements TableCellRenderer {
        public ActionButtonRenderer() {
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

    private class ActionButtonEditor extends DefaultCellEditor {
        private final JButton btn;
        private String maSP;

        public ActionButtonEditor(JCheckBox checkBox) {
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
                    JOptionPane.showMessageDialog(KhoPanel.this,
                            "Chỉ Quản lý mới có quyền chỉnh sửa thông tin sản phẩm!",
                            "Từ chối", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                moDialogCapNhatSP(maSP);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.maSP = (value != null) ? value.toString() : "";
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return maSP;
        }
    }
    
}