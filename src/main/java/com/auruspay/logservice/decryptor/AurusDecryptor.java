package com.auruspay.logservice.decryptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class AurusDecryptor {

    private static final Logger log = LoggerFactory.getLogger(AurusDecryptor.class);

    private static final String HEX_KEY = "A309BB49B764D95BD17666F0709C2881";

    public static String decryptor(String encryptedInput) {

        if (encryptedInput == null || encryptedInput.isBlank()) {
            log.warn("Empty encrypted input received");
            return "";
        }

        try {
            // 🔥 Clean input
            String cleanInput = encryptedInput.replaceAll("\\s", "");

            String decrypted = decrypt(cleanInput, HEX_KEY);

            return decodeHtml(decrypted);

        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            return "DECRYPTION_ERROR";
        }
    }

    private static String decrypt(String base64Content, String hexKey) throws Exception {

        byte[] keyBytes = new byte[16];

        for (int i = 0; i < 32; i += 2) {
            keyBytes[i / 2] = (byte) (
                    (Character.digit(hexKey.charAt(i), 16) << 4)
                            + Character.digit(hexKey.charAt(i + 1), 16)
            );
        }

        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decodedBuffer = Base64.getDecoder().decode(base64Content);
        byte[] decryptedBuffer = cipher.doFinal(decodedBuffer);

        return new String(decryptedBuffer, StandardCharsets.UTF_8);
    }

    // 🔥 HTML decoding clean method
    private static String decodeHtml(String input) {
        return input.replace("&gt;", ">")
                .replace("&lt;", "<")
                .replace("&amp;", "&")
                .replace("&#37;", "%")
                .replace("&apos;", "'")
                .replace("&quot;", "\"");
    }
}