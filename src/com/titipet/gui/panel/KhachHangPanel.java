package com.titipet.gui.panel;

import com.titipet.bus.AuthService;
import com.titipet.bus.KhachHangService;
import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.enums.HangTV;
import com.titipet.entity.enums.VaiTro;
import com.titipet.gui.dialog.ThemKhachHangDialog;
import com.titipet.gui.dialog.CapNhatKhachHangDialog;
import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

/**
 * KhachHangPanel: Màn hình quản lý khách hàng & thẻ VIP.
 * - Tích hợp phân quyền theo vai trò (Thợ Spa không được thêm khách hàng).
 * - Thanh tìm kiếm phong cách Live Search (gõ đến đâu lọc đến đó).
 */
public class KhachHangPanel extends JPanel{
	
	// Đối tượng tầng nghiệp vụ (BUS) để gọi dữ liệu
    private final KhachHangService khachHangService;
    private AuthService authService;
    private VaiTro vaiTroHienTai;
    
    // Nút chức năng phân quyền
    private JButton btnThemKhachHang;
	
    // Thanh tìm kiếm
    private JTextField txtTimKiem;           
    private JLabel lblClearSearch;

    // Bảng dữ liệu và KPI
    private JTable tblKhachHang;
    private DefaultTableModel tableModel;
    private JLabel lblTongThanhVien;
    private JLabel lblHangBac;
    private JLabel lblHangVang;
    private JLabel lblHangKimCuong;
    
    // Nhãn hiển thị ngày và trạng thái ca làm việc
    private JLabel lblNgayHienTai;
    private JLabel lblTrangThaiCa;
    private RoundedPannel statusBadge;
    
    // 2. BẢNG MÀU CHUẨN FLAT DESIGN ĐỒNG BỘ TOÀN HỆ THỐNG
    private static final Color COLOR_BG = new Color(245, 247, 251);           // Nền xám nhạt toàn màn hình
    private static final Color COLOR_PRIMARY_BLUE = new Color(24, 119, 242);    // Xanh dương chủ đạo TiTiPet
    private static final Color COLOR_CARD_BG = Color.WHITE;                    // Nền trắng cho các thẻ card
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);         // Màu chữ đen đậm tiêu đề
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);     // Màu chữ phụ/mờ
    private static final Color COLOR_BORDER = new Color(226, 232, 240);         // Màu đường viền khung input, bảng
    private static final Color COLOR_FB_SEARCH_BG = new Color(240, 242, 245); // Nền xám nhạt
    
    // Định dạng số tiền (Ví dụ: 2,400,000 đ)
    private final DecimalFormat dfTien = new DecimalFormat("#,##0 đ");

    /**
     * Constructor chính: Được gọi từ MainFrame(authService)
     */
    public KhachHangPanel(AuthService authService) {
        this.authService = authService;
        this.khachHangService = new KhachHangService();

        // Lấy vai trò an toàn từ Optional<NhanVien> của AuthService
        if (authService != null && authService.getNhanVienDangNhap().isPresent()) {
            this.vaiTroHienTai = authService.getNhanVienDangNhap().get().getVaiTro();
        } else {
            this.vaiTroHienTai = VaiTro.QUAN_LY;
        }

        initComponents();
        apDungPhanQuyen();
        loadData("");
    }
    
    /**
     * Constructor phụ: Truyền trực tiếp vai trò
     */
    public KhachHangPanel(VaiTro vaiTroNguoiDung) {
        this.vaiTroHienTai = (vaiTroNguoiDung != null) ? vaiTroNguoiDung : VaiTro.QUAN_LY;
        this.khachHangService = new KhachHangService();
        initComponents();
        apDungPhanQuyen();
        loadData("");
    }

    /**
     * Khởi tạo và liên kết các khối giao diện chính theo chiều dọc
     */
    private void initComponents() {
        // Sử dụng BorderLayout cho panel gốc
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);

        // Khối chứa chính xếp theo trục dọc (Y_AXIS)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(COLOR_BG);
        // Thiết lập khoảng đệm (padding) 4 phía cho nội dung
        mainContent.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Khối 1: Header (Tiêu đề module, ngày tháng, trạng thái ca làm việc)
        mainContent.add(createHeaderSection());
        mainContent.add(Box.createVerticalStrut(18)); // Khoảng cách giữa header và thẻ KPI

        // Khối 2: Bốn thẻ KPI thống kê cấp độ thành viên
        mainContent.add(createMetricsSection());
        mainContent.add(Box.createVerticalStrut(20)); // Khoảng cách giữa thẻ KPI và bảng dữ liệu

        // Khối 3: Thẻ lớn chứa thanh tìm kiếm, bảng danh sách khách hàng và ghi chú
        mainContent.add(createCustomerTableSection());

        // Bọc toàn bộ vào JScrollPane để tự động cuộn trang khi thu nhỏ cửa sổ
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null); // Bỏ viền cuộn ngoài
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Tăng tốc độ lăn chuột
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // Không cho cuộn ngang

        add(scrollPane, BorderLayout.CENTER);
    }

    // KHỐI 1: HEADER SECTION
    /**
     * Dựng khối tiêu đề trang và các huy hiệu trạng thái ca làm việc
     */
    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false); // Trong suốt để lấy nền chung

        // Cụm văn bản tiêu đề bên trái
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblSub = new JLabel("TI TI PET - WORKSPACE");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSub.setForeground(COLOR_TEXT_MUTED);

        JLabel lblMain = new JLabel("Quản lý khách hàng & VIP");
        lblMain.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblMain.setForeground(COLOR_TEXT_DARK);

        JLabel lblDesc = new JLabel("Lịch sử mua hàng, cấp thành viên và điểm tích lũy");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(COLOR_TEXT_MUTED);

        titlePanel.add(lblSub);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblMain);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(lblDesc);

        // Cụm huy hiệu hiển thị ngày tháng và ca làm việc bên phải
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        // 1. Huy hiệu ngày: Tự động lấy ngày hiện tại của máy tính
        RoundedPannel dateBadge = new RoundedPannel(Color.WHITE, 12);
        dateBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 6));
        String ngayHomNay = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        lblNgayHienTai = new JLabel(ngayHomNay);
        lblNgayHienTai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNgayHienTai.setForeground(COLOR_TEXT_DARK);
        dateBadge.add(lblNgayHienTai);

        // 2. Huy hiệu trạng thái ca: Mặc định an toàn "Chưa mở ca"
        statusBadge = new RoundedPannel(new Color(254, 243, 199), 12);
        statusBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 6));
        lblTrangThaiCa = new JLabel("● Chưa mở ca");
        lblTrangThaiCa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTrangThaiCa.setForeground(new Color(217, 119, 6));
        statusBadge.add(lblTrangThaiCa);

        rightActions.add(dateBadge);
        rightActions.add(statusBadge);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(rightActions, BorderLayout.EAST);
        return panel;
    }
    
    /**
     * Phương thức public để thành viên làm module "Mở ca / Chốt ca"
     * có thể gọi cập nhật trạng thái từ MainFrame mà không cần can thiệp code nội bộ.
     */
    public void capNhatTrangThaiCa(boolean daMoCa) {
        if (daMoCa) {
            statusBadge.setBackground(new Color(220, 252, 231)); // Nền xanh lá nhạt
            lblTrangThaiCa.setText("● Đang trong ca");
            lblTrangThaiCa.setForeground(new Color(22, 163, 74)); // Chữ xanh lá
        } else {
            statusBadge.setBackground(new Color(254, 243, 199)); // Nền vàng cam
            lblTrangThaiCa.setText("● Chưa mở ca");
            lblTrangThaiCa.setForeground(new Color(217, 119, 6)); // Chữ vàng cam
        }
        statusBadge.repaint();
    }

    // KHỐI 2: 4 THẺ THỐNG KÊ (METRICS KPI)
    /**
     * Dựng hàng ngang gồm 4 thẻ KPI thống kê số lượng thành viên theo từng cấp bậc
     */
    private JPanel createMetricsSection() {
        // Dùng GridLayout 1 dòng 4 cột, khoảng cách giữa các thẻ là 16px
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90)); // Giới hạn chiều cao cố định của hàng thẻ

        // Khởi tạo các nhãn lưu số lượng
        lblTongThanhVien = new JLabel("0");
        lblHangBac = new JLabel("0");
        lblHangVang = new JLabel("0");
        lblHangKimCuong = new JLabel("0");

        // Thêm 4 thẻ KPI với màu nền icon riêng biệt
        grid.add(createMetricCard("👥", lblTongThanhVien, "Thành viên", new Color(239, 246, 255)));
        grid.add(createMetricCard("🥈", lblHangBac, "Hạng Bạc", new Color(241, 245, 249)));
        grid.add(createMetricCard("🥇", lblHangVang, "Hạng Vàng", new Color(254, 243, 199)));
        grid.add(createMetricCard("💎", lblHangKimCuong, "Kim cương", new Color(243, 232, 255)));

        return grid;
    }

    /**
     * Hàm phụ trợ dựng từng thẻ KPI đơn lẻ
     * @param icon Ký tự biểu tượng hoặc Emoji
     * @param lblVal Nhãn chứa giá trị số đếm
     * @param title Tên cấp bậc/chỉ số
     * @param iconBg Màu nền của ô vuông chứa icon
     */
    private JPanel createMetricCard(String icon, JLabel lblVal, String title, Color iconBg) {
        // Dùng RoundedPannel của nhóm để bo góc thẻ trắng
        RoundedPannel card = new RoundedPannel(COLOR_CARD_BG, 14);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 16));

        // Ô vuông bo tròn nhỏ chứa icon bên trái
        RoundedPannel iconBox = new RoundedPannel(iconBg, 12);
        iconBox.setPreferredSize(new Dimension(48, 48));
        iconBox.setLayout(new GridBagLayout()); // Căn icon chính giữa ô
        JLabel lblIco = new JLabel(icon);
        lblIco.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconBox.add(lblIco);

        // Khối văn bản hiển thị số lượng và tên chỉ số bên phải
        JPanel textGroup = new JPanel();
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.setOpaque(false);

        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(COLOR_TEXT_DARK);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        textGroup.add(lblVal);
        textGroup.add(Box.createVerticalStrut(2));
        textGroup.add(lblTitle);

        card.add(iconBox);
        card.add(textGroup);
        return card;
    }

    // KHỐI 3: CARD CHỨA BẢNG VÀ CÁC CÔNG CỤ TÌM KIẾM
    /**
     * Dựng thẻ Card lớn màu trắng bao gồm: Tiêu đề danh sách, thanh tìm kiếm, bảng JTable và banner chính sách
     */
    private JPanel createCustomerTableSection() {
        RoundedPannel card = new RoundedPannel(COLOR_CARD_BG, 16);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        //3.1. HÀNG TIÊU ĐỀ BẢNG VÀ NÚT THÊM MỚI
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel();
        leftTitle.setLayout(new BoxLayout(leftTitle, BoxLayout.Y_AXIS));
        leftTitle.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Danh sách khách hàng thân thiết");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTableTitle.setForeground(COLOR_TEXT_DARK);

        JLabel lblTableSub = new JLabel("1 điểm / 50.000đ - Quy đổi 1 điểm = 1.000đ");
        lblTableSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTableSub.setForeground(COLOR_TEXT_MUTED);

        leftTitle.add(lblTableTitle);
        leftTitle.add(Box.createVerticalStrut(2));
        leftTitle.add(lblTableSub);

        // Nút thêm khách hàng (được phân quyền ẩn/hiện)
        btnThemKhachHang = new JButton("+ Thêm khách hàng");
        btnThemKhachHang.setBackground(COLOR_PRIMARY_BLUE);
        btnThemKhachHang.setForeground(Color.WHITE);
        btnThemKhachHang.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnThemKhachHang.setFocusPainted(false);
        btnThemKhachHang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThemKhachHang.setBorder(new EmptyBorder(10, 18, 10, 18));
        btnThemKhachHang.addActionListener(e -> moDialogThemKhachHang());

        topBar.add(leftTitle, BorderLayout.WEST);
        topBar.add(btnThemKhachHang, BorderLayout.EAST);

        //3.2. THANH TÌM KIẾM VÀ CÁC THAO TÁC XUẤT DỮ LIỆU
        JPanel searchBarRow = new JPanel(new BorderLayout(12, 0));
        searchBarRow.setOpaque(false);

        // Hộp tìm kiếm hình viên thuốc (pill-shape) xám nhạt bo 20px
        RoundedPannel searchBox = new RoundedPannel(COLOR_FB_SEARCH_BG, 20);
        searchBox.setPreferredSize(new Dimension(0, 42));
        searchBox.setLayout(new BorderLayout(8, 0));
        searchBox.setBorder(new EmptyBorder(0, 14, 0, 14));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblSearchIcon.setForeground(COLOR_TEXT_MUTED);

        txtTimKiem = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !(FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    g2.setColor(COLOR_TEXT_MUTED);
                    g2.drawString("Tìm kiếm theo tên khách hàng hoặc số điện thoại...", 5, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        txtTimKiem.setOpaque(false);
        txtTimKiem.setBorder(null);
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTimKiem.setForeground(COLOR_TEXT_DARK);

        lblClearSearch = new JLabel("X");
        lblClearSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblClearSearch.setForeground(COLOR_TEXT_MUTED);
        lblClearSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblClearSearch.setVisible(false);
        lblClearSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                txtTimKiem.setText("");
                txtTimKiem.requestFocus();
            }
        });

        // LIVE SEARCH: Lọc bảng ngay khi gõ từng phím
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { xuLyTimKiem(); }
            @Override
            public void removeUpdate(DocumentEvent e) { xuLyTimKiem(); }
            @Override
            public void changedUpdate(DocumentEvent e) { xuLyTimKiem(); }

            private void xuLyTimKiem() {
                String kw = txtTimKiem.getText().trim();
                lblClearSearch.setVisible(!kw.isEmpty());
                loadData(kw);
            }
        });

        searchBox.add(lblSearchIcon, BorderLayout.WEST);
        searchBox.add(txtTimKiem, BorderLayout.CENTER);
        searchBox.add(lblClearSearch, BorderLayout.EAST);

        searchBarRow.add(searchBox, BorderLayout.CENTER);

        JPanel cardHeaderGroup = new JPanel();
        cardHeaderGroup.setLayout(new BoxLayout(cardHeaderGroup, BoxLayout.Y_AXIS));
        cardHeaderGroup.setOpaque(false);
        cardHeaderGroup.add(topBar);
        cardHeaderGroup.add(Box.createVerticalStrut(14));
        cardHeaderGroup.add(searchBarRow);

        card.add(cardHeaderGroup, BorderLayout.NORTH);

        // 3.3. BẢNG DỮ LIỆU JTABLE HIỂN THỊ KHÁCH HÀNG
        String[] columns = {
                "KHÁCH HÀNG", "SĐT", "THÚ CƯNG", "CẤP ĐỘ", "ĐIỂM HIỆN CÓ", "TỔNG CHI TIÊU GHI NHẬN", "THAO TÁC"
        };

        // Tạo Model cho JTable và khóa chỉnh sửa (chỉ cho phép click ở cột cuối Thao tác)
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Chỉ cột thao tác mới cho phép click tương tác
            }
        };

        tblKhachHang = new JTable(tableModel);
        tblKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblKhachHang.setRowHeight(52); // Đặt chiều cao dòng 52px để bảng thoáng
        tblKhachHang.setShowVerticalLines(false); // Ẩn đường kẻ dọc
        tblKhachHang.setGridColor(new Color(241, 245, 249)); // Màu đường kẻ ngang
        tblKhachHang.setSelectionBackground(new Color(248, 250, 252)); // Nền khi click chọn dòng
        tblKhachHang.setSelectionForeground(COLOR_TEXT_DARK);

        // Định dạng tiêu đề cột (TableHeader)
        tblKhachHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblKhachHang.getTableHeader().setForeground(COLOR_TEXT_MUTED);
        tblKhachHang.getTableHeader().setBackground(COLOR_CARD_BG);
        tblKhachHang.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tblKhachHang.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        // Gán bộ vẽ Renderer riêng cho từng cột
        tblKhachHang.getColumnModel().getColumn(0).setCellRenderer(new CustomerCellRenderer());     // Cột 0: Avatar viết tắt + Tên
        tblKhachHang.getColumnModel().getColumn(1).setCellRenderer(new DefaultAlignRenderer(SwingConstants.LEFT)); // Cột 1: SĐT
        tblKhachHang.getColumnModel().getColumn(2).setCellRenderer(new DefaultAlignRenderer(SwingConstants.LEFT)); // Cột 2: Tên thú cưng
        tblKhachHang.getColumnModel().getColumn(3).setCellRenderer(new RankBadgeRenderer());        // Cột 3: Huy hiệu cấp bậc màu
        tblKhachHang.getColumnModel().getColumn(4).setCellRenderer(new PointCellRenderer());        // Cột 4: Điểm thưởng kèm biểu tượng dấu chân
        tblKhachHang.getColumnModel().getColumn(5).setCellRenderer(new DefaultAlignRenderer(SwingConstants.LEFT)); // Cột 5: Tổng tiền chi tiêu
        tblKhachHang.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());    // Cột 6: Nút bấm xem hồ sơ
        tblKhachHang.getColumnModel().getColumn(6).setCellEditor(new ActionButtonEditor(new JCheckBox()));

        // Đặt bảng vào JScrollPane có viền mờ
        JScrollPane tableScroll = new JScrollPane(tblKhachHang);
        tableScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDER));
        tableScroll.getViewport().setBackground(COLOR_CARD_BG);

        card.add(tableScroll, BorderLayout.CENTER);

        return card;
    }
   
    // 4. LOGIC PHÂN QUYỀN
    /**
     * PHÂN QUYỀN: Thợ Spa không được phép thêm khách hàng
     */
    private void apDungPhanQuyen() {
        boolean laThoSpa;
        if (authService != null) {
            laThoSpa = authService.coVaiTro(VaiTro.THO_SPA);
        } else {
            laThoSpa = (vaiTroHienTai == VaiTro.THO_SPA);
        }

        // Ẩn nút đối với Thợ Spa, hiển thị cho Quản lý và Thu ngân
        btnThemKhachHang.setVisible(!laThoSpa);
    }

    public void setVaiTroHienTai(VaiTro vaiTroMoi) {
        this.vaiTroHienTai = vaiTroMoi;
        apDungPhanQuyen();
    }
    
    /**
     * Tự động duyệt qua toàn bộ dữ liệu từng dòng để tính toán bề rộng lý tưởng cho từng cột,
     * đảm bảo họ tên dài không bao giờ bị cắt chữ.
     */
    private void tuDongCanChinhDoRongCot(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        for (int col = 0; col < table.getColumnCount(); col++) {
            // Lấy độ rộng của tiêu đề cột
            TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
            Component headerComp = headerRenderer.getTableCellRendererComponent(
                    table, table.getColumnModel().getColumn(col).getHeaderValue(), false, false, 0, col
            );
            int width = headerComp.getPreferredSize().width + 25; // Thêm padding đệm viền

            // Quét qua các dòng để tìm ô có text dài nhất trong cột
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                width = Math.max(comp.getPreferredSize().width + 25, width);
            }

            // Đặt giới hạn tối thiểu cho cột để bảng thoáng đẹp
            if (col == 0) width = Math.max(width, 180); // Cột Khách Hàng tối thiểu 180px
            if (col == 2) width = Math.max(width, 140); // Cột Thú Cưng tối thiểu 140px

            table.getColumnModel().getColumn(col).setPreferredWidth(width);
        }
    }

    // 5. PHƯƠNG THỨC TẢI VÀ NẠP DỮ LIỆU TỪ TẦNG NGHIỆP VỤ (BUS)
    /**
     * Tải dữ liệu từ database thông qua tầng BUS, đổ lên JTable và tính toán lại 4 thẻ KPI[cite: 1]
     * @param keyword Từ khóa tìm kiếm (họ tên hoặc SĐT)
     */
    private void loadData(String keyword) {
        // Xóa trắng bảng cũ trước khi nạp lại
        tableModel.setRowCount(0);
        int countTong = 0;
        int countBac = 0;
        int countVang = 0;
        int countKimCuong = 0;

        try {
            // Lấy danh sách dòng dữ liệu trực tiếp từ KhachHangService
            List<Object[]> rows = khachHangService.layDanhSachHienThiBang(keyword);

            for (Object[] r : rows) {
                countTong++;
                // Lấy enum HangTV từ mảng dữ liệu để phân loại và đếm số lượng cho 4 thẻ KPI
                HangTV hang = (HangTV) r[3];
                if (hang == HangTV.HANG_1) countBac++;
                else if (hang == HangTV.HANG_2) countVang++;
                else if (hang == HangTV.HANG_3) countKimCuong++;

                // Thêm một dòng vào TableModel
                tableModel.addRow(new Object[]{
                        r[0],                                       // Cột 0: Họ tên
                        r[1],                                       // Cột 1: Số điện thoại
                        r[2],                                       // Cột 2: Danh sách tên thú cưng
                        hang,                                       // Cột 3: Enum Hạng thành viên
                        r[4],                              			// Cột 4: Điểm kèm icon
                        dfTien.format((BigDecimal) r[5]),           // Cột 5: Định dạng tiền Việt Nam
                        r[6]                                        // Cột 6: Mã KH làm tham số định danh
                });
            }

            // Cập nhật lại số liệu hiển thị trên 4 thẻ KPI
            lblTongThanhVien.setText(String.valueOf(countTong));
            lblHangBac.setText(String.valueOf(countBac));
            lblHangVang.setText(String.valueOf(countVang));
            lblHangKimCuong.setText(String.valueOf(countKimCuong));
            
            // GỌI CĂN CHỈNH BỀ RỘNG CỘT
            tuDongCanChinhDoRongCot(tblKhachHang);

        } catch (Exception e) {
            // Hiển thị thông báo khi có lỗi truy vấn dữ liệu từ CSDL
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mở hộp thoại popup Thêm khách hàng mới và tải lại bảng sau khi đóng
     */
    private void moDialogThemKhachHang() {
        ThemKhachHangDialog dialog = new ThemKhachHangDialog((Frame) SwingUtilities.getWindowAncestor(this), khachHangService);
        dialog.setVisible(true);
        txtTimKiem.setText(""); // Xóa text trên ô tìm kiếm
        // Nạp lại dữ liệu để cập nhật khách hàng mới vừa thêm vào bảng
        loadData("");
    }

    // 6. CÁC LỚP RENDERER ĐỊNH DẠNG TÙY BIẾN CHO JTABLE

    /**
     * CustomerCellRenderer: Vẽ ô chứa avatar tròn viết tắt 2 chữ cái đầu cùng họ tên khách hàng
     */
    private static class CustomerCellRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblAvatar = new JLabel("", SwingConstants.CENTER);
        private final JLabel lblName = new JLabel();

        public CustomerCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
            setOpaque(true);

            // Tạo khung avatar hình vuông bo nhẹ với màu nền tím nhạt
            lblAvatar.setPreferredSize(new Dimension(36, 36));
            lblAvatar.setOpaque(true);
            lblAvatar.setBackground(new Color(224, 231, 255));
            lblAvatar.setForeground(new Color(67, 56, 202));
            lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 12));

            lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblName.setForeground(COLOR_TEXT_DARK);

            add(lblAvatar);
            add(lblName);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String fullName = value != null ? value.toString() : "";
            lblName.setText(fullName);
            lblAvatar.setText(getInitials(fullName)); // Lấy 2 chữ cái đầu làm avatar
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }

        /**
         * Rút gọn họ tên thành 2 chữ cái viết tắt (Ví dụ: "Hồng Nhiên" -> "HN")
         */
        private String getInitials(String name) {
            if (name == null || name.trim().isEmpty()) return "KH";
            String[] parts = name.trim().split("\\s+");
            if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
    }

    /**
     * RankBadgeRenderer: Vẽ huy hiệu bo góc đổi màu tự động theo từng cấp hạng thành viên
     */
    private static class RankBadgeRenderer extends JPanel implements TableCellRenderer {
        private final JLabel lblBadge = new JLabel("", SwingConstants.CENTER);

        public RankBadgeRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 12));
            setOpaque(true);

            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblBadge.setOpaque(true);
            lblBadge.setBorder(new EmptyBorder(4, 10, 4, 10)); // Khoảng đệm bên trong huy hiệu
            add(lblBadge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            HangTV hang = (value instanceof HangTV) ? (HangTV) value : HangTV.THUONG;

            // Thiết lập màu sắc và nhãn tương ứng với từng cấp hạng
            switch (hang) {
                case HANG_1 -> {
                    lblBadge.setText("Bạc");
                    lblBadge.setBackground(new Color(241, 245, 249));
                    lblBadge.setForeground(new Color(71, 85, 105));
                }
                case HANG_2 -> {
                    lblBadge.setText("Vàng");
                    lblBadge.setBackground(new Color(254, 243, 199));
                    lblBadge.setForeground(new Color(217, 119, 6));
                }
                case HANG_3 -> {
                    lblBadge.setText("Kim cương");
                    lblBadge.setBackground(new Color(243, 232, 255));
                    lblBadge.setForeground(new Color(147, 51, 234));
                }
                default -> {
                    lblBadge.setText("Thường");
                    lblBadge.setBackground(new Color(243, 244, 246));
                    lblBadge.setForeground(new Color(107, 114, 128));
                }
            }

            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    /**
     * DefaultAlignRenderer: Căn chỉnh lề trái và tạo khoảng đệm chữ cho các cột cơ bản
     */
    private static class DefaultAlignRenderer extends DefaultTableCellRenderer {
        public DefaultAlignRenderer(int alignment) {
            setHorizontalAlignment(alignment);
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }
    }

    /**
     * PointCellRenderer: Định dạng in đậm cho cột điểm thưởng
     */
    private static class PointCellRenderer extends DefaultTableCellRenderer {
        public PointCellRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(COLOR_TEXT_DARK);
            setBorder(new EmptyBorder(0, 10, 0, 10));
        }
    }

    /**
     * ActionButtonRenderer: Vẽ nút bấm "Hồ sơ / Sửa" trong từng dòng của bảng
     */
    private static class ActionButtonRenderer extends JButton implements TableCellRenderer {
        public ActionButtonRenderer() {
            setText("Sửa");
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(COLOR_PRIMARY_BLUE);
            setBackground(new Color(239, 246, 255));
            setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254), 1));
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    /**
     * ActionButtonEditor: Xử lý sự kiện khi người dùng click vào nút "Sửa" trong bảng
     */
    private class ActionButtonEditor extends DefaultCellEditor {
        private final JButton btn;
        private String maKH;
        private int selectedRow;

        public ActionButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            btn = new JButton("Sửa");
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setForeground(COLOR_PRIMARY_BLUE);
            btn.setBackground(new Color(239, 246, 255));
            btn.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254), 1));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Bắt sự kiện khi click nút "Sửa"
            btn.addActionListener(e -> {
                fireEditingStopped(); // Ngừng trạng thái chỉnh sửa của cell
                
                // Lấy tổng chi tiêu từ cột thứ 5 của dòng đang chọn
                BigDecimal tongChiTieu = BigDecimal.ZERO;
                try {
                    // Cột 5 chứa định dạng tiền (ví dụ: "2,400,000 đ"), chuyển đổi lại thành số nếu cần
                    // Hoặc tra cứu nhanh trực tiếp từ Service
                    String valTien = tableModel.getValueAt(selectedRow, 5).toString()
                            .replace(" đ", "").replace(",", "").replace(".", "");
                    tongChiTieu = new BigDecimal(valTien);
                } catch (Exception ignored) {}

                // Mở Dialog Cập nhật khách hàng
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(KhachHangPanel.this);
                CapNhatKhachHangDialog dialog = new CapNhatKhachHangDialog(
                        parentFrame, khachHangService, maKH, tongChiTieu
                );
                dialog.setVisible(true);

                // Tải lại bảng sau khi hoàn tất chỉnh sửa để cập nhật dữ liệu mới nhất
                loadData(txtTimKiem.getText());
            });
        }
        
       
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.selectedRow = row;
            this.maKH = (value != null) ? value.toString() : "";
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return maKH;
        }
    }
}
