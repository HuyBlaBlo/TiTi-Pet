
package com.titipet.gui.panel;

import com.titipet.dao.CaLamViecSpaDAO;
import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.CaLamViecSpa;
import com.titipet.gui.dialog.GhiNhanVatTuDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LichLamViecThoSpaPanel extends JPanel {

    private final String maNV;
    private final CaLamViecSpaDAO dao = new CaLamViecSpaDAO();

    private final Color BLUE = new Color(36, 101, 190);
    private final Color DARK_BLUE = new Color(29, 61, 112);
    private final Color BG = new Color(244, 248, 255);
    private final Color LIGHT_BLUE = new Color(235, 243, 255);
    private final Color BORDER = new Color(225, 233, 245);
    private final Color TEXT = new Color(80, 105, 145);

    private JLabel lblChoLam;
    private JLabel lblDangLam;
    private JLabel lblHoanThanh;
    private JLabel lblBanGiao;

    private JPanel pnlChoLam;
    private JPanel pnlDangLam;
    private JPanel pnlHoanThanh;
    private JPanel pnlBanGiao;

    public LichLamViecThoSpaPanel(String maNV) {
        this.maNV = maNV;

        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        content.add(taoHeader());
        content.add(Box.createVerticalStrut(18));
        content.add(taoThongBao());
        content.add(Box.createVerticalStrut(15));
        content.add(taoKhuVucTrangThai());
        content.add(Box.createVerticalStrut(15));
        content.add(taoQuyTac());
        content.add(Box.createVerticalStrut(15));
        content.add(taoFooter());

        add(content, BorderLayout.CENTER);

        taiDuLieu();
    }

    // ================= HEADER =================

    private JPanel taoHeader() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG);
        titlePanel.setBorder(new EmptyBorder(18, 0, 5, 0));

        JPanel titleLeft = new JPanel();
        titleLeft.setLayout(new BoxLayout(titleLeft, BoxLayout.Y_AXIS));
        titleLeft.setBackground(BG);

        JLabel brand = new JLabel("T I T I   P E T  •  WORKSPACE");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 10));
        brand.setForeground(BLUE);

        JLabel title = new JLabel("Lịch làm việc Spa");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
                "Theo dõi và thực hiện các ca dịch vụ được phân công"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT);

        titleLeft.add(brand);
        titleLeft.add(Box.createVerticalStrut(5));
        titleLeft.add(title);
        titleLeft.add(Box.createVerticalStrut(4));
        titleLeft.add(subtitle);

        JLabel date = new JLabel(
                new SimpleDateFormat("dd/MM/yyyy").format(new Date())
        );
        date.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        date.setForeground(TEXT);


        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(date);

        titlePanel.add(titleLeft, BorderLayout.WEST);
        titlePanel.add(right, BorderLayout.EAST);

        p.add(titlePanel);
        return p;
    }

    // ================= NOTICE =================

    private JPanel taoThongBao() {
        JPanel p = new RoundedPanel(12, LIGHT_BLUE);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 15, 12, 15));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        JLabel lbl = new JLabel(
                "Thực hiện ca theo thứ tự: Chờ thực hiện → "
                + "Đang thực hiện → Đã hoàn thành → Đã bàn giao."
        );

        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(BLUE);

        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ================= STATUS BOARD =================

    private JPanel taoKhuVucTrangThai() {
        JPanel board = new JPanel(new GridLayout(1, 4, 12, 0));
        board.setBackground(BG);
        board.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
        board.setPreferredSize(new Dimension(1200, 420));

        pnlChoLam = new JPanel();
        pnlDangLam = new JPanel();
        pnlHoanThanh = new JPanel();
        pnlBanGiao = new JPanel();

        lblChoLam = new JLabel("0");
        lblDangLam = new JLabel("0");
        lblHoanThanh = new JLabel("0");
        lblBanGiao = new JLabel("0");

        board.add(taoCotTrangThai(
                "Chờ thực hiện", lblChoLam,
                pnlChoLam, new Color(230, 239, 255)));

        board.add(taoCotTrangThai(
                "Đang thực hiện", lblDangLam,
                pnlDangLam, new Color(255, 242, 220)));

        board.add(taoCotTrangThai(
                "Đã hoàn thành", lblHoanThanh,
                pnlHoanThanh, new Color(225, 248, 238)));

        board.add(taoCotTrangThai(
                "Đã bàn giao", lblBanGiao,
                pnlBanGiao, new Color(235, 243, 255)));

        return board;
    }

    private JPanel taoCotTrangThai(
            String title,
            JLabel count,
            JPanel danhSach,
            Color badgeColor) {

        JPanel column = new RoundedPanel(14, Color.WHITE);
        column.setLayout(new BorderLayout(0, 10));
        column.setBorder(new EmptyBorder(13, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(DARK_BLUE);

        count.setOpaque(true);
        count.setBackground(badgeColor);
        count.setForeground(BLUE);
        count.setFont(new Font("Segoe UI", Font.BOLD, 11));
        count.setHorizontalAlignment(SwingConstants.CENTER);
        count.setPreferredSize(new Dimension(28, 22));

        header.add(lblTitle, BorderLayout.WEST);
        header.add(count, BorderLayout.EAST);

        danhSach.setLayout(new BoxLayout(danhSach, BoxLayout.Y_AXIS));
        danhSach.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(danhSach);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        column.add(header, BorderLayout.NORTH);
        column.add(scroll, BorderLayout.CENTER);

        return column;
    }

    // ================= LOAD DATA =================

    public void taiDuLieu() {

        List<CaLamViecSpa> ds = dao.layCaTheoThoSpa(maNV);

        pnlChoLam.removeAll();
        pnlDangLam.removeAll();
        pnlHoanThanh.removeAll();
        pnlBanGiao.removeAll();

        int cho = 0;
        int dang = 0;
        int hoan = 0;
        int banGiao = 0;

        for (CaLamViecSpa ca : ds) {

            JPanel card = taoCardCa(ca);

            if ("DA_BAN_GIAO".equals(ca.getTrangThaiBanGiao())) {
                pnlBanGiao.add(card);
                pnlBanGiao.add(Box.createVerticalStrut(8));
                banGiao++;

            } else if ("CHO_LAM".equals(ca.getTrangThaiThucHien())) {
                pnlChoLam.add(card);
                pnlChoLam.add(Box.createVerticalStrut(8));
                cho++;

            } else if ("DANG_LAM".equals(ca.getTrangThaiThucHien())) {
                pnlDangLam.add(card);
                pnlDangLam.add(Box.createVerticalStrut(8));
                dang++;

            } else if ("HOAN_THANH".equals(ca.getTrangThaiThucHien())) {
                pnlHoanThanh.add(card);
                pnlHoanThanh.add(Box.createVerticalStrut(8));
                hoan++;
            }
        }

        hienThiRong(pnlChoLam);
        hienThiRong(pnlDangLam);
        hienThiRong(pnlHoanThanh);
        hienThiRong(pnlBanGiao);

        lblChoLam.setText(String.valueOf(cho));
        lblDangLam.setText(String.valueOf(dang));
        lblHoanThanh.setText(String.valueOf(hoan));
        lblBanGiao.setText(String.valueOf(banGiao));

        revalidate();
        repaint();
    }

    // ================= CARD =================

    private JPanel taoCardCa(CaLamViecSpa ca) {

    	JPanel p = new RoundedPanel(10, BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ma = new JLabel("Lịch " + ca.getMaLichHen());
        ma.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ma.setForeground(BLUE);

        JLabel pet = new JLabel(ca.getTenThuCung());
        pet.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pet.setForeground(DARK_BLUE);

        JLabel owner = taoLabel("Chủ: " + ca.getTenKH());
        JLabel phone = taoLabel("SĐT: " + ca.getSdtKH());
        JLabel service = taoLabel("Dịch vụ: " + ca.getTenDichVu());

        JLabel time = taoLabel("Giờ hẹn: "
                + (ca.getThoiGianHen() == null
                ? "Chưa xác định"
                : ca.getThoiGianHen().toString().replace("T", " ")));

        JLabel breed = taoLabel("Loại: " + ca.getLoaiThuCung());

        JLabel weight = taoLabel("Cân nặng: "
                + (ca.getCanNang() == null ? "Chưa có"
                : ca.getCanNang() + " kg"));

        JLabel fur = taoLabel("Tình trạng lông: "
                + ca.getTinhTrangLong());

        JLabel note = taoLabel("Ghi chú: "
                + ca.getGhiChuLichHen());

        p.add(ma);
        p.add(Box.createVerticalStrut(5));
        p.add(pet);
        p.add(Box.createVerticalStrut(5));
        p.add(owner);
        p.add(Box.createVerticalStrut(4));
        p.add(phone);
        p.add(Box.createVerticalStrut(4));
        p.add(service);
        p.add(Box.createVerticalStrut(4));
        p.add(time);
        p.add(Box.createVerticalStrut(4));
        p.add(breed);
        p.add(Box.createVerticalStrut(4));
        p.add(weight);
        p.add(Box.createVerticalStrut(4));
        p.add(fur);
        p.add(Box.createVerticalStrut(4));
        p.add(note);
        p.add(Box.createVerticalStrut(10));

        // ===== NÚT CHUYỂN TRẠNG THÁI =====
        JButton btn = taoNutTrangThai(ca);

        if (btn != null) {
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(btn);
            p.add(Box.createVerticalStrut(8));
        }

        // ===== NÚT GHI NHẬN VẬT TƯ =====
        RoundedButton btnVatTu = new RoundedButton(
                "Ghi nhận vật tư",
                new Color(235, 243, 255)
        );

        btnVatTu.setForeground(new Color(36, 101, 190));
        btnVatTu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVatTu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVatTu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVatTu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVatTu.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnVatTu.addActionListener(e -> {

            GhiNhanVatTuDialog dialog =
                    new GhiNhanVatTuDialog(
                            (Frame) SwingUtilities.getWindowAncestor(this)
                    );

            dialog.setVisible(true);

            String vatTu = dialog.getVatTuDaLuu();

            if (vatTu != null) {
                System.out.println("Vật tư đã ghi nhận: " + vatTu);
            }
        });

        p.add(btnVatTu);

        return p;
    }

    private JLabel taoLabel(String text) {
        JLabel lbl = new JLabel("<html><div style='width:220px'>"
                + text + "</div></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // ================= ACTION BUTTON =================

    private JButton taoNutTrangThai(CaLamViecSpa ca) {

        JButton btn = null;

        if ("CHO_LAM".equals(ca.getTrangThaiThucHien())) {

            btn = new RoundedButton("Bắt đầu thực hiện", BLUE);
            btn.addActionListener(e -> xuLy(ca, "BAT_DAU"));

        } else if ("DANG_LAM".equals(ca.getTrangThaiThucHien())) {

            btn = new RoundedButton("Hoàn thành", new Color(225, 153, 40));
            btn.addActionListener(e -> xuLy(ca, "HOAN_THANH"));

        } else if ("HOAN_THANH".equals(ca.getTrangThaiThucHien())
                && !"DA_BAN_GIAO".equals(ca.getTrangThaiBanGiao())) {

            btn = new RoundedButton("Bàn giao", new Color(36, 155, 110));
            btn.addActionListener(e -> xuLy(ca, "BAN_GIAO"));
        }

        if (btn != null) {
            btn.setPreferredSize(new Dimension(180, 34));
            btn.setMaximumSize(new Dimension(220, 36));
        }

        return btn;
    }

    private void xuLy(CaLamViecSpa ca, String action) {

        String message;

        if ("BAT_DAU".equals(action)) {
            message = "Bạn muốn bắt đầu thực hiện ca này?";
        } else if ("HOAN_THANH".equals(action)) {
            message = "Xác nhận hoàn thành dịch vụ?";
        } else {
            message = "Xác nhận bàn giao thú cưng?";
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, message, "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success;

        if ("BAT_DAU".equals(action)) {
            success = dao.batDauCa(ca.getMaChiTietDichVu(), maNV);
        } else if ("HOAN_THANH".equals(action)) {
            success = dao.hoanThanhCa(ca.getMaChiTietDichVu(), maNV);
        } else {
            success = dao.banGiaoCa(ca.getMaChiTietDichVu(), maNV);
        }

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Cập nhật trạng thái thành công!");
            taiDuLieu();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Không thể cập nhật trạng thái ca!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ================= RULES =================

    private JPanel taoQuyTac() {

        JPanel p = new RoundedPanel(16, Color.WHITE);
        p.setLayout(new BorderLayout(0, 15));
        p.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Quy trình thực hiện dịch vụ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(DARK_BLUE);

        JPanel rules = new JPanel(new GridLayout(1, 3, 12, 0));
        rules.setBackground(Color.WHITE);

        rules.add(taoRule(
                "Kiểm tra thú cưng và tình trạng lông trước khi thực hiện."));

        rules.add(taoRule(
                "Cập nhật trạng thái hoàn thành sau khi kết thúc dịch vụ."));

        rules.add(taoRule(
                "Bàn giao thú cưng cho khách sau khi xác nhận hoàn tất."));

        p.add(title, BorderLayout.NORTH);
        p.add(rules, BorderLayout.CENTER);

        return p;
    }

    private JPanel taoRule(String text) {
        JPanel p = new RoundedPanel(10, LIGHT_BLUE);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lbl = new JLabel("<html>" + text + "</html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT);

        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ================= FOOTER =================

    private JLabel taoFooter() {
        JLabel footer = new JLabel(
                "TiTi Pet • Spa Workspace • Quản lý ca làm việc");

        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(155, 173, 202));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        return footer;
    }

    private void hienThiRong(JPanel panel) {
        if (panel.getComponentCount() == 0) {
            JLabel empty = new JLabel("Chưa có ca");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(new Color(145, 165, 195));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);

            panel.add(Box.createVerticalStrut(25));
            panel.add(empty);
        }
    }

    // ================= ROUNDED PANEL =================

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color color;

        public RoundedPanel(int radius, Color color) {
            this.radius = radius;
            this.color = color;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                    radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================= ROUNDED BUTTON =================

    private static class RoundedButton extends JButton {

        private final Color bgColor;

        public RoundedButton(String text, Color color) {
            super(text);
            this.bgColor = color;

            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================= TEST =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ConnectDB.getInstance().connect();

                JFrame frame = new JFrame("TiTi Pet - Lịch làm việc Spa");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1450, 850);
                frame.setLocationRelativeTo(null);

                frame.setContentPane(
                        new LichLamViecThoSpaPanel("NV001"));

                frame.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Không thể kết nối SQL Server:\n"
                                + e.getMessage());
            }
        });
    }
}
