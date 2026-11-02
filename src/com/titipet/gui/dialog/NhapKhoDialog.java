package com.titipet.gui.dialog;

import com.titipet.bus.KhoService;
import com.titipet.entity.SanPham;
import com.titipet.entity.ThucAn;
import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Các chức năng chính:
 * 1. Chọn sản phẩm có sẵn trong CSDL từ dropdown.
 * 2. Nhập số lượng nhập thêm.
 * 3. Nhập đơn giá nhập (VNĐ) để cập nhật giá vốn mới nhất.
 * 4. Nhập HSD trên bao bì (chỉ kích hoạt với Thức ăn, tự động khóa với Phụ kiện).
 * 5. Ghi chú nhập hàng.
 */
public class NhapKhoDialog extends JDialog {

    // --- TẦNG NGHIỆP VỤ ---
    private final KhoService khoHangService;

    // --- CÁC THÀNH PHẦN GIAO DIỆN FORM ---
    private JComboBox<SanPham> cbSanPham;
    private JTextField txtSoLuongNhap;
    private JTextField txtGiaNhap;
    private JTextField txtHanSuDung;
    private JTextArea txtGhiChu;
    private JButton btnXacNhan;
    private JButton btnDong;

    // --- BẢNG MÀU CHUẨN FLAT DESIGN TITIPET ---
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242); // Xanh dương thương hiệu
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);      // Chữ đen đậm tiêu đề
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // Chữ phụ mờ
    private static final Color COLOR_BORDER = new Color(226, 232, 240);      // Viền khung input
    private static final Color COLOR_TIP_BG = new Color(239, 246, 255);      // Nền hộp thông tin gợi ý
    private static final Color COLOR_TIP_TEXT = new Color(29, 78, 216);      // Màu chữ hộp gợi ý
    private static final Color COLOR_DISABLED_BG = new Color(241, 245, 249); // Nền xám khi input bị khóa

    // Định dạng ngày hiển thị dd/MM/yyyy
    private static final DateTimeFormatter DF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo Dialog Nhập kho
     * @param parent Frame cha chứa dialog
     * @param service Tầng nghiệp vụ xử lý dữ liệu kho hàng
     */
    public NhapKhoDialog(Frame parent, KhoService service) {
        super(parent, "Nhập kho & hạn sử dụng", true);
        this.khoHangService = service;

        initComponents();
        taiDanhSachSanPham();
    }

    /**
     * Dựng giao diện form nhập kho
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Khung nền chính bo tròn 16px
        RoundedPannel mainCard = new RoundedPannel(Color.WHITE, 16);
        mainCard.setLayout(new BorderLayout(0, 16));
        mainCard.setBorder(new EmptyBorder(22, 26, 22, 26));

        // 1. HEADER (TIÊU ĐỀ + NÚT ĐÓNG X)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Nhập kho & hạn sử dụng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_TEXT_DARK);

        btnDong = new JButton("X");
        btnDong.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnDong.setForeground(COLOR_TEXT_MUTED);
        btnDong.setBackground(COLOR_DISABLED_BG);
        btnDong.setPreferredSize(new Dimension(32, 32));
        btnDong.setBorder(null);
        btnDong.setFocusPainted(false);
        btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDong.addActionListener(e -> dispose());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnDong, BorderLayout.EAST);
        mainCard.add(headerPanel, BorderLayout.NORTH);

        // 2. FORM CÁC TRƯỜNG NHẬP LIỆU
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;

        // HÀNG 0: CHỌN SẢN PHẨM CÓ SẴN (GỘP 2 CỘT)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 12, 0);

        cbSanPham = new JComboBox<>();
        cbSanPham.setPreferredSize(new Dimension(0, 38));
        cbSanPham.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbSanPham.setBackground(Color.WHITE);
        // Khi chọn sản phẩm khác -> Tự động đồng bộ đơn giá nhập và bật/tắt ô HSD
        cbSanPham.addActionListener(e -> dongBoTheoSanPhamDuocChon());
        formPanel.add(taoNhomField("Chọn sản phẩm có sẵn *", cbSanPham), gbc);

        // HÀNG 1 - CỘT TRÁI: SỐ LƯỢNG NHẬP THÊM
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 12, 8);

        txtSoLuongNhap = taoInputField();
        txtSoLuongNhap.putClientProperty("JTextField.placeholderText", "Ví dụ: 10");
        formPanel.add(taoNhomField("Số lượng nhập thêm *", txtSoLuongNhap), gbc);

        // HÀNG 1 - CỘT PHẢI: ĐƠN GIÁ NHẬP (VNĐ)
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 8, 12, 0);

        txtGiaNhap = taoInputField();
        txtGiaNhap.putClientProperty("JTextField.placeholderText", "Ví dụ: 25000");
        formPanel.add(taoNhomField("Đơn giá nhập (VNĐ) *", txtGiaNhap), gbc);

        // HÀNG 2: HẠN SỬ DỤNG TRÊN BAO BÌ (GỘP 2 CỘT)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 12, 0);

        txtHanSuDung = taoInputField();
        txtHanSuDung.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy (ví dụ: 04/08/2027)");
        formPanel.add(taoNhomField("HSD trên bao bì (chỉ áp dụng cho Thức ăn)", txtHanSuDung), gbc);

        // HÀNG 3: GHI CHÚ NHẬP HÀNG (GỘP 2 CỘT)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 12, 0);

        JPanel pnlGhiChu = new JPanel();
        pnlGhiChu.setLayout(new BoxLayout(pnlGhiChu, BoxLayout.Y_AXIS));
        pnlGhiChu.setOpaque(false);
        pnlGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblGhiChu = new JLabel("Ghi chú nhập hàng");
        lblGhiChu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGhiChu.setForeground(COLOR_TEXT_DARK);
        lblGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtGhiChu = new JTextArea(3, 0);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        scrollGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlGhiChu.add(lblGhiChu);
        pnlGhiChu.add(Box.createVerticalStrut(6));
        pnlGhiChu.add(scrollGhiChu);
        formPanel.add(pnlGhiChu, gbc);

        // HÀNG 4: HỘP THÔNG BÁO GỢI Ý (TIP BOX XANH DƯƠNG NHẠT)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 16, 0);

        RoundedPannel tipPanel = new RoundedPannel(COLOR_TIP_BG, 10);
        tipPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JLabel lblTip = new JLabel("Thức ăn: cập nhật HSD để hệ thống tự động cảnh báo cận hạn ≤ 30 ngày.");
        lblTip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTip.setForeground(COLOR_TIP_TEXT);
        tipPanel.add(lblTip);
        formPanel.add(tipPanel, gbc);

        // HÀNG 5: NÚT XÁC NHẬN NHẬP KHO TRÀN VIỀN
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);

        btnXacNhan = new JButton("Xác nhận nhập kho");
        btnXacNhan.setPreferredSize(new Dimension(0, 42));
        btnXacNhan.setBackground(COLOR_PRIMARY_BLUE);
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXacNhan.setBorder(BorderFactory.createEmptyBorder());
        btnXacNhan.addActionListener(e -> xuLyNhapKho());
        formPanel.add(btnXacNhan, gbc);

        mainCard.add(formPanel, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);

        pack();
        setSize(580, 560);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    /**
     * Nạp toàn bộ danh sách sản phẩm đang kinh doanh vào dropdown
     */
    private void taiDanhSachSanPham() {
        cbSanPham.removeAllItems();
        try {
            List<SanPham> ds = khoHangService.layDanhSachSanPhamKho("");
            for (SanPham sp : ds) {
                cbSanPham.addItem(sp);
            }
            dongBoTheoSanPhamDuocChon();
        } catch (Exception ex) {
            System.err.println("[LỖI NẠP SẢN PHẨM]: " + ex.getMessage());
        }
    }

    /**
     * Đồng bộ giao diện theo sản phẩm người dùng đang chọn:
     * - Tự động điền đơn giá nhập hiện tại.
     * - Nếu là Thức ăn: Mở ô HSD và điền hạn hiện có.
     * - Nếu là Phụ kiện: Khóa ô HSD và xóa trống text.
     */
    private void dongBoTheoSanPhamDuocChon() {
        SanPham sp = (SanPham) cbSanPham.getSelectedItem();
        if (sp == null) return;

        // 1. Hiển thị đơn giá nhập hiện có
        if (sp.getGiaNhap() != null && sp.getGiaNhap().compareTo(BigDecimal.ZERO) > 0) {
            txtGiaNhap.setText(sp.getGiaNhap().toPlainString());
        } else {
            txtGiaNhap.setText("");
        }

        // 2. Kiểm tra loại để bật/tắt HSD
        if (sp instanceof ThucAn ta) {
            txtHanSuDung.setEnabled(true);
            txtHanSuDung.setBackground(Color.WHITE);
            if (ta.getHanSuDung() != null) {
                txtHanSuDung.setText(ta.getHanSuDung().format(DF_DATE));
            } else {
                txtHanSuDung.setText("");
            }
        } else {
            // Phụ kiện: khóa không cho nhập hạn sử dụng
            txtHanSuDung.setText("");
            txtHanSuDung.setEnabled(false);
            txtHanSuDung.setBackground(COLOR_DISABLED_BG);
        }
    }

    /**
     * Tạo ô input bo góc với viền xám nhạt
     */
    private JTextField taoInputField() {
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
     * Gom nhóm nhãn tiêu đề và component tương ứng theo chiều dọc, căn lề trái
     */
    private JPanel taoNhomField(String labelText, JComponent comp) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT); // Căn lề trái cho panel con

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT); //Căn chữ sát lề trái

        comp.setAlignmentX(Component.LEFT_ALIGNMENT);  //Căn component (combobox, input) sát lề trái

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(comp);
        return panel;
    }

    /**
     * Xử lý xác thực dữ liệu và gọi Service cập nhật vào CSDL
     */
    private void xuLyNhapKho() {
        try {
            SanPham sp = (SanPham) cbSanPham.getSelectedItem();
            if (sp == null) {
                throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần nhập kho!");
            }

            // 1. Kiểm tra số lượng nhập
            int soLuong;
            try {
                soLuong = Integer.parseInt(txtSoLuongNhap.getText().trim());
                if (soLuong <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Số lượng nhập thêm phải là số nguyên dương lớn hơn 0!");
            }

            // 2. Kiểm tra đơn giá nhập
            BigDecimal giaNhap = BigDecimal.ZERO;
            String strGiaNhap = txtGiaNhap.getText().trim();
            if (!strGiaNhap.isEmpty()) {
                try {
                    giaNhap = new BigDecimal(strGiaNhap);
                    if (giaNhap.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Đơn giá nhập phải là số không âm!");
                }
            }

            // 3. Kiểm tra định dạng HSD nếu là Thức ăn
            LocalDate hsdMoi = null;
            if (sp instanceof ThucAn) {
                String strDate = txtHanSuDung.getText().trim();
                if (!strDate.isEmpty()) {
                    try {
                        hsdMoi = LocalDate.parse(strDate, DF_DATE);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Hạn sử dụng không đúng định dạng dd/MM/yyyy (ví dụ: 04/08/2027)!");
                    }
                }
            }

            // 4. Gọi tầng BUS thực hiện nghiệp vụ
            khoHangService.xuLyNhapKho(sp.getMaSanPham(), soLuong, giaNhap, hsdMoi);

            JOptionPane.showMessageDialog(this,
                    "Nhập kho thành công!\nSản phẩm: " + sp.getTenSanPham() 
                    + "\nSố lượng nhập thêm: +" + soLuong
                    + "\nĐơn giá nhập: " + giaNhap.toPlainString() + " đ",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo nhập liệu", JOptionPane.WARNING_MESSAGE);
        }
    }
}