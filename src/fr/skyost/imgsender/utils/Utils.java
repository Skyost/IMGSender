package fr.skyost.imgsender.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class Utils {
	
	public static final URL createURLWithoutException(final String url) {
		try {
			return new URL(url);
		}
		catch(MalformedURLException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static final boolean isFloat(final String floatStr) {
		try {
			Float.parseFloat(floatStr);
			return true;
		}
		catch(NumberFormatException ex) { }
		return false;
	}
	
	public static final String colourize(String in) {
		return (" " + in).replaceAll("([^\\\\](\\\\\\\\)*)&(.)", "$1§$3").replaceAll("([^\\\\](\\\\\\\\)*)&(.)", "$1§$3").replaceAll("(([^\\\\])\\\\((\\\\\\\\)*))&(.)", "$2$3&$5").replaceAll("\\\\\\\\", "\\\\").trim();
	}
	
	public static final String decolourize(String in) {
		return (" " + in).replaceAll("\\\\", "\\\\\\\\").replaceAll("&", "\\\\&").replaceAll("§", "&").trim();
	}

}
