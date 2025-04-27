package ciclops.security;

import java.util.Base64;

public record CipherData(byte[] cipherText, byte[] salt, byte[] iv) {
    private static final String VERSION = "v1";

    public static CipherData deserialize(String data) {
        final String[] parts = data.split(":");

        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid encrypted data format");
        }

        final String version = parts[0];
        switch (version) {
            case "v1":
                return deserializeV1(parts);
            default:
                throw new IllegalArgumentException("Unsupported version: " + version);
        }
    }

    private static CipherData deserializeV1(String[] parts) {
        final byte[] cipherText = fromB64(parts[1]);
        final byte[] salt = fromB64(parts[2]);
        final byte[] iv = fromB64(parts[3]);

        return new CipherData(cipherText, salt, iv);
    }

    private static String toB64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] fromB64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    public String serialize() {
        return VERSION + ":" + toB64(cipherText) + ":" + toB64(salt) + ":" + toB64(iv);
    }
}
