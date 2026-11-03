package com.titipet.gui.panel;

import com.titipet.bus.HoaDonService;
import com.titipet.dao.HoaDonDAO.HoaDonChiTiet;
import com.titipet.dao.HoaDonDAO.HoaDonTomTat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Màn hình Hóa đơn / lịch sử giao dịch theo mẫu TiTi Pet, không ghi sửa dữ liệu. */
public final class HoaDonPanel extends JPanel {
    private static final Color BG = new Color(245, 248, 254);
    private static final Color WHITE = Color.WHITE;
    private static final Color BLUE = new Color(37, 102, 204);
    private static final Color NAVY = new Color(29, 65, 111);
    private static final Color MUTED = new Color(136, 155, 185);
    private static final Color BORDER = new Color(228, 236, 247);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final HoaDonService service;
    private final JTextField timKiem = new JTextField();
    private final JComboBox<String> locTrangThai = new JComboBox<>(new String[] {
            "Đã thanh toán", "Tất cả trạng thái", "Chờ thanh toán", "Đang đối soát", "Đã hủy"});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {"MÃ HÓA ĐƠN", "THỜI GIAN", "KHÁCH HÀNG", "KÊNH",
                    "THANH TOÁN", "TỔNG TIỀN", "TRẠNG THÁI", "THAO TÁC"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable bang = new JTable(model);
    private final JLabel trangThai = new JLabel("Đang tải hóa đơn...");
    private final JLabel tongSo = new JLabel("0 hóa đơn");
    private final JPanel vungDanhSach = new JPanel(new BorderLayout());
    private List<HoaDonTomTat> duLieu = List.of();
    private int phienTai = 0;

    public HoaDonPanel(HoaDonService service) {
        this.service = java.util.Objects.requireNonNull(service);
        setLayout(new BorderLayout());
        setBackground(BG);
        taoGiaoDien();
        taoSuKien();
        SwingUtilities.invokeLater(this::taiDuLieu);
    }

    private void taoGiaoDien() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 28, 24, 28));
        add(content, BorderLayout.CENTER);

        JPanel heading = trong(new BorderLayout(12, 8));
        JPanel titles = trong(new GridLayout(3, 1, 0, 4));
        titles.add(chu("T I T I P E T  ·  W O R K S P A C E", 10, BLUE, true));
        titles.add(chu("Hóa đơn & lịch sử giao dịch", 26, NAVY, true));
        titles.add(chu("Tra cứu, xem chi tiết và in hóa đơn; theo dõi kênh bán hàng", 12, MUTED, false));
        heading.add(titles, BorderLayout.CENTER);
        JLabel hint = chu("●  Dữ liệu từ SQL Server", 11, BLUE, true);
        hint.setHorizontalAlignment(SwingConstants.RIGHT);
        heading.add(hint, BorderLayout.EAST);
        content.add(heading, BorderLayout.NORTH);

        JPanel card = trong(new BorderLayout(0, 20));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)));
        content.add(card, BorderLayout.CENTER);

        JPanel controls = trong(new BorderLayout(0, 15));
        JPanel header = trong(new BorderLayout(12, 0));
        JPanel captions = trong(new GridLayout(2, 1, 0, 4));
        captions.add(chu("Danh sách hóa đơn", 16, NAVY, true));
        captions.add(chu("Hóa đơn từ POS, Spa và các kênh bán hàng; hiển thị tối đa 200 bản ghi gần nhất", 11, MUTED, false));
        header.add(captions, BorderLayout.CENTER);
        JButton csv = nut("Xuất CSV", WHITE, NAVY);
        csv.addActionListener(e -> xuatCsv());
        header.add(csv, BorderLayout.EAST);
        controls.add(header, BorderLayout.NORTH);

        JPanel searchArea = trong(new BorderLayout(10, 0));
        timKiem.putClientProperty("JTextField.placeholderText", "Tìm mã hóa đơn, tên khách hàng, số điện thoại...");
        timKiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timKiem.setPreferredSize(new Dimension(270, 44));
        timKiem.setToolTipText("Nhấn Enter để tìm kiếm");
        searchArea.add(timKiem, BorderLayout.CENTER);
        locTrangThai.setPreferredSize(new Dimension(185, 44));
        locTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchArea.add(locTrangThai, BorderLayout.EAST);
        JButton search = nut("⌕  Tìm", BLUE, WHITE);
        search.setPreferredSize(new Dimension(82, 44));
        JPanel searchWithButton = trong(new BorderLayout(10, 0));
        searchWithButton.add(searchArea, BorderLayout.CENTER);
        searchWithButton.add(search, BorderLayout.EAST);
        controls.add(searchWithButton, BorderLayout.CENTER);
        search.addActionListener(e -> taiDuLieu());
        timKiem.addActionListener(e -> taiDuLieu());
        locTrangThai.addActionListener(e -> taiDuLieu());
        card.add(controls, BorderLayout.NORTH);

        bang.setRowHeight(48);
        bang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bang.setForeground(NAVY);
        bang.setGridColor(BORDER);
        bang.setShowVerticalLines(false);
        bang.setSelectionBackground(new Color(228, 238, 255));
        bang.setSelectionForeground(NAVY);
        bang.setFillsViewportHeight(true);
        bang.getTableHeader().setPreferredSize(new Dimension(0, 42));
        bang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        bang.getTableHeader().setBackground(new Color(246, 249, 254));
        bang.getTableHeader().setForeground(MUTED);
        bang.getTableHeader().setReorderingAllowed(false);
        bang.setAutoCreateRowSorter(false);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        bang.getColumnModel().getColumn(0).setPreferredWidth(195);
        bang.getColumnModel().getColumn(1).setPreferredWidth(130);
        bang.getColumnModel().getColumn(2).setPreferredWidth(150);
        bang.getColumnModel().getColumn(3).setPreferredWidth(90);
        bang.getColumnModel().getColumn(4).setPreferredWidth(110);
        bang.getColumnModel().getColumn(5).setPreferredWidth(120);
        bang.getColumnModel().getColumn(6).setPreferredWidth(120);
        bang.getColumnModel().getColumn(7).setPreferredWidth(100);

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
        cell.setBorder(new EmptyBorder(0, 10, 0, 8));
        for (int i = 0; i < bang.getColumnCount(); i++) {
            bang.getColumnModel().getColumn(i).setCellRenderer(cell);
        }
        DefaultTableCellRenderer amount = new DefaultTableCellRenderer();
        amount.setHorizontalAlignment(SwingConstants.RIGHT);
        amount.setForeground(BLUE);
        amount.setBorder(new EmptyBorder(0, 8, 0, 12));
        bang.getColumnModel().getColumn(5).setCellRenderer(amount);
        DefaultTableCellRenderer action = new DefaultTableCellRenderer();
        action.setForeground(BLUE);
        action.setHorizontalAlignment(SwingConstants.CENTER);
        bang.getColumnModel().getColumn(7).setCellRenderer(action);

        vungDanhSach.setOpaque(false);
        card.add(vungDanhSach, BorderLayout.CENTER);
        hienThiBang();

        JPanel footer = trong(new BorderLayout());
        footer.add(tongSo, BorderLayout.WEST);
        trangThai.setForeground(MUTED);
        trangThai.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(trangThai, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);
    }

    private void taoSuKien() {
        bang.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int index = bang.rowAtPoint(e.getPoint());
                if (index < 0 || index >= duLieu.size()) return;
                // Click cột thao tác hoặc nhấp đúp dòng để xem hóa đơn.
                if (bang.columnAtPoint(e.getPoint()) == 7 || e.getClickCount() >= 2) {
                    xemChiTiet(duLieu.get(index).maHoaDon());
                }
            }
        });
        bang.setToolTipText("Nhấp đúp vào dòng hoặc chọn 'Xem chi tiết'");
    }

    /** Gọi lại khi người dùng chuyển từ POS sang menu Hóa đơn. */
    public void taiDuLieu() {
        final int phien = ++phienTai;
        final String keyword = timKiem.getText();
        final String filter = maTrangThai();
        trangThai.setText("Đang truy vấn SQL Server...");
        new SwingWorker<List<HoaDonTomTat>, Void>() {
            @Override protected List<HoaDonTomTat> doInBackground() throws Exception {
                return service.timKiem(keyword, filter);
            }
            @Override protected void done() {
                if (phien != phienTai) return;
                try {
                    duLieu = List.copyOf(get());
                    model.setRowCount(0);
                    for (HoaDonTomTat h : duLieu) {
                        model.addRow(new Object[] {
                                h.maHoaDon(),
                                h.ngayLap() == null ? "" : FORMAT.format(h.ngayLap()),
                                h.tenKhach(), HoaDonService.tenNguon(h.nguonDon()),
                                HoaDonService.tenThanhToan(h.phuongThuc()),
                                HoaDonService.tien(h.tienPhaiThu()) + " đ",
                                HoaDonService.tenTrangThai(h.trangThai()), "Xem chi tiết"
                        });
                    }
                    hienThiBang();
                    tongSo.setText(duLieu.size() + " hóa đơn (tối đa 200)");
                    trangThai.setText(duLieu.isEmpty()
                            ? "Không tìm thấy hóa đơn phù hợp." : "Đã cập nhật từ SQL Server.");
                } catch (InterruptedException err) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException err) {
                    trangThai.setText("Không tải được hóa đơn.");
                    JOptionPane.showMessageDialog(HoaDonPanel.this,
                            "Không thể tải dữ liệu hóa đơn:\n" + loi(err),
                            "Lỗi truy vấn", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void hienThiBang() {
        vungDanhSach.removeAll();
        if (duLieu.isEmpty()) {
            JPanel empty = trong(new GridBagLayout());
            empty.setBackground(new Color(248, 250, 255));
            empty.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
            JPanel body = trong(new GridLayout(3, 1, 0, 4));
            JLabel paw = chu("🐾", 28, NAVY, true);
            paw.setHorizontalAlignment(SwingConstants.CENTER);
            body.add(paw);
            JLabel t = chu("Chưa có hóa đơn", 14, NAVY, true);
            t.setHorizontalAlignment(SwingConstants.CENTER);
            body.add(t);
            JLabel c = chu("Tạo hóa đơn từ Bán hàng POS hoặc thay đổi bộ lọc", 12, MUTED, false);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            body.add(c);
            empty.add(body);
            vungDanhSach.add(empty, BorderLayout.CENTER);
        } else {
            JScrollPane scroll = new JScrollPane(bang);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER));
            scroll.getViewport().setBackground(WHITE);
            scroll.getVerticalScrollBar().setUnitIncrement(20);
            vungDanhSach.add(scroll, BorderLayout.CENTER);
        }
        vungDanhSach.revalidate();
        vungDanhSach.repaint();
    }

    private String maTrangThai() {
        return switch (locTrangThai.getSelectedIndex()) {
            case 0 -> "DA_THANH_TOAN";
            case 2 -> "CHO_THANH_TOAN";
            case 3 -> "DANG_DOI_SOAT";
            case 4 -> "HUY";
            default -> "";
        };
    }

    private void xemChiTiet(String maHoaDon) {
        trangThai.setText("Đang tải chi tiết " + maHoaDon + "...");
        new SwingWorker<HoaDonChiTiet, Void>() {
            @Override protected HoaDonChiTiet doInBackground() throws Exception {
                return service.chiTiet(maHoaDon);
            }
            @Override protected void done() {
                try {
                    HoaDonChiTiet item = get();
                    moHopThoai(item);
                    trangThai.setText("Đã xem " + maHoaDon);
                } catch (InterruptedException err) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException err) {
                    JOptionPane.showMessageDialog(HoaDonPanel.this,
                            "Không thể xem hóa đơn:\n" + loi(err),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void moHopThoai(HoaDonChiTiet h) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Chi tiết hóa đơn · " + h.maHoaDon(),
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0, 12));
        dialog.getContentPane().setBackground(WHITE);
        JTextArea receipt = new JTextArea(service.noiDungHoaDon(h));
        receipt.setEditable(false);
        receipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        receipt.setForeground(NAVY);
        receipt.setBackground(WHITE);
        receipt.setMargin(new Insets(18, 20, 18, 20));
        JScrollPane scroll = new JScrollPane(receipt);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel actions = trong(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actions.setBorder(new EmptyBorder(5, 15, 12, 15));
        JButton close = nut("Đóng", WHITE, NAVY);
        JButton print = nut("In hóa đơn", BLUE, WHITE);
        close.addActionListener(e -> dialog.dispose());
        print.addActionListener(e -> {
            try {
                receipt.print(null, null, true, null, null, true);
            } catch (java.awt.print.PrinterException error) {
                JOptionPane.showMessageDialog(dialog, "Không thể in: " + error.getMessage());
            }
        });
        actions.add(close);
        actions.add(print);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setSize(665, 700);
        dialog.setMinimumSize(new Dimension(520, 480));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void xuatCsv() {
        if (duLieu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Danh sách đang trống, chưa có dữ liệu để xuất.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("TiTiPet_HoaDon_" + java.time.LocalDate.now() + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) {
            target = new File(target.getParentFile(), target.getName() + ".csv");
        }
        if (target.exists() && JOptionPane.showConfirmDialog(this,
                "Tệp đã có. Bạn muốn ghi đè?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        final File destination = target;
        final List<HoaDonTomTat> snapshot = new ArrayList<>(duLieu);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                service.xuatCsv(snapshot, destination.toPath());
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(HoaDonPanel.this,
                            "Đã xuất " + snapshot.size() + " hóa đơn vào:\n" + destination);
                } catch (InterruptedException err) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException err) {
                    JOptionPane.showMessageDialog(HoaDonPanel.this,
                            "Không thể xuất CSV: " + loi(err));
                }
            }
        }.execute();
    }

    private static String loi(ExecutionException e) {
        Throwable cause = e.getCause();
        return cause == null ? e.getMessage() : cause.getMessage();
    }

    private static JPanel trong(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static JLabel chu(String text, int size, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    private static JButton nut(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)));
        return button;
    }
}
