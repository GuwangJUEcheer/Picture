package hokumei.sys.picture.backend.model.dto.user;

import hokumei.sys.picture.backend.common.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "用户查询请求对象")
public class UserQueryRequest extends PageRequest implements Serializable {

	@ApiModelProperty(value = "用户ID", example = "1001")
	private Long id;

	@ApiModelProperty(value = "用户昵称", example = "张三")
	private String userName;

	@ApiModelProperty(value = "账号", example = "user123")
	private String userAccount;

	@ApiModelProperty(value = "用户简介", example = "Java 开发者")
	private String userProfile;

	@ApiModelProperty(value = "用户角色（user/admin/ban）", example = "user")
	private String userRole;

	private static final long serialVersionUID = 1L;
}

