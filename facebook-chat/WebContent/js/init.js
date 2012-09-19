(function() {
	var connector = Connector;

	// var userIdKey = "";
	// var refHost = "localhost";
	var refHost = "70.188.154.244";
	var refPort = "1111";

	window.chatroomMemberListStore = new Ext.data.JsonStore( {
		root : 'members',
		fields : [ {
			name : 'FBUserKey',
			type : 'string'
		}, {
			name : 'name',
			type : 'string'
		} ]
	});

	window.chatroomListStore = new Ext.data.JsonStore( {
		root : 'chatrooms',
		fields : [ {
			name : 'FBUserKey',
			type : 'String'
		}, {
			name : 'name',
			type : 'string'
		} ]
	});

	window.buddyListStore = new Ext.data.JsonStore( {
		root : 'buddies',
		fields : [ {
			name : 'buddy',
			type : 'string'
		} ]
	});

	Ext.onReady(function() {

		var loginPanel = new LoginPanelUi( {
			id : 'loginPanelID',
			renderTo : 'loginPanelDIV'
		});

		connector.receive.request( {
			command : Command.GET_ID,
			callback : function() {
				console.log('success!');
				connector.receive.request( {
					command : Command.OPEN_COMM,
					callback : function() {
						console.log('opened comm.');
					}
				});
			}
		});
	});
})();