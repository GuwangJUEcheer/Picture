package hokumei.sys.picture.backend.controller;

import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import hokumei.sys.picture.backend.annotation.AuthCheck;
import hokumei.sys.picture.backend.common.BaseResponse;
import hokumei.sys.picture.backend.common.ResultUtils;
import hokumei.sys.picture.backend.constant.UserConstant;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import hokumei.sys.picture.backend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * 文件上传 下载 删除管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

	@Resource
	private CosManager cosManager;

	/**
	 * 测试上传代码逻辑
	 * @param multipartFile 文件名字 地址
	 * @return 上传结果
	 */
	@PostMapping("test")
	public BaseResponse<Boolean> testUpload(@RequestPart MultipartFile multipartFile) {

		if(multipartFile == null){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		String fileName = multipartFile.getOriginalFilename();
		String filepath = String.format("/test/%s",fileName);
		//创建临时文件 在服务器
		File file = null;

		try {
			//新建临时文件
			file = File.createTempFile(filepath,null);
			//将文件的内容传入到临时文件中
			multipartFile.transferTo(file);
			//上传文件
			cosManager.putObject(filepath,file);
		}catch (Exception e){
			log.error("file upload error,filepath = {}", filepath, e);
		   throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
		}finally {
			if(file != null){
				//删除临时文件
				boolean delete = file.delete();
				if(!delete){
					log.error("临时文件上传失败");
				}
			}
		}
		return ResultUtils.success(true);
	}


	/**
	 * 测试文件下载
	 * @param filepath 文件路径
	 * @param response 响应对象
	 */
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	@GetMapping("/test/download")
	public void testDownloadFile(String filepath, HttpServletResponse response) {
		// 使用 try-with-resources 确保 COSObjectInputStream 自动关闭
		try (COSObjectInputStream cosObjectInput = cosManager.getObject(filepath).getObjectContent()) {
			// 将 InputStream 转为字节数组
			byte[] bytes = IOUtils.toByteArray(cosObjectInput);

			// 设置响应头，指定下载类型和文件名
			response.setContentType("application/octet-stream;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + filepath);

			// 写入响应输出流
			response.getOutputStream().write(bytes);
			//刷新到响应区去
			response.getOutputStream().flush();
		} catch (IOException e) {
			log.error("file download error,filepath = {}", filepath, e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"下载失败");
		}
	}
}
