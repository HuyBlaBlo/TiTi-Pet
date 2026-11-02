package com.titipet.gui.dialog;

import com.titipet.bus.SanPhamService;
import com.titipet.entity.PhuKien;
import com.titipet.entity.SanPham;
import com.titipet.entity.ThucAn;
import com.titipet.entity.enums.LoaiSanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CapNhatSanPhamDialog extends JDialog {

    private final SanPhamService sanPhamService;
    private final SanPham sanPhamHienTai;
    private boolean daCapNhatThanhCong = false;

    // CÁC THÀNH PHẦN NHẬP LIỆU
    private JTextField txtTenMatHang;
    private JComboBox<String> cboNhomSanPham;
    private JTextField txtDonGiaBan;
    private JTextField txtTonKho;
    private JTextField txtHanSuDung;
    private JComboBox<String> cboHoTroDoiTra;

    private JButton btnCapNhatMatHang;

    // BẢNG MÀU THIẾT KẾ PHẲNG
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_BORDER = new Color(203, 213, 225);
    private static final Color COLOR_BG = Color.WHITE;
    private static final Color COLOR_READONLY_BG = new Color(241, 245, 249);

    private static final DateTimeFormatter DF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public CapNhatSanPhamDialog(Frame parent, SanPhamService sanPhamService, SanPham sanPham) {
        super(parent, "Chỉnh sửa sản phẩm", true);
        this.sanPhamService = (sanPhamService != null) ? sanPhamService : new SanPhamService();
        this.sanPhamHienTai = sanPham;

        initComponents();
        dienDuLieuHienTai();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(540, 410);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        // 1. HEADER (TIÊU ĐỀ + NÚT ĐÓNG ✕)
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_BG);
        pnlHeader.setBorder(new EmptyBorder(18, 24, 10, 24));

        JLabel lblTitle = new JLabel("Chỉnh sửa sản phẩm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(COLOR_TEXT_DARK);
        lblTitle.setHorizontalAlignment(SwingConstants.LEFT);

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

        // 2. KHU VỰC FORM (LƯỚI 3 HÀNG x 2 CỘT CĂN TRÁI)
        JPanel pnlContent = new JPanel(new GridLayout(3, 2, 18, 14));
        pnlContent.setBackground(COLOR_BG);
        pnlContent.setBorder(new EmptyBorder(10, 24, 18, 24));

        // Hàng 1: Tên mặt hàng * | Nhóm sản phẩm *
        txtTenMatHang = createTextField("Nhập tên mặt hàng...");

        cboNhomSanPham = new JComboBox<>(new String[]{"Thức ăn", "Phụ kiện"});
        cboNhomSanPham.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboNhomSanPham.setBackground(Color.WHITE);
        cboNhomSanPham.setPreferredSize(new Dimension(0, 38));
        cboNhomSanPham.setEnabled(false); // Không đổi loại để đảm bảo cấu trúc bảng con

        pnlContent.add(createFieldItem("Tên mặt hàng *", txtTenMatHang));
        pnlContent.add(createFieldItem("Nhóm sản phẩm *", cboNhomSanPham));

        // Hàng 2: Đơn giá bán (đ) * | Tồn kho ban đầu
        txtDonGiaBan = createTextField("0");
        txtTonKho = createTextField("0");

        pnlContent.add(createFieldItem("Đơn giá bán (đ) *", txtDonGiaBan));
        pnlContent.add(createFieldItem("Tồn kho ban đầu", txtTonKho));

        // Hàng 3: Hạn sử dụng | Hỗ trợ đổi trả?
        txtHanSuDung = createTextField("dd/MM/yyyy");

        cboHoTroDoiTra = new JComboBox<>(new String[]{"Không", "Có (theo điều kiện)"});
        cboHoTroDoiTra.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboHoTroDoiTra.setBackground(Color.WHITE);
        cboHoTroDoiTra.setPreferredSize(new Dimension(0, 38));

        pnlContent.add(createFieldItem("Hạn sử dụng", txtHanSuDung));
        pnlContent.add(createFieldItem("Hỗ trợ đổi trả?", cboHoTroDoiTra));

        add(pnlContent, BorderLayout.CENTER);

        // 3. NÚT CẬP NHẬT
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(COLOR_BG);
        pnlFooter.setBorder(new EmptyBorder(0, 24, 22, 24));

        btnCapNhatMatHang = new JButton("Cập nhật mặt hàng") {
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

        btnCapNhatMatHang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCapNhatMatHang.setForeground(Color.WHITE);
        btnCapNhatMatHang.setPreferredSize(new Dimension(0, 42));
        btnCapNhatMatHang.setContentAreaFilled(false);
        btnCapNhatMatHang.setBorderPainted(false);
        btnCapNhatMatHang.setFocusPainted(false);
        btnCapNhatMatHang.setOpaque(false);
        btnCapNhatMatHang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCapNhatMatHang.addActionListener(e -> xuLyCapNhat());

        pnlFooter.add(btnCapNhatMatHang, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel createFieldItem(String labelText, JComponent inputComponent) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBackground(COLOR_BG);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        inputComponent.setAlignmentX(Component.LEFT_ALIGNMENT);

        item.add(label);
        item.add(Box.createVerticalStrut(6));
        item.add(inputComponent);
        return item;
    }

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(COLOR_TEXT_DARK);
        tf.setPreferredSize(new Dimension(0, 38));
        tf.setHorizontalAlignment(SwingConstants.LEFT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(0, 10, 0, 10)
        ));
        return tf;
    }

    /**
     * Nạp dữ liệu của sản phẩm đang chọn lên form
     */
    private void dienDuLieuHienTai() {
        if (sanPhamHienTai == null) return;

        txtTenMatHang.setText(sanPhamHienTai.getTenSanPham());

        if (sanPhamHienTai.getGiaBan() != null) {
            txtDonGiaBan.setText(String.format("%.0f", sanPhamHienTai.getGiaBan()));
        }

        txtTonKho.setText(String.valueOf(sanPhamHienTai.getSoLuongTonKho()));

        boolean laThucAn = (sanPhamHienTai instanceof ThucAn) || (sanPhamHienTai.getLoaiSanPham() == LoaiSanPham.THUC_AN);
        cboNhomSanPham.setSelectedIndex(laThucAn ? 0 : 1);

        if (laThucAn) {
            txtHanSuDung.setEnabled(true);
            txtHanSuDung.setBackground(Color.WHITE);
            if (sanPhamHienTai instanceof ThucAn ta && ta.getHanSuDung() != null) {
                txtHanSuDung.setText(ta.getHanSuDung().format(DF_DATE));
            } else {
                txtHanSuDung.setText(LocalDate.now().plusMonths(6).format(DF_DATE));
            }
        } else {
            txtHanSuDung.setEnabled(false);
            txtHanSuDung.setBackground(COLOR_READONLY_BG);
            txtHanSuDung.setText("Không áp dụng");
        }
    }

    /**
     * Xử lý xác thực và gọi Service lưu thay đổi
     */
    private void xuLyCapNhat() {
        String ten = txtTenMatHang.getText().trim();
        String strGiaBan = txtDonGiaBan.getText().trim();
        String strTon = txtTonKho.getText().trim();

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên mặt hàng!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtTenMatHang.requestFocus();
            return;
        }

        BigDecimal giaBan;
        try {
            giaBan = new BigDecimal(strGiaBan.replace(",", "").replace(".", ""));
            if (giaBan.compareTo(BigDecimal.ZERO) < 0) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đơn giá bán không hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtDonGiaBan.requestFocus();
            return;
        }

        int ton;
        try {
            ton = Integer.parseInt(strTon);
            if (ton < 0) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Số lượng tồn kho không hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtTonKho.requestFocus();
            return;
        }

        boolean laThucAn = (sanPhamHienTai instanceof ThucAn) || (sanPhamHienTai.getLoaiSanPham() == LoaiSanPham.THUC_AN);
        LocalDate hanSuDung = null;

        if (laThucAn) {
            String strHsd = txtHanSuDung.getText().trim();
            try {
                hanSuDung = LocalDate.parse(strHsd, DF_DATE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Hạn sử dụng không đúng định dạng (dd/MM/yyyy)!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                txtHanSuDung.requestFocus();
                return;
            }
        }

        try {
            sanPhamHienTai.setTenSanPham(ten);
            sanPhamHienTai.setGiaBan(giaBan);
            sanPhamHienTai.setSoLuongTonKho(ton);

            if (laThucAn && sanPhamHienTai instanceof ThucAn ta) {
                ta.setHanSuDung(hanSuDung);
            }

            sanPhamService.capNhatSanPham(sanPhamHienTai, hanSuDung);

            daCapNhatThanhCong = true;
            JOptionPane.showMessageDialog(this, "Cập nhật mặt hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDaCapNhatThanhCong() {
        return daCapNhatThanhCong;
    }

}