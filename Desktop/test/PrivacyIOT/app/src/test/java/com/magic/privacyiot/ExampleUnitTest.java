package edu.uncg.span.privacyiot;

import edu.uncg.span.privacyiot.encryption.EncryptionUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        String key = "Mw6dmjllnKukeKkg1jEFTw==";
        String line = "hello";
        String de = EncryptionUtils.decrypt(key,line);
        System.out.println(de);
        assertEquals(4, 2 + 2);
    }
}