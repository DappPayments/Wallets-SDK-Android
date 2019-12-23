package mx.dapp.sdk.wallet.network;


import android.content.Context;

import mx.dapp.sdk.wallet.utils.DappException;

public class DappConnection {

    public static void readCode(Context context, String code, ResponseHandler responseHandler) throws DappException {
        NetworkConnect nc = new NetworkConnect(context, responseHandler);
        nc.execute("/dapp-codes/" + code);
    }
}
