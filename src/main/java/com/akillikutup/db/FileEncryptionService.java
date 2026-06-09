package com.akillikutup.db;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collections;

public class FileEncryptionService {
    private static final String ANAHTAR_DOSYASI = "data" + File.separator + "secret.key";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    private static SecretKey gizliAnahtar;
    
    public static synchronized void init() {
        if (gizliAnahtar != null) return;
        
        File anahtarDosya = new File(ANAHTAR_DOSYASI);
        try {
            if (!anahtarDosya.getParentFile().exists()) {
                anahtarDosya.getParentFile().mkdirs();
            }
            if (anahtarDosya.exists()) {
                byte[] anahtarBytes = Files.readAllBytes(anahtarDosya.toPath());
                gizliAnahtar = new SecretKeySpec(anahtarBytes, "AES");
            } else {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(256);
                gizliAnahtar = keyGen.generateKey();
                Files.write(anahtarDosya.toPath(), gizliAnahtar.getEncoded());
                dosyaErisiminiKisila(anahtarDosya.toPath());
                System.out.println("BILGI: Yeni AES-256 anahtari olusturuldu ve kaydedildi.");
            }
        } catch (Exception e) {
            throw new RuntimeException("AES anahtari yuklenirken veya uretilirken hata olustu: " + e.getMessage(), e);
        }
    }

    public static String encrypt(String value) {
        if (gizliAnahtar == null) init();
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, gizliAnahtar, parameterSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Veri sifrelenirken hata: " + e.getMessage(), e);
        }
    }

    public static String decrypt(String encryptedValue) {
        if (gizliAnahtar == null) init();
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedValue);
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, gizliAnahtar, parameterSpec);
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Veri deşifre edilirken hata: " + e.getMessage(), e);
        }
    }

    public static void dosyaErisiminiKisila(Path yol) {
        if (!Files.exists(yol)) return;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                AclFileAttributeView aclView = Files.getFileAttributeView(yol, AclFileAttributeView.class);
                if (aclView != null) {
                    UserPrincipal owner = Files.getOwner(yol);
                    AclEntry entry = AclEntry.newBuilder()
                            .setType(AclEntryType.ALLOW)
                            .setPrincipal(owner)
                            .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA, 
                                            AclEntryPermission.APPEND_DATA, AclEntryPermission.READ_NAMED_ATTRS,
                                            AclEntryPermission.WRITE_NAMED_ATTRS, AclEntryPermission.EXECUTE,
                                            AclEntryPermission.READ_ATTRIBUTES, AclEntryPermission.WRITE_ATTRIBUTES,
                                            AclEntryPermission.DELETE, AclEntryPermission.READ_ACL, AclEntryPermission.SYNCHRONIZE)
                            .build();
                    aclView.setAcl(Collections.singletonList(entry));
                }
            } else {
                Files.setPosixFilePermissions(yol, PosixFilePermissions.fromString("rwx------"));
            }
        } catch (UnsupportedOperationException e) {
            
        } catch (Exception e) {
            System.err.println("UYARI: Dosya erisimi kisitlanamadi (" + yol.toString() + "): " + e.getMessage());
        }
    }
}
