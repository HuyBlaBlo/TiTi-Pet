package com.titipet.gui.panel;

import com.titipet.bus.KhachHangService;
import com.titipet.bus.ThuCungService;
import com.titipet.entity.NhanVien;
import com.titipet.entity.ThuCung;
import com.titipet.entity.enums.LoaiThuCungApDung;
import com.titipet.entity.enums.VaiTro;
import com.titipet.gui.dialog.CapNhatThuCungDialog;
import com.titipet.gui.dialog.ThemThuCungDialog;
import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ThuCungPanel extends JPanel{
	
	// --- CÁC DỊCH VỤ NGHIỆP VỤ ---
	private final NhanVien nhanVienDangNhap;
    private final ThuCungService thuCungService;
    private final KhachHangService khachHangService;
    
 // --- BIẾN KIỂM TRA QUYỀN HẠN ---
    private boolean coQuyenChinhSua = false;

    // --- CÁC THÀNH PHẦN GIAO DIỆN CHÍNH ---
    private JTextField txtTimKiem;                 // Ô tìm kiếm nhanh
    private JComboBox<String> cbLocLoai;           // Bộ lọc: "Tất cả", "Chó", "Mèo"
    private JPanel gridContainer;                  // Panel chứa danh sách các thẻ Card
    private JScrollPane scrollPane;                // Cuộn danh sách thẻ

    // --- BẢNG MÀU GIAO DIỆN FLAT DESIGN ---
    private static final Color COLOR_BG = new Color(248, 250, 252);              // Nền xám nhạt toàn trang (#F8FAFC)
    private static final Color COLOR_PRIMARY = new Color(24, 119, 242);          // Xanh dương chủ đạo
    private static final Color COLOR_PRIMARY_HOVER = new Color(13, 100, 210);
    private static final Color COLOR_CARD_BG = Color.WHITE;                      // Nền trắng cho Card
    private static final Color COLOR_CARD_BORDER = new Color(226, 232, 240);      // Viền thẻ
    private static final Color COLOR_TEXT_MAIN = new Color(30, 41, 59);          // Chữ đậm chính (#1E293B)
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);      // Chữ phụ, ghi chú (#64748B)
    private static final Color COLOR_TAG_CHO = new Color(239, 246, 255);         // Tag màu cho Chó
    private static final Color COLOR_TAG_CHO_TEXT = new Color(29, 78, 216);
    private static final Color COLOR_TAG_MEO = new Color(254, 242, 242);         // Tag màu cho Mèo
    private static final Color COLOR_TAG_MEO_TEXT = new Color(185, 28, 28);

    /**
     * Khởi tạo giao diện Quản lý thú cưng
     */
    public ThuCungPanel(NhanVien nv) {
    	this.nhanVienDangNhap = nv;
        this.thuCungService = new ThuCungService();
        this.khachHangService = new KhachHangService();

        // 1. Kiểm tra quyền hạn của nhân viên
        kiemTraPhanQuyen();

        // 2. Xây dựng giao diện
        initComponents();

        // 3. Tải danh sách thẻ thú cưng
        taiDuLieuTheThucung();
    }
    
    /**
     * Xử lý logic phân quyền người dùng:
     * - QUAN_LY, THU_NGAN: Có quyền Thêm mới và Chỉnh sửa hồ sơ.
     * - THO_SPA: Không được phép thêm mới hay chỉnh sửa (chỉ xem thông tin phục vụ dịch vụ).
     */
    private void kiemTraPhanQuyen() {
        if (nhanVienDangNhap == null || nhanVienDangNhap.getVaiTro() == null) {
            // Mặc định cho phép thao tác nếu chạy test hoặc không có phiên đăng nhập
            coQuyenChinhSua = true;
            return;
        }

        VaiTro vaiTro = nhanVienDangNhap.getVaiTro();
        // Quản lý và Thu ngân có quyền Thêm/Sửa; Thợ Spa chỉ có quyền xem
        coQuyenChinhSua = (vaiTro == VaiTro.QUAN_LY || vaiTro == VaiTro.THU_NGAN);
    }

    /**
     * Khởi tạo bố cục và các thành phần giao diện
     */
    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // 1. THANH CÔNG CỤ TRÊN CÙNG (HEADER: TÌM KIẾM, BỘ LỌC, NÚT THÊM MỚI)
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setOpaque(false);

        // Khối bên trái: Thanh tìm kiếm + Bộ lọc theo loài
        JPanel searchFilterGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchFilterGroup.setOpaque(false);

        // Ô tìm kiếm nhanh với placeholder và viền bo
        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(280, 40));
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTimKiem.putClientProperty("JTextField.placeholderText", "Tìm theo tên bé, tên chủ, SĐT...");
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1),
                new EmptyBorder(0, 12, 0, 12)
        ));

        // Bắt sự kiện gõ phím để Live Search (tìm tức thì không cần nhấn Enter)
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { taiDuLieuTheThucung(); }
            @Override
            public void removeUpdate(DocumentEvent e) { taiDuLieuTheThucung(); }
            @Override
            public void changedUpdate(DocumentEvent e) { taiDuLieuTheThucung(); }
        });

        // ComboBox lọc nhanh theo loài
        cbLocLoai = new JComboBox<>(new String[]{"Tất cả loài", "Chó", "Mèo"});
        cbLocLoai.setPreferredSize(new Dimension(140, 40));
        cbLocLoai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbLocLoai.setBackground(Color.WHITE);
        cbLocLoai.addActionListener(e -> taiDuLieuTheThucung());

        searchFilterGroup.add(txtTimKiem);
        searchFilterGroup.add(cbLocLoai);

        // Khối bên phải: Nút "+ Thêm thú cưng"
        JButton btnThem = new JButton("+ Thêm thú cưng");
        btnThem.setPreferredSize(new Dimension(160, 40));
        btnThem.setBackground(COLOR_PRIMARY);
        btnThem.setForeground(Color.WHITE);
        btnThem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnThem.setFocusPainted(false);
        btnThem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThem.setBorder(BorderFactory.createEmptyBorder());
        btnThem.addActionListener(e -> moDialogThem());

        topBar.add(searchFilterGroup, BorderLayout.WEST);
        topBar.add(btnThem, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // 2. KHU VỰC LƯỚI THẺ (CARD GRID PANEL)
        // Sử dụng GridLayout linh hoạt: tự động chia nhiều cột dựa trên kích thước panel
        gridContainer = new JPanel();
        gridContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 18));
        gridContainer.setBackground(COLOR_BG);

        scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Cuộn mượt

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Nạp dữ liệu từ tầng BUS và hiển thị lên lưới thẻ Card Grid
     */
    public void taiDuLieuTheThucung() {
        gridContainer.removeAll();

        String tuKhoa = txtTimKiem.getText().trim();
        String loaiLoc = (String) cbLocLoai.getSelectedItem();

        try {
            // Lấy danh sách thú cưng từ CSDL theo từ khóa tìm kiếm
            List<ThuCung> ds = thuCungService.layDanhSachCard(tuKhoa);

            int dem = 0;
            for (ThuCung tc : ds) {
                // Áp dụng bộ lọc loài
                if ("Chó".equals(loaiLoc) && tc.getLoaiThuCung() != LoaiThuCungApDung.CHO) continue;
                if ("Mèo".equals(loaiLoc) && tc.getLoaiThuCung() != LoaiThuCungApDung.MEO) continue;

                // Tạo và gắn Card vào container
                gridContainer.add(taoTheThuCung(tc));
                dem++;
            }

            // Nếu không có dữ liệu khớp, hiển thị thông báo rỗng
            if (dem == 0) {
                JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                emptyPanel.setOpaque(false);
                JLabel lblEmpty = new JLabel("Không tìm thấy thú cưng nào phù hợp.");
                lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                lblEmpty.setForeground(COLOR_TEXT_MUTED);
                emptyPanel.add(lblEmpty);
                gridContainer.add(emptyPanel);
            }

        } catch (Exception ex) {
            System.err.println("[LỖI TẢI THẺ THÚ CƯNG]: " + ex.getMessage());
        }

        // Cập nhật lại giao diện
        gridContainer.revalidate();
        gridContainer.repaint();
    }

    /**
     * Tạo một Card độc lập đại diện cho 1 thú cưng
     * 
     * @param tc Đối tượng ThuCung
     * @return JPanel định dạng RoundedPannel dạng thẻ đẹp mắt
     */
    private JPanel taoTheThuCung(ThuCung tc) {
        // Thẻ bo góc 14px, nền trắng
        RoundedPannel card = new RoundedPannel(COLOR_CARD_BG, 14);
        card.setPreferredSize(new Dimension(280, 200));
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- 1. PHẦN ĐẦU CARD: TÊN PET + TAG LOÀI ---
        JPanel topCard = new JPanel(new BorderLayout());
        topCard.setOpaque(false);

        JLabel lblTen = new JLabel(tc.getTenThuCung());
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTen.setForeground(COLOR_TEXT_MAIN);

        // Huy hiệu (Badge/Tag) biểu thị Loài
        boolean laCho = (tc.getLoaiThuCung() == LoaiThuCungApDung.CHO);
        JLabel lblTagLoai = new JLabel(laCho ? "Chó" : "Mèo");
        lblTagLoai.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTagLoai.setOpaque(true);
        lblTagLoai.setBackground(laCho ? COLOR_TAG_CHO : COLOR_TAG_MEO);
        lblTagLoai.setForeground(laCho ? COLOR_TAG_CHO_TEXT : COLOR_TAG_MEO_TEXT);
        lblTagLoai.setBorder(new EmptyBorder(3, 8, 3, 8));

        topCard.add(lblTen, BorderLayout.WEST);
        topCard.add(lblTagLoai, BorderLayout.EAST);
        card.add(topCard, BorderLayout.NORTH);

        // --- 2. PHẦN THÂN CARD: THÔNG TIN CHI TIẾT ---
        JPanel bodyCard = new JPanel();
        bodyCard.setLayout(new BoxLayout(bodyCard, BoxLayout.Y_AXIS));
        bodyCard.setOpaque(false);

        // Mã thú cưng + Cân nặng
        String canNangStr = tc.getCanNang() > 0 ? (tc.getCanNang() + " kg") : "--";
        bodyCard.add(taoDongThongTin("Mã / Cân nặng:", tc.getMaThuCung() + " • " + canNangStr));
        bodyCard.add(Box.createVerticalStrut(4));

        // Tên chủ nuôi
        String tenChu = (tc.getChuNuoi() != null && tc.getChuNuoi().getHoTen() != null) 
                        ? tc.getChuNuoi().getHoTen() : "Chưa có";
        bodyCard.add(taoDongThongTin("Chủ nuôi:", tenChu));
        bodyCard.add(Box.createVerticalStrut(4));

        // Số điện thoại chủ nuôi
        String sdt = (tc.getChuNuoi() != null && tc.getChuNuoi().getSoDienThoai() != null) 
                     ? tc.getChuNuoi().getSoDienThoai() : "--";
        bodyCard.add(taoDongThongTin("SĐT liên hệ:", sdt));
        bodyCard.add(Box.createVerticalStrut(4));

        // Ghi chú (nếu có)
        String ghiChu = (tc.getGhiChu() != null && !tc.getGhiChu().isBlank()) ? tc.getGhiChu() : "Không có";
        if (ghiChu.length() > 28) ghiChu = ghiChu.substring(0, 25) + "..."; // Cắt ngắn nếu quá dài
        bodyCard.add(taoDongThongTin("Ghi chú:", ghiChu));

        card.add(bodyCard, BorderLayout.CENTER);

        // --- 3. ĐÁY CARD: NÚT SỬA HỒ SƠ (PHÂN QUYỀN) ---
        if (coQuyenChinhSua) {
            JPanel bottomCard = new JPanel(new BorderLayout());
            bottomCard.setOpaque(false);

            JButton btnSua = new JButton("Chỉnh sửa hồ sơ");
            btnSua.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnSua.setForeground(COLOR_PRIMARY);
            btnSua.setBackground(new Color(241, 245, 249));
            btnSua.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            btnSua.setFocusPainted(false);
            btnSua.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSua.addActionListener(e -> moDialogCapNhat(tc));

            bottomCard.add(btnSua, BorderLayout.EAST);
            card.add(bottomCard, BorderLayout.SOUTH);

            // Click đúp vào Card để mở nhanh Dialog sửa
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        moDialogCapNhat(tc);
                    }
                }
            });
        }

        return card;
    }

    /**
     * Hàm trợ giúp: Tạo một dòng thông tin dạng "Nhãn: Giá trị"
     */
    private JPanel taoDongThongTin(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);

        JLabel lblTieuDe = new JLabel(label);
        lblTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTieuDe.setForeground(COLOR_TEXT_MUTED);

        JLabel lblGiaTri = new JLabel(value);
        lblGiaTri.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaTri.setForeground(COLOR_TEXT_MAIN);

        row.add(lblTieuDe, BorderLayout.WEST);
        row.add(lblGiaTri, BorderLayout.CENTER);
        return row;
    }

    /**
     * Mở Dialog thêm thú cưng mới (Có kiểm tra bảo vệ quyền)
     */
    private void moDialogThem() {
        if (!coQuyenChinhSua) {
            JOptionPane.showMessageDialog(this, 
                "Thợ Spa không có quyền tạo mới hồ sơ thú cưng!", 
                "Từ chối truy cập", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        ThemThuCungDialog dlg = new ThemThuCungDialog(parentFrame, thuCungService, khachHangService);
        dlg.setVisible(true);
        taiDuLieuTheThucung();
    }

    /**
     * Mở Dialog cập nhật thú cưng
     */
    private void moDialogCapNhat(ThuCung tc) {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        CapNhatThuCungDialog dlg = new CapNhatThuCungDialog(parentFrame, thuCungService, khachHangService, tc);
        dlg.setVisible(true);
        taiDuLieuTheThucung(); // Tải lại danh sách sau khi đóng dialog
    }
}
