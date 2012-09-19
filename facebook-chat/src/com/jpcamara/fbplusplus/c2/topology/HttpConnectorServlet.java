package com.jpcamara.fbplusplus.c2.topology;

import static com.jpcamara.fbplusplus.c2.model.Data.REQUEST;
import static com.jpcamara.fbplusplus.c2.model.Data.REQUEST_DATA;
import static com.jpcamara.fbplusplus.c2.model.Data.RESPONSE;
import static com.jpcamara.fbplusplus.c2.model.Data.USER;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.jpcamara.fbplusplus.c2.AppContext;
import com.jpcamara.fbplusplus.c2.contract.Connector;
import com.jpcamara.fbplusplus.c2.contract.Request;
import com.jpcamara.fbplusplus.c2.model.Command;
import com.jpcamara.fbplusplus.c2.model.Data;
import com.jpcamara.fbplusplus.c2.model.User;
import com.jpcamara.fbplusplus.c2.util.LoggerUtil;

/**
 * Entry point into the application. Hands off data it receives to the HttpConnector 
 * 
 * @author johnpcamara
 */
@SuppressWarnings("serial")
@WebServlet(name = "AppServlet", urlPatterns = { "/App" }, asyncSupported = true, loadOnStartup = 1)
public class HttpConnectorServlet extends HttpServlet {// implements Connector {

	private LoggerUtil aLogger = new LoggerUtil(this);
	private static final Map<String, User> users = new ConcurrentHashMap<String, User>();
	static {
		AppContext.instance().setAttribute(Data.USERS, users);
	}

	/**
	 * GET is used to open a communication with the server. The clients will
	 * keep this communication open to allow for server-push notifications
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * POST is where requests from the user come from.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String requestId = retrieveRequestId(request);
		aLogger.info("Command : " + request.getParameter("command"));
		if (isValidRequest(request) == false) {
			rejectRequest(request, response);
			return;
		}

		if (requestId == null) {
			setupCookieId(request, response);
		} else {
			receiveCommand(request, response);
		}
	}

	/**
	 * There has to be a command, and if there isn't a requestId the command
	 * needs to be Command.GET_ID
	 * 
	 * @param request
	 * @return
	 */
	private boolean isValidRequest(HttpServletRequest request) {
		String requestId = retrieveRequestId(request);
		Command command = Command.fromValue(request.getParameter("command"));
		if (requestId == null) {
			return Command.GET_ID == command;
		}
		return command != null;
	}

	private void setupCookieId(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		User user = new User();
		users.put(user.getUniqueId(), user);
		aLogger.info("inside setup cookies: " + users.toString());
		response.setContentType("text/html");

		Cookie requestId = new Cookie("requestId", user.getUniqueId());
		response.addCookie(requestId);

		response.getWriter().print(returnId(user.getUniqueId()));
		response.getWriter().flush();
		request.startAsync().complete();
	}

	/**
	 * Sends a failed response back
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void rejectRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setContentType("text/html");

		response.getWriter().print(returnMessage("0", "Request was invalid"));
		response.getWriter().flush();
		request.startAsync().complete();
	}

	/**
	 * Returns the id in the proper format to the user
	 * 
	 * @param id
	 * @return
	 */
	private String returnId(String id) {
		JSONObject message = new JSONObject();
		try {
			message.put("success", "1");
			message.put("message", "Successfully retrieved an ID");
			message.put("requestId", id);
		} catch (Exception e) {
			aLogger.warning("Failure to request with requestId", e);
		}

		return message.toString();
	}
	
	private String returnMessage(String success, String message) {
		JSONObject json = new JSONObject();
		try {
			json.put("success", success);
			json.put("message", message);
		} catch (Exception e) {
			aLogger.warning("Failure to request with requestId", e);
		}
		return json.toString();
	}

	private String retrieveRequestId(HttpServletRequest request) {
		String requestId = null;
		for (Cookie c : request.getCookies()) {
			if (c.getName().equals("requestId")) {
				requestId = c.getValue();
			}
		}
		return requestId;
	}

	/**
	 * Takes in a request payload, creates a Request object for it, and hands
	 * that off to the bottom domain of this connector
	 * 
	 * @param request
	 * @param response
	 */
	private void receiveCommand(HttpServletRequest request,
			HttpServletResponse response) {
		Command command = Command.fromValue(request.getParameter("command"));
		Request commandRequest = new MessageImpl(command);
		commandRequest.setId(retrieveRequestId(request));
		commandRequest.getData().put(REQUEST, request);
		commandRequest.getData().put(RESPONSE, response);
		
		commandRequest.getData().put(REQUEST_DATA, grabMapContents(request));
		aLogger.info("users overall : " + users.toString());
		commandRequest.getData().put(USER, users.get(commandRequest.getId()));
		Connector http = TopologyRegistry.instance().get("HttpConnector");
		http.receive(commandRequest);
	}	
	
	/**
	 * Grab the contents of the request map that we want, and return them
	 * If you don't do this, and your request/response objects get destroyed, their map
	 * data gets destroyed with them. 
	 * @param request
	 * @return
	 */
	private Map<String, Object> grabMapContents(HttpServletRequest request) {
		Map<String, Object> map = Collections.synchronizedMap(new HashMap<String, Object>());
		Map<String, String[]> requestData = request.getParameterMap();
		if (requestData.get("userName") != null) {
			map.put("userName", requestData.get("userName")[0]);
		}
		if (requestData.get("command") != null) {
			map.put("command", requestData.get("command")[0]);
		}
		if (requestData.get("password") != null) {
			map.put("password", requestData.get("password")[0]);
		}
		if (requestData.get("userIds") != null) {
			String[] requestIds = requestData.get("userIds");
			String[] ids = new String[requestIds.length];
			System.arraycopy(requestIds, 0, ids, 0, ids.length);
			map.put("userIds", Arrays.asList(ids));
		}
		if (requestData.get("chatContent") != null) {
			map.put("chatContent", requestData.get("chatContent")[0]);
		}
		if (requestData.get("chatRoomId") != null) {
			map.put("chatRoomId", requestData.get("chatRoomId")[0]);
		}
		aLogger.info("grabMapContents: " + map.toString());
		return map;
	}
}