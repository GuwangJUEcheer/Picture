package hokumei.sys.picture.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 图片表
 *
 * @TableName picture
 */
@TableName(value = "picture")
@Data
public class Picture {
	/**
	 * id
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

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
	private String tags;

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
	 * 创建用户 ID
	 */
	private Long userId;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 编辑时间
	 */
	private Date editTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 * 是否删除
	 */
	private Integer isDelete;

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
	 * 缩略图 url
	 */
	private String thumbnailUrl;

	/**
	 * 空间 id（为 0 表示公共空间）
	 */
	private Long spaceId;

	/**
	 * 图片主色调 rgb
	 */
	private String picColor;
}