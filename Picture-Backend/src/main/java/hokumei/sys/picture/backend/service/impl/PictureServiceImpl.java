package hokumei.sys.picture.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.exception.ThrowUtils;
import hokumei.sys.picture.backend.manager.CosManager;
import hokumei.sys.picture.backend.manager.upload.FilePictureUpload;
import hokumei.sys.picture.backend.manager.upload.PictureUploadTemplate;
import hokumei.sys.picture.backend.manager.upload.UrlPictureUpload;
import hokumei.sys.picture.backend.model.dto.file.UploadPictureResult;
import hokumei.sys.picture.backend.model.dto.picture.*;
import hokumei.sys.picture.backend.model.entity.Picture;
import hokumei.sys.picture.backend.model.entity.Space;
import hokumei.sys.picture.backend.model.entity.User;
import hokumei.sys.picture.backend.model.enums.PictureReviewStatusEnum;
import hokumei.sys.picture.backend.model.vo.PictureVO;
import hokumei.sys.picture.backend.model.vo.UserVO;
import hokumei.sys.picture.backend.service.PictureService;
import hokumei.sys.picture.backend.mapper.PictureMapper;
import hokumei.sys.picture.backend.service.SpaceService;
import hokumei.sys.picture.backend.service.UserService;
import hokumei.sys.picture.backend.utils.ColorSimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.transaction.Transaction;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 17685
* @description 针对表【picture(图片表)】的数据库操作Service实现
* @createDate 2025-03-03 18:52:01
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

	@Resource
	private UserService userService;

	@Resource
	private FilePictureUpload filePictureUpload;

	@Resource
	private UrlPictureUpload urlPictureUpload;

	@Resource
	private CosManager cosManager;

	@Resource
	private SpaceService spaceService;

	@Resource
	private TransactionTemplate transactionTemplate;

	@Override
	public PictureVO uploadPicture(PictureUploadRequest pictureUploadRequest, Object inputSource, User loginUser) {
		//校验参数
		ThrowUtils.throwIf(loginUser==null, ErrorCode.NOT_LOGIN_ERROR);
        // 校验空间是否存在
		Long spaceId = pictureUploadRequest.getSpaceId();
		if (spaceId != null) {
			Space space = spaceService.getById(spaceId);
			ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
			// 必须空间创建人（管理员）才能上传
			if (!loginUser.getId().equals(space.getUserId())) {
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
			}
			if(space.getTotalCount() >= space.getMaxCount()){
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "空间不足");
			}
			if(space.getTotalSize() >= space.getMaxSize()){
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "空间不足");
			}
		}
		//判断更新还是删除
		Long pictureId = null;
		if(pictureUploadRequest!=null){
			pictureId = pictureUploadRequest.getId();
		}
		//如果是更新查看 看图片是否存在
		if(pictureId!=null && pictureId >0){
			Picture oldPicture = this.getById(pictureId);
			ThrowUtils.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR,"图片不存在");
			Long oldSpaceId = oldPicture.getSpaceId();
			if(spaceId == null){
				if(oldSpaceId != null){
					spaceId = oldSpaceId;
				}
			}else{
				ThrowUtils.throwIf(!spaceId.equals(oldSpaceId), ErrorCode.PARAMS_ERROR,"上传空间变化");
			}
		}
		// 上传图片，得到图片信息
		// 按照用户 id 划分目录 => 按照空间划分目录
		String uploadPrefix;
		if (spaceId == null) {
			// 公共图库
			uploadPrefix = String.format("public/%s", loginUser.getId());
		} else {
			// 空间
			uploadPrefix = String.format("space/%s", spaceId);
		}

		PictureUploadTemplate uploadTemplate = filePictureUpload;
		if(inputSource instanceof String){
			uploadTemplate = urlPictureUpload;
		}
		UploadPictureResult uploadPictureResult = uploadTemplate.uploadPicture(inputSource, uploadPrefix);
		//构造要入库的图片信息
		Picture picture = new Picture();
		picture.setUrl(uploadPictureResult.getUrl());
		picture.setName(uploadPictureResult.getName());
		if(pictureUploadRequest!=null && !StringUtil.isBlank(pictureUploadRequest.getPicName())){
			picture.setName(pictureUploadRequest.getPicName());
		}
		picture.setPicSize(uploadPictureResult.getPicSize());
		picture.setPicWidth(uploadPictureResult.getPicWidth());
		picture.setPicHeight(uploadPictureResult.getPicHeight());
		picture.setPicScale(uploadPictureResult.getPicScale());
		picture.setPicFormat(uploadPictureResult.getPicFormat());
		picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
		picture.setSpaceId(spaceId);
		picture.setUserId(loginUser.getId());
		this.fillReviewStatus(picture,loginUser);
		//操作数据库
		//PictureId不是空 则更新
		if(pictureId!=null && pictureId>0){
			picture.setId(pictureId);
			picture.setEditTime(new Date());
		}
		Long finalSpaceId = spaceId;
		transactionTemplate.execute(status -> {
			boolean result = this.saveOrUpdate(picture);
			ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"数据操作失败");
			if(finalSpaceId!=null && finalSpaceId>0){
				boolean updateResult = spaceService.lambdaUpdate().eq(Space::getId, finalSpaceId).setSql(
						"totalSize = totalSize + "  + picture.getPicSize()
				).setSql("totalCount = totalCount + 1").update();
				ThrowUtils.throwIf(!updateResult,ErrorCode.OPERATION_ERROR,"数据操作失败");
			}
			return picture;
		});

		return PictureVO.objToVo(picture);
	}

	@Override
	public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
		QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
		if (pictureQueryRequest == null) {
			return queryWrapper;
		}
		// 从对象中取值
		Long id = pictureQueryRequest.getId();
		String name = pictureQueryRequest.getName();
		String introduction = pictureQueryRequest.getIntroduction();
		String category = pictureQueryRequest.getCategory();
		List<String> tags = pictureQueryRequest.getTags();
		Long picSize = pictureQueryRequest.getPicSize();
		Integer picWidth = pictureQueryRequest.getPicWidth();
		Integer picHeight = pictureQueryRequest.getPicHeight();
		Double picScale = pictureQueryRequest.getPicScale();
		String picFormat = pictureQueryRequest.getPicFormat();
		String searchText = pictureQueryRequest.getSearchText();
		Long userId = pictureQueryRequest.getUserId();
		String sortField = pictureQueryRequest.getSortField();
		String sortOrder = pictureQueryRequest.getSortOrder();
		Integer reviewStatus = pictureQueryRequest.getReviewStatus();
		String reviewMessage = pictureQueryRequest.getReviewMessage();
		Long reviewerId = pictureQueryRequest.getReviewerId();
		Long spaceId = pictureQueryRequest.getSpaceId();
		Boolean onlySpaceNull = pictureQueryRequest.getOnlySpaceNull();
		Date reviewTime = pictureQueryRequest.getReviewTime();
		Date startEditTime = pictureQueryRequest.getStartEditTime();
		Date endEditTime = pictureQueryRequest.getEndEditTime();

		// 从多字段中搜索
		if (StrUtil.isNotBlank(searchText)) {
			// 需要拼接查询条件
			queryWrapper.and(qw -> qw.like("name", searchText)
					.or()
					.like("introduction", searchText)
			);
		}
		queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
		queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId",spaceId);
		queryWrapper.isNull(onlySpaceNull, "spaceId");
		queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
		queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
		queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
		queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
		queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
		queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
		queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
		queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
		queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
		queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
		queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
		queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus) && reviewStatus>=0, "reviewStatus", reviewStatus);
		// JSON 数组查询
		if (CollUtil.isNotEmpty(tags)) {
			for (String tag : tags) {
				queryWrapper.like("tags", "\"" + tag + "\"");
			}
		}
		// 排序
		queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
		return queryWrapper;
	}

	@Override
	public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
		// 对象转封装类
		PictureVO pictureVO = PictureVO.objToVo(picture);
		// 关联查询用户信息
		Long userId = picture.getUserId();
		if (userId != null && userId > 0) {
			User user = userService.getById(userId);
			UserVO userVO = userService.getUserVO(user);
			pictureVO.setUser(userVO);
		}
		return pictureVO;
	}

	/**
	 * 分页获取图片封装
	 */
	@Override
	public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
		List<Picture> pictureList = picturePage.getRecords();
		Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
		if (CollUtil.isEmpty(pictureList)) {
			return pictureVOPage;
		}
		// 对象列表 => 封装对象列表
		List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
		// 1. 关联查询用户信息
		Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
		Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
				.collect(Collectors.groupingBy(User::getId));
		// 2. 填充信息
		pictureVOList.forEach(pictureVO -> {
			Long userId = pictureVO.getUserId();
			User user = null;
			if (userIdUserListMap.containsKey(userId)) {
				user = userIdUserListMap.get(userId).get(0);
			}
			pictureVO.setUser(userService.getUserVO(user));
		});
		pictureVOPage.setRecords(pictureVOList);
		return pictureVOPage;
	}

	@Override
	public void validPicture(Picture picture) {
		ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
		// 从对象中取值
		Long id = picture.getId();
		String url = picture.getUrl();
		String introduction = picture.getIntroduction();
		// 修改数据时，id 不能为空，有参数则校验
		ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
		if (StrUtil.isNotBlank(url)) {
			ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
		}
		if (StrUtil.isNotBlank(introduction)) {
			ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
		}
	}

	@Override
	public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
		//1 校验参数
		ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
		Long id = pictureReviewRequest.getId();
		Integer reviewStatus = pictureReviewRequest.getReviewStatus();
		String reviewMessage = pictureReviewRequest.getReviewMessage();
		if(id == null || PictureReviewStatusEnum.REVIEWING.equals(PictureReviewStatusEnum.getEnumByValue(reviewStatus))
				|| StringUtils.isBlank(reviewMessage)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		//2 判断图片是否存在
		Picture oldPicture = this.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR);

		//3 校验图片状态是否是已经审核
		ThrowUtils.throwIf(oldPicture.getReviewStatus()!=PictureReviewStatusEnum.REVIEWING.getValue(), ErrorCode.PARAMS_ERROR);
		//4 数据库进行操作
		//MybatisPlus框架 有字段的都会被更新 所以只更新像更新的字段的话 就需要只把对应字段传值
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureReviewRequest, picture);
		picture.setReviewerId(loginUser.getId());
		picture.setReviewTime(new Date());
		picture.setId(picture.getId());
		boolean result = this.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
	}

	/**
	 * 自动装填图片状态 上传编辑之后 待审核
	 */
	@Override
	public void fillReviewStatus(Picture picture, User loginUser){
		//if admin
       if(userService.isAdmin(loginUser)) {
		   picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
		   picture.setReviewerId(loginUser.getId());
		   picture.setReviewMessage("auto pass by admin");
		   picture.setReviewTime(new Date());
	   }else{
		   //normal user
		   picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
	   }
	}

	@Override
	public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
		//校验内容
		String searchText = pictureUploadByBatchRequest.getSearchText();
		Integer count = pictureUploadByBatchRequest.getCount();
		ThrowUtils.throwIf(count>30,ErrorCode.PARAMS_ERROR,"抓取图片超过30条");
		String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
		if(StrUtil.isBlank(namePrefix)){
			namePrefix = searchText;
		}
		//抓取内容
		// 要抓取的地址
		String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
		Document document;
		try {
			document = Jsoup.connect(fetchUrl).get();
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR,"页面获取参数异常");
		}
		//获得参数
		Element div = document.getElementsByClass("dgControl").first();
		if(ObjUtil.isNull(div)) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取元素失败");
		}
        Elements elements = div.select("img.mimg");
		int uploadSuccessCount = 0;
		//遍历图片
		for(Element element : elements) {
			String fileUrl = element.attr("src");
			if(StrUtil.isBlank(fileUrl)){
				log.info(String.format("当前链接失效 %s",fileUrl));
				continue;
			}
			//处理地址
			int questionMarkIndex = fileUrl.indexOf("?");
			if(questionMarkIndex > -1){
				fileUrl = fileUrl.substring(0, questionMarkIndex);
			}
			try{
				//上传图片
				PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
				pictureUploadRequest.setUrl(fileUrl);
				//没有的话 按照搜索关键词进行检索
				pictureUploadRequest.setPicName(namePrefix + (uploadSuccessCount + 1));
				PictureVO pictureVO = this.uploadPicture(pictureUploadRequest,fileUrl,loginUser);
				uploadSuccessCount++;
				log.info("上传成功 id={}",pictureVO.getId());
			}catch (Exception e){
				log.error("图片上传失败",e);
				continue;
			}
			if(uploadSuccessCount>=count){
				break;
			}
		}
		return uploadSuccessCount;
	}

	@Async
	@Override
	public void clearPicture(Picture oldPicture) {
		if(oldPicture == null){
			return;
		}
		String url = oldPicture.getUrl();
		//判断图片是否被多次引用
		long count = this.lambdaQuery().eq(Picture::getUrl,url).count();
		if(count > 1){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		cosManager.deleteObject(url);
		String thumbNailPicUrl  =  oldPicture.getThumbnailUrl();
		if(StrUtil.isNotBlank(thumbNailPicUrl)){
			cosManager.deleteObject(thumbNailPicUrl);
		}
	}

	@Override
	public void checkPictureAuth(User loginUser, Picture picture) {
		Long spaceId = picture.getSpaceId();
		Long userId = loginUser.getId();
		//公共图库
		if(spaceId == null) {
			if(!userService.isAdmin(loginUser) && !userId.equals(picture.getUserId())) {
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有权限");
			}
		}else{
			//私有空间 仅空间管理员可以进行操作
			if(!userId.equals(picture.getUserId())){
				throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有权限");
			}
		}
	}

	@Override
	public void deletePicture(long pictureId, User loginUser) {
		ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		// 判断是否存在
		Picture oldPicture = this.getById(pictureId);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 校验权限
		checkPictureAuth(loginUser, oldPicture);
		//开启事务
		transactionTemplate.execute(status -> {
			// 操作数据库
			boolean result = this.removeById(pictureId);
			ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
			boolean updateResult = spaceService.lambdaUpdate().eq(Space::getId, oldPicture.getSpaceId()).setSql(
					"totalSize = totalSize - "  + oldPicture.getPicSize()
			).setSql("totalCount = totalCount - 1").update();
			ThrowUtils.throwIf(!updateResult,ErrorCode.OPERATION_ERROR,"数据操作失败");
			return true;
		});
		// 异步清理文件
		this.clearPicture(oldPicture);
	}

	@Override
	public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
		// 在此处将实体类和 DTO 进行转换
		Picture picture = new Picture();
		BeanUtils.copyProperties(pictureEditRequest, picture);
		// 注意将 list 转为 string
		picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
		// 设置编辑时间
		picture.setEditTime(new Date());
		// 数据校验
		this.validPicture(picture);
		// 判断是否存在
		long id = pictureEditRequest.getId();
		Picture oldPicture = this.getById(id);
		ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
		// 校验权限
		checkPictureAuth(loginUser, oldPicture);
		// 补充审核参数
		this.fillReviewStatus(picture, loginUser);
		// 操作数据库
		boolean result = this.updateById(picture);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
	}


	@Override
	public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
		// 1. 校验参数
		ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
		// 2. 校验空间权限
		Space space = spaceService.getById(spaceId);
		ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
		if (!loginUser.getId().equals(space.getUserId())) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
		}
		// 3. 查询该空间下所有图片（必须有主色调）
		List<Picture> pictureList = this.lambdaQuery()
				.eq(Picture::getSpaceId, spaceId)
				.isNotNull(Picture::getPicColor)
				.list();
		// 如果没有图片，直接返回空列表
		if (CollUtil.isEmpty(pictureList)) {
			return Collections.emptyList();
		}
		// 将目标颜色转为 Color 对象
		Color targetColor = Color.decode(picColor);
		// 4. 计算相似度并排序
		List<Picture> sortedPictures = pictureList.stream()
				.sorted(Comparator.comparingDouble(picture -> {
					// 提取图片主色调
					String hexColor = picture.getPicColor();
					// 没有主色调的图片放到最后
					if (StrUtil.isBlank(hexColor)) {
						return Double.MAX_VALUE;
					}
					Color pictureColor = Color.decode(hexColor);
					// 越大越相似
					return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
				}))
				// 取前 12 个
				.limit(12)
				.collect(Collectors.toList());

		// 转换为 PictureVO
		return sortedPictures.stream()
				.map(PictureVO::objToVo)
				.collect(Collectors.toList());
	}
}




