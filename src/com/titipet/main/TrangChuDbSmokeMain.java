package com.titipet.main;

import com.titipet.connectDB.ConnectDB;
import com.titipet.dao.TrangChuDAO;
import java.time.LocalDate;

/** Kiểm tra các truy vấn Trang chủ bằng SQL Server thật, không cần đăng nhập giao diện. */
public final class TrangChuDbSmokeMain {
    private TrangChuDbSmokeMain() { }

    public static void main(String[] args) throws Exception {
        ConnectDB db = ConnectDB.getInstance();
        try {
            db.connect();
            System.out.println("Đã kết nối database: " + ConnectDB.getConnection().getCatalog());
            TrangChuDAO dao = new TrangChuDAO();
            LocalDate ngay = LocalDate.now();
            var soLieu = dao.laySoLieu(ngay);
            System.out.println("Ngày " + ngay + ": thú cưng=" + soLieu.tongThuCung()
                    + ", lịch hẹn=" + soLieu.lichHenHomNay()
                    + ", hóa đơn=" + soLieu.hoaDonDaThanhToan()
                    + ", doanh thu=" + soLieu.doanhThuHomNay());
            System.out.println("Lịch hôm nay: " + dao.layLichPhucVu(ngay, 10).size());
            System.out.println("Cảnh báo kho: " + dao.layCanhBaoKho(ngay, 7, 10).size());
            System.out.println("Hóa đơn gần đây: " + dao.layHoaDonGanDay(10).size());
            System.out.println("Ca thu ngân NV002: " + dao.timCaHienTai("NV002"));
            System.out.println("Công việc thợ NV004: " + dao.laySoLieuThoSpa(ngay, "NV004"));
            System.out.println("HOÀN TẤT: tất cả truy vấn Trang chủ thực thi thành công.");
        } finally {
            db.disconnect();
        }
    }
}
