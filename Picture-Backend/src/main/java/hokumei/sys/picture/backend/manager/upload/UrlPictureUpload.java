package hokumei.sys.picture.backend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.exception.ThrowUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
public class UrlPictureUpload extends PictureUploadTemplate {

	private final static List<String> ALLOW_FILE_TYPE = Arrays.asList("image/png", "image/webp", "image/jpeg");

	private final static List<String> ALLOW_URL_PROTOCOL = Arrays.asList("http", "https");



	@Override
	protected String getOriginFilename(Object inputSource) {
		String fileUrl = (String) inputSource;
		// 获取当前时间
		LocalDateTime now = LocalDateTime.now();

		// 定义格式
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		// 格式化时间
		String formattedDateTime = now.format(formatter);
		return FileUtil.mainName(fileUrl) + formattedDateTime + "." + FileUtil.getSuffix(fileUrl);
	}

	@Override
	protected void validPicture(Object inputSource) {
		String fileURL = (String) inputSource;
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

	@Override
	protected void processFile(Object inputSource, File file) throws Exception {
		String fileUrl = (String) inputSource;
		// 下载文件到临时目录
		HttpUtil.downloadFile(fileUrl, file);
	}

}

