package hokumei.sys.picture.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import hokumei.sys.picture.backend.annotation.AuthCheck;
import hokumei.sys.picture.backend.common.BaseResponse;
import hokumei.sys.picture.backend.common.ResultUtils;
import hokumei.sys.picture.backend.constant.UserConstant;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.exception.ThrowUtils;
import hokumei.sys.picture.backend.model.dto.user.*;
import hokumei.sys.picture.backend.model.entity.User;
import hokumei.sys.picture.backend.model.vo.LoginUserVO;
import hokumei.sys.picture.backend.model.vo.UserVO;
import hokumei.sys.picture.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

	@Resource
	private UserService userService;

	/**
	 * 用户注册
	 */
	@PostMapping("/register")
	public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
		ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);

		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();

		long result = userService.userRegister(userAccount, userPassword, checkPassword);
		return ResultUtils.success(result);
	}

	/**
	 * 用户登录
	 */
	@PostMapping("/login")
	public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);

		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();

		LoginUserVO loginUserVO = userService.userlogin(userAccount, userPassword, request);
		return ResultUtils.success(loginUserVO);
	}


	@PostMapping("/getAllUsers")
	public List<User> getAllUsers() {
		return userService.list();
	}

	/**
	 * 根据用户名模糊查询所有的user
	 * @param userAccount 用户名
	 * @return 用户列表
	 */
	@GetMapping("/search")
	public List<User> search(@RequestParam String userAccount,HttpServletRequest request) {
		//仅有管理员可以查询
		Object userObject = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
		User user = (User) userObject;
		if (user == null) {
			return new ArrayList<>();
		}
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.like("username",userAccount);
		return userService.list(queryWrapper);
	}

	@PostMapping("/logOut")
	public BaseResponse<Boolean> logOut(HttpServletRequest request) {
		return ResultUtils.success(userService.logOut(request));
	}

	/**
	 * 获取当前登录用户
	 */
	@GetMapping("/get/login")
	public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		return ResultUtils.success(userService.getLoginUserVO(loginUser));
	}

	/**
	 * 追加用户权限
	 * @param userAddRequest 追加请求Bean
	 * @return 用户Id
	 */
	@PostMapping("/add")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
		User user = new User();
		BeanUtils.copyProperties(userAddRequest, user);

		// 加密默认密码
		String encryptPassword = userService.getEncryptPassword(UserConstant.DEFALUT_PASSWORD);
		user.setUserPassword(encryptPassword);

		// 插入数据
		boolean result = userService.save(user);
		if (!result) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR);
		}

		return ResultUtils.success(user.getId());
	}

	/**
	 * 根据 id 获取用户（仅管理员）
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<User> getUserById(long id) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

		User user = userService.getById(id);
		ThrowUtils.throwIf(user == null, ErrorCode.SYSTEM_ERROR);

		return ResultUtils.success(user);
	}

	/**
	 * 根据 id 获取包装类
	 */
	@GetMapping("/get/vo")
	public BaseResponse<UserVO> getUserVOById(long id) {
		BaseResponse<User> response = getUserById(id);
		User user = response.getData();

		return ResultUtils.success(userService.getUserVO(user));
	}


	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
		if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User user = new User();
		BeanUtils.copyProperties(userUpdateRequest, user);
		boolean result = userService.updateById(user);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	@PostMapping("/list/page/vo")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<UserVO>> ListUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
		ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
		long current = userQueryRequest.getCurrent();
		long pageSize = userQueryRequest.getPageSize();
		Page<User> userPage = userService.page(new Page<>(current, pageSize),
				userService.getQueryWrapper(userQueryRequest));
		List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
		Page<UserVO> userPageList = new Page<>(current, pageSize, userPage.getTotal());
		userPageList.setRecords(userVOList);
		return ResultUtils.success(userPageList);
	}

	@PostMapping("/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	@Transactional(rollbackFor = Exception.class)
	public BaseResponse<Boolean> deleteBtId(@RequestBody UserDeleteRequest userDeleteRequest) {
		ThrowUtils.throwIf(userDeleteRequest == null, ErrorCode.PARAMS_ERROR);
		String id = userDeleteRequest.getId();
	    boolean result = userService.removeById(id);
		return ResultUtils.success(result);
	}
}

