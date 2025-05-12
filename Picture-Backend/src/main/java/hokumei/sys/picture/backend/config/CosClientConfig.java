package hokumei.sys.picture.backend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云存储桶
 */
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

	/**
	 * 域名
	 */
	private String host;

	/**
	 * secretId
	 */
	private String secretId;

	/**
	 * 密钥 (注意不要泄露)
	 */
	private String secretKey;

	/**
	 * 区域
	 */
	private String region;

	/**
	 * 桶名
	 */
	private String bucket;

	@Bean
	public COSClient getCOSClient() {
		// 1 传入获取到的临时密钥 (tmpSecretId, tmpSecretKey, sessionToken)
		String sessionToken = "TOKEN";
		BasicSessionCredentials cred = new BasicSessionCredentials(secretId, secretKey, sessionToken);
		// 2 设置 bucket 的地域
		// clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分
		//COS_REGION 参数：配置成存储桶 bucket 的实际地域，例如 ap-beijing，更多 COS 地域的简称请参见 https://cloud.tencent.com/document/product/436/6224
		ClientConfig clientConfig = new ClientConfig(new Region(region));
		// 3 生成 cos 客户端
		return new COSClient(cred, clientConfig);
	}
}
