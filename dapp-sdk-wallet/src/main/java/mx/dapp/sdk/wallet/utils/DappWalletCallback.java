package mx.dapp.sdk.wallet.utils;

import mx.dapp.sdk.wallet.dto.DappWalletPayment;

public interface DappWalletCallback {
    void onSuccess(DappWalletPayment payment);

    void onError(DappException exception);
}
