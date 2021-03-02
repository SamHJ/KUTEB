package com.naijaunik.kuteb.Api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

  public class ServerResponse implements Serializable {

        @SerializedName("data")
        private JsonArray data;
        @SerializedName("result")
        private String result;
        @SerializedName("admin_settings")
        private JsonObject admin_settings;
        @SerializedName("userdata")
        private JsonObject userdata;
        @SerializedName("type")
        private String type;
        @SerializedName("totalearnings")
        private String totalearnings;


        public ServerResponse(JsonArray data, String result,
                              JsonObject admin_settings, JsonObject userdata,String type,
                              String totalearnings){
            this.data = data;
            this.result = result;
            this.admin_settings = admin_settings;
            this.userdata = userdata;
            this.type = type;
            this.totalearnings = totalearnings;
        }

      public String getTotalearnings() {
          return totalearnings;
      }

      public void setTotalearnings(String totalearnings) {
          this.totalearnings = totalearnings;
      }

      public String getType() {
          return type;
      }

      public void setType(String type) {
          this.type = type;
      }

      public JsonObject getUserdata() {
          return userdata;
      }

      public void setUserdata(JsonObject userdata) {
          this.userdata = userdata;
      }

      public JsonObject getAdmin_settings() {
          return admin_settings;
      }

      public void setAdmin_settings(JsonObject admin_settings) {
          this.admin_settings = admin_settings;
      }

      public String getResult() {
          return result;
      }

      public void setResult(String result) {
          this.result = result;
      }

      public JsonArray getData() {
          return data;
      }

      public void setData(JsonArray data) {
          this.data = data;
      }

    }