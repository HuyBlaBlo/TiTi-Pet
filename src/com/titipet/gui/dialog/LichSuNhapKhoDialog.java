package com.titipet.gui.dialog;

import com.titipet.bus.KhoService;
import com.titipet.dao.KhoHangDAO.LichSuNhapItem;
import com.titipet.util.RoundedPannel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LichSuNhapKhoDialog extends JDialog {

    private final KhoService service;
    private JTable tblLichSu;
    private DefaultTableModel model;
    private final DecimalFormat dfTien = new DecimalFormat("#,##0 đ");
    private static final DateTimeFormatter DF_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DF_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LichSuNhapKhoDialog(Frame parent, KhoService service) {
        super(parent, "Lịch sử nhập kho sản phẩm", true);
        this.service = service;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        RoundedPannel card = new RoundedPannel(Color.WHITE, 16);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("Lịch sử nhập kho & điều chỉnh tồn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));

        JButton btnClose = new JButton("X");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnClose.setBackground(new Color(241, 245, 249));
        btnClose.setPreferredSize(new Dimension(32, 32));
        btnClose.setBorder(null);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] cols = {"MÃ PHIẾU", "MÃ SP", "TÊN SẢN PHẨM", "SL NHẬP", "ĐƠN GIÁ NHẬP", "HSD GHI NHẬN", "THỜI GIAN NHẬP", "GHI CHÚ"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblLichSu = new JTable(model);
        tblLichSu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblLichSu.setRowHeight(44);
        tblLichSu.setShowVerticalLines(false);
        tblLichSu.setGridColor(new Color(241, 245, 249));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblLichSu.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblLichSu.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(tblLichSu);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
        setSize(950, 520);
        setLocationRelativeTo(parent);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        try {
            List<LichSuNhapItem> list = service.layDanhSachLichSuNhapKho();
            for (LichSuNhapItem item : list) {
                String strHsd = item.hanSuDungMoi != null ? item.hanSuDungMoi.format(DF_DATE) : "--";
                String strNgay = item.ngayNhap != null ? item.ngayNhap.format(DF_TIME) : "--";
                model.addRow(new Object[]{
                        "#" + item.maLichSu,
                        item.maSP,
                        item.tenSP,
                        "+" + item.soLuongNhap,
                        dfTien.format(item.giaNhap),
                        strHsd,
                        strNgay,
                        item.ghiChu != null ? item.ghiChu : ""
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi nạp lịch sử: " + ex.getMessage());
        }
    }
}