package hokumei.sys.picture.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {

	ADMIN("管理员", "admin"),
	USER("普通用户", "user");

	private final String text;
	private final String value;

	// 用于缓存映射，提高查询效率
	private static final Map<String, UserRoleEnum> VALUE_MAP = new HashMap<>();

	// 静态初始化映射表
	static {
		for (UserRoleEnum role : UserRoleEnum.values()) {
			VALUE_MAP.put(role.value, role);
		}
	}

	UserRoleEnum(String text, String value) {
		this.text = text;
		this.value = value;
	}

	/**
	 * 根据 value 获取枚举
	 *
	 * @param value 枚举值
	 * @return 匹配的枚举对象（如果不存在则返回 null）
	 */
	public static UserRoleEnum getEnumByValue(String value) {
		if (ObjUtil.isEmpty(value)) {
			return null;
		}
		return VALUE_MAP.get(value);
	}
}
