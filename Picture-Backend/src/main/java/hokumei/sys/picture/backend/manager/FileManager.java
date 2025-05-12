package hokumei.sys.picture.backend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import hokumei.sys.picture.backend.config.CosClientConfig;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.exception.ThrowUtils;
import hokumei.sys.picture.backend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件上传 更进一步的封装
 * 业务相关 打上Service的注解
 */
@Slf4j
@Service
@Deprecated
public class FileManager {

	@Resource
	private CosClientConfig cosClientConfig;

	@Resource
	private CosManager cosManager;

	/**
	 * 通用上传文件类
	 * @param multipartFile 文件
	 * @param folder 文件所在文件夹
	 * @return 图片详细信息
	 */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile,String folder) {
        //校验图片
		validPicture(multipartFile);
	    //图片上传地址
		String fileName = String.format("%s_%s.%s", DateUtil.format(new Date(), "yyyyMMddHHmmss"),
				RandomUtil.randomString(12), FileUtil.getSuffix(multipartFile.getOriginalFilename()));
		String filePath = String.format("%s/%s", folder, fileName);
	    //解析结果并返回
		//创建临时文件 在服务器
		File file = null;
		try {
			//新建临时文件
			file = File.createTempFile(filePath,null);
			//将文件的内容传入到临时文件中
			multipartFile.transferTo(file);
			//上传文件
			PutObjectResult putObjectResult = cosManager.putObjectPicture(filePath, file,fileName);
			//获取图片信息对象
			ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

			//计算图片比例
			int width = imageInfo.getWidth();
			int height = imageInfo.getHeight();
			double scale = NumberUtil.round((double) width /height,2).doubleValue();
			UploadPictureResult uploadPictureResult = new UploadPictureResult();
			uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + filePath);
			uploadPictureResult.setName(FileUtil.mainName(multipartFile.getOriginalFilename()));
			uploadPictureResult.setPicSize(FileUtil.size(file));
			uploadPictureResult.setPicWidth(width);
			uploadPictureResult.setPicHeight(height);
			uploadPictureResult.setPicScale(scale);
			uploadPictureResult.setPicFormat(imageInfo.getFormat());
			return uploadPictureResult;
		}catch (Exception e){
			log.error("file upload error,filepath = {}", filePath, e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
		}finally {
			this.deleteTemplateFile(file);
		}
	}

	/**
	 * todo 根据url上传文件
	 * @param fileURL 文件的URL地址
	 * @param folder 文件所在文件夹
	 * @return 图片详细信息
	 */
	public UploadPictureResult uploadPictureByURL(String fileURL,String folder) {
		//todo
		validPicture(fileURL);

		//图片上传地址
		String fileName = FileUtil.mainName(fileURL);
		String filePath = String.format("%s/%s", folder, fileName);
		//解析结果并返回
		//创建临时文件 在服务器
		File file = null;
		try {
			//新建临时文件
			file = File.createTempFile(filePath,null);
			//下载文件
			HttpUtil.downloadFile(fileURL,filePath);
			//上传文件
			PutObjectResult putObjectResult = cosManager.putObjectPicture(filePath, file,fileName+ FileUtil.getSuffix(fileURL));
			//获取图片信息对象
			ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

			//计算图片比例
			int width = imageInfo.getWidth();
			int height = imageInfo.getHeight();
			double scale = NumberUtil.round((double) width /height,2).doubleValue();
			UploadPictureResult uploadPictureResult = new UploadPictureResult();
			uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + filePath);
			uploadPictureResult.setName(fileName);
			uploadPictureResult.setPicSize(FileUtil.size(file));
			uploadPictureResult.setPicWidth(width);
			uploadPictureResult.setPicHeight(height);
			uploadPictureResult.setPicScale(scale);
			uploadPictureResult.setPicFormat(imageInfo.getFormat());
			return uploadPictureResult;
		}catch (Exception e){
			log.error("file upload error,filepath = {}", filePath, e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
		}finally {
			this.deleteTemplateFile(file);
		}
	}

	//根据URL来校验图片是否满足条件
	private void validPicture(String fileURL) {
		//非空
		ThrowUtils.throwIf(StrUtil.isBlank(fileURL),ErrorCode.PARAMS_ERROR,"文件地址为空");
		//文件路径格式
		try {
			new URL(fileURL);
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件地址格式错误");
		}
		//校验URL的协议
		ThrowUtils.throwIf(!fileURL.startsWith("http://") && !fileURL.startsWith("https://"),ErrorCode.PARAMS_ERROR);
		//发送HEAD请求
		HttpResponse httpResponse = null;
		try{
			httpResponse = HttpUtil.createRequest(Method.HEAD, fileURL).execute();
			if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
				//有的请求没有HEAD请求
				return;
			}
			//文件类型校验
			String contentType = httpResponse.header("Content-Type");
			// 不为空，才校验是否合法，这样校验规则相对宽松
			if (StrUtil.isNotBlank(contentType)) {
				// 允许的图片类型
				final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
				ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
						ErrorCode.PARAMS_ERROR, "文件类型错误");
			}
			// 文件存在，文件大小校验
			String contentLengthStr = httpResponse.header("Content-Length");
			if (StrUtil.isNotBlank(contentLengthStr)) {
				long contentLength = Long.parseLong(contentLengthStr);
				final long ONE_M = 1024 * 1024;
				ThrowUtils.throwIf(contentLength > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
			}
		}finally {
			//记得释放资源
			if (httpResponse != null) {
				httpResponse.close();
			}
		}
		
	}

	/**
	 * 校验文件 不符合要求则抛出异常
	 * @param file
	 */
	private void validPicture(MultipartFile file) {
		ThrowUtils.throwIf(file==null, ErrorCode.PARAMS_ERROR,"文件不能为空");
		//1 校验文件大小
        long fileSize = file.getSize();
		final long ONE_M = 1024 * 1024;
		//文件不能超过2MB
		ThrowUtils.throwIf(fileSize > 2*ONE_M, ErrorCode.PARAMS_ERROR,"文件不能超过2M");

		//2校验文件后缀 使用胡图工具类 获得文件后缀名
		String fileSuffiex = FileUtil.getSuffix(file.getOriginalFilename());
		//定义允许上传的文件后缀列表
        final List<String> ALLOW_SUFFIX_LIST = Arrays.asList("jpg","jpeg","png","gif","bmp","webp");
		//后缀名必须是上面的列表的一种
		ThrowUtils.throwIf(!ALLOW_SUFFIX_LIST.contains(fileSuffiex), ErrorCode.PARAMS_ERROR,"不是支持的图片类型");
	}

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
