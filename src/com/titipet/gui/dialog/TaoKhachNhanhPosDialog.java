package com.titipet.gui.dialog;

import com.titipet.bus.KhachHangService;
import com.titipet.entity.KhachHang;
import com.titipet.entity.enums.HangTV;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.UUID;

/** Popup thu ngân thêm nhanh khách hàng, không chỉnh hạng VIP hay điểm. */
public class TaoKhachNhanhPosDialog extends JDialog {
    private static final Color NAVY = new Color(29, 61, 112);
    private static final Color BLUE = new Color(36, 101, 190);
    private static final Color MUTED = new Color(97, 117, 145);
    private final JTextField hoTen = new JTextField();
    private final JTextField soDienThoai = new JTextField();
    private final JLabel loi = new JLabel(" ");
    private final KhachHangService service = new KhachHangService();
    private String maKhachHangDaTao;

    public TaoKhachNhanhPosDialog(Frame parent) {
        super(parent, "Tạo khách hàng nhanh • TiTi Pet POS", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(23, 26, 22, 26));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Thêm khách hàng mới");
        title.setForeground(NAVY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 21));
        JLabel sub = new JLabel("Lưu nhanh tại quầy và tự chọn vào hóa đơn POS");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(MUTED);
        head.add(title);
        head.add(Box.createVerticalStrut(5));
        head.add(sub);
        root.add(head, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.add(input("Họ và tên khách hàng *", hoTen));
        fields.add(Box.createVerticalStrut(13));
        fields.add(input("Số điện thoại (10 chữ số) *", soDienThoai));
        fields.add(Box.createVerticalStrut(12));
        JLabel info = new JLabel("Hạng mới: Thường · Điểm tích lũy: 0");
        info.setForeground(MUTED);
        info.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        fields.add(info);
        root.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(0, 10));
        actions.setOpaque(false);
        loi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loi.setForeground(new Color(188, 50, 50));
        actions.add(loi, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton cancel = new JButton("Hủy");
        JButton save = new JButton("Lưu và chọn khách hàng");
        save.setBackground(BLUE);
        save.setForeground(Color.WHITE);
        save.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancel.setPreferredSize(new Dimension(88, 38));
        save.setPreferredSize(new Dimension(190, 38));
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> luu());
        buttons.add(cancel);
        buttons.add(save);
        actions.add(buttons, BorderLayout.SOUTH);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(save);
        setSize(490, 380);
        setLocationRelativeTo(parent);
        SwingUtilities.invokeLater(hoTen::requestFocusInWindow);
    }

    private JPanel input(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel heading = new JLabel(label);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 12));
        heading.setForeground(NAVY);
        field.setPreferredSize(new Dimension(0, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(216, 226, 242)),
                new EmptyBorder(5, 12, 5, 12)));
        p.add(heading, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void luu() {
        String ten = hoTen.getText().strip();
        String sdt = soDienThoai.getText().strip();
        if (ten.isEmpty() || !sdt.matches("0[0-9]{9}")) {
            loi.setText("Vui lòng nhập tên và số điện thoại hợp lệ (10 chữ số, bắt đầu 0).");
            return;
        }
        try {
            String ma = "KH" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 14).toUpperCase(java.util.Locale.ROOT);
            service.themKhachHang(new KhachHang(ma, ten, sdt, HangTV.THUONG, 0));
            maKhachHangDaTao = ma;
            dispose();
        } catch (Exception ex) {
            loi.setText("Không lưu được: " + ex.getMessage());
        }
    }

    /** null khi người dùng hủy hoặc việc lưu không thành công. */
    public String getMaKhachHangDaTao() {
        return maKhachHangDaTao;
    }
}
