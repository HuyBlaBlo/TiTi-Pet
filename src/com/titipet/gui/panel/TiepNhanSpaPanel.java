package com.titipet.gui.panel;

import com.titipet.dao.DichVuSpaDAO;
import com.titipet.dao.LichHenDAO;
import com.titipet.entity.DichVuSpa;
import com.titipet.entity.LichHen;
import com.titipet.entity.NhanVien;
import com.titipet.entity.enums.TrangThaiLichHen;
import com.titipet.bus.TiepNhanService;
import com.titipet.bus.TiepNhanSpaContext;
import java.util.function.Consumer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.titipet.bus.KhachHangService;
import com.titipet.entity.KhachHang;
import com.titipet.entity.ThuCung;
import com.titipet.gui.dialog.ThemKhachHangDialog;
import com.titipet.gui.dialog.ThemThuCungDialog;



public class TiepNhanSpaPanel extends JPanel {
	private final LichHenDAO lichHenDAO = new LichHenDAO();
	private final DichVuSpaDAO dichVuSpaDAO = new DichVuSpaDAO();
	private final TiepNhanService tiepNhanService = new TiepNhanService();
	
	private final KhachHangService khachHangService = new KhachHangService();

	// Khách hàng và thú cưng của khách vãng lai đang tiếp nhận
	private KhachHang khachVangLaiDangChon;
	private ThuCung thuCungVangLaiDangChon;

    private static final Color BG = new Color(245,248,253);
    private static final Color BLUE = new Color(36,101,190);
    private static final Color LIGHT_BLUE = new Color(235,243,255);
    private static final Color TEXT = new Color(35,51,75);
    private static final Color MUTED = new Color(117,135,160);
    private static final Color BORDER = new Color(222,231,243);
    private static final Color RED = new Color(190,55,65);
    private static final Color GREEN = new Color(24,138,105);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0 đ");

    private List<LichHen> danhSachCho = new ArrayList<>();
    private List<NhanVien> danhSachTho = new ArrayList<>();
    private List<DichVuSpa> danhSachDichVu = new ArrayList<>();
    private LichHen lichHenDangChon;
    private boolean khachVangLai;
    private JLabel lblTenKhachHang;

    private JLabel lblChoTiepNhan, lblDangPhucVu, lblDaHoanThanh;
    private JPanel danhSachKhachPanel;
    private JLabel lblMaLich, lblTenKhach, lblTenThuCung, lblGioHen;
    private JPanel panelDichVu;
    private List<JCheckBox> danhSachCheckBoxDichVu = new ArrayList<>();
    private JRadioButton radBinhThuong;
    private JRadioButton radRoiLong;
    private JRadioButton radVeRan;
    private JRadioButton radKhac;
    private JComboBox<NhanVien> cboThoSpa;
    private JTextField txtCanNang, txtTinhTrangKhac;
    private JTextArea txtGhiChu;
    private JLabel lblGioTra, lblGiaDichVu, lblPhuThu, lblTongTien;
    
    private Consumer<TiepNhanSpaContext> onLapHoaDon;

    public void setOnLapHoaDon(Consumer<TiepNhanSpaContext> callback) {
        this.onLapHoaDon = callback;
    }

    public TiepNhanSpaPanel() {
        initComponents();
        loadDuLieu();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0,8));
        setBackground(BG);
        setBorder(new EmptyBorder(10,12,10,12));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
        top.add(createHeader());
        top.add(Box.createVerticalStrut(8));
        top.add(createStatistics());
        add(top,BorderLayout.NORTH);
        add(createMainContent(),BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout(8,0));
        p.setOpaque(false);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));

        JLabel crumb = new JLabel("TI TI PET  •  WORKSPACE");
        crumb.setFont(new Font("Segoe UI",Font.BOLD,9));
        crumb.setForeground(BLUE);
        JLabel title = new JLabel("Tiếp nhận thú cưng");
        title.setFont(new Font("Segoe UI",Font.BOLD,20));
        title.setForeground(TEXT);
        JLabel sub = new JLabel("Kiểm tra tình trạng thú cưng và xác nhận dịch vụ Spa");
        sub.setFont(new Font("Segoe UI",Font.PLAIN,10));
        sub.setForeground(MUTED);
        left.add(crumb); left.add(title); left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,2));
        right.setOpaque(false);
        right.add(createBadge(LocalDate.now().format(DATE),Color.WHITE,MUTED));
        right.add(createBadge("● Chưa mở ca",new Color(255,247,224),new Color(190,119,20)));
        p.add(left,BorderLayout.WEST);
        p.add(right,BorderLayout.EAST);
        return p;
    }

    private JPanel createStatistics() {
    	JPanel p = new JPanel(new GridLayout(1, 3, 8, 0));
        p.setOpaque(false);

        lblChoTiepNhan = new JLabel("0");
        lblDangPhucVu = new JLabel("0");
        lblDaHoanThanh = new JLabel("0");

        p.add(createStat(new JLabel("🐾"), lblChoTiepNhan,
                "Chờ tiếp nhận", new Color(255, 244, 229)));

        p.add(createStat(new JLabel("✂"), lblDangPhucVu,
                "Đang phục vụ", LIGHT_BLUE));

        p.add(createStat(new JLabel("✓"), lblDaHoanThanh,
                "Đã hoàn thành", new Color(229, 248, 241)));

        return p;
    }

    private JPanel createStat(JLabel jLabel, JLabel number,
            String caption, Color iconBg) {
    	 JPanel card = createCard();
    	    card.setLayout(new BorderLayout(10, 0));
    	    card.setBorder(new EmptyBorder(8, 10, 8, 10));

    	    // Ô chứa logo bo góc
    	    JPanel iconBox = new JPanel(new GridBagLayout()) {
    	        @Override
    	        protected void paintComponent(Graphics g) {
    	            Graphics2D g2 = (Graphics2D) g.create();

    	            g2.setRenderingHint(
    	                RenderingHints.KEY_ANTIALIASING,
    	                RenderingHints.VALUE_ANTIALIAS_ON
    	            );

    	            g2.setColor(iconBg);
    	            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

    	            g2.dispose();
    	        }
    	    };

    	    iconBox.setOpaque(false);
    	    iconBox.setPreferredSize(new Dimension(38, 38));
    	    iconBox.setMinimumSize(new Dimension(38, 38));
    	    iconBox.setMaximumSize(new Dimension(38, 38));

    	    // Logo nằm chính giữa ô
    	    jLabel.setOpaque(false);
    	    iconBox.add(jLabel);

    	    // Số lượng và tiêu đề
    	    number.setFont(new Font("Segoe UI", Font.BOLD, 23));
    	    number.setForeground(TEXT);

    	    JLabel sub = new JLabel(caption);
    	    sub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
    	    sub.setForeground(MUTED);

    	    JPanel text = new JPanel();
    	    text.setOpaque(false);
    	    text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

    	    text.add(number);
    	    text.add(sub);

    	    card.add(iconBox, BorderLayout.WEST);
    	    card.add(text, BorderLayout.CENTER);

    	    return card;
    }

    private JPanel createMainContent() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy=0; g.weighty=1; g.fill=GridBagConstraints.BOTH;
        g.gridx=0; g.weightx=1.8; g.insets=new Insets(0,0,0,8);
        p.add(createReceptionForm(),g);
        g.gridx=1; g.weightx=1; g.insets=new Insets(0,0,0,0);
        p.add(createWaitingList(),g);
        return p;
    }

    private JPanel createReceptionForm() {
        JPanel card=createCard();
        card.setLayout(new BorderLayout(0,7));
        card.setBorder(new EmptyBorder(9,10,8,10));

        JPanel header=new JPanel(new BorderLayout(6,0));
        header.setOpaque(false);
        JPanel titleGroup=new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup,BoxLayout.Y_AXIS));
        JLabel title=new JLabel("Phiếu tiếp nhận");
        title.setFont(new Font("Segoe UI",Font.BOLD,12)); title.setForeground(TEXT);
        JLabel sub=new JLabel("Một thú cưng • Một nhân viên phụ trách • Nhiều dịch vụ");
        sub.setFont(new Font("Segoe UI",Font.PLAIN,9)); sub.setForeground(MUTED);
        titleGroup.add(title); titleGroup.add(sub);
        JButton guest=button("+ Khách vãng lai",LIGHT_BLUE,BLUE);
        guest.addActionListener(e->moKhachVangLai());
        header.add(titleGroup,BorderLayout.CENTER); header.add(guest,BorderLayout.EAST);

        JPanel form=new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g=new GridBagConstraints();
        g.insets=new Insets(3,4,3,4); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1;

        lblTenKhach=new JLabel("Chưa chọn khách hàng");
        lblTenThuCung=new JLabel("Chưa chọn thú cưng");
        lblMaLich=new JLabel("Chưa có mã phiếu");
        g.gridy=0; g.gridx=0; g.gridwidth=1;
        form.add(infoBox("CHỦ THÚ CƯNG",lblTenKhach),g);
        g.gridx=1; form.add(infoBox("THÚ CƯNG",lblTenThuCung),g);
        g.gridx=2; form.add(infoBox("MÃ LỊCH HẸN",lblMaLich),g);

        lblGioHen=new JLabel("Giờ hẹn: --:--");
        lblGioHen.setFont(new Font("Segoe UI",Font.BOLD,10)); lblGioHen.setForeground(TEXT);
        g.gridy=1; g.gridx=0; g.gridwidth=3; form.add(lblGioHen,g);

        txtCanNang=new JTextField();
     // Chỉ cho nhập chữ số và tối đa một dấu thập phân
        ((javax.swing.text.AbstractDocument) txtCanNang.getDocument())
                .setDocumentFilter(new javax.swing.text.DocumentFilter() {

            @Override
            public void replace(FilterBypass fb, int offset, int length,
                                String text, javax.swing.text.AttributeSet attrs)
                    throws javax.swing.text.BadLocationException {

                String current = fb.getDocument().getText(
                        0, fb.getDocument().getLength());

                String updated = current.substring(0, offset)
                        + text
                        + current.substring(offset + length);

                if (updated.matches("\\d*([.,]\\d*)?")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String text,
                                     javax.swing.text.AttributeSet attrs)
                    throws javax.swing.text.BadLocationException {
                replace(fb, offset, 0, text, attrs);
            }
        });
        addField(form,g,2,"Cân nặng thực tế (kg) *",txtCanNang,3);

        panelDichVu = new JPanel();
        panelDichVu.setLayout(new BoxLayout(panelDichVu, BoxLayout.Y_AXIS));
        panelDichVu.setBackground(Color.WHITE);

        JScrollPane serviceScroll = new JScrollPane(panelDichVu);
        serviceScroll.setPreferredSize(new Dimension(100, 110));
        serviceScroll.setBorder(BorderFactory.createLineBorder(BORDER));

        g.gridy = 3;
        g.gridx = 0;
        g.gridwidth = 3;
        form.add(label("Dịch vụ xác nhận * (tick chọn nhiều dịch vụ)"), g);

        g.gridy = 4;
        form.add(serviceScroll, g);
        JPanel conditions=new JPanel(new GridLayout(1,4,4,0));
        conditions.setOpaque(false);
        radBinhThuong = new JRadioButton("Bình thường");
        radRoiLong = new JRadioButton("Rối lông (+50.000 đ)");
        radVeRan = new JRadioButton("Ve, bọ/rận (+70.000 đ)");
        radKhac = new JRadioButton("Khác (+100.000 đ)");

        ButtonGroup groupTinhTrang = new ButtonGroup();
        groupTinhTrang.add(radBinhThuong);
        groupTinhTrang.add(radRoiLong);
        groupTinhTrang.add(radVeRan);
        groupTinhTrang.add(radKhac);

        radBinhThuong.setSelected(true);

        txtTinhTrangKhac = new JTextField();
        txtTinhTrangKhac.setEnabled(false);

        // Bình thường
        radBinhThuong.addActionListener(e -> {
            txtTinhTrangKhac.setEnabled(false);
            txtTinhTrangKhac.setText("");
            capNhatGiaVaGioTra();
        });

        // Rối lông
        radRoiLong.addActionListener(e -> {
            txtTinhTrangKhac.setEnabled(false);
            txtTinhTrangKhac.setText("");
            capNhatGiaVaGioTra();
        });

        // Ve, bọ/rận
        radVeRan.addActionListener(e -> {
            txtTinhTrangKhac.setEnabled(false);
            txtTinhTrangKhac.setText("");
            capNhatGiaVaGioTra();
        });

        // Khác
        radKhac.addActionListener(e -> {
            txtTinhTrangKhac.setEnabled(true);
            txtTinhTrangKhac.requestFocus();
            capNhatGiaVaGioTra();
        });
        
     // Thêm 4 radio vào panel
        conditions.add(radBinhThuong);
        conditions.add(radRoiLong);
        conditions.add(radVeRan);
        conditions.add(radKhac);

        // Tiêu đề tình trạng lông
        g.gridy = 5;
        g.gridx = 0;
        g.gridwidth = 3;
        form.add(label("Tình trạng lông/da:"), g);

        // Hiển thị 4 radio button
        g.gridy = 6;
        form.add(conditions, g);
        
        txtTinhTrangKhac.setText("");
        capNhatGiaVaGioTra();
        JPanel other=new JPanel(new BorderLayout(6,0)); 
        other.setOpaque(false);
        other.add(new JLabel("Mô tả khác:"),BorderLayout.WEST); 
        other.add(txtTinhTrangKhac,BorderLayout.CENTER);
        g.gridy=7; 
        form.add(other,g);

        cboThoSpa=new JComboBox<>();
        cboThoSpa.setFont(new Font("Segoe UI",Font.PLAIN,11));
        cboThoSpa.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus){
                super.getListCellRendererComponent(list,value,index,selected,focus);
                if(value instanceof NhanVien)setText(((NhanVien)value).getHoTen());
                return this;
            }
        });
        addField(form,g,8,"Nhân viên Spa phụ trách *",cboThoSpa,3);

        lblGioTra=new JLabel("--:--");
        lblGioTra.setFont(new Font("Segoe UI",Font.BOLD,12)); lblGioTra.setForeground(TEXT);
        addField(form,g,9,"Giờ trả dự kiến (hệ thống tự tính)",valueBox(lblGioTra),3);

        txtGhiChu=new JTextArea(2,20);
        txtGhiChu.setLineWrap(true); txtGhiChu.setWrapStyleWord(true);
        JScrollPane note=new JScrollPane(txtGhiChu); note.setPreferredSize(new Dimension(100,48));
        addField(form,g,10,"Ghi chú tình trạng thực tế",note,3);

        JPanel prices=new JPanel(new GridLayout(3,1,0,1));
        prices.setBackground(new Color(229,248,241)); prices.setBorder(new EmptyBorder(5,8,5,8));
        lblGiaDichVu=new JLabel("Giá dịch vụ: 0 đ");
        lblPhuThu=new JLabel("Phụ thu: 0 đ");
        lblTongTien=new JLabel("Tổng dự kiến: 0 đ");
        lblGiaDichVu.setForeground(GREEN); lblPhuThu.setForeground(GREEN); lblTongTien.setForeground(GREEN);
        lblGiaDichVu.setFont(new Font("Segoe UI",Font.PLAIN,10));
        lblPhuThu.setFont(new Font("Segoe UI",Font.PLAIN,10));
        lblTongTien.setFont(new Font("Segoe UI",Font.BOLD,11));
        prices.add(lblGiaDichVu); prices.add(lblPhuThu); prices.add(lblTongTien);
        g.gridy=11; form.add(prices,g);

        JLabel notice=new JLabel("Chi phí là tạm tính; xác nhận trước khi lập hóa đơn.");
        notice.setFont(new Font("Segoe UI",Font.ITALIC,9)); notice.setForeground(new Color(190,119,20));
        g.gridy=12; form.add(notice,g);

        JPanel footer=new JPanel(new BorderLayout(8,0)); footer.setOpaque(false);
        JButton reject = button("× Từ chối dịch vụ",
        	    new Color(255,239,240), RED);
        JButton invoice=button("→ Tạo hóa đơn",BLUE,Color.WHITE);
        reject.addActionListener(e->tuChoiDichVu()); 
        invoice.addActionListener(e -> {
            if (onLapHoaDon != null) {
                taoHoaDon();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Callback lập hóa đơn chưa được cấu hình trong MainFrame!"
                );
            }
        });
        footer.add(reject,BorderLayout.WEST); footer.add(invoice,BorderLayout.EAST);

        JScrollPane formScroll=new JScrollPane(form);
        formScroll.setBorder(null); formScroll.getViewport().setBackground(Color.WHITE);
        formScroll.getVerticalScrollBar().setUnitIncrement(12);
        card.add(header,BorderLayout.NORTH); card.add(formScroll,BorderLayout.CENTER); card.add(footer,BorderLayout.SOUTH);
        return card;
    }

    private void addField(JPanel parent,GridBagConstraints g,int row,String title,JComponent input,int span){
        g.gridy=row; g.gridx=0; g.gridwidth=span;
        JPanel wrap=new JPanel(new BorderLayout(0,3)); wrap.setOpaque(false);
        wrap.add(label(title),BorderLayout.NORTH); wrap.add(input,BorderLayout.CENTER);
        parent.add(wrap,g);
    }

    private JPanel createWaitingList(){
        JPanel card=createCard(); card.setLayout(new BorderLayout(0,7));
        card.setBorder(new EmptyBorder(9,9,8,9));
        JPanel header=new JPanel(new BorderLayout()); header.setOpaque(false);
        JLabel title=new JLabel("Danh sách khách chờ");
        title.setFont(new Font("Segoe UI",Font.BOLD,12)); title.setForeground(TEXT);
        JButton refresh=button("Làm mới",LIGHT_BLUE,BLUE); refresh.addActionListener(e->loadDuLieu());
        header.add(title,BorderLayout.WEST); header.add(refresh,BorderLayout.EAST);

        danhSachKhachPanel=new JPanel();
        danhSachKhachPanel.setOpaque(false);
        danhSachKhachPanel.setLayout(new BoxLayout(danhSachKhachPanel,BoxLayout.Y_AXIS));
        JScrollPane scroll=new JScrollPane(danhSachKhachPanel);
        scroll.setBorder(null); scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        card.add(header,BorderLayout.NORTH); card.add(scroll,BorderLayout.CENTER);
        return card;
    }

    // Thẻ lịch chờ được thu nhỏ để hiển thị được nhiều lịch hơn.
    private JPanel createWaitingItem(LichHen lh){
        JPanel item=new JPanel(new BorderLayout(6,2));
        item.setBackground(new Color(249,251,255));
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),new EmptyBorder(5,6,5,6)));
//        JLabel icon=new JLabel("🐾"); icon.setFont(new Font("Segoe UI",Font.PLAIN,15));
        String pet=lh.getThuCung()==null?"Thú cưng":lh.getThuCung().getTenThuCung();
        String customer=lh.getKhachHang()==null?"Khách hàng":lh.getKhachHang().getHoTen();
        String service=lh.getDichVu()==null?"Dịch vụ Spa":lh.getDichVu().getTenDichVu();
        JLabel name=new JLabel(pet); name.setFont(new Font("Segoe UI",Font.BOLD,10)); name.setForeground(TEXT);
        JLabel detail=new JLabel(customer+" • "+service); detail.setFont(new Font("Segoe UI",Font.PLAIN,9)); detail.setForeground(MUTED);
        JLabel time=new JLabel(lh.getThoiGianHen().format(TIME)); time.setFont(new Font("Segoe UI",Font.BOLD,9)); time.setForeground(BLUE);
        JPanel info=new JPanel(); info.setOpaque(false); info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        info.add(name); info.add(detail); info.add(time);
        JButton status=button("Chờ",new Color(255,244,229),new Color(190,119,20));
        status.setFont(new Font("Segoe UI",Font.BOLD,9)); status.setBorder(new EmptyBorder(4,6,4,6));
        status.addActionListener(e->chonLichHen(lh));
//        item.add(icon,BorderLayout.WEST); 
        item.add(info,BorderLayout.CENTER); 
        item.add(status,BorderLayout.EAST);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseClicked(java.awt.event.MouseEvent e){chonLichHen(lh);}
        });
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE,58));
        return item;
    }

    private void loadDuLieu(){
        try{
            danhSachTho=lichHenDAO.layDanhSachThoSpa();
            danhSachDichVu = dichVuSpaDAO.layDanhSachDichVuSpa();
            cboThoSpa.removeAllItems();
            for(NhanVien nv:danhSachTho)cboThoSpa.addItem(nv);
            hienThiDanhSachDichVu();

            danhSachCho=new ArrayList<>();
            List<LichHen> today=lichHenDAO.layLichHenTheoNgay(LocalDate.now());
            for(LichHen lh:today)if(lh.getTrangThai()==TrangThaiLichHen.CHO_TIEP_NHAN)danhSachCho.add(lh);
            hienThiDanhSachCho(); capNhatThongKe();
            if(!danhSachCho.isEmpty()&&lichHenDangChon==null&&!khachVangLai)chonLichHen(danhSachCho.get(0));
        }catch(SQLException ex){showError("Không thể tải dữ liệu tiếp nhận: "+ex.getMessage());}
    }
    
    
    private void hienThiDanhSachCho(){
        danhSachKhachPanel.removeAll();
        if(danhSachCho.isEmpty()){
            JLabel empty=new JLabel("Hiện không có khách chờ.");
            empty.setFont(new Font("Segoe UI",Font.PLAIN,10)); empty.setForeground(MUTED);
            empty.setBorder(new EmptyBorder(10,4,10,4)); danhSachKhachPanel.add(empty);
        }else{
            for(LichHen lh:danhSachCho){
                danhSachKhachPanel.add(createWaitingItem(lh));
                danhSachKhachPanel.add(Box.createVerticalStrut(5));
            }
        }
        danhSachKhachPanel.revalidate(); danhSachKhachPanel.repaint();
    }

    private void capNhatThongKe(){
        lblChoTiepNhan.setText(String.valueOf(danhSachCho.size()));
        // Chưa có bảng/DAO thống kê ca đang phục vụ và đã hoàn thành.
        lblDangPhucVu.setText("0"); lblDaHoanThanh.setText("0");
    }

    private void chonLichHen(LichHen lh) {
        if (lh == null) return;
        
     // Xóa dữ liệu khách vãng lai đang tiếp nhận
        khachVangLaiDangChon = null;
        thuCungVangLaiDangChon = null;

        lichHenDangChon = lh;
        khachVangLai = false;

        lblTenKhach.setText(
                lh.getKhachHang() == null
                        ? "Khách hàng"
                        : lh.getKhachHang().getHoTen()
        );

        lblTenThuCung.setText(
                lh.getThuCung() == null
                        ? "Thú cưng"
                        : lh.getThuCung().getTenThuCung()
        );

        lblMaLich.setText(lh.getMaLichHen());
        lblGioHen.setText("Giờ hẹn: " + lh.getThoiGianHen().format(TIME));

        txtCanNang.setText(
                lh.getThuCung() == null
                        ? ""
                        : String.valueOf(lh.getThuCung().getCanNang())
        );

        txtGhiChu.setText(
                lh.getGhiChu() == null ? "" : lh.getGhiChu()
        );

        // Bỏ chọn toàn bộ dịch vụ cũ
        for (JCheckBox cb : danhSachCheckBoxDichVu) {
            cb.setSelected(false);
        }

        // Chọn dịch vụ có trong lịch hẹn
        if (lh.getDichVu() != null) {
            for (int i = 0; i < danhSachDichVu.size(); i++) {
                DichVuSpa dv = danhSachDichVu.get(i);

                if (dv.getMaDichVu().equals(lh.getDichVu().getMaDichVu())) {
                    danhSachCheckBoxDichVu.get(i).setSelected(true);
                    break;
                }
            }
        }

        // Chọn nhân viên Spa
        if (lh.getThoSpa() != null) {
            for (int i = 0; i < cboThoSpa.getItemCount(); i++) {
                if (cboThoSpa.getItemAt(i).getMaNV()
                        .equals(lh.getThoSpa().getMaNV())) {
                    cboThoSpa.setSelectedIndex(i);
                    break;
                }
            }
        }

        radBinhThuong.setSelected(true);
        txtTinhTrangKhac.setText("");
        txtTinhTrangKhac.setEnabled(false);

        capNhatGiaVaGioTra();
    }

    private void moKhachVangLai() {

        // Mở dialog thêm khách hàng
        Window owner = SwingUtilities.getWindowAncestor(this);

        ThemKhachHangDialog dialog =
                new ThemKhachHangDialog(
                        owner instanceof Frame ? (Frame) owner : null,
                        khachHangService
                );

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Dialog đóng thì tiếp tục lấy thông tin khách
        String sdt = JOptionPane.showInputDialog(
                this,
                "Nhập lại SĐT khách hàng vừa tạo:",
                "Xác nhận khách hàng",
                JOptionPane.QUESTION_MESSAGE
        );

        if (sdt == null || sdt.trim().isEmpty()) {
            return;
        }

        KhachHang khachHang;

        try {
            khachHang = khachHangService.timKhachHangTheoMaHoacSDT(
                    sdt.trim()
            );

        } catch (Exception ex) {
            showError("Không tìm thấy khách hàng:\n" + ex.getMessage());
            return;
        }

        if (khachHang == null) {
            showWarning("Không tìm thấy khách hàng vừa tạo.");
            return;
        }

        // Mở dialog thêm thú cưng
        moDialogThemThuCung(khachHang);
    }
    
    private void hienThiKhachVangLai(
            KhachHang khachHang,
            ThuCung thuCung
    ) {

        lblTenKhach.setText(khachHang.getHoTen());

        lblTenThuCung.setText(thuCung.getTenThuCung());

        // Không có lịch hẹn
        lblMaLich.setText("--");

        lblGioHen.setText("Giờ hẹn: Khách trực tiếp");

        // Lấy cân nặng từ hồ sơ thú cưng
        txtCanNang.setText(
        	    String.valueOf(thuCung.getCanNang())
        	);

        txtGhiChu.setText(
                thuCung.getGhiChu() == null
                        ? ""
                        : thuCung.getGhiChu()
        );

        // Reset dịch vụ
        for (JCheckBox cb : danhSachCheckBoxDichVu) {
            cb.setSelected(false);
        }

        // Reset tình trạng lông
        radBinhThuong.setSelected(true);
        txtTinhTrangKhac.setText("");
        txtTinhTrangKhac.setEnabled(false);

        capNhatGiaVaGioTra();
    }
    
    private void moDialogThemThuCung(KhachHang khachHang) {

        Window owner = SwingUtilities.getWindowAncestor(this);

        ThemThuCungDialog dialog =
                new ThemThuCungDialog(owner, khachHang);

        dialog.setVisible(true);

        // Lấy thú cưng vừa được thêm
        ThuCung thuCung = dialog.getThuCungMoi();

        // Người dùng đóng dialog hoặc hủy
        if (thuCung == null) {
            return;
        }

        // Lưu thông tin khách vãng lai đang tiếp nhận
        khachVangLaiDangChon = khachHang;
        thuCungVangLaiDangChon = thuCung;

        // Chuyển sang chế độ khách vãng lai
        khachVangLai = true;
        lichHenDangChon = null;

        // Hiển thị thông tin trên phiếu
        hienThiKhachVangLai(khachHang, thuCung);
    }
    
    private List<DichVuSpa> layDichVuDuocChon() {
        List<DichVuSpa> ketQua = new ArrayList<>();

        for (int i = 0; i < danhSachCheckBoxDichVu.size(); i++) {
            JCheckBox cb = danhSachCheckBoxDichVu.get(i);

            if (cb.isSelected()) {
                ketQua.add(danhSachDichVu.get(i));
            }
        }

        return ketQua;
    }

    private void capNhatGiaVaGioTra() {
        if (lblGioTra == null) return;

        List<DichVuSpa> selected = layDichVuDuocChon();

        BigDecimal price = BigDecimal.ZERO;
        int minutes = 0;

        // Tính tổng tiền và thời lượng của các dịch vụ được chọn
        for (DichVuSpa dv : selected) {
            if (dv.getDonGiaCoBan() != null) {
                price = price.add(dv.getDonGiaCoBan());
            }

            minutes += layThoiLuongPhut(dv);
        }

        // Tính phụ thu tình trạng lông
        BigDecimal surcharge = tinhPhuThu();

        lblGiaDichVu.setText(
                "Giá dịch vụ (" + selected.size() + "): " + MONEY.format(price)
        );

        lblPhuThu.setText("Phụ thu: " + MONEY.format(surcharge));

        lblTongTien.setText(
                "Tổng dự kiến: " + MONEY.format(price.add(surcharge))
        );

        // Tính giờ trả dự kiến
        LocalDateTime start = LocalDateTime.now();

        if (lichHenDangChon != null && !khachVangLai) {
            start = lichHenDangChon.getThoiGianHen();

            if (start.isBefore(LocalDateTime.now())) {
                start = LocalDateTime.now();
            }
        }

        if (minutes <= 0) {
            lblGioTra.setText("--:--");
        } else {
            lblGioTra.setText(
                    start.plusMinutes(minutes).format(TIME)
                            + " (" + minutes + " phút)"
            );
        }
    }

    // Lấy thời lượng thực tế từ cột thoiLuongPhut trong bảng DichVuSpa.
    private int layThoiLuongPhut(DichVuSpa dv){
        return dv.getThoiLuongPhut();
    }

    private BigDecimal tinhPhuThu() {

        if (radRoiLong.isSelected()) {
            return new BigDecimal("50000");
        }

        if (radVeRan.isSelected()) {
            return new BigDecimal("70000");
        }

        if (radKhac.isSelected()) {
            return new BigDecimal("100000");
        }

        return BigDecimal.ZERO;
    }
    
    private void tuChoiDichVu(){
        if(khachVangLai||lichHenDangChon==null){
            showWarning("Chức năng từ chối khách vãng lai chưa được kết nối lưu phiếu."); return;
        }
        String reason=JOptionPane.showInputDialog(this,"Nhập lý do từ chối dịch vụ:",
                "Từ chối dịch vụ",JOptionPane.WARNING_MESSAGE);
        if(reason==null)return;
        if(reason.trim().isEmpty()){showWarning("Vui lòng nhập lý do từ chối.");return;}
        int confirm=JOptionPane.showConfirmDialog(this,"Xác nhận từ chối dịch vụ?\nLý do: "+reason,
                "Xác nhận từ chối",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(confirm==JOptionPane.YES_OPTION){
            showWarning("Đã ghi nhận lý do trên giao diện:\n"+reason+
                    "\n\nChưa lưu database. Cần bổ sung trạng thái TU_CHOI và DAO xử lý.");
        }
    }

    /** Tạo yêu cầu từ dữ liệu đang có, giao MainFrame mở POS. Không ghi CSDL tại đây. */
    private void taoHoaDon() {
        try {
            if (onLapHoaDon == null) {
                throw new IllegalStateException("Chưa cấu hình điều hướng sang POS.");
            }
            String maKH, maPet, maLich = null;
            if (khachVangLai) {
                if (khachVangLaiDangChon == null || thuCungVangLaiDangChon == null)
                    throw new IllegalArgumentException("Vui lòng thêm khách và thú cưng trước.");
                maKH = khachVangLaiDangChon.getMaKH();
                maPet = thuCungVangLaiDangChon.getMaThuCung();
            } else {
                if (lichHenDangChon == null || lichHenDangChon.getKhachHang() == null
                        || lichHenDangChon.getThuCung() == null)
                    throw new IllegalArgumentException("Vui lòng chọn lịch hẹn hợp lệ.");
                maKH = lichHenDangChon.getKhachHang().getMaKH();
                maPet = lichHenDangChon.getThuCung().getMaThuCung();
                maLich = lichHenDangChon.getMaLichHen();
            }
            BigDecimal canNang = new BigDecimal(txtCanNang.getText().strip().replace(",", "."));
            if (radKhac.isSelected() && txtTinhTrangKhac.getText().isBlank())
                throw new IllegalArgumentException("Vui lòng mô tả tình trạng lông/da.");
            String tinhTrang = radRoiLong.isSelected() ? "Rối lông"
                    : radVeRan.isSelected() ? "Ve, bọ/rận"
                    : radKhac.isSelected() ? "Khác: " + txtTinhTrangKhac.getText().strip()
                    : "Bình thường";
            NhanVien tho = (NhanVien) cboThoSpa.getSelectedItem();
            if (tho == null) throw new IllegalArgumentException("Chưa chọn thợ Spa.");
            List<DichVuSpa> selected = layDichVuDuocChon();
            if (selected.isEmpty()) throw new IllegalArgumentException("Cần chọn ít nhất một dịch vụ.");
            List<TiepNhanSpaContext.DichVuDaChon> dong = new ArrayList<>();
            int phut = 0;
            for (int i = 0; i < selected.size(); i++) {
                DichVuSpa dv = selected.get(i);
                BigDecimal gia = dv.getDonGiaCoBan();
                if (gia == null) throw new IllegalArgumentException("Dịch vụ chưa có đơn giá.");
                // Phụ thu tình trạng lông được cộng vào dịch vụ đầu tiên, không thất thoát khi chuyển POS.
                if (i == 0) gia = gia.add(tinhPhuThu());
                dong.add(new TiepNhanSpaContext.DichVuDaChon(dv.getMaDichVu(), gia));
                phut += dv.getThoiLuongPhut();
            }
            LocalDateTime batDau = LocalDateTime.now();
            if (lichHenDangChon != null && lichHenDangChon.getThoiGianHen().isAfter(batDau))
                batDau = lichHenDangChon.getThoiGianHen();
            TiepNhanSpaContext yeuCau = tiepNhanService.xacNhanDuLieu(
                new TiepNhanSpaContext(maKH, maPet, maLich, tho.getMaNV(), canNang,
                    tinhTrang, txtGhiChu.getText().strip(), batDau,
                    batDau.plusMinutes(phut), dong));
            onLapHoaDon.accept(yeuCau);
        } catch (NumberFormatException ex) {
            showWarning("Cân nặng không hợp lệ.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showWarning(ex.getMessage());
        }
    }

    /**
     * Nạp lịch vừa chọn từ màn hình Đặt lịch hẹn: giữ mã lịch để POS liên kết,
     * điền sẵn khách hàng, thú cưng, dịch vụ, thợ và ghi chú.
     * MainFrame phải gọi SAU hienThiManHinh(), vì taiDuLieu() reset lựa chọn.
     */
    public void napLichHenDaDat(LichHen lichHen) {
        com.titipet.bus.ChuyenLichHenTiepNhanService
                .kiemTraLichChoTiepNhan(lichHen, LocalDate.now());
        chonLichHen(lichHen);
    }

    /** Làm mới các lịch chờ sau khi thanh toán hoặc quay về màn hình tiếp nhận. */
    public void taiDuLieu() {
        lichHenDangChon = null;
        khachVangLai = false;
        khachVangLaiDangChon = null;
        thuCungVangLaiDangChon = null;
        loadDuLieu();
    }

    private JPanel createCard(){
        JPanel p=new JPanel(); p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),new EmptyBorder(5,5,5,5)));
        return p;
    }
    private JPanel infoBox(String title,JLabel value){
        JPanel p=new JPanel(); p.setBackground(LIGHT_BLUE); p.setBorder(new EmptyBorder(5,6,5,6));
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        JLabel label=new JLabel(title); label.setFont(new Font("Segoe UI",Font.PLAIN,8)); label.setForeground(MUTED);
        value.setFont(new Font("Segoe UI",Font.BOLD,10)); value.setForeground(BLUE);
        p.add(label); p.add(Box.createVerticalStrut(2)); p.add(value); return p;
    }
    private JPanel valueBox(JLabel value){
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(new Color(245,247,251));
        p.setBorder(new EmptyBorder(5,7,5,7)); p.add(value); return p;
    }
    private JLabel label(String text){
        JLabel l=new JLabel(text); l.setFont(new Font("Segoe UI",Font.BOLD,10)); l.setForeground(TEXT); return l;
    }
    private JLabel createBadge(String text,Color bg,Color fg){
        JLabel l=new JLabel(text); l.setFont(new Font("Segoe UI",Font.BOLD,9)); l.setForeground(fg);
        l.setOpaque(true); l.setBackground(bg); l.setBorder(new EmptyBorder(5,7,5,7)); return l;
    }
    private JButton button(String text,Color bg,Color fg){
        JButton b=new JButton(text); b.setFont(new Font("Segoe UI",Font.BOLD,9)); b.setForeground(fg);
        b.setBackground(bg); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6,8,6,8)); b.setOpaque(true); b.setContentAreaFilled(true); b.setBorderPainted(false);
        return b;
    }
    private void showWarning(String message){
        JOptionPane.showMessageDialog(this,message,"Thông báo",JOptionPane.WARNING_MESSAGE);
    }
    private void showError(String message){
        JOptionPane.showMessageDialog(this,message,"Lỗi",JOptionPane.ERROR_MESSAGE);
    }
    
    private void hienThiDanhSachDichVu() {
        panelDichVu.removeAll();
        danhSachCheckBoxDichVu.clear();

        // Chỉ lấy dịch vụ đang hoạt động
        List<DichVuSpa> dichVuHoatDong = new ArrayList<>();

        for (DichVuSpa dv : danhSachDichVu) {
            if (dv.isDangHoatDong()) {
                dichVuHoatDong.add(dv);
            }
        }

        // Thay danh sách gốc bằng danh sách đang hoạt động
        danhSachDichVu = dichVuHoatDong;

        for (DichVuSpa dv : danhSachDichVu) {
            BigDecimal gia = dv.getDonGiaCoBan() == null
                    ? BigDecimal.ZERO
                    : dv.getDonGiaCoBan();

            JCheckBox cb = new JCheckBox(
                    dv.getTenDichVu()
                    + " • " + MONEY.format(gia)
                    + " • " + dv.getThoiLuongPhut() + " phút"
            );

            cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            cb.setBackground(Color.WHITE);
            cb.setFocusPainted(false);
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);

            cb.addActionListener(e -> capNhatGiaVaGioTra());

            danhSachCheckBoxDichVu.add(cb);
            panelDichVu.add(cb);
        }

        panelDichVu.revalidate();
        panelDichVu.repaint();
    } 
    
    public void setLichHenDangChon(LichHen lichHen) {
        if (lichHen == null) {
            return;
        }

        chonLichHen(lichHen);
    }
    
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception ignored){}
            JFrame frame=new JFrame("TiTiPet - Tiếp nhận thú cưng");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new TiepNhanSpaPanel());
            frame.setSize(1180,700);
            frame.setMinimumSize(new Dimension(950,600));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
