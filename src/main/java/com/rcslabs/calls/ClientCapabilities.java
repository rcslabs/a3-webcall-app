package com.rcslabs.calls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientCapabilities {

	private Map<String, Object> cc;
	private String userAgent;
	private String profile;
	private boolean ice;
	private boolean ssrcRequired;
	private List<String> audio;
	private List<String> video;
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public void setIce(boolean ice) {
		this.ice = ice;
	}

	public void setSsrcRequired(boolean ssrcRequired) {
		this.ssrcRequired = ssrcRequired;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getProfile() {
		return profile;
	}

	public boolean isIce() {
		return ice;
	}

	public boolean isSsrcRequired() {
		return ssrcRequired;
	}

	public List<String> getAudio() {
		return audio;
	}

	public List<String> getVideo() {
		return video;
	}
	
	@SuppressWarnings("unchecked")
	public ClientCapabilities(Map<String, Object> capabilities) {
		super();
		cc = capabilities;		
		userAgent = (String)cc.get("userAgent");
		profile = (String)cc.get("profile");
		ice = (null != cc.get("ice") ? (Boolean)cc.get("ice") : false);
		ssrcRequired = (null != cc.get("ssrcRequired") ? (Boolean)cc.get("ssrcRequired") : false);
		audio = (List<String>)cc.get("audio");
		video = (List<String>)cc.get("video");
	}
	
	@SuppressWarnings("unchecked")
	public ClientCapabilities(Object capabilities) {
		this((Map<String, Object>)capabilities);
	}
	
	public ClientCapabilities() {
		super();
		cc = new HashMap<String, Object>();
		audio = new ArrayList<String>();
		video = new ArrayList<String>();		
		userAgent = null;
		profile = null;
		ice = false;
		ssrcRequired = false;
	}	
	
	public void addAudio(String codec) {
		audio.add(codec);
	}

	public void addVideo(String codec) {
		video.add(codec);
	}	

	public boolean isWrtc(){
		return ("RTP/SAVPF".equals(profile));
	}
	
	public boolean isSip(){
		return ("RTP/AVP".equals(profile));
	}	
	
	public Map<String, Object> getRawData() {
		if(null != userAgent) cc.put("userAgent", userAgent);
		if(null != profile)   cc.put("profile", profile);
		cc.put("ice", ice);
		if(null != audio && 0 != audio.size()) cc.put("audio", audio);
		if(null != video && 0 != video.size()) cc.put("video", video);
		return cc;
	}
	
}
