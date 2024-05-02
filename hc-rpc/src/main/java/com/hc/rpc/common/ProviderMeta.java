package com.hc.rpc.common;

import java.io.Serializable;

/**
 * 服务提供者信息
 * @Author hc
 */
public class ProviderMeta implements Serializable {
    private String name;
    private String address;
    private String version;

    /**
     * 关于redis注册中心的属性
     */
    private long endTime;

    private String UUID;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProviderMeta other = (ProviderMeta) o;
        return name.equals(other.name)
                && address.equals(other.address) &&
                version.equals(other.version);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
