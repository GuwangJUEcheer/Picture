package hokumei.sys.picture.backend.exception;

import hokumei.sys.picture.backend.exception.BusinessException;

/**
 * 封装异常抛出类
 */
public class ThrowUtils {

	/**
	 * 条件成立 则抛出异常
	 * @param condition
	 * @param runtimeException
	 */
	public static void throwIf(boolean condition,RuntimeException runtimeException){
		if(condition){
			throw runtimeException;
		}
	}

	public static void throwIf(boolean condition,ErrorCode errorCode){
		throwIf(condition,new BusinessException(errorCode));
	}

	public static void throwIf(boolean condition,ErrorCode errorCode,String message){
		throwIf(condition,new BusinessException(errorCode,message));
	}
}
