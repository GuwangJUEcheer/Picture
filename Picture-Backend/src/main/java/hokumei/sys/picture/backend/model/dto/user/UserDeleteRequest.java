package hokumei.sys.picture.backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDeleteRequest implements Serializable {
	//删除索引
	private String id;
}
