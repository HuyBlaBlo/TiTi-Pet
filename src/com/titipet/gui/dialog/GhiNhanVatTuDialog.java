
package com.titipet.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class GhiNhanVatTuDialog extends JDialog {

    private JTextField txtVatTu;
    private JButton btnLuu;
    private JButton btnHuy;

    private String vatTuDaLuu;

    private static final Color BLUE = new Color(36, 101, 190);
    private static final Color DARK_BLUE = new Color(29, 61, 112);
    private static final Color BG = new Color(244, 248, 255);
    private static final Color BORDER = new Color(225, 233, 245);
    private static final Color TEXT = new Color(80, 105, 145);

    public GhiNhanVatTuDialog(Frame owner) {
        super(owner, "Ghi nhận vật tư", true);

        khoiTaoGiaoDien();

        setSize(420, 250);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void khoiTaoGiaoDien() {

        JPanel pnlMain = new JPanel(new BorderLayout(0, 15));
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(new EmptyBorder(25, 28, 25, 28));

        // ===== HEADER =====
        JLabel lblTieuDe = new JLabel("GHI NHẬN VẬT TƯ");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTieuDe.setForeground(DARK_BLUE);
        lblTieuDe.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblMoTa = new JLabel("Ghi nhận vật tư tiêu hao cho ca Spa");
        lblMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMoTa.setForeground(TEXT);
        lblMoTa.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.add(lblTieuDe);
        pnlHeader.add(lblMoTa);

        // ===== INPUT =====
        JPanel pnlInput = new JPanel(new BorderLayout(0, 8));
        pnlInput.setBackground(Color.WHITE);

        JLabel lblVatTu = new JLabel("Vật tư tiêu hao");
        lblVatTu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblVatTu.setForeground(DARK_BLUE);

        txtVatTu = new JTextField();
        txtVatTu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtVatTu.setPreferredSize(new Dimension(0, 38));
        txtVatTu.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));

        txtVatTu.setToolTipText("Nhập tên vật tư tiêu hao");

        pnlInput.add(lblVatTu, BorderLayout.NORTH);
        pnlInput.add(txtVatTu, BorderLayout.CENTER);

        // ===== BUTTON =====
        btnLuu = new RoundedButton(
                "Lưu vật tư",
                new Color(36, 101, 190)
        );
        btnLuu.setBackground(BLUE);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLuu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnHuy = new RoundedButton(
                "Huỷ",
                new Color(36, 101, 190)
        );
        btnHuy.setBackground(BG);
        btnHuy.setForeground(DARK_BLUE);
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel pnlButton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButton.setBackground(Color.WHITE);
        pnlButton.add(btnHuy);
        pnlButton.add(btnLuu);

        // ===== EVENTS =====
        btnLuu.addActionListener(e -> luuVatTu());
        btnHuy.addActionListener(e -> dispose());

        txtVatTu.addActionListener(e -> luuVatTu());

        // ===== ADD COMPONENT =====
        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlInput, BorderLayout.CENTER);
        pnlMain.add(pnlButton, BorderLayout.SOUTH);

        setContentPane(pnlMain);
    }
    
    private void luuVatTu() {

        String vatTu = txtVatTu.getText().trim();

        if (vatTu.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập tên vật tư tiêu hao!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            txtVatTu.requestFocus();
            return;
        }

        vatTuDaLuu = vatTu;

        JOptionPane.showMessageDialog(
                this,
                "Ghi nhận vật tư thành công!",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        );

        dispose();
    }

    public String getVatTuDaLuu() {
        return vatTuDaLuu;
    }
    private static class RoundedButton extends JButton {

        private final Color bgColor;

        public RoundedButton(String text, Color color) {
            super(text);
            this.bgColor = color;

            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.dispose();
            super.paintComponent(g);
        }
    }

}
