package ru.razborka.marketplace.auth.telegram;

import org.junit.jupiter.api.Test;
import ru.razborka.marketplace.common.exception.BusinessException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TelegramLoginVerifierTest {

    private final TelegramLoginVerifier verifier = new TelegramLoginVerifier();

    @Test
    void acceptsValidSignature() throws Exception {
        String botToken = "test-token";
        Map<String, String> data = new HashMap<>();
        data.put("id", "42");
        data.put("first_name", "Test");
        data.put("username", "tester");
        String checkString = "first_name=Test\nid=42\nusername=tester";
        byte[] secretKey = MessageDigest.getInstance("SHA-256").digest(botToken.getBytes(StandardCharsets.UTF_8));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
        byte[] raw = mac.doFinal(checkString.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(raw.length * 2);
        for (byte b : raw) {
            hex.append(String.format("%02x", b));
        }
        data.put("hash", hex.toString());
        Map<String, String> out = verifier.verifyAndParse(botToken, new HashMap<>(data));
        assertEquals("42", out.get("id"));
    }

    @Test
    void rejectsBadSignature() {
        Map<String, String> data = new HashMap<>();
        data.put("id", "1");
        data.put("hash", "deadbeef");
        assertThrows(BusinessException.class, () -> verifier.verifyAndParse("token", data));
    }
}
