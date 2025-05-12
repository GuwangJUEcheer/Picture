package hokumei.sys.picture.backend.manager;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import hokumei.sys.picture.backend.common.DeleteRequest;
import hokumei.sys.picture.backend.config.CosClientConfig;
import hokumei.sys.picture.backend.exception.BusinessException;
import hokumei.sys.picture.backend.exception.ErrorCode;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传 下载管理类 和业务逻辑无关
 */
@Component
public class CosManager {

	private static final String uploadRule = "imageInfo";

	@Resource
	private CosClientConfig cosClientConfig;

	@Resource
	private COSClient cosClient;

	/**
	 *
	 * @param key 指定文件上传到 COS 上的路径，即对象键。例如对象键为 folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
	 * @param file 实际上上传的文件
	 */
	public void putObject(String key, File file) {
		String bucketName = cosClientConfig.getBucket();
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key,file);
		PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
	}


	/**
	 * 图片列表
	 * @param prefix 前缀 什么文件夹下面的文件
	 * @param maxKeys 最大的条数
	 * @return 存储对象 key value List
	 */
	public List<COSObjectSummary> getList(String prefix,int maxKeys) {
		if(maxKeys > 10000){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
		// 设置 bucket 名称
		listObjectsRequest.setBucketName(cosClientConfig.getBucket());
         // prefix 表示列出的 object 的 key 以 prefix 开始
		listObjectsRequest.setPrefix(String.format("%s/",prefix));
        // deliter 表示分隔符, 设置为/表示列出当前目录下的 object, 设置为空表示列出所有的 object
		listObjectsRequest.setDelimiter("/");
         // 设置最大遍历出多少个对象, 一次 listobject 最大支持1000
		listObjectsRequest.setMaxKeys(maxKeys);
		ObjectListing objectListing = null;
		try {
			objectListing = cosClient.listObjects(listObjectsRequest);
		} catch (Exception e) {
			 throw new CosClientException(e.getMessage());
		}
		// common prefix 表示被 delimiter 截断的路径, 如 delimter 设置为/, common prefix 则表示所有子目录的路径
		List<String> commonPrefixs = objectListing.getCommonPrefixes();
		// object summary 表示所有列出的 object 列表
		List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();
		String nextMarker = objectListing.getNextMarker();
		listObjectsRequest.setMarker(nextMarker);
		return cosObjectSummaries;
	}

	/**
	 *
	 * @param key  指定文件在 COS 上的路径，即对象键。例如对象键为 folder/picture.jpg，则表示下载的文件 picture.jpg 在 folder 路径下
	 * @return 文件对象
	 */
	public COSObject getObject(String key){
       // 方法1 获取下载输入流
		GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
		return cosClient.getObject(getObjectRequest);
	}

	/**
	 * 上传图片信息(携带信息)
	 * @param key 指定文件上传到 COS 上的路径，即对象键。例如对象键为 folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
	 * @param file 实际上上传的文件
	 */
	public PutObjectResult putObjectPicture(String key, File file,String originName) throws JsonProcessingException {
		String bucketName = cosClientConfig.getBucket();
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key,file);
		//对图片进行预处理
		PicOperations pictureOperations = new PicOperations();
		//1 代表原图信息
		pictureOperations.setIsPicInfo(1);

		//制定规则
		List<PicOperations.Rule> rules = new ArrayList<>();
		//图片压缩 指定成webp格式
		String webKey = "/" + FileUtil.mainName(originName) + ".webp";
		PicOperations.Rule newRule = new PicOperations.Rule();
		newRule.setBucket(cosClientConfig.getBucket());
		newRule.setFileId(webKey);
		newRule.setRule("imageMogr2/format/webp");
		rules.add(newRule);

		//图片自动缩略图
		PicOperations.Rule trumpNailRule = new PicOperations.Rule();
		String thumbNailKey = "/" + FileUtil.mainName(originName) + "thumbnail." + FileUtil.getSuffix(originName);
		trumpNailRule.setBucket(cosClientConfig.getBucket());
		trumpNailRule.setFileId(thumbNailKey);
		trumpNailRule.setRule("imageMogr2/thumbnail/256x256!");
		rules.add(trumpNailRule);
		pictureOperations.setRules(rules);
		//构造处理参数
		putObjectRequest.setPicOperations(pictureOperations);
		return cosClient.putObject(putObjectRequest);
	}

	public void deleteObject(String key){
		 cosClient.deleteObject(cosClientConfig.getBucket(),key);
	}
}
