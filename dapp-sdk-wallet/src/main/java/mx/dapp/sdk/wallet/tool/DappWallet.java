package mx.dapp.sdk.wallet.tool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import mx.dapp.sdk.wallet.R;
import mx.dapp.sdk.wallet.dto.DappCode;
import mx.dapp.sdk.wallet.gui.DappReaderActivity;
import mx.dapp.sdk.wallet.network.DappConnection;
import mx.dapp.sdk.wallet.network.ResponseHandler;
import mx.dapp.sdk.wallet.utils.DappCallback;
import mx.dapp.sdk.wallet.utils.DappEnviroment;
import mx.dapp.sdk.wallet.utils.DappException;
import mx.dapp.sdk.wallet.utils.DappQRType;


public class DappWallet {

    private static final String DAPP_HOST = "https://dapp.mx/";
    private static final int DAPP_REQUEST = 555;

    private static String apiKey;
    private static DappEnviroment dappEnv;

    public static final int RESULT_CANCELLED = -2;
    public static final int RESULT_NOT_INIT = -3;
    public static final int RESULT_RESPONSE_ERROR = -4;
    public static final int RESULT_INVALID_DATA = -5;
    public static final int RESULT_INVALID_QR_CODE = -14;
    public static final int RESULT_PERMISSION_REJECTED = -15;

    public static void init(String apiKey, DappEnviroment enviroment) {
        DappWallet.apiKey = apiKey;
        DappWallet.dappEnv = enviroment;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static DappEnviroment getEnviroment() {
        return dappEnv;
    }

    public static boolean isConfigured() {
        return apiKey != null;
    }

    public static boolean isValidDappQR(String code) {
        if (code != null && code.length() > 0) {
            String result = getDappId(code);
            if (result == null) {
                return false;
            }
            return result.length() > 0;
        }
        return false;
    }

    public static boolean isCodi(String code) {
        try {
            JSONObject jsonObject = new JSONObject(code);
            return validateJsonCodi(jsonObject);
        } catch (JSONException e) {
            return false;
        }
    }

    public static DappQRType getQRType(String code) {
        DappQRType type = DappQRType.UNKNOWN;
        if (code.contains(DAPP_HOST)) {
            type = DappQRType.DAPP;
        } else {
            try {
                JSONObject jsonObject = new JSONObject(code);
                if (validateJsonCodi(jsonObject)) {
                    if (jsonObject.has("dapp")) {
                        type = DappQRType.CODI_DAPP;
                    } else {
                        type = DappQRType.CODI;
                    }
                } else {
                    if (jsonObject.has("dapp")) {
                        type = DappQRType.DAPP;
                    }
                }
            } catch (JSONException e) {
                return DappQRType.UNKNOWN;
            }
        }
        return type;
    }

    public static String getDappId(String code) {
        return code.contains(DAPP_HOST) ? getDappCode(code) : getCodiDappCode(code);
    }

    public static void readDappQR(Activity activity, String qrString,
                                  DappCallback callback) {
        try {
            if (isValidDappQR(qrString)) {
                String dappCode = getDappId(qrString);
                networkReadDappCode(activity, dappCode, callback);
            } else {
                callback.onError(getDappException(activity, RESULT_INVALID_QR_CODE));
            }
        } catch (DappException e) {
            callback.onError(e);
        }
    }

    public static void dappReader(Activity context) {
        try {
            Intent intent = new Intent(context, DappReaderActivity.class);
            context.startActivityForResult(intent, DAPP_REQUEST);
        } catch (NoClassDefFoundError ncdfe) {
            throw new RuntimeException(
                    context.getString(R.string.ex_reader_library_not_linked), ncdfe);
        }
    }


    public static void onReaderResult(Context context, int requestCode, int resultCode, Intent data,
                                      DappCallback callback) {
        try {
            if (requestCode == DAPP_REQUEST) {
                if (resultCode == Activity.RESULT_OK) {
                    String qrString = data.getExtras().getString(DappReaderActivity.QR_STR_KEY);

                    if (isValidDappQR(qrString)) {
                        String dappCode = getDappId(qrString);
                        networkReadDappCode(context, dappCode, callback);
                    }
                    else {
                        throw getDappException(context, RESULT_INVALID_QR_CODE);
                    }
                }
                else {
                    DappException e = getDappException(context, RESULT_CANCELLED);
                    callback.onError(e);
                }
            }
        } catch (DappException e) {
            callback.onError(e);
        }
    }

    public static DappException getDappException(Context context, int resultCode) {
        int messageId = R.string.ex_error_default;
        switch (resultCode) {
            case RESULT_CANCELLED:
                messageId = R.string.ex_cancelled;
                break;
            case RESULT_INVALID_DATA:
                messageId = R.string.ex_invalid_data;
                break;
            case RESULT_RESPONSE_ERROR:
                messageId = R.string.ex_server_response_error;
                break;
            case RESULT_INVALID_QR_CODE:
                messageId = R.string.ex_invalid_qr;
                break;
            case RESULT_PERMISSION_REJECTED:
                messageId = R.string.ex_permission_rejected;
                break;
            case RESULT_NOT_INIT:
                messageId = R.string.ex_not_init;
                break;
        }
        return new DappException(context.getString(messageId), resultCode);
    }

    private static void networkReadDappCode(final Context context, String dappCode,
                                            final DappCallback callback)
            throws DappException {
        DappConnection.readCode(context, dappCode, new ResponseHandler() {
            @Override
            public void processStart() {

            }

            @Override
            public void processSuccess(String json) {
                buildCodeResult(context, json, callback);
            }

            @Override
            public void processFailed(Exception e) {
                DappException de = new DappException(e.getMessage(), RESULT_RESPONSE_ERROR);
                callback.onError(de);
            }
        });
    }

    private static boolean validateJsonCodi(JSONObject jsonObject) {
        boolean top = jsonObject.has("TYP") &&
                jsonObject.has("v") &&
                jsonObject.has("ic") &&
                jsonObject.has("CRY");
        if (top) {
            return validateJsonCodiV(jsonObject.optJSONObject("v")) &&
                    validateJsonCodiIC(jsonObject.optJSONObject("ic"));
        }
        return false;
    }

    private static boolean validateJsonCodiV(JSONObject jsonObject) {
        return jsonObject.has("DEV");
    }

    private static boolean validateJsonCodiIC(JSONObject jsonObject) {
        return jsonObject.has("IDC") &&
                jsonObject.has("SER") &&
                jsonObject.has("ENC");
    }

    private static String getDappCode(String code) {
        try {
            return code.substring(code.lastIndexOf("/") + 1);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getCodiDappCode(String code) {
        JSONObject json;
        try {
            json = new JSONObject(code);
        } catch (JSONException e) {
            return null;
        }
        return json.optString("dapp");
    }

    private static void buildCodeResult(Context context, String responseBody,
                                        DappCallback callback) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(responseBody);
        } catch (JSONException e) {
            DappException de = new DappException(e.getMessage(), RESULT_RESPONSE_ERROR);
            callback.onError(de);
            return;
        }

        int rc = jsonObject.optInt("rc", -1);
        String msg = jsonObject.optString("msg", context.getString(R.string.ex_server_response_error));
        Object data = jsonObject.opt("data");
        if (rc != 0) {
            DappException e = new DappException(msg, RESULT_RESPONSE_ERROR);
            callback.onError(e);
        }
        else {
            DappCode payment = new DappCode((JSONObject) data);
            callback.onSuccess(payment);
        }
    }

}
