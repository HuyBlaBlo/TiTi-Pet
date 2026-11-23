package com.titipet.gui;

import com.titipet.bus.AuthService;
import com.titipet.connectDB.ConnectDB;
import com.titipet.util.RoundedPannelLinear;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

/**
 * Splash screen khởi động ứng dụng TiTiPet.
 * Hiển thị tiến trình nạp tài nguyên, cấu hình, kết nối CSDL và khởi tạo dịch vụ xác thực.
 */
public class SplashFrame extends JFrame {
    // Bảng màu đồng bộ từ LoginFrame
    private static final Color DARK_BLUE = new Color(10, 45, 111);
    private static final Color GRADIENT_TOP = new Color(245, 248, 254);
    private static final Color GRADIENT_BOTTOM = new Color(210, 230, 255);
    private static final Color MUTED = new Color(110, 120, 138);
    private static final Color BORDER_COLOR = new Color(185, 205, 235);
    private static final String FONT_FAMILY = resolveFontFamily();

    private final JLabel lblStatus = new JLabel("Đang chuẩn bị khởi động...");
    private final JLabel lblPercent = new JLabel("0%");
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    public SplashFrame() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Trong suốt xung quanh góc bo tròn

        initUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        // Nền bo góc chuyển màu tuyến tính
        RoundedPannelLinear rootPanel = new RoundedPannelLinear(
                GRADIENT_TOP,
                GRADIENT_BOTTOM,
                28,
                RoundedPannelLinear.Direction.VERTICAL
        );
        rootPanel.setLayout(new BorderLayout(0, 12));
        rootPanel.setBorder(new EmptyBorder(22, 28, 24, 28));
        rootPanel.setPreferredSize(new Dimension(540, 360));

        // 1. TOP: Nhãn phân hệ và phiên bản
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblSubsystem = new JLabel("DESKTOP POS & SPA MANAGEMENT");
        lblSubsystem.setFont(font(Font.BOLD, 12));
        lblSubsystem.setForeground(DARK_BLUE);

        JLabel lblVersion = new JLabel("v1.0.0");
        lblVersion.setFont(font(Font.BOLD, 12));
        lblVersion.setForeground(MUTED);

        topPanel.add(lblSubsystem, BorderLayout.WEST);
        topPanel.add(lblVersion, BorderLayout.EAST);
        rootPanel.add(topPanel, BorderLayout.NORTH);

        // 2. CENTER: Logo/Biểu tượng, Tên phần mềm, Mô tả, Đội ngũ phát triển
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        centerPanel.add(Box.createVerticalGlue());

        LogoComponent logoComp = new LogoComponent();
        logoComp.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(logoComp);

        centerPanel.add(Box.createVerticalStrut(10));

        JLabel lblTitle = new JLabel("TiTiPet");
        lblTitle.setFont(font(Font.BOLD, 30));
        lblTitle.setForeground(DARK_BLUE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblTitle);

        centerPanel.add(Box.createVerticalStrut(4));

        JLabel lblDesc = new JLabel("Hệ thống quản lý dịch vụ & chăm sóc thú cưng chuyên nghiệp");
        lblDesc.setFont(font(Font.PLAIN, 13));
        lblDesc.setForeground(MUTED);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblDesc);

        centerPanel.add(Box.createVerticalStrut(6));

        JLabel lblDev = new JLabel("Phát triển bởi Nhóm 04 - DHKTPM19A");
        lblDev.setFont(font(Font.ITALIC, 11));
        lblDev.setForeground(new Color(130, 140, 155));
        lblDev.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblDev);

        centerPanel.add(Box.createVerticalGlue());

        rootPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. BOTTOM: Trạng thái nạp, %, ProgressBar
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);

        lblStatus.setFont(font(Font.PLAIN, 12));
        lblStatus.setForeground(DARK_BLUE);

        lblPercent.setFont(font(Font.BOLD, 12));
        lblPercent.setForeground(DARK_BLUE);

        statusRow.add(lblStatus, BorderLayout.WEST);
        statusRow.add(lblPercent, BorderLayout.EAST);
        bottomPanel.add(statusRow);

        bottomPanel.add(Box.createVerticalStrut(8));

        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setForeground(DARK_BLUE);
        progressBar.setBackground(new Color(230, 238, 248));
        progressBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 10));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        bottomPanel.add(progressBar);

        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    /**
     * Bắt đầu tiến trình tải ngầm qua SwingWorker.
     */
    public void startLoading() {
        SwingWorker<AuthService, Object[]> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected AuthService doInBackground() {
                try {
                    // Bước 1: Khởi tạo tài nguyên UI
                    publishProgress(15, "Đang tải tài nguyên giao diện...");
                    sleepSmoothly(250);

                    // Bước 2: Đọc cấu hình hệ thống
                    publishProgress(35, "Đang kiểm tra cấu hình hệ thống...");
                    sleepSmoothly(250);

                    // Bước 3: Kết nối cơ sở dữ liệu
                    publishProgress(60, "Đang kết nối cơ sở dữ liệu...");
                    ConnectDB.getInstance().connect();
                    sleepSmoothly(300);

                    // Bước 4: Khởi tạo dịch vụ ứng dụng
                    publishProgress(85, "Đang khởi tạo dịch vụ xác thực...");
                    AuthService authService = new AuthService();
                    sleepSmoothly(250);

                    // Hoàn tất
                    publishProgress(100, "Khởi động hoàn tất!");
                    sleepSmoothly(200);

                    return authService;
                } catch (SQLException ex) {
                    errorMessage = "Không thể kết nối cơ sở dữ liệu!\n" + ex.getMessage();
                    publishProgress(100, "Lỗi kết nối cơ sở dữ liệu.");
                    return null;
                } catch (Exception ex) {
                    errorMessage = "Đã xảy ra lỗi trong quá trình khởi động:\n" + ex.getMessage();
                    publishProgress(100, "Lỗi khởi động hệ thống.");
                    return null;
                }
            }

            private void publishProgress(int progress, String message) {
                publish(new Object[]{progress, message});
            }

            private void sleepSmoothly(long ms) {
                try {
                    Thread.sleep(ms);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            protected void process(List<Object[]> chunks) {
                if (!chunks.isEmpty()) {
                    Object[] latest = chunks.get(chunks.size() - 1);
                    int val = (Integer) latest[0];
                    String text = (String) latest[1];
                    progressBar.setValue(val);
                    lblPercent.setText(val + "%");
                    lblStatus.setText(text);
                }
            }

            @Override
            protected void done() {
                AuthService authService = null;
                try {
                    authService = get();
                } catch (Exception ignored) {
                }

                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(
                            SplashFrame.this,
                            errorMessage,
                            "Lỗi kết nối CSDL",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

                dispose();

                // Chuyển sang màn hình Đăng nhập
                AuthService serviceToUse = (authService != null) ? authService : new AuthService();
                LoginFrame loginFrame = new LoginFrame(serviceToUse);
                loginFrame.setVisible(true);
            }
        };

        worker.execute();
    }

    private static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }

    private static String resolveFontFamily() {
        for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if ("Inter".equalsIgnoreCase(family)) {
                return family;
            }
        }
        return Font.SANS_SERIF;
    }

    /**
     * Component hiển thị logo hình ảnh nếu tồn tại, nếu không sẽ vẽ biểu tượng paw vector bằng Graphics2D.
     */
    private static final class LogoComponent extends JComponent {
        private final BufferedImage logoImg;

        private LogoComponent() {
            setPreferredSize(new Dimension(80, 80));
            setMinimumSize(new Dimension(80, 80));
            setMaximumSize(new Dimension(80, 80));
            logoImg = loadLogoImage();
        }

        private BufferedImage loadLogoImage() {
            String[] candidatePaths = {
                    "/resources/icons/logo.png",
                    "/icons/logo.png",
                    "/resources/images/login/logo.png",
                    "/images/login/logo.png"
            };

            for (String p : candidatePaths) {
                try {
                    URL res = SplashFrame.class.getResource(p);
                    if (res != null) {
                        return ImageIO.read(res);
                    }
                } catch (IOException ignored) {
                }
            }

            // Kiểm tra file trên ổ đĩa
            Path[] fileCandidates = {
                    Path.of("resources", "icons", "logo.png"),
                    Path.of("resources", "images", "login", "logo.png")
            };
            for (Path f : fileCandidates) {
                if (Files.isRegularFile(f)) {
                    try {
                        return ImageIO.read(f.toFile());
                    } catch (IOException ignored) {
                    }
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                int w = getWidth();
                int h = getHeight();

                if (logoImg != null) {
                    double scale = Math.min((double) w / logoImg.getWidth(), (double) h / logoImg.getHeight());
                    int drawW = (int) Math.round(logoImg.getWidth() * scale);
                    int drawH = (int) Math.round(logoImg.getHeight() * scale);
                    int x = (w - drawW) / 2;
                    int y = (h - drawH) / 2;
                    g2.drawImage(logoImg, x, y, drawW, drawH, null);
                } else {
                    // Vẽ biểu tượng vector dấu chân thú cưng (Paw print)
                    drawVectorPaw(g2, w, h);
                }
            } finally {
                g2.dispose();
            }
        }

        private void drawVectorPaw(Graphics2D g2, int w, int h) {
            // Nền huy hiệu tròn nhẹ nhàng
            g2.setColor(new Color(10, 45, 111, 20));
            g2.fillOval(4, 4, w - 8, h - 8);

            // Màu paw
            g2.setColor(DARK_BLUE);

            // Đệm chính (main pad)
            RoundRectangle2D mainPad = new RoundRectangle2D.Double(
                    w * 0.26, h * 0.46,
                    w * 0.48, h * 0.36,
                    22, 22
            );
            g2.fill(mainPad);

            // 4 ngón chân (toes)
            g2.fill(new Ellipse2D.Double(w * 0.18, h * 0.28, w * 0.16, h * 0.22));
            g2.fill(new Ellipse2D.Double(w * 0.36, h * 0.16, w * 0.16, h * 0.24));
            g2.fill(new Ellipse2D.Double(w * 0.54, h * 0.16, w * 0.16, h * 0.24));
            g2.fill(new Ellipse2D.Double(w * 0.72, h * 0.28, w * 0.16, h * 0.22));
        }
    }
}
