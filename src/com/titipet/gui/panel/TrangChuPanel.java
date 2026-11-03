package com.titipet.gui.panel;

import com.titipet.bus.AuthService;
import com.titipet.bus.TrangChuService;
import com.titipet.dao.TrangChuDAO;
import com.titipet.entity.NhanVien;
import com.titipet.entity.enums.VaiTro;
import com.titipet.gui.MainFrame;
import com.titipet.gui.PosUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/** Màn hình Trang chủ: hiển thị dữ liệu từ TrangChuService và điều hướng bằng mã menu. */
public class TrangChuPanel extends JPanel {
    private static final Color BG = PosUI.BG;
    private static final Color NAVY = PosUI.NAVY;
    private static final Color MUTED = PosUI.MUTED;
    private static final Color BLUE = PosUI.BLUE;
    private static final Color BORDER = PosUI.BORDER;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final java.text.NumberFormat MONEY =
            java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("vi-VN"));

    private final TrangChuService service;
    private final VaiTro vaiTro;
    private final Consumer<String> chonMenu;
    private final JLabel ngay = label("", 12, MUTED, false);
    private final JLabel trangThaiCa = label("Đang tải trạng thái ca...", 12, MUTED, true);
    private final JLabel thongBao = label("", 12, MUTED, false);
    private final JLabel[] giaTriThe = new JLabel[4];
    private final JLabel[] ghiChuThe = new JLabel[4];
    private final JPanel danhSachLich = danhSach();
    private final JPanel danhSachCanhBao = danhSach();
    private final JPanel danhSachHoaDon = danhSach();
    private SwingWorker<TrangChuService.DuLieuTrangChu, Void> tacVuTai;

    /** Nhận cùng phiên đăng nhập với MainFrame để Service tự lấy đúng mã và vai trò. */
    public TrangChuPanel(AuthService authService, String tenNhanVien, Consumer<String> chonMenu) {
        Objects.requireNonNull(authService, "Cần phiên đăng nhập để mở Trang chủ.");
        this.vaiTro = authService.getNhanVienDangNhap()
                .map(NhanVien::getVaiTro)
                .orElseThrow(() -> new IllegalStateException("Chưa có vai trò đăng nhập."));
        this.service = new TrangChuService(authService);
        this.chonMenu = Objects.requireNonNull(chonMenu);
        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBackground(BG);
        noiDung.setBorder(new EmptyBorder(26, 28, 30, 28));
        JPanel dauTrang = tieuDe();
        dauTrang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        noiDung.add(dauTrang);
        noiDung.add(khoangCach(18));
        JPanel chaoMung = banner(tenNhanVien);
        chaoMung.setPreferredSize(new Dimension(100, 200));
        chaoMung.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        noiDung.add(chaoMung);
        noiDung.add(khoangCach(18));
        JPanel cacThe = cacTheSoLieu();
        cacThe.setPreferredSize(new Dimension(100, 170));
        cacThe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        noiDung.add(cacThe);
        noiDung.add(khoangCach(18));
        noiDung.add(khuNoiDung());
        noiDung.add(khoangCach(12));
        thongBao.setVisible(false);
        noiDung.add(thongBao);

        JScrollPane cuon = new JScrollPane(noiDung);
        cuon.setBorder(null);
        cuon.getViewport().setBackground(BG);
        cuon.getVerticalScrollBar().setUnitIncrement(18);
        cuon.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(cuon, BorderLayout.CENTER);
    }

    /** MainFrame gọi khi mở lại Trang chủ để số liệu không bị cũ. */
    public void taiDuLieu() {
        if (tacVuTai != null && !tacVuTai.isDone()) return;
        thongBao.setVisible(false);
        tacVuTai = new SwingWorker<>() {
            @Override protected TrangChuService.DuLieuTrangChu doInBackground() {
                return service.layDuLieuTrangChu();
            }
            @Override protected void done() {
                try {
                    hienThiDuLieu(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    thongBao.setText("Việc tải dữ liệu đã bị gián đoạn.");
                    thongBao.setVisible(true);
                } catch (ExecutionException e) {
                    thongBao.setText("Không tải được Trang chủ: " + e.getCause().getMessage());
                    thongBao.setVisible(true);
                }
            }
        };
        tacVuTai.execute();
    }

    private JPanel tieuDe() {
        JPanel p = trong(new BorderLayout());
        JPanel ten = cot();
        ten.add(label("TI TI PET • WORKSPACE", 10, BLUE, true));
        ten.add(label(vaiTro == VaiTro.THO_SPA ? "Công việc Spa của tôi" : "Tổng quan cửa hàng",
                27, NAVY, true));
        ten.add(label(vaiTro == VaiTro.THO_SPA
                ? "Lịch và dịch vụ được phân công cho bạn"
                : "Hoạt động TiTi Pet trong một màn hình", 12, MUTED, false));
        p.add(ten, BorderLayout.WEST);
        JPanel benPhai = trong(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        benPhai.add(ngay);
        benPhai.add(trangThaiCa);
        p.add(benPhai, BorderLayout.EAST);
        return p;
    }

    private JPanel banner(String tenNhanVien) {
        JPanel p = the(new Color(39, 105, 191));
        p.setLayout(new BorderLayout(18, 0));
        p.setBorder(new EmptyBorder(25, 30, 25, 30));
        JPanel chu = cot();
        chu.add(label("GOOD DAY, " + (tenNhanVien == null ? "TI TI PET" : tenNhanVien.toUpperCase()) + "!",
                11, new Color(205, 225, 255), true));
        chu.add(label("Chào mừng bạn đến với TiTi Pet 🐾", 23, Color.WHITE, true));
        chu.add(khoangCach(8));
        chu.add(label(vaiTro == VaiTro.THO_SPA
                        ? "Theo dõi lịch và tiến độ những dịch vụ được phân công"
                        : "Chủ động lịch Spa, thanh toán nhanh, kiểm soát kho và cập nhật kết quả ca trực",
                12, new Color(221, 235, 255), false));
        if (vaiTro != VaiTro.THO_SPA) {
            chu.add(label("trên một hệ thống thống nhất.",
                    12, new Color(221, 235, 255), false));
            JButton moCa = nut("◷ Bắt đầu mở ca làm việc", BLUE, Color.WHITE);
            moCa.addActionListener(e -> chonMenu.accept(MainFrame.CHOT_CA));
            chu.add(khoangCach(13));
            chu.add(moCa);
        }
        p.add(chu, BorderLayout.CENTER);
        JLabel anh = icon("div.hero-art.png", 130);
        p.add(anh, BorderLayout.EAST);
        return p;
    }

    private JPanel cacTheSoLieu() {
        int soThe = vaiTro == VaiTro.THO_SPA ? 3 : 4;
        JPanel p = trong(new GridLayout(1, soThe, 16, 0));
        String[] tieuDe = vaiTro == VaiTro.THO_SPA
                ? new String[]{"Lịch của tôi hôm nay", "Dịch vụ chờ làm", "Dịch vụ đang làm"}
                : new String[]{"Tổng thú cưng", "Lịch hẹn hôm nay",
                    vaiTro == VaiTro.THU_NGAN ? "Hóa đơn ca của tôi" : "Hóa đơn thanh toán",
                    "Ca Spa đang xử lý"};
        String[] hinh = vaiTro == VaiTro.THO_SPA
                ? new String[]{"📆.png", "✂.png", "✂.png"}
                : new String[]{"🐾.png", "📆.png", "🧾.png", "✂.png"};
        Color[] nenIcon = {new Color(232, 241, 255), new Color(255, 243, 222),
                new Color(227, 248, 243), new Color(242, 234, 255)};
        Color[] mauIcon = {new Color(44, 106, 190), new Color(193, 116, 42),
                new Color(36, 155, 142), new Color(151, 91, 200)};
        for (int i = 0; i < soThe; i++) {
            JPanel card = theSoLieu(hinh[i]);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(20, 22, 20, 22));
            card.add(oIcon(hinh[i], nenIcon[i], mauIcon[i]));
            card.add(khoangCach(14));
            giaTriThe[i] = label("—", 25, NAVY, true);
            ghiChuThe[i] = label("Đang tải...", 11, new Color(34, 150, 118), true);
            card.add(giaTriThe[i]);
            card.add(label(tieuDe[i], 12, MUTED, false));
            card.add(ghiChuThe[i]);
            p.add(card);
        }
        return p;
    }

    private JPanel lichHomNay() {
        JPanel p = khung(vaiTro == VaiTro.THO_SPA ? "Lịch của tôi hôm nay" : "Lịch phục vụ hôm nay",
                vaiTro == VaiTro.THO_SPA ? "Chỉ những lịch đã phân công cho bạn"
                        : "Trạng thái đồng bộ giữa quầy và thợ Spa");
        if (vaiTro != VaiTro.THO_SPA) {
            JButton xem = nut("Xem tất cả →", BLUE, new Color(231, 240, 255));
            xem.addActionListener(e -> chonMenu.accept(MainFrame.LICH_HEN));
            ganNutTieuDe(p, xem);
        }
        p.add(danhSachLich, BorderLayout.CENTER);
        return p;
    }

    private JPanel canhBao() {
        JPanel p = khung("Cảnh báo vận hành", "Hàng ít tồn hoặc sắp hết hạn");
        // TODO: Mở nút Chi tiết khi KhoPanel có giao diện hoàn chỉnh.
        JButton chiTiet = nut("Chi tiết", MUTED, new Color(245, 248, 253));
        chiTiet.setEnabled(false);
        chiTiet.setToolTipText("Màn hình Kho đang được xây dựng");
        ganNutTieuDe(p, chiTiet);
        p.add(danhSachCanhBao, BorderLayout.CENTER);
        return p;
    }

    private JPanel thaoTacNhanh() {
        JPanel p = khung("Thao tác nhanh", "Đi đến màn hình phù hợp");
        JPanel nut = trong(new GridLayout(1, 5, 8, 0));
        nut.add(nutDieuHuong("Hồ sơ thú cưng", "🐾.png", MainFrame.THU_CUNG));
        nut.add(nutDieuHuong("Đặt Spa", "📆.png", MainFrame.LICH_HEN));
        nut.add(nutDieuHuong("Bán hàng", "🛒.png", MainFrame.BAN_HANG));
        // TODO: Bật nút khi panel Hóa đơn / giao dịch có giao diện hoàn chỉnh.
        JButton xemHoaDon = nutNhanh("Xem hóa đơn", "🧾.png");
        xemHoaDon.setEnabled(false);
        xemHoaDon.setToolTipText("Màn hình Hóa đơn đang được xây dựng");
        nut.add(xemHoaDon);
        nut.add(nutDieuHuong("Khách hàng & VIP", "👤.png", MainFrame.KHACH_HANG));
        p.add(nut, BorderLayout.CENTER);
        return p;
    }

    private JPanel hoaDonGanDay() {
        JPanel p = khung("Hoạt động gần đây", vaiTro == VaiTro.THU_NGAN
                ? "Hóa đơn đã thanh toán trong ca của bạn" : "Các hóa đơn vừa thanh toán");
        // TODO: Nút Xem hóa đơn sẽ nối khi màn hình Hóa đơn / giao dịch được hoàn thiện.
        p.add(danhSachHoaDon, BorderLayout.CENTER);
        return p;
    }

    /** Hai cột theo ảnh mẫu: bên trái rộng hơn để đọc lịch và chứa thao tác nhanh. */
    private JPanel khuNoiDung() {
        // Thợ Spa không được tạo cả khung kho/hóa đơn; Service cũng không truy vấn chúng.
        if (vaiTro == VaiTro.THO_SPA) {
            JPanel congViec = cot();
            JPanel lich = lichHomNay();
            lich.setPreferredSize(new Dimension(100, 310));
            lich.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));
            congViec.add(lich);
            return congViec;
        }

        JPanel trai = cot();
        JPanel lich = lichHomNay();
        lich.setPreferredSize(new Dimension(100, 260));
        lich.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        trai.add(lich);
        trai.add(khoangCach(18));
        JPanel thaoTac = thaoTacNhanh();
        thaoTac.setPreferredSize(new Dimension(100, 160));
        thaoTac.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        trai.add(thaoTac);

        JPanel phai = cot();
        JPanel canhBao = canhBao();
        canhBao.setPreferredSize(new Dimension(100, 260));
        canhBao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        phai.add(canhBao);
        phai.add(khoangCach(18));
        JPanel hoaDon = hoaDonGanDay();
        hoaDon.setPreferredSize(new Dimension(100, 160));
        hoaDon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        phai.add(hoaDon);

        JPanel haiCot = trong(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.6;
        gbc.insets = new Insets(0, 0, 0, 9);
        haiCot.add(trai, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.insets = new Insets(0, 9, 0, 0);
        haiCot.add(phai, gbc);
        haiCot.setPreferredSize(new Dimension(100, 438));
        haiCot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 438));
        return haiCot;
    }

    private void hienThiDuLieu(TrangChuService.DuLieuTrangChu d) {
        if (d.vaiTro() != vaiTro) {
            thongBao.setText("Phiên đăng nhập đã thay đổi. Vui lòng mở lại Trang chủ.");
            thongBao.setVisible(true);
            return;
        }
        ngay.setText("📅 " + d.ngay().format(DATE));
        trangThaiCa.setText(vaiTro == VaiTro.THO_SPA ? "● Lịch được phân công"
                : d.caHienTai().map(c -> "● " +
                ("DANG_MO".equals(c.trangThai()) ? "Ca đang mở" : "Ca chờ xác nhận"))
                .orElse("● Chưa mở ca"));
        TrangChuDAO.SoLieu s = d.soLieu();
        if (vaiTro == VaiTro.THO_SPA) {
            giaTriThe[0].setText(String.valueOf(s.lichHenHomNay()));
            giaTriThe[1].setText(String.valueOf(s.dichVuChoLam()));
            giaTriThe[2].setText(String.valueOf(s.dichVuDangLam()));
            ghiChuThe[0].setText("Lịch được giao hôm nay");
            ghiChuThe[1].setText("Việc được giao đang chờ");
            ghiChuThe[2].setText("Việc được giao đang làm");
        } else {
            giaTriThe[0].setText(String.valueOf(s.tongThuCung()));
            giaTriThe[1].setText(String.valueOf(s.lichHenHomNay()));
            giaTriThe[2].setText(String.valueOf(s.hoaDonDaThanhToan()));
            giaTriThe[3].setText(String.valueOf(s.dichVuDangLam()));
            ghiChuThe[0].setText("Hồ sơ đã ghi nhận");
            ghiChuThe[1].setText(s.lichChoTiepNhan() + " lịch chờ tiếp nhận");
            ghiChuThe[2].setText(MONEY.format(s.doanhThuHomNay())
                    + (vaiTro == VaiTro.THU_NGAN ? " đ doanh thu ca" : " đ doanh thu hôm nay"));
            ghiChuThe[3].setText(s.dichVuChoLam() + " dịch vụ chờ làm");
        }

        danhSachLich.removeAll();
        if (d.lichPhucVu().isEmpty()) danhSachLich.add(trangThaiRong("🐾.png",
                vaiTro == VaiTro.THO_SPA ? "Chưa có lịch được phân công hôm nay"
                        : "Chưa có lịch hôm nay"));
        for (TrangChuDAO.LichPhucVu lich : d.lichPhucVu()) {
            danhSachLich.add(dong(lich.gioHen().format(TIME) + "  •  " + lich.tenThuCung()
                    + "  /  " + lich.tenKhachHang(), lich.tenDichVu() + "  •  " + lich.trangThai()));
        }
        if (vaiTro != VaiTro.THO_SPA) {
            danhSachCanhBao.removeAll();
            if (d.canhBaoKho().isEmpty()) danhSachCanhBao.add(trangThaiRong("✂.png", "Không có cảnh báo kho"));
            for (TrangChuDAO.CanhBaoKho kho : d.canhBaoKho()) {
                String ghiChu = "Còn " + kho.soLuongTon() + " sản phẩm";
                if (kho.hanSuDung() != null) ghiChu += "  •  HSD " + kho.hanSuDung().format(DATE);
                danhSachCanhBao.add(dong(kho.tenSanPham(), ghiChu));
            }
            danhSachHoaDon.removeAll();
            if (d.hoaDonGanDay().isEmpty()) danhSachHoaDon.add(trangThaiRong("🧾.png", "Chưa có hoạt động gần đây"));
            for (TrangChuDAO.HoaDonGanDay hd : d.hoaDonGanDay()) {
                danhSachHoaDon.add(dong(hd.maHoaDon() + "  •  " + hd.tenKhachHang(),
                        hd.ngayLap().format(DATE) + "  •  " + MONEY.format(hd.tienPhaiThu()) + " đ"));
            }
        }
        revalidate();
        repaint();
    }

    private static JPanel khung(String tieuDe, String phuDe) {
        JPanel p = the(Color.WHITE);
        p.setLayout(new BorderLayout(12, 14));
        p.setBorder(new EmptyBorder(20, 22, 20, 22));
        JPanel header = cot();
        header.add(label(tieuDe, 16, NAVY, true));
        header.add(label(phuDe, 11, MUTED, false));
        p.add(header, BorderLayout.NORTH);
        return p;
    }

    private static void ganNutTieuDe(JPanel khung, JButton nut) {
        Component tieuDe = ((BorderLayout) khung.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        khung.remove(tieuDe);
        JPanel hang = trong(new BorderLayout());
        hang.add(tieuDe, BorderLayout.WEST);
        hang.add(nut, BorderLayout.EAST);
        khung.add(hang, BorderLayout.NORTH);
    }

    private static JPanel dong(String tieuDe, String phuDe) {
        JPanel p = cot();
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(8, 4, 8, 4)));
        p.add(label(tieuDe, 12, NAVY, true));
        p.add(label(phuDe, 11, MUTED, false));
        return p;
    }

    private static JPanel trangThaiRong(String tenAnh, String noiDung) {
        JPanel p = trong(new GridBagLayout());
        p.setBackground(new Color(248, 251, 255));
        p.setOpaque(true);
        p.setBorder(BorderFactory.createLineBorder(new Color(220, 234, 250)));
        p.setPreferredSize(new Dimension(100, 145));
        JPanel chu = cot();
        JLabel icon = iconMau(tenAnh, 30, BLUE);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel moTa = label(noiDung, 13, MUTED, true);
        moTa.setAlignmentX(Component.CENTER_ALIGNMENT);
        chu.add(icon);
        chu.add(khoangCach(8));
        chu.add(moTa);
        p.add(chu);
        return p;
    }

    private JButton nutDieuHuong(String ten, String tenAnh, String maMenu) {
        JButton b = nutNhanh(ten, tenAnh);
        b.addActionListener(e -> chonMenu.accept(maMenu));
        return b;
    }

    private static JButton nutNhanh(String ten, String tenAnh) {
        JButton b = nut(ten, BLUE, new Color(249, 251, 255));
        b.setIcon(iconTheoAnh(tenAnh, 25).getIcon());
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setIconTextGap(8);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(11, 6, 9, 6)));
        return b;
    }

    private static JPanel danhSach() {
        JPanel p = cot();
        p.add(label("Đang tải...", 12, MUTED, false));
        return p;
    }

    private static JPanel cot() {
        JPanel p = trong(null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static JPanel trong(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    private static JPanel the(Color mau) {
        return PosUI.roundPanel(mau, 18);
    }

    /** Thẻ số liệu có hình PNG lặp mờ ở góc, giống ảnh mẫu. */
    private static JPanel theSoLieu(String tenAnh) {
        ImageIcon anh = PosUI.assetIcon("resources/icons/home-page/" + tenAnh, 90, 90);
        return new JPanel() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                if (anh != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.07f));
                    g2.drawImage(anh.getImage(), getWidth() - 85, getHeight() - 80, null);
                }
                g2.setComposite(AlphaComposite.SrcOver);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    /** Nền màu nhỏ phía sau biểu tượng PNG trên từng thẻ số liệu. */
    private static JPanel oIcon(String tenAnh, Color nen, Color mau) {
        JPanel p = the(nen);
        p.setLayout(new GridBagLayout());
        p.setPreferredSize(new Dimension(46, 46));
        p.setMaximumSize(new Dimension(46, 46));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add("🐾.png".equals(tenAnh) ? iconMau(tenAnh, 25, mau) : iconTheoAnh(tenAnh, 25));
        return p;
    }

    private static JLabel iconTheoAnh(String tenAnh, int co) {
        ImageIcon anh = PosUI.assetIcon("resources/icons/home-page/" + tenAnh, co, co);
        return anh == null ? new JLabel() : new JLabel(anh);
    }

    /** Đổi màu nét PNG theo từng ô, giữ nguyên độ trong suốt của ảnh gốc. */
    private static JLabel iconMau(String tenAnh, int co, Color mau) {
        ImageIcon goc = PosUI.assetIcon("resources/icons/home-page/" + tenAnh, co, co);
        if (goc == null) return new JLabel();
        BufferedImage anh = new BufferedImage(co, co, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = anh.createGraphics();
        g.drawImage(goc.getImage(), 0, 0, null);
        g.setComposite(AlphaComposite.SrcIn);
        g.setColor(mau);
        g.fillRect(0, 0, co, co);
        g.dispose();
        return new JLabel(new ImageIcon(anh));
    }

    private static JLabel label(String chu, int co, Color mau, boolean dam) {
        JLabel l = new JLabel(chu);
        l.setFont(new Font("Segoe UI", dam ? Font.BOLD : Font.PLAIN, co));
        l.setForeground(mau);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JButton nut(String chu, Color mauChu, Color mauNen) {
        JButton b = new JButton(chu);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(mauChu);
        b.setBackground(mauNen);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 12, 9, 12));
        return b;
    }

    private static Component khoangCach(int cao) {
        return Box.createRigidArea(new Dimension(0, cao));
    }

    private static JLabel icon(String ten, int co) {
        ImageIcon anh = PosUI.assetIcon("resources/icons/home-page/" + ten, co, co);
        return anh == null ? label("", co, MUTED, false) : new JLabel(anh);
    }
}
