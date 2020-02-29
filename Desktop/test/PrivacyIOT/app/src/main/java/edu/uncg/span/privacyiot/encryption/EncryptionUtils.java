package edu.uncg.span.privacyiot.encryption;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.StringTokenizer;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {

    public static byte[] decrypt(byte[] secrectKeyBytes,String receivedMsg) {
        try
        {
            StringTokenizer tokenizer = new StringTokenizer(receivedMsg);

            byte[] received_ciphertext = Base64.decode(tokenizer.nextToken(":"),Base64.DEFAULT);
            byte[] received_tag = Base64.decode(tokenizer.nextToken(":"),Base64.DEFAULT);
            byte[] received_nonce = Base64.decode(tokenizer.nextToken(":"),Base64.DEFAULT);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128,received_nonce);
            SecretKey secretKey = new SecretKeySpec(secrectKeyBytes,0,secrectKeyBytes.length,"AES");
            cipher.init(Cipher.DECRYPT_MODE,secretKey,gcmParameterSpec);
            byte[] decryptedData1 = cipher.update(received_ciphertext);
            byte[] decryptedData2 = cipher.doFinal(received_tag);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if(decryptedData1 != null)
                baos.write(decryptedData1);
            if(decryptedData2 != null)
                baos.write(decryptedData2);
            return baos.toByteArray();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
