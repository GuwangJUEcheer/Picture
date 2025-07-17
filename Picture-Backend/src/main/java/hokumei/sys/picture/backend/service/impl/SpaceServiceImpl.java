package hokumei.sys.picture.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hokumei.sys.picture.backend.model.dto.space.SpaceAddRequest;
import hokumei.sys.picture.backend.model.entity.Space;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.exception.ThrowUtils;
import hokumei.sys.picture.backend.model.dto.space.SpaceQueryRequest;
import hokumei.sys.picture.backend.model.entity.SpaceUser;
import hokumei.sys.picture.backend.model.entity.User;
import hokumei.sys.picture.backend.model.enums.SpaceLevelEnum;
import hokumei.sys.picture.backend.model.enums.SpaceRoleEnum;
import hokumei.sys.picture.backend.model.enums.SpaceTypeEnum;
import hokumei.sys.picture.backend.model.vo.SpaceVO;
import hokumei.sys.picture.backend.model.vo.UserVO;
import hokumei.sys.picture.backend.service.SpaceService;
import hokumei.sys.picture.backend.mapper.SpaceMapper;
import hokumei.sys.picture.backend.service.SpaceUserService;
import hokumei.sys.picture.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 17685
 * @description 针对表【space(空间表)】的数据库操作Service实现
 * @createDate 2025-05-05 00:05:15
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
		implements SpaceService {

	@Resource
	private UserService userService;

	@Resource
	private SpaceUserService spaceUserService;

	@Resource
	private TransactionTemplate transactionTemplate;

	// 全局静态锁池，按 userId 维度加锁
	private static final Map<Long, Object> LOCK_MAP = new ConcurrentHashMap<>();

	@Override
	public Long addSpace(SpaceAddRequest addRequest, User loginUser) {
		//1 填充参数默认值
		Space space = new Space();
		BeanUtils.copyProperties(addRequest, space);
		if (StrUtil.isBlank(space.getSpaceName())) {
			space.setSpaceName("默认空间");
		}
		if (space.getSpaceLevel() == null) {
			space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
		}
		if (space.getSpaceType() == null) {
			space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
		}
		this.fillSpaceBySpaceLevel(space);
		//2 校验参数
		this.validSpace(space, true);
		//3 校验权限 非管理员只能创建普通空间
		Long userId = loginUser.getId();
		space.setUserId(userId);
		if ((space.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		//4 同一个用户只能创建一个空间
		QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();
		spaceQueryWrapper.eq("userId", userId);
		//java8 将字符串放到常量池之中
		// 获取或创建该 userId 对应的锁对象
		//自动释放
		Object lock = LOCK_MAP.computeIfAbsent(userId, k -> new Object());
		//单节点的锁
		synchronized (lock) {
			Long spaceId;
			try {
				//status 事物状态
				spaceId = transactionTemplate.execute(status -> {
					//是否已有空间
					boolean exists = this.lambdaQuery().eq(Space::getUserId, userId)
							.eq(Space::getSpaceType, space.getSpaceType()).exists();
					ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "不可以重复创建空间");
					//创建
					boolean result = this.save(space);
					ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
					// 如果是团队空间，关联新增团队成员记录
					if (SpaceTypeEnum.TEAM.getValue() == addRequest.getSpaceType()) {
						SpaceUser spaceUser = new SpaceUser();
						spaceUser.setSpaceId(space.getId());
						spaceUser.setUserId(userId);
						spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
						result = spaceUserService.save(spaceUser);
						ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
					}
					// 返回新写入的数据 id
					return space.getId();
				});
			} finally {
				LOCK_MAP.remove(userId);
			}
			return Optional.ofNullable(spaceId).orElse(-1L);
		}
		//使用Transaction
//		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//			@Override
//			public void afterCommit() {
//				// 🚀 事务提交之后才会执行这里
//				doSomethingAfterCommit();
//			}
//		});
	}

	@Override
	public void validSpace(Space space, boolean isAdd) {
		ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
		// 从对象中取值
		String spaceName = space.getSpaceName();
		Integer spaceLevel = space.getSpaceLevel();
		SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
		// 要创建
		if (isAdd) {
			if (StrUtil.isBlank(spaceName)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
			}
			if (spaceLevel == null) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
			}
		}
		// 修改数据时，如果要改空间级别
		if (spaceLevel != null && spaceLevelEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
		}
		if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
		}
	}

	@Override
	public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
		QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
		if (spaceQueryRequest == null) {
			return queryWrapper;
		}
		// 从对象中取值
		Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
		Integer spaceType = spaceQueryRequest.getSpaceType();
		String spaceName = spaceQueryRequest.getSpaceName();
		Long id = spaceQueryRequest.getId();
		String sortField = spaceQueryRequest.getSortField();
		String sortOrder = spaceQueryRequest.getSortOrder();
		Long userId = spaceQueryRequest.getUserId();
		int pageSize = spaceQueryRequest.getPageSize();

		queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
		queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
		queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
		queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
		// 排序
		queryWrapper.orderBy(StrUtil.isNotEmpty(sortOrder), sortOrder.equals("ascend"), sortField);
		return queryWrapper;
	}

	@Override
	public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
		// 对象转封装类
		SpaceVO spaceVO = SpaceVO.objToVo(space);
		// 关联查询用户信息
		Long userId = space.getUserId();
		if (userId != null && userId > 0) {
			User user = userService.getById(userId);
			UserVO userVO = userService.getUserVO(user);
			spaceVO.setUser(userVO);
		}
		return spaceVO;
	}

	@Override
	public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
		List<Space> spaceList = spacePage.getRecords();
		Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
		if (CollUtil.isEmpty(spaceList)) {
			return spaceVOPage;
		}
		// 对象列表 => 封装对象列表
		List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
		// 1. 关联查询用户信息
		Set<Long> userIdSet = spaceVOList.stream().map(SpaceVO::getUserId).collect(Collectors.toSet());
		Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 2. 填充信息
		spaceVOList.forEach(spaceVO -> {
			Long userId = spaceVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			spaceVO.setUser(userService.getUserVO(user));
		});
		spaceVOPage.setRecords(spaceVOList);
		return spaceVOPage;
	}

	@Override
	public void fillSpaceBySpaceLevel(Space space) {
		// 根据空间级别，自动填充限额
		SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
		if (spaceLevelEnum != null) {
			long maxSize = spaceLevelEnum.getMaxSize();
			if (space.getMaxSize() == null) {
				space.setMaxSize(maxSize);
			}
			long maxCount = spaceLevelEnum.getMaxCount();
			if (space.getMaxCount() == null) {
				space.setMaxCount(maxCount);
			}
		}
	}

	/**
	 * 空间权限校验
	 *
	 * @param loginUser
	 * @param space
	 */
	@Override
	public void checkSpaceAuth(User loginUser, Space space) {
		// 仅本人或管理员可访问
		if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
	}

}




