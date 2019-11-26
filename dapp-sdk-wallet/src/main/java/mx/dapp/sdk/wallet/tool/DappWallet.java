package mx.dapp.sdk.wallet.tool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import mx.dapp.sdk.wallet.dto.DappWalletPayment;
import mx.dapp.sdk.wallet.gui.ReaderActivity;
import mx.dapp.sdk.wallet.network.Conection;
import mx.dapp.sdk.wallet.network.DappResponseProcess;
import mx.dapp.sdk.wallet.utils.DappEnviroment;
import mx.dapp.sdk.wallet.utils.DappException;
import mx.dapp.sdk.wallet.utils.DappQRType;
import mx.dapp.sdk.wallet.utils.DappWalletCallback;


public class DappWallet {

    private static final String DAPP_HOST = "https://dapp.mx/";
    public static final String CODE_STR = "str_code";
    private static final int DAPP_REQUEST = 555;
    private static final int RESULT_CANCEL = 0;
    private static final String RESULT_CANCEL_MESSAGE = "Process canceled by user.";
    public static final int RESULT_OK = 1;
    private static final int RESULT_NOT_INIT = -3;
    private static final String RESULT_NOT_INIT_MESSAGE = "Dapp Wallet library has not been initialized.";
    private static final int RESULT_RESPONSE_ERROR = -4;
    private static final String RESULT_RESPONSE_ERROR_MESSAGE = "An error has occurred during processing server response.";
    private static final int RESULT_INVALID_DATA = -5;
    private static final String RESULT_INVALID_DATA_MESSAGE = "Invalid data.";
    private static final String RESULT_ERROR_DEFAULT_MESSAGE = "An error has occurred.";
    private static final String RESULT_READER_LIBRARY_NOT_LINKED_MESSAGE = "In order to use Dapp QR Scanner, please add 'me.dm7.barcodescanner:zxing' and 'com.google.zxing:core' to your gradle";
    private static final int RESULT_INVALID_QR_CODE = -14;
    private static final String RESULT_INVALID_QR_CODE_MESSAGE = "Invalid QR code.";
    public static final int RESULT_PERMISSION_REJECTED = -15;
    private static final String RESULT_PERMISSION_REJECTED_MESSAGE = "Permission denied. Cannot use this functionality.";

    private static String api;
    private static Activity context;
    private static DappWalletCallback dappWalletCallback;
    private static DappEnviroment env;

    public static void init(Activity activity, String apiKey, DappEnviroment enviroment) {
        api = apiKey;
        context = activity;
        env = enviroment;
    }

    public static String getApiKey() {
        return api;
    }

    private static boolean isInit() {
        return api != null;
    }

    public static boolean isValidDappQR(String code) throws DappException {
        if (isInit()) {
            if (code != null && code.length() > 0) {
                String result = getResult(code);
                return result.length() > 0;
            }
        } else {
            throw new DappException(RESULT_NOT_INIT_MESSAGE, RESULT_NOT_INIT);
        }
        return false;
    }

    public static void readDappQR(String code, DappWalletCallback userCallback) {
        dappWalletCallback = userCallback;
        try {
            if (isValidDappQR(code)) {
                String result = getResult(code);
                Conection.readCode(result, new DappResponseProcess() {
                    @Override
                    public void processStart() {

                    }

                    @Override
                    public void processSuccess(String json) {
                        builCodeResult(json);
                    }

                    @Override
                    public void processFailed(Exception e) {
                        DappException de = new DappException(e.getMessage(), RESULT_RESPONSE_ERROR);
                        dappWalletCallback.onError(de);
                    }
                });
            } else {
                dappWalletCallback.onError(new DappException(RESULT_INVALID_QR_CODE_MESSAGE, RESULT_INVALID_QR_CODE));
            }
        } catch (DappException e) {
            dappWalletCallback.onError(e);
        }
    }

    private static String getResult(String code) {
        return code.contains(DAPP_HOST) ? getDappCode(code) : getCodiDappCode(code);
    }


    private static void readCode(String code) throws DappException {
        if (isValidDappQR(code)) {
            String result = getResult(code);
            Conection.readCode(code, new DappResponseProcess() {
                @Override
                public void processStart() {

                }

                @Override
                public void processSuccess(String json) {
                    builCodeResult(json);
                }

                @Override
                public void processFailed(Exception e) {
                    dappWalletCallback.onError(new DappException(e.getMessage(), RESULT_RESPONSE_ERROR));
                }
            });
        } else {
            throw new DappException(RESULT_INVALID_QR_CODE_MESSAGE, RESULT_INVALID_QR_CODE);
        }
    }


    public static void dappReader(DappWalletCallback userCallback) {
        dappWalletCallback = userCallback;
        try {
            Intent intent = new Intent(context, ReaderActivity.class);
            context.startActivityForResult(intent, DAPP_REQUEST);
        } catch (NoClassDefFoundError ncdfe) {
            throw new RuntimeException(RESULT_READER_LIBRARY_NOT_LINKED_MESSAGE, ncdfe);
        }
    }


    public static void onReaderResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == DAPP_REQUEST) {
                if (resultCode == RESULT_OK) {
                    String code = data.getExtras().getString(CODE_STR);
                    readCode(code);
                } else {
                    DappException e = manageResultError(resultCode);
                    dappWalletCallback.onError(e);
                }
            }
        } catch (DappException e) {
            dappWalletCallback.onError(e);
        }
    }


    public static boolean isCodi(String code) {
        try {
            JSONObject jsonObject = new JSONObject(code);
            return validateJsonCodi(jsonObject);
        } catch (JSONException e) {
            return false;
        }
    }

    private static boolean validateJsonCodi(JSONObject jsonObject) {
        boolean top = jsonObject.has("TPY") && jsonObject.has("v") && jsonObject.has("ic") && jsonObject.has("CRY");
        if (top) {
            return validateJsonCodiV(jsonObject.optJSONObject("v")) && validateJsonCodiIC(jsonObject.optJSONObject("ic"));
        }
        return false;
    }

    private static boolean validateJsonCodiV(JSONObject jsonObject) {
        return jsonObject.has("DEV");
    }

    private static boolean validateJsonCodiIC(JSONObject jsonObject) {
        return jsonObject.has("IDC") && jsonObject.has("SER") && jsonObject.has("ENC");
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


    private static String getDappCode(String code) {
        return code.substring(code.lastIndexOf("/") + 1, code.length());
    }

    private static String getCodiDappCode(String code) {
        JSONObject json = null;
        try {
            json = new JSONObject(code);
        } catch (JSONException e) {
            return "";
        }
        return json.optString("dapp", "");
    }

    private static void builCodeResult(String responseBody) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(responseBody);
        } catch (JSONException e) {
            DappException de = new DappException(RESULT_ERROR_DEFAULT_MESSAGE, RESULT_RESPONSE_ERROR);
            dappWalletCallback.onError(de);
            return;
        }
        int rc = jsonObject.optInt("rc", -1);
        String msg = jsonObject.optString("msg", RESULT_RESPONSE_ERROR_MESSAGE);
        Object data = jsonObject.opt("data");
        if (rc != 0) {
            DappException e = new DappException(msg, RESULT_RESPONSE_ERROR);
            dappWalletCallback.onError(e);
        } else {
            DappWalletPayment payment = new DappWalletPayment((JSONObject) data);
            dappWalletCallback.onSuccess(payment);
        }
    }

    private static DappException manageResultError(int resultCode) {
        String message;
        switch (resultCode) {
            case RESULT_CANCEL:
                message = RESULT_CANCEL_MESSAGE;
                break;
            case RESULT_INVALID_DATA:
                message = RESULT_INVALID_DATA_MESSAGE;
                break;
            case RESULT_NOT_INIT:
                message = RESULT_NOT_INIT_MESSAGE;
                break;
            case RESULT_RESPONSE_ERROR:
                message = RESULT_RESPONSE_ERROR_MESSAGE;
                break;
            case RESULT_INVALID_QR_CODE:
                message = RESULT_INVALID_QR_CODE_MESSAGE;
                break;
            case RESULT_PERMISSION_REJECTED:
                message = RESULT_PERMISSION_REJECTED_MESSAGE;
                break;
            default:
                message = RESULT_ERROR_DEFAULT_MESSAGE;
                break;
        }
        return new DappException(message, resultCode);
    }

    public static Context getContext() {
        return context;
    }

    public static DappEnviroment getEnviroment() {
        return env;
    }
}
