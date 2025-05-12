package hokumei.sys.picture.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import hokumei.sys.picture.backend.constant.UserConstant;
import hokumei.sys.picture.backend.model.dto.user.UserQueryRequest;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.model.entity.User;
import hokumei.sys.picture.backend.mapper.UserMapper;
import hokumei.sys.picture.backend.model.enums.UserRoleEnum;
import hokumei.sys.picture.backend.model.vo.LoginUserVO;
import hokumei.sys.picture.backend.model.vo.UserVO;
import hokumei.sys.picture.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
* @author 17685
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-02-21 14:08:40
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {
	/**
	 * 盐值
	 */
	private static final String salt = "test01";

	private UserMapper userMapper;

	public UserServiceImpl(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	@Override
	public boolean save(User entity) {
		return super.save(entity);
	}

	@Override
	public long userRegister(String userAccount, String userPassword, String checkPassword) {
		// 1. 校验账号、密码是否为空
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"表单必须项目为空"); // 空值
		}
		// 账号长度不符合
		// 2. 校验账号长度
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
		}

		// 3. 校验密码长度
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");// 密码长度不符合
		}

		// 4. 校验账号是否包含特殊字符
		String validPattern = "^[a-zA-Z0-9_]+$"; // 仅允许字母、数字、下划线
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if (!matcher.matches()) {
			return -1; // 包含特殊字符
		}

		// 5. 校验密码是否一致
		if (!userPassword.equals(checkPassword)) {
			return -1; // 密码不一致
		}


		// 6. 检查账号是否重复
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("username", userAccount);
		long count = this.count(queryWrapper);
		if (count > 0) {
			return -1; // 账号已存在
		}

		// 7. 对密码进行加密后保存到数据库 盐值 salt
		String encryptedPassword = this.getEncryptPassword(userPassword);
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encryptedPassword);
		user.setUserName("无名");
		user.setUserRole(UserRoleEnum.USER.getValue());
		boolean saveResult = this.save(user);
		if (!saveResult) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
		}
		return user.getId();
	}

	@Override
	public LoginUserVO userlogin(String userAccount, String userPassword, HttpServletRequest request) {
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号错误");
		}
		if (userPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
		}

		// 2. 对用户传递的密码进行加密
		String encryptPassword = getEncryptPassword(userPassword);

		// 3. 查询数据库中的用户是否存在
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		queryWrapper.eq("userPassword", encryptPassword);
		User user = this.baseMapper.selectOne(queryWrapper);

		// 不存在，抛异常
		if (user == null) {
			log.info("user login failed, userAccount cannot match userPassword");
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
		}
		// 4. 保存用户的登录态
		request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
		return this.getLoginUserVO(user);
	}


	@Override
	public boolean logOut(HttpServletRequest request) {
		request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
		return true;
	}


	@Override
	public User getLoginUser(HttpServletRequest request) {
		// 判断是否已经登录
		Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
		User currentUser = (User) userObj;
		if (currentUser == null || currentUser.getId() == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}

		// 从数据库中查询（追求性能的情况下，直接返回上述结果）
		Long userId = currentUser.getId();
		currentUser = this.getById(userId);
		if (currentUser == null) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}

		return currentUser;
	}

	@Override
	public UserVO getUserVO(User user) {
		if (user == null) {
			return null;
		}
		UserVO userVO = new UserVO();
		BeanUtil.copyProperties(user, userVO);
		return userVO;
	}

	@Override
	public List<UserVO> getUserVOList(List<User> userList) {
		if (CollUtil.isEmpty(userList)) {
			return new ArrayList<>();
		}
		return userList.stream()
				.map(user -> getUserVO(user))
				.collect(Collectors.toList());
	}

	/**
	 * 获取加密后的密码
	 *
	 * @param userPassword 用户密码
	 * @return 加密后的密码
	 */
	@Override
	public  String getEncryptPassword(String userPassword) {
		// 加盐，混淆密码
		final String SALT = "encrypt";
		return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
	}

	@Override
	public LoginUserVO getLoginUserVO(User user) {
	  LoginUserVO loginUserVO = new LoginUserVO();
	  BeanUtils.copyProperties(user,loginUserVO);
      return loginUserVO;
	}

	@Override
	public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
		if (userQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
		}

		Long id = userQueryRequest.getId();
		String userName = userQueryRequest.getUserName();
		String userAccount = userQueryRequest.getUserAccount();
		String userProfile = userQueryRequest.getUserProfile();
		String userRole = userQueryRequest.getUserRole();
		int current = userQueryRequest.getCurrent();
		int pageSize = userQueryRequest.getPageSize();
		String sortField = userQueryRequest.getSortField();
		String sortOrder = userQueryRequest.getSortOrder();

		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
		queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
		queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
		queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
		queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
		queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);

		return queryWrapper;
	}

	@Override
	public boolean isAdmin(User user) {
		return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
	}
}




