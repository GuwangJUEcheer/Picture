package hokumei.sys.picture.backend.common;

import hokumei.sys.picture.backend.exception.ErrorCode;

/**
 * @author 谷王珏
 * 返回工具类
 */
public class ResultUtils {

    /**
     * 成功
     * @param data 原本的返回值
     * @return 返回封装类型
     * @param <T> 泛型
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return BaseResponse 对象
     */
    public static  BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    public static  BaseResponse error(int code,String message) {
        return new BaseResponse<>(code,message);
    }

    public static  BaseResponse error(ErrorCode code,String message) {
        return new BaseResponse<>(code,message);
    }

    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse<>(code, message, description);
    }
}
