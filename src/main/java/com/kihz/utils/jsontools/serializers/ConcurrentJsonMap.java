package com.kihz.utils.jsontools.serializers;

import com.kihz.utils.jsontools.containers.JsonMap;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentJsonMap<V> extends JsonMap<V> {

    public ConcurrentJsonMap() {
        super(new ConcurrentHashMap<>());
    }
}