package hokumei.sys.picture.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) //针对方法
@Retention(RetentionPolicy.RUNTIME) //运行时候生效
public @interface AuthCheck {

	/**
	 * 必须具有的某个角色
	 */
	String mustRole() default "";
}
