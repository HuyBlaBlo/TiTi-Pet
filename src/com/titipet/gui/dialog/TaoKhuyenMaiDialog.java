package com.titipet.gui.dialog;

import com.titipet.bus.KhuyenMaiService;
import com.titipet.entity.KhuyenMai;
import com.titipet.entity.enums.PhamVi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TaoKhuyenMaiDialog extends JDialog {

    private final KhuyenMaiService khuyenMaiService;
    private boolean daLuuThanhCong = false;

    // CÁC THÀNH PHẦN NHẬP LIỆU
    private JTextField txtTenChuongTrinh;
    private JComboBox<String> cboDanhMucApDung;
    private JComboBox<String> cboKieuGiam;
    private JTextField txtGiaTriGiam;
    private JComboBox<String> cboTrangThai;
    private JTextField txtNgayBatDau;
    private JTextField txtNgayKetThuc;

    private JButton btnLuuChuongTrinh;

    // BẢNG MÀU CỐ ĐỊNH
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_BORDER = new Color(203, 213, 225);
    private static final Color COLOR_BG = Color.WHITE;

    private static final DateTimeFormatter DF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TaoKhuyenMaiDialog(Frame parent, KhuyenMaiService khuyenMaiService) {
        super(parent, "Tạo khuyến mãi", true);
        this.khuyenMaiService = (khuyenMaiService != null) ? khuyenMaiService : new KhuyenMaiService();

        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(560, 490);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        // 1. HEADER (TIÊU ĐỀ + NÚT ĐÓNG ✕)
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_BG);
        pnlHeader.setBorder(new EmptyBorder(18, 24, 10, 24));

        JLabel lblTitle = new JLabel("Tạo khuyến mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(COLOR_TEXT_DARK);

        JButton btnClose = new JButton("X");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btnClose.setForeground(COLOR_TEXT_MUTED);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnClose, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. NỘI DUNG FORM
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(COLOR_BG);
        pnlBody.setBorder(new EmptyBorder(10, 24, 16, 24));

        // Hàng 1: Tên chương trình *
        txtTenChuongTrinh = createTextField("");
        pnlBody.add(createFieldGroup("Tên chương trình *", txtTenChuongTrinh));
        pnlBody.add(Box.createVerticalStrut(12));

        // Hàng 2: Danh mục áp dụng | Kiểu giảm
        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false);

        cboDanhMucApDung = new JComboBox<>(new String[]{"Tất cả", "Phụ kiện", "Thức ăn", "Dịch vụ"});
        cboDanhMucApDung.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboDanhMucApDung.setBackground(Color.WHITE);
        cboDanhMucApDung.setPreferredSize(new Dimension(0, 38));

        cboKieuGiam = new JComboBox<>(new String[]{"Phần trăm (%)", "Số tiền cố định (đ)"});
        cboKieuGiam.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboKieuGiam.setBackground(Color.WHITE);
        cboKieuGiam.setPreferredSize(new Dimension(0, 38));

        row2.add(createFieldGroup("Danh mục áp dụng", cboDanhMucApDung));
        row2.add(createFieldGroup("Kiểu giảm", cboKieuGiam));
        pnlBody.add(row2);
        pnlBody.add(Box.createVerticalStrut(12));

        // Hàng 3: Giá trị giảm * | Trạng thái
        JPanel row3 = new JPanel(new GridLayout(1, 2, 16, 0));
        row3.setOpaque(false);

        txtGiaTriGiam = createTextField("10");

        cboTrangThai = new JComboBox<>(new String[]{"Đang bật", "Tắt"});
        cboTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboTrangThai.setBackground(Color.WHITE);
        cboTrangThai.setPreferredSize(new Dimension(0, 38));

        row3.add(createFieldGroup("Giá trị giảm *", txtGiaTriGiam));
        row3.add(createFieldGroup("Trạng thái", cboTrangThai));
        pnlBody.add(row3);
        pnlBody.add(Box.createVerticalStrut(12));

        // Hàng 4: Ngày bắt đầu * | Ngày kết thúc *
        JPanel row4 = new JPanel(new GridLayout(1, 2, 16, 0));
        row4.setOpaque(false);

        txtNgayBatDau = createTextField(LocalDate.now().format(DF_DATE));
        txtNgayKetThuc = createTextField(LocalDate.now().plusMonths(1).format(DF_DATE));

        row4.add(createFieldGroupWithIcon("Ngày bắt đầu *", txtNgayBatDau, "📅"));
        row4.add(createFieldGroupWithIcon("Ngày kết thúc *", txtNgayKetThuc, "📅"));
        pnlBody.add(row4);

        add(pnlBody, BorderLayout.CENTER);

        // 3. NÚT LƯU CHƯƠNG TRÌNH
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(COLOR_BG);
        pnlFooter.setBorder(new EmptyBorder(4, 24, 22, 24));

        btnLuuChuongTrinh = new JButton("Lưu chương trình") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_PRIMARY_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLuuChuongTrinh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLuuChuongTrinh.setForeground(Color.WHITE);
        btnLuuChuongTrinh.setPreferredSize(new Dimension(0, 42));
        btnLuuChuongTrinh.setContentAreaFilled(false);
        btnLuuChuongTrinh.setBorderPainted(false);
        btnLuuChuongTrinh.setFocusPainted(false);
        btnLuuChuongTrinh.setOpaque(false);
        btnLuuChuongTrinh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLuuChuongTrinh.addActionListener(e -> xuLyLuuKhuyenMai());

        pnlFooter.add(btnLuuChuongTrinh, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel createFieldGroup(String labelText, JComponent inputComponent) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        inputComponent.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(inputComponent);
        return panel;
    }

    private JPanel createFieldGroupWithIcon(String labelText, JTextField textField, String iconSymbol) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel inputWrap = new JPanel(new BorderLayout());
        inputWrap.setBackground(Color.WHITE);
        inputWrap.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        inputWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        inputWrap.setPreferredSize(new Dimension(0, 38));

        textField.setBorder(new EmptyBorder(0, 10, 0, 10));
        inputWrap.add(textField, BorderLayout.CENTER);

        JLabel lblIcon = new JLabel(iconSymbol);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblIcon.setForeground(COLOR_TEXT_MUTED);
        lblIcon.setBorder(new EmptyBorder(0, 0, 0, 10));
        inputWrap.add(lblIcon, BorderLayout.EAST);

        inputWrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(inputWrap);
        return panel;
    }

    private JTextField createTextField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(COLOR_TEXT_DARK);
        tf.setPreferredSize(new Dimension(0, 38));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setHorizontalAlignment(SwingConstants.LEFT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(0, 10, 0, 10)
        ));
        return tf;
    }

    private void xuLyLuuKhuyenMai() {
        String ten = txtTenChuongTrinh.getText().trim();
        String strGiaTri = txtGiaTriGiam.getText().trim();
        String strNgayBD = txtNgayBatDau.getText().trim();
        String strNgayKT = txtNgayKetThuc.getText().trim();

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên chương trình!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtTenChuongTrinh.requestFocus();
            return;
        }

        BigDecimal giaTri;
        try {
            giaTri = new BigDecimal(strGiaTri.replace(",", "").replace(".", ""));
            if (giaTri.compareTo(BigDecimal.ZERO) <= 0) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giá trị giảm không hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtGiaTriGiam.requestFocus();
            return;
        }

        LocalDate dBD, dKT;
        try {
            dBD = LocalDate.parse(strNgayBD, DF_DATE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu sai định dạng (dd/MM/yyyy)!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtNgayBatDau.requestFocus();
            return;
        }

        try {
            dKT = LocalDate.parse(strNgayKT, DF_DATE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc sai định dạng (dd/MM/yyyy)!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtNgayKetThuc.requestFocus();
            return;
        }

        if (dKT.isBefore(dBD)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc không được trước ngày bắt đầu!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtNgayKetThuc.requestFocus();
            return;
        }

        // Chuyển sang LocalDateTime bắt đầu từ 00:00:00 và kết thúc tại 23:59:59
        LocalDateTime ngayBD = dBD.atStartOfDay();
        LocalDateTime ngayKT = dKT.atTime(LocalTime.MAX);

        // Ánh xạ lựa chọn với enum PhamVi
        PhamVi phamVi;
        int idxDanhMuc = cboDanhMucApDung.getSelectedIndex();
        switch (idxDanhMuc) {
            case 1 -> phamVi = PhamVi.PHU_KIEN;
            case 2 -> phamVi = PhamVi.THUC_AN;
            case 3 -> phamVi = PhamVi.DICH_VU;
            default -> phamVi = PhamVi.TOAN_DON;
        }

        KhuyenMai km = new KhuyenMai();
        km.setTenChuongTrinh(ten);
        km.setPhamVi(phamVi);
        km.setGiamTram(giaTri);
        km.setNgayBatDau(ngayBD);
        km.setNgayKetThuc(ngayKT);
        km.setTrangThai(cboTrangThai.getSelectedIndex() == 0);

        try {
            boolean thanhCong = khuyenMaiService.themKhuyenMai(km);
            if (thanhCong) {
                daLuuThanhCong = true;
                JOptionPane.showMessageDialog(this, "Tạo chương trình khuyến mãi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tạo khuyến mãi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDaLuuThanhCong() {
        return daLuuThanhCong;
    }

}