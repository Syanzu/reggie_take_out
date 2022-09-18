package com.syanzu.reggie.common;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用的返回结果类，服务端响应的数据最终都封装成此对象
 * @param <T>
 */
@Data
public class R<T> {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map map = new HashMap(); //动态数据

    /**
     * Login验证成功后返回给前端的经R包装后的结果
     * @param object Entity实体类对象
     * @param <T>
     * @return 实体类对象 + 成功码
     */
    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }


    /**
     *
     * @param msg 失败需要提示的信息
     * @param <T>
     * @return 失败的的提示信息 + 失败码
     */
    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
