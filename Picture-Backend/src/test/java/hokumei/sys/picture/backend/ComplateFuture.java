package hokumei.sys.picture.backend;

import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ComplateFuture {

	//第一步 新建一个线程池
//	@Resource
//	private ThreadToolExecutor customExecutor

	@Test
	public void test() {
		ExecutorService treadTool = new ThreadPoolExecutor(
				4,// corePoolSize 核心线程数
				10,// maximumPoolSize 最大线程数
				60L, TimeUnit.SECONDS, // 空闲线程存活时间
				new LinkedBlockingQueue<>(100), // 任务队列
				Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.AbortPolicy() // 拒绝策略
		);

		List<Object> dataList = new ArrayList<>();

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		int batchSize = 100;
		for (int i = 0; i < dataList.size(); i += batchSize) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				System.out.println("aaa");
			});
			futures.add(future);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
	}
}
