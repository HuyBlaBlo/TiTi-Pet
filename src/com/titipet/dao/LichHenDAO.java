package com.titipet.dao;

import com.titipet.connectDB.ConnectDB;
import com.titipet.entity.DichVuSpa;
import com.titipet.entity.KhachHang;
import com.titipet.entity.LichHen;
import com.titipet.entity.NhanVien;
import com.titipet.entity.ThuCung;
import com.titipet.entity.enums.HangTV;
import com.titipet.entity.enums.LoaiThuCungApDung;
import com.titipet.entity.enums.TrangThaiLichHen;
import com.titipet.entity.enums.VaiTro;
import com.titipet.gui.panel.LichHenPanel;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * DAO xử lý dữ liệu cho màn hình Đặt lịch hẹn Spa.
 * Khớp với cấu trúc bảng dbo.LichHen trong database/TiTiPet.sql hiện tại.
 */

public class LichHenDAO {
	private Connection getActiveConnection() throws SQLException {
        Connection con = ConnectDB.getConnection();
        if (con == null || con.isClosed()) {
            ConnectDB.getInstance().connect();
            con = ConnectDB.getConnection();
        }
        return con;
    }

    // ========================= 1. LẤY DANH SÁCH LỊCH =========================

    private static final String SQL_SELECT_LICH_HEN = """
        SELECT lh.maLichHen, lh.thoiGianHen, lh.trangThai, lh.ghiChu,
               kh.maKH, kh.hoTen, kh.soDienThoai, kh.hangTV, kh.diemTichLuy,
               tc.maThuCung, tc.tenThuCung, tc.loaiThuCung, tc.canNang,
               tc.tinhTrangLong, tc.ghiChu AS ghiChuThuCung,
               dv.maDichVu, dv.tenDichVu, dv.donGiaCoBan, dv.thueSuat,
               dv.giaDaBaoGomThue, dv.loaiThuCungApDung, dv.dangHoatDong,
               nv.maNV, nv.hoTen AS tenThoSpa, nv.vaiTro, nv.tenDangNhap
        FROM dbo.LichHen lh
        JOIN dbo.KhachHang kh ON kh.maKH = lh.maKH
        JOIN dbo.ThuCung tc ON tc.maThuCung = lh.maThuCung AND tc.maKH = lh.maKH
        JOIN dbo.DichVuSpa dv ON dv.maDichVu = lh.maDichVu
        LEFT JOIN dbo.NhanVien nv ON nv.maNV = lh.maNVThoSpa
        """;

    /** Lấy lịch hẹn trong một ngày để hiển thị ở khung "Lịch làm việc của thợ". */
    public List<LichHen> layLichHenTheoNgay(LocalDate ngay) throws SQLException {
        if (ngay == null) throw new IllegalArgumentException("Ngày xem lịch không được để trống.");
        String sql = SQL_SELECT_LICH_HEN + """
             WHERE lh.thoiGianHen >= ? AND lh.thoiGianHen < ?
             ORDER BY lh.thoiGianHen ASC
             """;
        List<LichHen> ds = new ArrayList<>();
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(ngay.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(ngay.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapLichHen(rs));
            }
        }
        return ds;
    }
    
    /** Lấy một lịch hẹn theo mã, dùng khi mở chi tiết hoặc chỉnh sửa lịch. */
    public LichHen timTheoMa(String maLichHen) throws SQLException {
        String sql = SQL_SELECT_LICH_HEN + " WHERE lh.maLichHen = ?";
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapLichHen(rs) : null;
            }
        }
    }
    
    /** Tìm lịch theo tên/SĐT khách hàng hoặc tên thú cưng. */
    public List<LichHen> timKiemLichHen(String tuKhoa, LocalDate ngay) throws SQLException {
        String sql = SQL_SELECT_LICH_HEN + """
             WHERE (? IS NULL OR ? = '' OR kh.hoTen LIKE ? OR kh.soDienThoai LIKE ?
                    OR tc.tenThuCung LIKE ? OR lh.maLichHen LIKE ?)
               AND (? IS NULL OR (lh.thoiGianHen >= ? AND lh.thoiGianHen < ?))
             ORDER BY lh.thoiGianHen DESC
             """;
        String kw = tuKhoa == null ? "" : tuKhoa.trim();
        String pattern = "%" + kw + "%";
        List<LichHen> ds = new ArrayList<>();
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
            ps.setString(1, kw); ps.setString(2, kw);
            ps.setString(3, pattern); ps.setString(4, pattern);
            ps.setString(5, pattern); ps.setString(6, pattern);
            if (ngay == null) {
                ps.setNull(7, Types.DATE); ps.setNull(8, Types.TIMESTAMP); ps.setNull(9, Types.TIMESTAMP);
            } else {
                ps.setDate(7, Date.valueOf(ngay));
                ps.setTimestamp(8, Timestamp.valueOf(ngay.atStartOfDay()));
                ps.setTimestamp(9, Timestamp.valueOf(ngay.plusDays(1).atStartOfDay()));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(mapLichHen(rs));
            }
        }
        return ds;
    }
    
    // ========================= 2. THÊM / ĐỔI LỊCH =========================

    /**
     * Thêm lịch mới. Tự sinh mã LH nếu Entity chưa có mã.
     * Chặn trùng giờ của cùng thợ (hiện schema chưa có thời lượng dịch vụ,
     * nên kiểm tra trùng thời điểm bắt đầu chính xác).
     */
    public boolean themLichHen(LichHen lichHen) throws SQLException {
        validateLichHen(lichHen);
        Connection con = getActiveConnection();
        boolean oldAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            if (lichHen.getMaLichHen() == null || lichHen.getMaLichHen().isBlank()) {
                lichHen.setMaLichHen("LH" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
            }
            if (biTrungLich(con, lichHen.getThoSpa().getMaNV(), lichHen.getThoiGianHen(), null)) {
                con.rollback();
                return false;
            }
            String sql = """
            	    INSERT INTO dbo.LichHen
            	        (maLichHen, thoiGianHen, trangThai, maKH, maThuCung,
            	         maDichVu, maNVThoSpa, ghiChu, soLanDoiLich)
            	    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
            	    """;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, lichHen.getMaLichHen());
                    ps.setTimestamp(2, Timestamp.valueOf(lichHen.getThoiGianHen()));
                    ps.setString(3, "CHO_TIEP_NHAN");
                    ps.setString(4, lichHen.getKhachHang().getMaKH());
                    ps.setString(5, lichHen.getThuCung().getMaThuCung());
                    ps.setString(6, lichHen.getDichVu().getMaDichVu());
                    ps.setString(7, lichHen.getThoSpa().getMaNV());
                    ps.setString(8, lichHen.getGhiChu());
                    boolean ok = ps.executeUpdate() == 1;
                    con.commit();
                    return ok;
                }
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(oldAutoCommit);
            }
        }
    /** Đổi ngày/giờ hoặc thợ phụ trách của lịch đã tồn tại. */
    public boolean doiLichHen(String maLichHen, LocalDateTime thoiGianMoi,
            String maThoMoi, String ghiChu) throws SQLException {
    	if (maLichHen == null || maLichHen.isBlank()
                || thoiGianMoi == null
                || maThoMoi == null || maThoMoi.isBlank()) {
            throw new IllegalArgumentException(
                    "Thiếu mã lịch, thời gian hoặc thợ Spa."
            );
        }
    	
    	if (thoiGianMoi.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Ngày giờ hẹn mới không được nằm trong quá khứ."
            );
        }

        Connection con = getActiveConnection();
        boolean oldAutoCommit = con.getAutoCommit();
        
        try {
            con.setAutoCommit(false);

            // Lấy giờ hẹn cũ và số lần đã dời
            String sqlKiemTra = """
                SELECT thoiGianHen, soLanDoiLich
                FROM dbo.LichHen WITH (UPDLOCK, HOLDLOCK)
                WHERE maLichHen = ? AND trangThai <> 'HUY'
                """;

            LocalDateTime thoiGianHenCu;
            int soLanDoi;

            try (PreparedStatement ps = con.prepareStatement(sqlKiemTra)) {
                ps.setString(1, maLichHen);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }

                    thoiGianHenCu = rs.getTimestamp(
                            "thoiGianHen"
                    ).toLocalDateTime();

                    soLanDoi = rs.getInt("soLanDoiLich");
                }
            }

            // Chỉ được dời lịch một lần
            if (soLanDoi >= 1) {
                throw new IllegalArgumentException(
                        "Lịch hẹn này đã được dời trước đó. "
                        + "Không thể dời lần thứ hai."
                );
            }

            // Không được dời khi còn 2 tiếng hoặc ít hơn
            if (!LocalDateTime.now().isBefore(
                    thoiGianHenCu.minusHours(2))) {

                throw new IllegalArgumentException(
                        "Không thể dời lịch khi còn 2 tiếng "
                        + "hoặc ít hơn trước giờ hẹn."
                );
            }

            // Kiểm tra trùng lịch của thợ
            if (biTrungLich(
                    con, maThoMoi, thoiGianMoi, maLichHen)) {
                con.rollback();
                return false;
            }

         // Cập nhật lịch và tăng số lần dời
            String sql = """
                UPDATE dbo.LichHen
                SET thoiGianHen = ?,
                    maNVThoSpa = ?,
                    ghiChu = ?,
                    soLanDoiLich = soLanDoiLich + 1
                WHERE maLichHen = ?
                  AND trangThai <> 'HUY'
                  AND soLanDoiLich = 0
                """;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(thoiGianMoi));
                ps.setString(2, maThoMoi);
                ps.setString(3, ghiChu);
                ps.setString(4, maLichHen);

                boolean ok = ps.executeUpdate() == 1;

                if (ok) {
                    con.commit();
                } else {
                    con.rollback();
                }

                return ok;
            }

        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;

        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }
    // ========================= 3. TRẠNG THÁI: TIẾP NHẬN / HỦY =========================

    /** Chỉ dùng trạng thái mà CHECK constraint trong database hiện tại cho phép. */
    public boolean capNhatTrangThai(String maLichHen, TrangThaiLichHen trangThai) throws SQLException {
        if (maLichHen == null || maLichHen.isBlank() || trangThai == null) {
            throw new IllegalArgumentException("Mã lịch và trạng thái không được để trống.");
        }
        final String dbStatus = switch (trangThai) {
            case CHO_TIEP_NHAN -> "CHO_TIEP_NHAN";
            case DA_TIEP_NHAN -> "DA_TIEP_NHAN";
            case DA_HUY -> "HUY";
            default -> throw new IllegalArgumentException(
                    "Database hiện tại chưa hỗ trợ trạng thái " + trangThai + ".");
        };
        String sql = "UPDATE dbo.LichHen SET trangThai = ? WHERE maLichHen = ?";
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
            ps.setString(1, dbStatus);
            ps.setString(2, maLichHen);
            return ps.executeUpdate() == 1;
        }
    }
    public boolean tiepNhanLichHen(String maLichHen) throws SQLException {
        return capNhatTrangThai(maLichHen, TrangThaiLichHen.DA_TIEP_NHAN);
    }

    public boolean huyLichHen(String maLichHen) throws SQLException {
        return capNhatTrangThai(maLichHen, TrangThaiLichHen.DA_HUY);
    }

    // ========================= 4. DỮ LIỆU ĐỔ COMBOBOX =========================

    /** Danh sách thú cưng của khách đang chọn trên form. */
    public List<ThuCung> layThuCungTheoKhachHang(String maKH) throws SQLException {
        String sql = """
            SELECT tc.maThuCung, tc.tenThuCung, tc.loaiThuCung, tc.canNang,
                   tc.tinhTrangLong, tc.ghiChu,
                   kh.maKH, kh.hoTen, kh.soDienThoai, kh.hangTV, kh.diemTichLuy
            FROM dbo.ThuCung tc JOIN dbo.KhachHang kh ON kh.maKH = tc.maKH
            WHERE tc.maKH = ? ORDER BY tc.tenThuCung
            """;
        List<ThuCung> ds = new ArrayList<>();
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql)) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachHang kh = mapKhachHang(rs);
                    ds.add(new ThuCung(rs.getString("maThuCung"), rs.getString("tenThuCung"),
                            rs.getString("loaiThuCung"), rs.getDouble("canNang"),
                            rs.getString("tinhTrangLong"), rs.getString("ghiChu"), kh));
                }
            }
        }
        return ds;
    }

    /** Danh sách thợ Spa đang hoạt động để đổ vào combo "Thợ Spa phụ trách". */
    public List<NhanVien> layDanhSachThoSpa() throws SQLException {
        String sql = """
            SELECT maNV, tenDangNhap, hoTen, vaiTro
            FROM dbo.NhanVien
            WHERE dangHoatDong = 1 AND vaiTro = 'THO_SPA'
            ORDER BY hoTen
            """;
        List<NhanVien> ds = new ArrayList<>();
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new NhanVien(rs.getString("maNV"), null, rs.getString("hoTen"),
                        VaiTro.valueOf(rs.getString("vaiTro")), rs.getString("tenDangNhap")));
            }
        }
        return ds;
    }
    /** Danh sách dịch vụ Spa đang hoạt động để đổ vào combo dịch vụ. */
    public List<DichVuSpa> layDanhSachDichVuSpa() throws SQLException {
        String sql = """
            SELECT maDichVu, tenDichVu, donGiaCoBan, thueSuat,
                   giaDaBaoGomThue, loaiThuCungApDung, dangHoatDong
            FROM dbo.DichVuSpa WHERE dangHoatDong = 1 ORDER BY tenDichVu
            """;
        List<DichVuSpa> ds = new ArrayList<>();
        try (PreparedStatement ps = getActiveConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String loai = rs.getString("loaiThuCungApDung");
                LoaiThuCungApDung loaiEnum = null;
                // Enum hiện tại của project chưa có CA_HAI; tránh làm hỏng lúc đọc dữ liệu.
                try { loaiEnum = LoaiThuCungApDung.valueOf(loai); }
                catch (IllegalArgumentException ignored) { /* bổ sung CA_HAI vào enum nếu cần */ }
                ds.add(new DichVuSpa(rs.getString("maDichVu"), rs.getString("tenDichVu"),
                        rs.getBigDecimal("donGiaCoBan"), rs.getBigDecimal("thueSuat"),
                        rs.getBoolean("giaDaBaoGomThue"), loaiEnum, rs.getBoolean("dangHoatDong")));
            }
        }
        return ds;
    }
    
 // ========================= 5. HÀM HỖ TRỢ =========================

    private boolean biTrungLich(Connection con, String maTho, LocalDateTime thoiGian,
                                String maLichBoQua) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM dbo.LichHen WITH (UPDLOCK, HOLDLOCK)
            WHERE maNVThoSpa = ? AND thoiGianHen = ? AND trangThai <> 'HUY'
              AND (? IS NULL OR maLichHen <> ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTho);
            ps.setTimestamp(2, Timestamp.valueOf(thoiGian));
            ps.setString(3, maLichBoQua);
            ps.setString(4, maLichBoQua);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    private void validateLichHen(LichHen lh) {
        if (lh == null || lh.getThoiGianHen() == null || lh.getKhachHang() == null
                || lh.getThuCung() == null || lh.getDichVu() == null || lh.getThoSpa() == null
                || lh.getKhachHang().getMaKH() == null || lh.getThuCung().getMaThuCung() == null
                || lh.getDichVu().getMaDichVu() == null || lh.getThoSpa().getMaNV() == null) {
            throw new IllegalArgumentException("Vui lòng nhập đủ khách hàng, thú cưng, dịch vụ, ngày giờ và thợ Spa.");
        }
    }

    private LichHen mapLichHen(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang(rs.getString("maKH"), rs.getString("hoTen"),
                rs.getString("soDienThoai"), HangTV.valueOf(rs.getString("hangTV")),
                rs.getInt("diemTichLuy"));
        ThuCung tc = new ThuCung(rs.getString("maThuCung"), rs.getString("tenThuCung"),
                rs.getString("loaiThuCung"), rs.getDouble("canNang"),
                rs.getString("tinhTrangLong"), rs.getString("ghiChuThuCung"), kh);
        String loai = rs.getString("loaiThuCungApDung");
        LoaiThuCungApDung loaiEnum = null;
        try { loaiEnum = LoaiThuCungApDung.valueOf(loai); }
        catch (IllegalArgumentException ignored) { /* CA_HAI chưa có trong enum hiện tại */ }
        DichVuSpa dv = new DichVuSpa(rs.getString("maDichVu"), rs.getString("tenDichVu"),
                rs.getBigDecimal("donGiaCoBan"), rs.getBigDecimal("thueSuat"),
                rs.getBoolean("giaDaBaoGomThue"), loaiEnum, rs.getBoolean("dangHoatDong"));
        NhanVien tho = null;
        String maNV = rs.getString("maNV");
        if (maNV != null) {
            tho = new NhanVien(maNV, null, rs.getString("tenThoSpa"),
                    VaiTro.valueOf(rs.getString("vaiTro")), rs.getString("tenDangNhap"));
        }
        String trangThaiDB = rs.getString("trangThai");
        TrangThaiLichHen trangThai = switch (trangThaiDB) {
            case "HUY" -> TrangThaiLichHen.DA_HUY;
            case "DA_TIEP_NHAN" -> TrangThaiLichHen.DA_TIEP_NHAN;
            default -> TrangThaiLichHen.CHO_TIEP_NHAN;
        };
        Timestamp timestamp = rs.getTimestamp("thoiGianHen");
        return new LichHen(rs.getString("maLichHen"), timestamp.toLocalDateTime(),
                trangThai, kh, dv, rs.getString("ghiChu"), tc, tho);
    }

    private KhachHang mapKhachHang(ResultSet rs) throws SQLException {
        return new KhachHang(rs.getString("maKH"), rs.getString("hoTen"),
                rs.getString("soDienThoai"), HangTV.valueOf(rs.getString("hangTV")),
                rs.getInt("diemTichLuy"));
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            JFrame frame = new JFrame("TiTiPet - Đặt lịch hẹn Spa");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new LichHenPanel());
            frame.setSize(1280, 760);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
