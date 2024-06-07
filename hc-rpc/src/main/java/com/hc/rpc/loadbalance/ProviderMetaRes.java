package com.hc.rpc.loadbalance;

import com.hc.rpc.common.ProviderMeta;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @Author hc
 */
public class ProviderMetaRes {

    private ProviderMeta cur;
    private Collection<ProviderMeta> others;

    public static ProviderMetaRes build(ProviderMeta cur,Collection<ProviderMeta> providerMetas) {
        final ProviderMetaRes providerMetaRes = new ProviderMetaRes();
        providerMetaRes.others = new ArrayList<>(providerMetas);
        providerMetaRes.cur = cur;
        providerMetaRes.others.remove(cur);
        return providerMetaRes;
    }


    public ProviderMeta getCur() {
        return cur;
    }

    public Collection<ProviderMeta> getOthers() {
        return others;
    }
}
