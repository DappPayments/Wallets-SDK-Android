package mx.dapp.sdk.wallet.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import mx.dapp.sdk.wallet.network.customtls.Tls12SocketFactory;
import mx.dapp.sdk.wallet.tool.DappWallet;
import mx.dapp.sdk.wallet.utils.DappException;

class NetworkConnect extends AsyncTask<String, Long, String> {

    private static final String URL_VERSION = "v1";

    private Exception exception;
    private ResponseHandler responseHandler;

    private String performGetCall(String requestUrl) {
        URL url;
        StringBuilder response = new StringBuilder();

        try {
            url = new URL(requestUrl);

            String base64 = "";
            try {
                byte[] data = (DappWallet.getApiKey() + ":").getBytes("UTF-8");
                base64 = Base64.encodeToString(data, Base64.NO_WRAP);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            try {
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                conn.setSSLSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            conn.setRequestProperty("Authorization", "Basic " + base64);
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            } else {
                response = new StringBuilder();

            }
        } catch (Exception e) {
            exception = e;
        }

        return response.toString();
    }

    NetworkConnect(Context context,  ResponseHandler responseHandler) throws DappException {
        if (!DappWallet.isConfigured()) {
            throw DappWallet.getDappException(context, DappWallet.RESULT_NOT_INIT);
        }

        this.responseHandler = responseHandler;
    }

    @Override
    protected void onPreExecute() {
        if (responseHandler != null) {
            responseHandler.processStart();
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        String baseURL = DappWallet.getEnviroment().getTarget();
        String endpoint = strings[0];

        String responseBody = performGetCall(baseURL + URL_VERSION + endpoint);

        return responseBody;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
    }

    @Override
    protected void onCancelled() {
    }

    @Override
    protected void onPostExecute(String result) {
        if (responseHandler != null) {
            if (exception != null) {
                responseHandler.processFailed(exception);
                return;
            }

            if (result != null) {
                responseHandler.processSuccess(result);
            }
        }
    }

}
