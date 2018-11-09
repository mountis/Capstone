

package developer.elkane.udacity.wizflow.utils;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


public class Security {


    public static String md5(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.d(Constants.TAG, "Something is gone wrong calculating MD5", e);
        }
        return "";
    }


    public static String encrypt(String value, String password) {
        String encrypedValue = "";
        try {
            DESKeySpec keySpec = new DESKeySpec(password.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            byte[] clearText = value.getBytes("UTF8");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypedValue = Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);
            return encrypedValue;
        } catch (InvalidKeyException | NoSuchPaddingException | InvalidKeySpecException | BadPaddingException |
                IllegalBlockSizeException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
            Log.d(Constants.TAG, "Something is gone wrong encrypting", e);
        }
        return encrypedValue;
    }


    public static String decrypt(String value, String password) {
        String decryptedValue;
        try {
            DESKeySpec keySpec = new DESKeySpec(password.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] encrypedPwdBytes = Base64.decode(value, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));

            decryptedValue = new String(decrypedValueBytes);
        } catch (InvalidKeyException | UnsupportedEncodingException | InvalidKeySpecException |
                NoSuchAlgorithmException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException
                e) {
            Log.e(Constants.TAG, "Error decrypting");
            return value;
        } catch (IllegalArgumentException e) {
            Log.e(Constants.TAG, "Error decrypting: old notes were not encrypted but just masked to users");
            return value;
        }
        return decryptedValue;
    }


}
