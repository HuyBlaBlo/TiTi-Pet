package com.titipet.gui.dialog;

import com.titipet.bus.BanHangService;
import com.titipet.bus.BanHangSpaService;
import com.titipet.dao.BanHangSpaDAO;
import com.titipet.gui.PosUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * UC008 - Popup them dich vu Spa vao gio POS.
 * Chi chon va xac nhan thong tin; nghiep vu thuc thi tai BUS.
 * Khong thao tac SQL tai GUI.
 */
public final class ThemDichVuSpaDialog extends JDialog {
    private static final Color NAVY = PosUI.NAVY;
    private static final Color BLUE = PosUI.BLUE;
    private static final Color MUTED = PosUI.MUTED;
    private static final Color WHITE = PosUI.WHITE;
    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final NumberFormat VND = NumberFormat.getNumberInstance(
            Locale.forLanguageTag("vi-VN"));

    private final BanHangSpaService spaService;
    private final JComboBox<BanHangSpaDAO.DichVu> cboDichVu;
    private final JComboBox<BanHangSpaDAO.ThuCung> cboThuCung;
    private final JTextField txtLoai = PosUI.field("");
    private final JTextField txtCanNang = PosUI.field("");
    private final JTextField txtDonGia = PosUI.field("Nhập giá đã xác nhận");
    private final JLabel lblGiaThamKhao = PosUI.text("", 11, false, MUTED);
    private final JCheckBox chkXacNhan = new JCheckBox(
            "Đã kiểm tra cân nặng và xác nhận đơn giá với khách hàng");
    private BanHangService.DongSpa ketQua;

    /** Hien thi dialog modal; Cancel / X / Esc tra ve null. */
    public static BanHangService.DongSpa hienThi(
            Component parent,
            List<BanHangSpaDAO.DichVu> dichVus,
            List<BanHangSpaDAO.ThuCung> thuCungs,
            BanHangSpaService spaService) {
        if (dichVus == null || dichVus.isEmpty()
                || thuCungs == null || thuCungs.isEmpty()) {
            throw new IllegalArgumentException("Thiếu dịch vụ Spa hoặc thú cưng.");
        }
        Window owner = SwingUtilities.getWindowAncestor(parent);
        ThemDichVuSpaDialog dialog = new ThemDichVuSpaDialog(
                owner, dichVus, thuCungs, spaService);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return dialog.ketQua;
    }

    private ThemDichVuSpaDialog(
            Window owner,
            List<BanHangSpaDAO.DichVu> dichVus,
            List<BanHangSpaDAO.ThuCung> thuCungs,
            BanHangSpaService spaService) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.spaService = java.util.Objects.requireNonNull(spaService);
        cboDichVu = PosUI.combo(dichVus.toArray(new BanHangSpaDAO.DichVu[0]));
        cboThuCung = PosUI.combo(thuCungs.toArray(new BanHangSpaDAO.ThuCung[0]));
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(taoGiaoDien());
        ganSuKien();
        capNhatGiaThamKhao();
        capNhatThuCung();
        pack();
        setResizable(false);
    }

    private JPanel taoGiaoDien() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WHITE);
        root.setBorder(BorderFactory.createLineBorder(PosUI.BORDER, 1));
        root.setPreferredSize(new Dimension(565, 650));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 20));
        JLabel title = PosUI.text("Thêm dịch vụ vào hóa đơn", 19, true, NAVY);
        header.add(title, BorderLayout.CENTER);
        JButton close = PosUI.button("✕", NAVY, PosUI.PALE, PosUI.BORDER);
        close.setPreferredSize(new Dimension(36, 34));
        close.setToolTipText("Đóng (Esc)");
        close.addActionListener(e -> dispose());
        header.add(close, BorderLayout.EAST);
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(WHITE);
        north.add(header, BorderLayout.CENTER);
        JSeparator line = new JSeparator();
        line.setForeground(PosUI.BORDER);
        north.add(line, BorderLayout.SOUTH);
        root.add(north, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setBackground(WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 23, 24));

        addLabel(form, "Dịch vụ Spa *");
        form.add(kichThuoc(cboDichVu, 44));
        addGap(form, 14);

        addLabel(form, "Thú cưng của khách hàng *");
        form.add(kichThuoc(cboThuCung, 44));
        addGap(form, 14);

        txtLoai.setEditable(false);
        txtCanNang.setEditable(false);
        txtLoai.setBackground(PosUI.PALE);
        txtCanNang.setBackground(PosUI.PALE);
        JPanel duo = new JPanel(new GridLayout(1, 2, 12, 0));
        duo.setOpaque(false);
        duo.setAlignmentX(Component.LEFT_ALIGNMENT);
        duo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        duo.add(taoOThongTin("Loài", txtLoai));
        duo.add(taoOThongTin("Cân nặng (kg)", txtCanNang));
        form.add(duo);
        addGap(form, 14);

        addLabel(form, "Đơn giá đã xác nhận theo cân nặng (VNĐ) *");
        form.add(kichThuoc(txtDonGia, 44));
        addGap(form, 5);
        lblGiaThamKhao.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblGiaThamKhao);
        addGap(form, 13);

        chkXacNhan.setOpaque(false);
        chkXacNhan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkXacNhan.setForeground(NAVY);
        chkXacNhan.setFocusPainted(false);
        chkXacNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(chkXacNhan);
        addGap(form, 12);

        JPanel note = new JPanel(new BorderLayout());
        note.setBackground(new Color(255, 248, 236));
        note.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(252, 230, 189)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel content = new JLabel("<html><div style='width:475px'>"
                + "Dịch vụ Spa cần được tiếp nhận và kiểm tra thực tế tại quầy. "
                + "Đơn giá theo cân nặng phải được xác nhận với khách trước khi lập hóa đơn."
                + "</div></html>");
        content.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        content.setForeground(new Color(155, 105, 31));
        note.add(content, BorderLayout.CENTER);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        form.add(note);
        addGap(form, 15);

        JButton btnThem = PosUI.button("+ Thêm vào giỏ POS", WHITE, BLUE, null);
        btnThem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnThem.setPreferredSize(new Dimension(100, 46));
        btnThem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnThem.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnThem.addActionListener(e -> xacNhanThem());
        form.add(btnThem);

        root.add(form, BorderLayout.CENTER);
        getRootPane().setDefaultButton(btnThem);
        return root;
    }

    private static JPanel taoOThongTin(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel lbl = PosUI.text(label, 12, true, NAVY);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private static void addLabel(JPanel parent, String text) {
        JLabel label = PosUI.text(text, 12, true, NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(label);
        addGap(parent, 6);
    }

    private static void addGap(JPanel parent, int size) {
        parent.add(Box.createRigidArea(new Dimension(1, size)));
    }

    private static <T extends JComponent> T kichThuoc(T c, int height) {
        c.setPreferredSize(new Dimension(100, height));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setFont(FONT);
        return c;
    }

    private void ganSuKien() {
        cboDichVu.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof BanHangSpaDAO.DichVu dv) {
                    setText(dv.ten() + " · " + tien(dv.giaCoBan()));
                }
                return this;
            }
        });
        cboThuCung.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof BanHangSpaDAO.ThuCung pet) {
                    setText(pet.ten() + " (" + tenLoai(pet.loai())
                            + ", " + pet.canNang().stripTrailingZeros().toPlainString() + " kg)");
                }
                return this;
            }
        });
        cboDichVu.addActionListener(e -> capNhatGiaThamKhao());
        cboThuCung.addActionListener(e -> capNhatThuCung());
        txtDonGia.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { chkXacNhan.setSelected(false); }
            @Override public void removeUpdate(DocumentEvent e) { chkXacNhan.setSelected(false); }
            @Override public void changedUpdate(DocumentEvent e) { chkXacNhan.setSelected(false); }
        });
        getRootPane().registerKeyboardAction(
                e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void capNhatGiaThamKhao() {
        BanHangSpaDAO.DichVu dv = (BanHangSpaDAO.DichVu) cboDichVu.getSelectedItem();
        if (dv == null) return;
        txtDonGia.setText(dv.giaCoBan().stripTrailingZeros().toPlainString());
        lblGiaThamKhao.setText("Giá cơ bản: " + tien(dv.giaCoBan())
                + " · Chỉnh giá nếu khác sau kiểm tra thực tế");
        chkXacNhan.setSelected(false);
    }

    private void capNhatThuCung() {
        BanHangSpaDAO.ThuCung pet = (BanHangSpaDAO.ThuCung) cboThuCung.getSelectedItem();
        txtLoai.setText(pet == null ? "" : tenLoai(pet.loai()));
        txtCanNang.setText(pet == null || pet.canNang() == null
                ? "" : pet.canNang().stripTrailingZeros().toPlainString());
        chkXacNhan.setSelected(false);
    }

    private void xacNhanThem() {
        try {
            if (!chkXacNhan.isSelected()) {
                throw new IllegalArgumentException(
                        "Bạn phải xác nhận cân nặng và đơn giá Spa trước khi thêm vào giỏ.");
            }
            BigDecimal gia = docDonGia(txtDonGia.getText());
            ketQua = spaService.chonDichVu(
                    (BanHangSpaDAO.DichVu) cboDichVu.getSelectedItem(),
                    (BanHangSpaDAO.ThuCung) cboThuCung.getSelectedItem(), gia);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Kiểm tra thông tin Spa", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static BigDecimal docDonGia(String input) {
        String s = input == null ? "" : input.strip()
                .replace("₫", "").replace("đ", "").replace(" ", "");
        if (s.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập đơn giá Spa.");
        // Ho tro '150000', '150000.00', '150.000', '150.000,00'.
        if (s.matches("[0-9]{1,3}(\\.[0-9]{3})+")) s = s.replace(".", "");
        else if (s.matches("[0-9]{1,3}(\\.[0-9]{3})+,[0-9]{1,2}")) {
            s = s.replace(".", "").replace(",", ".");
        } else if (s.contains(",") && !s.contains(".")) s = s.replace(",", ".");
        BigDecimal gia;
        try { gia = new BigDecimal(s); }
        catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Đơn giá phải là số hợp lệ, ví dụ 150000.");
        }
        if (gia.signum() <= 0 || gia.scale() > 2) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn 0 và tối đa 2 chữ số thập phân.");
        }
        return gia;
    }

    private static String tenLoai(String ma) {
        return "CHO".equalsIgnoreCase(ma) ? "Chó"
                : "MEO".equalsIgnoreCase(ma) ? "Mèo" : ma;
    }

    private static String tien(BigDecimal gia) {
        return gia == null ? "0 ₫" : VND.format(gia) + " ₫";
    }
}
