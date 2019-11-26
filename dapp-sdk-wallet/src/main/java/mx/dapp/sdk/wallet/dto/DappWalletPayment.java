package mx.dapp.sdk.wallet.dto;

import org.json.JSONObject;

public class DappWalletPayment {

    private String id;
    private String amount;
    private String description;
    private DappUser user;
    private JSONObject data;

    public DappWalletPayment(JSONObject data) {
        this.id = data.optString("id");
        this.amount = data.optString("amount");
        this.description = data.optString("description");
        this.user = new DappUser(data.optJSONObject("dapp_user"));
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public DappUser getUser() {
        return user;
    }

    public JSONObject getData() {
        return data;
    }

    @Override
    public String toString() {
        return "DappWalletPayment{" +
                "id='" + id + '\'' +
                ", amount='" + amount + '\'' +
                ", description='" + description + '\'' +
                ", user=" + user +
                '}';
    }
}
