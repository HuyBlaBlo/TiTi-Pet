
package com.titipet.gui.panel;

import com.titipet.dao.LichThoSpaDAO;
import com.titipet.connectDB.ConnectDB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LichThoSpaPanel extends JPanel {

    private final LichThoSpaDAO dao = new LichThoSpaDAO();

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

    public LichThoSpaPanel() {
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

        JPanel breadcrumb = new JPanel(new BorderLayout());
        breadcrumb.setBackground(Color.WHITE);
        breadcrumb.setBorder(new EmptyBorder(10, 25, 10, 25));

//        JLabel lblBread = new JLabel(
//            "Quản lý cửa hàng   ›   Điều phối & thực hiện Spa"
//        );
//        lblBread.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        lblBread.setForeground(TEXT);
//
//        breadcrumb.add(lblBread, BorderLayout.WEST);
//
//        JLabel lblUser = new JLabel("Quản lý TiTi Pet");
//        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        lblUser.setForeground(DARK_BLUE);
//
//        breadcrumb.add(lblUser, BorderLayout.EAST);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BG);
        titlePanel.setBorder(new EmptyBorder(18, 0, 5, 0));

        JPanel titleLeft = new JPanel();
        titleLeft.setLayout(new BoxLayout(titleLeft, BoxLayout.Y_AXIS));
        titleLeft.setBackground(BG);

        JLabel brand = new JLabel("T I T I   P E T  •  WORKSPACE");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 10));
        brand.setForeground(BLUE);

        JLabel title = new JLabel("Điều phối & thực hiện Spa");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(DARK_BLUE);

        JLabel subtitle = new JLabel(
            "Theo dõi ca dịch vụ theo trạng thái công việc"
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

        titlePanel.add(titleLeft, BorderLayout.WEST);
        titlePanel.add(date, BorderLayout.EAST);

//        p.add(breadcrumb);
        p.add(titlePanel);

        return p;
    }

    // ================= NOTICE =================

    private JPanel taoThongBao() {
        JPanel p = new RoundedPanel(12, LIGHT_BLUE);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 15, 12, 15));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel lbl = new JLabel(
            "Thợ Spa chỉ xem và cập nhật các ca được phân công. "
            + "Chuyển trạng thái: Đã tiếp nhận → Đang thực hiện "
            + "→ Hoàn thành → Đã bàn giao."
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
        board.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        pnlChoLam = new JPanel();
        pnlDangLam = new JPanel();
        pnlHoanThanh = new JPanel();
        pnlBanGiao = new JPanel();

        lblChoLam = new JLabel("0");
        lblDangLam = new JLabel("0");
        lblHoanThanh = new JLabel("0");
        lblBanGiao = new JLabel("0");

        board.add(taoCotTrangThai("Chờ thực hiện", lblChoLam,
                pnlChoLam, new Color(230, 239, 255)));

        board.add(taoCotTrangThai("Đang thực hiện", lblDangLam,
                pnlDangLam, new Color(255, 242, 220)));

        board.add(taoCotTrangThai("Đã hoàn thành", lblHoanThanh,
                pnlHoanThanh, new Color(225, 248, 238)));

        board.add(taoCotTrangThai("Đã bàn giao", lblBanGiao,
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
        header.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(DARK_BLUE);

        count.setOpaque(true);
        count.setBackground(badgeColor);
        count.setForeground(BLUE);
        count.setFont(new Font("Segoe UI", Font.BOLD, 11));
        count.setHorizontalAlignment(SwingConstants.CENTER);
        count.setPreferredSize(new Dimension(25, 22));

        header.add(lblTitle, BorderLayout.WEST);
        header.add(count, BorderLayout.EAST);

        danhSach.setLayout(new BoxLayout(danhSach, BoxLayout.Y_AXIS));
        danhSach.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(danhSach);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        column.add(header, BorderLayout.NORTH);
        column.add(scroll, BorderLayout.CENTER);
        
        column.setCursor(new Cursor(Cursor.HAND_CURSOR));

        column.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                hienThiDanhSachCa(title, danhSach);
            }
        });

        return column;
    }
    
    private void hienThiDanhSachCa(String title, JPanel danhSach) {

        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            title,
            true
        );

        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(DARK_BLUE);

        JPanel ds = new JPanel();
        ds.setLayout(new BoxLayout(ds, BoxLayout.Y_AXIS));
        ds.setBackground(Color.WHITE);

        boolean coCa = false;

        for (Component c : danhSach.getComponents()) {
            if (c instanceof JPanel) {
                ds.add(c);
                coCa = true;
            }
        }

        if (!coCa) {
            JLabel empty = new JLabel("Chưa có ca");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(TEXT);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            ds.add(empty);
        }

        JScrollPane scroll = new JScrollPane(ds);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        RoundedButton btnDong = new RoundedButton("Đóng", BLUE);
        btnDong.setPreferredSize(new Dimension(100, 36));

        btnDong.addActionListener(e -> dialog.dispose());
        btnDong.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDong.setForeground(Color.WHITE);
        btnDong.setBackground(BLUE);
        btnDong.setFocusPainted(false);
        btnDong.setBorderPainted(false);
        btnDong.setContentAreaFilled(false);
        btnDong.setOpaque(false);
        btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDong.setPreferredSize(new Dimension(100, 36));

        btnDong.addActionListener(e -> dialog.dispose());
        btnDong.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);
        bottom.add(btnDong);

        content.add(lblTitle, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }
    
    private static class RoundedButton extends JButton {

        private Color bgColor;

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
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================= RULES =================

    private JPanel taoQuyTac() {
        JPanel p = new RoundedPanel(16, Color.WHITE);
        p.setLayout(new BorderLayout(0, 15));
        p.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Quy tắc bàn giao thú cưng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(DARK_BLUE);

        JPanel rules = new JPanel(new GridLayout(1, 3, 12, 0));
        rules.setBackground(Color.WHITE);

        rules.add(taoRule(
            "Khách nhận trả: tiếp tục chăm sóc, "
            + "không thu phụ lưu chuồng."
        ));

        rules.add(taoRule(
            "Nếu có đồ dùng đi trong lúc chờ, "
            + "ghi lại và tình trạng chi phí thực tế."
        ));

        rules.add(taoRule(
            "Mọi dịch vụ đều đã thanh toán trước "
            + "khi Spa bắt đầu thực hiện."
        ));

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
            "TiTi Pet • Interactive UX prototype • "
            + "Dữ liệu lưu trong trình duyệt, không kết nối máy chủ"
        );

        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(155, 173, 202));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        return footer;
    }

    // ================= LOAD DATA =================

    public void taiDuLieu() {
        int[] s = dao.layThongKe();

        lblChoLam.setText(String.valueOf(s[0]));
        lblDangLam.setText(String.valueOf(s[1]));
        lblHoanThanh.setText(String.valueOf(s[2]));
        lblBanGiao.setText(String.valueOf(s[3]));

        taiDanhSachCa();
    }

    private void taiDanhSachCa() {
        List<Object[]> ds = dao.layDanhSachSpa();

        pnlChoLam.removeAll();
        pnlDangLam.removeAll();
        pnlHoanThanh.removeAll();
        pnlBanGiao.removeAll();

        for (Object[] row : ds) {
            String trangThai = String.valueOf(row[9]);
            String banGiao = String.valueOf(row[10]);

            JPanel card = taoCardCa(row);

            if ("CHO_LAM".equals(trangThai)) {
                pnlChoLam.add(card);
                pnlChoLam.add(Box.createVerticalStrut(8));

            } else if ("DANG_LAM".equals(trangThai)) {
                pnlDangLam.add(card);
                pnlDangLam.add(Box.createVerticalStrut(8));

            } else if ("HOAN_THANH".equals(trangThai)
                    && "CHUA_BAN_GIAO".equals(banGiao)) {
                pnlHoanThanh.add(card);
                pnlHoanThanh.add(Box.createVerticalStrut(8));

            } else if ("DA_BAN_GIAO".equals(banGiao)) {
                pnlBanGiao.add(card);
                pnlBanGiao.add(Box.createVerticalStrut(8));
            }
        }

        hienThiRong(pnlChoLam);
        hienThiRong(pnlDangLam);
        hienThiRong(pnlHoanThanh);
        hienThiRong(pnlBanGiao);

        revalidate();
        repaint();
    }

    private JPanel taoCardCa(Object[] row) {
        JPanel p = new RoundedPanel(10, BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ma = new JLabel("Lịch " + row[0]);
        ma.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ma.setForeground(BLUE);

        JLabel pet = new JLabel(String.valueOf(row[1]));
        pet.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pet.setForeground(DARK_BLUE);

        JLabel owner = new JLabel(String.valueOf(row[3]));
        owner.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        owner.setForeground(TEXT);

        JLabel service = new JLabel(String.valueOf(row[5]));
        service.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        service.setForeground(TEXT);

        JLabel groomer = new JLabel(
            "Thợ: " + (row[6] == null ? "Chưa phân công" : row[6])
        );
        groomer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        groomer.setForeground(TEXT);

        p.add(ma);
        p.add(Box.createVerticalStrut(5));
        p.add(pet);
        p.add(Box.createVerticalStrut(4));
        p.add(owner);
        p.add(Box.createVerticalStrut(4));
        p.add(service);
        p.add(Box.createVerticalStrut(4));
        p.add(groomer);

        return p;
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
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                    radius, radius);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // ================= TEST =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ConnectDB.getInstance().connect();

                JFrame frame = new JFrame("TiTi Pet - Điều phối Spa");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1450, 850);
                frame.setLocationRelativeTo(null);
                frame.setContentPane(new LichThoSpaPanel());
                frame.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Không thể kết nối SQL Server:\n" + e.getMessage());
            }
        });
    }
}
