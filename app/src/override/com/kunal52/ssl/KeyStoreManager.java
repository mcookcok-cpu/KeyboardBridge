package com.kunal52.ssl;

import com.kunal52.AndroidRemoteContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.UUID;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/** Android-safe replacement for the upstream desktop-oriented keystore. */
public final class KeyStoreManager {
    private static final String PASSWORD = "KeyboardBridge-Store-2026";
    private static final String ALIAS = "androidtv-local";
    private static final String REMOTE_ALIAS = "androidtv-remote-%s";
    private final AndroidRemoteContext context = AndroidRemoteContext.getInstance();
    private final KeyStore keyStore;

    public KeyStoreManager() { keyStore = load(); }

    private KeyStore load() {
        try {
            File f = context.getKeyStoreFile();
            File p = f.getParentFile();
            if (p != null && !p.exists()) p.mkdirs();
            KeyStore ks = KeyStore.getInstance("PKCS12");
            if (f.exists() && f.length() > 0) {
                try (FileInputStream in = new FileInputStream(f)) {
                    ks.load(in, PASSWORD.toCharArray());
                } catch (Exception oldStore) {
                    // v0.2 could leave a non-PKCS12 keystore behind. Reset it once.
                    // The TV pairing identity is regenerated and paired again.
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                    ks = KeyStore.getInstance("PKCS12");
                    ks.load(null, PASSWORD.toCharArray());
                }
            } else ks.load(null, PASSWORD.toCharArray());
            if (!ks.containsAlias(ALIAS)) { createIdentity(ks, ALIAS); store(ks); }
            return ks;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create identity KeyStore: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private void createIdentity(KeyStore ks, String alias) throws GeneralSecurityException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        X509Certificate cert = SslUtil.generateX509V3Certificate(pair, "CN=KeyboardBridge/androidtv");
        ks.setKeyEntry(alias, pair.getPrivate(), PASSWORD.toCharArray(), new Certificate[]{cert});
    }

    private void store(KeyStore ks) throws Exception {
        File f = context.getKeyStoreFile();
        File p = f.getParentFile();
        if (p != null && !p.exists()) p.mkdirs();
        try (FileOutputStream out = new FileOutputStream(f)) { ks.store(out, PASSWORD.toCharArray()); }
    }
    public void store() { try { store(keyStore); } catch (Exception e) { throw new IllegalStateException("Unable to store identity KeyStore", e); } }
    public KeyManager[] getKeyManagers() throws GeneralSecurityException {
        KeyManagerFactory f = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        f.init(keyStore, PASSWORD.toCharArray());
        return f.getKeyManagers();
    }
    public TrustManager[] getTrustManagers() {
        return new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers(){ return new X509Certificate[0]; }
            public void checkClientTrusted(X509Certificate[] c,String a){}
            public void checkServerTrusted(X509Certificate[] c,String a){}
        }};
    }
    public boolean hasServerIdentityAlias(){ try{return keyStore.containsAlias(ALIAS);}catch(Exception e){return false;} }
    public void initializeKeyStore(){ initializeKeyStore(UUID.randomUUID().toString()); }
    public void initializeKeyStore(String id){ try { if(keyStore.containsAlias(ALIAS)) keyStore.deleteEntry(ALIAS); createIdentity(keyStore,ALIAS); store(); } catch(Exception e){throw new IllegalStateException("Unable to create identity KeyStore",e);} }
    public Certificate removeCertificate(String id){ try {String a=String.format(REMOTE_ALIAS,id); Certificate c=keyStore.getCertificate(a); if(keyStore.containsAlias(a))keyStore.deleteEntry(a); store(); return c;}catch(Exception e){return null;} }
    public void storeCertificate(Certificate c){storeCertificate(c,Integer.toString(c.hashCode()));}
    public void storeCertificate(Certificate c,String id){try{String a=String.format(REMOTE_ALIAS,id);if(keyStore.containsAlias(a))keyStore.deleteEntry(a);keyStore.setCertificateEntry(a,c);store();}catch(Exception e){throw new IllegalStateException("Unable to store TV certificate",e);}}
    public void clear(){try{Enumeration<String> e=keyStore.aliases();while(e.hasMoreElements())keyStore.deleteEntry(e.nextElement());createIdentity(keyStore,ALIAS);store();}catch(Exception e){throw new IllegalStateException("Unable to reset identity KeyStore",e);}}
}
