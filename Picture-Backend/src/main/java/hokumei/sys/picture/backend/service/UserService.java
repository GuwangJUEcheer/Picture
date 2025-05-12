package hokumei.sys.picture.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import hokumei.sys.picture.backend.model.dto.user.UserQueryRequest;
import hokumei.sys.picture.backend.model.entity.User;
import hokumei.sys.picture.backend.model.vo.LoginUserVO;
import hokumei.sys.picture.backend.model.vo.UserVO;

import java.util.List;
import javax.servlet.http.HttpServletRequest;


/**
* @author 17685
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-02-21 14:08:40
*/
public interface UserService extends IService<User> {

	/**
	 * 用户注册
	 *
	 * @param userAccount   用户账号
	 * @param userPassword  用户密码
	 * @param checkPassword 校验密码
	 * @return 注册结果（返回用户ID）
	 */
	long userRegister(String userAccount, String userPassword, String checkPassword);

	/**
	 * 登录方法
	 *
	 * @param userAccount  用户账号
	 * @param userPassword 用户密码
	 * @return
	 */
	LoginUserVO userlogin(String userAccount, String userPassword, HttpServletRequest request);

	boolean logOut(HttpServletRequest request);

	User getLoginUser(HttpServletRequest request);

	LoginUserVO getLoginUserVO(User user);

	/**
	 * 获取脱敏后的用户信息
	 *
	 * @param user 用户对象
	 * @return UserVO（脱敏后的用户信息）
	 */
	UserVO getUserVO(User user);

	/**
	 * 获取脱敏后的用户信息列表
	 *
	 * @param userList 用户对象列表
	 * @return List<UserVO>（脱敏后的用户信息列表）
	 */
	List<UserVO> getUserVOList(List<User> userList);

	/**
	 * 获得加密后的密码
	 * @param userPassword
	 * @return
	 */
	String getEncryptPassword(String userPassword);

	/**
	 * 拼接的用户条件
	 * @param userQueryRequest 用户查询请求参数
	 * @return
	 */
	QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

	public boolean isAdmin(User user);
}
