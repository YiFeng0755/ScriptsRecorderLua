package com.android.uiautomator;

//import android.util.JsonReader;
//import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSON2XML{
	public static DocumentBuilderFactory dBuilderFactory = DocumentBuilderFactory.newInstance();
	public static DocumentBuilder documentBuilder;
	public static Document document;
	public static Element rootElement;
//	public static final String UIDUMP_DEVICE_PATH = "/sdcard/lua_uidump.xml";
	public static final String LOG_TAG = "luaviewer_helper";
	public static double proportion;
	
	
	public static Element iterorJson(Document document, Object value, Element rootElement) throws DOMException, ParserConfigurationException, TransformerException{
//		System.out.println(value);
		Element node = document.createElement("node");
//		int count = 0;
//		node.setAttribute("index", String.valueOf(count));
//		count++;
		
		rootElement.appendChild(node);
//		System.out.println(toXMLString(document));
		JSONObject jsonObject = ((JSONObject)value);
//		System.out.println(jsonObject);
//		System.out.println(jsonObject.keySet());
		
		if(null != jsonObject.get("position") && null != jsonObject.get("size")){
			node.setAttribute("bounds", boundJson(jsonObject.get("position"), jsonObject.get("size")));
		}
	
		for(Object key : jsonObject.keySet()){
			if (key.equals("position") || key.equals("size")) {
				continue;
				/*if (key.equals("position")) {
//					System.out.println(key);
//					System.out.println(jsonObject.get(key));
					String value11 = node.getAttribute("bounds");
					
					if(value11!=null){
//						System.out.println(value11);
						value11 = BoundJsonXY(jsonObject.get(key)) +value11 ;
//						System.out.println(value11);
						node.setAttribute("bounds", value11);
					}else{
						node.setAttribute("bounds", BoundJsonXY(jsonObject.get(key)));
					}
//					System.out.println(toXMLString(document));
				}else{
//					System.out.println(key);
//					System.out.println(jsonObject.get(key));
					String value1 = node.getAttribute("bounds");
					if(value1!=null){
//						System.out.println(value1);
						value1 =value1 +BoundJsonHW(jsonObject.get(key));
						node.setAttribute("bounds", value1);
					}else{
						node.setAttribute("bounds", BoundJsonHW(jsonObject.get(key)));
					}
					
//					System.out.println(toXMLString(document));
				}*/
				}
			else if (key.equals("children")) {
//				System.out.println(toXMLString(document));
//				System.out.println(jsonObject.get(key));
				if (jsonObject.get(key) instanceof JSONArray) {
					JSONArray2XML(document, jsonObject.get(key),node);
//					System.out.println(toXMLString(document));
				}else if (jsonObject.get(key).equals("")) {
					iterorJson(document, jsonObject.get(key), node);
				}
				}
			else if (!key.equals("tag")){
//				System.out.println(key.toString());
//				System.out.println(jsonObject.get(key).toString());
				node.setAttribute(key.toString(), StringEscapeUtils.unescapeXml(jsonObject.get(key).toString()));
//				System.out.println(toXMLString(document));
			}
			
//			System.out.println(toXMLString(document));
		}
//		System.out.println(toXMLString(document));
		return node;	
	}
	
	public static void setInitSize(JSONObject json){
		double phoneWidth = json.getJSONObject("frame_size").getDouble("width");
		double designWidth = json.getJSONObject("size").getDouble("width");
		proportion = phoneWidth/designWidth;
	}
		
	public static String boundJson(Object xyValue, Object hwValue){
		String str = "[";
		JSONObject jsonObject = (JSONObject) xyValue;
		int x = (int) jsonObject.getDouble("x");
		int y = (int) jsonObject.getDouble("y");
		str = str + x + "," + y +"][";
		jsonObject = (JSONObject) hwValue;
		int w = (int) (jsonObject.getDouble("width")*proportion);
		int h = (int) (jsonObject.getDouble("height")*proportion);
		str = str + (x + w) + "," + (y + h) +"]";
		return str;
	}
	
	public static String BoundJsonXY(Object value){
		StringBuffer sbBuffer = new StringBuffer("");
//		System.out.println(value.toString());
		if ((value.toString().contains(":"))) {
			sbBuffer.append("[");
			JSONObject jsonObject = ((JSONObject)value);
			Object string = jsonObject.getDouble("x");
			String string1 = string.toString();		
//			System.out.println(string1);
			if (string1.contains(".")) {
				String intString = string1.substring(0,string1.indexOf("."));
				sbBuffer.append(intString);
				}else {
					sbBuffer.append(string1);
				}				
				sbBuffer.append(",");
//				System.out.println(sbBuffer);
			
			Object string0 = jsonObject.getDouble("y");
			String string2 = string0.toString();	
//			System.out.println(string2);
			if (string2.contains(".")) {
				String intString2 = string2.substring(0,string2.indexOf("."));
				sbBuffer.append(intString2);
			}else {
				sbBuffer.append(string2);
			}	
//			System.out.println(sbBuffer);
		}else {
			sbBuffer.append(value);
		}
		
//		System.out.println(sbBuffer+" "+sbBuffer.length());
//		sbBuffer=sbBuffer.deleteCharAt(sbBuffer.length()-1);
//		String[] strs = sbBuffer.toString().split(",");  
//		if (strs.length>=3) {
//			sbBuffer=sbBuffer.delete(sbBuffer.lastIndexOf(","), sbBuffer.length());
//			System.out.println(sbBuffer+" "+sbBuffer.length());
//		}
		sbBuffer.append("]");
//		System.out.println(sbBuffer);
		return sbBuffer.toString();
	}
	
	public static String BoundJsonHW(Object value){
		StringBuffer sbBuffer = new StringBuffer("");
//		System.out.println(value.toString());
		if ((value.toString().contains(":"))) {
			sbBuffer.append("[");
			JSONObject jsonObject = ((JSONObject)value);
			Object string = jsonObject.getDouble("width");
			String string1 = string.toString();		
//			System.out.println(string1);
			if (string1.contains(".")) {
				String intString = string1.substring(0,string1.indexOf("."));
				sbBuffer.append(intString);
				}else {
					sbBuffer.append(string1);
				}				
				sbBuffer.append(",");
//				System.out.println(sbBuffer);
			
			Object string0 = jsonObject.getDouble("height");
			String string2 = string0.toString();	
//			System.out.println(string2);
			if (string2.contains(".")) {
				String intString2 = string2.substring(0,string2.indexOf("."));
				sbBuffer.append(intString2);
			}else {
				sbBuffer.append(string2);
			}	
//			System.out.println(sbBuffer);
		}else {
			sbBuffer.append(value);
		}
		sbBuffer.append("]");
//		System.out.println(sbBuffer);
		return sbBuffer.toString();
	}
	
	
	
	public static String BoundJson(Object value){
		StringBuffer sbBuffer = new StringBuffer("");
		if ((value.toString().contains(":"))) {
			sbBuffer.append("[");
			JSONObject jsonObject = ((JSONObject)value);
//			System.out.println(jsonObject.keySet());
			for (Object key: jsonObject.keySet()) {
				
//				System.out.println(jsonObject.get(key));
				Object string = jsonObject.get(key);
//				System.out.println(string);
				String string1 = string.toString();		
				
				if (string1.contains(".")) {
					String intString = string1.substring(0,string1.indexOf("."));
				
//					System.out.println(intString);
					sbBuffer.append(intString);

				}else {
					sbBuffer.append(string1);
				}
				
				
				sbBuffer.append(",");
			}			
		}else {
			sbBuffer.append(value);
		}
		
//		System.out.println(sbBuffer+" "+sbBuffer.length());
		sbBuffer=sbBuffer.deleteCharAt(sbBuffer.length()-1);
		String[] strs = sbBuffer.toString().split(",");  
		if (strs.length>=3) {
			sbBuffer=sbBuffer.delete(sbBuffer.lastIndexOf(","), sbBuffer.length());
//			System.out.println(sbBuffer+" "+sbBuffer.length());
		}
		sbBuffer.append("]");
		return sbBuffer.toString();
	}
	
	public static Node JSONArray2XML(Document document, Object object,Element node) throws ParserConfigurationException, DOMException, TransformerException{
		JSONArray nameaArray = JSONArray.fromObject(object);
//		System.out.println(nameaArray);
//		JSONArray nameList = JSONArray.jsonObject.get(key);
		int length = nameaArray.size();
//		System.out.println("length:"+length);
		if (length>0) {
			 for(int i = 0; i < length; i++){ 
				 JSONObject oj = nameaArray.getJSONObject(i); 
//				 System.out.println(oj);
				 if (oj != null && oj.size()!=0) {
					iterorJson(document,oj,node);
				}
				 
//				 JSONArray nameaArray1 = JSONArray.fromObject(oj);
//				 System.out.println(oj);
//				 if (nameaArray1.size()>1){
//				 }
				 document.getChildNodes();
//				 System.out.println(toXMLString(document));
			 }
		}
        return rootElement;
	}
	
    //返回文档Document的XML格式的字符串  
    public static String toXMLString(Document document) throws TransformerException{  
        //创建一个DOM转换器 
        TransformerFactory  transformerFactory  =  TransformerFactory.newInstance();  
        Transformer transformer = transformerFactory.newTransformer();  
        /* 
         * 设置输出属性  
         *     encoding = "GB2312" 代表 输出的编码格式为 GB2312 
         *     indent = "yes" 代表缩进输出 
         */  
        transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");  
//        transformer.setOutputProperty(OutputKeys.INDENT,"yes"); 
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");  
        ByteArrayOutputStream  outputStream  =  new  ByteArrayOutputStream();  
        //transformer.transform()方法 将 XML Source转换为 Result  
        transformer.transform(new DOMSource(document), new StreamResult(outputStream));  
        return outputStream.toString();   
    }  
	
	
//	public static void main(String args[]) throws ParserConfigurationException, IOException{
    public JSON2XML(String jsonString, File file, String rotation)throws ParserConfigurationException, IOException{
//		String jsonString = "{\"visible\":true,\"type\":\"class<Drawing>\",\"name\":\"\",\"position\":{\"y\":0,\"x\":0,\"z\":0},\"children\":[{\"visible\":true,\"type\":\"class<Widget>\",\"name\":\"aa\",\"position\":{\"y\":200,\"x\":500,\"z\":0},\"children\":[{\"visible\":true,\"type\":\"class<Sprite>\",\"name\":\"sprite_1\",\"position\":{\"y\":103,\"x\":-300,\"z\":0},\"children\":{},\"size\":{\"height\":95,\"width\":95}},{\"visible\":true,\"type\":\"class<Sprite>\",\"name\":\"sprite_2\",\"position\":{\"y\":103,\"x\":-205,\"z\":0},\"children\":{},\"size\":{\"height\":95,\"width\":95}},{\"visible\":true,\"type\":\"class<Sprite>\",\"name\":\"sprite_3\",\"position\":{\"y\":103,\"x\":-110,\"z\":0},\"children\":{},\"size\":{\"height\":95,\"width\":95}},{\"visible\":true,\"type\":\"class<Sprite>\",\"name\":\"sprite_4\",\"position\":{\"y\":103,\"x\":-15,\"z\":0},\"children\":{},\"size\":{\"height\":93,\"width\":96}},{\"visible\":true,\"type\":\"class<Sprite>\",\"name\":\"sprite_5\",\"position\":{\"y\":103,\"x\":80,\"z\":0},\"children\":{},\"size\":{\"height\":94,\"width\":95}},{\"visible\":true,\"type\":\"class<Sprite>\",\"name\":\"sprite_6\",\"position\":{\"y\":103,\"x\":175,\"z\":0},\"children\":{},\"size\":{\"height\":95,\"width\":95}},{\"visible\":true,\"type\":\"class<Sprite>\",\"name\":\"sprite_7\",\"position\":{\"y\":103,\"x\":270,\"z\":0},\"children\":{},\"size\":{\"height\":95,\"width\":96}},{\"visible\":true,\"type\":\"class<Sprite>\",\"name\":\"sprite_8\",\"position\":{\"y\":103,\"x\":365,\"z\":0},\"children\":{},\"size\":{\"height\":96,\"width\":94}},{\"visible\":true,\"type\":\"class<Sprite>\",\"name\":\"sprite_9\",\"position\":{\"y\":103,\"x\":460,\"z\":0},\"children\":{},\"size\":{\"height\":95,\"width\":95}}],\"size\":{\"height\":300,\"width\":300}}],\"size\":{\"height\":955,\"width\":1918}}";
//    	System.out.println("jsonString:"+jsonString);
//    	System.out.println("JSON to xml");
		JSONObject json = JSONObject.fromObject(jsonString);
//		System.out.println("json:"+json);
        //创建DOM树
		setInitSize(json);
		
		dBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilder = dBuilderFactory.newDocumentBuilder();
		document = documentBuilder.newDocument();

		try {
			Element rootElement = document.createElement("hierarchy");
			rootElement.setAttribute("rotation", rotation);
			document.appendChild(rootElement);
//			System.out.println(json.keySet());
//			System.out.println(toXMLString(document));
			rootElement.appendChild(iterorJson(document, json,rootElement));
//			System.out.println(toXMLString(document));
//			return document.toString();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {  
            TransformerFactory tfactory = TransformerFactory.newInstance();  
            Transformer transformer = tfactory.newTransformer();  
            DOMSource source = new DOMSource(document);  
            /* 新文件的地址 */  
//            File file = new File(file);  
            StreamResult result = new StreamResult(file);  
            transformer.transform(source, result);  
        } catch (TransformerException e) {  
            e.printStackTrace();  
        }
	}
}
