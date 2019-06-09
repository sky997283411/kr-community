package com.kr.community.common.ueditor;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
public class BinaryUploader {
    static Logger logger = LoggerFactory.getLogger(BinaryUploader.class);

	public static final State save(HttpServletRequest request, Map<String, Object> conf) {
		
		boolean isAjaxUpload = request.getHeader( "X_Requested_With" ) != null;

		if (!ServletFileUpload.isMultipartContent(request)) {
			return new BaseState(false, AppInfo.NOT_MULTIPART_CONTENT);
		}
		
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

        if ( isAjaxUpload ) {
            upload.setHeaderEncoding( "UTF-8" );
        }

		try {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multipartRequest.getFile("upfile");

			String savePath = (String) conf.get("savePath");
			String urlPathPrefix = (String) conf.get("fileUrlPathPrefix");;
			String originFileName = file.getOriginalFilename();
			String suffix = FileType.getSuffixByFilename(originFileName);
			String uuid = UUID.randomUUID().toString().replaceAll("-","");

			originFileName = originFileName.substring(0, originFileName.length() - suffix.length());
			savePath = savePath + uuid+ suffix;
			File saveImage = new File(savePath);
			file.transferTo(saveImage);
			String size =String.valueOf(file.getSize());
			String group = "uplaodImage";
			String urlPath = urlPathPrefix + uuid + suffix;
			
			long maxSize = ((Long) conf.get("maxSize")).longValue();

			if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
				return new BaseState(false, AppInfo.NOT_ALLOW_FILE_TYPE);
			}

			State storageState  = new BaseState();
			storageState.putInfo("group",group);
			storageState.putInfo("original",originFileName);
			storageState.putInfo("size",size);
			storageState.putInfo("state","SUCCESS");
			storageState.putInfo("title","iimage");
			storageState.putInfo("type",suffix);
			storageState.putInfo("url",urlPath);

			return storageState;
		} catch (Exception e) {
			return new BaseState(false, AppInfo.PARSE_REQUEST_ERROR);
		}
	}

	private static boolean validType(String type, String[] allowTypes) {
		List<String> list = Arrays.asList(allowTypes);

		return list.contains(type);
	}
}
