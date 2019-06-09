package com.kr.community.common.ueditor;


import javax.servlet.http.HttpServletRequest;
import java.util.Map;


public class Uploader {
	private HttpServletRequest request = null;
	private Map<String, Object> conf = null;

	public Uploader(HttpServletRequest request, Map<String, Object> conf) {
		this.request = request;
		this.conf = conf;
	}

	public final State doExec() {
		String filedName = (String) this.conf.get("fieldName");
		State state = null;

		state = BinaryUploader.save(this.request, this.conf);


		return state;
	}
}
