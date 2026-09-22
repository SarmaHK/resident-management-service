import java.security.*;
import java.util.Base64;
public class GenKey {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        String b64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
        System.out.println("-----BEGIN PUBLIC KEY-----");
        for (int i = 0; i < b64.length(); i += 64) {
            System.out.println(b64.substring(i, Math.min(b64.length(), i + 64)));
        }
        System.out.println("-----END PUBLIC KEY-----");
    }
}
