package hokumei.sys.picture.backend.controller;

import hokumei.sys.picture.backend.common.BaseResponse;
import hokumei.sys.picture.backend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查
 */
@RestController
public class HealthyController {

	/**
	 * 健康检查
	 */
	@GetMapping("/health")
	public BaseResponse<String> health() {
		return ResultUtils.success("ok");
	}
}
