package hokumei.sys.picture.backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传请求类
 */
@Data
public class PictureUploadRequest implements Serializable {

	/**
	 * 图片 id（用于修改）
	 */
	private Long id;

	/**
	 * file URL
	 */
	private String url;

	/**
	 * 图片名称
	 */
	private String picName;

	/**
	 * 空间Id
	 */
	private Long spaceId;

	private static final long serialVersionUID = 1L;
}

