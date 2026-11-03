package com.titipet.gui.dialog;

import com.titipet.dao.LichHenDAO;
import com.titipet.entity.LichHen;
import com.titipet.entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DoiLichDialog extends JDialog {
    private final LichHenDAO lichHenDAO = new LichHenDAO();
    private final LichHen lichHen;

    private JSpinner spnNgay;
    private JComboBox<String> cboGio;
    private JComboBox<NhanVien> cboTho;
    private JTextArea txtGhiChu;

    private boolean updated = false;
    private LocalDate newDate;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final Color BLUE = new Color(36, 101, 190);
    private static final Color TEXT = new Color(35, 51, 75);
    private static final Color BORDER = new Color(222, 231, 243);

    public DoiLichDialog(Window parent, LichHen lichHen) {
        super(parent, "Dời lịch hẹn Spa", ModalityType.APPLICATION_MODAL);
        this.lichHen = lichHen;
        initComponents();
        loadDanhSachTho();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("DỜI LỊCH HẸN SPA", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(BLUE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(title, BorderLayout.CENTER);
        main.add(titlePanel);
        main.add(Box.createVerticalStrut(8));

        JLabel maLich = new JLabel(
                "Mã lịch: " + lichHen.getMaLichHen(),
                SwingConstants.CENTER
        );

        maLich.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        maLich.setForeground(TEXT);
        maLich.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ngayCu = new JLabel(
                "Lịch hiện tại: " + lichHen.getThoiGianHen().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                ),
                SwingConstants.CENTER
        );

        ngayCu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ngayCu.setForeground(TEXT);
        ngayCu.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Đặt mỗi JLabel vào panel có BorderLayout để căn giữa thật sự
        JPanel maLichPanel = new JPanel(new BorderLayout());
        maLichPanel.setOpaque(false);
        maLichPanel.add(maLich, BorderLayout.CENTER);

        JPanel ngayCuPanel = new JPanel(new BorderLayout());
        ngayCuPanel.setOpaque(false);
        ngayCuPanel.add(ngayCu, BorderLayout.CENTER);

        main.add(maLichPanel);
        main.add(Box.createVerticalStrut(4));
        main.add(ngayCuPanel);
        main.add(Box.createVerticalStrut(20));


        // Chỉ cho chọn từ ngày hôm nay trở đi.
        LocalDate homNay = LocalDate.now();
        LocalDate ngayCuCuaLich = lichHen.getThoiGianHen().toLocalDate();
        LocalDate ngayMacDinh = ngayCuCuaLich.isBefore(homNay)
                ? homNay : ngayCuCuaLich;

        Date minDate = Date.from(
                homNay.atStartOfDay(ZoneId.systemDefault()).toInstant()
        );
        Date initialDate = Date.from(
                ngayMacDinh.atStartOfDay(ZoneId.systemDefault()).toInstant()
        );

        spnNgay = new JSpinner(new SpinnerDateModel(
                initialDate, minDate, null, Calendar.DAY_OF_MONTH
        ));
        spnNgay.setEditor(new JSpinner.DateEditor(spnNgay, "dd/MM/yyyy"));

        cboGio = new JComboBox<>();
        for (int hour = 8; hour <= 20; hour++) {
            cboGio.addItem(String.format("%02d:00", hour));
            if (hour < 20) {
                cboGio.addItem(String.format("%02d:30", hour));
            }
        }
        cboGio.setSelectedItem(lichHen.getThoiGianHen().format(TIME_FORMAT));

        cboTho = new JComboBox<>();

        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setText(
                lichHen.getGhiChu() == null ? "" : lichHen.getGhiChu()
        );

        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);

        addField(main, "Ngày hẹn mới", spnNgay);
        addField(main, "Giờ hẹn mới", cboGio);
        addField(main, "Thợ Spa phụ trách", cboTho);
        addField(main, "Ghi chú", scrollGhiChu);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnHuy = new JButton("Đóng");
        JButton btnXacNhan = new JButton("Xác nhận dời lịch");

        btnHuy.addActionListener(e -> dispose());
        btnXacNhan.addActionListener(e -> xacNhanDoiLich());

        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setForeground(TEXT);

        btnXacNhan.setBackground(BLUE);
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setOpaque(true);
        btnXacNhan.setContentAreaFilled(true);
        btnXacNhan.setBorderPainted(false);

        btnHuy.setFocusPainted(false);
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttons.add(btnHuy);
        buttons.add(btnXacNhan);

        main.add(Box.createVerticalStrut(12));
        main.add(buttons);
        add(main, BorderLayout.CENTER);

        pack();
        setResizable(false);
    }

    private void addField(JPanel panel, String label, JComponent input) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT);
        lbl.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel fieldPanel = new JPanel(new BorderLayout(0, 5));
        fieldPanel.setOpaque(false);

        fieldPanel.add(lbl, BorderLayout.NORTH);
        fieldPanel.add(input, BorderLayout.CENTER);

        fieldPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(fieldPanel);
        panel.add(Box.createVerticalStrut(12));
    }

    private void loadDanhSachTho() {
        try {
            List<NhanVien> danhSach = lichHenDAO.layDanhSachThoSpa();
            for (NhanVien nv : danhSach) {
                cboTho.addItem(nv);
            }

            if (lichHen.getThoSpa() != null) {
                for (int i = 0; i < cboTho.getItemCount(); i++) {
                    NhanVien nv = cboTho.getItemAt(i);
                    if (nv.getMaNV().equals(lichHen.getThoSpa().getMaNV())) {
                        cboTho.setSelectedIndex(i);
                        break;
                    }
                }
            }

            cboTho.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus
                ) {
                    super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus
                    );
                    if (value instanceof NhanVien) {
                        setText(((NhanVien) value).getHoTen());
                    }
                    return this;
                }
            });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải danh sách thợ Spa: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void xacNhanDoiLich() {
        try {
            Date selectedDate = (Date) spnNgay.getValue();
            LocalDate ngay = selectedDate.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            if (ngay.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(
                        this, "Ngày hẹn mới không được nằm trong quá khứ.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String gio = (String) cboGio.getSelectedItem();
            if (gio == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn giờ hẹn.");
                return;
            }

            LocalTime thoiGian = LocalTime.parse(gio, TIME_FORMAT);
            LocalDateTime thoiGianMoi = LocalDateTime.of(ngay, thoiGian);

            if (thoiGianMoi.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(
                        this, "Không thể dời lịch về thời gian trong quá khứ.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            NhanVien thoDuocChon = (NhanVien) cboTho.getSelectedItem();
            if (thoDuocChon == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn thợ Spa.");
                return;
            }

            String ghiChu = txtGhiChu.getText().trim();
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Bạn có chắc muốn dời lịch hẹn này?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            boolean success = lichHenDAO.doiLichHen(
                    lichHen.getMaLichHen(), thoiGianMoi,
                    thoDuocChon.getMaNV(), ghiChu
            );

            if (!success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Thợ Spa đã có lịch hẹn vào thời điểm này.\n"
                                + "Vui lòng chọn giờ hoặc thợ khác.",
                        "Trùng lịch", JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            updated = true;
            newDate = ngay;

            JOptionPane.showMessageDialog(
                    this, "Dời lịch hẹn thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE
            );
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, "Không thể cập nhật lịch hẹn: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(),
                    "Không thể dời lịch", JOptionPane.WARNING_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "Dữ liệu không hợp lệ: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public boolean isUpdated() {
        return updated;
    }

    public LocalDate getNewDate() {
        return newDate;
    }
}
