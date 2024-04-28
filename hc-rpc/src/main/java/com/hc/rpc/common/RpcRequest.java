package com.hc.rpc.common;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author hc
 */
public class RpcRequest implements Serializable {
    private String version;
    private String clazzName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] parameterTypes;
    private Map<String,Object> serviceAttachments;
    private Map<String,Object> clientAttachments;


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Map<String, Object> getServiceAttachments() {
        return serviceAttachments;
    }

    public void setServiceAttachments(Map<String, Object> serviceAttachments) {
        this.serviceAttachments = serviceAttachments;
    }

    public Map<String, Object> getClientAttachments() {
        return clientAttachments;
    }

    public void setClientAttachments(Map<String, Object> clientAttachments) {
        this.clientAttachments = clientAttachments;
    }
}
