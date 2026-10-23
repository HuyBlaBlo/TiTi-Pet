package com.titipet.gui.panel;

import com.titipet.bus.BanHangService;
import com.titipet.entity.SanPhamBanHang;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Giao diện POS bán hàng.
 * GUI chỉ nhận sự kiện và hiển thị dữ liệu.
 * Tất cả nghiệp vụ được gọi qua BanHangService.
 */
public class BanHangPanel extends JPanel {

    private final BanHangService service;

    private final JTextField txtTuKhoa =
            new JTextField(25);

    private final JSpinner spnSoLuong =
            new JSpinner(
                    new SpinnerNumberModel(
                            1, 1, 1000000, 1
                    )
            );

    private final JLabel lblTongTien =
            new JLabel("Tạm tính: 0 đ");

    private final JLabel lblTrangThai =
            new JLabel("Sẵn sàng");

    private final JButton btnTim =
            new JButton("Tìm kiếm");

    private final JButton btnThem =
            new JButton("Thêm vào giỏ");

    private final DefaultTableModel modelSanPham =
            taoModel(
                    "Mã",
                    "Sản phẩm",
                    "Mã vạch",
                    "Giá bán",
                    "Tồn kho"
            );

    private final DefaultTableModel modelGioHang =
            taoModel(
                    "Mã",
                    "Sản phẩm",
                    "Đơn giá",
                    "Số lượng",
                    "Thành tiền"
            );

    private final JTable tblSanPham =
            new JTable(modelSanPham);

    private final JTable tblGioHang =
            new JTable(modelGioHang);

    private List<SanPhamBanHang> danhSachSanPham =
            new ArrayList<>();

    private final NumberFormat tienVietNam =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("vi-VN")
            );

    public BanHangPanel() {

        this(new BanHangService());
    }

    public BanHangPanel(BanHangService service) {

        this.service = service;

        setLayout(new BorderLayout(10, 10));

        setBorder(
                BorderFactory.createEmptyBorder(
                        12, 12, 12, 12
                )
        );

        setPreferredSize(new Dimension(1150, 680));

        taoGiaoDien();

        ganSuKien();

        SwingUtilities.invokeLater(this::timKiem);
    }

    /**
     * Tạo bảng chỉ đọc.
     */
    private static DefaultTableModel taoModel(
            String... columns
    ) {

        return new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };
    }

    /**
     * Khởi tạo giao diện bằng Layout Manager.
     */
    private void taoGiaoDien() {

        // Khu vực tìm kiếm
        JPanel thanhTimKiem =
                new JPanel(new BorderLayout(8, 0));

        thanhTimKiem.add(
                new JLabel("Tìm sản phẩm / mã vạch:"),
                BorderLayout.WEST
        );

        thanhTimKiem.add(
                txtTuKhoa,
                BorderLayout.CENTER
        );

        thanhTimKiem.add(
                btnTim,
                BorderLayout.EAST
        );

        add(thanhTimKiem, BorderLayout.NORTH);

        // Khu vực danh sách sản phẩm
        JPanel benTrai =
                new JPanel(new BorderLayout(8, 8));

        benTrai.setBorder(
                BorderFactory.createTitledBorder(
                        "Danh sách sản phẩm"
                )
        );

        benTrai.add(
                new JScrollPane(tblSanPham),
                BorderLayout.CENTER
        );

        JPanel thaoTacSanPham =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT, 8, 4
                        )
                );

        thaoTacSanPham.add(
                new JLabel("Số lượng:")
        );

        thaoTacSanPham.add(spnSoLuong);

        thaoTacSanPham.add(btnThem);

        benTrai.add(
                thaoTacSanPham,
                BorderLayout.SOUTH
        );

        // Khu vực giỏ hàng
        JPanel benPhai =
                new JPanel(new BorderLayout(8, 8));

        benPhai.setBorder(
                BorderFactory.createTitledBorder(
                        "Giỏ hàng"
                )
        );

        benPhai.add(
                new JScrollPane(tblGioHang),
                BorderLayout.CENTER
        );

        JPanel phanCuoiGio =
                new JPanel(new BorderLayout(4, 8));

        JPanel thaoTacGio =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT, 8, 4
                        )
                );

        JButton btnSua =
                new JButton("Sửa số lượng");

        JButton btnXoa =
                new JButton("Xóa dòng");

        JButton btnXoaTatCa =
                new JButton("Xóa giỏ");

        thaoTacGio.add(btnSua);
        thaoTacGio.add(btnXoa);
        thaoTacGio.add(btnXoaTatCa);

        phanCuoiGio.add(
                thaoTacGio,
                BorderLayout.NORTH
        );

        phanCuoiGio.add(
                lblTongTien,
                BorderLayout.SOUTH
        );

        benPhai.add(
                phanCuoiGio,
                BorderLayout.SOUTH
        );

        // Chia hai vùng có thể co giãn
        JSplitPane split =
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        benTrai,
                        benPhai
                );

        split.setResizeWeight(0.55);
        split.setContinuousLayout(true);

        add(split, BorderLayout.CENTER);

        add(lblTrangThai, BorderLayout.SOUTH);

        tblSanPham.setSelectionMode(
                javax.swing.ListSelectionModel
                        .SINGLE_SELECTION
        );

        tblGioHang.setSelectionMode(
                javax.swing.ListSelectionModel
                        .SINGLE_SELECTION
        );

        tblSanPham.setAutoCreateRowSorter(true);

        // Sự kiện sửa số lượng
        btnSua.addActionListener(
                e -> suaSoLuong()
        );

        // Sự kiện xóa dòng
        btnXoa.addActionListener(
                e -> xoaDong()
        );

        // Sự kiện xóa toàn bộ giỏ
        btnXoaTatCa.addActionListener(e -> {

            service.xoaToanBoGio();

            hienThiGioHang();
        });
    }

    /**
     * Gắn sự kiện cho các thành phần giao diện.
     */
    private void ganSuKien() {

        btnTim.addActionListener(
                e -> timKiem()
        );

        txtTuKhoa.addActionListener(
                e -> timKiem()
        );

        btnThem.addActionListener(
                e -> themVaoGio()
        );

        // Nhấp đúp sản phẩm để thêm vào giỏ
        tblSanPham.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e
                    ) {

                        if (e.getClickCount() == 2) {

                            themVaoGio();
                        }
                    }
                }
        );
    }

    /**
     * Tìm kiếm sản phẩm.
     * SwingWorker giúp giao diện không bị treo
     * trong lúc truy vấn SQL Server.
     */
    private void timKiem() {

        String tuKhoa = txtTuKhoa.getText();

        btnTim.setEnabled(false);

        lblTrangThai.setText(
                "Đang tải sản phẩm..."
        );

        new SwingWorker<
                List<SanPhamBanHang>,
                Void
                >() {

            @Override
            protected List<SanPhamBanHang>
            doInBackground() {

                return service.timSanPham(tuKhoa);
            }

            @Override
            protected void done() {

                btnTim.setEnabled(true);

                try {

                    danhSachSanPham = get();

                    modelSanPham.setRowCount(0);

                    for (SanPhamBanHang p
                            : danhSachSanPham) {

                        modelSanPham.addRow(
                                new Object[]{
                                        p.maSanPham(),
                                        p.tenSanPham(),
                                        p.maVach(),
                                        dinhDangTien(
                                                p.giaBan()
                                        ),
                                        p.soLuongTonKho()
                                }
                        );
                    }

                    lblTrangThai.setText(
                            danhSachSanPham.isEmpty()
                                    ? "Không tìm thấy sản phẩm"
                                    : "Tìm thấy "
                                    + danhSachSanPham.size()
                                    + " sản phẩm"
                    );

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    hienLoi(
                            "Thao tác tải sản phẩm bị gián đoạn."
                    );

                } catch (ExecutionException ex) {

                    Throwable cause = ex.getCause();

                    hienLoi(
                            cause == null
                                    ? "Không tải được sản phẩm"
                                    : cause.getMessage()
                    );
                }
            }

        }.execute();
    }

    /**
     * Thêm sản phẩm được chọn vào giỏ.
     */
    private void themVaoGio() {

        int viewRow =
                tblSanPham.getSelectedRow();

        if (viewRow < 0) {

            hienLoi(
                    "Vui lòng chọn một sản phẩm từ danh sách."
            );

            return;
        }

        int modelRow =
                tblSanPham.convertRowIndexToModel(
                        viewRow
                );

        try {

            service.themVaoGio(
                    danhSachSanPham.get(modelRow),
                    (Integer) spnSoLuong.getValue()
            );

            hienThiGioHang();

        } catch (
                IllegalArgumentException
                | IllegalStateException ex
        ) {

            hienLoi(ex.getMessage());
        }
    }

    /**
     * Thay đổi số lượng sản phẩm trong giỏ.
     */
    private void suaSoLuong() {

        String ma =
                layMaSanPhamTrongGio();

        if (ma == null) {
            return;
        }

        String nhap =
                JOptionPane.showInputDialog(
                        this,
                        "Số lượng mới:",
                        "1"
                );

        if (nhap == null) {
            return;
        }

        try {

            service.capNhatSoLuong(
                    ma,
                    Integer.parseInt(
                            nhap.trim()
                    )
            );

            hienThiGioHang();

        } catch (NumberFormatException ex) {

            hienLoi(
                    "Vui lòng nhập số lượng nguyên hợp lệ."
            );

        } catch (IllegalArgumentException ex) {

            hienLoi(ex.getMessage());
        }
    }

    /**
     * Xóa sản phẩm được chọn khỏi giỏ hàng.
     */
    private void xoaDong() {

        String ma =
                layMaSanPhamTrongGio();

        if (ma != null) {

            service.xoaKhoiGio(ma);

            hienThiGioHang();
        }
    }

    /**
     * Lấy mã sản phẩm đang chọn trong giỏ.
     */
    private String layMaSanPhamTrongGio() {

        int row =
                tblGioHang.getSelectedRow();

        if (row < 0) {

            hienLoi(
                    "Vui lòng chọn sản phẩm trong giỏ hàng."
            );

            return null;
        }

        return (String)
                modelGioHang.getValueAt(
                        row, 0
                );
    }

    /**
     * Hiển thị lại giỏ và tổng tiền.
     */
    private void hienThiGioHang() {

        modelGioHang.setRowCount(0);

        for (BanHangService.DongGioHang dong
                : service.layGioHang()) {

            modelGioHang.addRow(
                    new Object[]{
                            dong.sanPham().maSanPham(),
                            dong.sanPham().tenSanPham(),
                            dinhDangTien(
                                    dong.sanPham().giaBan()
                            ),
                            dong.soLuong(),
                            dinhDangTien(
                                    dong.thanhTien()
                            )
                    }
            );
        }

        lblTongTien.setText(
                "Tạm tính tiền hàng: "
                + dinhDangTien(
                        service.tinhTongTienHang()
                )
        );
    }

    /**
     * Định dạng tiền Việt Nam.
     */
    private String dinhDangTien(BigDecimal tien) {

        return tienVietNam.format(tien);
    }

    /**
     * Hiển thị lỗi ở tầng GUI.
     */
    private void hienLoi(String noiDung) {

        lblTrangThai.setText(
                "Lỗi: " + noiDung
        );

        JOptionPane.showMessageDialog(
                this,
                noiDung,
                "TiTi Pet",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Chạy thử panel độc lập.
     * Không thay đổi Main.java của trưởng nhóm.
     */
    public static void main(String[] args) {

        try {

            UIManager.setLookAndFeel(
                    "com.formdev.flatlaf.FlatLightLaf"
            );

        } catch (Exception ex) {

            // Dùng Swing mặc định nếu chưa có FlatLaf.
        }

        SwingUtilities.invokeLater(() -> {

            JFrame frame =
                    new JFrame(
                            "TiTi Pet - Bán hàng POS (bản thử)"
                    );

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.setContentPane(
                    new BanHangPanel()
            );

            frame.pack();

            frame.setLocationRelativeTo(null);

            frame.setVisible(true);
        });
    }
}