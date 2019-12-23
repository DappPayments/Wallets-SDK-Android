package mx.dapp.sdk.wallet.network;

public interface ResponseHandler {
    void processStart();
    void processSuccess(String json);
    void processFailed(Exception e);
}
