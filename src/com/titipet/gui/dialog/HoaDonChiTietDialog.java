package com.titipet.gui.dialog;

import com.titipet.bus.HoaDonLuuTruService;
import com.titipet.bus.HoaDonService;
import com.titipet.dao.HoaDonDAO.DongChiTiet;
import com.titipet.dao.HoaDonDAO.HoaDonChiTiet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Popup hóa đơn thống nhất cho POS và lịch sử giao dịch. */
public final class HoaDonChiTietDialog {
    private static final Color BLUE = new Color(35, 101, 191);
    private static final Color INK = new Color(33, 58, 97);
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private HoaDonChiTietDialog() { }

    public static void hienThi(Component parent, HoaDonChiTiet h) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Hóa đơn TiTi Pet | " + h.maHoaDon(),
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.getContentPane().setBackground(new Color(245, 248, 253));
        dialog.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BLUE);
        header.setBorder(new EmptyBorder(20, 26, 18, 26));
        JLabel title = new JLabel("TI TI PET  |  HÓA ĐƠN BÁN HÀNG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Chi tiết thanh toán   •   " + h.maHoaDon());
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(220, 236, 255));
        header.add(subtitle, BorderLayout.SOUTH);
        dialog.add(header, BorderLayout.NORTH);

        JEditorPane receipt = new JEditorPane("text/html", html(h));
        receipt.setEditable(false);
        receipt.setBackground(Color.WHITE);
        receipt.setCaretPosition(0);
        JScrollPane pane = new JScrollPane(receipt);
        pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 20),
                BorderFactory.createLineBorder(new Color(225, 233, 245))));
        pane.getVerticalScrollBar().setUnitIncrement(18);
        dialog.add(pane, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(0, 15, 5, 15));
        JButton close = button("Đóng", Color.WHITE, INK);
        JButton export = button("Xuất hóa đơn mã hóa (.tpet)", Color.WHITE, BLUE);
        JButton print = button("In hóa đơn", BLUE, Color.WHITE);
        close.addActionListener(e -> dialog.dispose());
        export.addActionListener(e -> xuatTep(dialog, h));
        print.addActionListener(e -> {
            try {
                receipt.print(null, null, true, null, null, true);
            } catch (Exception error) {
                JOptionPane.showMessageDialog(dialog, "Không thể in: " + error.getMessage(),
                        "Lỗi in hóa đơn", JOptionPane.ERROR_MESSAGE);
            }
        });
        actions.add(close); actions.add(export); actions.add(print);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setSize(790, 790);
        dialog.setMinimumSize(new Dimension(590, 530));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void xuatTep(JDialog parent, HoaDonChiTiet h) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất bản hóa đơn mã hóa");
        chooser.setSelectedFile(new File("TiTiPet_" + h.maHoaDon() + ".tpet"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase(Locale.ROOT).endsWith(".tpet"))
            f = new File(f.getParentFile(), f.getName() + ".tpet");
        if (f.exists() && JOptionPane.showConfirmDialog(parent,
                "Tệp đã tồn tại, ghi đè?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        final File target = f;
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                new HoaDonLuuTruService().xuat(h.maHoaDon(), target.toPath());
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(parent, "Đã xuất bản mã hóa:\n" + target
                            + "\nHãy sao lưu khóa mã hóa riêng để có thể khôi phục tệp.");
                } catch (Exception err) {
                    Throwable cause = err.getCause() == null ? err : err.getCause();
                    JOptionPane.showMessageDialog(parent, "Không xuất được hóa đơn:\n" + cause.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static JButton button(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 229, 241)),
                new EmptyBorder(10, 15, 10, 15)));
        return b;
    }

    private static String html(HoaDonChiTiet h) {
        StringBuilder b = new StringBuilder();
        b.append("<html><body style='font-family:SansSerif;color:#213a61;background:#ffffff; margin:22px'>")
         .append("<div style='text-align:center'><h1 style='color:#2365bf'>TI TI PET</h1>")
         .append("<div>PET SHOP &amp; SPA • GÒ VẤP, TP. HCM</div>")
         .append("<p>245 Đường Số 8, P.11, Gò Vấp, TP.HCM<br/>0903 845 579 • 0917 131 337</p>")
         .append("<h2>HÓA ĐƠN BÁN HÀNG</h2></div>")
         .append("<table width='100%' cellpadding='6' cellspacing='0'>")
         .append(tr("Mã hóa đơn", h.maHoaDon()))
         .append(tr("Ngày lập", h.ngayLap() == null ? "—" : NGAY.format(h.ngayLap())))
         .append(tr("Khách hàng", h.tenKhach())).append(tr("Điện thoại", h.soDienThoai()))
         .append(tr("Nhân viên", h.tenNhanVien()))
         .append(tr("Nguồn đơn", HoaDonService.tenNguon(h.nguonDon())))
         .append(tr("Thanh toán", HoaDonService.tenThanhToan(h.phuongThuc())))
         .append(tr("Trạng thái", HoaDonService.tenTrangThai(h.trangThai())))
         .append("</table><hr style='color:#d8e5f3'/>")
         .append("<table width='100%' cellspacing='0' cellpadding='7' border='0'>")
         .append("<tr style='background:#e8f2ff;color:#235baa'><th align='left'>SẢN PHẨM / DỊCH VỤ</th>")
         .append("<th>SL</th><th align='right'>ĐƠN GIÁ</th><th align='right'>THÀNH TIỀN</th></tr>");
        for (DongChiTiet d : h.cacDong()) {
            b.append("<tr><td><b>").append(esc(d.ten())).append("</b><br/><small>")
             .append(esc(d.loai()));
            if (d.moTa() != null && !d.moTa().isBlank()) b.append(" • ").append(esc(d.moTa()));
            b.append("</small></td><td align='center'>").append(d.soLuong())
             .append("</td><td align='right'>").append(tien(d.donGia()))
             .append("</td><td align='right'>").append(tien(d.thanhTien()))
             .append("</td></tr>");
        }
        b.append("</table><hr style='color:#d8e5f3'/>")
         .append("<table width='100%' cellpadding='6' cellspacing='0'>")
         .append(tr("Tiền hàng", tien(h.tongTienHang())))
         .append(tr("Dịch vụ Spa", tien(h.tongTienSpa())));
        if (h.tenKhuyenMai() != null && !h.tenKhuyenMai().isBlank())
            b.append(tr("Mã ưu đãi", h.tenKhuyenMai()));
        b.append(tr("Giảm giá khuyến mãi", "− " + tien(h.tienGiamKM())))
         .append(tr("Ưu đãi thành viên", "− " + tien(h.tienGiamHangTV())))
         .append(tr("Đổi điểm (" + h.soDiemSuDung() + ")", "− " + tien(h.tienGiamDiem())))
         .append(tr("Giá trị tính thuế", tien(h.tongTienTruocThue())))
         .append(tr("Thuế GTGT (8%)", "+ " + tien(h.tongTienThue())))
         .append("<tr style='background:#e8f2ff'><td><h2>TỔNG THANH TOÁN</h2></td><td align='right'><h2>")
         .append(tien(h.tienPhaiThu())).append("</h2></td></tr>")
         .append(tr("Điểm được tích", "+ " + h.soDiemDuocTich() + " điểm"))
         .append("</table><p align='center'><b>Cảm ơn quý khách đã tin tưởng TiTi Pet!</b></p>")
         .append("<p align='center'><small>Vui lòng giữ hóa đơn để được hỗ trợ khi cần.</small></p>")
         .append("</body></html>");
        return b.toString();
    }

    private static String tr(String left, String right) {
        return "<tr><td>" + esc(left) + "</td><td align='right'><b>" + esc(right) + "</b></td></tr>";
    }
    private static String tien(java.math.BigDecimal n) {
        return HoaDonService.tien(n) + " đ";
    }
    private static String esc(String s) {
        if (s == null) return "—";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
