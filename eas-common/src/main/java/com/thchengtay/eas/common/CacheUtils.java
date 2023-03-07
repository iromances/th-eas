package com.thchengtay.eas.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @auth shihao
 * @date created on 2020/12/21 23:25
 * @desc
 **/
public class CacheUtils {

    private CacheUtils(){

    }

    private static final Map<String, Object> container = new HashMap<>();

    public static void put(String key, Object obj){
        container.put(key, obj);
    }

    public static Object get(String key){
        return container.get(key);
    }

}
