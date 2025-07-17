package hokumei.sys.picture.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import hokumei.sys.picture.backend.model.dto.spaceuser.SpaceUserAddRequest;
import hokumei.sys.picture.backend.model.dto.spaceuser.SpaceUserQueryRequest;
import hokumei.sys.picture.backend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import hokumei.sys.picture.backend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 17685
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-07-16 14:41:07
*/
public interface SpaceUserService extends IService<SpaceUser> {

	void validSpaceUser(SpaceUser spaceUser, boolean add);

	long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

	QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

	SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

	List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
