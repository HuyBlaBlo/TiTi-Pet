package com.titipet.bus;

import com.titipet.dao.NhanVienDAO;
import com.titipet.entity.NhanVien;
import com.titipet.entity.enums.VaiTro;
import com.titipet.util.PasswordUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class AuthService {
    private final NhanVienDAO nhanVienDAO;
    private NhanVien nhanVienDangNhap;

    public AuthService() {
        this(new NhanVienDAO());
    }

    public AuthService(NhanVienDAO nhanVienDAO) {
        this.nhanVienDAO = Objects.requireNonNull(nhanVienDAO);
    }

    /**
     * Kiểm tra thông tin đăng nhập và trả về nhân viên đăng nhập thành công.
     */
    public NhanVien dangNhap(String tenDangNhap, String matKhau){
        validateInput(tenDangNhap,matKhau);
        NhanVien nhanVien;
        try{
            nhanVien = nhanVienDAO
                    .timTheoTenDangNhap(tenDangNhap.trim())
                    .orElseThrow(AuthService::thongTinDangNhapKhongDung);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kiểm tra thông tin đăng nhập, vui lòng kiểm tra lại kết nối với db");
        }
        //Kiem tra mat khau
        if(PasswordUtil.kiemTraMatKhau(matKhau,nhanVien.getMatKhau())){
            throw thongTinDangNhapKhongDung();
        }

        nhanVien.setMatKhau(null);

        nhanVienDangNhap =  nhanVien;
        return nhanVienDangNhap;
    }

    private void validateInput(String tenDangNhap, String matKhau) {
        if(null == tenDangNhap || tenDangNhap.isBlank()){
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập");
        }
        if(null == matKhau || matKhau.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu!");
        }
    }

    // Đăng xuất
    public void dangXuat(){
        nhanVienDangNhap = null;
    }

    // Lấy nhân viên đang đăng nhập
    public Optional<NhanVien> getNhanVienDangNhap(){
        return Optional.ofNullable(nhanVienDangNhap);
    }
    // Kiểm tra đã đăng nhập chưa
    public boolean daDangNhap(){
        return nhanVienDangNhap!=null ;
    }

    // Kiểm tra vai trò
    public boolean coVaiTro(VaiTro vaiTro) {
        return nhanVienDangNhap != null
                && nhanVienDangNhap.getVaiTro() == vaiTro;
    }

    /**
    private boolean matKhauKhop(String matKhauNhap, String matKhauDaLuu) {
        if (matKhauDaLuu == null) {
            return false;
        }

        return MessageDigest.isEqual(
                matKhauNhap.getBytes(StandardCharsets.UTF_8),
                matKhauDaLuu.getBytes(StandardCharsets.UTF_8)
        );
    }
*/
    private static IllegalArgumentException thongTinDangNhapKhongDung(){
        return new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng");
    }

}
