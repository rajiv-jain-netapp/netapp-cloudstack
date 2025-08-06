package org.apache.cloudstack.storage.util;

import com.cloud.utils.exception.CloudRuntimeException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class NetAppUtil {

    private static final Logger s_logger = Logger.getLogger(NetAppUtil.class);
    private final long connTimeout = 5000;
    private final boolean skipTlsValidation = false;
    static final ObjectMapper mapper = new ObjectMapper();
    private CloseableHttpClient client;
    public <T> T POST(String path, Object input, final TypeReference<T> type) {
        CloseableHttpResponse response = null;
        String managementIp = "<mgmt-ip>";
        try {
            HttpPost request = new HttpPost("https://10.196.64.59/api/storage/volumes");
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");
            //request.addHeader("X-auth-token", accessToken);
            request.addHeader("username", "admin");
            request.addHeader("password", "netapp1!");

            if (input != null) {
                try {
                    String data = mapper.writeValueAsString(input);
                    request.setEntity(new StringEntity(data));
                } catch (IOException e) {
                    throw new RuntimeException("Error processing request payload", e);
                }
            }

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                response = client.execute(request);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200 || statusCode == 201) {
                    if (type != null) {
                        if (type.getType().getTypeName().equals(String.class.getName())) {
                            return (T) response.getFirstHeader("Location").getValue();
                        } else {
                            HttpEntity entity = response.getEntity();
                            return mapper.readValue(entity.getContent(), type);
                        }
                    }
                } else if (statusCode == 400) {
                    Map<String, Object> payload = mapper.readValue(response.getEntity().getContent(), new TypeReference<Map<String, Object>>() {});
                    throw new RuntimeException("Invalid request error 400: " + payload);
                } else if (statusCode == 401 || statusCode == 403) {
                    throw new RuntimeException("Authentication or Authorization failed");
                } else {
                    Map<String, Object> payload = mapper.readValue(response.getEntity().getContent(), new TypeReference<Map<String, Object>>() {});
                    throw new RuntimeException("Invalid request error " + statusCode + ": " + payload);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error sending request", e);
            }
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    System.out.println("Error closing response");
                }
            }
        }

        return null;
    }

    public CloseableHttpClient getClient() {
        if (client == null) {
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout((int) connTimeout)
                    .setConnectionRequestTimeout((int) connTimeout)
                    .setSocketTimeout((int) connTimeout).build();

            HostnameVerifier verifier = null;
            SSLContext sslContext = null;

            /**
             * we have not configured for tls validations for now.
             */
            if (skipTlsValidation) {
                try {
                    verifier = NoopHostnameVerifier.INSTANCE;
                    sslContext = new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
                } catch (KeyManagementException e) {
                    throw new CloudRuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new CloudRuntimeException(e);
                } catch (KeyStoreException e) {
                    throw new CloudRuntimeException(e);
                }
            }

            client = HttpClients.custom()
                    .setDefaultRequestConfig(config)
                    .setSSLHostnameVerifier(verifier)
                    .setSSLContext(sslContext)
                    .build();
        }
        return client;
    }


}
