package mx.dapp.sdk.wallet.utils;


public enum DappEnviroment {
    PRODUCTION("https://wallets.dapp.mx/"),
    SANDBOX("https://wallets-sandbox.dapp.mx/");

    private String target;

    DappEnviroment(String target){
        this.target = target;
    }

    public String getTarget(){
        return target;
    }
}
