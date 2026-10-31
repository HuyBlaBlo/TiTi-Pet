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

public class CapNhatThuCungDialog extends JDialog{

	// --- CÁC DỊCH VỤ NGHIỆP VỤ (SERVICES) VÀ ĐỐI TƯỢNG DỮ LIỆU HIỆN HÀNH ---
	private final ThuCungService thuCungService;
    private final KhachHangService khachHangService;
    private final ThuCung thuCungHienTai; // Thực thể lưu vết dữ liệu gốc cần cập nhật

    // --- CÁC THÀNH PHẦN NHẬP LIỆU GIAO DIỆN (UI COMPONENTS) ---
    private JComboBox<KhachHang> cbChuNuoi;            // Chọn chủ nuôi từ danh sách KH
    private JTextField txtTenThuCung;                   // Nhập tên thú cưng
    private JComboBox<LoaiThuCungApDung> cbLoai;        // Chọn loài (Chó / Mèo) theo Enum
    private JTextField txtCanNang;                      // Nhập cân nặng (đơn vị kg)
    private JTextArea txtGhiChu;                        // Nhập các ghi chú đặc biệt / tình trạng

    // Bảng màu Flat Design
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_BORDER = new Color(226, 232, 240);

    /**
     * Constructor khởi tạo Dialog cập nhật thú cưng
     * 
     * @param parent        Cửa sổ cha (Frame) gọi dialog
     * @param tcService     Service xử lý nghiệp vụ thú cưng
     * @param khService     Service xử lý nghiệp vụ khách hàng
     * @param thuCung       Đối tượng thú cưng chứa dữ liệu cũ cần sửa
     */
    public CapNhatThuCungDialog(Frame parent, ThuCungService tcService, KhachHangService khService, ThuCung thuCung) {
        super(parent, "Cập nhật hồ sơ thú cưng", true);
        this.thuCungService = tcService;
        this.khachHangService = khService;
        this.thuCungHienTai = thuCung;

        initComponents(); 		// Xây dựng bố cục giao diện
        loadDuLieuLenForm();	// Đổ dữ liệu cũ của thú cưng lên các ô nhập liệu
    }

    /**
     * Xây dựng toàn bộ bố cục giao diện người dùng
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        RoundedPannel mainCard = new RoundedPannel(Color.WHITE, 16);
        mainCard.setLayout(new BorderLayout(0, 16));
        mainCard.setBorder(new EmptyBorder(22, 26, 22, 26));

        // --- 1. TIÊU ĐỀ & NÚT ĐÓNG ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Cập nhật hồ sơ thú cưng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_TEXT_DARK);

        // Nút 'X' đóng nhanh cửa sổ
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

        // --- 2. FORM NHẬP LIỆU ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; // Giãn đều theo cả chiều ngang và dọc
        gbc.weightx = 0.5;                  // Phân chia chiều rộng 50-50 giữa 2 cột

        // HÀNG 1 - TRÁI: CHỦ NUÔI
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 12, 8);

        JPanel chuNuoiGroup = new JPanel(new BorderLayout(6, 4));
        chuNuoiGroup.setOpaque(false);

        JLabel lblChuNuoi = new JLabel("Chủ nuôi *");
        lblChuNuoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChuNuoi.setForeground(COLOR_TEXT_DARK);

        // Cấu hình JComboBox chứa danh sách đối tượng KhachHang
        cbChuNuoi = new JComboBox<>();
        cbChuNuoi.setPreferredSize(new Dimension(0, 38));
        cbChuNuoi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbChuNuoi.setBackground(Color.WHITE);
        // Tùy biến bộ hiển thị: hiển thị dạng "Họ tên - Số điện thoại" thay vì mã hash mặc định
        cbChuNuoi.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang kh) {
                    setText(kh.getHoTen() + " - " + kh.getSoDienThoai());
                }
                return this;
            }
        });

        chuNuoiGroup.add(lblChuNuoi, BorderLayout.NORTH);
        chuNuoiGroup.add(cbChuNuoi, BorderLayout.CENTER);
        formPanel.add(chuNuoiGroup, gbc);

        // HÀNG 1 - PHẢI: TÊN THÚ CƯNG
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 8, 12, 0);
        formPanel.add(createFieldGroup("Tên thú cưng *", txtTenThuCung = createInputField()), gbc);

        // HÀNG 2 - TRÁI: LOÀI (ENUM)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 12, 8);
        // Nạp trực tiếp các giá trị từ Enum LoaiThuCungApDung để đảm bảo Type-safety
        cbLoai = new JComboBox<>(LoaiThuCungApDung.values());
        cbLoai.setPreferredSize(new Dimension(0, 38));
        cbLoai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbLoai.setBackground(Color.WHITE);
        formPanel.add(createFieldGroup("Loài *", cbLoai), gbc);

        // HÀNG 2 - PHẢI: CÂN NẶNG
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 8, 12, 0);
        txtCanNang = createInputField();
        formPanel.add(createFieldGroup("Cân nặng (kg) *", txtCanNang), gbc);

        // HÀNG 3: GHI CHÚ
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Gộp 2 cột thành 1 vùng nhập rộng
        gbc.insets = new Insets(0, 0, 16, 0);

        JPanel ghiChuPanel = new JPanel();
        ghiChuPanel.setLayout(new BoxLayout(ghiChuPanel, BoxLayout.Y_AXIS));
        ghiChuPanel.setOpaque(false);

        JLabel lblGhiChu = new JLabel("Ghi chú cần lưu ý");
        lblGhiChu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGhiChu.setForeground(COLOR_TEXT_DARK);

        txtGhiChu = new JTextArea(3, 0);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtGhiChu.setLineWrap(true);       // Tự động xuống dòng khi chạm mép phải
        txtGhiChu.setWrapStyleWord(true);   // Ngắt dòng theo từ (không cắt ngang chữ)
        txtGhiChu.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

        ghiChuPanel.add(lblGhiChu);
        ghiChuPanel.add(Box.createVerticalStrut(6));
        ghiChuPanel.add(scrollGhiChu);
        formPanel.add(ghiChuPanel, gbc);

        // HÀNG 4: NÚT LƯU THAY ĐỔI
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
        btnSave.addActionListener(e -> xuLyCapNhatThuCung());
        formPanel.add(btnSave, gbc);

        mainCard.add(formPanel, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);

        // Thiết lập kích thước và vị trí mở hộp thoại ở giữa màn hình
        pack();
        setSize(600, 390);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    /**
     * Nạp dữ liệu khách hàng vào ComboBox và đổ dữ liệu thú cưng hiện tại lên form
     */
    private void loadDuLieuLenForm() {
        cbChuNuoi.removeAllItems();
        try {
        	// Lấy danh sách toàn bộ khách hàng từ CSDL thông qua tầng BUS
            List<KhachHang> ds = khachHangService.layTatCaKhachHang();
            for (KhachHang kh : ds) {
                cbChuNuoi.addItem(kh);
                if (thuCungHienTai.getChuNuoi() != null && kh.getMaKH().equals(thuCungHienTai.getChuNuoi().getMaKH())) {
                    cbChuNuoi.setSelectedItem(kh);
                }
            }
        } catch (Exception ignored) {}
        
        // Đổ thông tin chi tiết của thú cưng lên các trường nhập liệu
        if (thuCungHienTai != null) {
            txtTenThuCung.setText(thuCungHienTai.getTenThuCung());
            // Tự động chọn đúng loài thú cưng trên JComboBox
            cbLoai.setSelectedItem(thuCungHienTai.getLoaiThuCung());
            txtCanNang.setText(String.valueOf(thuCungHienTai.getCanNang()));
            txtGhiChu.setText(thuCungHienTai.getGhiChu());
        }
    }

    /**
     * Hàm trợ giúp: Tạo JTextField với định dạng viền bo và padding đồng bộ
     */
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

    /**
     * Hàm trợ giúp: Gom nhãn (Label) và ô nhập liệu thành một cụm dọc chuẩn UX
     */
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

    /**
     * Xử lý nghiệp vụ kiểm tra tính hợp lệ và gọi Service cập nhật hồ sơ thú cưng
     */
    private void xuLyCapNhatThuCung() {
        try {
        	// 1. Kiểm tra chủ nuôi
            KhachHang chuNuoi = (KhachHang) cbChuNuoi.getSelectedItem();
            if (chuNuoi == null) {
                throw new IllegalArgumentException("Vui lòng chọn chủ nuôi!");
            }

            // 2. Kiểm tra tên thú cưng
            String ten = txtTenThuCung.getText().trim();
            if (ten.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập tên thú cưng!");
            }

            // 3. Kiểm tra định dạng số của cân nặng
            double canNang;
            try {
                canNang = Double.parseDouble(txtCanNang.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cân nặng phải là dạng số hợp lệ (ví dụ: 3.5)!");
            }

            // 4. Lấy loài được chọn và ghi chú
            LoaiThuCungApDung loaiChon = (LoaiThuCungApDung) cbLoai.getSelectedItem();
            String ghiChu = txtGhiChu.getText().trim();

            // 5. Cập nhật dữ liệu mới vào đối tượng hiện hành
            thuCungHienTai.setTenThuCung(ten);
            thuCungHienTai.setLoaiThuCung(loaiChon);
            thuCungHienTai.setCanNang(canNang);
            thuCungHienTai.setGhiChu(ghiChu);
            thuCungHienTai.setChuNuoi(chuNuoi);

            // 6. Đẩy dữ liệu xuống tầng nghiệp vụ BUS để ghi vào DB
            thuCungService.capNhatThuCung(thuCungHienTai);
            JOptionPane.showMessageDialog(this, "Cập nhật hồ sơ thú cưng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Đóng hộp thoại sau khi cập nhật thành công
        } catch (Exception ex) {
            // Hiển thị hộp thoại cảnh báo nếu có lỗi nhập liệu hoặc lỗi CSDL
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }
}
