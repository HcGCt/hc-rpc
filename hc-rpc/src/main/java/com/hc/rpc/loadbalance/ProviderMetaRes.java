package com.hc.rpc.loadbalance;

import com.hc.rpc.common.ProviderMeta;

import java.util.Collection;

/**
 * @Author hc
 */
public class ProviderMetaRes {

    private ProviderMeta cur;
    private Collection<ProviderMeta> others;

    public static ProviderMetaRes build(ProviderMeta cur,Collection<ProviderMeta> others) {
        final ProviderMetaRes providerMetaRes = new ProviderMetaRes();
        providerMetaRes.cur = cur;
        others.remove(cur);
        providerMetaRes.others = others;
        return providerMetaRes;
    }


    public ProviderMeta getCur() {
        return cur;
    }

    public Collection<ProviderMeta> getOthers() {
        return others;
    }
}
