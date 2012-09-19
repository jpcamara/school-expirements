/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpcamara.fbplusplus.c2.topology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jpcamara.fbplusplus.c2.AppContext;
import com.jpcamara.fbplusplus.c2.contract.Connector;
import com.jpcamara.fbplusplus.c2.contract.Domain;
import com.jpcamara.fbplusplus.c2.contract.Notification;
import com.jpcamara.fbplusplus.c2.contract.Request;
import com.jpcamara.fbplusplus.c2.contract.Topology;
import com.jpcamara.fbplusplus.c2.model.Command;
import com.jpcamara.fbplusplus.c2.model.Data;
import com.jpcamara.fbplusplus.c2.util.LoggerUtil;

/**
 * 
 * @author johnpcamara
 */
public class HttpConnector implements Connector {

	private LoggerUtil aLogger = new LoggerUtil(this);
	private Domain<Request, Notification> topDomain;
	private Domain<Notification, Request> bottomDomain;
	private List<Topology> topComponents;
	private List<Topology> bottomComponents;
	private final Queue<Request> requests = new ConcurrentLinkedQueue<Request>();
	private final Queue<Notification> notifications = new ConcurrentLinkedQueue<Notification>();

	public HttpConnector() {
		topDomain = new TopDomain(this);
		bottomDomain = new BottomDomain(this);
		topComponents = new ArrayList<Topology>();
		bottomComponents = new ArrayList<Topology>();
	}

	public Domain<Request, Notification> topDomain() {
		return topDomain;
	}

	public Domain<Notification, Request> bottomDomain() {
		return bottomDomain;
	}

	@SuppressWarnings("unchecked")
	public void send(Notification aNotification) {
		aLogger.info("HttpConnector: send(Notification) " + aNotification);
		Queue<Notification> notifications = (Queue<Notification>) AppContext
				.instance().getAttribute(Data.NOTIFICATIONS);
		notifications.add(aNotification);
	}

	@SuppressWarnings("unchecked")
	public void send(Request aRequest) {
		HttpServletRequest request = (HttpServletRequest) aRequest.getData()
				.get(Data.REQUEST);
		HttpServletResponse response = (HttpServletResponse) aRequest.getData()
				.get(Data.RESPONSE);
		// HttpSession session = request.getSession();

		final AsyncContext async = request.startAsync(request, response);
		async.setTimeout(25000L);

		/**
		 * If we're opening up a communication, add the AsyncContext to the
		 * queue and return. This way the context will continue to exist until
		 * it's given an appropriate reason to complete()
		 */
		if (aRequest.getCommand() == Command.OPEN_COMM) {
			final Queue<AsyncContext> jobs = (Queue<AsyncContext>) AppContext
					.instance().getAttribute(Data.JOBS);
			jobs.add(async);
			async.addListener(new AsyncListener() {
				@Override
				public void onComplete(AsyncEvent event) throws IOException {
					aLogger.info("closing connection on completion");
					jobs.remove(async);
				}

				@Override
				public void onError(AsyncEvent event) throws IOException {
					aLogger.info("closing connection on error");
					jobs.remove(async);
				}

				@Override
				public void onStartAsync(AsyncEvent event) throws IOException {
				}

				@Override
				public void onTimeout(AsyncEvent event) throws IOException {
					aLogger.info("closing connection on timeout");
					jobs.remove(async);
				}
			});
			aLogger.info("adding the communication to the queue");
			return;
			/**
			 * For everything else, we'll return immediately with a success as
			 * an acknowledgment of the request. The response to these calls
			 * will go back through the AsyncContext objects that are in the
			 * "jobs" Queue once they've finished their processing
			 */
		} else {
			try {
				response.setContentType("text/html");
				response.getWriter().print("{\"success\": \"1\"}");
				response.getWriter().flush();
			} catch (Exception e) {
				aLogger.warning("Could not write response message");
			}
			async.complete();
		}

		aRequest.getData().remove(Data.REQUEST);
		aRequest.getData().remove(Data.RESPONSE);

		for (Topology c : topComponents) {
			c.bottomDomain().receive(aRequest);
		}
	}

	public void receive(Notification aNotification) {
		notifications.add(aNotification);
	}

	public void receive(Request aRequest) {
		// requests.add(aRequest);
		send(aRequest);
	}

	public void run() {
		while (true) {
			synchronized (requests) {
				if (requests.isEmpty() == false) {
					send(requests.poll());
				}
			}
			synchronized (notifications) {
				if (notifications.isEmpty() == false) {
					send(notifications.poll());
				}
			}
		}
	}

	public void bindTopTopology(Topology c) {
		topComponents.add(c);
	}

	public void bindBottomTopology(Topology c) {
		bottomComponents.add(c);
	}

}
