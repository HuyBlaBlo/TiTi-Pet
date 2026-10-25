package com.titipet.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.titipet.bus.AuthService;
import com.titipet.entity.NhanVien;
import com.titipet.util.RoundedPannel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LoginFrame extends JFrame {
    private static final Color BACKGROUND = new Color(245, 248, 254);
    private static final Color LEFT_BACKGROUND = new Color(193, 220, 250);
    private static final Color DARK_BLUE = new Color(10, 45, 111);
    private static final Color TEXT = new Color(30, 30, 30);
    private static final Color MUTED = new Color(130, 127, 127);
    private static final Color ERROR_COLOR = new Color(188, 49, 65);
    private static final String FONT_FAMILY = fontFamily();
    private static final int BREAKPOINT = 850;

    public LoginFrame(AuthService authService) {
        setTitle("TiTiPet - Đăng nhập");
        LoginView view = new LoginView(Objects.requireNonNull(authService));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(view);
        getRootPane().setDefaultButton(view.btnDangNhap);
        GraphicsConfiguration configuration = getGraphicsConfiguration();
        Rectangle screen = configuration.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
        Rectangle workArea = new Rectangle(
                screen.x + screenInsets.left,
                screen.y + screenInsets.top,
                screen.width - screenInsets.left - screenInsets.right,
                screen.height - screenInsets.top - screenInsets.bottom
        );
        // Kích thước ban đầu theo màn hình hiện tại, tránh giữ canvas 1440 x 900.
        view.setPreferredSize(workArea.getSize());
        setMaximizedBounds(workArea);
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private static final class LoginView extends JPanel {
        private final AuthService authService;
        private final ArtworkPanel artwork = new ArtworkPanel();
        private final FormContainer formContainer = new FormContainer();
        private final RoundedPannel card = new RoundedPannel(Color.WHITE, 64);
        private final JPanel form = new JPanel(new GridBagLayout());
        private final JTextField txtTenDangNhap = new JTextField();
        private final JPasswordField txtMatKhau = new JPasswordField();
        private final JButton btnHienMatKhau = new JButton();
        private final JButton btnDangNhap = new JButton("ĐĂNG NHẬP");
        private final JLabel lblThongBao = new JLabel(" ");
        private final JLabel title = new JLabel("Đăng nhập");
        private final JLabel subtitle = new JLabel("Chào mừng bạn quay trở lại !");
        private final JLabel usernameLabel = new JLabel("Tên đăng nhập");
        private final JLabel passwordLabel = new JLabel("Mật khẩu");
        private final JLabel footerLabel = new JLabel("2026 Titi Pet - Hệ thống quản lý thú cưng");
        private final JPanel footer = new JPanel(new GridBagLayout());
        private final JScrollPane scroll;
        private final char echoChar;
        private boolean dangXuLy;
        private boolean wide = true;
        private double scale = 1;

        private LoginView(AuthService authService) {
            super(new GridBagLayout());
            this.authService = authService;
            setBackground(BACKGROUND);

            form.setOpaque(false);
            card.setLayout(new BorderLayout());
            card.add(form, BorderLayout.CENTER);
            formContainer.setOpaque(false);
            formContainer.add(card, BorderLayout.CENTER);

            title.setForeground(DARK_BLUE);
            subtitle.setForeground(MUTED);
            usernameLabel.setForeground(TEXT);
            passwordLabel.setForeground(TEXT);
            usernameLabel.setLabelFor(txtTenDangNhap);
            passwordLabel.setLabelFor(txtMatKhau);

            configureField(txtTenDangNhap, "Nhập tên đăng nhập");
            configureField(txtMatKhau, "Nhập mật khẩu");
            txtTenDangNhap.setName("tenDangNhap");
            txtMatKhau.setName("matKhau");
            echoChar = txtMatKhau.getEchoChar();
            btnHienMatKhau.setFocusable(false);
            btnHienMatKhau.setBorderPainted(false);
            btnHienMatKhau.setContentAreaFilled(false);
            btnHienMatKhau.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnHienMatKhau.setToolTipText("Hiện mật khẩu");
            btnHienMatKhau.getAccessibleContext().setAccessibleName("Hiện mật khẩu");
            txtMatKhau.putClientProperty("JTextField.trailingComponent", btnHienMatKhau);

            btnDangNhap.setName("dangNhap");
            btnDangNhap.setForeground(Color.WHITE);
            btnDangNhap.setBackground(DARK_BLUE);
            btnDangNhap.setFocusPainted(false);
            btnDangNhap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnDangNhap.putClientProperty("JButton.defaultButtonFollowsFocus", false);
            lblThongBao.setForeground(ERROR_COLOR);
            footerLabel.setForeground(new Color(145, 142, 143));
            footer.setOpaque(false);
            addFooterLine(0);
            GridBagConstraints textConstraints = new GridBagConstraints();
            textConstraints.gridx = 1;
            textConstraints.insets = new Insets(0, 14, 0, 14);
            footer.add(footerLabel, textConstraints);
            addFooterLine(2);

            // Khoảng cách tham chiếu theo ảnh mẫu 2000 x 1248.
            addRow(title, 0, 0);
            addRow(subtitle, 1, 12);
            addRow(usernameLabel, 2, 90);
            addRow(txtTenDangNhap, 3, 18);
            addRow(passwordLabel, 4, 52);
            addRow(txtMatKhau, 5, 26);
            addRow(btnDangNhap, 6, 96);
            addRow(lblThongBao, 7, 14);
            addRow(footer, 8, 86);
            GridBagConstraints filler = new GridBagConstraints();
            filler.gridy = 9;
            filler.weighty = 1;
            form.add(Box.createGlue(), filler);

            scroll = new JScrollPane(formContainer);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(20);
            scroll.getViewport().setBackground(BACKGROUND);
            scroll.setPreferredSize(new Dimension(0, 0));
            scroll.setMinimumSize(new Dimension(0, 0));
            artwork.setMinimumSize(new Dimension(0, 0));
            arrangeColumns();

            btnHienMatKhau.addActionListener(event -> togglePassword());
            btnDangNhap.addActionListener(event -> dangNhap());
            DocumentListener listener = new DocumentListener() {
                public void insertUpdate(DocumentEvent event) { updateLoginButton(); }
                public void removeUpdate(DocumentEvent event) { updateLoginButton(); }
                public void changedUpdate(DocumentEvent event) { updateLoginButton(); }
            };
            txtTenDangNhap.getDocument().addDocumentListener(listener);
            txtMatKhau.getDocument().addDocumentListener(listener);
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {
                    updateLayout();
                }
            });
            updateLayout();
            updateLoginButton();
        }

        private void configureField(JTextField field, String placeholder) {
            field.setForeground(TEXT);
            field.setBackground(new Color(238, 238, 238));
            field.putClientProperty("JTextField.placeholderText", placeholder);
            field.putClientProperty("FlatLaf.style",
                    "arc: 24; borderWidth: 1; borderColor: #242424;"
                            + " focusedBorderColor: #0A2D6F; focusWidth: 1; innerFocusWidth: 0");
        }

        private void addRow(JComponent component, int row, int gap) {
            component.putClientProperty("login.gap", gap);
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = row;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            form.add(component, constraints);
        }

        private void addFooterLine(int column) {
            JPanel line = new JPanel();
            line.setBackground(new Color(231, 220, 249));
            line.setPreferredSize(new Dimension(1, 4));
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = column;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            footer.add(line, constraints);
        }

        private void arrangeColumns() {
            removeAll();

            if (wide) {
                GridBagConstraints left = new GridBagConstraints();
                left.gridx = 0;
                left.gridy = 0;
                left.weightx = 0.485;
                left.weighty = 1.0;
                left.fill = GridBagConstraints.BOTH;

                add(artwork, left);
            }

            GridBagConstraints right = new GridBagConstraints();
            right.gridx = wide ? 1 : 0;
            right.gridy = 0;
            right.weightx = wide ? 0.515 : 1.0;
            right.weighty = 1.0;
            right.fill = GridBagConstraints.BOTH;

            add(scroll, right);

            revalidate();
            repaint();
        }
        private void updateLayout() {
            int width = getWidth() > 0 ? getWidth() : Math.max(BREAKPOINT, getPreferredSize().width);
            int height = getHeight() > 0 ? getHeight() : Math.max(600, getPreferredSize().height);
            boolean nextWide = width >= BREAKPOINT;
            if (wide != nextWide) {
                wide = nextWide;
                arrangeColumns();
            }
            scale = Math.max(0.58, Math.min(1.25,
                    Math.min(width / (wide ? 2000.0 : 900.0), height / 1248.0)));
            int sideWidth = wide ? (int) (width * 0.515) : width;
            formContainer.setBorder(BorderFactory.createEmptyBorder(
                    px(95), wide ? px(82) : px(24), px(84), wide ? px(46) : px(24)));
            card.setCornerRadius(px(64));
            card.setBorder(BorderFactory.createEmptyBorder(px(38), px(58), px(38), px(58)));
            title.setFont(font(Font.BOLD, px(70)));
            subtitle.setFont(font(Font.PLAIN, px(36)));
            usernameLabel.setFont(font(Font.PLAIN, px(29)));
            passwordLabel.setFont(font(Font.PLAIN, px(29)));
            txtTenDangNhap.setFont(font(Font.PLAIN, px(24)));
            txtMatKhau.setFont(font(Font.PLAIN, px(24)));
            lblThongBao.setFont(font(Font.PLAIN, px(20)));
            footerLabel.setFont(font(Font.PLAIN, px(23)));
            btnDangNhap.setFont(font(Font.BOLD, px(28)));
            txtTenDangNhap.setMargin(new Insets(px(10), px(16), px(10), px(16)));
            txtMatKhau.setMargin(new Insets(px(10), px(16), px(10), px(16)));
            txtTenDangNhap.putClientProperty("JTextField.leadingIcon", new FormIcon("user", px(40)));
            txtMatKhau.putClientProperty("JTextField.leadingIcon", new FormIcon("lock", px(40)));
            btnHienMatKhau.setIcon(new FormIcon(
                    txtMatKhau.getEchoChar() == 0 ? "eye" : "eye-off", px(48)));
            btnHienMatKhau.setPreferredSize(new Dimension(px(72), px(60)));
            btnDangNhap.setIcon(new FormIcon("arrow", px(36)));
            btnDangNhap.setDisabledIcon(new FormIcon("arrow", px(36)));
            btnDangNhap.setHorizontalTextPosition(SwingConstants.LEFT);
            btnDangNhap.setIconTextGap(px(28));
            btnDangNhap.putClientProperty("FlatLaf.style",
                    "arc: 24; borderWidth: 1; disabledBackground: #E8F5FF;"
                            + " disabledText: #CBCBCB; disabledBorderColor: #D4D4D4");
            for (JComponent field : new JComponent[]{txtTenDangNhap, txtMatKhau, btnDangNhap}) {
                field.setPreferredSize(new Dimension(1, px(72)));
                field.setMinimumSize(new Dimension(0, px(72)));
            }
            GridBagLayout layout = (GridBagLayout) form.getLayout();
            for (Component child : form.getComponents()) {
                if (child instanceof JComponent component
                        && component.getClientProperty("login.gap") instanceof Integer gap) {
                    GridBagConstraints constraints = layout.getConstraints(child);
                    int right = child == txtTenDangNhap || child == txtMatKhau
                            || child == btnDangNhap ? px(60) : 0;
                    constraints.insets = new Insets(px(gap), 0, 0, right);
                    layout.setConstraints(child, constraints);
                }
            }
            // Scroll khi cửa sổ thấp, không ép các ô nhập nhỏ đến mức khó sử dụng.
            formContainer.setPreferredSize(new Dimension(sideWidth, px(1248)));
            revalidate();
            repaint();
        }

        private int px(int value) {
            return Math.max(1, (int) Math.round(value * scale));
        }

        private void togglePassword() {
            boolean visible = txtMatKhau.getEchoChar() == 0;
            txtMatKhau.setEchoChar(visible ? echoChar : (char) 0);
            String action = visible ? "Hiện mật khẩu" : "Ẩn mật khẩu";
            btnHienMatKhau.setToolTipText(action);
            btnHienMatKhau.getAccessibleContext().setAccessibleName(action);
            btnHienMatKhau.setIcon(new FormIcon(visible ? "eye-off" : "eye", px(48)));
        }

        private void updateLoginButton() {
            char[] password = txtMatKhau.getPassword();
            try {
                btnDangNhap.setEnabled(!dangXuLy
                        && !txtTenDangNhap.getText().isBlank() && password.length > 0);
            } finally {
                Arrays.fill(password, '\0');
            }
        }

        private void dangNhap() {
            if (dangXuLy || !btnDangNhap.isEnabled()) {
                return;
            }
            String username = txtTenDangNhap.getText();
            char[] password = txtMatKhau.getPassword();
            dangXuLy = true;
            txtTenDangNhap.setEditable(false);
            txtMatKhau.setEditable(false);
            updateLoginButton();
            btnDangNhap.setText("ĐANG ĐĂNG NHẬP...");
            lblThongBao.setForeground(DARK_BLUE);
            lblThongBao.setText("Đang kiểm tra thông tin...");

            new SwingWorker<NhanVien, Void>() {
                @Override
                protected NhanVien doInBackground() {
                    try {
                        return authService.dangNhap(username, new String(password));
                    } finally {
                        Arrays.fill(password, '\0');
                    }
                }

                @Override
                protected void done() {
                    dangXuLy = false;
                    txtTenDangNhap.setEditable(true);
                    txtMatKhau.setEditable(true);
                    btnDangNhap.setText("ĐĂNG NHẬP");
                    try {
                        NhanVien employee = get();
                        lblThongBao.setText(" ");
                        txtMatKhau.setText("");
                        JOptionPane.showMessageDialog(LoginView.this,
                                "Đăng nhập thành công!\nXin chào: " + employee.getHoTen()
                                        + "\nVai trò: " + employee.getVaiTro(),
                                "TiTiPet", JOptionPane.INFORMATION_MESSAGE);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        showError("Đăng nhập bị gián đoạn.");
                    } catch (ExecutionException exception) {
                        Throwable cause = exception.getCause();
                        showError(cause instanceof IllegalArgumentException
                                ? cause.getMessage()
                                : "Lỗi hệ thống. Vui lòng kiểm tra CSDL.");
                    } finally {
                        updateLoginButton();
                    }
                }
            }.execute();
        }

        private void showError(String message) {
            lblThongBao.setForeground(ERROR_COLOR);
            lblThongBao.setText(message);
        }
    }

    private static final class FormContainer extends JPanel implements Scrollable {
        private final BufferedImage paw = loadImage("images/login/paw.png", true);

        private FormContainer() {
            super(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                double scale = getHeight() / 1248.0;
                g.scale(scale, scale);
                drawImage(g, paw, 18, 1155, 90, 90);
            } finally {
                g.dispose();
            }
        }

        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 20; }
        public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return Math.max(20, visible.height - 20);
        }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getParent().getHeight() >= getPreferredSize().height;
        }
    }

    /** Ảnh trang  */
    private static final class ArtworkPanel extends JPanel {
        private final BufferedImage background = loadImage("images/login/login-background.png", false);
        private final BufferedImage logo = loadImage("images/login/logo.png", true);
        private final BufferedImage pets = loadImage("images/login/img_pets.png", true);
        private final BufferedImage heart = loadImage("images/login/heart.png", true);
        private final BufferedImage grass = loadImage("images/login/grass.png", true);
        private final BufferedImage paw = loadImage("images/login/paw.png", true);
        private final BufferedImage[] features = {
                loadImage("images/login/paw-1.png", true),
                loadImage("icons/login/icon-spa.png", true),
                loadImage("icons/login/icon-cart.png", true),
                loadImage("icons/login/icon-shield.png", true)
        };

        private ArtworkPanel() {
            setBackground(LEFT_BACKGROUND);
            setPreferredSize(new Dimension(0, 0));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                if (background != null) {
                    g.drawImage(background, 0, 0, getWidth() - 20, getHeight(), null);
                }
                double scale = Math.min(getWidth() / 970.0, getHeight() / 1248.0);
                g.translate((getWidth() - 970 * scale) / 2, (getHeight() - 1248 * scale) / 2);
                g.scale(scale, scale);
                drawImage(g, logo, 330, 130, 340, 84);
                centeredText(g, "Chăm sóc thú cưng", 505, 245, 34, Color.WHITE);
                centeredText(g, "Quản lý dễ dàng", 505, 295, 34, Color.WHITE);
                drawImage(g, heart, 320, 316, 110, 100);
                drawImage(g, pets, 80, 413, 805, 380);
                drawImage(g, grass, 0, 675, 275, 125);
                drawImage(g, paw, 850, 20, 110, 105);
                drawImage(g, paw, 35, 430, 110, 105);
                int[] centers = {165, 355, 565, 765};
                String[][] labels = {
                        {"Quản lý", "thú cưng"},
                        {"Đặt lịch dịch", "vụ Spa"},
                        {"Bán phụ kiện", "và sản phẩm"},
                        {"Chăm sóc", "khách hàng"}
                };
                for (int i = 0; i < centers.length; i++) {
                    drawImage(g, features[i], centers[i] - 42, 880, 84, 84);
                    centeredText(g, labels[i][0], centers[i], 1018, 24, Color.WHITE);
                    centeredText(g, labels[i][1], centers[i], 1050, 24, Color.WHITE);
                }
            } finally {
                g.dispose();
            }
        }
    }

    /** Icon*/
    private static final class FormIcon implements Icon {
        private final String type;
        private final int size;

        private FormIcon(String type, int size) {
            this.type = type;
            this.size = size;
        }

        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.translate(x, y);
                g.scale(size / 48.0, size / 48.0);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(component.isEnabled() ? component.getForeground() : new Color(203, 203, 203));
                g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                switch (type) {
                    case "user" -> {
                        g.draw(new Ellipse2D.Double(16, 5, 16, 16));
                        g.drawRoundRect(8, 29, 32, 16, 10, 10);
                    }
                    case "lock" -> {
                        g.drawRoundRect(9, 22, 30, 23, 3, 3);
                        g.drawArc(16, 5, 16, 28, 0, 180);
                    }
                    case "eye", "eye-off" -> {
                        Path2D eye = new Path2D.Double();
                        eye.moveTo(2, 24);
                        eye.curveTo(15, 7, 33, 7, 46, 24);
                        eye.curveTo(33, 41, 15, 41, 2, 24);
                        g.draw(eye);
                        g.draw(new Ellipse2D.Double(18, 18, 12, 12));
                        if ("eye-off".equals(type)) {
                            g.drawLine(3, 5, 45, 43);
                        }
                    }
                    case "arrow" -> {
                        g.drawLine(5, 24, 41, 24);
                        g.drawLine(28, 12, 42, 24);
                        g.drawLine(28, 36, 42, 24);
                    }
                    default -> { }
                }
            } finally {
                g.dispose();
            }
        }
    }

    private static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }

    private static String fontFamily() {
        for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if ("Inter".equalsIgnoreCase(family)) {
                return family;
            }
        }
        return Font.SANS_SERIF;
    }

    /** Load */
    private static BufferedImage loadImage(String name, boolean trimTransparent) {
        try {
            URL resource = LoginFrame.class.getResource("/" + name);
            if (resource == null) {
                resource = LoginFrame.class.getResource("/resources/" + name);
            }
            BufferedImage image;
            if (resource != null) {
                image = ImageIO.read(resource);
            } else {
                Path file = Path.of("resources", name);
                if (!Files.isRegularFile(file)) {
                    System.err.println("Không tìm thấy ảnh đăng nhập: " + name);
                    return null;
                }
                image = ImageIO.read(file.toFile());
            }
            if (image == null || !trimTransparent || !image.getColorModel().hasAlpha()) {
                return image;
            }
            int left = image.getWidth(), top = image.getHeight(), right = -1, bottom = -1;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) >>> 24) > 8) {
                        left = Math.min(left, x);
                        top = Math.min(top, y);
                        right = Math.max(right, x);
                        bottom = Math.max(bottom, y);
                    }
                }
            }
            return right < left ? image : image.getSubimage(left, top, right - left + 1, bottom - top + 1);
        } catch (IOException exception) {
            System.err.println("Không tải được ảnh " + name + ": " + exception.getMessage());
            return null;
        }
    }

    private static void drawImage(Graphics2D g, BufferedImage image, int x, int y, int width, int height) {
        if (image == null) {
            return;
        }
        double scale = Math.min(width / (double) image.getWidth(), height / (double) image.getHeight());
        int w = (int) Math.round(image.getWidth() * scale);
        int h = (int) Math.round(image.getHeight() * scale);
        g.drawImage(image, x + (width - w) / 2, y + (height - h) / 2, w, h, null);
    }

    private static void centeredText(Graphics2D g, String text, int centerX, int y, int size, Color color) {
        g.setFont(font(Font.BOLD, size));
        g.setColor(color);
        g.drawString(text, centerX - g.getFontMetrics().stringWidth(text) / 2, y);
    }
}
