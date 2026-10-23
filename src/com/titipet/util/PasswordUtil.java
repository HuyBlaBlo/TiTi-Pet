package com.titipet.util;

import at.favre.lib.crypto.bcrypt.BCrypt;


public class PasswordUtil {
    /**
     * Xử lí băm mật khẩu trước khi lưu vào database
     */
    public static String bamMatKhau(String matKhau){
        if(null == matKhau || matKhau.isBlank()){
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return BCrypt.withDefaults().hashToString(12,matKhau.toCharArray());
    }

    /**
     *  Kiểm tra mật khẩu khi đăng nhập
     */
    public static boolean kiemTraMatKhau(String matKhauNhap, String matKhauDaBam){
        if(null == matKhauNhap || null == matKhauDaBam ){
            return false;
        }

        try{
            return BCrypt.verifyer().verify(matKhauNhap.toCharArray(),matKhauDaBam).verified;
        } catch (IllegalArgumentException e) {
            return false;
        }

    }

}
