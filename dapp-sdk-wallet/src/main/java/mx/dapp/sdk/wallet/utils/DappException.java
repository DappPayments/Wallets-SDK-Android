package mx.dapp.sdk.wallet.utils;


public class DappException extends Exception {

    private int codeError;

    public DappException(String message, int codeError){
        super(message);
        this.codeError = codeError;
    }

    public int getCodeError() {
        return codeError;
    }

    public void setCodeError(int codeError) {
        this.codeError = codeError;
    }
}
