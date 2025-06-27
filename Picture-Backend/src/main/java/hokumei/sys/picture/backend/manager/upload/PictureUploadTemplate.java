package hokumei.sys.picture.backend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import hokumei.sys.picture.backend.config.CosClientConfig;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.manager.CosManager;
import hokumei.sys.picture.backend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;


/**
 * 模板方法 设计模式
 */
@Slf4j
public abstract class PictureUploadTemplate {

	@Resource
	private CosClientConfig cosClientConfig;

	@Resource
	private CosManager cosManager;

	/**
	 * 通用上传文件类
	 * @param inputSource 文件
	 * @param filePath 文件所在文件夹
	 * @return 图片详细信息
	 */
	public UploadPictureResult uploadPicture(Object inputSource,String filePath) {
		//校验图片
		validPicture(inputSource);
		//图片上传地址
		String fileName = getOriginFilename(inputSource);
		//解析结果并返回
		//创建临时文件 在服务器
		File file = null;
		try {
			//新建临时文件
			file = File.createTempFile(filePath,null);
			filePath = String.format("%s/%s",filePath,fileName);
			processFile(inputSource,file);
			//上传文件
			PutObjectResult putObjectResult = cosManager.putObjectPicture(filePath, file,fileName);
			//获取图片信息对象
			ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
			//拿到处理之后的结果
			ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
			//所有处理之后的图片
			List<CIObject> objectList = processResults.getObjectList();
			CIObject compressedCIObject = null;
			CIObject thumbNailObject = null;
			if(CollUtil.isNotEmpty(objectList)) {
				//获取压缩之后的图片
				compressedCIObject = objectList.get(0);
				if(objectList.size() > 1) {
					thumbNailObject	 = objectList.get(1);
				}
			}
			return buildUploadResult(fileName,compressedCIObject,thumbNailObject);
		}catch (Exception e){
			log.error("file upload error,filepath = {}", filePath, e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
		}finally {
			this.deleteTemplateFile(file);
		}
	}

	private UploadPictureResult buildUploadResult(String originalFilename,CIObject compressedCiObject,CIObject thumbnailCiObject ) {
        // 封装返回结果
		UploadPictureResult uploadPictureResult = new UploadPictureResult();
		uploadPictureResult.setName(FileUtil.mainName(originalFilename));
		if(compressedCiObject != null) {
			// 计算宽高
			int picWidth = compressedCiObject.getWidth();
			int picHeight = compressedCiObject.getHeight();
			double picScale = NumberUtil.round((picWidth * 1.0) / picHeight, 2).doubleValue();
			uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
			uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
			uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
			uploadPictureResult.setPicWidth(picWidth);
			uploadPictureResult.setPicHeight(picHeight);
			uploadPictureResult.setPicScale(picScale);
		}
		if(thumbnailCiObject != null) {
			uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" +thumbnailCiObject.getKey());
		}
		return uploadPictureResult;
	}

	private UploadPictureResult buildUploadResult(String originalFilename,File file ,String uploadPath,ImageInfo imageInfo ) {
		// 计算宽高
		int picWidth = imageInfo.getWidth();
		int picHeight = imageInfo.getHeight();
        double picSale = NumberUtil.round((picWidth * 1.0) / picHeight, 2).doubleValue();
		//分装返回结果
		UploadPictureResult uploadPictureResult = new UploadPictureResult();
		uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
		uploadPictureResult.setName(FileUtil.mainName(originalFilename));
		uploadPictureResult.setPicSize(FileUtil.size(file));
		uploadPictureResult.setPicWidth(picWidth);
		uploadPictureResult.setPicHeight(picHeight);
		uploadPictureResult.setPicScale(picSale);
		uploadPictureResult.setPicFormat(imageInfo.getFormat());
		uploadPictureResult.setPicColor(imageInfo.getAve());
		return uploadPictureResult;
	}

	protected abstract void processFile(Object inputSource, File file) throws Exception;

	protected abstract String getOriginFilename(Object inputSource);

	protected abstract void validPicture(Object inputSource);

	/**
	 * 清理临时文件
	 * 使用ctrl alt m 抽出
	 * @param file
	 */
	private void deleteTemplateFile(File file) {
		if(file != null){
			//删除临时文件
			boolean delete = file.delete();
			if(!delete){
				log.error("临时文件上传失败");
			}
		}
	}
}
