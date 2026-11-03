package com.titipet.gui.panel;

import com.titipet.bus.AuthService;
import com.titipet.bus.KhuyenMaiService;
import com.titipet.entity.KhuyenMai;
import com.titipet.entity.enums.PhamVi;
import com.titipet.entity.enums.VaiTro;
import com.titipet.gui.dialog.CapNhatKhuyenMaiDialog;
import com.titipet.gui.dialog.TaoKhuyenMaiDialog;
import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KhuyenMaiPanel extends JPanel {

    private final KhuyenMaiService khuyenMaiService;
    private AuthService authService;
    private VaiTro vaiTroHienTai;

    private JTable tblKhuyenMai;
    private DefaultTableModel modelKhuyenMai;
    private JButton btnTaoKhuyenMai;

    // BẢNG MÀU THIẾT KẾ FLAT
    private static final Color COLOR_BG = new Color(245, 247, 251);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_ACTIVE_BG = new Color(236, 253, 245);
    private static final Color COLOR_ACTIVE_TEXT = new Color(5, 150, 105);

    private static final DateTimeFormatter DF_DATE = DateTimeFormatter.ofPattern("d/M/yyyy");
    private final DecimalFormat dfTien = new DecimalFormat("#,##0 đ");

    public KhuyenMaiPanel(AuthService authService) {
        this.authService = authService;
        this.khuyenMaiService = new KhuyenMaiService();

        if (authService != null && authService.getNhanVienDangNhap().isPresent()) {
            this.vaiTroHienTai = authService.getNhanVienDangNhap().get().getVaiTro();
        } else {
            this.vaiTroHienTai = VaiTro.QUAN_LY;
        }

        initComponents();
        taiDuLieuKhuyenMai();
        apDungPhanQuyen();
    }

    public KhuyenMaiPanel(VaiTro vaiTro) {
        this.vaiTroHienTai = (vaiTro != null) ? vaiTro : VaiTro.QUAN_LY;
        this.khuyenMaiService = new KhuyenMaiService();

        initComponents();
        taiDuLieuKhuyenMai();
        apDungPhanQuyen();
    }

    public KhuyenMaiPanel() {
        this(VaiTro.QUAN_LY);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // THỢ SPA: KHÓA TRUY CẬP THEO PHÂN QUYỀN RBAC
        if (vaiTroHienTai == VaiTro.THO_SPA) {
            JPanel lockPanel = new JPanel(new GridBagLayout());
            lockPanel.setBackground(COLOR_BG);
            JLabel lblLock = new JLabel("Bạn không có quyền truy cập module Khuyến mãi!");
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

        // 2. Khối bảng danh sách khuyến mãi
        mainContent.add(createKhuyenMaiTableSection());

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

        JLabel lblMain = new JLabel("Chương trình khuyến mãi");
        lblMain.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblMain.setForeground(COLOR_TEXT_DARK);

        titlePanel.add(lblSub);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblMain);

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

    // 2. KHỐI BẢNG DANH SÁCH CHƯƠNG TRÌNH KHUYẾN MÃI
    private JPanel createKhuyenMaiTableSection() {
        RoundedPannel card = new RoundedPannel(COLOR_CARD_BG, 16);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel();
        leftTitle.setLayout(new BoxLayout(leftTitle, BoxLayout.Y_AXIS));
        leftTitle.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách chương trình khuyến mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(COLOR_TEXT_DARK);

        JLabel lblSub = new JLabel("Hệ thống kiểm tra hiệu lực tự động khi áp dụng đơn hàng");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(COLOR_TEXT_MUTED);

        leftTitle.add(lblTitle);
        leftTitle.add(Box.createVerticalStrut(2));
        leftTitle.add(lblSub);

        btnTaoKhuyenMai = new JButton("+ Tạo khuyến mãi");
        btnTaoKhuyenMai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTaoKhuyenMai.setBackground(COLOR_PRIMARY_BLUE);
        btnTaoKhuyenMai.setForeground(Color.WHITE);
        btnTaoKhuyenMai.setFocusPainted(false);
        btnTaoKhuyenMai.setContentAreaFilled(true);
        btnTaoKhuyenMai.setOpaque(true);
        btnTaoKhuyenMai.setBorderPainted(false);
        btnTaoKhuyenMai.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTaoKhuyenMai.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnTaoKhuyenMai.addActionListener(e -> moDialogTaoKhuyenMai());

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(btnTaoKhuyenMai, BorderLayout.EAST);

        // Cấu hình Bảng dữ liệu
        String[] cols = {"CHƯƠNG TRÌNH", "ÁP DỤNG CHO", "GIÁ TRỊ", "THỜI GIAN", "TRẠNG THÁI", "THAO TÁC"};
        modelKhuyenMai = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return laQuanLy() && c == 5;
            }
        };

        tblKhuyenMai = new JTable(modelKhuyenMai);
        tblKhuyenMai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblKhuyenMai.setRowHeight(52);
        tblKhuyenMai.setShowVerticalLines(false);
        tblKhuyenMai.setGridColor(new Color(241, 245, 249));
        tblKhuyenMai.setSelectionBackground(new Color(248, 250, 252));
        tblKhuyenMai.setSelectionForeground(COLOR_TEXT_DARK);

        // Header bảng chữ đậm sắc nét
        tblKhuyenMai.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblKhuyenMai.getTableHeader().setBackground(COLOR_CARD_BG);
        tblKhuyenMai.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tblKhuyenMai.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(COLOR_TEXT_DARK);
                lbl.setBackground(Color.WHITE);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                lbl.setHorizontalAlignment(column == 4 || column == 5 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return lbl;
            }
        };

        for (int i = 0; i < tblKhuyenMai.getColumnCount(); i++) {
            tblKhuyenMai.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        tblKhuyenMai.getColumnModel().getColumn(0).setCellRenderer(new ChuongTrinhRenderer());
        tblKhuyenMai.getColumnModel().getColumn(1).setCellRenderer(createLeftRenderer());
        tblKhuyenMai.getColumnModel().getColumn(2).setCellRenderer(createLeftRenderer());
        tblKhuyenMai.getColumnModel().getColumn(3).setCellRenderer(createLeftRenderer());
        tblKhuyenMai.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
        tblKhuyenMai.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonsRenderer());
        tblKhuyenMai.getColumnModel().getColumn(5).setCellEditor(new ActionButtonsEditor(new JCheckBox()));
        tblKhuyenMai.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane tableScroll = new JScrollPane(tblKhuyenMai);
        tableScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDER));
        tableScroll.getViewport().setBackground(COLOR_CARD_BG);

        // Banner thông tin màu cam nhạt ở đáy bảng
        RoundedPannel bannerWarning = new RoundedPannel(new Color(254, 243, 199), 10);
        bannerWarning.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        JLabel lblNotice = new JLabel("Mỗi hóa đơn áp dụng 1 ưu đãi khuyến mãi. Được cộng dồn điểm và không gia tăng cộng tích điểm nếu có áp dụng giảm giá trên chiết khấu quy định dữ liệu.");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNotice.setForeground(new Color(180, 83, 9));
        bannerWarning.add(lblNotice);

        card.add(topBar, BorderLayout.NORTH);
        card.add(tableScroll, BorderLayout.CENTER);
        card.add(bannerWarning, BorderLayout.SOUTH);
        return card;
    }

    private DefaultTableCellRenderer createLeftRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.LEFT);
        r.setBorder(new EmptyBorder(0, 10, 0, 10));
        return r;
    }

    // 3. TẢI DỮ LIỆU TỪ BUS
    public void taiDuLieuKhuyenMai() {
        if (modelKhuyenMai == null) return;
        modelKhuyenMai.setRowCount(0);

        try {
            List<KhuyenMai> list = khuyenMaiService.layDanhSachKhuyenMai();
            for (KhuyenMai km : list) {
                // Định dạng hiển thị phạm vi áp dụng
                String strPhamVi = "Tất cả";
                if (km.getPhamVi() != null) {
                    switch (km.getPhamVi()) {
                        case PHU_KIEN -> strPhamVi = "Phụ kiện";
                        case THUC_AN -> strPhamVi = "Thức ăn";
                        case DICH_VU -> strPhamVi = "Dịch vụ";
                        default -> strPhamVi = "Tất cả";
                    }
                }

                // Định dạng giá trị giảm (nếu <= 100 hiển thị %, ngược lại hiển thị đ)
                String strGiaTri;
                if (km.getGiamTram() != null) {
                    if (km.getGiamTram().compareTo(new java.math.BigDecimal("100")) <= 0) {
                        strGiaTri = km.getGiamTram().stripTrailingZeros().toPlainString() + "%";
                    } else {
                        strGiaTri = dfTien.format(km.getGiamTram());
                    }
                } else {
                    strGiaTri = "0 đ";
                }

                // Định dạng thời gian hiệu lực
                String strThoiGian = "";
                if (km.getNgayBatDau() != null && km.getNgayKetThuc() != null) {
                    strThoiGian = km.getNgayBatDau().toLocalDate().format(DF_DATE) + " → " + km.getNgayKetThuc().toLocalDate().format(DF_DATE);
                }

                boolean hieuLuc = km.kiemTraHieuLuc(LocalDateTime.now());

                modelKhuyenMai.addRow(new Object[]{
                        km,
                        strPhamVi,
                        strGiaTri,
                        strThoiGian,
                        hieuLuc ? "Đang áp dụng" : "Ngừng áp dụng",
                        km
                });
            }
        } catch (Exception ex) {
            System.err.println("[LỖI TẢI KHUYẾN MÃI]: " + ex.getMessage());
        }
    }

    // 4. PHÂN QUYỀN
    private boolean laQuanLy() {
        if (authService != null) {
            return authService.coVaiTro(VaiTro.QUAN_LY);
        }
        return vaiTroHienTai == VaiTro.QUAN_LY;
    }

    private void apDungPhanQuyen() {
        boolean quanLy = laQuanLy();
        if (btnTaoKhuyenMai != null) btnTaoKhuyenMai.setVisible(quanLy);

        if (tblKhuyenMai != null && !quanLy) {
            int colThaoTac = -1;
            for (int i = 0; i < tblKhuyenMai.getColumnCount(); i++) {
                if ("THAO TÁC".equalsIgnoreCase(tblKhuyenMai.getColumnName(i))) {
                    colThaoTac = i;
                    break;
                }
            }
            if (colThaoTac != -1) {
                tblKhuyenMai.removeColumn(tblKhuyenMai.getColumnModel().getColumn(colThaoTac));
            }
        }
    }

    // 5. ĐIỀU HƯỚNG DIALOG
    private void moDialogTaoKhuyenMai() {
        if (!laQuanLy()) return;
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        TaoKhuyenMaiDialog dlg = new TaoKhuyenMaiDialog(parent, khuyenMaiService);
        dlg.setVisible(true);
        if (dlg.isDaLuuThanhCong()) {
            taiDuLieuKhuyenMai();
        }
    }

    private void moDialogCapNhatKhuyenMai(KhuyenMai km) {
        if (!laQuanLy()) return;
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        CapNhatKhuyenMaiDialog dlg = new CapNhatKhuyenMaiDialog(parent, khuyenMaiService, km);
        dlg.setVisible(true);
        if (dlg.isDaCapNhatThanhCong()) {
            taiDuLieuKhuyenMai();
        }
    }

    // 6. RENDERERS & EDITORS CỐ ĐỊNH
    private static class ChuongTrinhRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblTen = new JLabel();
        private final JLabel lblMa = new JLabel();

        public ChuongTrinhRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(true);
            setBorder(new EmptyBorder(6, 10, 6, 10));

            lblTen.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTen.setForeground(COLOR_TEXT_DARK);

            lblMa.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblMa.setForeground(COLOR_TEXT_MUTED);

            add(lblTen);
            add(Box.createVerticalStrut(2));
            add(lblMa);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? new Color(248, 250, 252) : Color.WHITE);
            if (value instanceof KhuyenMai km) {
                lblTen.setText(km.getTenChuongTrinh());
                lblMa.setText(km.getMaKhuyenMai());
            } else if (value != null) {
                lblTen.setText(value.toString());
                lblMa.setText("");
            }
            return this;
        }
    }

    private static class StatusBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblStatus = new JLabel();
        private final RoundedPannel badge = new RoundedPannel(COLOR_ACTIVE_BG, 12);

        public StatusBadgeRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10));
            setOpaque(true);

            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblStatus.setForeground(COLOR_ACTIVE_TEXT);

            badge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
            badge.add(lblStatus);
            add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? new Color(248, 250, 252) : Color.WHITE);
            String text = (value != null) ? value.toString() : "Đang áp dụng";
            lblStatus.setText(text);
            if ("Đang áp dụng".equalsIgnoreCase(text)) {
                badge.setBackground(COLOR_ACTIVE_BG);
                lblStatus.setForeground(COLOR_ACTIVE_TEXT);
            } else {
                badge.setBackground(new Color(241, 245, 249));
                lblStatus.setForeground(COLOR_TEXT_MUTED);
            }
            return this;
        }
    }

    private static class ActionButtonsRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnSua = new JButton("Sửa");
        private final JButton btnTat = new JButton("Bật/Tắt");

        public ActionButtonsRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 10));
            setOpaque(true);

            formatButton(btnSua, new Color(239, 246, 255), new Color(29, 78, 216), new Color(191, 219, 254));
            formatButton(btnTat, new Color(254, 242, 242), new Color(220, 38, 38), new Color(254, 202, 202));

            add(btnSua);
            add(btnTat);
        }

        private void formatButton(JButton btn, Color bg, Color fg, Color border) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBackground(bg);
            btn.setForeground(fg);
            btn.setContentAreaFilled(true);
            btn.setOpaque(true);
            btn.setBorderPainted(true);
            btn.setBorder(BorderFactory.createLineBorder(border, 1));
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(46, 28));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? new Color(248, 250, 252) : Color.WHITE);
            return this;
        }
    }

    private class ActionButtonsEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        private final JButton btnSua = new JButton("Sửa");
        private final JButton btnBatTat = new JButton("Bật/Tắt");
        private KhuyenMai currentKM;

        public ActionButtonsEditor(JCheckBox checkBox) {
            super(checkBox);
            panel.setOpaque(true);

            formatButton(btnSua, new Color(239, 246, 255), new Color(29, 78, 216), new Color(191, 219, 254));
            formatButton(btnBatTat, new Color(254, 242, 242), new Color(220, 38, 38), new Color(254, 202, 202));

            btnSua.addActionListener(e -> {
                fireEditingStopped();
                if (currentKM != null) {
                    moDialogCapNhatKhuyenMai(currentKM);
                }
            });

            btnBatTat.addActionListener(e -> {
                fireEditingStopped();
                if (currentKM != null) {
                    int c = JOptionPane.showConfirmDialog(KhuyenMaiPanel.this,
                            "Bạn có muốn chuyển đổi trạng thái (Bật/Tắt) khuyến mãi: " + currentKM.getTenChuongTrinh() + "?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (c == JOptionPane.YES_OPTION) {
                        try {
                            khuyenMaiService.batTatTrangThai(currentKM.getMaKhuyenMai(), !currentKM.isTrangThai());
                            taiDuLieuKhuyenMai();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(KhuyenMaiPanel.this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            panel.add(btnSua);
            panel.add(btnBatTat);
        }

        private void formatButton(JButton btn, Color bg, Color fg, Color border) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBackground(bg);
            btn.setForeground(fg);
            btn.setContentAreaFilled(true);
            btn.setOpaque(true);
            btn.setBorderPainted(true);
            btn.setBorder(BorderFactory.createLineBorder(border, 1));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(46, 28));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof KhuyenMai km) {
                this.currentKM = km;
            }
            panel.setBackground(isSelected ? new Color(248, 250, 252) : Color.WHITE);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentKM;
        }
    }

}