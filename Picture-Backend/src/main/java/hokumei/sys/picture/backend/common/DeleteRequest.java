package hokumei.sys.picture.backend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求类
 */
@Data
public class DeleteRequest implements Serializable {

	/**
	 * id 图片的Id 或者是帖子的Id
	 */
	private Long id;

	private static final long serialVersionUID = 1L;
}
