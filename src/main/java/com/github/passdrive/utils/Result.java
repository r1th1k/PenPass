package com.github.passdrive.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Result {
    private int code;
    private String message;
    private JsonObject data = null;

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(int code, String message, JsonObject data) {
        this(code, message);
        this.data = data;
    }

    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", code);
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("data", data.getAsString());
        return jsonObject;
    }
}
