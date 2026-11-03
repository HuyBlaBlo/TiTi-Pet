package com.titipet.bus;

import com.titipet.dao.BanHangKhachHangDAO;
import com.titipet.entity.KhachHangBanHang;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Quy đổi SQLException thành thông báo nghiệp vụ cho GUI. */
public class BanHangKhachHangService {
    private final BanHangKhachHangDAO dao;
    public BanHangKhachHangService() { this(new BanHangKhachHangDAO()); }
    public BanHangKhachHangService(BanHangKhachHangDAO dao) { this.dao = Objects.requireNonNull(dao); }
    public List<KhachHangBanHang> tim(String tuKhoa) {
        try { return dao.timKhachHang(tuKhoa); }
        catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải danh sách khách hàng từ SQL Server.", ex);
        }
    }
}
