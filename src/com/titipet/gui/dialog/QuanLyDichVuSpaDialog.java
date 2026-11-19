package com.titipet.gui.dialog;

import com.titipet.bus.SanPhamService;
import com.titipet.entity.DichVuSpa;
import com.titipet.entity.enums.LoaiThuCungApDung;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/** Form them/sua dich vu Spa danh muc; GUI chi goi BUS. */
public final class QuanLyDichVuSpaDialog extends JDialog {
    private final SanPhamService service;
    private final DichVuSpa data;
    private final boolean editing;
    private boolean saved;
    private final JTextField ten = new JTextField(24);
    private final JTextField gia = new JTextField(24);
    private final JTextField thue = new JTextField("0", 24);
    private final JTextField thoiGian = new JTextField("60", 24);
    private final JComboBox<LoaiThuCungApDung> loai = new JComboBox<>(LoaiThuCungApDung.values());
    private final JCheckBox giaDaCoThue = new JCheckBox("Đơn giá đã bao gồm thuế");
    private final JCheckBox hoatDong = new JCheckBox("Dịch vụ đang hoạt động", true);

    public static boolean hienThi(Component parent, SanPhamService service, DichVuSpa existing) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        QuanLyDichVuSpaDialog dialog = new QuanLyDichVuSpaDialog(owner, service, existing);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return dialog.saved;
    }

    private QuanLyDichVuSpaDialog(Window owner, SanPhamService service, DichVuSpa existing) {
        super(owner, existing == null ? "Thêm dịch vụ Spa" : "Sửa dịch vụ Spa", ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.editing = existing != null;
        this.data = existing == null ? new DichVuSpa() : existing;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBorder(new EmptyBorder(20, 22, 20, 22));
        root.setBackground(Color.WHITE);
        JLabel header = new JLabel(editing ? "Chỉnh sửa dịch vụ Spa" : "Thêm dịch vụ Spa mới");
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        root.add(header, BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridLayout(0, 1, 4, 3));
        fields.setOpaque(false);
        addField(fields, "Tên dịch vụ *", ten);
        addField(fields, "Loài thú cưng *", loai);
        addField(fields, "Đơn giá cơ bản (đ) *", gia);
        addField(fields, "Thuế suất (%) *", thue);
        addField(fields, "Thời lượng (phút) *", thoiGian);
        fields.add(giaDaCoThue);
        fields.add(hoatDong);
        root.add(fields, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton cancel = new JButton("Hủy");
        cancel.addActionListener(e -> dispose());
        JButton save = new JButton(editing ? "Lưu thay đổi" : "Thêm dịch vụ");
        save.setBackground(new Color(30, 103, 209));
        save.setForeground(Color.WHITE);
        save.addActionListener(e -> luu());
        buttons.add(cancel);
        buttons.add(save);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        if (editing) {
            ten.setText(data.getTenDichVu());
            gia.setText(data.getDonGiaCoBan().toPlainString());
            thue.setText(data.getThueSuat().toPlainString());
            thoiGian.setText(String.valueOf(data.getThoiLuongPhut()));
            loai.setSelectedItem(data.getLoaiThuCungApDung());
            giaDaCoThue.setSelected(data.isGiaDaBaoGomThue());
            hoatDong.setSelected(data.isDangHoatDong());
        }
        getRootPane().setDefaultButton(save);
        pack();
        setMinimumSize(new Dimension(440, 490));
        setResizable(true);
    }

    private static void addField(JPanel parent, String label, JComponent component) {
        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        parent.add(title);
        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        parent.add(component);
    }

    private void luu() {
        try {
            data.setTenDichVu(ten.getText().strip());
            data.setDonGiaCoBan(new BigDecimal(gia.getText().strip().replace(",", "")));
            data.setThueSuat(new BigDecimal(thue.getText().strip().replace(",", ".")));
            data.setThoiLuongPhut(Integer.parseInt(thoiGian.getText().strip()));
            data.setLoaiThuCungApDung((LoaiThuCungApDung) loai.getSelectedItem());
            data.setGiaDaBaoGomThue(giaDaCoThue.isSelected());
            data.setDangHoatDong(hoatDong.isSelected());
            service.luuDichVuSpa(data, editing);
            saved = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá, thuế và thời lượng phải là số hợp lệ.");
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Không lưu được dịch vụ Spa", JOptionPane.ERROR_MESSAGE);
        }
    }
}
