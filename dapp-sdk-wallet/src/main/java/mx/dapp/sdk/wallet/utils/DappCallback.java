package mx.dapp.sdk.wallet.utils;

import mx.dapp.sdk.wallet.dto.DappCode;

public interface DappCallback {
    void onSuccess(DappCode payment);

    void onError(DappException exception);
}
