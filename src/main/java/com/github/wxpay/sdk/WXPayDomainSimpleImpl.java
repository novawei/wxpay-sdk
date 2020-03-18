
package com.github.wxpay.sdk;

import com.github.wxpay.sdk.IWXPayDomain.DomainInfo;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.conn.ConnectTimeoutException;

public class WXPayDomainSimpleImpl implements IWXPayDomain {
    private final int MIN_SWITCH_PRIMARY_MSEC;
    private long switchToAlternateDomainTime;
    private Map<String, WXPayDomainSimpleImpl.DomainStatics> domainData;

    private WXPayDomainSimpleImpl() {
        this.MIN_SWITCH_PRIMARY_MSEC = 180000;
        this.switchToAlternateDomainTime = 0L;
        this.domainData = new HashMap();
    }

    public static IWXPayDomain instance() {
        return WXPayDomainSimpleImpl.WxpayDomainHolder.holder;
    }

    public synchronized void report(String domain, long elapsedTimeMillis, Exception ex) {
        WXPayDomainSimpleImpl.DomainStatics info = (WXPayDomainSimpleImpl.DomainStatics)this.domainData.get(domain);
        if (info == null) {
            info = new WXPayDomainSimpleImpl.DomainStatics(domain);
            this.domainData.put(domain, info);
        }

        if (ex == null) {
            if (info.succCount >= 2) {
                info.connectTimeoutCount = info.dnsErrorCount = info.otherErrorCount = 0;
            } else {
                ++info.succCount;
            }
        } else if (ex instanceof ConnectTimeoutException) {
            info.succCount = info.dnsErrorCount = 0;
            ++info.connectTimeoutCount;
        } else if (ex instanceof UnknownHostException) {
            info.succCount = 0;
            ++info.dnsErrorCount;
        } else {
            info.succCount = 0;
            ++info.otherErrorCount;
        }

    }

    public synchronized DomainInfo getDomain(WXPayConfig config) {
        WXPayDomainSimpleImpl.DomainStatics primaryDomain = (WXPayDomainSimpleImpl.DomainStatics)this.domainData.get("api.mch.weixin.qq.com");
        if (primaryDomain != null && !primaryDomain.isGood()) {
            long now = System.currentTimeMillis();
            if (this.switchToAlternateDomainTime == 0L) {
                this.switchToAlternateDomainTime = now;
                return new DomainInfo("api2.mch.weixin.qq.com", false);
            } else {
                WXPayDomainSimpleImpl.DomainStatics alternateDomain;
                if (now - this.switchToAlternateDomainTime >= 180000L) {
                    this.switchToAlternateDomainTime = 0L;
                    primaryDomain.resetCount();
                    alternateDomain = (WXPayDomainSimpleImpl.DomainStatics)this.domainData.get("api2.mch.weixin.qq.com");
                    if (alternateDomain != null) {
                        alternateDomain.resetCount();
                    }

                    return new DomainInfo("api.mch.weixin.qq.com", true);
                } else {
                    alternateDomain = (WXPayDomainSimpleImpl.DomainStatics)this.domainData.get("api2.mch.weixin.qq.com");
                    return alternateDomain != null && !alternateDomain.isGood() && alternateDomain.badCount() >= primaryDomain.badCount() ? new DomainInfo("api.mch.weixin.qq.com", true) : new DomainInfo("api2.mch.weixin.qq.com", false);
                }
            }
        } else {
            return new DomainInfo("api.mch.weixin.qq.com", true);
        }
    }

    private static class WxpayDomainHolder {
        private static IWXPayDomain holder = new WXPayDomainSimpleImpl();

        private WxpayDomainHolder() {
        }
    }

    static class DomainStatics {
        final String domain;
        int succCount = 0;
        int connectTimeoutCount = 0;
        int dnsErrorCount = 0;
        int otherErrorCount = 0;

        DomainStatics(String domain) {
            this.domain = domain;
        }

        void resetCount() {
            this.succCount = this.connectTimeoutCount = this.dnsErrorCount = this.otherErrorCount = 0;
        }

        boolean isGood() {
            return this.connectTimeoutCount <= 2 && this.dnsErrorCount <= 2;
        }

        int badCount() {
            return this.connectTimeoutCount + this.dnsErrorCount * 5 + this.otherErrorCount / 4;
        }
    }
}
