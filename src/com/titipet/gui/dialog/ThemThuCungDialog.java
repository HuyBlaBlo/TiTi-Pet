package com.titipet.gui.dialog;

import com.titipet.bus.KhachHangService;
import com.titipet.bus.ThuCungService;
import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.KhachHang;
import com.titipet.entity.ThuCung;
import com.titipet.entity.enums.LoaiThuCungApDung;
import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ThemThuCungDialog extends JDialog{
	
	private final ThuCungService thuCungService;
    private final KhachHangService khachHangService;

    // Các thành phần nhập liệu trên giao diện
    private JComboBox<KhachHang> cbChuNuoi;
    private JTextField txtTenThuCung;
    private JComboBox<LoaiThuCungApDung> cbLoai;
    private JTextField txtCanNang;
    private JTextArea txtGhiChu;

    // Bảng màu Flat Design
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);

    public ThemThuCungDialog(Frame parent, ThuCungService tcService, KhachHangService khService) {
        super(parent, "Thêm hồ sơ thú cưng", true);
        this.thuCungService = tcService;
        this.khachHangService = khService;

        initComponents();
        loadDanhSachKhachHang();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        RoundedPannel mainCard = new RoundedPannel(Color.WHITE, 16);
        mainCard.setLayout(new BorderLayout(0, 16));
        mainCard.setBorder(new EmptyBorder(22, 26, 22, 26));

        // --- 1. TIÊU ĐỀ & NÚT ĐÓNG ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Thêm hồ sơ thú cưng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_TEXT_DARK);

        JButton btnClose = new JButton("X");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnClose.setForeground(COLOR_TEXT_MUTED);
        btnClose.setBackground(new Color(241, 245, 249));
        btnClose.setPreferredSize(new Dimension(32, 32));
        btnClose.setBorder(null);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnClose, BorderLayout.EAST);
        mainCard.add(headerPanel, BorderLayout.NORTH);

        // --- 2. FORM NHẬP LIỆU (BỐ CỤC 2 CỘT CÂN ĐỐI) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;

        // HÀNG 1 - CỘT TRÁI: CHỦ NUÔI & NÚT THÊM KHÁCH MỚI
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 12, 8);

        JPanel chuNuoiGroup = new JPanel(new BorderLayout(6, 4));
        chuNuoiGroup.setOpaque(false);

        JPanel lblGroup = new JPanel(new BorderLayout());
        lblGroup.setOpaque(false);

        JLabel lblChuNuoi = new JLabel("Chủ nuôi *");
        lblChuNuoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChuNuoi.setForeground(COLOR_TEXT_DARK);

        JButton btnThemKhachNhanh = new JButton("+ Thêm khách mới");
        btnThemKhachNhanh.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnThemKhachNhanh.setForeground(COLOR_PRIMARY_BLUE);
        btnThemKhachNhanh.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        btnThemKhachNhanh.setContentAreaFilled(false);
        btnThemKhachNhanh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThemKhachNhanh.addActionListener(e -> {
            ThemKhachHangDialog dlg = new ThemKhachHangDialog((Frame) SwingUtilities.getWindowAncestor(this), khachHangService);
            dlg.setVisible(true);
            loadDanhSachKhachHang();
        });

        lblGroup.add(lblChuNuoi, BorderLayout.WEST);
        lblGroup.add(btnThemKhachNhanh, BorderLayout.EAST);

        cbChuNuoi = new JComboBox<>();
        cbChuNuoi.setPreferredSize(new Dimension(0, 38));
        cbChuNuoi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbChuNuoi.setBackground(Color.WHITE);
        cbChuNuoi.setRenderer(new DefaultListCellRenderer() {
        	@Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang kh) {
                    String ten = kh.getHoTen() != null ? kh.getHoTen() : "";
                    String sdt = kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "";
                    setText(ten + (sdt.isEmpty() ? "" : " - " + sdt));
                } else if (value == null) {
                    setText("-- Chọn chủ nuôi --");
                }
                return this;
            }
        });

        chuNuoiGroup.add(lblGroup, BorderLayout.NORTH);
        chuNuoiGroup.add(cbChuNuoi, BorderLayout.CENTER);
        formPanel.add(chuNuoiGroup, gbc);

        // HÀNG 1 - CỘT PHẢI: TÊN THÚ CƯNG
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 8, 12, 0);
        formPanel.add(createFieldGroup("Tên thú cưng *", txtTenThuCung = createInputField()), gbc);

        // HÀNG 2 - CỘT TRÁI: LOÀI (ENUM)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 12, 8);
        cbLoai = new JComboBox<>(LoaiThuCungApDung.values());
        cbLoai.setPreferredSize(new Dimension(0, 38));
        cbLoai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbLoai.setBackground(Color.WHITE);
        formPanel.add(createFieldGroup("Loài *", cbLoai), gbc);

        // HÀNG 2 - CỘT PHẢI: CÂN NẶNG
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 8, 12, 0);
        txtCanNang = createInputField();
        formPanel.add(createFieldGroup("Cân nặng (kg) *", txtCanNang), gbc);

        // HÀNG 3: GHI CHÚ (CHIẾM HẾT 2 CỘT)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 16, 0);

        JPanel ghiChuPanel = new JPanel();
        ghiChuPanel.setLayout(new BoxLayout(ghiChuPanel, BoxLayout.Y_AXIS));
        ghiChuPanel.setOpaque(false);

        JLabel lblGhiChu = new JLabel("Ghi chú cần lưu ý");
        lblGhiChu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGhiChu.setForeground(COLOR_TEXT_DARK);

        txtGhiChu = new JTextArea(3, 0);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

        ghiChuPanel.add(lblGhiChu);
        ghiChuPanel.add(Box.createVerticalStrut(6));
        ghiChuPanel.add(scrollGhiChu);
        formPanel.add(ghiChuPanel, gbc);

        // HÀNG 4: NÚT XÁC NHẬN LƯU HỒ SƠ
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);

        JButton btnSave = new JButton("Lưu hồ sơ thú cưng");
        btnSave.setPreferredSize(new Dimension(0, 42));
        btnSave.setBackground(COLOR_PRIMARY_BLUE);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(BorderFactory.createEmptyBorder());
        btnSave.addActionListener(e -> xuLyThemThuCung());
        formPanel.add(btnSave, gbc);

        mainCard.add(formPanel, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);

        pack();
        setSize(600, 390);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    private void loadDanhSachKhachHang() {
        cbChuNuoi.removeAllItems();
        try {
            List<KhachHang> ds = khachHangService.layTatCaKhachHang();
            if (ds == null || ds.isEmpty()) {
                System.out.println("[DEBUG] Không tìm thấy khách hàng nào trong CSDL!");
                return;
            }
            for (KhachHang kh : ds) {
                cbChuNuoi.addItem(kh);
            }
            if (cbChuNuoi.getItemCount() > 0) {
                cbChuNuoi.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            System.err.println("[LỖI LOAD KHÁCH HÀNG]: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private JTextField createInputField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(0, 12, 0, 12)
        ));
        return field;
    }

    private JPanel createFieldGroup(String labelText, JComponent comp) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT_DARK);

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(comp);
        return panel;
    }

    private void xuLyThemThuCung() {
        try {
            KhachHang chuNuoi = (KhachHang) cbChuNuoi.getSelectedItem();
            if (chuNuoi == null) {
                throw new IllegalArgumentException("Vui lòng chọn chủ nuôi!");
            }

            String ten = txtTenThuCung.getText().trim();
            if (ten.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập tên thú cưng!");
            }

            double canNang;
            try {
                canNang = Double.parseDouble(txtCanNang.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cân nặng phải là dạng số hợp lệ (ví dụ: 3.5)!");
            }

            LoaiThuCungApDung loaiChon = (LoaiThuCungApDung) cbLoai.getSelectedItem();
            String ghiChu = txtGhiChu.getText().trim();

            ThuCung tc = new ThuCung();
            tc.setTenThuCung(ten);
            tc.setLoaiThuCung(loaiChon);
            tc.setCanNang(canNang);
            tc.setGhiChu(ghiChu);
            tc.setChuNuoi(chuNuoi);

            thuCungService.themThuCung(tc);
            JOptionPane.showMessageDialog(this, "Thêm hồ sơ thú cưng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }
}

