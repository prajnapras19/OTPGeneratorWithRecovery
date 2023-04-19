package com.example.otpgeneratorwithrecovery.crypto;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OTP Key URI Formatter, based on https://github.com/google/google-authenticator/wiki/Key-Uri-Format
 * format: PREFIX://TYPE/LABEL?PARAMETERS
 */
public class OTPURIFormat {
    private String otpURIString;
    private String prefix;
    private String type;
    private String label;
    private Map<String, String> parameterMap;

    public OTPURIFormat(String otpURIString) {
        String[] prefixSplit = otpURIString.split("://");
        if (prefixSplit.length != 2) {
            return;
        }

        String[] typeSplit = prefixSplit[1].split("/");
        if (typeSplit.length != 2) {
            return;
        }

        String[] labelSplit = typeSplit[1].split("\\?");
        if (labelSplit.length != 2) {
            return;
        }

        this.otpURIString = otpURIString;
        this.prefix = URLDecoder.decode(prefixSplit[0], StandardCharsets.UTF_8);
        this.type = URLDecoder.decode(typeSplit[0], StandardCharsets.UTF_8);
        this.label = URLDecoder.decode(labelSplit[0], StandardCharsets.UTF_8);
        String parameters = labelSplit[1];

        this.parameterMap = new HashMap<>();
        String[] parameterList = parameters.split("&");
        for (int i = 0; i < parameterList.length; i++) {
            String[] parameter = parameterList[i].split("=");
            if (parameter.length != 2) {
                continue;
            }
            parameterMap.put(parameter[0], URLDecoder.decode(parameter[1], StandardCharsets.UTF_8));
        }
    }

    public OTPURIFormat(String prefix, String type, String label, Map<String, String> parameterMap) {
        this.prefix = prefix;
        this.type = type;
        this.label = label;
        this.parameterMap = parameterMap;

        List<String> parameterList = new ArrayList<>();
        for (Map.Entry<String,String> parameter : this.parameterMap.entrySet()) {
            parameterList.add(String.format("%s=%s", parameter.getKey(), URLEncoder.encode(parameter.getValue(), StandardCharsets.UTF_8)));
        }
        String parameters = String.join("&", parameterList);

        this.otpURIString =  String.format("%s://%s/%s?%s",
                URLEncoder.encode(this.prefix, StandardCharsets.UTF_8),
                URLEncoder.encode(this.type, StandardCharsets.UTF_8),
                URLEncoder.encode(this.label, StandardCharsets.UTF_8),
                parameters);
    }

    public String toString() {
        return this.otpURIString;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getType() {
        return this.type;
    }

    public String getLabel() {
        return this.label;
    }

    public String getParameter(String key) {
        return this.parameterMap.get(key);
    }

    public Map<String, String> getParameterMap() {
        return this.parameterMap;
    }
}