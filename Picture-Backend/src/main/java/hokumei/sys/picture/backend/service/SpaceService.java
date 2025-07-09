package hokumei.sys.picture.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import hokumei.sys.picture.backend.model.dto.space.SpaceAddRequest;
import hokumei.sys.picture.backend.model.entity.Picture;
import hokumei.sys.picture.backend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import hokumei.sys.picture.backend.model.dto.space.SpaceQueryRequest;
import hokumei.sys.picture.backend.model.entity.User;
import hokumei.sys.picture.backend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 17685
* @description 针对表【space(空间表)】的数据库操作Service
* @createDate 2025-05-05 00:05:15
*/
public interface SpaceService extends IService<Space> {

	Long addSpace(SpaceAddRequest addRequest, User loginUser);
	/**
	 * 校验参数
	 * @param Space 需要校验的 Space 对象
	 * @param isAdd 是否是创建时间
	 */
	void validSpace(Space Space,boolean isAdd);

	/**
	 * 获取查询的 queryWrapper
	 * @param SpaceQueryRequest 图片请求类
	 * @return 可用来查询的 queryWrapper
	 */
	QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest SpaceQueryRequest);

	/**
	 * 获取单个图片的 VO 对象
	 * @param Space Space 对象
	 * @param request request 请求
	 * @return 对应图片的 VO
	 */
	SpaceVO getSpaceVO(Space Space, HttpServletRequest request);

	/**
	 * 分页获取图片 VO 对象
	 * @param SpacePage  page 对象
	 * @param request request 请求
	 * @return 分页的 VO
	 */
	Page<SpaceVO> getSpaceVOPage(Page<Space> SpacePage, HttpServletRequest request);

	/**
	 * 根据空间 实现扩容
	 * @param space 空间
	 */
	void fillSpaceBySpaceLevel(Space space);

	void checkSpaceAuth(User loginUser, Space space);
}
