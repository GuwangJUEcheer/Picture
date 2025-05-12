package hokumei.sys.picture.backend.model.dto.file;

import lombok.Data;

/**
 * 上传图片返回结果类
 */
@Data
public class UploadPictureResult {


	/**
	 * 图片 URL
	 */
	private String url;

	/**
	 * 图片名称
	 */
	private String name;

	/**
	 * 图片体积
	 */
	private Long picSize;

	/**
	 * 图片宽度
	 */
	private Integer picWidth;

	/**
	 * 图片高度
	 */
	private Integer picHeight;

	/**
	 * 图片缩放比例
	 */
	private Double picScale;

	/**
	 * 图片格式
	 */
	private String picFormat;

	/**
	 * 缩略图
	 */
	private String thumbnailUrl;

}
