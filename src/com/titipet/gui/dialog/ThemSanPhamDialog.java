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

public class ThemSanPhamDialog extends JDialog {

    private final SanPhamService sanPhamService;
    private boolean daLuuThanhCong = false;

    // CÁC THÀNH PHẦN NHẬP LIỆU
    private JTextField txtTenMatHang;
    private JComboBox<String> cboNhomSanPham;
    private JTextField txtDonGiaBan;
    private JTextField txtTonKhoBanDau;
    private JTextField txtHanSuDung;
    private JComboBox<String> cboHoTroDoiTra;

    private JButton btnTaoMatHang;

    // BẢNG MÀU THIẾT KẾ
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_BORDER = new Color(203, 213, 225);
    private static final Color COLOR_BG = Color.WHITE;

    private static final DateTimeFormatter DF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ThemSanPhamDialog(Frame parent, SanPhamService sanPhamService) {
        super(parent, "Tạo mặt hàng mới", true);
        this.sanPhamService = (sanPhamService != null) ? sanPhamService : new SanPhamService();

        initComponents();
        capNhatTheoLoai();
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

        JLabel lblTitle = new JLabel("Tạo mặt hàng mới");
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

        // 2. KHU VỰC FORM (LƯỚI 3 HÀNG x 2 CỘT)
        JPanel pnlContent = new JPanel(new GridLayout(3, 2, 18, 14));
        pnlContent.setBackground(COLOR_BG);
        pnlContent.setBorder(new EmptyBorder(10, 24, 18, 24));

        // Hàng 1: Tên mặt hàng * | Nhóm sản phẩm *
        txtTenMatHang = createTextField("Nhập tên mặt hàng...");

        cboNhomSanPham = new JComboBox<>(new String[]{"Thức ăn", "Phụ kiện"});
        cboNhomSanPham.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboNhomSanPham.setBackground(Color.WHITE);
        cboNhomSanPham.setPreferredSize(new Dimension(0, 38));
        cboNhomSanPham.addActionListener(e -> capNhatTheoLoai());

        pnlContent.add(createFieldItem("Tên mặt hàng *", txtTenMatHang));
        pnlContent.add(createFieldItem("Nhóm sản phẩm *", cboNhomSanPham));

        // Hàng 2: Đơn giá bán (đ) * | Tồn kho ban đầu
        txtDonGiaBan = createTextField("0");
        txtTonKhoBanDau = createTextField("0");

        pnlContent.add(createFieldItem("Đơn giá bán (đ) *", txtDonGiaBan));
        pnlContent.add(createFieldItem("Tồn kho ban đầu", txtTonKhoBanDau));

        // Hàng 3: Hạn sử dụng | Hỗ trợ đổi trả?
        txtHanSuDung = createTextField("dd/MM/yyyy");
        txtHanSuDung.setText(LocalDate.now().plusMonths(12).format(DF_DATE));

        cboHoTroDoiTra = new JComboBox<>(new String[]{"Có (theo điều kiện)", "Không hỗ trợ"});
        cboHoTroDoiTra.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboHoTroDoiTra.setBackground(Color.WHITE);
        cboHoTroDoiTra.setPreferredSize(new Dimension(0, 38));

        pnlContent.add(createFieldItem("Hạn sử dụng", txtHanSuDung));
        pnlContent.add(createFieldItem("Hỗ trợ đổi trả?", cboHoTroDoiTra));

        add(pnlContent, BorderLayout.CENTER);

        // 3. NÚT TẠO MẶT HÀNG
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(COLOR_BG);
        pnlFooter.setBorder(new EmptyBorder(0, 24, 22, 24));

        btnTaoMatHang = new JButton("Tạo mặt hàng") {
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

        btnTaoMatHang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTaoMatHang.setForeground(Color.WHITE);
        btnTaoMatHang.setPreferredSize(new Dimension(0, 42));
        btnTaoMatHang.setContentAreaFilled(false);
        btnTaoMatHang.setBorderPainted(false);
        btnTaoMatHang.setFocusPainted(false);
        btnTaoMatHang.setOpaque(false);
        btnTaoMatHang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTaoMatHang.addActionListener(e -> xuLyTaoMatHang());

        pnlFooter.add(btnTaoMatHang, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);
    }
    /**
     * Tạo khối nhãn và ô nhập liệu
     */
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
    
    /**
     * Tạo ô văn bản JTextField phẳng viền bo góc nhẹ
     */
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

    private void capNhatTheoLoai() {
        boolean laThucAn = cboNhomSanPham.getSelectedIndex() == 0;
        txtHanSuDung.setEnabled(laThucAn);
        txtHanSuDung.setBackground(laThucAn ? Color.WHITE : new Color(241, 245, 249));
        if (!laThucAn) {
            txtHanSuDung.setText("Không áp dụng");
        } else {
            txtHanSuDung.setText(LocalDate.now().plusMonths(12).format(DF_DATE));
        }
    }

    private void xuLyTaoMatHang() {
        String ten = txtTenMatHang.getText().trim();
        String strGiaBan = txtDonGiaBan.getText().trim();
        String strTon = txtTonKhoBanDau.getText().trim();

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

        int ton = 0;
        try {
            if (!strTon.isEmpty()) ton = Integer.parseInt(strTon);
            if (ton < 0) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Tồn kho ban đầu không hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtTonKhoBanDau.requestFocus();
            return;
        }

        boolean laThucAn = cboNhomSanPham.getSelectedIndex() == 0;
        LoaiSanPham loai = laThucAn ? LoaiSanPham.THUC_AN : LoaiSanPham.PHU_KIEN;

        try {
            // Tự động sinh mã tiếp theo và mã vạch ngầm
            String maMoi = sanPhamService.taoMaSanPhamMoi(loai);
            String maVachTuDong = "893" + (System.currentTimeMillis() % 1000000000L);
            BigDecimal giaNhapMacDinh = giaBan.multiply(new BigDecimal("0.70"));

            if (laThucAn) {
                LocalDate hsd;
                try {
                    hsd = LocalDate.parse(txtHanSuDung.getText().trim(), DF_DATE);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Hạn sử dụng không đúng định dạng (dd/MM/yyyy)!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                    txtHanSuDung.requestFocus();
                    return;
                }

                ThucAn ta = new ThucAn();
                ta.setMaSanPham(maMoi);
                ta.setTenSanPham(ten);
                ta.setMaVach(maVachTuDong);
                ta.setGiaBan(giaBan);
                ta.setGiaNhap(giaNhapMacDinh);
                ta.setSoLuongTonKho(ton);
                ta.setNguongCanhBaoTon(3);
                ta.setLoaiSanPham(LoaiSanPham.THUC_AN);
                ta.setHanSuDung(hsd);
                ta.setTrongLuong(1.0);

                sanPhamService.themSanPham(ta, hsd);
            } else {
                PhuKien pk = new PhuKien();
                pk.setMaSanPham(maMoi);
                pk.setTenSanPham(ten);
                pk.setMaVach(maVachTuDong);
                pk.setGiaBan(giaBan);
                pk.setGiaNhap(giaNhapMacDinh);
                pk.setSoLuongTonKho(ton);
                pk.setNguongCanhBaoTon(3);
                pk.setLoaiSanPham(LoaiSanPham.PHU_KIEN);
                pk.setKichThuoc("Tiêu chuẩn");
                pk.setMauSac("Đa dạng");
                pk.setChatLieu("Cao cấp");

                sanPhamService.themSanPham(pk, (LocalDate) null);
            }

            daLuuThanhCong = true;
            JOptionPane.showMessageDialog(this, "Tạo mặt hàng mới thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tạo mặt hàng: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDaLuuThanhCong() {
        return daLuuThanhCong;
    }

}