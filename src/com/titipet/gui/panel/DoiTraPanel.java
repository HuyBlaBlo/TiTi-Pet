
package com.titipet.gui.panel;

import com.titipet.bus.DoiTraService;
import com.titipet.dao.DoiTraDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class DoiTraPanel extends JPanel {

    private static final Color NEN = new Color(245, 248, 254);
    private static final Color XANH = new Color(30, 68, 121);
    private static final Color NHAT = new Color(129, 153, 189);
    private static final Color VIEN = new Color(223, 234, 249);
    private static final Color CAM = new Color(158, 104, 29);

    private final DoiTraService service = new DoiTraService();

    private final JTextField txtMaHD = new JTextField();

    private final JButton btnTim = nut("Tra cứu", true);
    private final JButton btnKiemTra =
            nut("Kiểm tra điều kiện", true);

    private final DefaultListModel<DoiTraDAO.DongHang> modelHang =
            new DefaultListModel<>();

    private final JList<DoiTraDAO.DongHang> dsHang =
            new JList<>(modelHang);

    private final DefaultListModel<DoiTraDAO.LichSu> modelLichSu =
            new DefaultListModel<>();

    private final JList<DoiTraDAO.LichSu> dsLichSu =
            new JList<>(modelLichSu);

    private final JCheckBox cbTinhTrang = new JCheckBox(
            "Đã kiểm tra sạch, chưa sử dụng, không mùi/lông"
    );

    private final JCheckBox cbNhomCam = new JCheckBox(
            "Không thuộc nhóm tã/bỉm, vệ sinh pet, thuốc, hỗ trợ chuồng"
    );

    private final JLabel lblThongTin = nhan(
            "Nhập mã hóa đơn để bắt đầu",
            15, XANH, Font.BOLD
    );

    private final JLabel lblKetQua = nhan(
            "Cần một hóa đơn POS đã thanh toán có sản phẩm hợp lệ.",
            12, NHAT, Font.PLAIN
    );

    private final JLabel lblTrangThai =
            nhan("", 12, CAM, Font.PLAIN);

    private final JLabel lblLichSu = nhan(
            "Chưa có dữ liệu ở mục này",
            12, NHAT, Font.PLAIN
    );

    private final JPanel khuKetQua =
            new JPanel(new BorderLayout(8, 10));

    private final DefaultComboBoxModel<DoiTraDAO.SanPhamThayThe>
            modelThayThe = new DefaultComboBoxModel<>();
    private final JComboBox<DoiTraDAO.SanPhamThayThe> cbSanPhamMoi =
            new JComboBox<>(modelThayThe);
    private final JButton btnTinhChenh = nut("Tính chênh lệch", true);
    private final JLabel lblBaoGia = nhan(
            "Chưa có báo giá.", 12, CAM, Font.BOLD);
    private final JPanel khungDoiHang = new JPanel();

    private final String maNV;
    private final JButton btnXacNhan = nut("Xác nhận đổi hàng", true);
    private final JComboBox<String> cbPhuongThuc = new JComboBox<>(
            new String[]{"TIEN_MAT", "CHUYEN_KHOAN"});
    private final JCheckBox cbDaThuTien = new JCheckBox(
            "Đã nhận đủ số tiền khách phải trả thêm");
    private java.math.BigDecimal giaDaBao = null;

    // Nhanh TRA HANG (tien hoan va xac nhan chi tien)
    private final JPanel khungTraHang = new JPanel();
    private final JButton btnTinhHoan = nut("Tính tiền hoàn", true);
    private final JLabel lblGiaHoan = nhan("Chưa tính tiền hoàn.",12,CAM,Font.BOLD);
    private final JComboBox<String> cbHoanBang = new JComboBox<>(
            new String[]{"TIEN_MAT","CHUYEN_KHOAN"});
    private final JCheckBox cbDaHoan = new JCheckBox("Đã thực tế hoàn tiền cho khách");
    private final JButton btnXacNhanTra = nut("Xác nhận trả hàng / hoàn tiền",true);
    private java.math.BigDecimal giaHoanDaBao = null;

    private DoiTraDAO.HoaDonGoc hoaDon;

    public DoiTraPanel() {
        this(null); // MainFrame can goi constructor nhan maNV khi tich hop.
    }

    public DoiTraPanel(String maNV) {
        this.maNV = maNV;
        setLayout(new BorderLayout());
        setBackground(NEN);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBorder(new EmptyBorder(22, 26, 25, 26));
        content.setBackground(NEN);

        content.add(tieuDe(), BorderLayout.NORTH);
        content.add(haiCot(), BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);

        btnTim.addActionListener(e -> traCuu());
        txtMaHD.addActionListener(e -> traCuu());
        btnKiemTra.addActionListener(e -> kiemTra());
        btnTinhChenh.addActionListener(e -> tinhGiaDoi());
        btnTinhHoan.addActionListener(e -> tinhGiaTra());
        btnXacNhanTra.addActionListener(e -> xacNhanTraHang());
        btnXacNhanTra.setEnabled(false);
        cbHoanBang.addActionListener(e -> huyBaoGia());
        cbDaHoan.setOpaque(false);
        cbTinhTrang.addActionListener(e -> huyBaoGia());
        cbNhomCam.addActionListener(e -> huyBaoGia());
        btnXacNhan.addActionListener(e -> xacNhanDoiHang());
        cbPhuongThuc.addActionListener(e -> huyBaoGia());
        cbDaThuTien.setOpaque(false);
        btnXacNhan.setEnabled(false);
        cbSanPhamMoi.addActionListener(e -> {
            huyBaoGia();
            lblBaoGia.setText("Chọn Tính chênh lệch để cập nhật.");
        });
        btnTinhChenh.setEnabled(false);


        btnKiemTra.setEnabled(false);

        dsHang.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnKiemTra.setEnabled(
                        dsHang.getSelectedValue() != null
                );
                lblTrangThai.setText("");
                huyBaoGia();
                khungDoiHang.setVisible(false);
                khungTraHang.setVisible(false);
                modelThayThe.removeAllElements();
                btnTinhChenh.setEnabled(false);
            }
        });

        khoiChonHangMoi();
        khoiTraHang();
        taoTrangThaiRong();
    }

    // HEADER
    private JPanel tieuDe() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel k = nhan(
                "TITI PET • WORKSPACE",
                10, new Color(37, 95, 183), Font.BOLD
        );

        JLabel h = nhan(
                "Đổi/trả hàng",
                25, XANH, Font.BOLD
        );

        JLabel d = nhan(
                "Tra cứu hóa đơn gốc và kiểm tra các điều kiện đổi trả",
                12, NHAT, Font.PLAIN
        );

        for (JLabel x : new JLabel[]{k, h, d}) {
            x.setAlignmentX(LEFT_ALIGNMENT);
        }

        p.add(k);
        p.add(Box.createVerticalStrut(5));
        p.add(h);
        p.add(Box.createVerticalStrut(5));
        p.add(d);

        return p;
    }

    // BO CUC HAI COT
    private JPanel haiCot() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();

        g.gridy = 0;
        g.gridx = 0;
        g.weightx = .61;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 0, 14);

        p.add(cotTrai(), g);

        g.gridx = 1;
        g.weightx = .39;
        g.insets = new Insets(0, 0, 0, 0);

        p.add(cotPhai(), g);

        return p;
    }

    // COT TRAI - XU LY DOI TRA
    private JPanel cotTrai() {
        JPanel box = card();

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(
                new BoxLayout(inner, BoxLayout.Y_AXIS)
        );

        
        JLabel title = nhan(
                "Xử lý yêu cầu đổi / trả",
                16, XANH, Font.BOLD
        );

        title.setIcon(new DoiTraSymbol(DoiTraSymbol.EXCHANGE, 20));
        title.setIconTextGap(8);


        inner.add(title);
        inner.add(nhan(
                "Điều kiện kiểm tra trước khi tạo phiếu đổi hàng",
                11, NHAT, Font.PLAIN
        ));

        inner.add(Box.createVerticalStrut(14));

        JTextArea note = new JTextArea(
                "Quy định: không đổi tã/bỉm, vệ sinh pet, "
                + "thức ăn, thuốc, hàng hỗ trợ chuồng; "
                + "tối đa 1 lần; liên hệ sau 12:00; "
                + "hàng mới bằng giá hoặc cao hơn. "
                + "Hóa đơn mẫu nêu mốc 24 giờ."
        );

        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setEditable(false);
        note.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        note.setForeground(CAM);
        note.setBackground(new Color(255, 247, 233));
        note.setBorder(new EmptyBorder(12, 12, 12, 12));
        note.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 94)
        );
        note.setAlignmentX(LEFT_ALIGNMENT);

        inner.add(note);
        inner.add(Box.createVerticalStrut(15));

        inner.add(nhan(
                "Mã hóa đơn cần tra cứu *",
                12, XANH, Font.BOLD
        ));

        inner.add(Box.createVerticalStrut(6));

        JPanel tim = new JPanel(new BorderLayout(9, 0));
        tim.setOpaque(false);
        tim.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );

        txtMaHD.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        txtMaHD.putClientProperty(
                "JTextField.placeholderText",
                "VD: HD..."
        );

        txtMaHD.putClientProperty(
                "FlatLaf.style",
                "arc: 10; margin: 8,10,8,10"
        );

        tim.add(txtMaHD, BorderLayout.CENTER);
        tim.add(btnTim, BorderLayout.EAST);
        tim.setAlignmentX(LEFT_ALIGNMENT);

        inner.add(tim);
        inner.add(Box.createVerticalStrut(12));

        khuKetQua.setOpaque(false);
        khuKetQua.setAlignmentX(LEFT_ALIGNMENT);

        inner.add(khuKetQua);
        inner.add(Box.createVerticalStrut(10));
        inner.add(lblTrangThai);

        box.add(inner, BorderLayout.NORTH);

        return box;
    }

    // COT PHAI - LICH SU VA QUY DINH
    private JPanel cotPhai() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);

        JPanel lichSu = card();

        JLabel title = nhan(
                "Lịch sử đổi trả",
                15, XANH, Font.BOLD
        );

        lichSu.add(title, BorderLayout.NORTH);

        dsLichSu.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        dsLichSu.setBackground(Color.WHITE);
        dsLichSu.setFixedCellHeight(34);

        JScrollPane s = new JScrollPane(dsLichSu);
        s.setPreferredSize(new Dimension(260, 150));
        s.setBorder(
                BorderFactory.createLineBorder(VIEN)
        );

        JPanel hisBody = new JPanel(
                new BorderLayout(0, 10)
        );
        hisBody.setOpaque(false);

        hisBody.add(s, BorderLayout.CENTER);
        hisBody.add(lblLichSu, BorderLayout.SOUTH);

        lichSu.add(hisBody, BorderLayout.CENTER);

        JPanel quyDinh = card();

        quyDinh.add(nhan(
                "Trường hợp không đủ điều kiện",
                14, XANH, Font.BOLD
        ), BorderLayout.NORTH);

        JTextArea info = new JTextArea(
                "Không hỗ trợ sản phẩm thuộc danh mục hạn chế, "
                + "quá mốc kiểm tra, đã đổi 1 lần, "
                + "liên hệ trước 12 giờ hoặc sản phẩm đã sử dụng/dính bẩn.\n\n"
                + "Hàng thay thế cần có giá trị bằng hoặc cao hơn hàng cũ."
        );

        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        info.setForeground(NHAT);
        info.setOpaque(false);

        quyDinh.add(info, BorderLayout.CENTER);

        p.add(lichSu, BorderLayout.NORTH);
        p.add(quyDinh, BorderLayout.CENTER);

        return p;
    }

    // TRANG THAI BAN DAU
    private void taoTrangThaiRong() {
        khuKetQua.removeAll();

        JPanel empty = new JPanel();
        empty.setBackground(new Color(248, 251, 255));
        empty.setLayout(
                new BoxLayout(empty, BoxLayout.Y_AXIS)
        );

        empty.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(VIEN),
                        new EmptyBorder(28, 12, 28, 12)
                )
        );

        
        JLabel icon = new JLabel(
                new DoiTraSymbol(DoiTraSymbol.PAW, 30)
        );

        icon.setHorizontalAlignment(SwingConstants.CENTER);


        for (JLabel l : new JLabel[]{
                icon, lblThongTin, lblKetQua
        }) {
            l.setAlignmentX(CENTER_ALIGNMENT);
            empty.add(l);
            empty.add(Box.createVerticalStrut(7));
        }

        khuKetQua.add(empty, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // TIM HOA DON TU SQL SERVER
    private void traCuu() {
        String maHD = txtMaHD.getText().trim();
        if (maHD.matches("0[0-9]{8,10}")) {
            traCuuTheoDienThoai(maHD);
            return;
        }

        if (maHD.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng nhập mã hóa đơn."
            );
            return;
        }

        btnTim.setEnabled(false);
        lblTrangThai.setText("Đang tra cứu SQL Server...");

        new SwingWorker<DoiTraService.DuLieu, Void>() {

            @Override
            protected DoiTraService.DuLieu doInBackground() {
                return service.traCuu(maHD);
            }

            @Override
            protected void done() {
                btnTim.setEnabled(true);

                try {
                    hienThi(get());

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException ex) {
                    lblTrangThai.setText(
                            "Lỗi: " + ex.getCause().getMessage()
                    );
                }
            }

        }.execute();
    }

    private void traCuuTheoDienThoai(String sdt) {
        btnTim.setEnabled(false);
        lblTrangThai.setText("Đang tìm hóa đơn theo số điện thoại...");
        new SwingWorker<java.util.List<DoiTraDAO.HoaDonTomTat>,Void>() {
            @Override protected java.util.List<DoiTraDAO.HoaDonTomTat> doInBackground() {
                return service.timTheoSoDienThoai(sdt);
            }
            @Override protected void done() {
                btnTim.setEnabled(true);
                try {
                    var hd = get();
                    if (hd.isEmpty()) { lblTrangThai.setText("Không tìm thấy hóa đơn đã thanh toán."); return; }
                    Object chon = JOptionPane.showInputDialog(DoiTraPanel.this,
                            "Chọn hóa đơn cần xử lý:","Tra cứu theo SĐT",
                            JOptionPane.QUESTION_MESSAGE,null,hd.toArray(),hd.get(0));
                    if (chon instanceof DoiTraDAO.HoaDonTomTat value) {
                        txtMaHD.setText(value.maHD());
                        traCuu();
                    }
                } catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
                catch(ExecutionException ex) {
                    lblTrangThai.setText("Tra cứu thất bại: " + ex.getCause().getMessage());
                }
            }
        }.execute();
    }

    // HIEN THI KET QUA TRA CUU
    private void hienThi(DoiTraService.DuLieu data) {
        hoaDon = data.hoaDon();

        modelHang.clear();
        modelLichSu.clear();

        cbNhomCam.setSelected(false);
        cbTinhTrang.setSelected(false);
        btnKiemTra.setEnabled(false);
        khungDoiHang.setVisible(false);
        khungTraHang.setVisible(false);
        modelThayThe.removeAllElements();
        lblBaoGia.setText("Chưa có báo giá.");
        huyBaoGia();

        if (hoaDon == null) {
            lblThongTin.setText("Không tìm thấy hóa đơn");
            lblKetQua.setText(
                    "Kiểm tra lại mã hóa đơn bạn đã nhập."
            );
            lblTrangThai.setText(
                    "Không tìm thấy: " + txtMaHD.getText().trim()
            );

            taoTrangThaiRong();
            return;
        }

        for (var h : hoaDon.sanPham()) {
            modelHang.addElement(h);
        }

        for (var d : data.lichSu()) {
            modelLichSu.addElement(d);
        }

        lblLichSu.setText(
                data.lichSu().isEmpty()
                        ? "Chưa có phiếu đổi trả của hóa đơn này"
                        : data.lichSu().size()
                                + " giao dịch đổi/trả"
        );

        khuKetQua.removeAll();

        JPanel found = new JPanel();
        found.setOpaque(false);
        found.setLayout(
                new BoxLayout(found, BoxLayout.Y_AXIS)
        );

        JLabel info = nhan(
                "Hóa đơn " + hoaDon.maHD()
                        + " • " + hoaDon.tenKH(),
                14, XANH, Font.BOLD
        );

        found.add(info);

        found.add(nhan(
                "Ngày mua: "
                        + hoaDon.ngayLap().format(
                                DateTimeFormatter.ofPattern(
                                        "dd/MM/yyyy HH:mm"
                                )
                        )
                        + "  |  Trạng thái: "
                        + hoaDon.trangThai(),
                12, NHAT, Font.PLAIN
        ));

        found.add(Box.createVerticalStrut(10));

        found.add(nhan(
                "Chọn sản phẩm cần đổi/trả:",
                12, XANH, Font.BOLD
        ));

        dsHang.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        dsHang.setFixedCellHeight(36);

        JScrollPane ls = new JScrollPane(dsHang);

        ls.setPreferredSize(
                new Dimension(
                        350,
                        Math.min(
                                175,
                                Math.max(55, modelHang.size() * 38)
                        )
                )
        );

        ls.setBorder(
                BorderFactory.createLineBorder(VIEN)
        );

        found.add(ls);
        found.add(Box.createVerticalStrut(12));

        for (JCheckBox cb : new JCheckBox[]{
                cbNhomCam, cbTinhTrang
        }) {
            cb.setOpaque(false);
            cb.setFont(
                    new Font("Segoe UI", Font.PLAIN, 12)
            );
            found.add(cb);
        }

        found.add(Box.createVerticalStrut(10));
        found.add(btnKiemTra);
        found.add(Box.createVerticalStrut(10));
        found.add(khungDoiHang);
        found.add(Box.createVerticalStrut(12));
        found.add(khungTraHang);

        found.add(Box.createVerticalStrut(10));

        found.add(nhan(
                "Chỉ thử nghiệm trên bản sao DB; hoàn tiền phải được UC007 đối soát.",
                11, CAM, Font.PLAIN
        ));

        khuKetQua.add(found, BorderLayout.CENTER);

        lblTrangThai.setText(
                modelHang.isEmpty()
                        ? "Hóa đơn chưa có chi tiết sản phẩm."
                        : "Đã tìm thấy hóa đơn."
        );

        revalidate();
        repaint();
    }

    // KIEM TRA DIEU KIEN DOI TRA
    private void kiemTra() {
        DoiTraService.KetQua k = service.kiemTra(
                hoaDon,
                dsHang.getSelectedValue(),
                cbTinhTrang.isSelected(),
                cbNhomCam.isSelected()
        );

        lblTrangThai.setForeground(
                k.dat() ? new Color(28, 133, 87) : CAM
        );

        lblTrangThai.setText(k.thongBao());

        if (k.dat()) {
            khungTraHang.setVisible(true);
            taiHangThayThe();
        } else {
            khungDoiHang.setVisible(false);
            khungTraHang.setVisible(false);
        }

        JOptionPane.showMessageDialog(
                this,
                k.thongBao(),
                k.dat()
                        ? "Kiểm tra sơ bộ đạt"
                        : "Không đủ điều kiện",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void khoiChonHangMoi() {
        khungDoiHang.setOpaque(false);
        khungDoiHang.setLayout(
                new BoxLayout(khungDoiHang, BoxLayout.Y_AXIS));
        khungDoiHang.setAlignmentX(LEFT_ALIGNMENT);
        for (JComponent c : new JComponent[]{
                nhan("Sản phẩm thay thế (còn tồn kho)",
                        13, XANH, Font.BOLD),
                cbSanPhamMoi,
                nhan("Hình thức thanh toán khoản chênh lệch:",
                        12, XANH, Font.BOLD), cbPhuongThuc,
                btnTinhChenh, lblBaoGia, cbDaThuTien,
                btnXacNhan
        }) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            khungDoiHang.add(c);
            khungDoiHang.add(Box.createVerticalStrut(8));
        }
        cbSanPhamMoi.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42));
        cbPhuongThuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cbDaThuTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        khungDoiHang.setVisible(false);
    }

    private void taiHangThayThe() {
        DoiTraDAO.DongHang cu = dsHang.getSelectedValue();
        if (cu == null) return;
        lblBaoGia.setText("Đang tải danh sách sản phẩm thay thế...");
        khungDoiHang.setVisible(true);
        btnTinhChenh.setEnabled(false);
        new SwingWorker<java.util.List<DoiTraDAO.SanPhamThayThe>, Void>() {
            @Override
            protected java.util.List<DoiTraDAO.SanPhamThayThe>
                    doInBackground() {
                return service.sanPhamThayThe(cu.maSP());
            }
            @Override
            protected void done() {
                modelThayThe.removeAllElements();
                try {
                    for (DoiTraDAO.SanPhamThayThe p : get()) {
                        modelThayThe.addElement(p);
                    }
                    boolean coHang = modelThayThe.getSize() > 0;
                    btnTinhChenh.setEnabled(coHang);
                    lblBaoGia.setText(coHang
                            ? "Chọn hàng mới để tính số tiền khách trả thêm."
                            : "Không tìm thấy phụ kiện còn hàng để thay thế.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    lblBaoGia.setText(cause == null
                            ? "Lỗi không xác định"
                            : cause.getMessage());
                }
                revalidate();
                repaint();
            }
        }.execute();
    }

    private void tinhGiaDoi() {
        DoiTraService.BaoGiaDoi baoGia = service.baoGiaDoi(
                hoaDon,
                dsHang.getSelectedValue(),
                (DoiTraDAO.SanPhamThayThe)
                        cbSanPhamMoi.getSelectedItem(),
                cbTinhTrang.isSelected(),
                cbNhomCam.isSelected()
        );
        if (baoGia.hopLe()) {
            giaDaBao = baoGia.tienChenhLech();
            btnXacNhan.setEnabled(maNV != null && !maNV.isBlank());
            lblBaoGia.setForeground(new Color(24, 124, 79));
            lblBaoGia.setText("Khách trả thêm: "
                    + baoGia.tienChenhLech().toPlainString() + " đ");
        } else {
            huyBaoGia();
            lblBaoGia.setForeground(CAM);
            lblBaoGia.setText(baoGia.thongBao());
        }
        JOptionPane.showMessageDialog(this,
                baoGia.thongBao() + (baoGia.hopLe()
                        ? "\nSố tiền chênh lệch: "
                            + baoGia.tienChenhLech().toPlainString() + " đ"
                        : ""),
                "Báo giá đổi hàng",
                baoGia.hopLe()
                        ? JOptionPane.INFORMATION_MESSAGE
                        : JOptionPane.WARNING_MESSAGE);
    }

    private void huyBaoGia() {
        giaDaBao = null;
        btnXacNhan.setEnabled(false);
        cbDaThuTien.setSelected(false);
        giaHoanDaBao = null;
        btnXacNhanTra.setEnabled(false);
        cbDaHoan.setSelected(false);
    }

    private void xacNhanDoiHang() {
        if (!kiemTraCheDoGhi()) return;
        if (giaDaBao == null || hoaDon == null) return;
        DoiTraDAO.DongHang cu = dsHang.getSelectedValue();
        DoiTraDAO.SanPhamThayThe moi =
                (DoiTraDAO.SanPhamThayThe) cbSanPhamMoi.getSelectedItem();
        if (cu == null || moi == null) return;
        final java.math.BigDecimal baoGia = giaDaBao;
        final String phuongThuc = (String) cbPhuongThuc.getSelectedItem();
        final boolean daThu = cbDaThuTien.isSelected();
        final boolean tinhTrangDat = cbTinhTrang.isSelected();
        final boolean nhomDat = cbNhomCam.isSelected();
        if (baoGia.signum() > 0 && !daThu) {
            JOptionPane.showMessageDialog(this,
                    "Phải xác nhận đã thu đủ " + baoGia + " đ.");
            return;
        }
        int yes = JOptionPane.showConfirmDialog(this,
                "Đổi " + cu.tenSP() + " sang " + moi.tenSP()
                + "\nKhách trả thêm: " + baoGia + " đ"
                + "\nHình thức: " + phuongThuc
                + "\nXác nhận đã thu tiền và cập nhật kho?",
                "Xác nhận giao dịch UC002", JOptionPane.YES_NO_OPTION);
        if (yes != JOptionPane.YES_OPTION) return;
        btnXacNhan.setEnabled(false);
        new SwingWorker<com.titipet.dao.DoiTraGiaoDichDAO.KetQuaLuu, Void>() {
            @Override protected com.titipet.dao.DoiTraGiaoDichDAO.KetQuaLuu
                    doInBackground() {
                return service.xacNhanDoiHang(maNV, hoaDon, cu, moi,
                        tinhTrangDat, nhomDat,
                        daThu, phuongThuc, baoGia);
            }
            @Override protected void done() {
                try {
                    var k = get();
                    JOptionPane.showMessageDialog(DoiTraPanel.this,
                            (Boolean.getBoolean("titipet.uc002.rollbackOnly")
                                    ? "THỬ THÀNH CÔNG — ĐÃ ROLLBACK. Phiếu: "
                                    : "Đã đổi thành công. Phiếu: ") + k.maDoiTra()
                            + "\nHóa đơn phụ thu: "
                            + (k.maHoaDonPhuThu() == null ? "Không cần" : k.maHoaDonPhuThu())
                            + "\nThu thêm: " + k.tienTraThem() + " đ");
                    huyBaoGia();
                    traCuu();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    String message = e.getCause() == null ? e.getMessage()
                            : e.getCause().getMessage();
                    JOptionPane.showMessageDialog(DoiTraPanel.this, message,
                            "Không thể đổi hàng", JOptionPane.ERROR_MESSAGE);
                    huyBaoGia();
                }
            }
        }.execute();
    }

    private void khoiTraHang() {
        khungTraHang.setOpaque(false);
        khungTraHang.setLayout(new BoxLayout(khungTraHang,BoxLayout.Y_AXIS));
        khungTraHang.setAlignmentX(LEFT_ALIGNMENT);
        for (JComponent item : new JComponent[]{
                nhan("TRẢ HÀNG / HOÀN TIỀN",13,XANH,Font.BOLD),
                btnTinhHoan,lblGiaHoan,
                nhan("Hình thức hoàn tiền",12,XANH,Font.BOLD),cbHoanBang,
                cbDaHoan,btnXacNhanTra}) {
            item.setAlignmentX(LEFT_ALIGNMENT);
            khungTraHang.add(item);
            khungTraHang.add(Box.createVerticalStrut(8));
        }
        cbDaHoan.setFont(new Font("Segoe UI",Font.PLAIN,12));
        cbHoanBang.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        khungTraHang.setVisible(false);
    }

    private void tinhGiaTra() {
        var bg = service.baoGiaTra(hoaDon,dsHang.getSelectedValue(),
                cbTinhTrang.isSelected(),cbNhomCam.isSelected());
        if(!bg.hopLe()) {
            huyBaoGia();
            lblGiaHoan.setForeground(CAM);
            lblGiaHoan.setText(bg.thongBao());
        } else {
            giaHoanDaBao=bg.tienHoan();
            lblGiaHoan.setForeground(new Color(24,124,79));
            lblGiaHoan.setText("Dự kiến hoàn: " + bg.tienHoan().toPlainString() + " đ");
            btnXacNhanTra.setEnabled(maNV != null && !maNV.isBlank());
        }
    }

    private boolean kiemTraCheDoGhi() {
        if (Boolean.getBoolean("titipet.uc002.testWrite") &&
             (Boolean.getBoolean("titipet.uc002.rollbackOnly") ||
              Boolean.getBoolean("titipet.uc002.allowCommit"))) return true;
        JOptionPane.showMessageDialog(this,
                "Ghi dữ liệu đang khóa. Cần kiểm thử rollback trên bản sao CSDL.");
        return false;
    }

    private void xacNhanTraHang() {
        if (!kiemTraCheDoGhi()) return;
        if (giaHoanDaBao == null || hoaDon == null || dsHang.getSelectedValue()==null) return;
        if (!cbDaHoan.isSelected()) {
            JOptionPane.showMessageDialog(this,"Phải xác nhận đã hoàn đủ tiền trước khi lưu.");
            return;
        }
        var hd=hoaDon;
        var cu=dsHang.getSelectedValue();
        var gia=giaHoanDaBao;
        String method=(String)cbHoanBang.getSelectedItem();
        boolean tinhTrang=cbTinhTrang.isSelected();
        boolean nhom=cbNhomCam.isSelected();
        int confirm=JOptionPane.showConfirmDialog(this,
                "Xác nhận đã HOÀN " + gia + " đ qua " + method
                + "\nSản phẩm: " + cu.tenSP() + "\nHóa đơn: " + hd.maHD()
                + "\nGhi phiếu và nhập lại tồn kho?",
                "Xác nhận trả hàng", JOptionPane.YES_NO_OPTION);
        if(confirm != JOptionPane.YES_OPTION) return;
        btnXacNhanTra.setEnabled(false);
        new SwingWorker<com.titipet.dao.DoiTraGiaoDichDAO.KetQuaTra,Void>() {
            @Override protected com.titipet.dao.DoiTraGiaoDichDAO.KetQuaTra doInBackground() {
                return service.xacNhanTraHang(maNV,hd,cu,gia,method,true,tinhTrang,nhom);
            }
            @Override protected void done() {
                try {
                    var k=get();
                    JOptionPane.showMessageDialog(DoiTraPanel.this,
                        (Boolean.getBoolean("titipet.uc002.rollbackOnly")
                            ? "THỬ THÀNH CÔNG — ĐÃ ROLLBACK: " : "Đã trả hàng: ")
                        + k.maDoiTra() + "\nTiền hoàn: " + k.tienHoan() + " đ");
                    huyBaoGia();
                    traCuu();
                } catch(InterruptedException ex) {Thread.currentThread().interrupt();}
                catch(ExecutionException ex) {
                    JOptionPane.showMessageDialog(DoiTraPanel.this,
                       ex.getCause()==null?ex.getMessage():ex.getCause().getMessage(),
                       "Không thể trả hàng",JOptionPane.ERROR_MESSAGE);
                    huyBaoGia();
                }
            }
        }.execute();
    }

    // CAC HAM TAO GIAO DIEN
    private static JButton nut(
            String title,
            boolean primary) {

        JButton b = new JButton(title);

        b.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        b.setBackground(
                primary
                        ? new Color(37, 95, 183)
                        : Color.WHITE
        );

        b.setForeground(
                primary ? Color.WHITE : XANH
        );

        b.setFocusPainted(false);

        b.putClientProperty(
                "FlatLaf.style",
                "arc: 10; margin: 9,14,9,14"
        );

        return b;
    }

    private static JLabel nhan(
            String text,
            int size,
            Color color,
            int style) {

        JLabel l = new JLabel(text);

        l.setFont(
                new Font("Segoe UI", style, size)
        );

        l.setForeground(color);

        return l;
    }

    private static JPanel card() {
        JPanel p = new RoundedCard();

        p.setLayout(new BorderLayout(0, 15));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        return p;
    }
    
/**
 * Icon vector cho UC002.
 * Khong phu thuoc emoji hoac font he dieu hanh.
 */
private static class DoiTraSymbol implements Icon {

    public static final int EXCHANGE = 1;
    public static final int PAW = 2;

    private final int type;
    private final int size;

    public DoiTraSymbol(int type, int size) {
        this.type = type;
        this.size = size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(
            Component c,
            Graphics g,
            int x,
            int y) {

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.translate(x, y);
            g2.scale(size / 24.0, size / 24.0);

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(XANH);

            if (type == EXCHANGE) {

                g2.setStroke(new BasicStroke(
                        1.8f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                // Mui ten phia tren huong sang phai
                g2.drawLine(3, 8, 20, 8);
                g2.drawLine(16, 4, 20, 8);
                g2.drawLine(20, 8, 16, 12);

                // Mui ten phia duoi huong sang trai
                g2.drawLine(4, 16, 21, 16);
                g2.drawLine(4, 16, 8, 12);
                g2.drawLine(4, 16, 8, 20);

            } else if (type == PAW) {

                // Bon ngon chan
                g2.fillOval(2, 8, 5, 6);
                g2.fillOval(7, 3, 5, 7);
                g2.fillOval(13, 3, 5, 7);
                g2.fillOval(18, 8, 5, 6);

                // Dem ban chan
                g2.fillOval(6, 13, 12, 9);
            }

        } finally {
            g2.dispose();
        }
    }
}

    private static class RoundedCard extends JPanel {

        RoundedCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    24, 24
            );

            g2.setColor(VIEN);
            g2.drawRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    24, 24
            );

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
