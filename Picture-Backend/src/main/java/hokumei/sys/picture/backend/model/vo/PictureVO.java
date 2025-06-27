package hokumei.sys.picture.backend.model.vo;

import hokumei.sys.picture.backend.model.entity.Picture;
import hokumei.sys.picture.backend.utils.JsonUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * 图片通用返回类
 */
@Data
public class PictureVO {

	private String id;

	/**
	 * 图片 URL
	 */
	private String url;

	/**
	 * 图片名称
	 */
	private String name;

	/**
	 * 简介
	 */
	private String introduction;

	/**
	 * 分类
	 */
	private String category;

	/**
	 * 标签（JSON 数组）
	 */
	private List<String> tags;

	/**
	 * 图片体积
	 */
	private Long picSize;

	/**
	 * 图片SpaceId
	 */
	private Long spaceId;

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
	 * 创建用户 ID
	 */
	private Long userId;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 * 缩略图
	 */
	private String thumbnailUrl;

	/**
	 * 创建用户的信息
	 */
	private UserVO user;

	/**
	 * 审核状态：0-待审核; 1-通过; 2-拒绝
	 */
	private Integer reviewStatus;

	/**
	 * 审核信息
	 */
	private String reviewMessage;

	/**
	 * 审核人 ID
	 */
	private Long reviewerId;

	/**
	 * 审核时间
	 */
	private Date reviewTime;


	/**
	 * 实体类和VO转换方法
	 */
	public static Picture voToObj(PictureVO pictureVO){
       if(pictureVO == null) return null;
	   Picture picture = new Picture();
	   if(StringUtils.isNotBlank(pictureVO.getId())){
		   picture.setId(Long.valueOf(pictureVO.getId()));
	   }
	   BeanUtils.copyProperties(pictureVO,picture);
	   picture.setTags(JsonUtils.listToJson(pictureVO.getTags()));
	   return picture;
	}

	/**
	 * 实体类转换位VO
	 */
	public static PictureVO objToVo(Picture picture){
		if(picture == null) return null;
		PictureVO pictureVO = new PictureVO();
		BeanUtils.copyProperties(picture,pictureVO);
		List<String> tagList = JsonUtils.jsonToList(picture.getTags(), String.class);
		pictureVO.setId(Long.toString(picture.getId()));
		pictureVO.setTags(tagList);
		return pictureVO;
	}
}
