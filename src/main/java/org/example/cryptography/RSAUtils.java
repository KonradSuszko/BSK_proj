package org.example.cryptography;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Getter
@UtilityClass
public class RSAUtils {
    public static @NotNull KeyPair initialize(String privateKeyPath, String publicKeyPath, String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, IOException {
        if (areFilesCreated(privateKeyPath, publicKeyPath)) {
            //read from file
            Pair<byte[], byte[]> privatePair = readFromFile(privateKeyPath);
            Pair<byte[], byte[]> publicPair = readFromFile(publicKeyPath);

            //decrypt
            byte[] ivArr = privatePair.getLeft();
            IvParameterSpec iv = new IvParameterSpec(ivArr);
            SecretKey key = GeneratorOfKeys.getKeyFromPassword(password, "2137");

            byte[] privateKey = decrypt(privatePair.getRight(), key, iv);
            byte[] publicKey = decrypt(publicPair.getRight(), key, iv);

            //initialize keys
            PKCS8EncodedKeySpec pkcs = new PKCS8EncodedKeySpec(privateKey);
            X509EncodedKeySpec x509 = new X509EncodedKeySpec(publicKey);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey prv = kf.generatePrivate(pkcs);
            PublicKey pub = kf.generatePublic(x509);

            return new KeyPair(pub, prv);
        } else {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            KeyPair kp = kpg.generateKeyPair();
            PrivateKey prv = kp.getPrivate();
            PublicKey pub = kp.getPublic();

            byte[] ivArr = GeneratorOfKeys.generateIv();
            IvParameterSpec iv = new IvParameterSpec(ivArr);
            SecretKey key = GeneratorOfKeys.getKeyFromPassword(password, "2137");

            byte[] encryptedPrivateKey = encrypt(prv.getEncoded(), key, iv);
            byte[] encryptedPublicKey = encrypt(pub.getEncoded(), key, iv);

            saveToFile(Pair.of(ivArr, encryptedPrivateKey), privateKeyPath);
            saveToFile(Pair.of(ivArr, encryptedPublicKey), publicKeyPath);

            return kp;
        }
    }

    private static boolean areFilesCreated(String privateKeyPath, String publicKeyPath) {
        File f1 = new File(privateKeyPath);
        File f2 = new File(publicKeyPath);

        return f1.exists() && f2.exists() && !f1.isDirectory() && !f2.isDirectory();
    }

    private static byte[] encrypt(byte[] content, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(content);
        return Base64.getEncoder().encode(cipherText);
    }

    private static byte[] decrypt(byte[] content, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(Base64.getDecoder().decode(content));
    }

    private static void saveToFile(@NotNull Pair<byte[], byte[]> pair, String path) {
        byte[] content = pair.getRight();
        byte[] iv = pair.getLeft();
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path))) {
            out.write(iv);
            out.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static @NotNull Pair<byte[], byte[]> readFromFile(String path) throws IOException {
        try (FileInputStream input = new FileInputStream(path)) {
            byte[] iv = new byte[16];
            input.read(iv);
            byte[] content = input.readAllBytes();
            return Pair.of(iv, content);
        }
    }
}
