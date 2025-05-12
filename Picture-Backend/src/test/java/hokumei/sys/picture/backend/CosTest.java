package hokumei.sys.picture.backend;

import com.qcloud.cos.model.COSObjectSummary;
import hokumei.sys.picture.backend.manager.CosManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class CosTest {

	@Resource
	private CosManager cosManager;
	@Test
	public void test() {
		List<COSObjectSummary> cosManagerList = cosManager.getList("test", 1000);
		for (COSObjectSummary cosObjectSummary : cosManagerList) {
			System.out.println(cosObjectSummary.getBucketName());
			System.out.println(cosObjectSummary.getKey());
		}
	}
}
