/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.uiautomator;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.TimeoutException;
import com.android.uiautomator.tree.BasicTreeNode;
import com.android.uiautomator.tree.RootWindowNode;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
public class UiAutomatorHelper {
    public static final int UIAUTOMATOR_MIN_API_LEVEL = 16;

	//    private static final String UIAUTOMATOR = "/system/bin/uiautomator";    //$NON-NLS-1$
	//    private static final String UIAUTOMATOR_DUMP_COMMAND = "dump";          //$NON-NLS-1$
	public static final String UIDUMP_DEVICE_PATH = "/mnt/sdcard/lua_uidump.json"; //$NON-NLS-1$
	public static final String UIDUMP_DEVICE_PATH1 = "/mnt/sdcard/lua_uidump.xml"; //$NON-NLS-1$
	public static final int XML_CAPTURE_TIMEOUT_SEC = 40000;

	private static boolean supportsUiAutomator(IDevice device) {
		String apiLevelString = device
				.getProperty(IDevice.PROP_BUILD_API_LEVEL);
		int apiLevel;
		try {
			apiLevel = Integer.parseInt(apiLevelString);
		} catch (NumberFormatException e) {
			apiLevel = UIAUTOMATOR_MIN_API_LEVEL;
		}

		return apiLevel >= UIAUTOMATOR_MIN_API_LEVEL;
	}

	private static void getUiHierarchyFile(IDevice device, File dst,
			IProgressMonitor monitor, boolean compressed) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		/*monitor.subTask("unInstall helper if it has been installed...");
		//			device.uninstallPackage("com.boyaa.luaviewer_helper");
		try {
			Runtime.getRuntime().exec("adb uninstall com.boyaa.luaviewer_helper");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		/*monitor.subTask("Install helper if it hasn't been installed...");
		// String installPath =
		// System.getProperty("user.dir")+"\\luaviewer_helper.apk";
		File f = new File("luaviewer_helper.apk");
		if (!f.exists()) {
			throw new RuntimeException(
					"Could not find luaviewer_helper.apk! Make sure it is in the same directory.");
		}
		try {
			device.installPackage("luaviewer_helper.apk", false);
		} catch (InstallException e) {
			throw new RuntimeException("install helper package failed. Info:"
					+ e);
		}*/

		monitor.subTask("Deleting old Lua UI XML snapshot ...");
		String command = "rm " + UIDUMP_DEVICE_PATH;

		CountDownLatch commandCompleteLatch = new CountDownLatch(1);
		 try {
		 device.executeShellCommand(command,
		 new CollectingOutputReceiver(commandCompleteLatch));
		 commandCompleteLatch.await(5, TimeUnit.SECONDS);
		 } catch (Exception e1) {
		 // ignore exceptions while deleting stale files
		 }

		monitor.subTask("Taking Lua UI XML snapshot...");
		command = "am startservice --user 0 com.boyaa.luaviewer_helper/.ViewerHelper";
		try {
			device.executeShellCommand(command, new CollectingOutputReceiver(
					commandCompleteLatch), XML_CAPTURE_TIMEOUT_SEC * 1000);
			commandCompleteLatch.await(XML_CAPTURE_TIMEOUT_SEC,
					TimeUnit.SECONDS);

			monitor.subTask("Wait Lua UI XML snapshot to dump...");
			
			
			FileIsExistReceiver fileIsExisRreceiver = new FileIsExistReceiver();
			long startTime = System.currentTimeMillis();
			int loopCount = 0;
			do{
				try {
					device.executeShellCommand(" ls " + UIDUMP_DEVICE_PATH, fileIsExisRreceiver);
					if((System.currentTimeMillis() > (startTime+1000*10)) && !fileIsExisRreceiver.isFileExist()){
						if(loopCount > 2){
							break;
						}
						device.executeShellCommand(command, new CollectingOutputReceiver(
								commandCompleteLatch), XML_CAPTURE_TIMEOUT_SEC * 1000);
						commandCompleteLatch.await(XML_CAPTURE_TIMEOUT_SEC,
								TimeUnit.SECONDS);
						startTime = System.currentTimeMillis();
						loopCount++;
					}
				} catch (TimeoutException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (AdbCommandRejectedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ShellCommandUnresponsiveException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}while(!fileIsExisRreceiver.isFileExist());
			

			monitor.subTask("Pull Lua UI XML snapshot from device...");
			System.out.println(dst.getAbsolutePath());
			device.getSyncService().pullFile(UIDUMP_DEVICE_PATH,
					dst.getAbsolutePath(),
					SyncService.getNullProgressMonitor());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// throw new RuntimeException("result:"+result);
	}

	// to maintain a backward compatible api, use non-compressed as default
	// snapshot type
	public static UiAutomatorResult takeSnapshot(IDevice device,
			IProgressMonitor monitor) throws UiAutomatorException {
		return takeSnapshot(device, monitor, false);
	}

	public static UiAutomatorResult takeSnapshot(IDevice device,
			IProgressMonitor monitor, boolean compressed)
			throws UiAutomatorException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.subTask("Checking if device support Lua Element Viewer");
		if (!supportsUiAutomator(device)) {
			String msg = "Lua Element Viewer requires a device with API Level "
					+ UIAUTOMATOR_MIN_API_LEVEL;
			throw new UiAutomatorException(msg, null);
		}

		monitor.subTask("Creating temporary files for Lua Elements hierarchy results.");
		File tmpDir = null;
		File xmlDumpFile = null;
		File jsonDumpFile = null;
		File screenshotFile = null;
		try {
			tmpDir = File.createTempFile("uiautomatorviewer_", "");
			tmpDir.delete();
			if (!tmpDir.mkdirs())
				throw new IOException("Failed to mkdir");
			jsonDumpFile = File.createTempFile("dump_", ".json", tmpDir);
			xmlDumpFile = File.createTempFile("dump_", ".uix", tmpDir);
			screenshotFile = File.createTempFile("screenshot_", ".png", tmpDir);
		} catch (Exception e) {
			String msg = "Error while creating temporary file to save snapshot: "
					+ e.getMessage();
			throw new UiAutomatorException(msg, e);
		}

		tmpDir.deleteOnExit();
		jsonDumpFile.deleteOnExit();
		xmlDumpFile.deleteOnExit();
		screenshotFile.deleteOnExit();

		monitor.subTask("Obtaining UI hierarchy for Lua Elements");
		try {
			UiAutomatorHelper.getUiHierarchyFile(device, jsonDumpFile, monitor,
					compressed);
		} catch (Exception e) {
			String msg = "Error while obtaining UI hierarchy XML file: "
					+ e.getMessage();
			throw new UiAutomatorException(msg, e);
		}
		
		RotationReceiver receiver = new RotationReceiver();
		try {
			device.executeShellCommand(" dumpsys display", receiver);
		} catch (TimeoutException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (AdbCommandRejectedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ShellCommandUnresponsiveException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		UiAutomatorModel model;
		// ��ȡjson�ļ���д��
		try {
			// File file = new File(jsonDumpFile.toString());
			// BufferedReader reader = null;
			// @SuppressWarnings("resource")
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					jsonDumpFile));
			String tmpString = null;
			String jsonstring = "";
			int line = 1;

			while ((tmpString = bufferedReader.readLine()) != null) {
				System.out.println("line " + line + ": " + tmpString);
				jsonstring = jsonstring + tmpString;
				line++;
			}
			System.out.println("jsonstring: " + jsonstring);
			JSON2XML json2xml = new JSON2XML(jsonstring, xmlDumpFile, receiver.getRotation());

			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			model = new UiAutomatorModel(xmlDumpFile);
		} catch (Exception e) {
			String msg = "Error while parsing UI hierarchy XML file: "
					+ e.getMessage();
			throw new UiAutomatorException(msg, e);
		}

		monitor.subTask("Obtaining device screenshot");
		RawImage rawImage;
		try {
			rawImage = device.getScreenshot();
		} catch (Exception e) {
			String msg = "Error taking device screenshot: " + e.getMessage();
			throw new UiAutomatorException(msg, e);
		}
		if (device.getApiLevel() < 23){
		// rotate the screen shot per device rotation
		BasicTreeNode root = model.getXmlRootNode();
		if (root instanceof RootWindowNode) {
			for (int i = 0; i < ((RootWindowNode) root).getRotation(); i++) {
				rawImage = rawImage.getRotated();
			}
		}
		}
		PaletteData palette = new PaletteData(rawImage.getRedMask(), rawImage.getGreenMask(), rawImage.getBlueMask());
		ImageData imageData = new ImageData(rawImage.width, rawImage.height, rawImage.bpp, palette, 1, rawImage.data);
		System.out.println(rawImage.width+" "+ rawImage.height);
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { imageData };
//		loader.save(screenshotFile.getAbsolutePath(), SWT.IMAGE_PNG);
		loader.save(screenshotFile.getAbsolutePath(), 5);
		//��ת
//		if (rawImage.width < rawImage.height) {
//			BufferedImage oldImage;
//			try {
//				oldImage = ImageIO.read(new FileInputStream(screenshotFile.getAbsolutePath()));
//				BufferedImage newImage = new BufferedImage(oldImage.getHeight(), oldImage.getWidth(), oldImage.getType());
//				Graphics2D graphics = (Graphics2D) newImage.getGraphics();
//				graphics.translate((newImage.getWidth() - oldImage.getWidth()) / 2, (newImage.getHeight() - oldImage.getHeight()) / 2);
//				graphics.drawImage(oldImage, 0, 0, oldImage.getWidth(), oldImage.getHeight(), null);
//				ImageIO.write(newImage, "JPG", new FileOutputStream(screenshotFile.getAbsolutePath()));
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}			
//		}
		
		System.out.println(screenshotFile.getAbsolutePath()+" Display.getDefault()"+Display.getDefault()+" imageData"+imageData);
		Image screenshot = new Image(Display.getDefault(), imageData);

        return new UiAutomatorResult(xmlDumpFile, model, screenshot);
    }

    @SuppressWarnings("serial")
    public static class UiAutomatorException extends Exception {
        public UiAutomatorException(String msg, Throwable t) {
            super(msg, t);
        }
    }

    public static class UiAutomatorResult {
        public final File uiHierarchy;
        public final UiAutomatorModel model;
        public final Image screenshot;

		public UiAutomatorResult(File uiXml, UiAutomatorModel m, Image s) {
			uiHierarchy = uiXml;
			model = m;
			screenshot = s;
		}
	}
	
	public static class RotationReceiver extends MultiLineReceiver {
		
		private Pattern pOverrideDis = Pattern.compile("mOverrideDisplayInfo.*");
		private Pattern pRotation = Pattern.compile("rotation\\s*\\d");
		private String rotation = "0";
		
		public RotationReceiver(){
			super();
		}
	

		@Override
		public boolean isCancelled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void processNewLines(String[] lines) {
			// TODO Auto-generated method stub
			Matcher mOverrideDis = null;
			Matcher mRotation = null;
			for(String line : lines){
				mOverrideDis = pOverrideDis.matcher(line);
				if(mOverrideDis.find()){
					mRotation = pRotation.matcher(mOverrideDis.group());
					if(mRotation.find()){
						String rotationStr = mRotation.group();
						rotation = rotationStr.substring(rotationStr.length()-1);
						break;
					}
				}
			}
		}
		
		public String getRotation(){
			return rotation;
		}
		
	}
	
	public static class FileIsExistReceiver extends MultiLineReceiver {
		
		private boolean fileExist = false;
		
		public FileIsExistReceiver(){
			super();
		}
	

		@Override
		public boolean isCancelled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void processNewLines(String[] lines) {
			// TODO Auto-generated method stub
			for(String line : lines){
				if(line.contains("No such")){
					break;
				}
				if(line.length()>0){
					fileExist = true;
				}
			}
			
		}
		
		public boolean isFileExist(){
			return fileExist;
		}
		
	}

}
