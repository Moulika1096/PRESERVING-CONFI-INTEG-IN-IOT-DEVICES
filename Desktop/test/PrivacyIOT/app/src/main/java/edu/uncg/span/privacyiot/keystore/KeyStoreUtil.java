package edu.uncg.span.privacyiot.keystore;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

public class KeyStoreUtil {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String alias = "keystore_alias";
    private static KeyPair generateKeyPair(Context ctx) throws GeneralSecurityException {
        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(1, Calendar.YEAR);
        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(ctx)
                .setAlias(alias)
                .setSubject(
                        new X500Principal(String.format("CN=%s, OU=%s", alias,
                                ctx.getPackageName())))
                .setSerialNumber(BigInteger.ONE).setStartDate(notBefore.getTime())
                .setEndDate(notAfter.getTime()).build();
        KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEY_STORE);
        kpGenerator.initialize(spec);
        KeyPair kp = kpGenerator.generateKeyPair();
        return kp;
    }
    public static KeyStore.PrivateKeyEntry getKeyEntry(Context ctx) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        boolean foundAlias = false;
        Enumeration<String> aliasList = keyStore.aliases();
        while(aliasList.hasMoreElements()) {
            String aliasEntry = aliasList.nextElement();
            if(alias.equals(aliasEntry)) {
                foundAlias = true;
                break;
            }
        }
        if(!foundAlias) {

            generateKeyPair(ctx);

        }
        KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
        return keyEntry;
    }
    public static byte[] wrapKey(PublicKey publicKey, Key key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return cipher.wrap(key);
    }
    public static Key unWrapKey(PrivateKey privateKey, byte[] wrappedKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.UNWRAP_MODE, privateKey);
        Key unwrappedKey = cipher.unwrap(wrappedKey,"AES",Cipher.SECRET_KEY);
        return unwrappedKey;
    }

}
