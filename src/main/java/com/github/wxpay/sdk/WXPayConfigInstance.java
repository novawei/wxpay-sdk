
package com.github.wxpay.sdk;

import java.io.InputStream;

public class WXPayConfigInstance extends WXPayConfig {
    private String appID;
    private String mchID;
    private String mchKey;
    private InputStream certStream;

    public WXPayConfigInstance() {
    }

    public String getAppID() {
        return this.appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getMchID() {
        return this.mchID;
    }

    public void setMchID(String mchID) {
        this.mchID = mchID;
    }

    public String getKey() {
        return this.mchKey;
    }

    public void setKey(String key) {
        this.mchKey = key;
    }

    public InputStream getCertStream() {
        return this.certStream;
    }

    public void setCertStream(InputStream certStream) {
        this.certStream = certStream;
    }

    public int getHttpConnectTimeoutMs() {
        return 6000;
    }

    public int getHttpReadTimeoutMs() {
        return 8000;
    }

    public IWXPayDomain getWXPayDomain() {
        return WXPayDomainSimpleImpl.instance();
    }

    public boolean shouldAutoReport() {
        return true;
    }

    public int getReportWorkerNum() {
        return 6;
    }

    public int getReportQueueMaxSize() {
        return 10000;
    }

    public int getReportBatchSize() {
        return 10;
    }
}
