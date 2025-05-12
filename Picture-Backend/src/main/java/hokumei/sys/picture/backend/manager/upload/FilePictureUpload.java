package hokumei.sys.picture.backend.manager.upload;

import cn.hutool.core.io.FileUtil;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.exception.ThrowUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/lieeew">leikooo</a>
 * @date 2024/12/21
 * @description
 */
@Component
public class FilePictureUpload extends PictureUploadTemplate {

	@Override
	protected void validPicture(Object inputSource) {
		MultipartFile multipartFile = (MultipartFile) inputSource;
		ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");

		// 1. 校验文件大小
		long fileSize = multipartFile.getSize();
		final long ONE_M = 1024 * 1024;
		ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");

		// 2. 校验文件后缀
		String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
		// 允许上传的后缀类型（或改成配置）
		final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "jpg", "webp");
		ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
	}

	@Override
	protected String getOriginFilename(Object inputSource) {
		MultipartFile multipartFile = (MultipartFile) inputSource;
		return multipartFile.getOriginalFilename();
	}

	@Override
	protected void processFile(Object inputSource, File file) throws Exception {
		MultipartFile multipartFile = (MultipartFile) inputSource;
		multipartFile.transferTo(file);
	}
}

