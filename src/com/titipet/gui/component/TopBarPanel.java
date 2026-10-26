package com.titipet.gui.component;

import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class TopBarPanel extends JPanel {
    private static final Color BLUE = new Color(37, 74, 124);
    private final JLabel currentPage = new JLabel();
    private final JTextField searchField = new JTextField();

    public TopBarPanel(String tenNhanVien, String vaiTro, Runnable onLogout) {
        super(new BorderLayout(24, 0));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(0, 76));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(232, 238, 247)),
                new EmptyBorder(16, 30, 16, 28)));
        add(taoDuongDan(), BorderLayout.WEST);
        add(taoTimKiem(), BorderLayout.CENTER);
        add(taoTaiKhoan(tenNhanVien, vaiTro, onLogout), BorderLayout.EAST);

        // cửa sổ hẹp ưu tiên tiêu đề và tài khoản thay vì bóp nhỏ ô tìm kiếm
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                searchField.setVisible(getWidth() >= 1050);
            }
        });
    }

    private JPanel taoDuongDan() {
        JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        breadcrumb.setOpaque(false);
        JLabel prefix = new JLabel("Quản lý cửa hàng  ›  ");
        prefix.setForeground(new Color(139, 161, 191));
        prefix.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        currentPage.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        currentPage.setForeground(BLUE);
        breadcrumb.add(prefix);
        breadcrumb.add(currentPage);
        return breadcrumb;
    }

    private JPanel taoTimKiem() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        searchField.setPreferredSize(new Dimension(420, 40));
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        searchField.setBackground(new Color(245, 248, 253));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm khách hàng, thú cưng, hóa đơn...");
        searchField.putClientProperty("FlatLaf.style",
                "arc: 20; borderWidth: 1; borderColor: #E8EEF7; focusWidth: 1; margin: 8,14,8,14");

        searchField.setEditable(false);
        searchField.setFocusable(false);
        center.add(searchField);
        return center;
    }

    private JPanel taoTaiKhoan(String name, String role, Runnable onLogout) {
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        right.setOpaque(false);
        RoundedPannel notification = new RoundedPannel(new Color(240, 246, 255), 22);
        notification.setLayout(new BorderLayout());
        notification.setPreferredSize(new Dimension(34, 34));
        notification.add(new JLabel(SidebarPanel.taiIcon("🔔.png", 17, null), SwingConstants.CENTER));
        notification.setToolTipText("Thông báo");
        right.add(notification);

        JButton account = new JButton(name + "  ⌄", SidebarPanel.taiIcon("👤.png", 30, null));
        account.setForeground(BLUE);
        account.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        account.setBorder(new EmptyBorder(0, 0, 0, 0));
        account.setContentAreaFilled(false);
        account.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        account.setToolTipText(role);
        JPanel details = new JPanel(new GridLayout(2, 1));
        details.setOpaque(false);
        details.add(account);
        JLabel roleLabel = new JLabel(role);
        roleLabel.setForeground(new Color(139, 161, 191));
        roleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        roleLabel.setBorder(new EmptyBorder(0, 34, 0, 0));
        details.add(roleLabel);
        right.add(details);

        JPopupMenu accountMenu = new JPopupMenu();
        JMenuItem logout = new JMenuItem("Đăng xuất");
        logout.addActionListener(event -> onLogout.run());
        accountMenu.add(logout);
        account.addActionListener(event -> accountMenu.show(account, 0, account.getHeight()));
        return right;
    }

    public void setTieuDe(String title) {

        currentPage.setText(title);
    }
}
