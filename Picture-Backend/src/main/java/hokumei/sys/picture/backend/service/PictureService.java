package hokumei.sys.picture.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import hokumei.sys.picture.backend.api.aliyun.CreateOutPaintingTaskResponse;
import hokumei.sys.picture.backend.common.BaseResponse;
import hokumei.sys.picture.backend.common.DeleteRequest;
import hokumei.sys.picture.backend.model.dto.file.UploadPictureResult;
import hokumei.sys.picture.backend.model.dto.picture.*;
import hokumei.sys.picture.backend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import hokumei.sys.picture.backend.model.entity.User;
import hokumei.sys.picture.backend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 17685
* @description 针对表【picture(图片表)】的数据库操作Service
* @createDate 2025-03-03 18:52:01
*/
public interface PictureService extends IService<Picture> {

	/**
	 * 用户上传文件
	 * @param pictureUploadRequest 请求体
	 * @param inputSource 文件名称
	 * @param user 当前登录用户
	 * @return 上传图片详细信息
	 */
	public PictureVO uploadPicture(PictureUploadRequest pictureUploadRequest, Object inputSource, User user);


	/**
	 * 校验参数
	 * @param picture 需要校验的 picture 对象
	 */
	void validPicture(Picture picture);

	/**
	 * 获取查询的 queryWrapper
	 * @param pictureQueryRequest 图片请求类
	 * @return 可用来查询的 queryWrapper
	 */
	QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

	/**
	 * 获取单个图片的 VO 对象
	 * @param picture picture 对象
	 * @param request request 请求
	 * @return 对应图片的 VO
	 */
	PictureVO getPictureVO(Picture picture, HttpServletRequest request);

	/**
	 * 分页获取图片 VO 对象
	 * @param picturePage  page 对象
	 * @param request request 请求
	 * @return 分页的 VO
	 */
	Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

	void doPictureReview(PictureReviewRequest pictureReviewRequest,User loginUser);

	void fillReviewStatus(Picture picture, User loginUser);

	/**
	 * 批量抓取和创建图片
	 *
	 * @param pictureUploadByBatchRequest
	 * @param loginUser
	 * @return 成功创建的图片数
	 */
	Integer uploadPictureByBatch(
			PictureUploadByBatchRequest pictureUploadByBatchRequest,
			User loginUser
	);

	void clearPicture(Picture oldPicture);

	/**
	 * 校验当前用户权限
	 * @param loginUser 登录用户
	 * @param picture 图片
	 */
	void checkPictureAuth(User loginUser,Picture picture);

	void deletePicture(long pictureId, User loginUser);

	void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

	List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

	CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
