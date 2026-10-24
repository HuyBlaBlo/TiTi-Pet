package com.titipet.gui.panel;

import com.titipet.bus.BanHangKhachHangService;
import com.titipet.bus.BanHangService;
import com.titipet.bus.BanHangTinhTienService;
import com.titipet.entity.KhachHangBanHang;
import com.titipet.entity.SanPhamBanHang;
import com.titipet.gui.PosUI;
import com.titipet.gui.PosIcons;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * POS theo bố cục mẫu POS.png (GUI -> BUS -> DAO).
 * Giao diện và giỏ hàng hoạt động; nút thanh toán chưa ghi CSDL, tránh tạo hóa đơn giả.
 * Khi tích hợp MainFrame, chỉ nhúng new BanHangPanel() trong vùng nội dung.
 */
public class BanHangPanel extends JPanel {
    private static final Color BLUE = PosUI.BLUE;
    private static final Color NAVY = PosUI.NAVY;
    private static final Color MUTED = PosUI.MUTED;
    private static final Color WHITE = PosUI.WHITE;
    private static final Color BORDER = PosUI.BORDER;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private static final String[] CATEGORIES = {"Tất cả", "Thức ăn cho mèo", "Phụ kiện", "Đồ chơi", "Vệ sinh"};

    private final BanHangService sales;
    private final BanHangKhachHangService customerService;
    private final BanHangTinhTienService billing = new BanHangTinhTienService();
    private List<SanPhamBanHang> products = new ArrayList<>();
    private KhachHangBanHang customer;
    private String selectedCategory = "Tất cả";
    private int currentColumns = 4;

    private final JTextField search = PosUI.field("Nhập mã vạch, tên sản phẩm...");
    private final PosUI.StyledButton searchButton = PosUI.button("Tìm", BLUE, PosUI.SOFT_BLUE, null);
    private final JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
    private final JPanel productGrid = new JPanel(new GridLayout(0, 4, 13, 13));
    private JScrollPane productsScroll;
    private final JPanel cartList = new JPanel();
    private final JPanel cartBox = new JPanel(new BorderLayout());
    private final JLabel cartCount = PosUI.text("0 sản phẩm / dịch vụ", 11, false, MUTED);
    private final JLabel customerText = PosUI.text("Khách lẻ — chưa chọn khách hàng", 13, false, NAVY);
    private final JLabel tier = PosUI.text("Khách lẻ", 10, true, BLUE);
    private final JLabel points = PosUI.text("0 điểm (= 0 ₫)", 11, false, MUTED);
    private final JLabel maxPoints = PosUI.text("Tối đa 0 điểm", 10, false, MUTED);
    private final JTextField pointInput = PosUI.field("0");
    private final JLabel subtotal = PosUI.text("0 ₫", 12, true, NAVY);
    private final JLabel tierDiscount = PosUI.text("− 0 ₫", 12, true, NAVY);
    private final JLabel promoDiscount = PosUI.text("− 0 ₫", 12, true, NAVY);
    private final JLabel pointDiscount = PosUI.text("− 0 ₫", 12, true, NAVY);
    private final JLabel amountDue = PosUI.text("0 ₫", 19, true, BLUE);
    private final JLabel status = PosUI.text("Sẵn sàng", 11, false, MUTED);
    private final PosUI.StyledButton checkout = PosUI.button("Tính tiền / Xem thanh toán", WHITE, BLUE, null);
    private JComboBox<String> paymentMethod;
    private JComboBox<String> orderSource;

    public BanHangPanel() {
        this(new BanHangService(), new BanHangKhachHangService(), true);
    }
    public BanHangPanel(BanHangService sales) {
        this(sales, new BanHangKhachHangService(), true);
    }
    public BanHangPanel(BanHangService sales, BanHangKhachHangService customerService, boolean autoLoad) {
        this.sales = sales;
        this.customerService = customerService;
        setBackground(PosUI.BG);
        setLayout(new BorderLayout());
        setupInterface();
        wireEvents();
        if (autoLoad) SwingUtilities.invokeLater(this::loadProducts);
    }

    /** Dùng duy nhất trong bản preview tách biệt; dữ liệu thật lấy từ SQL. */
    public void showPreviewCustomer(KhachHangBanHang customer) { setCustomer(customer); }

    private void setupInterface() {
        JPanel main = new JPanel(new BorderLayout(16, 16));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(44, 28, 26, 28));
        add(main, BorderLayout.CENTER);
        main.add(createPageHeading(), BorderLayout.NORTH);

        JPanel left = createCatalog();
        JComponent right = createInvoice();
        JPanel columns = new JPanel(new BorderLayout(16, 0));
        columns.setOpaque(false);
        columns.add(left, BorderLayout.CENTER);
        columns.add(right, BorderLayout.EAST);
        columns.setPreferredSize(new Dimension(1200, 770));
        columns.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent event) {
                int w = columns.getWidth();
                int target = Math.max(365, Math.round(w * 0.395f));
                if (right.getPreferredSize().width != target) {
                    right.setPreferredSize(new Dimension(target, 760));
                    columns.revalidate();
                }
            }
        });
        right.setPreferredSize(new Dimension(465, 760));
        main.add(columns, BorderLayout.CENTER);
    }

    private JComponent createPageHeading() {
        JPanel heading = new JPanel(new BorderLayout(14, 0));
        heading.setOpaque(false);
        JPanel titles = PosUI.vertical(0);
        titles.add(PosUI.text("T I T I P E T  ·  W O R K S P A C E", 10, true, BLUE));
        titles.add(PosUI.space(6));
        titles.add(PosUI.text("Bán hàng tại quầy — POS", 28, true, NAVY));
        titles.add(PosUI.space(5));
        titles.add(PosUI.text("Thanh toán dịch vụ và sản phẩm, áp dụng chính sách thành viên", 13, false, MUTED));
        heading.add(titles, BorderLayout.CENTER);
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 1));
        tools.setOpaque(false);
        tools.add(PosUI.button(DATE.format(LocalDate.now()), MUTED, WHITE, BORDER));
        tools.add(PosUI.button("● Chưa mở ca", new Color(170, 111, 28), new Color(255, 243, 222), null));
        PosUI.StyledButton flow = PosUI.button("Hướng dẫn sử dụng", NAVY, WHITE, BORDER);
        flow.setIcon(PosIcons.of("help", 15, NAVY));
        flow.addActionListener(e -> message(
                "HƯỚNG DẪN SỬ DỤNG POS\n\n" +
                "1. Gõ tên, mã hoặc mã vạch sản phẩm rồi nhấn Tìm.\n" +
                "2. Nhấn dấu + trên thẻ sản phẩm để thêm vào giỏ.\n" +
                "3. Sửa số lượng bằng + / - hoặc xóa sản phẩm trong giỏ.\n" +
                "4. Chọn khách hàng để tính ưu đãi thành viên và điểm.\n" +
                "5. Chọn nguồn đơn hàng, phương thức thanh toán.\n" +
                "6. Xem tổng cần thanh toán ở cuối khung bên phải.\n" +
                "7. Nhấn Tính tiền để nhập tiền khách đưa và xem tiền thối.\n\n" +
                "Lưu ý: Bản POS này mới tính tiền, chưa lưu hóa đơn vào SQL Server."));
        tools.add(flow);
        heading.add(tools, BorderLayout.EAST);
        heading.setPreferredSize(new Dimension(100, 106));
        return heading;
    }

    private JPanel createCatalog() {
        PosUI.RoundPanel box = PosUI.roundPanel(WHITE, 18);
        box.setLayout(new BorderLayout(0, 26));
        box.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JPanel top = PosUI.vertical(0);
        JLabel catalogTitle = PosUI.text("Danh mục sản phẩm & dịch vụ", 16, true, NAVY);
        catalogTitle.setIcon(PosIcons.of("box", 17, BLUE));
        catalogTitle.setIconTextGap(7);
        top.add(catalogTitle);
        top.add(PosUI.text("Chọn nhanh mặt hàng hoặc nhập mã vạch", 11, false, MUTED));
        top.add(PosUI.space(28));
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(search, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(searchButton);
        PosUI.StyledButton spa = PosUI.button("+ Dịch vụ Spa", NAVY, WHITE, BORDER);
        spa.addActionListener(e -> message("Nút Dịch vụ Spa có trong mẫu thiết kế.\nViệc tạo chi tiết dịch vụ cần tích hợp module Spa của nhóm."));
        actions.add(spa);
        searchRow.add(actions, BorderLayout.EAST);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(searchRow);
        top.add(PosUI.space(20));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(chips);
        setChips();
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(top, BorderLayout.NORTH);

        productGrid.setOpaque(false);
        productGrid.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JPanel gridHolder = new JPanel(new BorderLayout());
        gridHolder.setOpaque(false);
        gridHolder.add(productGrid, BorderLayout.NORTH);
        productsScroll = new JScrollPane(gridHolder);
        productsScroll.setOpaque(false);
        productsScroll.getViewport().setOpaque(false);
        productsScroll.setBorder(null);
        productsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        productsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        productsScroll.getVerticalScrollBar().setUnitIncrement(17);
        productsScroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int width = productsScroll.getViewport().getWidth();
                int col = width >= 715 ? 4 : width >= 525 ? 3 : width >= 365 ? 2 : 1;
                if (col != currentColumns) {
                    currentColumns = col;
                    refreshProductCards();
                }
            }
        });
        box.add(productsScroll, BorderLayout.CENTER);

        PosUI.RoundPanel note = PosUI.roundPanel(new Color(238, 245, 255), 12);
        note.setLayout(new BorderLayout());
        note.setBorder(BorderFactory.createEmptyBorder(10, 13, 10, 13));
        note.add(PosUI.text("Hàng hết hạn, hết tồn hoặc chưa có trong danh mục sẽ không thể đưa vào giỏ. Quét mã vạch được mô phỏng qua ô tìm kiếm.", 11, false, new Color(90, 126, 177)));
        note.setPreferredSize(new Dimension(100, 45));
        box.add(note, BorderLayout.SOUTH);
        return box;
    }

    private void setChips() {
        chips.removeAll();
        for (String category : CATEGORIES) {
            boolean on = category.equals(selectedCategory);
            PosUI.StyledButton c = PosUI.button(category, on ? BLUE : new Color(104, 132, 171),
                    on ? PosUI.SOFT_BLUE : PosUI.PALE, on ? new Color(187, 212, 249) : BORDER);
            c.addActionListener(e -> {
                selectedCategory = category;
                setChips();
                refreshProductCards();
            });
            chips.add(c);
        }
        chips.revalidate(); chips.repaint();
    }

    private JPanel createInvoice() {
        PosUI.RoundPanel outer = PosUI.roundPanel(WHITE, 18);
        outer.setLayout(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));
        JPanel body = PosUI.vertical(0);
        body.add(createInvoiceHeading());
        body.add(PosUI.space(16));
        body.add(createCustomerBox());
        body.add(PosUI.space(15));
        body.add(createCartArea());
        body.add(PosUI.space(15));
        body.add(PosUI.line());
        body.add(PosUI.space(15));
        body.add(createTransactionControls());
        body.add(PosUI.space(12));

        // Vùng nhập thông tin được phép cuộn, kể cả khi cửa sổ nhỏ.
        // Tổng tiền và nút Tính tiền LUÔN HIỆN ở chân khung bên phải.
        JScrollPane scroll = new JScrollPane(body);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        outer.add(scroll, BorderLayout.CENTER);

        JPanel footer = PosUI.vertical(0);
        footer.setOpaque(true);
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(11, 0, 2, 0)));
        footer.add(createTotals());
        footer.add(PosUI.space(11));
        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setOpaque(false);
        checkout.setPreferredSize(new Dimension(100, 42));
        actionRow.add(checkout, BorderLayout.CENTER);
        footer.add(actionRow);
        footer.add(PosUI.space(6));
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(status);
        outer.add(footer, BorderLayout.SOUTH);
        return outer;
    }

    private JComponent createInvoiceHeading() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JPanel title = PosUI.vertical(0);
        JLabel receipt = PosUI.text("Hóa đơn hiện tại", 16, true, NAVY);
        receipt.setIcon(PosIcons.of("cart", 17, BLUE));
        receipt.setIconTextGap(7);
        title.add(receipt);
        title.add(cartCount);
        row.add(title, BorderLayout.CENTER);
        PosUI.StyledButton clear = PosUI.button("Xóa giỏ", PosUI.RED,
                new Color(255, 240, 244), new Color(255, 221, 225));
        clear.addActionListener(e -> {
            if (sales.layGioHang().isEmpty()) return;
            int answer = JOptionPane.showConfirmDialog(this, "Xóa toàn bộ sản phẩm trong giỏ?", "TiTiPet POS", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                sales.xoaToanBoGio(); refreshCart();
            }
        });
        row.add(clear, BorderLayout.EAST);
        return row;
    }

    private JComponent createCustomerBox() {
        JPanel all = PosUI.vertical(0);
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(PosUI.text("Khách hàng / Thành viên", 12, true, NAVY), BorderLayout.WEST);
        PosUI.StyledButton add = PosUI.button("+ Thêm khách mới", BLUE, new Color(238, 245, 255), new Color(195, 217, 251));
        add.addActionListener(e -> message("Tính năng tạo khách hàng thuộc màn Khách hàng & VIP.\nPOS chỉ chọn khách đã lưu để tránh ghi CSDL sai nghiệp vụ."));
        row.add(add, BorderLayout.EAST);
        all.add(row);
        all.add(PosUI.space(11));
        PosUI.RoundPanel picker = PosUI.roundPanel(WHITE, 10);
        picker.setLayout(new BorderLayout());
        picker.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        picker.add(customerText, BorderLayout.CENTER);
        picker.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        picker.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { selectCustomer(); }
        });
        picker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        picker.setPreferredSize(new Dimension(100, 44));
        all.add(picker);
        all.add(PosUI.space(9));
        JPanel info = new JPanel(new BorderLayout());
        info.setOpaque(false);
        tier.setOpaque(true); tier.setBackground(PosUI.SOFT_BLUE);
        info.add(tier, BorderLayout.WEST);
        info.add(points, BorderLayout.EAST);
        all.add(info);
        return all;
    }

    private JComponent createCartArea() {
        cartList.setOpaque(false);
        cartList.setLayout(new BoxLayout(cartList, BoxLayout.Y_AXIS));
        cartBox.setOpaque(false);
        cartBox.setBorder(null);
        cartBox.add(cartList, BorderLayout.CENTER);
        cartBox.setPreferredSize(new Dimension(100, 170));
        cartBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        refreshCart();
        return cartBox;
    }

    private JComponent createTransactionControls() {
        JPanel controls = new JPanel(new GridLayout(2, 2, 15, 17));
        controls.setOpaque(false);
        controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 178));
        controls.add(fieldGroup("Khuyến mãi (tối đa 1)", PosUI.combo(new String[]{"Không áp dụng"})));
        JPanel pointsGroup = PosUI.vertical(0);
        pointsGroup.add(PosUI.text("Điểm sử dụng", 11, true, NAVY));
        pointsGroup.add(PosUI.space(6));
        pointInput.setText("0");
        pointInput.setFont(PosUI.REG);
        pointInput.setPreferredSize(new Dimension(100, 44));
        pointInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        pointsGroup.add(pointInput);
        pointsGroup.add(PosUI.space(4));
        pointsGroup.add(maxPoints);
        controls.add(pointsGroup);
        orderSource = PosUI.combo(new String[]{"Tại quầy", "Shopee", "TikTok"});
        paymentMethod = PosUI.combo(new String[]{"Tiền mặt", "Chuyển khoản", "Qua sàn"});
        controls.add(fieldGroup("Nguồn đơn hàng", orderSource));
        controls.add(fieldGroup("Phương thức thanh toán", paymentMethod));
        return controls;
    }

    private JComponent fieldGroup(String label, JComponent field) {
        JPanel group = PosUI.vertical(0);
        group.add(PosUI.text(label, 11, true, NAVY));
        group.add(PosUI.space(6));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(field);
        return group;
    }

    private JComponent createTotals() {
        JPanel totals = PosUI.vertical(0);
        totals.add(amountLine("Tạm tính", subtotal));
        totals.add(PosUI.space(5));
        totals.add(amountLine("Ưu đãi thành viên", tierDiscount));
        totals.add(PosUI.space(5));
        totals.add(amountLine("Khuyến mãi", promoDiscount));
        totals.add(PosUI.space(5));
        totals.add(amountLine("Đổi điểm (0%)", pointDiscount));
        totals.add(PosUI.space(9));
        totals.add(PosUI.line());
        totals.add(PosUI.space(7));
        totals.add(amountLineStrong("Cần thanh toán", amountDue));
        totals.add(PosUI.space(4));
        totals.add(PosUI.text("Tạm tính · Chưa áp dụng KM/VAT phát sinh", 10, false, MUTED));
        return totals;
    }

    private JComponent amountLine(String text, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        p.add(PosUI.text(text, 12, false, MUTED), BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        return p;
    }
    private JComponent amountLineStrong(String text, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        p.add(PosUI.text(text, 17, true, NAVY), BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        return p;
    }

    private void wireEvents() {
        searchButton.addActionListener(e -> loadProducts());
        search.addActionListener(e -> loadProducts());
        pointInput.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotals(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotals(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotals(); }
        });
        checkout.addActionListener(e -> showPaymentCalculation());
    }

    private void loadProducts() {
        String query = search.getText().strip();
        searchButton.setEnabled(false);
        status.setText("Đang tìm sản phẩm...");
        new SwingWorker<List<SanPhamBanHang>, Void>() {
            @Override protected List<SanPhamBanHang> doInBackground() {
                return sales.timSanPham(query);
            }
            @Override protected void done() {
                searchButton.setEnabled(true);
                try {
                    products = get();
                    refreshProductCards();
                    status.setText("Đã tải " + products.size() + " sản phẩm từ danh mục");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    status.setText("Đã dừng tải sản phẩm.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    status.setText("Lỗi tải sản phẩm: " + (cause == null ? "Không rõ" : cause.getMessage()));
                    // Không chặn màn hình bằng modal khi khởi tạo ứng dụng.
                    if (cause != null) cause.printStackTrace();
                }
            }
        }.execute();
    }

    private void refreshProductCards() {
        if (productGrid == null) return;
        productGrid.removeAll();
        int cols = Math.max(1, currentColumns);
        productGrid.setLayout(new GridLayout(0, cols, 13, 13));
        List<SanPhamBanHang> visible = products.stream().filter(this::matchesCategory).collect(Collectors.toList());
        for (SanPhamBanHang product : visible) productGrid.add(createProductCard(product));
        if (visible.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);
            empty.add(PosUI.text("Chưa có sản phẩm phù hợp trong danh mục", 13, false, MUTED));
            productGrid.add(empty);
        }
        productGrid.revalidate(); productGrid.repaint();
    }

    /** DB có THUC_AN / PHU_KIEN; nhóm nhỏ Đồ chơi/Vệ sinh được nhận diện bằng tên. */
    private boolean matchesCategory(SanPhamBanHang p) {
        String type = p.loaiSanPham() == null ? "" : p.loaiSanPham().toUpperCase(Locale.ROOT);
        String name = p.tenSanPham() == null ? "" : p.tenSanPham().toLowerCase(Locale.forLanguageTag("vi-VN"));
        boolean toy = name.contains("đồ chơi") || name.contains("bóng") || name.contains("toy");
        boolean clean = name.contains("vệ sinh") || name.contains("tã") || name.contains("khử mùi") || name.contains("tắm");
        return switch (selectedCategory) {
            case "Thức ăn cho mèo" -> "THUC_AN".equals(type) && (name.contains("mèo") || name.contains("cat"));
            case "Phụ kiện" -> "PHU_KIEN".equals(type) && !toy && !clean;
            case "Đồ chơi" -> toy;
            case "Vệ sinh" -> clean;
            default -> true;
        };
    }

    private JComponent createProductCard(SanPhamBanHang p) {
        PosUI.RoundPanel card = PosUI.roundPanel(WHITE, 14);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 12));
        card.setPreferredSize(new Dimension(172, 166));
        JPanel body = PosUI.vertical(0);
        String emoji = chooseIcon(p);
        JLabel icon = PosUI.text("", 23, false, NAVY);
        ImageIcon img = imageForSample(p);
        if (img != null) icon.setIcon(img);
        else icon.setIcon(PosIcons.of(chooseIcon(p), 33, BLUE));
        icon.setOpaque(true);
        icon.setBackground(img == null ? PosUI.SOFT_BLUE : Color.WHITE);
        if (img == null) icon.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
        icon.setMaximumSize(new Dimension(52, 52));
        body.add(icon);
        body.add(PosUI.space(9));
        JTextArea name = new JTextArea(p.tenSanPham());
        name.setLineWrap(true); name.setWrapStyleWord(true);
        name.setEditable(false); name.setFocusable(false);
        name.setOpaque(false); name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        name.setForeground(NAVY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        name.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        name.setPreferredSize(new Dimension(140, 36));
        body.add(name);
        String hsd = p.hanSuDung() == null ? "Không áp dụng" : p.hanSuDung().format(DATE);
        JLabel info = PosUI.text("Tồn: " + p.soLuongTonKho() + " · HSD: " + hsd, 10, false, MUTED);
        body.add(info);
        card.add(body, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(PosUI.text(money(p.giaBan()), 16, true, BLUE), BorderLayout.WEST);
        PosUI.StyledButton add = PosUI.button("+", BLUE, WHITE, null);
        add.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        add.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        boolean valid = p.soLuongTonKho() > 0 &&
                (p.hanSuDung() == null || !p.hanSuDung().isBefore(LocalDate.now()));
        add.setEnabled(valid);
        add.addActionListener(e -> addProduct(p));
        bottom.add(add, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private ImageIcon imageForSample(SanPhamBanHang p) {
        String n = p.tenSanPham().toLowerCase(Locale.forLanguageTag("vi-VN"));
        String file = null;
        if (n.contains("snappy tom")) file = "product_1.png";
        else if (n.contains("muina") || n.contains("miuna")) file = "product_2.png";
        else if (n.contains("dây dắt")) file = "product_3.png";
        else if (n.contains("vòng cổ")) file = "product_4.png";
        else if (n.contains("đồ chơi bóng")) file = "product_5.png";
        else if (n.contains("tã bỉm")) file = "product_6.png";
        else if (n.contains("áo thú cưng")) file = "product_7.png";
        return file == null ? null : PosUI.sampleIcon(file, 50, 50);
    }

    private String chooseIcon(SanPhamBanHang p) {
        String name = p.tenSanPham().toLowerCase(Locale.forLanguageTag("vi-VN"));
        if (name.contains("áo")) return "shirt";
        if (name.contains("đồ chơi") || name.contains("bóng")) return "paw";
        if (name.contains("thức ăn") || name.contains("hạt") || name.contains("pate")) return "food";
        if (name.contains("spa") || name.contains("tắm")) return "spa";
        return "paw";
    }

    private void addProduct(SanPhamBanHang p) {
        try {
            if (p.hanSuDung() != null && p.hanSuDung().isBefore(LocalDate.now()))
                throw new IllegalArgumentException("Sản phẩm đã quá hạn sử dụng.");
            sales.themVaoGio(p, 1);
            refreshCart();
            status.setText("Đã thêm: " + p.tenSanPham());
        } catch (IllegalArgumentException e) { message(e.getMessage()); }
    }

    private void refreshCart() {
        cartList.removeAll();
        List<BanHangService.DongGioHang> cart = sales.layGioHang();
        int itemCount = cart.stream().mapToInt(BanHangService.DongGioHang::soLuong).sum();
        cartCount.setText(itemCount + " sản phẩm / dịch vụ");
        if (cart.isEmpty()) {
            PosUI.RoundPanel empty = PosUI.roundPanel(PosUI.PALE, 12);
            empty.setLayout(new GridBagLayout());
            JPanel texts = PosUI.vertical(0);
            JLabel paw = PosUI.text("", 29, false, NAVY);
            paw.setIcon(PosIcons.of("paw", 32, NAVY));
            paw.setAlignmentX(Component.CENTER_ALIGNMENT);
            texts.add(paw);
            texts.add(PosUI.space(9));
            texts.add(PosUI.text("Giỏ hàng đang trống", 14, true, MUTED));
            texts.add(PosUI.space(10));
            texts.add(PosUI.text("Nhấn dấu + ở sản phẩm để thêm vào hóa đơn", 12, false, MUTED));
            empty.add(texts);
            empty.setPreferredSize(new Dimension(100, 181));
            cartList.add(empty);
        } else {
            for (BanHangService.DongGioHang row : cart) {
                cartList.add(createCartRow(row));
                cartList.add(PosUI.space(7));
            }
        }
        cartList.revalidate(); cartList.repaint();
        updateTotals();
    }

    private JComponent createCartRow(BanHangService.DongGioHang row) {
        SanPhamBanHang p = row.sanPham();
        PosUI.RoundPanel panel = PosUI.roundPanel(PosUI.PALE, 11);
        panel.setLayout(new BorderLayout(4, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 9));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        JPanel details = PosUI.vertical(0);
        JLabel name = PosUI.text(p.tenSanPham(), 12, true, NAVY);
        details.add(name);
        details.add(PosUI.space(4));
        details.add(PosUI.text(money(p.giaBan()) + " × " + row.soLuong() + " = " + money(row.thanhTien()), 11, false, MUTED));
        panel.add(details, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        actions.setOpaque(false);
        PosUI.StyledButton minus = PosUI.button("−", BLUE, WHITE, BORDER);
        PosUI.StyledButton plus = PosUI.button("+", BLUE, WHITE, BORDER);
        PosUI.StyledButton del = PosUI.button("×", PosUI.RED, WHITE, BORDER);
        JLabel number = PosUI.text(String.valueOf(row.soLuong()), 13, true, NAVY);
        minus.addActionListener(e -> {
            if (row.soLuong() <= 1) sales.xoaKhoiGio(p.maSanPham());
            else sales.capNhatSoLuong(p.maSanPham(), row.soLuong() - 1);
            refreshCart();
        });
        plus.addActionListener(e -> {
            try { sales.themVaoGio(p, 1); refreshCart(); }
            catch (IllegalArgumentException ex) { message(ex.getMessage()); }
        });
        del.addActionListener(e -> { sales.xoaKhoiGio(p.maSanPham()); refreshCart(); });
        actions.add(minus); actions.add(number); actions.add(plus); actions.add(del);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private void selectCustomer() {
        JTextField filter = PosUI.field("Nhập tên hoặc số điện thoại, để trống = tất cả");
        int decision = JOptionPane.showConfirmDialog(this, filter, "Tìm khách hàng", JOptionPane.OK_CANCEL_OPTION);
        if (decision != JOptionPane.OK_OPTION) return;
        String keyword = filter.getText().strip();
        SwingWorker<List<KhachHangBanHang>, Void> worker = new SwingWorker<>() {
            @Override protected List<KhachHangBanHang> doInBackground() {
                return customerService.tim(keyword);
            }
            @Override protected void done() {
                try {
                    List<KhachHangBanHang> list = get();
                    if (list.isEmpty()) { message("Không tìm thấy khách hàng phù hợp."); return; }
                    JComboBox<KhachHangBanHang> combo = new JComboBox<>(list.toArray(new KhachHangBanHang[0]));
                    JPanel selection = new JPanel(new BorderLayout(0, 9));
                    selection.add(new JLabel("Chọn khách hàng:"), BorderLayout.NORTH);
                    selection.add(combo, BorderLayout.CENTER);
                    int choice = JOptionPane.showConfirmDialog(BanHangPanel.this, selection,
                            "Khách hàng / Thành viên", JOptionPane.OK_CANCEL_OPTION);
                    if (choice == JOptionPane.OK_OPTION) setCustomer((KhachHangBanHang) combo.getSelectedItem());
                } catch (Exception ex) {
                    message("Không tải được khách hàng: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void setCustomer(KhachHangBanHang customer) {
        this.customer = customer;
        customerText.setText(customer == null ? "Khách lẻ — chưa chọn khách hàng" : customer.hoTen() + " · " + customer.soDienThoai());
        tier.setText(billing.tenHang(customer));
        int available = customer == null ? 0 : Math.max(0, customer.diemTichLuy());
        points.setText(available + " điểm (= " + money(BigDecimal.valueOf(available).multiply(BigDecimal.valueOf(1000))) + ")");
        maxPoints.setText("Tối đa " + available + " điểm");
        pointInput.setText("0");
        updateTotals();
    }

    private void updateTotals() {
        BigDecimal sum = sales.tinhTongTienHang();
        int rawPoints;
        try { rawPoints = Math.max(0, Integer.parseInt(pointInput.getText().strip())); }
        catch (NumberFormatException ex) { rawPoints = 0; }
        BanHangTinhTienService.KetQua bill = billing.uocTinh(sum, customer, rawPoints);
        subtotal.setText(money(bill.tamTinh()));
        tierDiscount.setText("− " + money(bill.giamHang()));
        promoDiscount.setText("− 0 ₫");
        pointDiscount.setText("− " + money(bill.giamDiem()));
        amountDue.setText(money(bill.phaiThu()));
        int canUse = billing.uocTinh(sum, customer, Integer.MAX_VALUE).diemSuDung();
        maxPoints.setText("Tối đa " + canUse + " điểm");
        checkout.setEnabled(!sales.layGioHang().isEmpty());
    }

    /** Calculator preview: no database writes, payment remains the responsibility of HoaDonService. */
    private void showPaymentCalculation() {
        if (sales.layGioHang().isEmpty()) {
            message("Giỏ hàng đang trống. Hãy thêm sản phẩm trước khi tính tiền.");
            return;
        }
        int pointsRequested;
        try { pointsRequested = Integer.parseInt(pointInput.getText().trim()); }
        catch (NumberFormatException ex) { message("Điểm sử dụng phải là số nguyên không âm."); return; }
        if (pointsRequested < 0) { message("Điểm sử dụng không được âm."); return; }
        BanHangTinhTienService.KetQua result = billing.uocTinh(
                sales.tinhTongTienHang(), customer, pointsRequested);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        content.add(PosUI.text("THÔNG TIN TÍNH TIỀN", 12, true, BLUE));
        content.add(PosUI.space(12));
        content.add(amountLine("Tạm tính", PosUI.text(money(result.tamTinh()), 12, true, NAVY)));
        content.add(PosUI.space(8));
        content.add(amountLine("Giảm hạng thành viên", PosUI.text("− " + money(result.giamHang()), 12, true, NAVY)));
        content.add(PosUI.space(8));
        content.add(amountLine("Giảm điểm (" + result.diemSuDung() + " điểm)",
                PosUI.text("− " + money(result.giamDiem()), 12, true, NAVY)));
        content.add(PosUI.space(10));
        content.add(amountLineStrong("Cần thanh toán", PosUI.text(money(result.phaiThu()), 18, true, BLUE)));
        content.add(PosUI.space(12));
        String method = (String) paymentMethod.getSelectedItem();
        content.add(PosUI.text("Hình thức: " + method, 12, true, NAVY));
        content.add(PosUI.space(8));
        if ("Tiền mặt".equals(method)) {
            content.add(PosUI.text("Tiền khách đưa (VNĐ):", 12, false, NAVY));
            content.add(PosUI.space(5));
            JTextField received = PosUI.field("Ví dụ: 100000 hoặc 100.000");
            received.setText(result.phaiThu().toBigInteger().toString());
            received.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            content.add(received);
            content.add(PosUI.space(9));
            JLabel change = PosUI.text("Tiền thối lại: 0 ₫", 15, true, BLUE);
            content.add(change);
            Runnable update = () -> {
                try {
                    BigDecimal rest = billing.tinhTienThua(result.phaiThu(), received.getText());
                    if (rest.signum() < 0) {
                        change.setForeground(PosUI.RED);
                        change.setText("Còn thiếu: " + money(rest.negate()));
                    } else {
                        change.setForeground(BLUE);
                        change.setText("Tiền thối lại: " + money(rest));
                    }
                } catch (IllegalArgumentException exception) {
                    change.setForeground(PosUI.RED);
                    change.setText("Vui lòng nhập số tiền hợp lệ.");
                }
            };
            received.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update.run(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update.run(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update.run(); }
            });
            update.run();
        }
        content.add(PosUI.space(14));
        JLabel warning = PosUI.text("CHƯA GHI HÓA ĐƠN VÀO SQL SERVER", 11, true, PosUI.RED);
        content.add(warning);
        content.add(PosUI.space(5));
        JTextArea note = new JTextArea("Đây là bảng tính tạm. Chức năng xác nhận giao dịch, trừ kho, cập nhật điểm và VAT cần được tích hợp với ca làm việc và tài khoản nhân viên trong module Hóa đơn.");
        note.setEditable(false);
        note.setFocusable(false);
        note.setOpaque(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setFont(PosUI.SMALL);
        note.setForeground(MUTED);
        note.setPreferredSize(new Dimension(420, 68));
        content.add(note);
        JOptionPane.showMessageDialog(this, content, "TiTi Pet — Bảng tính thanh toán",
                JOptionPane.PLAIN_MESSAGE);
        status.setText("Đã tính tiền cho " + sales.layGioHang().size() + " mặt hàng · chưa lưu giao dịch");
    }

    private String money(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        return MONEY.format(amount) + " ₫";
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text, "TiTi Pet — POS", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Chạy độc lập với khung sidebar và topbar giống bản mẫu. */
    public static void main(String[] args) {
        com.titipet.gui.BanHangPOSDemoFrame.main(args);
    }
}
