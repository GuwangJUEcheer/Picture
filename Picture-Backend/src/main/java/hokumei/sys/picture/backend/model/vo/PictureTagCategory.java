package hokumei.sys.picture.backend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 返回给前端的分类VO
 */
@Data
public class PictureTagCategory {

	private List<String> tagList;

	private List<String> categoryList;
}
