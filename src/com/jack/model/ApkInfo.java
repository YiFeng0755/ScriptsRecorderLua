package com.jack.model;

import java.io.File;

import com.jack.utils.CmdConfig;
import com.jack.utils.CmdUtil;

public class ApkInfo {
	private static String pkg;
	private static String launchActivity;

	public static void analyzeApk(String apkPath){
		String apkinfo = CmdUtil.run("aapt dump badging " + apkPath);
		pkg = CmdUtil.stringFilter.grep(apkinfo, "package:").split("'")[1].trim();
		launchActivity = CmdUtil.stringFilter.grep(apkinfo, "launchable-").split("'")[1].trim();
	}
	
	public static String getPkg(){
		return pkg;
	}
	public static String getLaunchActivity(){
		return launchActivity;
	}
}
