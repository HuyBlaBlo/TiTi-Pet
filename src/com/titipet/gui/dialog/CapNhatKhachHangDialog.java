package com.titipet.gui.dialog;

import com.titipet.bus.KhachHangService;
import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.KhachHang;
import com.titipet.entity.enums.HangTV;
import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class CapNhatKhachHangDialog extends JDialog{

	// Đối tượng tầng nghiệp vụ
    private final KhachHangService khachHangService;
    
    // Dữ liệu khách hàng đang chỉnh sửa
    private final String maKH;
    private KhachHang khachHangHienTai;
    private final BigDecimal tongChiTieu;

    // Các thành phần điều khiển trên giao diện
    private JTextField txtHoTen;
    private JTextField txtSoDienThoai;
    private JComboBox<HangTV> cbHangTV;
    private JTextField txtDiemHienCo;
    private JLabel lblInfo;

    // Bảng màu thiết kế Flat Design đồng bộ hệ thống TiTiPet
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_INPUT_BG = Color.WHITE;
    private static final Color COLOR_READONLY_BG = new Color(248, 250, 252);
    private static final Color COLOR_INFO_BOX_BG = new Color(239, 246, 255);

    // Format tiền tệ VNĐ
    private final DecimalFormat dfTien = new DecimalFormat("#,##0 đ");

    /**
     * Constructor mở hộp thoại cập nhật
     * @param parent Cửa sổ cha
     * @param service Tầng xử lý nghiệp vụ KhachHangService
     * @param maKH Mã khách hàng cần chỉnh sửa
     * @param tongChiTieu Tổng chi tiêu lấy từ bảng truyền sang
     */
    public CapNhatKhachHangDialog(Frame parent, KhachHangService service, String maKH, BigDecimal tongChiTieu) {
        super(parent, "Cập nhật khách hàng", true); // Modal Dialog chặn thao tác bên dưới
        this.khachHangService = service;
        this.maKH = maKH;
        this.tongChiTieu = (tongChiTieu != null) ? tongChiTieu : BigDecimal.ZERO;

        initComponents();
        loadThongTinKhachHang();
    }

    /**
     * Bố trí toàn bộ giao diện theo chuẩn thiết kế
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Khối Card bao quanh sử dụng RoundedPannel có sẵn
        RoundedPannel mainCard = new RoundedPannel(Color.WHITE, 16);
        mainCard.setLayout(new BorderLayout(0, 16));
        mainCard.setBorder(new EmptyBorder(22, 26, 22, 26));

        // 1. TIÊU ĐỀ POPUP VÀ NÚT ĐÓNG
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Cập nhật khách hàng");
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

        // 2. PHẦN THÂN NHẬP LIỆU (GRIDBAGLAYOUT 2 CỘT TỰ CO GIÃN)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 12, 0);

        // HÀNG 1: Họ và tên & Điện thoại 10 chữ số
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 12, 8);
        formPanel.add(createFieldGroup("Họ và tên *", txtHoTen = createInputField(false)), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 8, 12, 0);
        formPanel.add(createFieldGroup("Điện thoại 10 chữ số *", txtSoDienThoai = createInputField(false)), gbc);

        // HÀNG 2: Cấp độ thành viên & Điểm hiện có
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 12, 8);
        JPanel rankGroup = new JPanel();
        rankGroup.setLayout(new BoxLayout(rankGroup, BoxLayout.Y_AXIS));
        rankGroup.setOpaque(false);

        JLabel lblRankTitle = new JLabel("Cấp độ thành viên");
        lblRankTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRankTitle.setForeground(COLOR_TEXT_DARK);

        cbHangTV = new JComboBox<>(HangTV.values());
        cbHangTV.setPreferredSize(new Dimension(0, 38));
        cbHangTV.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbHangTV.setBackground(COLOR_INPUT_BG);
        // Hiển thị nhãn tiếng Việt cho enum
        cbHangTV.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof HangTV hang) {
                    switch (hang) {
                        case HANG_1 -> setText("Bạc");
                        case HANG_2 -> setText("Vàng");
                        case HANG_3 -> setText("Kim cương");
                        default -> setText("Thường");
                    }
                }
                return this;
            }
        });

        JLabel lblRankHint = new JLabel("Chỉ quản lý thay đổi cấp hạng");
        lblRankHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblRankHint.setForeground(COLOR_TEXT_MUTED);

        rankGroup.add(lblRankTitle);
        rankGroup.add(Box.createVerticalStrut(6));
        rankGroup.add(cbHangTV);
        rankGroup.add(Box.createVerticalStrut(4));
        rankGroup.add(lblRankHint);
        formPanel.add(rankGroup, gbc);

        // Ô hiển thị Điểm hiện có (chỉ đọc)
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 8, 12, 0);
        txtDiemHienCo = createInputField(true);
        formPanel.add(createFieldGroup("Điểm hiện có", txtDiemHienCo), gbc);

        // HÀNG 3: BANNER THÔNG TIN CHI TIÊU VÀ ĐIỂM
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(6, 0, 16, 0);

        RoundedPannel infoBox = new RoundedPannel(COLOR_INFO_BOX_BG, 10);
        infoBox.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 10)); // Căn giữa nội dung
        lblInfo = new JLabel("Tổng chi tiêu ghi nhận: 0 đ. Điểm chỉ tăng/giảm khi có hóa đơn hợp lệ, không sửa thủ công.");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(29, 78, 216));
        infoBox.add(lblInfo);
        formPanel.add(infoBox, gbc);

        // HÀNG 4: NÚT LƯU HỒ SƠ KHÁCH HÀNG
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);

        JButton btnSave = new JButton("Lưu hồ sơ khách hàng");
        btnSave.setPreferredSize(new Dimension(0, 42));
        btnSave.setBackground(COLOR_PRIMARY_BLUE);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(BorderFactory.createEmptyBorder());
        btnSave.addActionListener(e -> xuLyCapNhatKhachHang());
        formPanel.add(btnSave, gbc);

        mainCard.add(formPanel, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);

        // Kích thước chuẩn rộng rãi, căn giữa màn hình
        pack();
        setSize(640, 370);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    /**
     * Nạp dữ liệu của khách hàng từ DB lên các ô nhập liệu
     */
    private void loadThongTinKhachHang() {
        try {
            khachHangHienTai = khachHangService.timKhachHangTheoMaHoacSDT(maKH);
            if (khachHangHienTai != null) {
                txtHoTen.setText(khachHangHienTai.getHoTen());
                txtSoDienThoai.setText(khachHangHienTai.getSoDienThoai());
                cbHangTV.setSelectedItem(khachHangHienTai.getHangTV());

                // Tính tiền quy đổi theo công thức 1 điểm = 1.000đ từ BUS
                int diem = khachHangHienTai.getDiemTichLuy();
                BigDecimal tienQuyDoi = khachHangService.quyDoiDiemThanhTien(diem);
                txtDiemHienCo.setText(diem + " điểm = " + dfTien.format(tienQuyDoi));

                // Cập nhật banner ghi nhận chi tiêu thực tế
                lblInfo.setText("Tổng chi tiêu ghi nhận: " + dfTien.format(tongChiTieu) 
                        + ". Điểm chỉ tăng/giảm khi có hóa đơn hợp lệ, không sửa thủ công.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể tải thông tin khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField createInputField(boolean isReadOnly) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setEditable(!isReadOnly);
        field.setBackground(isReadOnly ? COLOR_READONLY_BG : COLOR_INPUT_BG);
        field.setForeground(COLOR_TEXT_DARK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(0, 12, 0, 12)
        ));
        return field;
    }

    private JPanel createFieldGroup(String labelText, JComponent inputComponent) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT_DARK);

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(inputComponent);
        return panel;
    }

    /**
     * Gửi yêu cầu cập nhật xuống tầng BUS[cite: 1, 2]
     */
    private void xuLyCapNhatKhachHang() {
        try {
            if (khachHangHienTai == null) {
                throw new Exception("Không tìm thấy hồ sơ khách hàng để cập nhật!");
            }

            // Gán thông tin mới
            khachHangHienTai.setHoTen(txtHoTen.getText().trim());
            khachHangHienTai.setSoDienThoai(txtSoDienThoai.getText().trim());
            khachHangHienTai.setHangTV((HangTV) cbHangTV.getSelectedItem());

            // Gọi BUS thực hiện cập nhật CSDL
            khachHangService.capNhatKhachHang(khachHangHienTai);

            JOptionPane.showMessageDialog(this, "Cập nhật hồ sơ khách hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Đóng popup

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        }
    }

}
