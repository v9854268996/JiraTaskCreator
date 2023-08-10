package Tools;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.security.cert.CertificateException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

public class InsecureHttpClientFactory {

    DefaultHttpClient hc;

    public DefaultHttpClient buildHttpClient() throws NoSuchAlgorithmException,
            KeyManagementException,
            KeyStoreException,
            UnrecoverableKeyException {
        hc = new DefaultHttpClient();
        hc.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        //configureProxy();
        configureCookieStore();
        configureSSLHandling();
        return hc;
    }

    private void configureProxy() {
        HttpHost proxy = new HttpHost("proxy.example.org", 3182);
        hc.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    private void configureCookieStore() {
        CookieStore cStore = new BasicCookieStore();
        hc.setCookieStore(cStore);
    }

    private void configureSSLHandling() throws NoSuchAlgorithmException,
            KeyManagementException,
            KeyStoreException,
            UnrecoverableKeyException {
        Scheme http =
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
        SSLSocketFactory sf = buildSSLSocketFactory();
        Scheme https = new Scheme("https", 443, sf);
        SchemeRegistry sr = hc.getConnectionManager().getSchemeRegistry();
        sr.register(http);
        sr.register(https);
    }

    private SSLSocketFactory buildSSLSocketFactory() throws NoSuchAlgorithmException,
            KeyManagementException,
            KeyStoreException,
            UnrecoverableKeyException {
        TrustStrategy ts = new TrustStrategy() {

            public boolean isTrusted(X509Certificate[] x509Certificates,
                                     String s) {
                return true; // heck yea!
            }
        };

        SSLSocketFactory sf = null;

        try {
            /* build socket factory with hostname verification turned off. */
            sf =
                    new SSLSocketFactory(ts, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sf;
    }

}