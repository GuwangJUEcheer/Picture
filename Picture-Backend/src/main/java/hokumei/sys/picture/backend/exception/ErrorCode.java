package hokumei.sys.picture.backend.exception;

import lombok.Getter;

/**
 * 错误码
 *
 * @author guwangjue
 */
@Getter
public enum ErrorCode {
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN_ERROR(40100, "未登录", ""),
    NO_AUTH_ERROR(40101, "无权限", ""),
    SYSTEM_ERROR(5000, "系统内部异常", "系统内部发生的异常"),
    JSON_PARSE_ERROR(501, "JSON解析异常", "JSON解析异常"),
    OPERATION_ERROR(502, "操作异常", "操作异常"),
    NOT_FOUND_ERROR(503,"数据库查询失败","数据库查询异常");

    private final int code;
    private final String message;
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
}