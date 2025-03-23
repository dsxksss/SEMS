package com.sems.sportseventmanagementsystem.common;

import lombok.Data;

/**
 * 统一响应结果类
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    // 默认成功响应码
    private static final int SUCCESS_CODE = 200;
    // 默认失败响应码
    private static final int ERROR_CODE = 500;

    /**
     * 成功结果
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功结果
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return success("操作成功", data);
    }

    /**
     * 成功结果
     *
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 错误结果
     *
     * @param <T> 数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error() {
        return error("操作失败");
    }

    /**
     * 错误结果
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(String message) {
        return error(ERROR_CODE, message);
    }

    /**
     * 错误结果
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
} 