
package com.titipet.gui.panel;

import com.titipet.bus.ChotCaService;
import com.titipet.dao.CaLamViecDAO.TongHopCa;
import com.titipet.dao.PhieuKetCaDAO.PhieuChoDuyet;
import com.titipet.entity.enums.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChotCaPanel extends JPanel {

    // Mau giao dien theo prototype TiTi Pet
    private static final Color BG = new Color(245, 248, 254);
    private static final Color WHITE = Color.WHITE;
    private static final Color NAVY = new Color(29, 65, 111);
    private static final Color BLUE = new Color(37, 95, 183);
    private static final Color MUTED = new Color(139, 158, 187);
    private static final Color LINE = new Color(228, 236, 247);
    private static final Color PALE_BLUE = new Color(231, 239, 255);
    private static final Color WARNING_BG = new Color(255, 242, 221);
    private static final Color WARNING_TEXT = new Color(164, 106, 33);

    private static final Font BODY =
            new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SEMIBOLD =
            new Font("Segoe UI", Font.BOLD, 13);

    // Service
    private final ChotCaService service = new ChotCaService();
    private final String maNV;
    private final VaiTro vaiTro;

    // Thanh thong bao
    private final JLabel thongBao =
            text("Đang tải dữ liệu...", 12, MUTED, Font.PLAIN);

    // Giao dien Thu ngan
    private final JLabel trangThaiBadge =
            text("Chưa có ca", 12, WARNING_TEXT, Font.BOLD);

    private final JLabel maCaLabel =
            text("Chưa có ca đang mở", 14, NAVY, Font.BOLD);

    private final JLabel nvLabel = moneyLabel();
    private final JLabel soHoaDonLabel =
            text("0 hóa đơn", 13, NAVY, Font.BOLD);

    private final JLabel dauCaLabel = moneyLabel();
    private final JLabel matLabel = moneyLabel();
    private final JLabel ckLabel = moneyLabel();
    private final JLabel sanLabel = moneyLabel();

    private final JLabel duKienLabel =
            text("—", 18, NAVY, Font.BOLD);

    private final JLabel chenhLechLabel =
            text("—", 15, NAVY, Font.BOLD);

    private final JTextField tienMatThucTe = new JTextField();

    private final JCheckBox checkTienMat =
            check("Đã kiểm đếm tiền mặt tại quầy");

    private final JCheckBox checkChuyenKhoan =
            check("Đã đối chiếu giao dịch chuyển khoản");

    private final JCheckBox checkHoaDon =
            check("Đã kiểm tra hóa đơn bán hàng và Spa");

    private final JCheckBox checkSaiLech =
            check("Không còn giao dịch chưa phân loại");

    private final JButton guiPhieu =
            primary("Gửi phiếu kết ca & chờ duyệt");

    private final JButton taiLai =
            secondary("Làm mới doanh thu");

    // Giao dien Quan ly
    private final DefaultListModel<PhieuChoDuyet> danhSach =
            new DefaultListModel<>();

    private final JList<PhieuChoDuyet> danhSachPhieu =
            new JList<>(danhSach);

    private final JLabel maPhieuChiTiet = moneyLabel();
    private final JLabel caPhieuChiTiet = moneyLabel();
    private final JLabel nvPhieuChiTiet = moneyLabel();
    private final JLabel tienMatChiTiet = moneyLabel();
    private final JLabel tienCKChiTiet = moneyLabel();
    private final JLabel ngayPhieuChiTiet = moneyLabel();

    private final JLabel soPhieuCho =
            text("0 phiếu chờ duyệt", 13, MUTED, Font.PLAIN);

    private final JButton duyet =
            primary("Phê duyệt kết ca");

    private final JButton tuChoi =
            secondary("Từ chối / yêu cầu kiểm tra");

    private TongHopCa caDangXem;

    private final JLabel loiKhongCoCa =
            text("", 12, WARNING_TEXT, Font.PLAIN);

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ChotCaPanel(String maNV, VaiTro vaiTro) {
        this.maNV = maNV;
        this.vaiTro = vaiTro;

        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(22, 26, 18, 26));

        add(header(), BorderLayout.NORTH);

        JPanel content;

        if (vaiTro == VaiTro.THU_NGAN) {
            content = contentThuNgan();
        } else if (vaiTro == VaiTro.QUAN_LY) {
            content = contentQuanLy();
        } else {
            content = card();
            content.add(
                    text(
                            "Tài khoản không có quyền sử dụng chức năng Mở / chốt ca.",
                            15, NAVY, Font.BOLD
                    ),
                    BorderLayout.NORTH
            );
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(thongBao, BorderLayout.WEST);
        footer.add(
                text(
                        "TiTi Pet • Quản lý ca làm việc • Dữ liệu từ SQL Server",
                        10,
                        new Color(179, 192, 212),
                        Font.PLAIN
                ),
                BorderLayout.EAST
        );

        add(footer, BorderLayout.SOUTH);

        if (maNV == null || maNV.isBlank() || vaiTro == null) {
            thongBao.setText(
                    "Chưa nhận được nhân viên và vai trò đăng nhập."
            );
            guiPhieu.setEnabled(false);
            duyet.setEnabled(false);
            tuChoi.setEnabled(false);

        } else if (vaiTro == VaiTro.THU_NGAN) {

            taiLai.addActionListener(e -> taiCa());
            guiPhieu.addActionListener(e -> guiPhieu());

            tienMatThucTe.getDocument().addDocumentListener(
                    new DocumentListener() {
                        public void insertUpdate(DocumentEvent e) {
                            tinhChenhLech();
                        }

                        public void removeUpdate(DocumentEvent e) {
                            tinhChenhLech();
                        }

                        public void changedUpdate(DocumentEvent e) {
                            tinhChenhLech();
                        }
                    }
            );

            taiCa();

        } else if (vaiTro == VaiTro.QUAN_LY) {

            danhSachPhieu.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    hienThiPhieu(danhSachPhieu.getSelectedValue());
                }
            });

            duyet.addActionListener(e -> xuLyPhieu(true));
            tuChoi.addActionListener(e -> xuLyPhieu(false));

            taiDanhSach();
        }
    }

    // =====================================================
    // HEADER
    // =====================================================

    private JPanel header() {
        JPanel root = new JPanel(new BorderLayout(8, 6));
        root.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel tag = text(
                "TITI PET • WORKSPACE",
                10, BLUE, Font.BOLD
        );
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = text(
                "Quản lý ca làm việc",
                27, NAVY, Font.BOLD
        );
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = text(
                "Kiểm tra số liệu, quỹ đầu ca và đối soát cuối ca",
                12, MUTED, Font.PLAIN
        );
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(tag);
        left.add(Box.createVerticalStrut(5));
        left.add(title);
        left.add(Box.createVerticalStrut(5));
        left.add(sub);

        root.add(left, BorderLayout.WEST);

        JPanel badges = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        badges.setOpaque(false);

        String date = LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );

        badges.add(pill(date, PALE_BLUE, NAVY));

        badges.add(
                pill(
                        vaiTro == VaiTro.QUAN_LY ? "Quản lý" : "Thu ngân",
                        WARNING_BG,
                        WARNING_TEXT
                )
        );

        badges.add(pill("Hướng dẫn sử dụng", WHITE, NAVY));

        root.add(badges, BorderLayout.EAST);

        root.setBorder(new EmptyBorder(0, 0, 4, 0));

        return root;
    }

    // =====================================================
    // MAN HINH THU NGAN
    // =====================================================

    private JPanel contentThuNgan() {

        JPanel left = card();

        left.add(
                sectionTitle(
                        "Thông tin ca & đối soát cuối ca",
                        "Số tiền được tổng hợp từ các hóa đơn đã thanh toán"
                ),
                BorderLayout.NORTH
        );

        JPanel fields = vertical(12);

        fields.add(labelAndValue("Mã ca đang làm", maCaLabel));

        fields.add(
                twoFields(
                        labelAndValue("Thu ngân tiếp nhận", nvLabel),
                        labelAndValue("Số lượng giao dịch", soHoaDonLabel)
                )
        );

        fields.add(divider());

        fields.add(
                twoFields(
                        labelAndValue("Tiền đầu ca", dauCaLabel),
                        labelAndValue("Doanh thu tiền mặt", matLabel)
                )
        );

        fields.add(
                twoFields(
                        labelAndValue("Chuyển khoản thuần (đã trừ hoàn)", ckLabel),
                        labelAndValue("Qua sàn", sanLabel)
                )
        );

        fields.add(divider());

        fields.add(
                labelAndValue("Tiền mặt dự kiến cuối ca", duKienLabel)
        );

        fields.add(
                labelAndValue(
                        "Tiền mặt kiểm đếm thực tế (VNĐ) *",
                        tienMatThucTe
                )
        );

        fields.add(
                labelAndValue("Chênh lệch sau đối soát", chenhLechLabel)
        );

        fields.add(loiKhongCoCa);
        fields.add(Box.createVerticalStrut(3));

        fields.add(guiPhieu);
        fields.add(taiLai);

        left.add(fields, BorderLayout.CENTER);

        // Cot phai - checklist
        JPanel checks = card();

        checks.add(
                sectionTitle(
                        "Kiểm tra đối soát cuối ca",
                        "Xác nhận các bước kiểm tra trước khi gửi phiếu"
                ),
                BorderLayout.NORTH
        );

        JPanel checksBody = vertical(10);

        checksBody.add(checkTienMat);
        checksBody.add(checkChuyenKhoan);
        checksBody.add(checkHoaDon);
        checksBody.add(checkSaiLech);

        checks.add(checksBody, BorderLayout.CENTER);

        // Lich su
        JPanel history = card();

        history.add(
                sectionTitle(
                        "Lịch sử & phiếu chờ duyệt",
                        "Trạng thái gần nhất của ca đang làm"
                ),
                BorderLayout.NORTH
        );

        JPanel historyBody = vertical(10);
        historyBody.add(trangThaiBadge);

        historyBody.add(
                text(
                        "Phiếu chờ duyệt sẽ được Quản lý xử lý tại chức năng này.",
                        12, MUTED, Font.PLAIN
                )
        );

        history.add(historyBody, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 14));
        right.setOpaque(false);

        right.add(checks, BorderLayout.NORTH);
        right.add(history, BorderLayout.CENTER);

        return columns(left, right);
    }

    // =====================================================
    // MAN HINH QUAN LY
    // =====================================================

    private JPanel contentQuanLy() {

        JPanel left = card();

        left.add(
                sectionTitle(
                        "Phiếu kết ca chờ phê duyệt",
                        "Danh sách lấy trực tiếp từ cơ sở dữ liệu"
                ),
                BorderLayout.NORTH
        );

        danhSachPhieu.setFont(BODY);
        danhSachPhieu.setBackground(WHITE);
        danhSachPhieu.setSelectionBackground(PALE_BLUE);
        danhSachPhieu.setSelectionForeground(NAVY);
        danhSachPhieu.setFixedCellHeight(58);

        danhSachPhieu.setCellRenderer(
                (list, value, index, selected, focus) -> {

                    JPanel line = new JPanel(new BorderLayout(8, 4));
                    line.setOpaque(true);
                    line.setBackground(selected ? PALE_BLUE : WHITE);
                    line.setBorder(new EmptyBorder(7, 12, 7, 12));

                    JLabel code = text(
                            value.maPhieu() + " • Ca: " + value.maCa(),
                            13, NAVY, Font.BOLD
                    );

                    JLabel info = text(
                            "Nhân viên: " + value.maNV() + " | " +
                            value.ngayLap().format(
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy HH:mm"
                                    )
                            ),
                            11, MUTED, Font.PLAIN
                    );

                    line.add(code, BorderLayout.NORTH);
                    line.add(info, BorderLayout.SOUTH);

                    return line;
                }
        );

        JScrollPane listScroll = new JScrollPane(danhSachPhieu);
        listScroll.setBorder(BorderFactory.createLineBorder(LINE));
        listScroll.setPreferredSize(new Dimension(400, 310));

        left.add(listScroll, BorderLayout.CENTER);

        JPanel leftFoot = new JPanel(new BorderLayout());
        leftFoot.setOpaque(false);
        leftFoot.setBorder(new EmptyBorder(12, 0, 0, 0));

        leftFoot.add(soPhieuCho, BorderLayout.WEST);

        JButton lamMoi = secondary("Làm mới danh sách");
        lamMoi.addActionListener(e -> taiDanhSach());

        leftFoot.add(lamMoi, BorderLayout.EAST);
        left.add(leftFoot, BorderLayout.SOUTH);

        // Chi tiet phieu
        JPanel detail = card();

        detail.add(
                sectionTitle(
                        "Kiểm tra thông tin phiếu kết ca",
                        "Chọn phiếu bên trái để xem chi tiết trước khi duyệt"
                ),
                BorderLayout.NORTH
        );

        JPanel fields = vertical(12);

        fields.add(
                twoFields(
                        labelAndValue("Mã phiếu", maPhieuChiTiet),
                        labelAndValue("Mã ca", caPhieuChiTiet)
                )
        );

        fields.add(
                labelAndValue("Nhân viên lập phiếu", nvPhieuChiTiet)
        );

        fields.add(divider());

        fields.add(
                twoFields(
                        labelAndValue("Tiền mặt thực tế", tienMatChiTiet),
                        labelAndValue("Doanh thu chuyển khoản", tienCKChiTiet)
                )
        );

        fields.add(
                labelAndValue("Thời gian gửi phiếu", ngayPhieuChiTiet)
        );

        fields.add(divider());
        fields.add(duyet);
        fields.add(tuChoi);

        detail.add(fields, BorderLayout.CENTER);

        duyet.setEnabled(false);
        tuChoi.setEnabled(false);

        JPanel history = card();

        history.add(
                sectionTitle(
                        "Lịch sử & ghi chú duyệt",
                        "Phiếu phê duyệt sẽ chuyển ca sang trạng thái ĐÃ CHỐT"
                ),
                BorderLayout.NORTH
        );

        history.add(
                text(
                        "Nếu từ chối, Thu ngân được kiểm tra lại và gửi lại phiếu.",
                        12, MUTED, Font.PLAIN
                ),
                BorderLayout.CENTER
        );

        JPanel right = new JPanel(new BorderLayout(0, 14));
        right.setOpaque(false);

        right.add(detail, BorderLayout.NORTH);
        right.add(history, BorderLayout.CENTER);

        return columns(left, right);
    }

    // =====================================================
    // TAI CA TU SQL SERVER
    // =====================================================

    private void taiCa() {
        guiPhieu.setEnabled(false);

        chayNen(
                () -> service.layCaHienTai(maNV).orElse(null),
                ca -> {
                    caDangXem = ca;

                    if (ca == null) {
                        maCaLabel.setText("Chưa có ca đang mở");
                        nvLabel.setText(maNV);
                        soHoaDonLabel.setText("0 hóa đơn");
                        dauCaLabel.setText("—");
                        matLabel.setText("—");
                        ckLabel.setText("—");
                        sanLabel.setText("—");
                        duKienLabel.setText("—");
                        chenhLechLabel.setText("—");
                        trangThaiBadge.setText("Chưa mở ca");

                        loiKhongCoCa.setText(
                                "Cần mở ca tại UC006 trước khi gửi phiếu."
                        );

                        thongBao.setText(
                                "Chưa có ca đang mở hoặc chờ xác nhận."
                        );
                        return;
                    }

                    maCaLabel.setText(ca.maCa());
                    nvLabel.setText(ca.maNV());

                    soHoaDonLabel.setText(
                            ca.soHoaDon() + " hóa đơn"
                    );

                    dauCaLabel.setText(tien(ca.tienDauCa()));
                    matLabel.setText(tien(ca.doanhThuTienMat()));
                    ckLabel.setText(tien(ca.doanhThuCKThuan()));
                    sanLabel.setText(tien(ca.doanhThuQuaSan()));
                    duKienLabel.setText(tien(ca.tienMatDuKien()));

                    trangThaiBadge.setText(
                            "Trạng thái: " + ca.trangThai()
                    );

                    if (ca.doanhThuQuaSan().signum() != 0) {
                        loiKhongCoCa.setText(
                                "Ca có doanh thu qua sàn; chưa hỗ trợ kết ca qua sàn."
                        );
                    } else if (ca.hoaDonChuaPhanLoai() > 0) {
                        loiKhongCoCa.setText(
                                "Có hóa đơn chưa xác định hình thức thanh toán."
                        );
                    } else if (ca.soHoaDon() == 0) {
                        loiKhongCoCa.setText(
                                "Chưa có hóa đơn đã thanh toán trong ca."
                        );
                    } else {
                        loiKhongCoCa.setText("");
                    }

                    tinhChenhLech();
                }
        );
    }

    // =====================================================
    // TINH CHENH LECH
    // =====================================================

    private void tinhChenhLech() {
        if (vaiTro != VaiTro.THU_NGAN || caDangXem == null) {
            return;
        }

        try {
            BigDecimal diff = docTien(tienMatThucTe.getText())
                    .subtract(caDangXem.tienMatDuKien());

            chenhLechLabel.setText(tien(diff));

            chenhLechLabel.setForeground(
                    diff.signum() == 0
                            ? new Color(34, 138, 100)
                            : WARNING_TEXT
            );

        } catch (IllegalArgumentException ex) {
            chenhLechLabel.setText("Nhập số tiền hợp lệ");
            chenhLechLabel.setForeground(WARNING_TEXT);
        }

        boolean ready =
                "DANG_MO".equals(caDangXem.trangThai())
                && caDangXem.soHoaDon() > 0
                && caDangXem.hoaDonChuaPhanLoai() == 0
                && caDangXem.doanhThuQuaSan().signum() == 0;

        if (ready) {
            try {
                ready = docTien(tienMatThucTe.getText())
                        .compareTo(caDangXem.tienMatDuKien()) == 0;
            } catch (IllegalArgumentException ex) {
                ready = false;
            }
        }

        ready = ready
                && checkTienMat.isSelected()
                && checkChuyenKhoan.isSelected()
                && checkHoaDon.isSelected()
                && checkSaiLech.isSelected();

        guiPhieu.setEnabled(ready);
    }

    // =====================================================
    // GUI PHIEU KET CA
    // =====================================================

    private void guiPhieu() {
        if (caDangXem == null) return;

        final BigDecimal thucTe;

        try {
            thucTe = docTien(tienMatThucTe.getText());
        } catch (IllegalArgumentException ex) {
            thongBao.setText(ex.getMessage());
            return;
        }

        int xacNhan = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận gửi phiếu kết ca " + caDangXem.maCa()
                        + " để Quản lý phê duyệt?"
                        + "\nTiền mặt kiểm đếm: " + tien(thucTe),
                "Xác nhận kết ca",
                JOptionPane.YES_NO_OPTION
        );

        if (xacNhan != JOptionPane.YES_OPTION) return;

        chayNen(
                () -> service.guiPhieu(maNV, thucTe),
                maPhieu -> {
                    JOptionPane.showMessageDialog(
                            this,
                            "Đã gửi phiếu " + maPhieu
                                    + ".\nCa chuyển sang CHỜ XÁC NHẬN."
                                    + "\nChưa phải trạng thái ĐÃ CHỐT."
                    );
                    taiCa();
                }
        );
    }

    // =====================================================
    // DANH SACH PHIEU CHO DUYET
    // =====================================================

    private void taiDanhSach() {
        chayNen(
                service::layPhieuChoDuyet,
                ds -> {
                    danhSach.clear();

                    for (PhieuChoDuyet p : ds) {
                        danhSach.addElement(p);
                    }

                    soPhieuCho.setText(
                            ds.size() + " phiếu chờ duyệt"
                    );

                    hienThiPhieu(null);

                    thongBao.setText(
                            "Đã tải " + ds.size() + " phiếu chờ duyệt."
                    );
                }
        );
    }

    private void hienThiPhieu(PhieuChoDuyet p) {
        if (p == null) {
            maPhieuChiTiet.setText("—");
            caPhieuChiTiet.setText("—");
            nvPhieuChiTiet.setText("—");
            tienMatChiTiet.setText("—");
            tienCKChiTiet.setText("—");
            ngayPhieuChiTiet.setText("—");

            duyet.setEnabled(false);
            tuChoi.setEnabled(false);

        } else {
            maPhieuChiTiet.setText(p.maPhieu());
            caPhieuChiTiet.setText(p.maCa());
            nvPhieuChiTiet.setText(p.maNV());

            tienMatChiTiet.setText(tien(p.tienMat()));
            tienCKChiTiet.setText(tien(p.tienCK()));

            ngayPhieuChiTiet.setText(
                    p.ngayLap().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    )
            );

            duyet.setEnabled(true);
            tuChoi.setEnabled(true);
        }
    }

    // =====================================================
    // QUAN LY DUYET / TU CHOI
    // =====================================================

    private void xuLyPhieu(boolean dongY) {
        PhieuChoDuyet p = danhSachPhieu.getSelectedValue();

        if (p == null) {
            thongBao.setText("Chọn một phiếu trước khi xử lý.");
            return;
        }

        String action = dongY ? "PHÊ DUYỆT" : "TỪ CHỐI";

        String message =
                "Phiếu: " + p.maPhieu()
                        + "\nCa: " + p.maCa()
                        + "\nNhân viên: " + p.maNV()
                        + "\nTiền mặt: " + tien(p.tienMat())
                        + "\nChuyển khoản: " + tien(p.tienCK())
                        + "\n\nXác nhận " + action + "?";

        int confirm = JOptionPane.showConfirmDialog(
                this,
                message,
                "UC007 – " + action,
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        String lyDo = "";

        if (!dongY) {
            lyDo = JOptionPane.showInputDialog(
                    this,
                    "Nhập lý do từ chối / yêu cầu kiểm tra lại:"
            );

            if (lyDo == null || lyDo.isBlank()) return;
        }

        final String lyDoGui = lyDo;

        chayNen(
                () -> {
                    service.duyet(
                            p.maPhieu(), maNV, dongY, lyDoGui
                    );
                    return Boolean.TRUE;
                },
                ok -> {
                    JOptionPane.showMessageDialog(
                            this,
                            "Đã " + action.toLowerCase() + " phiếu."
                    );

                    taiDanhSach();
                }
        );
    }

    // =====================================================
    // SWING WORKER
    // =====================================================

    private <T> void chayNen(
            Supplier<T> action,
            Consumer<T> done
    ) {
        thongBao.setText("Đang xử lý...");

        new SwingWorker<T, Void>() {

            @Override
            protected T doInBackground() {
                return action.get();
            }

            @Override
            protected void done() {
                try {
                    done.accept(get());

                    if (thongBao.getText().equals("Đang xử lý...")) {
                        thongBao.setText("Đã cập nhật.");
                    }

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    thongBao.setText("Thao tác bị gián đoạn.");

                } catch (ExecutionException ex) {
                    Throwable root = ex.getCause();

                    String message = root == null
                            ? "Lỗi không xác định"
                            : root.getMessage();

                    thongBao.setText(message);

                    JOptionPane.showMessageDialog(
                            ChotCaPanel.this,
                            message,
                            "Lỗi UC007",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        }.execute();
    }

    // =====================================================
    // CAC HAM TAO GIAO DIEN
    // =====================================================

    private static JPanel columns(JPanel left, JPanel right) {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();

        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 0.56;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(0, 0, 0, 14);

        body.add(left, g);

        g.gridx = 1;
        g.weightx = 0.44;
        g.insets = new Insets(0, 0, 0, 0);

        body.add(right, g);

        return body;
    }

    private static JPanel card() {
        JPanel panel = new RoundedCard();
        panel.setLayout(new BorderLayout(0, 18));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    private static JPanel vertical(int gap) {
        JPanel p = new JPanel(new StackLayout(gap));
        p.setOpaque(false);
        return p;
    }

    private static class StackLayout implements LayoutManager {
        private final int gap;

        StackLayout(int gap) {
            this.gap = gap;
        }

        public void addLayoutComponent(
                String name, Component comp
        ) {}

        public void removeLayoutComponent(Component comp) {}

        public Dimension preferredLayoutSize(Container parent) {
            int width = 0;
            int height = 0;
            int shown = 0;

            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) continue;

                Dimension d = c.getPreferredSize();
                width = Math.max(width, d.width);
                height += d.height;
                shown++;
            }

            return new Dimension(
                    width,
                    height + Math.max(0, shown - 1) * gap
            );
        }

        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();

            int y = in.top;
            int width = Math.max(
                    1,
                    parent.getWidth() - in.left - in.right
            );

            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) continue;

                int height = c.getPreferredSize().height;
                c.setBounds(in.left, y, width, height);
                y += height + gap;
            }
        }
    }

    private static JPanel sectionTitle(
            String title,
            String sub
    ) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel first = text(title, 16, NAVY, Font.BOLD);
        first.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel second = text(sub, 11, MUTED, Font.PLAIN);
        second.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(first);
        p.add(Box.createVerticalStrut(4));
        p.add(second);

        return p;
    }

    private static JPanel labelAndValue(
            String labelText,
            JComponent value
    ) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel name = text(
                labelText,
                11,
                new Color(84, 109, 145),
                Font.BOLD
        );

        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(name);
        p.add(Box.createVerticalStrut(6));

        value.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (value instanceof JTextField field) {
            field.setFont(BODY);
            field.setForeground(NAVY);
            field.setBackground(WHITE);

            field.setPreferredSize(new Dimension(200, 39));

            field.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 39)
            );

            field.putClientProperty(
                    "JTextField.placeholderText",
                    "Nhập số tiền thực tế..."
            );

            field.putClientProperty(
                    "FlatLaf.style",
                    "arc: 10; borderWidth: 1; " +
                    "borderColor: #DDE8F6; margin: 8,12,8,12"
            );

        } else {
            value.setPreferredSize(new Dimension(200, 29));

            value.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 29)
            );
        }

        p.add(value);
        p.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 63)
        );

        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        return p;
    }

    private static JPanel twoFields(
            JPanel left,
            JPanel right
    ) {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);

        row.add(left);
        row.add(right);

        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 65)
        );

        return row;
    }

    private static JComponent divider() {
        JSeparator separator = new JSeparator();
        separator.setForeground(LINE);

        separator.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 1)
        );

        return separator;
    }

    private static JLabel text(
            String value,
            int size,
            Color color,
            int style
    ) {
        JLabel label = new JLabel(value);

        label.setFont(
                new Font("Segoe UI", style, size)
        );

        label.setForeground(color);

        return label;
    }

    private static JLabel moneyLabel() {
        return text("—", 14, NAVY, Font.BOLD);
    }

    private static JCheckBox check(String content) {
        JCheckBox box = new JCheckBox(content);

        box.setFont(BODY);
        box.setForeground(new Color(75, 94, 123));
        box.setBackground(WHITE);
        box.setFocusPainted(false);

        box.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LINE),
                        new EmptyBorder(10, 12, 10, 12)
                )
        );

        box.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 43)
        );

        box.setPreferredSize(new Dimension(280, 43));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.addActionListener(e -> {
            ChotCaPanel owner = (ChotCaPanel)
                    SwingUtilities.getAncestorOfClass(
                            ChotCaPanel.class, box
                    );

            if (owner != null) {
                owner.tinhChenhLech();
            }
        });

        return box;
    }

    private static JButton primary(String title) {
        JButton b = new JButton(title);

        b.setFont(SEMIBOLD);
        b.setBackground(BLUE);
        b.setForeground(WHITE);
        b.setFocusPainted(false);

        b.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        b.putClientProperty(
                "FlatLaf.style",
                "arc: 12; borderWidth: 0; " +
                "focusWidth: 0; margin: 10,18,10,18;"
        );

        b.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 43)
        );

        b.setAlignmentX(Component.LEFT_ALIGNMENT);

        return b;
    }

    private static JButton secondary(String title) {
        JButton b = new JButton(title);

        b.setFont(SEMIBOLD);
        b.setForeground(NAVY);
        b.setBackground(PALE_BLUE);
        b.setFocusPainted(false);

        b.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        b.putClientProperty(
                "FlatLaf.style",
                "arc: 12; borderWidth: 0; " +
                "focusWidth: 0; margin: 8,14,8,14;"
        );

        b.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 40)
        );

        b.setAlignmentX(Component.LEFT_ALIGNMENT);

        return b;
    }

    private static JLabel pill(
            String label,
            Color bg,
            Color fg
    ) {
        JLabel p = text(label, 11, fg, Font.BOLD);

        p.setOpaque(true);
        p.setBackground(bg);
        p.setBorder(new EmptyBorder(8, 12, 8, 12));

        return p;
    }

    private static String tien(BigDecimal amount) {
        if (amount == null) return "—";

        return NumberFormat.getNumberInstance(
                Locale.forLanguageTag("vi-VN")
        ).format(amount) + " đ";
    }

    private static BigDecimal docTien(String raw) {
        String value = raw == null
                ? ""
                : raw.strip()
                     .replace(".", "")
                     .replace(" ", "");

        if (!value.matches("[0-9]+")) {
            throw new IllegalArgumentException(
                    "Số tiền phải là số nguyên không âm."
            );
        }

        return new BigDecimal(value);
    }

    private static class RoundedCard extends JPanel {

        RoundedCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(WHITE);
            g2.fillRoundRect(
                    1, 1,
                    getWidth() - 3,
                    getHeight() - 3,
                    28, 28
            );

            g2.setColor(LINE);
            g2.drawRoundRect(
                    1, 1,
                    getWidth() - 3,
                    getHeight() - 3,
                    28, 28
            );

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
