package hokumei.sys.picture.backend.common;


import hokumei.sys.picture.backend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author  谷王珏
 * 通用返回类
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    public BaseResponse(int code,String message) {
        this(code, null, message, "");
    }

    public BaseResponse(int code, T data) {
        this(code, data, "", "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }

    public BaseResponse(ErrorCode errorCode,String message) {
        this(errorCode.getCode(), null,message, errorCode.getDescription());
    }
}
