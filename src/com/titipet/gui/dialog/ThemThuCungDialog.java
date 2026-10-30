
package com.titipet.gui.dialog;

import com.titipet.dao.ThuCungDAO;
import com.titipet.entity.KhachHang;
import com.titipet.entity.ThuCung;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ThemThuCungDialog extends JDialog {

    private static final Color COLOR_BLUE = new Color(36, 101, 190);
    private static final Color COLOR_TEXT = new Color(35, 51, 75);
    private static final Color COLOR_MUTED = new Color(117, 135, 160);
    private static final Color COLOR_BORDER = new Color(222, 231, 243);
    private static final Color COLOR_LIGHT_BLUE = new Color(235, 243, 255);

    private final KhachHang chuNuoi;
    private final ThuCungDAO thuCungDAO;

    private JTextField txtTenThuCung;
    private JComboBox<String> cboLoaiThuCung;
    private JTextField txtCanNang;
    private JTextField txtTinhTrangLong;
    private JTextArea txtGhiChu;

    private ThuCung thuCungMoi;

    public ThemThuCungDialog(Window owner, KhachHang chuNuoi) {
        super(owner, "Thêm thú cưng", ModalityType.APPLICATION_MODAL);

        if (chuNuoi == null) {
            throw new IllegalArgumentException(
                    "Khách hàng không được để trống."
            );
        }

        this.chuNuoi = chuNuoi;
        this.thuCungDAO = new ThuCungDAO();

        initComponents();

        setSize(520, 650);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(18, 22, 18, 22));

        main.add(createHeader(), BorderLayout.NORTH);
        main.add(createForm(), BorderLayout.CENTER);
        main.add(createFooter(), BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);
    }

    // ================= HEADER =================

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(
                "Thêm thú cưng mới",
                SwingConstants.CENTER
        );
        title.setFont(new Font("Segoe UI", Font.BOLD, 23));
        title.setForeground(COLOR_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel(
                "Nhập thông tin để tạo hồ sơ thú cưng cho khách hàng.",
                SwingConstants.CENTER
        );
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(COLOR_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(sub);
        header.add(Box.createVerticalStrut(12));

        JPanel ownerBox = new JPanel(new BorderLayout());
        ownerBox.setBackground(COLOR_LIGHT_BLUE);
        ownerBox.setBorder(new EmptyBorder(9, 12, 9, 12));

        JLabel ownerLabel = new JLabel(
                "Chủ nuôi: " + chuNuoi.getMaKH()
        );
        ownerLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ownerLabel.setForeground(COLOR_BLUE);

        ownerBox.add(ownerLabel, BorderLayout.CENTER);
        header.add(ownerBox);

        return header;
    }

    // ================= FORM =================

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(4, 0, 4, 0);

        // Tên thú cưng
        txtTenThuCung = createTextField();
        addField(form, gbc, "Tên thú cưng *", txtTenThuCung);

        // Loại thú cưng
        cboLoaiThuCung = new JComboBox<>(
                new String[]{"Chó", "Mèo"}
        );
        styleComboBox(cboLoaiThuCung);
        addField(form, gbc, "Loại thú cưng *", cboLoaiThuCung);

        // Cân nặng
        txtCanNang = createTextField();
        txtCanNang.setToolTipText(
                "Chỉ nhập số dương. Ví dụ: 4.5"
        );

        ((AbstractDocument) txtCanNang.getDocument())
                .setDocumentFilter(new WeightDocumentFilter());

        addField(form, gbc, "Cân nặng (kg) *", txtCanNang);

        // Tình trạng lông
        txtTinhTrangLong = createTextField();
        addField(form, gbc, "Tình trạng lông", txtTinhTrangLong);

        // Ghi chú
        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtGhiChu.setForeground(COLOR_TEXT);
        txtGhiChu.setBackground(Color.WHITE);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setBorder(
                new EmptyBorder(8, 10, 8, 10)
        );

        JScrollPane scroll = new JScrollPane(txtGhiChu);
        scroll.setBorder(
                new LineBorder(COLOR_BORDER, 1, true)
        );
        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        addField(form, gbc, "Ghi chú", scroll);

        return form;
    }

    // ================= ADD FIELD =================

    private void addField(
            JPanel form,
            GridBagConstraints gbc,
            String label,
            JComponent component
    ) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT);

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.insets = new Insets(4, 0, 3, 0);

        form.add(lbl, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 7, 0);

        form.add(component, gbc);

        gbc.insets = new Insets(4, 0, 4, 0);
    }

    // ================= FOOTER =================

    private JPanel createFooter() {
        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        footer.setOpaque(false);

        JButton btnHuy = createButton(
                "Hủy",
                Color.WHITE,
                COLOR_TEXT
        );

        JButton btnLuu = createButton(
                "Lưu thú cưng",
                COLOR_BLUE,
                Color.WHITE
        );

        btnLuu.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );
        btnLuu.setBorder(
                new EmptyBorder(10, 20, 10, 20)
        );
        btnLuu.setContentAreaFilled(true);
        btnLuu.setBorderPainted(false);

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> luuThuCung());

        footer.add(btnHuy);
        footer.add(btnLuu);

        return footer;
    }

    // ================= TEXT FIELD =================

    private JTextField createTextField() {
        JTextField field = new JTextField();

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        field.setForeground(COLOR_TEXT);
        field.setBackground(Color.WHITE);

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(COLOR_BORDER, 1, true),
                        new EmptyBorder(8, 10, 8, 10)
                )
        );

        return field;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        combo.setForeground(COLOR_TEXT);
        combo.setBackground(Color.WHITE);
        combo.setBorder(
                new LineBorder(COLOR_BORDER, 1, true)
        );
    }

    // ================= BUTTON =================

    private JButton createButton(
            String text,
            Color background,
            Color foreground
    ) {
        JButton button = new JButton(text);

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );
        button.setBorder(
                new EmptyBorder(10, 17, 10, 17)
        );
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        return button;
    }

    // ================= WEIGHT VALIDATION =================

    private static class WeightDocumentFilter extends DocumentFilter {

    	private boolean isValidWeight(String text) {
    	    if (text.isEmpty()) {
    	        return true;
    	    }

    	    // Chỉ cho nhập số, tối đa 2 chữ số thập phân
    	    if (!text.matches("\\d*(\\.\\d{0,2})?")) {
    	        return false;
    	    }

    	    // Không cho nhập số 0 ở đầu
    	    if (text.startsWith("0")) {
    	        return false;
    	    }

    	    try {
    	        BigDecimal value = new BigDecimal(text);
    	        return value.compareTo(BigDecimal.ZERO) > 0;
    	    } catch (NumberFormatException e) {
    	        return false;
    	    }
    	}
    	
        @Override
        public void insertString(
                FilterBypass fb,
                int offset,
                String string,
                AttributeSet attr
        ) throws BadLocationException {

            if (string == null) {
                return;
            }

            String oldText = fb.getDocument().getText(
                    0,
                    fb.getDocument().getLength()
            );

            String newText = oldText.substring(0, offset)
                    + string
                    + oldText.substring(offset);

            newText = newText.replace(',', '.');

            if (isValidWeight(newText)) {
                fb.replace(
                        0,
                        fb.getDocument().getLength(),
                        newText,
                        attr
                );
            }
        }

        @Override
        public void replace(
                FilterBypass fb,
                int offset,
                int length,
                String text,
                AttributeSet attrs
        ) throws BadLocationException {

            if (text == null) {
                text = "";
            }

            String oldText = fb.getDocument().getText(
                    0,
                    fb.getDocument().getLength()
            );

            String newText = oldText.substring(0, offset)
                    + text
                    + oldText.substring(offset + length);

            newText = newText.replace(',', '.');

            if (isValidWeight(newText)) {
                fb.replace(
                        0,
                        fb.getDocument().getLength(),
                        newText,
                        attrs
                );
            }
        }
    }

    // ================= SAVE PET =================

    private void luuThuCung() {
        String ten = txtTenThuCung.getText().trim();

        String loai = cboLoaiThuCung.getSelectedIndex() == 0
                ? "CHO" : "MEO";

        String canNangText = txtCanNang.getText().trim();
 
        String tinhTrangLong = txtTinhTrangLong.getText().trim();

        if (!tinhTrangLong.matches("[\\p{L}\\s,\\.\\-]*")) {
            showError("Tình trạng lông không được chứa số hoặc ký tự không hợp lệ.");
            txtTinhTrangLong.requestFocus();
            return;
        }

        if (tinhTrangLong.length() > 300) {
            showError("Tình trạng lông không được vượt quá 300 ký tự.");
            txtTinhTrangLong.requestFocus();
            return;
        }
        String ghiChu = txtGhiChu.getText().trim();

        if (ten.isEmpty()) {
            showError("Vui lòng nhập tên thú cưng.");
            txtTenThuCung.requestFocus();
            return;
        }

        if (ten.length() > 120) {
            showError(
                    "Tên thú cưng không được vượt quá 120 ký tự."
            );
            txtTenThuCung.requestFocus();
            return;
        }

        if (canNangText.isEmpty()) {
            showError("Vui lòng nhập cân nặng.");
            txtCanNang.requestFocus();
            return;
        }

        BigDecimal canNang;

        try {
            canNang = new BigDecimal(canNangText);
        } catch (NumberFormatException e) {
            showError(
                    "Cân nặng phải là số hợp lệ. Ví dụ: 4.5"
            );
            txtCanNang.requestFocus();
            return;
        }

        if (canNang.compareTo(BigDecimal.ZERO) <= 0) {
            showError(
                    "Cân nặng phải lớn hơn 0, không được nhập số âm hoặc 0."
            );
            txtCanNang.requestFocus();
            return;
        }

        if (canNang.precision() > 8 || canNang.scale() > 2) {
            showError(
                    "Cân nặng tối đa 2 chữ số thập phân."
            );
            txtCanNang.requestFocus();
            return;
        }

        if (tinhTrangLong.length() > 300) {
            showError(
                    "Tình trạng lông không được vượt quá 300 ký tự."
            );
            txtTinhTrangLong.requestFocus();
            return;
        }

        if (ghiChu.length() > 1000) {
            showError(
                    "Ghi chú không được vượt quá 1000 ký tự."
            );
            txtGhiChu.requestFocus();
            return;
        }

        try {
            thuCungMoi = thuCungDAO.themThuCung(
                    ten,
                    loai,
                    canNang,
                    tinhTrangLong.isEmpty() ? null : tinhTrangLong,
                    ghiChu.isEmpty() ? null : ghiChu,
                    chuNuoi
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Thêm thú cưng thành công!\nMã thú cưng: "
                            + thuCungMoi.getMaThuCung(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (SQLException | IllegalArgumentException e) {
            showError(
                    "Không thể thêm thú cưng:\n" + e.getMessage()
            );
        }
    }

    // ================= ERROR =================

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông tin chưa hợp lệ",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // ================= GET RESULT =================

    public ThuCung getThuCungMoi() {
        return thuCungMoi;
    }
}
