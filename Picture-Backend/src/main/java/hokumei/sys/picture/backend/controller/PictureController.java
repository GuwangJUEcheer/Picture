package hokumei.sys.picture.backend.controller;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import hokumei.sys.picture.backend.annotation.AuthCheck;
import hokumei.sys.picture.backend.api.imageSearch.ImageSearchApiFacade;
import hokumei.sys.picture.backend.api.model.ImageSearchResult;
import hokumei.sys.picture.backend.common.BaseResponse;
import hokumei.sys.picture.backend.common.DeleteRequest;
import hokumei.sys.picture.backend.common.ResultUtils;
import hokumei.sys.picture.backend.constant.UserConstant;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.exception.ThrowUtils;
import hokumei.sys.picture.backend.model.dto.picture.*;
import hokumei.sys.picture.backend.model.entity.Picture;
import hokumei.sys.picture.backend.model.entity.Space;
import hokumei.sys.picture.backend.model.entity.User;
import hokumei.sys.picture.backend.model.enums.PictureReviewStatusEnum;
import hokumei.sys.picture.backend.model.vo.PictureTagCategory;
import hokumei.sys.picture.backend.model.vo.PictureVO;
import hokumei.sys.picture.backend.model.vo.UserVO;
import hokumei.sys.picture.backend.service.PictureService;
import hokumei.sys.picture.backend.service.SpaceService;
import hokumei.sys.picture.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文件上传 下载 删除管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

	@Resource
	private UserService userService;

	@Resource
	private PictureService pictureService;

	@Resource
	private SpaceService spaceService;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
			.initialCapacity(1024)//初始缓存
			.maximumSize(10_000) //最大条数 10000
			//5min之后过期
			.expireAfterWrite(Duration.ofMinutes(5))
			//手动刷新 则不要这个逻辑 否则在build里面指定逻辑
			//.refreshAfterWrite(Duration.ofMinutes(1))
			.build();

	/**
	 * 上传图片（可覆盖上传）
	 */
	@PostMapping("/upload")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<PictureVO> uploadPicture(
			@RequestPart("file") MultipartFile multipartFile,
			PictureUploadRequest pictureUploadRequest,
			HttpServletRequest request) {

		User loginUser = userService.getLoginUser(request);
		PictureVO pictureVO = pictureService.uploadPicture(pictureUploadRequest, multipartFile, loginUser);
		return ResultUtils.success(pictureVO);
	}

	/**
	 * 上传图片（可覆盖上传）
	 */
	@PostMapping("/upload/url")
	public BaseResponse<PictureVO> uploadPictureByUrl(
			@RequestBody PictureUploadRequest pictureUploadRequest,
			HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		String url = pictureUploadRequest.getUrl();
		PictureVO pictureVO = pictureService.uploadPicture(pictureUploadRequest, url, loginUser);
		return ResultUtils.success(pictureVO);
	}

	/**
	 * 更新图片（仅管理员可用）
	 */
	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
		if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureUpdateRequest, picture);
		// 注意将 list 转为 string
		picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
		// 数据校验
		pictureService.validPicture(picture);
		// 判断是否存在
		long id = pictureUpdateRequest.getId();
		Picture oldPicture = pictureService.getById(id);
		pictureService.fillReviewStatus(oldPicture, userService.getLoginUser(request));
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 操作数据库
		boolean result = pictureService.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 根据 id 获取图片（仅管理员可用）
	 */
	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Picture> getPictureById(long id) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Picture picture = pictureService.getById(id);
		ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(picture);
	}

	/**
	 * 根据 id 获取图片（封装类）
	 */
	@GetMapping("/get/vo")
	public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		Picture picture = pictureService.getById(id);
		ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
		// 空间权限校验
		Long spaceId = picture.getSpaceId();
		if (spaceId != null) {
			User loginUser = userService.getLoginUser(request);
			pictureService.checkPictureAuth(loginUser, picture);
		}
		// 获取封装类
		return ResultUtils.success(pictureService.getPictureVO(picture, request));
	}

	/**
	 * 分页获取图片列表（仅管理员可用）
	 */
	@PostMapping("/list/page")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<PictureVO>> listPictureByPageAdmin(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		User loginUser = userService.getLoginUser(request);
		UserVO loginUserVO = userService.getUserVO(loginUser);
		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		List<PictureVO> pictureVOList = picturePage.getRecords().stream().map(dvo -> {
			PictureVO pictureVO = PictureVO.objToVo(dvo);
			pictureVO.setUser(loginUserVO);
			return pictureVO;
		}).collect(Collectors.toList());
		Page<PictureVO> pictureVOPage = new Page<>();
		pictureVOPage.setRecords(pictureVOList);
		pictureVOPage.setTotal(pictureService.count());
		return ResultUtils.success(pictureVOPage);
	}

	/**
	 * 分页获取图片列表（封装类）
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
															 HttpServletRequest request) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		// 空间权限校验
		Long spaceId = pictureQueryRequest.getSpaceId();
		if (spaceId == null) {
			// 公开图库
			// 普通用户默认只能看到审核通过的数据
			pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
			pictureQueryRequest.setOnlySpaceNull(true);
		} else {
			// 私有空间
			User loginUser = userService.getLoginUser(request);
			Space space = spaceService.getById(spaceId);
			ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
			if (!loginUser.getId().equals(space.getUserId())) {
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
			}
		}

		// 获取封装类
		return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
	}

	/**
	 * 分页获取图片列表 带缓存（封装类）
	 */
	@PostMapping("/list/page/vo/cache")
	public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
																	  HttpServletRequest request) {
		//todo 使用redis进行缓存
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
		pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
		//先查询缓存 缓存没有再查询数据库
		//将查询条件转换成md5
		String queryKey = JSONUtil.toJsonStr(pictureQueryRequest);
		//获得对应的哈希值
		String queryHashKey = DigestUtil.md5Hex(queryKey.getBytes());
		String redisKey = String.format("picture:listPictureVOByPageWithCache:%s", queryHashKey);
		//操作redis从缓存中进行查询
		ValueOperations<String, String> optionsOrValues = stringRedisTemplate.opsForValue();
		String cacheValue = optionsOrValues.get(redisKey);
		if (cacheValue != null) {
			LOCAL_CACHE.put(redisKey, cacheValue);
			//命中缓存返回结果
			Page<PictureVO> pictureVOPage = JSONUtil.toBean(cacheValue, Page.class);
			return ResultUtils.success(pictureVOPage);
		}

		// 查询数据库
		Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
				pictureService.getQueryWrapper(pictureQueryRequest));
		//存入缓存
		Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
		//过期时间5分钟
		optionsOrValues.set(redisKey, JSONUtil.toJsonStr(pictureVOPage), 300, TimeUnit.SECONDS);
		LOCAL_CACHE.put(redisKey, cacheValue);
		// 获取封装类
		return ResultUtils.success(pictureVOPage);
	}

	/**
	 * 编辑图片（给用户使用）
	 */
	@PostMapping("/edit")
	public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
		if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		pictureService.editPicture(pictureEditRequest, loginUser);
		return ResultUtils.success(true);
	}

	/**
	 * 获取标签 分类 绝大部分没必要新建一个表
	 *
	 * @return 返回分类VO对象
	 */
	@GetMapping("/tag_category")
	public BaseResponse<PictureTagCategory> listPictureTagCategory() {
		PictureTagCategory pictureTagCategory = new PictureTagCategory();
		List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
		List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
		pictureTagCategory.setTagList(tagList);
		pictureTagCategory.setCategoryList(categoryList);
		return ResultUtils.success(pictureTagCategory);
	}

	/**
	 * 删除图片
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deletePictures(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		Long id = deleteRequest.getId();
		pictureService.deletePicture(id, loginUser);
		return ResultUtils.success(true);
	}

	/**
	 * 图片上传审核
	 *
	 * @param pictureReviewRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/review")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
												 HttpServletRequest request) {
		ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		pictureService.doPictureReview(pictureReviewRequest, loginUser);
		return ResultUtils.success(true);
	}

	/**
	 * 抓取图片（仅管理员可用）
	 */
	@PostMapping("/scrape")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Integer> scrapePictures(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
		User loginUser = userService.getLoginUser(request);
		Integer successCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
		return ResultUtils.success(successCount);
	}

	/**
	 * 以图搜图
	 */
	@PostMapping("/search/picture")
	public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
		ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
		Long pictureId = searchPictureByPictureRequest.getPictureId();
		ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
		Picture oldPicture = pictureService.getById(pictureId);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(oldPicture.getUrl());
		return ResultUtils.success(resultList);
	}

	@PostMapping("/search/color")
	public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
		String picColor = searchPictureByColorRequest.getPicColor();
		Long spaceId = searchPictureByColorRequest.getSpaceId();
		User loginUser = userService.getLoginUser(request);
		List<PictureVO> result = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
		return ResultUtils.success(result);
	}
}
