package com.jpcamara.fbplusplus.c2.util;

import org.json.JSONException;
import org.json.JSONObject;


public class ResponseParser {
	/**
	 * parse the message posting response, and doing some corresponding things
	 * e.g. if it succeeds, we do nothing;
	 * else if we get some error, we print them.
	 * 
	 * @param response
	 * @throws JSONException 
	 */
	public static void messagePostingResultParser(String uid, String msg, String response) throws JSONException{
		if(response == null)
			return;
		String prefix = "for (;;);";
		if(response.startsWith(prefix))
			response = response.substring(prefix.length());
		
		//for (;;);{"error":0,"errorSummary":"","errorDescription":"No error.","payload":[],"bootload":[{"name":"js\/common.js.pkg.php","type":"js","src":"http:\/\/static.ak.fbcdn.net\/rsrc.php\/pkg\/60\/106715\/js\/common.js.pkg.php"}]}
		//for (;;);{"error":1356003,"errorSummary":"Send destination not online","errorDescription":"This person is no longer online.","payload":null,"bootload":[{"name":"js\/common.js.pkg.php","type":"js","src":"http:\/\/static.ak.fbcdn.net\/rsrc.php\/pkg\/60\/106715\/js\/common.js.pkg.php"}]}
		JSONObject respObjs = new JSONObject(response);
		if(respObjs == null)
			return;
		System.out.println("error: " + respObjs.getInt("error"));
		if(respObjs.get("error") != null){
			Long errorCode = (Long)respObjs.getLong("error");
			String errorString = "Error(" + errorCode + "): " 
					+ (String)respObjs.get("errorSummary") + ";"
					+ (String)respObjs.get("errorDescription");
					
			System.out.println(errorString);
		}
	}
}