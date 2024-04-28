package com.hc.rpc.loadbalance;

import com.hc.rpc.common.ProviderMate;

import java.util.Collection;

/**
 * @Author hc
 */
public class ProviderMateRes {

    private ProviderMate cur;
    private Collection<ProviderMate> others;

    public static ProviderMateRes build(ProviderMate cur,Collection<ProviderMate> others) {
        final ProviderMateRes providerMateRes = new ProviderMateRes();
        providerMateRes.cur = cur;
        others.remove(cur);
        providerMateRes.others = others;
        return providerMateRes;
    }


    public ProviderMate getCur() {
        return cur;
    }

    public Collection<ProviderMate> getOthers() {
        return others;
    }
}
