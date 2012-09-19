/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpcamara.fbplusplus.c2.topology;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jpcamara.fbplusplus.c2.model.User;
import com.jpcamara.fbplusplus.c2.util.ChannelRegistry;
import com.jpcamara.fbplusplus.c2.util.ResponseParser;

/**
 * 
 * @author johnpcamara
 */
public class FacebookChatApi {

	/** Facebook login page */
	private static String loginPageUrl = "http://www.facebook.com/login.php";
	/** Facebook homepage */
	private static String homePageUrl = "http://www.facebook.com/home.php?";
	/** logging capabilities */
	private static Logger aLogger = Logger.getLogger(FacebookChatApi.class
			.getName());
	/**
	 * The default parameters. Instantiated in {@link #setup setup}.
	 */
	private static HttpParams defaultParameters = null;
	/**
	 * The scheme registry. Instantiated in {@link #setup setup}.
	 */
	private static SchemeRegistry supportedSchemes;

	/**
	 * Initialize the static SchemaRegistry and HttpParams objects
	 */
	static {
		supportedSchemes = new SchemeRegistry();

		// Register the "http" and "https" protocol schemes, they are
		// required by the default operator to look up socket factories.
		SocketFactory sf = PlainSocketFactory.getSocketFactory();
		supportedSchemes.register(new Scheme("http", sf, 80));
		sf = SSLSocketFactory.getSocketFactory();
		supportedSchemes.register(new Scheme("https", sf, 80));

		// prepare parameters
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpProtocolParams.setHttpElementCharset(params, "UTF-8");
		HttpProtocolParams
				.setUserAgent(
						params,
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9) Gecko/2008052906 Firefox/3.0");
		defaultParameters = params;
	}
	/** The channel that the current user is defaulted to */
	private String channel;
	/** The form id on the users home page */
	private String postFormId;
	/** The user's ID */
	private String userId;
	/**
	 * FacebookPlusPlus User object, which contains the Facebook
	 * username/password
	 */
	private User fppUser;

	/** Used to indicate whether the user is logged in */
	private volatile boolean connected = false;
	/** Used for all HTTP communication */
	private HttpClient httpClient;
	private long seq = -1;

	/**
	 * Setup the API Sets up the HttpClient object for use Stores the username
	 * and password for later use
	 */
	public FacebookChatApi(User fppUser) {
		ClientConnectionManager ccm = new ThreadSafeClientConnManager(
				defaultParameters, supportedSchemes);

		DefaultHttpClient dhc = new DefaultHttpClient(ccm, defaultParameters);

		dhc.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);

		httpClient = dhc;
		this.fppUser = fppUser;
	}

	/**
	 * Opens a connection to the facebook API and grabs a session token as part
	 * of it
	 */
	public synchronized void connect() {
		try {
			// get the initial set of cookies that Facebook is expecting
			HttpGet loginGet = new HttpGet(loginPageUrl);
			HttpResponse response = httpClient.execute(loginGet);
			HttpEntity entity = response.getEntity();

			aLogger.log(Level.INFO, "Login form get: "
					+ response.getStatusLine());
			if (entity != null) {
				// consumeContent() is actually the opposite of what it's name
				// sounds like. what it does is indicate the content of this
				// entity is no longer needed
				entity.consumeContent();
			}

			// now go for the money shot - log into facebook
			HttpPost httpost = new HttpPost(loginPageUrl);

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("email", fppUser.getUserName()));
			nvps.add(new BasicNameValuePair("pass", fppUser.getPassword()));

			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			HttpResponse responsePost = httpClient.execute(httpost);
			entity = responsePost.getEntity();

			if (entity != null) {
				// aLogger.log(Level.INFO, EntityUtils.toString(entity));
				entity.consumeContent();
			}

			doParseHomePage();
		} catch (Exception e) {
			aLogger.log(Level.WARNING,
					"Failed attempt at logging into Facebook", e);
			connected = false;
			return;
		}

		connected = true;
	}

	public synchronized void disconnect() {
		connected = false;
		httpClient.getConnectionManager().shutdown();
	}

	// public static void main(String... args) {
	// User aUser = new User("", "");
	// FacebookChatApi api = new FacebookChatApi(aUser);
	// api.connect();
	// api.getFriends();
	// }

	public User getUser() {
		return fppUser;
	}

	/**
	 * Indicates whether the object is connected to the facebook API
	 * 
	 * @return boolean indicating connection status
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Retrieves the logged in users facebook friends that are logged in
	 * 
	 * @return a Friends object representing facebook friends that are logged in
	 * @throws RuntimeException
	 *             if user is not connected
	 */
	public String getBuddyList() {
		if (isConnected() == false) {
			handleUnconnected();
		}

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("__a", "1"));
		nvps.add(new BasicNameValuePair("notifications", "1"));
		nvps.add(new BasicNameValuePair("force_render", "true"));
		nvps.add(new BasicNameValuePair("post_form_id", postFormId));
		nvps.add(new BasicNameValuePair("user", userId));

		String responseStr = null;
		try {
			responseStr = facebookPostMethod("http://www.facebook.com",
					"/ajax/chat/buddy_list.php", nvps);

			aLogger.log(Level.INFO, "responseStr: " + responseStr);
			// ResponseParser.buddylistParser(responseStr);
		} catch (Exception e) {
			aLogger.log(Level.INFO, e.getMessage());
		}
		/**
		 * catch (JSONException e) { System.out.println(e.getMessage()); }
		 */
		if (responseStr != null) {
			responseStr = responseStr.replace("for (;;);", "");
		}
		return responseStr;
	}

	/**
	 * Handles unconnected issues by throwing an exception
	 * 
	 * @throws RuntimeException
	 *             since that's its purpose
	 */
	private void handleUnconnected() {
		throw new RuntimeException("You need to connect() to facebook first");
	}

	public void sendMessage(String uid, String msg) {

		aLogger.log(Level.INFO, "====== PostMessage begin======");
		aLogger.log(Level.INFO, "to:" + uid);
		aLogger.log(Level.INFO, "msg:" + msg);

		// String url = "http://www.facebook.com/ajax/chat/send.php";

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("msg_text", (msg == null) ? "" : msg));
		nvps.add(new BasicNameValuePair("msg_id", new Random()
				.nextInt(999999999)
				+ ""));
		nvps.add(new BasicNameValuePair("client_time", new Date().getTime()
				+ ""));
		nvps.add(new BasicNameValuePair("to", uid));
		nvps.add(new BasicNameValuePair("post_form_id", postFormId));

		aLogger.log(Level.INFO, "executeMethod...");
		try {
			String responseStr = facebookPostMethod("http://www.facebook.com",
					"/ajax/chat/send.php", nvps);
			// for (;;);{"t":"continue"}
			// for (;;);{"t":"refresh"}
			// for (;;);{"t":"refresh", "seq":0}
			// for
			// (;;);{"error":0,"errorSummary":"","errorDescription":"No error.","payload":[],"bootload":[{"name":"js\/common.js.pkg.php","type":"js","src":"http:\/\/static.ak.fbcdn.net\/rsrc.php\/pkg\/60\/106715\/js\/common.js.pkg.php"}]}
			// for
			// (;;);{"error":1356003,"errorSummary":"Send destination not online","errorDescription":"This person is no longer online.","payload":null,"bootload":[{"name":"js\/common.js.pkg.php","type":"js","src":"http:\/\/static.ak.fbcdn.net\/rsrc.php\/pkg\/60\/106715\/js\/common.js.pkg.php"}]}
			aLogger.log(Level.INFO, "+++++++++ PostMessage end +++++++++");
			// testHttpClient("http://www.facebook.com/home.php?");
			aLogger.log(Level.INFO, "response: " + responseStr);
			aLogger.log(Level.INFO, responseStr);
//			ResponseParser.messagePostingResultParser(uid, msg, responseStr);
		} catch (Exception e) {
			aLogger.log(Level.SEVERE, "Parsing exception", e);
		}
	}

	public JSONObject pollMessages() {
		seq = getSeq();

		// go seq
		// while (isConnected()) {
		// PostMessage("1190346972", "SEQ:"+seq);
		int currentSeq = getSeq();
		aLogger.log(Level.INFO, "My seq:" + seq + " | Current seq:"
				+ currentSeq + '\n');
		if (seq > currentSeq) {
			seq = currentSeq;
		}

		String msgResponseBody = null;
		JSONObject response = new JSONObject();
		while (seq <= currentSeq) {
			// get the old message between oldseq and seq
			msgResponseBody = facebookGetMethod(getMessageRequestingUrl(seq));

			System.out.println("=========msgResponseBody begin=========");
			System.out.println(msgResponseBody);
			response = parseChatMessage(msgResponseBody);
			System.out.println("+++++++++msgResponseBody end+++++++++");
			
			seq++;
		}
		return response;
	}

	/**
	 * Parse the content we need from the chat message
	 * @param msg
	 * @return
	 */
	private JSONObject parseChatMessage(String msg) {
		//for (;;);{"t":"msg","c":"p_1487850009","ms":[{"type":"msg","msg":{"text":"whats up nigga","time":1271736191172,"clientTime":1271736190347,"msgID":"2598661884"},
		//"from":52800671,"to":1487850009,"from_name":"Justin Port","to_name":"The Master","from_first_name":"Justin","to_first_name":"The Master"}]}
		msg = msg.replace("for (;;);", "");

		JSONObject resp = new JSONObject();
		aLogger.log(Level.INFO, "Content of message: " + msg);
		try {
			JSONObject obj = new JSONObject(msg);
			JSONArray fbmsg = (JSONArray) obj.get("ms");
			JSONObject theMsgArr = (JSONObject) fbmsg.get(0);
			JSONObject msgToBroadCast = (JSONObject) theMsgArr.get("msg");
			String fromId = theMsgArr.getString("from");
			String toId = theMsgArr.getString("to");

			String message = msgToBroadCast.getString("text");
			String name = theMsgArr.getString("from_name");

			resp.put("message", message);
			resp.put("name", name);
			resp.put("fromId", fromId);
			resp.put("toId", toId);
		} catch (Exception e) {
			aLogger.log(Level.SEVERE, "Couldn't parse message from facebook", e);
		}
		
		return resp;
	}

	/**
	 * Retrieve the "seq"
	 * 
	 * @return
	 */
	private int getSeq() {
		int tempSeq = -1;
		while (tempSeq == -1) {
			// for (;;);{"t":"refresh", "seq":0}
			String seqResponseBody;
			try {
				seqResponseBody = facebookGetMethod(getMessageRequestingUrl(-1));
				tempSeq = parseSeq(seqResponseBody);
				aLogger.log(Level.INFO, "getSeq(): SEQ: " + tempSeq);

				if (tempSeq >= 0) {
					return tempSeq;
				}
			} catch (JSONException e) {
				aLogger.log(Level.SEVERE, "Problem parsing json", e);
			}
			try {
				aLogger.log(Level.INFO,
						"retrying to fetch the seq code after 1 second...");
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				aLogger.log(Level.INFO, "Interrupted!", e);
			}
		}
		return tempSeq;
	}

	/**
	 * Parse the "seq" out of the message response body using JSON
	 * 
	 * @param msgResponseBody
	 * @return
	 * @throws JSONException
	 */
	private int parseSeq(String msgResponseBody) throws JSONException {
		if (msgResponseBody == null) {
			return -1;
		}
		String prefix = "for (;;);";
		if (msgResponseBody.startsWith(prefix)) {
			msgResponseBody = msgResponseBody.substring(prefix.length());
		}

		JSONObject body = new JSONObject(msgResponseBody);
		if (body != null) {
			return body.getInt("seq");
		} else {
			return -1;
		}
	}

	private String getMessageRequestingUrl(long seq) {
		// http://0.channel06.facebook.com/x/0/false/p_MYID=-1
		String url = "http://0.channel" + channel
				+ ".facebook.com/x/0/false/p_" + userId + "=" + seq;
		System.out.println("request:" + url);
		return url;
	}

	/**
	 * The general facebook get method.
	 * 
	 * @param url
	 *            the URL of the page we wanna get
	 * @return the response string
	 */
	private String facebookGetMethod(String url) {
		String responseStr = null;

		try {
			HttpGet loginGet = new HttpGet(url);
			HttpResponse response = httpClient.execute(loginGet);
			HttpEntity entity = response.getEntity();

			aLogger.log(Level.INFO, "facebookGetMethod: "
					+ response.getStatusLine());
			if (entity != null) {
				responseStr = EntityUtils.toString(entity);
				entity.consumeContent();
			}

			int statusCode = response.getStatusLine().getStatusCode();

			/**
			 * Check the success of the response. We want it in the 200's range
			 */
			if (statusCode != 200) {
				// error occured
				aLogger.log(Level.INFO, "Error Occured! Status Code = "
						+ statusCode);
				responseStr = null;
			}
			aLogger.log(Level.INFO, "Get Method done(" + statusCode
					+ "), response string length: "
					+ (responseStr == null ? 0 : responseStr.length()));
		} catch (Exception e) {
			aLogger.log(Level.INFO, "Failed to get the page: " + url);
			aLogger.log(Level.INFO, e.getMessage());
		}

		return responseStr;
	}

	/**
	 * The general facebook post method.
	 * 
	 * @param host
	 *            the host
	 * @param urlPostfix
	 *            the post fix of the URL
	 * @param data
	 *            the parameter
	 * @return the response string
	 */
	private String facebookPostMethod(String host, String urlPostfix,
			List<NameValuePair> nvps) {
		String responseStr = null;
		try {
			HttpPost httpost = new HttpPost(host + urlPostfix);
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			// execute postMethod
			HttpResponse postResponse = httpClient.execute(httpost);
			HttpEntity entity = postResponse.getEntity();

			aLogger.log(Level.INFO, "facebookPostMethod: "
					+ postResponse.getStatusLine());
			if (entity != null) {
				responseStr = EntityUtils.toString(entity);
				// aLogger.log(Level.INFO, responseStr);
				entity.consumeContent();
			}
			aLogger.log(Level.INFO, "Post Method done("
					+ postResponse.getStatusLine().getStatusCode()
					+ "), response string length: "
					+ (responseStr == null ? 0 : responseStr.length()));
		} catch (HttpException e) {
			aLogger.log(Level.INFO, e.getMessage());
		} catch (IOException e) {
			aLogger.log(Level.INFO, e.getMessage());
		} catch (URISyntaxException e) {
			aLogger.log(Level.INFO, e.getMessage());
		}
		// TODO process the respons string
		// if statusCode == 200: no error;(responsStr contains
		// "errorDescription":"No error.")
		// else retry?
		return responseStr;
	}

	/**
	 * Retrieves the uuid from the cookies, the form id from the home page and
	 * the channel from the homepage
	 */
	private void doParseHomePage() {
		String getMethodResponseBody = facebookGetMethod(homePageUrl);

		// deal with the cookies
		aLogger.log(Level.INFO, "The final cookies:");
		List<Cookie> finalCookies = ((DefaultHttpClient) httpClient)
				.getCookieStore().getCookies();
		if (finalCookies.isEmpty()) {
			aLogger.log(Level.INFO, "None");
		} else {
			for (int i = 0; i < finalCookies.size(); i++) {
				aLogger.log(Level.INFO, "- " + finalCookies.get(i).toString());
				// looking for our uid
				if (finalCookies.get(i).getName().equals("c_user")) {
					userId = finalCookies.get(i).getValue();
				}
			}
		}

		if (getMethodResponseBody == null) {
			aLogger.log(Level.INFO, "Can't get the home page! Exit.");
		}
		if (userId == null) {
			aLogger.log(Level.INFO, "Can't get the user's id! Exit.");
		} else {
			// set the facebook id for the given user
			fppUser.setFacebookId(userId);
		}

		channel = ChannelRegistry.instance().getChannel(fppUser);
		if (channel == null) {
			aLogger.log(Level.INFO, "Response body: " + getMethodResponseBody);
			// find the channel
			Matcher m = Pattern.compile("\\\"channel\\d{2,}.+").matcher(getMethodResponseBody);
	        m.find();
	        int start = m.start();
	        
	        String channelPrefix = "\\\"channel";
	        int channelBeginPos = start;//getMethodResponseBody.indexOf(channelPrefix)
	                //+ channelPrefix.length();
	        if (channelBeginPos < channelPrefix.length()){
	            aLogger.log(Level.SEVERE, "Error: Can't find channel!");
	        }
	        else {
	            //channel = getMethodResponseBody.substring(channelBeginPos,
	                   // channelBeginPos + 2);
	            channel = getMethodResponseBody.substring(channelBeginPos+8,
	                    channelBeginPos + 10);
	            aLogger.log(Level.INFO, "Channel: " + channel);
	        }
		}
		// find the post form id
		// <input type="hidden" id="post_form_id" name="post_form_id"
		// value="3414c0f2db19233221ad8c2374398ed6" />
		String postFormIDPrefix = "<input type=\"hidden\" id=\"post_form_id\" name=\"post_form_id\" value=\"";
		int formIdBeginPos = getMethodResponseBody.indexOf(postFormIDPrefix)
				+ postFormIDPrefix.length();
		if (formIdBeginPos < postFormIDPrefix.length()) {
			aLogger.log(Level.INFO, "Error: Can't find post form ID!");
		} else {
			postFormId = getMethodResponseBody.substring(formIdBeginPos,
					formIdBeginPos + 32);
			aLogger.log(Level.INFO, "post_form_id: " + postFormId);
		}
	}

	private static class FBJsonParser {

		/**
		 * Pulls the buddy list from a piece of JSON
		 * 
		 * @param json
		 * @return JSON object that represents a friends list
		 * @throws Exception
		 */
		public JSONObject parseBuddyList(String json) throws Exception {
			String sanitizedJson = handlePrefix(json);
			JSONObject respObjs = new JSONObject(json);
			if (respObjs == null) {
				return null;
			}
			aLogger.log(Level.INFO, "error: " + respObjs.getInt("error"));

			JSONObject buddyList = null;
			if (respObjs.get("error") != null) {
				if (respObjs.getInt("error") == 0) {
					// no error
					JSONObject payload = (JSONObject) respObjs.get("payload");
					if (payload != null) {
						buddyList = (JSONObject) payload.get("buddy_list");
					}
				} else {
					aLogger.log(Level.INFO, "Error("
							+ (Long) respObjs.get("error") + "): "
							+ (String) respObjs.get("errorSummary") + ";"
							+ (String) respObjs.get("errorDescription"));
				}
			}
			return buddyList;
		}

		private String handlePrefix(String json) {
			String prefix = "for (;;);";
			String jsonNoFor = json;
			if (json.startsWith(prefix)) {
				json = json.substring(prefix.length());
			}
			return jsonNoFor;
		}
	}
}

/**
 * INFO: - [version: 0][name: lsd][value: SBPbg][domain: .facebook.com][path:
 * /][expiry: null] Apr 12, 2010 10:40:23 PM
 * com.jpcamara.fbplusplus.c2.impl.FacebookChatApi connect INFO: - [version:
 * 0][name: c_user][value: 684290735][domain: .facebook.com][path: /][expiry:
 * null] Apr 12, 2010 10:40:23 PM
 * com.jpcamara.fbplusplus.c2.impl.FacebookChatApi connect INFO: - [version:
 * 0][name: datr][value:
 * 1271126422-7525ef3b90ba3eb2e35ab34d1da96e04fa9c68d4e736dd11e29ff][domain:
 * .facebook.com][path: /][expiry: Wed Apr 11 22:40:24 EDT 2012] Apr 12, 2010
 * 10:40:23 PM com.jpcamara.fbplusplus.c2.impl.FacebookChatApi connect INFO: -
 * [version: 0][name: lo][value: KM1ykDdkB2Rh8UngyuzpVw][domain:
 * .facebook.com][path: /][expiry: Wed Apr 11 22:40:24 EDT 2012] Apr 12, 2010
 * 10:40:23 PM com.jpcamara.fbplusplus.c2.impl.FacebookChatApi connect INFO: -
 * [version: 0][name: lxs][value: 1][domain: .facebook.com][path: /][expiry: Fri
 * Aug 06 16:27:04 EDT 2010] Apr 12, 2010 10:40:23 PM
 * com.jpcamara.fbplusplus.c2.impl.FacebookChatApi connect INFO: - [version:
 * 0][name: xs][value: d6c61c0d603757775f3b59926eb5cd6b][domain:
 * .facebook.com][path: /][expiry: null] // aLogger.log(Level.INFO,
 * "Post logon cookies:"); // List<Cookie> cookies = ((DefaultHttpClient)
 * httpClient).getCookieStore().getCookies(); // if (cookies.isEmpty()) { //
 * aLogger.log(Level.INFO, "No Cookies"); // } else { // for (int i = 0; i <
 * cookies.size(); i++) { // aLogger.log(Level.INFO, "- " +
 * cookies.get(i).toString()); // } // }
 */

// <a href="http://www.facebook.com/profile.php?id=xxxxxxxxx"
// class="profile_nav_link">
/*
 * String uidPrefix = "<a href=\"http://www.facebook.com/profile.php?id=";
 * String uidPostfix = "\" class=\"profile_nav_link\">";
 * //getMethodResponseBody.lastIndexOf(str, fromIndex) int uidPostFixPos =
 * getMethodResponseBody.indexOf(uidPostfix); if(uidPostFixPos >= 0){ int
 * uidBeginPos = getMethodResponseBody.lastIndexOf(uidPrefix, uidPostFixPos) +
 * uidPrefix.length(); if(uidBeginPos < uidPrefix.length()){
 * logger.error("Can't get the user's id! Exit."); return
 * FacebookErrorCode.Error_System_UIDNotFound; } uid =
 * getMethodResponseBody.substring(uidBeginPos, uidPostFixPos);
 * logger.info("UID: " + uid); }else{
 * logger.error("Can't get the user's id! Exit."); return
 * FacebookErrorCode.Error_System_UIDNotFound; }
 */
