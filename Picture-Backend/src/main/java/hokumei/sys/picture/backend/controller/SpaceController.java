package hokumei.sys.picture.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import hokumei.sys.picture.backend.annotation.AuthCheck;
import hokumei.sys.picture.backend.common.BaseResponse;
import hokumei.sys.picture.backend.common.DeleteRequest;
import hokumei.sys.picture.backend.common.ResultUtils;
import hokumei.sys.picture.backend.constant.UserConstant;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.exception.ThrowUtils;
import hokumei.sys.picture.backend.model.dto.space.*;
import hokumei.sys.picture.backend.model.dto.spaceAnalyze.SpaceUserAnalyzeRequest;
import hokumei.sys.picture.backend.model.dto.spaceAnalyze.SpaceUserAnalyzeResponse;
import hokumei.sys.picture.backend.model.entity.Space;
import hokumei.sys.picture.backend.model.entity.User;
import hokumei.sys.picture.backend.model.enums.SpaceLevelEnum;
import hokumei.sys.picture.backend.model.vo.SpaceVO;
import hokumei.sys.picture.backend.model.vo.UserVO;
import hokumei.sys.picture.backend.service.SpaceService;
import hokumei.sys.picture.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/space")
public class SpaceController {

	@Resource
	private UserService userService;

	@Resource
	private SpaceService spaceService;

	/**
	 * 删除空间
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		long id = deleteRequest.getId();
		// 判断是否存在
		Space oldSpace = spaceService.getById(id);
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可删除
		if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = spaceService.removeById(id);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 更新图片（仅管理员可用）
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
		if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 将实体类和 DTO 进行转换
		Space space = new Space();
		BeanUtils.copyProperties(spaceUpdateRequest, space);
		//数据自动填充(空间的大小 级别)
		spaceService.fillSpaceBySpaceLevel(space);
		// 指定是否是创建操作
		spaceService.validSpace(space,false);
		// 判断是否存在
		long id = spaceUpdateRequest.getId();
		Space oldSpace = spaceService.getById(id);
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = spaceService.updateById(space);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 根据 id 获取图片（仅管理员可用）
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Space> getSpaceById(long id) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Space space = spaceService.getById(id);
		ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(space);
	}

	/**
	 * 根据 id 获取图片（封装类）
	 */
	@GetMapping("/get/vo")
	public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Space space = spaceService.getById(id);
		ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(spaceService.getSpaceVO(space, request));
	}

	/**
	 * 分页获取图片列表（仅管理员可用）
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<SpaceVO>> listSpaceByPageAdmin(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
		long current = spaceQueryRequest.getCurrent();
		long size = spaceQueryRequest.getPageSize();
		User loginUser = userService.getLoginUser(request);
		UserVO loginUserVO = userService.getUserVO(loginUser);
		// 查询数据库
		Page<Space> spacePage = spaceService.page(new Page<>(current, size),
				spaceService.getQueryWrapper(spaceQueryRequest));
		List<SpaceVO> spaceVOList = spacePage.getRecords().stream().map(dvo->{
			SpaceVO spaceVO = SpaceVO.objToVo(dvo);
			spaceVO.setUser(loginUserVO);
			return spaceVO;
		}).collect(Collectors.toList());
		Page<SpaceVO> spaceVOPage = new Page<>();
		spaceVOPage.setRecords(spaceVOList);
		spaceVOPage.setTotal(spaceService.count());
		return ResultUtils.success(spaceVOPage);
	}

	/**
	 * 分页获取图片列表（封装类）
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
															 HttpServletRequest request) {
		long current = spaceQueryRequest.getCurrent();
		long size = spaceQueryRequest.getPageSize();
		User loginUser = userService.getLoginUser(request);
		if(!userService.isAdmin(loginUser)) {
			spaceQueryRequest.setUserId(loginUser.getId());
		}
		// 查询数据库
		Page<Space> spacePage = spaceService.page(new Page<>(current, size),
				spaceService.getQueryWrapper(spaceQueryRequest));
		// 获取封装类
		return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
	}

	/**
	 * 编辑图片（给用户使用）
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
		if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 在此处将实体类和 DTO 进行转换
		Space space = new Space();
		BeanUtils.copyProperties(spaceEditRequest, space);

		spaceService.fillSpaceBySpaceLevel(space);
		// 设置编辑时间
		space.setEditTime(new Date());
		// 数据校验
		spaceService.validSpace(space,false);
		User loginUser = userService.getLoginUser(request);
		// 判断是否存在
		long id = spaceEditRequest.getId();
		Space oldSpace = spaceService.getById(id);
		ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
		// 仅本人或管理员可编辑
		if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		// 操作数据库
		boolean result = spaceService.updateById(space);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	@PostMapping("/add")
    public BaseResponse<Long> addSpace(SpaceAddRequest addRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		long newId = spaceService.addSpace(addRequest, loginUser);
		return ResultUtils.success(newId);
	}


	@GetMapping("/list/level")
	public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
		List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()).map(spaceLevelEnum -> new SpaceLevel(spaceLevelEnum.getValue(), spaceLevelEnum.getText(),
				spaceLevelEnum.getMaxCount(), spaceLevelEnum.getMaxSize())).collect(Collectors.toList());
		return ResultUtils.success(spaceLevelList);
	}
}
