package com.jack.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jack.model.Operate;

import net.sf.json.JSONObject;

public class StringUtil {

	public static String getSubString(String str, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(0);
		}
		return "";

	}

	public static String strToScript(String operation) {
		StringBuffer sb = new StringBuffer();

		String type = operation.split("::")[0];
		if (Operate.SLEEP.equals(type)) {
			sb.append("time.sleep(");
			sb.append(operation.split("[|]")[1]);
			sb.append("000)");
			return sb.toString();
		}
		if (Operate.SENDKC.equals(type)) {
			sb.append("self.luadriver.keyevent(4)");
			return sb.toString();
		}

		sb.append("self.luadriver.");
		String str = "";
		if ((str = getSubString(operation, "name:.*,|\\}")).length() > 1
				&& (str = str.split(",|\\}")[0].split(":")[1]).length() > 0) {
			sb.append("find_lua_element_by_name(\"");
			sb.append(str);
		} else if ((str = getSubString(operation, "xpath:.*,|\\}")).length() > 1
				&& (str = str.split(",|\\}")[0].split(":")[1]).length() > 0) {
			sb.append("find_lua_element_by_xpath(\"");
			sb.append(str);
		} else {
			return "";
		}
		sb.append("\").");

		if (Operate.CLICK.equals(type)) {
			sb.append("click()");
		} else if (Operate.INPUT.equals(type)) {
			sb.append("send_keys(\"");
			sb.append(operation.split("[|]")[1]);
			sb.append("\")");
		}

		return sb.toString();
	}

	public static String strToIntenExtra(String operation) {
		String intetnExtra = "";
		if (operation.contains("|")) {
			intetnExtra += " --es \"value\" \"" + operation.split("[|]")[1] + "\"";
			String str = "";
			if ((str = getSubString(operation, "name:.*,|\\}")).length() > 1
					&& (str = str.split(",|\\}")[0].split(":")[1]).length() > 0) {
				intetnExtra += " --es \"type\" \"name\" --es \"setText\" \"" + str +"\"";
			}else if ((str = getSubString(operation, "xpath:.*,|\\}")).length() > 1
					&& (str = str.split(",|\\}")[0].split(":")[1]).length() > 0){
				intetnExtra += " --es \"type\" \"xpath\" --es \"setText\" \"" + str +"\"";
			}
		}

		return intetnExtra;
	}

}
