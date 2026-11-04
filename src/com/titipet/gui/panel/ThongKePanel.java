package com.titipet.gui.panel;

import com.titipet.bus.AuthService;
import com.titipet.bus.ThongKeService;
import com.titipet.dao.ThongKeDAO;
import com.titipet.dao.ThongKeDAO.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * UC Thống kê - Quản lý: giao diện dựa trên reports.html của Nhóm 06.
 * Không truy vấn SQL trong GUI; dữ liệu lấy từ ThongKeService -> ThongKeDAO.
 */
public final class ThongKePanel extends JPanel {
    private static final Color BG = new Color(245, 248, 254);
    private static final Color WHITE = Color.WHITE;
    private static final Color NAVY = new Color(27, 62, 110);
    private static final Color BLUE = new Color(39, 104, 207);
    private static final Color MUTED = new Color(127, 147, 177);
    private static final Color BORDER = new Color(226, 235, 248);
    private static final DateTimeFormatter VN_DATE = DateTimeFormatter
            .ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter VN_DATETIME = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm");
    private static final java.text.NumberFormat VND =
            java.text.NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    private final ThongKeService service;
    private final JTextField tuNgay = new JTextField(10);
    private final JTextField denNgay = new JTextField(10);
    private final JButton taiLai = nut("Xem báo cáo", BLUE, WHITE);
    private final JLabel trangThai = chu("Đang tải dữ liệu SQL Server...", 12, MUTED, false);
    private final JLabel[] soThe = new JLabel[4];
    private final JLabel[] ghiChuThe = new JLabel[4];
    private final BieuDoCot bieuDo = new BieuDoCot();
    private final JPanel theoKenh = trong(new GridLayout(0, 1, 0, 12));
    private final JPanel deXuat = cot();
    private final DefaultTableModel modelCa = new DefaultTableModel(
            new String[] {"Mã ca", "Thu ngân", "Quỹ đầu ca", "Tiền mặt thực đếm",
                    "Chênh lệch", "Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final DefaultTableModel modelHang = new DefaultTableModel(
            new String[] {"Sản phẩm", "SL bán", "Doanh số trước ưu đãi"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final DefaultTableModel modelSpa = new DefaultTableModel(
            new String[] {"Dịch vụ Spa", "Lượt", "Doanh số trước ưu đãi"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable bangCa = taoBang(modelCa);
    private final JTable bangHang = taoBang(modelHang);
    private final JTable bangSpa = taoBang(modelSpa);
    private SwingWorker<ThongKeService.BaoCao, Void> dangTai;
    private ThongKeService.BaoCao baoCaoHienTai;

    public ThongKePanel(AuthService authService) {
        this(new ThongKeService(authService));
    }

    private ThongKePanel(ThongKeService service) {
        this.service = java.util.Objects.requireNonNull(service);
        setBackground(BG);
        setLayout(new BorderLayout());
        LocalDate homNay = LocalDate.now();
        tuNgay.setText(homNay.minusDays(6).format(VN_DATE));
        denNgay.setText(homNay.format(VN_DATE));
        taoGiaoDien();
        SwingUtilities.invokeLater(this::taiDuLieu);
    }

    private void taoGiaoDien() {
        JPanel noiDung = cot();
        noiDung.setBorder(new EmptyBorder(24, 26, 35, 26));
        noiDung.add(tieuDe());
        noiDung.add(khoang(17));
        noiDung.add(thanhLoc());
        noiDung.add(khoang(17));
        noiDung.add(cacThe());
        noiDung.add(khoang(18));
        noiDung.add(hangBieuDo());
        noiDung.add(khoang(18));
        noiDung.add(hangBangXepHang());
        noiDung.add(khoang(18));
        noiDung.add(khungPhieu());
        noiDung.add(khoang(18));
        noiDung.add(khungDeXuat());
        noiDung.add(khoang(14));
        noiDung.add(chu("Lưu ý: Doanh thu thuần = tiền hóa đơn đã thanh toán trừ tiền hoàn trả hàng "
                + "trong kỳ; không phải lợi nhuận. Các đề xuất là gợi ý theo quy tắc, quản lý cần xác minh thực tế.",
                11, MUTED, false));
        noiDung.add(khoang(10));
        noiDung.add(trangThai);

        JScrollPane cuon = new JScrollPane(noiDung);
        cuon.setBorder(null);
        cuon.getViewport().setBackground(BG);
        cuon.getVerticalScrollBar().setUnitIncrement(20);
        cuon.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(cuon, BorderLayout.CENTER);
    }

    private JPanel tieuDe() {
        JPanel p = trong(new BorderLayout(14, 0));
        JPanel ten = cot();
        ten.add(chu("TI TI PET • WORKSPACE", 10, BLUE, true));
        ten.add(khoang(6));
        ten.add(chu("Thống kê và báo cáo", 27, NAVY, true));
        ten.add(khoang(4));
        ten.add(chu("Theo dõi doanh thu, hóa đơn, ca làm việc và khuyến nghị cho cửa hàng",
                12, MUTED, false));
        p.add(ten, BorderLayout.WEST);
        JLabel quyen = chu("QUẢN LÝ  •  Dữ liệu SQL Server", 11, BLUE, true);
        quyen.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(quyen, BorderLayout.EAST);
        return p;
    }

    private JPanel thanhLoc() {
        JPanel p = the(new BorderLayout(12, 8));
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        JPanel form = trong(new FlowLayout(FlowLayout.LEFT, 9, 0));
        form.add(chu("Từ ngày", 12, NAVY, true));
        dinhDangO(tuNgay);
        dinhDangO(denNgay);
        form.add(tuNgay);
        form.add(chu("Đến ngày", 12, NAVY, true));
        form.add(denNgay);
        JButton bay = nut("7 ngày", new Color(229, 240, 255), BLUE);
        JButton baMuoi = nut("30 ngày", new Color(229, 240, 255), BLUE);
        bay.addActionListener(e -> chonNhanh(7));
        baMuoi.addActionListener(e -> chonNhanh(30));
        form.add(bay);
        form.add(baMuoi);
        form.add(taiLai);
        taiLai.addActionListener(e -> taiDuLieu());
        tuNgay.addActionListener(e -> taiDuLieu());
        denNgay.addActionListener(e -> taiDuLieu());
        p.add(form, BorderLayout.CENTER);
        return p;
    }

    private void chonNhanh(int soNgay) {
        LocalDate homNay = LocalDate.now();
        tuNgay.setText(homNay.minusDays(soNgay - 1).format(VN_DATE));
        denNgay.setText(homNay.format(VN_DATE));
        taiDuLieu();
    }

    private JPanel cacThe() {
        JPanel p = trong(new GridLayout(1, 4, 14, 0));
        String[] ten = {"Tổng doanh thu thuần", "Doanh số dịch vụ Spa",
                "Tiền mặt thuần", "Hóa đơn đã thanh toán"};
        String[] icon = {" đ", " đ", "₫", " đ"};
        for (int i = 0; i < ten.length; i++) {
            JPanel card = the(new BorderLayout(0, 8));
            card.setBorder(new EmptyBorder(20, 20, 20, 20));
            JLabel dau = chu(icon[i], 21, BLUE, true);
            card.add(dau, BorderLayout.NORTH);
            JPanel than = cot();
            soThe[i] = chu("—", 25, NAVY, true);
            ghiChuThe[i] = chu("Đang tải", 11, MUTED, false);
            than.add(soThe[i]);
            than.add(khoang(4));
            than.add(chu(ten[i], 12, NAVY, true));
            than.add(ghiChuThe[i]);
            card.add(than, BorderLayout.CENTER);
            p.add(card);
        }
        return p;
    }

    private JPanel hangBieuDo() {
        JPanel p = trong(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 9);
        gbc.gridx = 0;
        gbc.weightx = 0.64;
        JPanel left = khung("Doanh thu trong 7 ngày cuối của khoảng lọc",
                "Tiền hóa đơn trừ hoàn trả hàng theo ngày phát sinh");
        JPanel chartSection = trong(new BorderLayout(0, 10));
        bieuDo.setPreferredSize(new Dimension(400, 235));
        chartSection.add(bieuDo, BorderLayout.CENTER);
        JButton xuat = nut("Xuất báo cáo CSV", new Color(230, 241, 255), BLUE);
        xuat.addActionListener(e -> xuatBaoCao());
        JPanel row = trong(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row.add(xuat);
        chartSection.add(row, BorderLayout.SOUTH);
        left.add(chartSection, BorderLayout.CENTER);
        p.add(left, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.36;
        gbc.insets = new Insets(0, 9, 0, 0);
        JPanel right = khung("Phân loại doanh thu", "Kênh bán hàng và thông tin giảm giá");
        right.add(theoKenh, BorderLayout.CENTER);
        p.add(right, gbc);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 370));
        return p;
    }

    private JPanel hangBangXepHang() {
        JPanel p = trong(new GridLayout(1, 2, 16, 0));
        JPanel hang = khung("Sản phẩm bán chạy", "Theo số lượng, chưa trừ đổi/trả");
        hang.add(taoCuonBang(bangHang, 220), BorderLayout.CENTER);
        JPanel spa = khung("Dịch vụ Spa được chọn nhiều", "Theo lượt đã thanh toán");
        spa.add(taoCuonBang(bangSpa, 220), BorderLayout.CENTER);
        p.add(hang);
        p.add(spa);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));
        return p;
    }

    private JPanel khungPhieu() {
        JPanel p = khung("Thống kê ca làm việc và lịch sử chốt ca",
                "Chênh lệch = tiền mặt thực đếm - (quỹ đầu ca + thu tiền mặt - hoàn tiền mặt)");
        JButton xuat = nut("Xuất đối soát CSV", new Color(230, 241, 255), BLUE);
        xuat.addActionListener(e -> xuatDoiSoat());
        JPanel b = trong(new BorderLayout(0, 12));
        JPanel toolbar = trong(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toolbar.add(xuat);
        b.add(toolbar, BorderLayout.NORTH);
        b.add(taoCuonBang(bangCa, 235), BorderLayout.CENTER);
        p.add(b, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 365));
        return p;
    }

    private JPanel khungDeXuat() {
        JPanel p = khung("Đề xuất hành động cho cửa hàng",
                "Phát hiện tự động theo số liệu thực tế; không thay thế quyết định của quản lý");
        deXuat.setBorder(new EmptyBorder(3, 0, 0, 0));
        deXuat.add(chu("Đang phân tích số liệu...", 12, MUTED, false));
        p.add(deXuat, BorderLayout.CENTER);
        return p;
    }

    /** Gọi khi mở lại menu để báo cáo cập nhật mà không khóa giao diện. */
    public void taiDuLieu() {
        if (dangTai != null && !dangTai.isDone()) return;
        LocalDate tu, den;
        try {
            tu = LocalDate.parse(tuNgay.getText().trim(), VN_DATE);
            den = LocalDate.parse(denNgay.getText().trim(), VN_DATE);
            if (tu.isAfter(den)) throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Nhập ngày theo định dạng dd/MM/yyyy và bảo đảm khoảng ngày hợp lệ.",
                    "Ngày thống kê không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }
        taiLai.setEnabled(false);
        trangThai.setForeground(MUTED);
        trangThai.setText("Đang đọc dữ liệu SQL Server...");
        dangTai = new SwingWorker<>() {
            @Override protected ThongKeService.BaoCao doInBackground() {
                return service.layBaoCao(tu, den);
            }
            @Override protected void done() {
                taiLai.setEnabled(true);
                try {
                    hienThi(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    hienThiLoi("Đã gián đoạn tải báo cáo.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    hienThiLoi(cause == null ? ex.getMessage() : cause.getMessage());
                }
            }
        };
        dangTai.execute();
    }

    private void hienThiLoi(String thongBao) {
        trangThai.setForeground(new Color(180, 55, 55));
        trangThai.setText("Không tải được báo cáo: " + thongBao);
        JOptionPane.showMessageDialog(this, thongBao, "Lỗi thống kê", JOptionPane.ERROR_MESSAGE);
    }

    private void hienThi(ThongKeService.BaoCao baoCao) {
        baoCaoHienTai = baoCao;
        DuLieu d = baoCao.duLieu();
        TongQuan q = d.tongQuan();
        soThe[0].setText(vnd(q.doanhThuThuan()));
        soThe[1].setText(vnd(q.doanhSoSpa()));
        soThe[2].setText(vnd(q.tienMatThuan()));
        soThe[3].setText(Long.toString(q.soHoaDon()));
        ghiChuThe[0].setText("Đã trừ " + vnd(q.tienHoan()) + " hoàn tiền");
        ghiChuThe[1].setText("Trước phân bổ ưu đãi/thuế");
        ghiChuThe[2].setText("Đã trừ hoàn tiền mặt");
        ghiChuThe[3].setText("Trong khoảng đã chọn");
        bieuDo.setNgay(d.bayNgay());

        theoKenh.removeAll();
        for (Kenh kenh : d.theoKenh()) {
            theoKenh.add(hangSoLieu(kenh.tenKenh(), vnd(kenh.thuan())));
        }
        theoKenh.add(new JSeparator());
        theoKenh.add(hangSoLieu("Doanh số sản phẩm (gốc)", vnd(q.doanhSoHang())));
        theoKenh.add(hangSoLieu("Khuyến mãi / ưu đãi đã áp dụng", vnd(q.tongUuDai())));
        theoKenh.add(hangSoLieu("Tổng hoàn tiền", vnd(q.tienHoan())));
        theoKenh.revalidate();
        theoKenh.repaint();

        modelCa.setRowCount(0);
        for (PhieuCa ca : d.phieuCa()) {
            modelCa.addRow(new Object[] {ca.maCa(), ca.thuNgan(), vnd(ca.quyDauCa()),
                    vnd(ca.tienMatThucDem()), vnd(ca.chenhLech()), ca.trangThai()});
        }
        modelHang.setRowCount(0);
        for (SanPhamBanChay sp : d.sanPhamBanChay()) {
            modelHang.addRow(new Object[] {sp.ten(), sp.soLuong(), vnd(sp.doanhSo())});
        }
        modelSpa.setRowCount(0);
        for (SpaBanChay spa : d.spaBanChay()) {
            modelSpa.addRow(new Object[] {spa.ten(), spa.soLan(), vnd(spa.doanhSo())});
        }
        deXuat.removeAll();
        for (ThongKeService.DeXuat item : baoCao.deXuat()) {
            JPanel card = trong(new BorderLayout(10, 5));
            card.setBackground(new Color(248, 251, 255));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    new EmptyBorder(12, 15, 12, 15)));
            JPanel text = cot();
            Color mau = "ƯU TIÊN".equals(item.mucDo())
                    ? new Color(174, 100, 18) : BLUE;
            text.add(chu(item.mucDo() + " · " + item.tieuDe(), 13, mau, true));
            text.add(khoang(6));
            text.add(doanVan(item.hanhDong(), NAVY));
            text.add(khoang(4));
            text.add(doanVan("Căn cứ: " + item.canCu(), MUTED));
            card.add(text, BorderLayout.CENTER);
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
            deXuat.add(card);
            deXuat.add(khoang(9));
        }
        deXuat.revalidate();
        deXuat.repaint();
        trangThai.setForeground(MUTED);
        trangThai.setText("Đã cập nhật " + baoCao.tuNgay().format(VN_DATE)
                + " – " + baoCao.denNgay().format(VN_DATE)
                + ". Báo cáo chỉ đọc và chỉ dành cho Quản lý.");
    }

    private void xuatBaoCao() {
        if (baoCaoHienTai == null) {
            JOptionPane.showMessageDialog(this, "Hãy tải báo cáo trước khi xuất CSV.");
            return;
        }
        Path path = chonFile("TiTiPet_ThongKe.csv");
        if (path == null) return;
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write('\ufeff');
            ThongKeService.BaoCao b = baoCaoHienTai;
            DuLieu d = b.duLieu();
            TongQuan q = d.tongQuan();
            csv(w, "BÁO CÁO THỐNG KÊ TITI PET");
            csv(w, "Từ ngày", b.tuNgay().toString(), "Đến ngày", b.denNgay().toString());
            csv(w, "Chỉ số", "Giá trị VND / số lượng");
            csv(w, "Tiền hóa đơn đã thanh toán", q.thuHoaDon());
            csv(w, "Tiền hoàn trả hàng", q.tienHoan());
            csv(w, "Doanh thu thuần sau hoàn", q.doanhThuThuan());
            csv(w, "Doanh số Spa trước ưu đãi", q.doanhSoSpa());
            csv(w, "Doanh số hàng trước ưu đãi", q.doanhSoHang());
            csv(w, "Tiền mặt thuần", q.tienMatThuan());
            csv(w, "Ưu đãi đã áp dụng", q.tongUuDai());
            csv(w, "Số hóa đơn", q.soHoaDon());
            csv(w);
            csv(w, "NGÀY", "TIỀN HÓA ĐƠN", "HOÀN TIỀN", "THUẦN");
            for (NgayDoanhThu ngay : d.bayNgay())
                csv(w, ngay.ngay(), ngay.thu(), ngay.hoan(), ngay.thuan());
            csv(w);
            csv(w, "KÊNH", "TIỀN HÓA ĐƠN", "HOÀN TIỀN", "THUẦN");
            for (Kenh k : d.theoKenh()) csv(w, k.tenKenh(), k.thu(), k.hoan(), k.thuan());
            csv(w);
            csv(w, "TOP SẢN PHẨM", "SL", "DOANH SỐ TRƯỚC ƯU ĐÃI");
            for (SanPhamBanChay x : d.sanPhamBanChay())
                csv(w, x.ten() + " (" + x.ma() + ")", x.soLuong(), x.doanhSo());
            csv(w);
            csv(w, "DỊCH VỤ SPA", "LƯỢT", "DOANH SỐ TRƯỚC ƯU ĐÃI");
            for (SpaBanChay x : d.spaBanChay())
                csv(w, x.ten() + " (" + x.ma() + ")", x.soLan(), x.doanhSo());
            csv(w);
            csv(w, "KHUYẾN NGHỊ", "HÀNH ĐỘNG", "CĂN CỨ");
            for (ThongKeService.DeXuat x : b.deXuat())
                csv(w, x.tieuDe(), x.hanhDong(), x.canCu());
            csv(w, "Lưu ý", "Doanh thu không phải lợi nhuận; doanh số SP/Spa chưa phân bổ ưu đãi.");
            JOptionPane.showMessageDialog(this, "Đã xuất báo cáo: " + path);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể xuất CSV: " + ex.getMessage(),
                    "Lỗi ghi tệp", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatDoiSoat() {
        if (baoCaoHienTai == null) {
            JOptionPane.showMessageDialog(this, "Hãy tải báo cáo trước khi xuất CSV.");
            return;
        }
        Path path = chonFile("TiTiPet_DoiSoatCa.csv");
        if (path == null) return;
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            w.write('\ufeff');
            csv(w, "Mã ca", "Thu ngân", "Ngày lập phiếu", "Quỹ đầu ca", "Thực đếm",
                    "Dự kiến tiền mặt", "Chênh lệch", "Trạng thái");
            for (PhieuCa ca : baoCaoHienTai.duLieu().phieuCa()) {
                csv(w, ca.maCa(), ca.thuNgan(), ca.ngayLap().format(VN_DATETIME),
                        ca.quyDauCa(), ca.tienMatThucDem(), ca.tienMatDuKien(),
                        ca.chenhLech(), ca.trangThai());
            }
            JOptionPane.showMessageDialog(this, "Đã xuất đối soát: " + path);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể xuất CSV: " + ex.getMessage(),
                    "Lỗi ghi tệp", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Path chonFile(String ten) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo cáo CSV (UTF-8)");
        chooser.setSelectedFile(new java.io.File(ten));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return null;
        Path file = chooser.getSelectedFile().toPath();
        if (Files.exists(file) && JOptionPane.showConfirmDialog(this,
                "Tệp đã tồn tại. Bạn có muốn ghi đè không?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return null;
        return file;
    }

    /** CSV được escape đầy đủ; chuỗi bắt đầu bằng công thức được vô hiệu hóa. */
    private static void csv(BufferedWriter w, Object... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) w.write(',');
            Object raw = values[i];
            String value = raw == null ? "" : raw.toString();
            if (raw instanceof String && !value.isBlank()) {
                char start = value.stripLeading().charAt(0);
                if (start == '=' || start == '+' || start == '-' || start == '@') value = "'" + value;
            }
            w.write('"');
            w.write(value.replace("\"", "\"\""));
            w.write('"');
        }
        w.newLine();
    }

    private JPanel hangSoLieu(String ten, String giaTri) {
        JPanel p = trong(new BorderLayout(8, 0));
        p.add(chu(ten, 12, MUTED, false), BorderLayout.WEST);
        p.add(chu(giaTri, 12, NAVY, true), BorderLayout.EAST);
        return p;
    }

    private JPanel khung(String tieuDe, String moTa) {
        JPanel p = the(new BorderLayout(0, 14));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel top = cot();
        top.add(chu(tieuDe, 16, NAVY, true));
        top.add(khoang(4));
        top.add(chu(moTa, 11, MUTED, false));
        p.add(top, BorderLayout.NORTH);
        return p;
    }

    private static JPanel the(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createLineBorder(BORDER));
        return p;
    }

    private static JPanel trong(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    private static JPanel cot() {
        JPanel p = trong(null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JLabel chu(String text, int size, Color color, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(color);
        return l;
    }

    private static JTextArea doanVan(String content, Color color) {
        JTextArea t = new JTextArea(content);
        t.setOpaque(false);
        t.setEditable(false);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setFocusable(false);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(color);
        t.setRows(2);
        return t;
    }

    private static JButton nut(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(9, 12, 9, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static Component khoang(int height) {
        return Box.createVerticalStrut(height);
    }

    private static void dinhDangO(JTextField t) {
        t.setColumns(10);
        t.setPreferredSize(new Dimension(112, 36));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setToolTipText("dd/MM/yyyy");
    }

    private static JTable taoBang(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(34);
        t.setGridColor(BORDER);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setBackground(new Color(246, 249, 254));
        t.getTableHeader().setForeground(NAVY);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        t.setAutoCreateRowSorter(true);
        return t;
    }

    private static JScrollPane taoCuonBang(JTable table, int height) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        scroll.setPreferredSize(new Dimension(320, height));
        return scroll;
    }

    private static String vnd(BigDecimal value) {
        return VND.format(value) + " đ";
    }

    /** Biểu đồ cột Java2D, không cần thư viện biểu đồ bên ngoài. */
    private static final class BieuDoCot extends JPanel {
        private List<NgayDoanhThu> days = List.of();
        BieuDoCot() { setOpaque(false); }
        void setNgay(List<NgayDoanhThu> data) {
            days = List.copyOf(data);
            repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int left = 22, top = 27;
                int w = Math.max(1, getWidth() - 35), h = Math.max(1, getHeight() - 66);
                BigDecimal max = BigDecimal.ZERO, min = BigDecimal.ZERO;
                for (NgayDoanhThu x : days) {
                    BigDecimal y = x.thuan();
                    if (y.compareTo(max) > 0) max = y;
                    if (y.compareTo(min) < 0) min = y;
                }
                if (max.signum() == 0 && min.signum() == 0) max = BigDecimal.ONE;
                double maxD = max.doubleValue(), minD = min.doubleValue();
                double scale = Math.max(1, maxD - minD);
                double zero = top + maxD / scale * h;
                g2.setColor(new Color(236, 243, 252));
                for (int i = 0; i <= 4; i++) {
                    int y = top + h * i / 4;
                    g2.drawLine(left, y, left + w, y);
                }
                g2.setColor(new Color(154, 177, 205));
                g2.drawLine(left, (int) zero, left + w, (int) zero);
                if (days.isEmpty()) {
                    g2.setColor(MUTED);
                    g2.drawString("Chưa có dữ liệu theo ngày", left + 18, top + 50);
                    return;
                }
                int step = w / days.size();
                int barW = Math.min(55, Math.max(8, step - 22));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                int n = 0;
                for (NgayDoanhThu x : days) {
                    double v = x.thuan().doubleValue();
                    int xPos = left + step * n + (step - barW) / 2;
                    double yVal = zero - v / scale * h;
                    int y = (int) Math.min(zero, yVal);
                    int height = Math.max(2, (int) Math.abs(zero - yVal));
                    g2.setColor(v < 0 ? new Color(213, 101, 90) : BLUE);
                    g2.fill(new RoundRectangle2D.Double(xPos, y, barW, height, 8, 8));
                    g2.setColor(NAVY);
                    String label = x.ngay().format(DateTimeFormatter.ofPattern("dd/MM"));
                    int textW = g2.getFontMetrics().stringWidth(label);
                    g2.drawString(label, xPos + (barW - textW) / 2, top + h + 21);
                    String val = compact(x.thuan());
                    int valW = g2.getFontMetrics().stringWidth(val);
                    int valY = v >= 0 ? Math.max(13, y - 7) : Math.min(top + h + 8, y + height + 12);
                    g2.drawString(val, xPos + (barW - valW) / 2, valY);
                    n++;
                }
            } finally {
                g2.dispose();
            }
        }
        private static String compact(BigDecimal v) {
            double a = v.doubleValue();
            if (Math.abs(a) >= 1_000_000) return String.format(Locale.US,"%.1ftr",a/1_000_000);
            if (Math.abs(a) >= 1_000) return String.format(Locale.US,"%.0fk",a/1_000);
            return Long.toString(v.longValue());
        }
    }
}
