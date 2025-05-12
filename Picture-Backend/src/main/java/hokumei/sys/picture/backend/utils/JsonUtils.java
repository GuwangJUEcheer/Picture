package hokumei.sys.picture.backend.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * json转换类
 */
public class JsonUtils {

	/**
	 * 返回对应集合的List
	 * @param jsonString 目标Json字符串
	 * @param type 类型类包装器
	 * @return List对应类型
	 * @param <T> 类型
	 */
	public static <T> List<T> jsonToList(String jsonString, Class<T> type) {
		Gson gson = new Gson();

		// 构造正确的 TypeToken
		Type listType = TypeToken.getParameterized(List.class, type).getType();

		// 解析 JSON 为 List<T>
		return gson.fromJson(jsonString, listType);
	}

	public static <T> String listToJson(List<T> list) {
		Gson gson = new Gson();
		return gson.toJson(list);
	}
}
