package com.titipet.gui.panel;

import com.titipet.bus.KhachHangService;
import com.titipet.dao.LichHenDAO;
import com.titipet.entity.DichVuSpa;
import com.titipet.entity.KhachHang;
import com.titipet.entity.LichHen;
import com.titipet.entity.NhanVien;
import com.titipet.entity.ThuCung;
import com.titipet.entity.enums.TrangThaiLichHen;
import com.titipet.util.RoundedPannel;
import com.titipet.gui.dialog.DoiLichDialog;
import com.titipet.gui.dialog.ThemThuCungDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Màn hình đặt lịch hẹn Spa - TiTiPet.
 * Giao diện được dựng bằng Swing, kế thừa JPanel và sử dụng trực tiếp LichHenDAO.
 */
public class LichHenPanel extends JPanel {

    private final LichHenDAO lichHenDAO;
    private final KhachHangService khachHangService;

    private static final Color COLOR_BG = new Color(245, 248, 253);
    private static final Color COLOR_BLUE = new Color(36, 101, 190);
    private static final Color COLOR_BLUE_DARK = new Color(28, 75, 145);
    private static final Color COLOR_TEXT = new Color(35, 51, 75);
    private static final Color COLOR_MUTED = new Color(117, 135, 160);
    private static final Color COLOR_BORDER = new Color(222, 231, 243);
    private static final Color COLOR_LIGHT_BLUE = new Color(235, 243, 255);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0 đ");

    private JTextField txtKhachHang;
    private JTextField txtNgayHen;
    private JTextField txtNgayLich;
    private JComboBox<String> cboGioHen;
    private JTextField txtGhiChu;
    private JComboBox<ComboItem<ThuCung>> cboThuCung;
    private JComboBox<ComboItem<DichVuSpa>> cboDichVu;
    private JComboBox<ComboItem<NhanVien>> cboThoSpa;
    private JComboBox<String> cboNguonDat;
    private JLabel lblGiaDuKien;
    private JLabel lblThongTinKhach;
    private JLabel lblNgayLich;
    private JPanel danhSachLichPanel;
    private KhachHang khachHangDangChon;

    public LichHenPanel() {
        lichHenDAO = new LichHenDAO();
        khachHangService = new KhachHangService();
        initComponents();
        loadComboboxData();
        loadLichHenTheoNgay(LocalDate.now());
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(COLOR_BG);
        main.setBorder(new EmptyBorder(22, 20, 22, 20));

        main.add(createHeader());
        main.add(Box.createVerticalStrut(18));
        main.add(createMainContent());

        JScrollPane scroll = new JScrollPane(main);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(COLOR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));

        JLabel breadcrumb = new JLabel("TI TI PET  •  WORKSPACE");
        breadcrumb.setFont(new Font("Segoe UI", Font.BOLD, 10));
        breadcrumb.setForeground(COLOR_BLUE);

        JLabel title = new JLabel("Đặt lịch hẹn Spa");
        title.setFont(new Font("Segoe UI", Font.BOLD, 23));
        title.setForeground(COLOR_TEXT);

        JLabel description = new JLabel("Kiểm tra khung giờ, phân công thợ và tiếp nhận đặt lịch đa kênh");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        description.setForeground(COLOR_MUTED);

        titleGroup.add(breadcrumb);
        titleGroup.add(Box.createVerticalStrut(4));
        titleGroup.add(title);
        titleGroup.add(Box.createVerticalStrut(3));
        titleGroup.add(description);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        badges.setOpaque(false);
        badges.add(createBadge(LocalDate.now().format(DATE_FORMAT), Color.WHITE, COLOR_MUTED));
        badges.add(createBadge("● Chưa mở ca", new Color(255, 247, 224), new Color(190, 119, 20)));
        JButton btnRefresh = createButton("Làm mới", Color.WHITE, COLOR_TEXT, false);
        btnRefresh.addActionListener(e -> {
            loadComboboxData();
            loadLichHenTheoNgay(parseNgay(txtNgayLich.getText()));
        });
        badges.add(btnRefresh);

        header.add(titleGroup, BorderLayout.WEST);
        header.add(badges, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.gridx = 0;
        gbc.weightx = 1.8;
        content.add(createBookingCard(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(createScheduleCard(), gbc);
        return content;
    }

    private JPanel createBookingCard() {
        RoundedPannel card = new RoundedPannel(Color.WHITE, 16);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(17, 18, 16, 18));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Phiếu tiếp nhận lịch hẹn Spa");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(COLOR_TEXT);
        JLabel sub = new JLabel("Ghi tên khách, điện thoại, Fanpage hoặc TikTok");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(COLOR_MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(3));
        heading.add(sub);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int row = 0;

        txtKhachHang = createPlaceholderTextField("Nhập SĐT hoặc mã khách hàng");
        JButton btnTimKhach = createButton("Tìm khách", COLOR_LIGHT_BLUE, COLOR_BLUE, false);
        btnTimKhach.addActionListener(e -> timKhachHang());
        JPanel customerInput = new JPanel(new BorderLayout(6, 0));
        customerInput.setOpaque(false);
        customerInput.add(txtKhachHang, BorderLayout.CENTER);
        customerInput.add(btnTimKhach, BorderLayout.EAST);
        addField(form, "Số điện thoại / Khách hàng *", customerInput, row++, 2);

        lblThongTinKhach = new JLabel("Chưa chọn khách hàng");
        lblThongTinKhach.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblThongTinKhach.setForeground(COLOR_MUTED);
        addField(form, "Thông tin khách hàng", lblThongTinKhach, row++, 2);

        cboThuCung = new JComboBox<>();

        JButton btnThemThuCung = createButton(
                "+ Thêm thú cưng",
                COLOR_LIGHT_BLUE,
                COLOR_BLUE,
                false
        );

        btnThemThuCung.addActionListener(e -> {
            // Kiểm tra đã chọn khách hàng chưa
            if (khachHangDangChon == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Vui lòng chọn khách hàng trước khi thêm thú cưng.",
                        "Chưa chọn khách hàng",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Mở dialog thêm thú cưng
            ThemThuCungDialog dialog = new ThemThuCungDialog(
                    SwingUtilities.getWindowAncestor(this),
                    khachHangDangChon
            );

            dialog.setVisible(true);

            // Nhận thú cưng vừa thêm
            ThuCung thuCungMoi = dialog.getThuCungMoi();

            // Thêm vào combobox và tự động chọn
            if (thuCungMoi != null) {
                cboThuCung.addItem(
                        new ComboItem<>(
                                thuCungMoi,
                                thuCungMoi.getTenThuCung()
                        )
                );

                cboThuCung.setSelectedIndex(cboThuCung.getItemCount() - 1);
            }
        });

        JPanel petInput = new JPanel(new BorderLayout(6, 0));
        petInput.setOpaque(false);
        petInput.add(cboThuCung, BorderLayout.CENTER);
        petInput.add(btnThemThuCung, BorderLayout.EAST);

        addField(form, "Thú cưng *", petInput, row++, 2);

        cboDichVu = new JComboBox<>();
        cboThoSpa = new JComboBox<>();
        addField(form, "Dịch vụ dự kiến *", cboDichVu, row, 1);
        addField(form, "Thợ Spa phụ trách *", cboThoSpa, row++, 1);

        txtNgayHen = createTextField(LocalDate.now().format(DATE_FORMAT));
        txtNgayHen.setEditable(false);
        JPanel dateHenPicker = createDatePicker(LocalDate.now(), txtNgayHen,
                ngay -> capNhatKhungGioTheoNgay(ngay));

        cboGioHen = createTimeComboBox();
        capNhatKhungGioTheoNgay(LocalDate.now());
        addField(form, "Ngày hẹn *", dateHenPicker, row, 1);
        addField(form, "Khung giờ hẹn *", cboGioHen, row++, 1);

        cboNguonDat = new JComboBox<>(new String[]{"Trực tiếp", "Điện thoại", "Facebook", "TikTok", "Zalo", "Khác"});
        lblGiaDuKien = new JLabel("Tự tính theo thời lượng / dịch vụ");
        lblGiaDuKien.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblGiaDuKien.setForeground(COLOR_MUTED);
        addField(form, "Nguồn đặt lịch", cboNguonDat, row, 1);
        addField(form, "Giá trị dự kiến", lblGiaDuKien, row++, 1);

        txtGhiChu = createPlaceholderTextField("Ví dụ: Bé hơi nhát nước, cần thao tác nhẹ nhàng...");
        addField(form, "Ghi chú đặc biệt từ khách", txtGhiChu, row++, 2);

        cboDichVu.addActionListener(e -> capNhatGiaDuKien());
        cboThuCung.addActionListener(e -> capNhatGiaDuKien());

        // Panel chứa dấu tick và nội dung ghi chú
        JPanel notePanel = new JPanel(new BorderLayout(2, 0));
        notePanel.setBackground(COLOR_LIGHT_BLUE);

        // Dấu tick
        JLabel tick = new JLabel("  \u2714");
        tick.setFont(new Font("Dialog", Font.BOLD, 15));
        tick.setForeground(new Color(46, 160, 90));

        // Nội dung ghi chú
        JLabel note = new JLabel("Hệ thống kiểm tra trùng lịch theo thợ + khoảng thời gian phục vụ. Đặt lịch online và phân công tại quầy.");
        note.setOpaque(true);
        note.setBackground(COLOR_LIGHT_BLUE);
        note.setForeground(new Color(66, 105, 160));
        note.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        note.setBorder(new EmptyBorder(9, 0, 9, 10));

        // Thêm vào panel
        notePanel.add(tick, BorderLayout.WEST);
        notePanel.add(note, BorderLayout.CENTER);

        JButton btnXacNhan = createButton(
        	    "Xác nhận và lưu lịch hẹn Spa",
        	    COLOR_BLUE,
        	    Color.WHITE,
        	    true
        	);

        	btnXacNhan.addActionListener(e -> themLichHen());

        // Panel chứa nút
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        btnXacNhan.setBackground(COLOR_BLUE);
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setOpaque(true);
        btnXacNhan.setContentAreaFilled(true);
        btnXacNhan.setBorderPainted(false);
        btnXacNhan.setFocusPainted(false);

        buttonPanel.add(btnXacNhan, BorderLayout.CENTER);
        buttonPanel.add(btnXacNhan, BorderLayout.CENTER);

        // Panel phía dưới
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        bottom.add(notePanel);
        bottom.add(Box.createVerticalStrut(9));
        bottom.add(buttonPanel);

        card.add(heading, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createScheduleCard() {
        RoundedPannel card = new RoundedPannel(Color.WHITE, 16);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(17, 16, 16, 16));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Lịch làm việc của thợ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(COLOR_TEXT);
        JLabel sub = new JLabel("Các hẹn trong ngày đã chọn");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(COLOR_MUTED);

        JPanel dateRow = new JPanel(new BorderLayout(6, 0));
        dateRow.setOpaque(false);
        txtNgayLich = createTextField(LocalDate.now().format(DATE_FORMAT));
        txtNgayLich.setEditable(false);
        // Ngày xem lịch độc lập với ngày hẹn trên phiếu.
        JPanel dateLichPicker = createDatePicker(LocalDate.now(), txtNgayLich,
                ngay -> loadLichHenTheoNgay(ngay));
        JButton btnXem = createButton("Xem", COLOR_LIGHT_BLUE, COLOR_BLUE, false);
        btnXem.addActionListener(e -> loadLichHenTheoNgay(parseNgay(txtNgayLich.getText())));
        dateRow.add(dateLichPicker, BorderLayout.CENTER);
        dateRow.add(btnXem, BorderLayout.EAST);

        header.add(title);
        header.add(Box.createVerticalStrut(3));
        header.add(sub);
        header.add(Box.createVerticalStrut(12));
        header.add(dateRow);

        danhSachLichPanel = new JPanel();
        danhSachLichPanel.setOpaque(false);
        danhSachLichPanel.setLayout(new BoxLayout(danhSachLichPanel, BoxLayout.Y_AXIS));

        JScrollPane scheduleScroll = new JScrollPane(danhSachLichPanel);
        scheduleScroll.setBorder(null);
        scheduleScroll.getViewport().setBackground(Color.WHITE);
        scheduleScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        card.add(header, BorderLayout.NORTH);
        card.add(scheduleScroll, BorderLayout.CENTER);
        return card;
    }

    /** Tạo ô chọn ngày bằng popup lịch; người dùng không thể gõ ngày thủ công. */
    private JPanel createDatePicker(LocalDate initialDate, JTextField display,
                                    Consumer<LocalDate> onSelected) {
        JPanel wrapper = new JPanel(new BorderLayout(5, 0));
        wrapper.setOpaque(false);
        display.setEditable(false);
        display.setFocusable(false);
        display.setCursor(Cursor.getDefaultCursor());
        JButton calendarButton = createButton("Chọn", COLOR_LIGHT_BLUE, COLOR_BLUE, false);
        calendarButton.setToolTipText("Chọn ngày trên lịch");
        wrapper.add(display, BorderLayout.CENTER);
        wrapper.add(calendarButton, BorderLayout.EAST);
        calendarButton.addActionListener(e -> showCalendarPopup(calendarButton, display,
                initialDateFromText(display), onSelected));
        display.putClientProperty("datePickerCallback", onSelected);
        return wrapper;
    }

    private LocalDate initialDateFromText(JTextField display) {
        try {
            return LocalDate.parse(display.getText().trim(), DATE_FORMAT);
        } catch (Exception ex) {
            return LocalDate.now();
        }
    }

    private void showCalendarPopup(Component anchor, JTextField display, LocalDate selectedDate,
                                   Consumer<LocalDate> onSelected) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        JPanel calendar = new JPanel(new BorderLayout(5, 8));
        calendar.setBackground(Color.WHITE);
        calendar.setBorder(new EmptyBorder(10, 10, 10, 10));
        YearMonth[] shownMonth = {YearMonth.from(selectedDate)};

        JPanel monthHeader = new JPanel(new BorderLayout(8, 0));
        monthHeader.setOpaque(false);
        JButton previous = createSmallButton("‹", COLOR_LIGHT_BLUE, COLOR_BLUE);
        JButton next = createSmallButton("›", COLOR_LIGHT_BLUE, COLOR_BLUE);
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        monthLabel.setForeground(COLOR_TEXT);
        monthHeader.add(previous, BorderLayout.WEST);
        monthHeader.add(monthLabel, BorderLayout.CENTER);
        monthHeader.add(next, BorderLayout.EAST);

        JPanel daysPanel = new JPanel(new GridLayout(0, 7, 3, 3));
        daysPanel.setOpaque(false);
        String[] weekdays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String day : weekdays) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            label.setForeground(COLOR_MUTED);
            daysPanel.add(label);
        }

        Runnable renderCalendar = () -> {
            monthLabel.setText("Tháng " + shownMonth[0].getMonthValue() + " / " + shownMonth[0].getYear());
            while (daysPanel.getComponentCount() > 7) {
                daysPanel.remove(7);
            }
            LocalDate firstDay = shownMonth[0].atDay(1);
            int offset = firstDay.getDayOfWeek().getValue() - 1;
            for (int i = 0; i < offset; i++) daysPanel.add(new JLabel(""));
            for (int day = 1; day <= shownMonth[0].lengthOfMonth(); day++) {
                LocalDate date = shownMonth[0].atDay(day);
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                dayButton.setFocusPainted(false);
                dayButton.setMargin(new Insets(5, 5, 5, 5));
                dayButton.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
                boolean isPast = date.isBefore(LocalDate.now());
                dayButton.setEnabled(!isPast);
                if (date.equals(selectedDate)) {
                    dayButton.setBackground(COLOR_BLUE);
                    dayButton.setForeground(Color.WHITE);
                } else {
                    dayButton.setBackground(Color.WHITE);
                    dayButton.setForeground(COLOR_TEXT);
                }
                dayButton.addActionListener(event -> {
                    display.setText(date.format(DATE_FORMAT));
                    popup.setVisible(false);
                    onSelected.accept(date);
                });
                daysPanel.add(dayButton);
            }
            daysPanel.revalidate();
            daysPanel.repaint();
        };

        previous.addActionListener(e -> {
            shownMonth[0] = shownMonth[0].minusMonths(1);
            renderCalendar.run();
        });
        next.addActionListener(e -> {
            shownMonth[0] = shownMonth[0].plusMonths(1);
            renderCalendar.run();
        });
        calendar.add(monthHeader, BorderLayout.NORTH);
        calendar.add(daysPanel, BorderLayout.CENTER);
        renderCalendar.run();
        popup.add(calendar);
        popup.show(anchor, 0, anchor.getHeight());
    }

    /** Danh sách giờ cố định, cách nhau 30 phút; không cho nhập tự do. */
    private JComboBox<String> createTimeComboBox() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setEditable(false);
        return combo;
    }

    /** Chỉ hiển thị giờ hoạt động 08:00-20:00; nếu chọn hôm nay thì ẩn giờ đã qua. */
    private void capNhatKhungGioTheoNgay(LocalDate ngay) {
        if (cboGioHen == null || ngay == null) return;
        Object oldSelection = cboGioHen.getSelectedItem();
        cboGioHen.removeAllItems();
        LocalTime now = LocalTime.now();
        for (int hour = 8; hour <= 20; hour++) {
            String[] slots = hour < 20
                    ? new String[]{String.format("%02d:00", hour), String.format("%02d:30", hour)}
                    : new String[]{"20:00"};
            for (String slot : slots) {
                LocalTime time = LocalTime.parse(slot, TIME_FORMAT);
                if (!ngay.equals(LocalDate.now()) || time.isAfter(now)) {
                    cboGioHen.addItem(slot);
                }
            }
        }
        if (oldSelection != null) cboGioHen.setSelectedItem(oldSelection);
        if (cboGioHen.getSelectedItem() == null && cboGioHen.getItemCount() > 0) {
            cboGioHen.setSelectedIndex(0);
        }
    }

    private void addField(JPanel form, String labelText, Component input, int row, int span) {
        GridBagConstraints gbc = new GridBagConstraints();
        Integer nextColumn = (Integer) form.getClientProperty("nextColumn");
        if (nextColumn == null) nextColumn = 0;
        gbc.gridx = span == 2 ? 0 : nextColumn;
        if (span == 2) form.putClientProperty("nextColumn", 0);
        else form.putClientProperty("nextColumn", 1 - nextColumn);
        gbc.gridy = row;
        gbc.gridwidth = span;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 9, span == 1 && gbc.gridx == 0 ? 10 : 0);

        JPanel field = new JPanel();
        field.setOpaque(false);
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(78, 96, 122));
        field.add(label);
        field.add(Box.createVerticalStrut(5));
        if (input instanceof JComponent) {
            JComponent component = (JComponent) input;
            component.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            if (component instanceof JComboBox<?>) styleCombo((JComboBox<?>) component);
            if (component instanceof JLabel) component.setBorder(new EmptyBorder(8, 9, 8, 9));
        }
        field.add(input);
        form.add(field, gbc);
    }

    private JTextField createTextField(String value) {
        JTextField field = new JTextField();
        field.setText(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(COLOR_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(8, 9, 8, 9)));
        return field;
    }

    /**
     * Tạo JTextField có placeholder đúng nghĩa: tự ẩn khi người dùng focus để nhập,
     * tự hiện lại khi rời ô mà không nhập nội dung.
     */
    private JTextField createPlaceholderTextField(String placeholder) {
        JTextField field = createTextField(placeholder);
        field.putClientProperty("placeholder", placeholder);
        field.putClientProperty("showingPlaceholder", Boolean.TRUE);
        field.setForeground(COLOR_MUTED);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (Boolean.TRUE.equals(field.getClientProperty("showingPlaceholder"))) {
                    field.setText("");
                    field.putClientProperty("showingPlaceholder", Boolean.FALSE);
                    field.setForeground(COLOR_TEXT);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    hienThiPlaceholder(field);
                }
            }
        });
        return field;
    }

    /** Trả về nội dung thật, không trả về chính dòng placeholder. */
    private String getPlaceholderFieldValue(JTextField field) {
        if (Boolean.TRUE.equals(field.getClientProperty("showingPlaceholder"))) {
            return "";
        }
        return field.getText().trim();
    }

    /** Đưa ô nhập về trạng thái placeholder ban đầu. */
    private void hienThiPlaceholder(JTextField field) {
        Object placeholder = field.getClientProperty("placeholder");
        if (placeholder != null) {
            field.setText(placeholder.toString());
            field.putClientProperty("showingPlaceholder", Boolean.TRUE);
            field.setForeground(COLOR_MUTED);
        }
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(Color.WHITE);
        combo.setForeground(COLOR_TEXT);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(4, 6, 4, 6)));
    }

    private JButton createButton(String text, Color bg, Color fg, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, primary ? 12 : 11));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(primary ? 10 : 8, primary ? 12 : 10, primary ? 10 : 8, primary ? 12 : 10));
        button.setOpaque(true);
        return button;
    }

    private JPanel createBadge(String text, Color bg, Color fg) {
        RoundedPannel badge = new RoundedPannel(bg, 12);
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(fg);
        badge.add(label);
        return badge;
    }

    private void loadComboboxData() {
        try {
            cboDichVu.removeAllItems();
            for (DichVuSpa dv : lichHenDAO.layDanhSachDichVuSpa()) {
                cboDichVu.addItem(new ComboItem<>(dv, dv.getTenDichVu()));
            }
            cboThoSpa.removeAllItems();
            for (NhanVien nv : lichHenDAO.layDanhSachThoSpa()) {
                cboThoSpa.addItem(new ComboItem<>(nv, nv.getHoTen()));
            }
            cboThuCung.removeAllItems();
            capNhatGiaDuKien();
        } catch (SQLException ex) {
            showError("Không thể tải danh sách dịch vụ / thợ Spa: " + ex.getMessage());
        }
    }

    private void timKhachHang() {
        String keyword = getPlaceholderFieldValue(txtKhachHang);
        if (keyword.isEmpty()) {
            showWarning("Vui lòng nhập số điện thoại hoặc mã khách hàng.");
            return;
        }
        try {
            khachHangDangChon = khachHangService.timKhachHangTheoMaHoacSDT(keyword);
            if (khachHangDangChon == null) {
                lblThongTinKhach.setText("Không tìm thấy khách hàng. Vui lòng kiểm tra lại.");
                cboThuCung.removeAllItems();
                showWarning("Không tìm thấy khách hàng phù hợp.");
                return;
            }
            lblThongTinKhach.setText(khachHangDangChon.getHoTen() + "  •  " + khachHangDangChon.getSoDienThoai());
            cboThuCung.removeAllItems();
            List<ThuCung> pets = lichHenDAO.layThuCungTheoKhachHang(khachHangDangChon.getMaKH());
            for (ThuCung pet : pets) {
                cboThuCung.addItem(new ComboItem<>(pet, pet.getTenThuCung() + " - " + pet.getLoaiThuCung()));
            }
            if (pets.isEmpty()) showWarning("Khách hàng chưa có thú cưng. Vui lòng thêm thú cưng trước khi đặt lịch.");
        } catch (Exception ex) {
            showError("Lỗi tìm khách hàng: " + ex.getMessage());
        }
    }

    private void capNhatGiaDuKien() {
        ComboItem<DichVuSpa> selected = (ComboItem<DichVuSpa>) cboDichVu.getSelectedItem();
        if (selected == null || selected.value == null || selected.value.getDonGiaCoBan() == null) {
            lblGiaDuKien.setText("Tự tính theo dịch vụ");
            return;
        }
        BigDecimal price = selected.value.getDonGiaCoBan();
        lblGiaDuKien.setText(MONEY_FORMAT.format(price));
    }

    private void themLichHen() {
        try {
            if (khachHangDangChon == null) {
                showWarning("Vui lòng tìm và chọn khách hàng trước.");
                return;
            }
            ComboItem<ThuCung> petItem = (ComboItem<ThuCung>) cboThuCung.getSelectedItem();
            ComboItem<DichVuSpa> serviceItem = (ComboItem<DichVuSpa>) cboDichVu.getSelectedItem();
            ComboItem<NhanVien> staffItem = (ComboItem<NhanVien>) cboThoSpa.getSelectedItem();
            if (petItem == null || serviceItem == null || staffItem == null) {
                showWarning("Vui lòng chọn thú cưng, dịch vụ và thợ Spa phụ trách.");
                return;
            }

            LocalDate date = parseNgay(txtNgayHen.getText());
            Object selectedTime = cboGioHen.getSelectedItem();
            if (selectedTime == null) {
                showWarning("Vui lòng chọn khung giờ hẹn.");
                return;
            }
            LocalTime time = LocalTime.parse(selectedTime.toString(), TIME_FORMAT);
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            if (dateTime.isBefore(LocalDateTime.now())) {
                showWarning("Thời gian hẹn không được ở trong quá khứ.");
                return;
            }

            LichHen lichHen = new LichHen();
            lichHen.setThoiGianHen(dateTime);
            lichHen.setKhachHang(khachHangDangChon);
            lichHen.setThuCung(petItem.value);
            lichHen.setDichVu(serviceItem.value);
            lichHen.setThoSpa(staffItem.value);
            lichHen.setTrangThai(TrangThaiLichHen.CHO_TIEP_NHAN);
            String ghiChu = getPlaceholderFieldValue(txtGhiChu);
            String nguon = "Nguồn: " + cboNguonDat.getSelectedItem();
            lichHen.setGhiChu(ghiChu.isEmpty() ? nguon : nguon + ". " + ghiChu);

            boolean inserted = lichHenDAO.themLichHen(lichHen);
            if (!inserted) {
                showWarning("Thợ Spa đã có lịch tại thời điểm này. Vui lòng chọn giờ hoặc thợ khác.");
                return;
            }
            JOptionPane.showMessageDialog(this, "Đặt lịch Spa thành công!\nMã lịch: " + lichHen.getMaLichHen(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtNgayLich.setText(date.format(DATE_FORMAT));
            loadLichHenTheoNgay(date);
            hienThiPlaceholder(txtGhiChu);
        } catch (DateTimeParseException ex) {
            showWarning("Ngày hoặc giờ không đúng định dạng. Ngày: dd/MM/yyyy, giờ: HH:mm.");
        } catch (SQLException ex) {
            showError("Không thể lưu lịch hẹn: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Dữ liệu chưa hợp lệ: " + ex.getMessage());
        }
    }

    private void loadLichHenTheoNgay(LocalDate ngay) {
        if (ngay == null || danhSachLichPanel == null) return;
        try {
            danhSachLichPanel.removeAll();
            List<LichHen> danhSach = lichHenDAO.layLichHenTheoNgay(ngay);
            if (danhSach.isEmpty()) {
                JLabel empty = new JLabel("Chưa có lịch hẹn trong ngày này.");
                empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                empty.setForeground(COLOR_MUTED);
                empty.setBorder(new EmptyBorder(20, 5, 10, 5));
                danhSachLichPanel.add(empty);
            } else {
                for (LichHen lh : danhSach) {
                    danhSachLichPanel.add(createAppointmentItem(lh));
                    danhSachLichPanel.add(Box.createVerticalStrut(10));
                }
            }
            danhSachLichPanel.revalidate();
            danhSachLichPanel.repaint();
        } catch (SQLException ex) {
            showError("Không thể tải lịch làm việc: " + ex.getMessage());
        }
    }

    private JPanel createAppointmentItem(LichHen lh) {
        RoundedPannel item = new RoundedPannel(new Color(249, 251, 255), 12);
        item.setLayout(new BorderLayout(8, 4));
        item.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel time = new JLabel(lh.getThoiGianHen().format(TIME_FORMAT));
        time.setFont(new Font("Segoe UI", Font.BOLD, 12));
        time.setForeground(COLOR_TEXT);

        String petName = lh.getThuCung() == null ? "Thú cưng" : lh.getThuCung().getTenThuCung();
        String customer = lh.getKhachHang() == null ? "Khách hàng" : lh.getKhachHang().getHoTen();
        String staff = lh.getThoSpa() == null ? "Chưa phân công" : lh.getThoSpa().getHoTen();
        String service = lh.getDichVu() == null ? "Dịch vụ Spa" : lh.getDichVu().getTenDichVu();

        JLabel status = new JLabel(trangThaiText(lh.getTrangThai()));
        status.setFont(new Font("Segoe UI", Font.BOLD, 10));
        status.setForeground(lh.getTrangThai() == TrangThaiLichHen.DA_HUY ? new Color(190, 55, 65) : new Color(190, 119, 20));

        JLabel detail = new JLabel(petName + "  •  " + service);
        detail.setFont(new Font("Segoe UI", Font.BOLD, 11));
        detail.setForeground(COLOR_TEXT);
        JLabel sub = new JLabel("Thợ: " + staff + "  •  " + customer);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sub.setForeground(COLOR_MUTED);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(time, BorderLayout.WEST);
        top.add(status, BorderLayout.EAST);
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(detail);
        center.add(Box.createVerticalStrut(4));
        center.add(sub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        actions.setOpaque(false);

        if (lh.getTrangThai() == TrangThaiLichHen.CHO_TIEP_NHAN) {
            JButton accept = createSmallButton(
                "Tiếp nhận", COLOR_LIGHT_BLUE, COLOR_BLUE
            );
            accept.addActionListener(e -> capNhatTrangThai(lh, true));
            actions.add(accept);
        }

        if (lh.getTrangThai() != TrangThaiLichHen.DA_HUY) {

            // Nút dời lịch
            JButton reschedule = createSmallButton(
                "Dời lịch", COLOR_LIGHT_BLUE, COLOR_BLUE
            );
            reschedule.addActionListener(e -> moFormDoiLich(lh));
            actions.add(reschedule);

            // Nút hủy
            JButton cancel = createSmallButton(
                "Hủy",
                new Color(255, 239, 240),
                new Color(190, 55, 65)
            );
            cancel.addActionListener(e -> capNhatTrangThai(lh, false));
            actions.add(cancel);
        }

        JPanel body = new JPanel(new BorderLayout(0, 5));
        body.setOpaque(false);
        body.add(center, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);

        item.add(top, BorderLayout.NORTH);
        item.add(body, BorderLayout.CENTER);
        return item;
    }
    private void moFormDoiLich(LichHen lh) {
        DoiLichDialog dialog = new DoiLichDialog(
                SwingUtilities.getWindowAncestor(this),
                lh
        );

        dialog.setVisible(true);

        if (dialog.isUpdated()) {
            LocalDate ngayMoi = dialog.getNewDate();

            // Cập nhật ngày đang xem trên giao diện
            txtNgayLich.setText(ngayMoi.format(DATE_FORMAT));

            // Tải lại danh sách lịch theo ngày mới
            loadLichHenTheoNgay(ngayMoi);
        }
    }

    private JButton createSmallButton(String text, Color bg, Color fg) {
        JButton button = createButton(text, bg, fg, false);
        button.setBorder(new EmptyBorder(5, 8, 5, 8));
        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        return button;
    }

    private void capNhatTrangThai(LichHen lh, boolean tiepNhan) {
        int confirm = JOptionPane.showConfirmDialog(this,
                tiepNhan ? "Xác nhận tiếp nhận lịch hẹn này?" : "Bạn có chắc muốn hủy lịch hẹn này?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            boolean ok = tiepNhan
                    ? lichHenDAO.tiepNhanLichHen(lh.getMaLichHen())
                    : lichHenDAO.huyLichHen(lh.getMaLichHen());
            if (ok) loadLichHenTheoNgay(parseNgay(txtNgayLich.getText()));
            else showWarning("Không thể cập nhật lịch hẹn. Vui lòng tải lại dữ liệu.");
        } catch (SQLException ex) {
            showError("Lỗi cập nhật trạng thái: " + ex.getMessage());
        }
    }

    private String trangThaiText(TrangThaiLichHen status) {
        if (status == TrangThaiLichHen.DA_TIEP_NHAN) return "Đã tiếp nhận";
        if (status == TrangThaiLichHen.DA_HUY) return "Đã hủy";
        return "Chờ tiếp nhận";
    }

    private LocalDate parseNgay(String value) {
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (Exception ex) {
            showWarning("Ngày phải có định dạng dd/MM/yyyy.");
            return null;
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private static class ComboItem<T> {
        private final T value;
        private final String label;
        private ComboItem(T value, String label) { this.value = value; this.label = label; }
        @Override public String toString() { return label; }
    }
}
