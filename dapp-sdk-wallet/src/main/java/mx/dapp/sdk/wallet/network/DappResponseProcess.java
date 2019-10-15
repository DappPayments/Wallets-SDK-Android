package mx.dapp.sdk.wallet.network;

public interface DappResponseProcess {

    void processStart();
    void processSuccess(String json);
    void processFailed(Exception e);

}
