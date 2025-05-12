package hokumei.sys.picture.backend.aop;

import hokumei.sys.picture.backend.annotation.AuthCheck;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.model.entity.User;
import hokumei.sys.picture.backend.model.enums.UserRoleEnum;
import hokumei.sys.picture.backend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
	private UserService userService;

	/**
	 *
	 * @param joinPoint 切点
	 * @param authCheck 权限
	 * @return 暂定
	 * @throws Throwable 异常处理
	 */
	@Around("@annotation(authCheck)")
	public Object check(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
         String mustRole = authCheck.mustRole();
		 RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		 HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

		 // 获得当前登录的用户信息
		User loginUser = userService.getLoginUser(request);
		UserRoleEnum mustRoleValue = UserRoleEnum.getEnumByValue(mustRole);
		if(mustRoleValue == null){
			return joinPoint.proceed();
		}

		UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
		if(userRoleEnum == null){
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		if(mustRoleValue.equals(UserRoleEnum.ADMIN) && !UserRoleEnum.ADMIN.equals(userRoleEnum)){
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		//通过权限校验 放行
		return joinPoint.proceed();
	}
}
