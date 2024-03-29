package mx.dapp.sdk.wallet.dto;

import org.json.JSONObject;

public class DappUser {

     private String name;
     private String image;
     private String address;
     private boolean suggestTip;

     public DappUser(JSONObject data){
         this.name = data.optString("name");
         this.image = data.optString("image");
         this.address = data.optString("address");
         this.suggestTip = data.optBoolean("suggest_tip");
     }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public boolean isSuggestTip() {
        return suggestTip;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "DappUser{" +
                "name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", address='" + address + '\'' +
                ", suggestTip=" + suggestTip +
                '}';
    }
}
