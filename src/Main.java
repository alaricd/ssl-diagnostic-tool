import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.Arrays;

public class Main {

    public static void main(final String[] args) {
        var urlString = "https://www.pengdows.com";

        if (args.length > 0) {
            urlString = args[0];
        }

        if (args.length > 1) {
            System.setProperty("javax.net.debug", "ssl,handshake,certpath");
        }

        try {
            final var url = URI.create(urlString).toURL();
            final var connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setSSLSocketFactory(createSslSocketFactory());
            connection.connect();

            explainCertificateChain(connection.getServerCertificates(), false);
            System.out.println("SSL certificate is valid!");
        } catch (final UnknownHostException e) {
            System.out.println("Unable to find host, check DNS, network, or VPN.");
        } catch (final SSLHandshakeException e) {
            System.out.println("SSL handshake failed: " + e.getMessage());
            diagnoseCertificateChain(urlString);
        } catch (final Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void diagnoseCertificateChain(final String urlString) {
        System.out.println("[INFO] Attempting to manually extract certificate chain for diagnostic purposes.");
        try {
            final var sslContext = SSLContext.getInstance("TLS");
            final var savingTrustManager = new SavingTrustManager();

            sslContext.init(null, new TrustManager[]{savingTrustManager}, new SecureRandom());
            final var socketFactory = sslContext.getSocketFactory();

            try (var socket = (SSLSocket) socketFactory.createSocket(URI.create(urlString).getHost(), 443)) {
                socket.startHandshake();
            } catch (Exception ignored) {
                // Handshake will fail, but we only care about the cert chain
            }

            if (savingTrustManager.chain != null) {
                explainCertificateChain(savingTrustManager.chain, true);
            } else {
                System.out.println("[ERROR] Could not capture certificate chain.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Manual certificate chain retrieval failed: " + e.getMessage());
        }
    }

    private static SSLSocketFactory createSslSocketFactory() throws Exception {
        final var sslContext = SSLContext.getInstance("TLS");
        final var combinedKeyStore = loadCombinedKeyStore();

        final var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(combinedKeyStore);

        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
        return sslContext.getSocketFactory();
    }

    private static KeyStore loadCombinedKeyStore() throws Exception {
        final var combinedKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        combinedKeyStore.load(null, null);

        loadTrustStore(combinedKeyStore, getDefaultJavaTrustStore(), System.getProperty("javax.net.ssl.trustStorePassword", "changeit"));
        loadInternalTrustStoreIfPresent(combinedKeyStore);

        return combinedKeyStore;
    }

    private static void loadTrustStore(final KeyStore combinedKeyStore, final String trustStorePath, final String password) throws Exception {
        final var trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (final var stream = new FileInputStream(trustStorePath)) {
            trustStore.load(stream, password.toCharArray());
        }

        final var aliases = trustStore.aliases();
        while (aliases.hasMoreElements()) {
            final var alias = aliases.nextElement();
            combinedKeyStore.setCertificateEntry(alias, trustStore.getCertificate(alias));
        }
    }

    private static void loadInternalTrustStoreIfPresent(final KeyStore combinedKeyStore) throws Exception {
        final var internalTrustStorePath = System.getProperty("internal.truststore.path", System.getenv("TRUSTSTORE_PATH"));
        final var internalTrustStorePassword = System.getProperty("internal.truststore.password", System.getenv("TRUSTSTORE_PASSWORD"));

        if (internalTrustStorePath != null && internalTrustStorePassword != null) {
            loadTrustStore(combinedKeyStore, internalTrustStorePath, internalTrustStorePassword);
            System.out.println("[INFO] Internal trust store loaded from: " + internalTrustStorePath);
        } else {
            System.out.println("[INFO] No internal trust store provided (using system truststore only).");
        }

        System.out.println("[INFO] Using system trust store from: " + getDefaultJavaTrustStore());
    }

    private static void explainCertificateChain(final Certificate[] certChain, final boolean strictRootCheck) {
        System.out.println("\n--- Certificate Chain Received ---\n");

        final var x509Chain = Arrays.stream(certChain)
                .map(cert -> (X509Certificate) cert)
                .toArray(X509Certificate[]::new);

        for (var i = 0; i < x509Chain.length; i++) {
            final var cert = x509Chain[i];
            final var isRoot = cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal());

            System.out.println("Certificate " + (i + 1) + ":");
            System.out.println("  Subject: " + cert.getSubjectX500Principal());
            System.out.println("  Issuer: " + cert.getIssuerX500Principal());
            System.out.println("  Valid From: " + cert.getNotBefore());
            System.out.println("  Valid To: " + cert.getNotAfter());
            System.out.println("  Signature Algorithm: " + cert.getSigAlgName());

            if (i == 0) {
                System.out.println("  [INFO] This is the endpoint certificate.");
            } else if (isRoot) {
                System.out.println("  [INFO] This is the root certificate.");
            } else {
                System.out.println("  [INFO] This is an intermediate certificate.");
            }
        }

        System.out.println("\n--- End of Certificate Chain ---");
    }

    private static String getDefaultJavaTrustStore() {
        return (System.getProperty("java.home") + "/lib/security/cacerts");
    }

    static class SavingTrustManager implements X509TrustManager {
        X509Certificate[] chain;

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            this.chain = chain;
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
            this.chain = chain;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
