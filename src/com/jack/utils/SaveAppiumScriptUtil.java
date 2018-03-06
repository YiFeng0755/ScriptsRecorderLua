package com.jack.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.android.uiautomator.NewScriptDialog;
import com.jack.model.Action;
import com.jack.model.ApkInfo;

public class SaveAppiumScriptUtil {

	public SaveAppiumScriptUtil() {
		// TODO Auto-generated constructor stub
	}

	public static void createAppiumScript(String scriptDirPath,
			ArrayList<Action> actions) {
		scriptDirPath += File.separator + "appiumScript";
		FileUtil.copyDirectory("appiumProjectTemplate", scriptDirPath);

		String scriptPath = scriptDirPath + File.separator + "src" + File.separator + "cases" + File.separator + "script.py";
		String cfgPath = scriptDirPath + File.separator + "src" + File.separator + "cfg" + File.separator + "cfg.ini";
		String operation = "";
		String scriptStr = System.getProperty("line.separator") + "        ";

		for (Action a : actions) {
			operation = a.getOperation();
			scriptStr += StringUtil.strToScript(operation) + System.getProperty("line.separator") + "        ";
		}
		
		scriptStr += System.getProperty("line.separator") + "if __name__ == '__main__':" + System.getProperty("line.separator")+ "    " + "debug_run_all()";
		
		FileWriter fw = null;
		
		try {
			fw = new FileWriter(scriptPath, true);
			fw.write(scriptStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(null != fw){
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		String cfgStr = System.getProperty("line.separator") + "apppackage = " + ApkInfo.getPkg() + System.getProperty("line.separator")
				+ "appactivity = " + ApkInfo.getLaunchActivity() + System.getProperty("line.separator") + "deviceName = " + NewScriptDialog.getsIDevice().getSerialNumber()
				+ System.getProperty("line.separator") + "luaport = " + NewScriptDialog.getmPort();
		
		try {
			fw = new FileWriter(cfgPath, true);
			fw.write(cfgStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(null != fw){
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	

}
