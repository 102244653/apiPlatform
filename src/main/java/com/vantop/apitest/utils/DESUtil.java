package com.vantop.apitest.utils;

import com.vantop.apitest.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.util.Base64;

@Slf4j
public class DESUtil {

    /**
     * 加密的key
     * */
    private final static String key = "vanTop-test";

    /**
     * 偏移变量，固定占8位字节
     */
    private final static String IV_PARAMETER = "12345678";
    /**
     * 密钥算法
     */
    private static final String ALGORITHM = "DES";
    /**
     * 加密/解密算法-工作模式-填充模式
     */
    private static final String CIPHER_ALGORITHM = "DES/CBC/PKCS5Padding";
    /**
     * 默认编码
     */
    private static final String CHARSET = "utf-8";

    /**
     * 生成key
     *
     * @param password
     * @return
     * @throws Exception
     */
    private static Key generateKey(String password) throws Exception {
        DESKeySpec dks = new DESKeySpec(password.getBytes(CHARSET));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        return keyFactory.generateSecret(dks);
    }


    /**
     * DES加密字符串
     *
     * @param password 加密密码，长度不能够小于8位
     * @param data 待加密字符串
     * @return 加密后内容
     */
    public static String encrypt( String data) {
//        if (password== null || password.length() < 8) {
//            throw new RuntimeException("加密失败，key不能小于8位");
//        }
        try{
            Key secretKey = generateKey(key);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETER.getBytes(CHARSET));
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] bytes = cipher.doFinal(data.getBytes(CHARSET));

            //JDK1.8及以上可直接使用Base64，JDK1.7及以下可以使用BASE64Encoder
            //Android平台可以使用android.util.Base64
            return new String(Base64.getEncoder().encode(bytes));

        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    /**
     * DES解密字符串
     *
     * @param password 解密密码，长度不能够小于8位
     * @param data 待解密字符串
     * @return 解密后内容
     */
    public static String decrypt(String data) {
//        if (password== null || password.length() < 8) {
//            throw new RuntimeException("加密失败，key不能小于8位");
//        }
        try{
            Key secretKey = generateKey(key);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETER.getBytes(CHARSET));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes(CHARSET))), CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    public String encryptPassword(String password){
        return DESUtil.encrypt(password);
    }

    public String createToken(User user){
        String info = user.getUid()+";"+user.getLoginTime();
        return DESUtil.encrypt(info);
    }

    /**
     * 获取精确到秒的时间戳
     */
    public String getTimeStamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    //判断时间戳是否在一定时间内
    public boolean compareTime(String timestamp){
        boolean bl=false;
        if(StringUtils.isEmpty(timestamp)){
            long logintime = Long.parseLong(timestamp);
            long nowtime = Long.parseLong(this.getTimeStamp());
            Long result = (nowtime - logintime) / (1000 * 60 * 60 * 24);
            if(result<=24*7){
                bl=true;
            }
        }
        return bl;
    }

    public static void main(String[] args) {
        String[] s = "1617182400627;1617243954;vanTop-token".split(";");
        for(String a:s){
            System.out.println(a);
        }
    }

}
