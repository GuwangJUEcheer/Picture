package hokumei.sys.picture.backend.model.dto.picture;

import hokumei.sys.picture.backend.api.aliyun.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 17685
 * AI阔图任务请求类
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

	/**
	 * 图片 id
	 */
	private Long pictureId;

	/**
	 * 扩图参数
	 */
	private CreateOutPaintingTaskRequest.Parameters parameters;

	private static final long serialVersionUID = 1L;
}

