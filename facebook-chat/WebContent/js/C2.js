/**
 * Defining the namespaces for the application. FacebookPP (aka, Facebook++),
 * and C2, since it's the C2 architectural style
 * 
 * @author jpcamara
 */
var Command = {
	GET_BUDDY_LIST : 'getBuddyList',
	GET_ID : 'getId',
	LOGIN : 'login',
	LOGOUT : 'logout',
	OPEN_COMM : 'openComm',
	CREATE_CHATROOM : 'createChatroom',
	JOIN_CHAT : 'joinChat',
	LEAVE_CHAT : 'leaveChat',
	RETRIEVE_CHATROOMS : 'retrieveChatroomList',
	SEND_CHAT : 'sendChat',
	RECEIVE_CHAT : 'receiveChat'
};

var GUIComponent, Connector;

var GUIComponent = {
	strategies : new function Strategy() {
		this[Command.LOGIN] = function(message) {
			if (message.success == "1") {
				var loginPanel = Ext.getCmp('loginPanelID');
				loginPanel.hide();
				var myViewport = new MyViewportUi( {
					renderTo : Ext.getBody(),
					id : 'viewportID'
				});
				myViewport.show();
				loader.hideLoader();
			} else {
				alert(message.message);
				loader.hideLoader();
			}
		};
		this[Command.CREATE_CHATROOM] = function(message) {
			if (message.success == "1") {
				Ext.getCmp('CreateChatID').disable();
				Ext.getCmp('JoinChatID').disable();
				Ext.getCmp('InviteBuddiesID').enable();
				Ext.getCmp('SendID').enable();
				Ext.getCmp('ClearID').enable();

				var data = {
					"members" : message.members
				};
				chatroomMemberListStore.loadData(data);

				alert("The chatroom was successfully created. You can now invite friends to your chat! Click the 'Invite Buddies' button under 'Chatroom Options' to invite friends to your chat.");
				loader.hideLoader();
			} else {
				alert(message.message);
				loader.hideLoader();
			}
		};
		this[Command.SEND_CHAT] = function(message) {
			if (message.success != "1") {
//				var chatOutput = Ext.getCmp('ChatOutputBoxID');
//				chatOutput
//						.update(chatOutput.el.dom.textContent + '<span style="color:red;font-weight:bold;"> This message did not make it to the server</span>');
				var chatOutput = document.getElementById('chatOutputBox');
				chatOutput.innerHTML = chatOutput.innerHTML + '<span style="color:red;font-weight:bold;"> This message did not make it to the server</span>';
			}
//			loader.hideLoader();
		};
		this[Command.RECEIVE_CHAT] = function(message) {
			if (message.success == "1") {
				var chatOutput = document.getElementById('chatOutputBox');
				var message = message.receivedChat && message.receivedChat.message;
				// if there's a message, use it. we can get undefined back if the request was closed and reopened
				if (message) {
					var pieces = message.split('::');
					message = '<span style="color:red; font-weight:bold;">' + pieces[0] + '</span>' + pieces[1];
					chatOutput.innerHTML = chatOutput.innerHTML + '<br/>' + message + '<br/>';
				}
			} else {
				alert(message.message);
			}
		};
		this[Command.LOGOUT] = function(message) {
			var loginPanel = Ext.getCmp('loginPanelID');
			var myViewport = Ext.getCmp('viewportID');
			myViewport.hide();
			document.location = document.location.href;
			loader.hideLoader();
		};
		this[Command.GET_BUDDY_LIST] = function(message) {
			var buddyList = new BuddyListUi( {
				id : 'buddyListID',
				closeAction : 'close'
			});
			buddyList.show();
			var data = message.buddylist;
			buddyListStore.loadData(data);
			loader.hideLoader();
		};
		this[Command.RETRIEVE_CHATROOMS] = function(message) {
			console.log('before constructor');
			var chatroomList = new ChatroomListUi( {
				id : 'chatroomListID',
				closeAction : 'close'
			});
			chatroomList.show();
			var data = {
				"chatrooms" : message.chatrooms
			};
			console.log('before data load');
			chatroomListStore.loadData(data);
			loader.hideLoader();
		};
		this[Command.JOIN_CHAT] = function(message) {
			var chatroomListWindow = Ext.getCmp("chatroomListID");
			if (chatroomListWindow) {
				chatroomListWindow.close();
			}
			Ext.getCmp('SendID').enable();
			Ext.getCmp('ClearID').enable();
			chatroomMemberListStore.loadData(message);
			loader.hideLoader();
		};
		this[Command.LEAVE_CHAT] = function (message) {
			chatroomMemberListStore.loadData(message);
		};
		
	},
	receive : {
		notification : function(message) {
			if (message.command in GUIComponent.strategies) {
				GUIComponent.strategies[message.command](message);
			}
		}
	}
};

/**
 * @author jpcamara
 */
Connector = {
	strategies : new function Strategy() {
		// login!
		this[Command.LOGIN] = function(message) {
			Connector._login(message.data);
		};
		this[Command.GET_BUDDY_LIST] = function(message) {
			Connector._getBuddyList();
		};
		// grab the requestId necessary for comm. with the server
		// but only if we don't already have one
		this[Command.GET_ID] = function(message) {
			var requestId = Ext.util.Cookies.get('requestId');
			// if we don't have a requestId, ask the app for one
			if (requestId == null) {
				Connector._requestId(message.callback);
			} else {
				if (message.callback) {
					message.callback();
				}
			}
		};
		// open a communication with the server, to enable comet (aka,
		// server-push)
		// style communication
		this[Command.OPEN_COMM] = function(message) {
			if (Connector.commOpen == false) {
				Connector._openComet();
			}
			if (message.callback) {
				message.callback();
			}
		};

		this[Command.CREATE_CHATROOM] = function(message) {
			Connector._createChatroom();
		};
		this[Command.LOGOUT] = function(message) {
			Connector._logout();
		};
		this[Command.SEND_CHAT] = function(message) {
			Connector._sendChat(message.data);
		};
		this[Command.RETRIEVE_CHATROOMS] = function(message) {
			Connector._retrieveChatrooms();
		};
		this[Command.JOIN_CHAT] = function(message) {
			Connector._joinChat(message.data);
		};
	},
	commOpen : false,
	// top: send requests
	// bottom: send notifications
	send : {
		_request : function(message) {

		},
		_notification : function(message) {
			GUIComponent.receive.notification(message);
		}
	},
	// top: receive notifications
	// bottom: receive requests
	receive : {
		request : function(message) {
			// if it has the appropriate strategy, call it
			if (message.command in Connector.strategies) {
				Connector.strategies[message.command](message);
			}
		}
	},
	_logout : function() {
		var me = this;
		data = {
			command : Command.LOGOUT
		};
		Ext.Ajax.request( {
			timeout : 60000,
			url : 'App',
			method : 'POST',
			params : data,
			success : function(response, options) {
				console.log(response.responseText);
				var json = JSON.parse(response.responseText);
				if (json.success != "1") {
					alert('failed to logout');
					loader.hideLoader();
				}
			},
			failure : function(response, options) {
				console.log('no logout for you.');
			}
		});
	},
	_joinChat : function(data) {
		var me = this;
		data.command = Command.JOIN_CHAT;
		Ext.Ajax.request( {
			timeout : 60000,
			url : 'App',
			method : 'POST',
			params : data,
			success : function(response, options) {
				console.log(response.responseText);
				var json = JSON.parse(response.responseText);
				if (json.success != "1") {
					alert('failed to join chatroom');
					loader.hideLoader();
				}
			},
			failure : function(response, options) {
				console.log('no chatroom for you.');
			}
		});
	},
	_retrieveChatrooms : function() {
		var me = this;
		data = {
			command : Command.RETRIEVE_CHATROOMS
		};
		// puts it in a java format

		Ext.Ajax.request( {
			timeout : 60000,
			url : 'App',
			method : 'POST',
			params : data,
			success : function(response, options) {
				console.log(response.responseText);
				var json = JSON.parse(response.responseText);
				if (json.success != "1") {
					alert('failed to retrieve chatrooms');
					loader.hideLoader();
				}
			},
			failure : function(response, options) {
				console.log('no chatrooms for you.');
			}
		});
	},
	// expected data: {command:String, data: { userIds: [], content: String }}
	_sendChat : function(data) {
		var me = this;
		data.command = Command.SEND_CHAT;
		// puts it in a java format

		Ext.Ajax.request( {
			timeout : 60000,
			url : 'App',
			method : 'POST',
			params : data,
			success : function(response, options) {
				console.log(response.responseText);
				var json = JSON.parse(response.responseText);
				if (json.success != "1") {
					alert('failed to send chat message');
					loader.hideLoader();
				}
			},
			failure : function(response, options) {
				console.log('no chatting for you.');
			}
		});
	},
	_getBuddyList : function() {
		var me = this;
		data = {
			command : Command.GET_BUDDY_LIST
		};
		Ext.Ajax.request( {
			timeout : 60000,
			url : 'App',
			method : 'POST',
			params : data,
			success : function(response, options) {
				console.log(response.responseText);
				var json = JSON.parse(response.responseText);
				if (json.success != "1") {
					alert('failed to get buddy list');
					loader.hideLoader();
				}
			},
			failure : function(response, options) {
				console.log('no buddy list for you.');
			}
		});
	},
	_createChatroom : function() {
		var me = this;
		data = {
			command : Command.CREATE_CHATROOM
		};
		Ext.Ajax.request( {
			timeout : 60000,
			url : 'App',
			method : 'POST',
			params : data,
			success : function(response, options) {
				console.log(response.responseText);
				var json = JSON.parse(response.responseText);
				if (json.success != "1") {
					alert('failed to create chatroom');
					loader.hideLoader();
				}
			},
			failure : function(response, options) {
				console.log('no chatroom for you');
			}
		});
	},
	_login : function(data) {
		var me = this;
		data.command = Command.LOGIN;
		Ext.Ajax.request( {
			timeout : 60000,
			url : 'App',
			method : 'POST',
			params : data,
			success : function(response, options) {
				console.log(response.responseText);
				var json = JSON.parse(response.responseText);
				if (json.success != "1") {
					alert('failed to login');
					loader.hideLoader();
				}
			},
			failure : function(response, options) {
				console.log('login failure');
			}
		});
	},
	_openComet : function() {
		var me = this;
		me.i = me.i || 0;
		me.i++;
		console.log("attempt " + me.i);
		Ext.Ajax.request( {
			timeout : 60000,
			url : 'App?command=' + Command.OPEN_COMM,
			method : 'GET',
			success : function(response, options) {
				console.log(response.responseText);
				console.log('before JSON.parse');
				var json = JSON.parse(response.responseText);
				console.log('after JSON.parse');
				me.send._notification(json);
				me._openComet();
			},
			failure : function(response, options) {
				me._openComet();
			}
		});
	},
	// requesting the id to comm. with the server
	_requestId : function(callback) {
		var me = this;
		Ext.Ajax
				.request( {
					timeout : 60000,
					url : 'App?command=' + Command.GET_ID,
					method : 'GET',
					success : function(response, options) {
						console.log(response.responseText);
						var json = JSON.parse(response.responseText);
						console.log("id: " + json.requestId + " message: "
								+ json.message + " success: " + json.message);
						if (json.success == 0) {
							me._requestId();
						} else {
							if (callback) {
								callback();
							}
						}
					},
					failure : function(response, options) {
						alert('there was a problem communicating with the app. please refresh your browser');
					}
				});
	}
};