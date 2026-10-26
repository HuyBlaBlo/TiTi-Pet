package com.titipet.gui.dialog;

import com.titipet.connectDB.ConnectDB;

import com.titipet.bus.KhachHangService;
import com.titipet.entity.KhachHang;
import com.titipet.entity.enums.HangTV;
import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ThemKhachHangDialog extends JDialog {
	
	private final KhachHangService khachHangService;
	
    private JTextField txtHoTen;
    private JTextField txtSoDienThoai;
    private JComboBox<HangTV> cbHangTV;
    private JTextField txtDiemHienCo;
    
    // Bảng màu thiết kế Flat Design đồng bộ hệ thống TiTiPet
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);     // Xanh dương TiTiPet
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);          // Chữ màu tối
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);      // Chữ màu phụ/mờ
    private static final Color COLOR_BORDER = new Color(226, 232, 240);          // Viền input
    private static final Color COLOR_INPUT_BG = Color.WHITE;                     // Nền ô nhập liệu
    private static final Color COLOR_READONLY_BG = new Color(248, 250, 252);     // Nền ô chỉ đọc
    private static final Color COLOR_INFO_BOX_BG = new Color(239, 246, 255);     // Nền banner xanh nhạt

    /**
     * Constructor khởi tạo popup
     * @param parent Cửa sổ cha (MainFrame hoặc Frame chứa)
     * @param service Tầng xử lý nghiệp vụ KhachHangService
     */
    public ThemKhachHangDialog(Frame parent, KhachHangService service) {
        super(parent, "Thêm khách hàng", true); // true = Modal dialog (chặn tương tác phía sau)
        this.khachHangService = service;
        initComponents();
    }
    
    /**
     * Khởi tạo và bố trí toàn bộ layout giao diện
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Khối Card chính bo góc màu trắng sử dụng trực tiếp class RoundedPannel
        RoundedPannel mainCard = new RoundedPannel(Color.WHITE, 16);
        mainCard.setLayout(new BorderLayout(0, 16));
        mainCard.setBorder(new EmptyBorder(22, 26, 22, 26)); // Padding viền xung quanh
       
        // 1. PHẦN HEADER (Tiêu đề "Thêm khách hàng" & Nút đóng X)       
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Thêm khách hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_TEXT_DARK);

        // Nút đóng X góc trên bên phải
        JButton btnClose = new JButton("X");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnClose.setForeground(COLOR_TEXT_MUTED);
        btnClose.setBackground(new Color(241, 245, 249));
        btnClose.setPreferredSize(new Dimension(32, 32));
        btnClose.setBorder(null);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose()); // Đóng form

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnClose, BorderLayout.EAST);
        mainCard.add(headerPanel, BorderLayout.NORTH);

        // 2. PHẦN THÂN FORM (Chia 2 cột cân bằng bằng GridBagLayout)       
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5; // Mỗi cột chiếm chính xác 50% độ rộng
        gbc.insets = new Insets(0, 0, 12, 0);

        // HÀNG 1: Họ và tên & Điện thoại 10 chữ số
        // Cột trái: Họ và tên
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 12, 8); // Cách cột phải 8px
        formPanel.add(createFieldGroup("Họ và tên *", txtHoTen = createInputField(false)), gbc);

        // Cột phải: Điện thoại 10 chữ số
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 8, 12, 0); // Cách cột trái 8px
        formPanel.add(createFieldGroup("Điện thoại 10 chữ số *", txtSoDienThoai = createInputField(false)), gbc);

        //HÀNG 2: Cấp độ thành viên & Điểm hiện có
        // Cột trái: Cấp độ thành viên + Chú thích mờ
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 12, 8);
        JPanel rankGroup = new JPanel();
        rankGroup.setLayout(new BoxLayout(rankGroup, BoxLayout.Y_AXIS));
        rankGroup.setOpaque(false);

        JLabel lblRankTitle = new JLabel("Cấp độ thành viên");
        lblRankTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRankTitle.setForeground(COLOR_TEXT_DARK);

     // Khởi tạo JComboBox trực tiếp với các giá trị của Enum HangTV
        cbHangTV = new JComboBox<>(HangTV.values());
        cbHangTV.setPreferredSize(new Dimension(0, 38));
        cbHangTV.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbHangTV.setBackground(COLOR_INPUT_BG);
        
     // Đổi cách hiển thị từ mã Enum sang Tiếng Việt
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

        // Mặc định chọn Thường
        cbHangTV.setSelectedItem("Thường");

        JLabel lblRankHint = new JLabel("Chỉ quản lý thay đổi cấp hạng");
        lblRankHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblRankHint.setForeground(COLOR_TEXT_MUTED);

        rankGroup.add(lblRankTitle);
        rankGroup.add(Box.createVerticalStrut(6));
        rankGroup.add(cbHangTV);
        rankGroup.add(Box.createVerticalStrut(4));
        rankGroup.add(lblRankHint);
        formPanel.add(rankGroup, gbc);

        // Cột phải: Điểm hiện có (Chỉ đọc: 0 điểm = 0 đ)
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 8, 12, 0);
        txtDiemHienCo = createInputField(true);
        txtDiemHienCo.setText("0 điểm = 0 đ");
        formPanel.add(createFieldGroup("Điểm hiện có", txtDiemHienCo), gbc);
      
        // 3. KHỐI BANNER THÔNG TIN (Hàng 3 - Sử dụng RoundedPannel)        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Chiếm toàn bộ chiều ngang (cả 2 cột)
        gbc.insets = new Insets(6, 0, 16, 0);

        RoundedPannel infoBox = new RoundedPannel(COLOR_INFO_BOX_BG, 10);
        infoBox.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 10));
        JLabel lblInfo = new JLabel("Tổng chi tiêu ghi nhận: 0 đ. Điểm chỉ tăng/giảm khi có hóa đơn hợp lệ, không sửa thủ công.");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(29, 78, 216));
        infoBox.add(lblInfo);
        formPanel.add(infoBox, gbc);
        
        // 4. NÚT LƯU HỒ SƠ KHÁCH HÀNG (Hàng 4)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Trải dài 100% bề ngang
        gbc.insets = new Insets(0, 0, 0, 0);

        JButton btnSave = new JButton("Lưu hồ sơ khách hàng");
        btnSave.setPreferredSize(new Dimension(0, 42));
        btnSave.setBackground(COLOR_PRIMARY_BLUE);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(BorderFactory.createEmptyBorder());
        btnSave.addActionListener(e -> xuLyLuuKhachHang());
        formPanel.add(btnSave, gbc);

        mainCard.add(formPanel, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);

        // Tự động căn chỉnh kích thước và căn giữa màn hình
        pack();
        setSize(640, 370);
        setLocationRelativeTo(getParent()); //căn chỉnh theo MainFrame
        setResizable(false);
    }
    
    /**
     * Tạo ô JTextField chuẩn Flat bo viền
     */
    private JTextField createInputField(boolean isReadOnly) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setEditable(!isReadOnly);
        field.setBackground(isReadOnly ? COLOR_READONLY_BG : COLOR_INPUT_BG);
        field.setForeground(COLOR_TEXT_DARK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(0, 12, 0, 12) // Padding chữ bên trong ô
        ));
        return field;
    }
    
    /**
     * Gom nhóm nhãn và ô nhập thành hàng dọc
     */
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
     * Xử lý lưu khách hàng khi nhấn nút
     */
    private void xuLyLuuKhachHang() {
        try {
            // Tự sinh mã khách hàng ngẫu nhiên theo chuẩn VARCHAR(20)
            String maTuSinh = "KH" + (System.currentTimeMillis() % 1000000);

            // Lấy enum HangTV đã chọn từ JComboBox
            HangTV hangDuocChon = (HangTV) cbHangTV.getSelectedItem();

            // Tạo đối tượng Entity KhachHang từ dữ liệu form
            KhachHang kh = new KhachHang(
                    maTuSinh,
                    txtHoTen.getText().trim(),
                    txtSoDienThoai.getText().trim(),
                    hangDuocChon,
                    0 // Điểm ban đầu mặc định là 0 điểm
            );

            // Gửi dữ liệu xuống tầng BUS kiểm tra validation và lưu CSDL
            khachHangService.themKhachHang(kh);

            // Hiển thị thông báo thành công
            JOptionPane.showMessageDialog(this, "Đã lưu hồ sơ khách hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            // Đóng popup
            dispose();

        } catch (Exception ex) {
            // Bắt ngoại lệ chứa thông báo tiếng Việt từ BUS và hiển thị hộp thoại cảnh báo
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo nghiệp vụ", JOptionPane.WARNING_MESSAGE);
        }
    }
    
}
