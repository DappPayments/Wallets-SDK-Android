package mx.dapp.sdk.wallet.network;


public class Conection {

    public static void readCode(String code, DappResponseProcess responseHandler) {
        DappWalletConnect dp = new DappWalletConnect(responseHandler);
        dp.execute("/dapp-codes/" + code);
    }
}
