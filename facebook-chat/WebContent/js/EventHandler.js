var event = {
    ChatroomSendMessage : function(msg){

    },
    Signoff : function(){
         AjaxController.request({
            event: 'ChatroomLeave',
             params: {
                FBUserKey: Ext.get('email').getValue()
            },
            responseSuccess: function(response){
                var chatroom = Ext.getCmp("ChatroomID");
                chatroom.close();
                loginPanel.show();

            },
            responseFailure: function(response){
                alert('There was an error trying to create your chatroom: ' + response.error);
            }
        })
    },
    ChatroomJoin :  function(chatName){
        AjaxController.request({
            event: 'ChatroomJoin',
            params: {
                FBUserKey: Ext.get('email').getValue(),
                name: chatName
            },
            responseSuccess: function(response){
                Ext.getCmp('CreateChatID').disable();
                Ext.getCmp('JoinChatID').disable();
                Ext.getCmp('InviteBuddiesID').disable();

                 var data = {
                    "members":  Ext.util.JSON.decode(response.members)

                }
                chatroomMemberListStore.loadData(data,true);

            },
            responseFailure: function(response){
                alert('There was an error trying to create your chatroom: ' + response.error);
            }
        })
    },
    ChatroomCreate : function(){
        AjaxController.request({
            event: 'ChatroomCreate',
            params: {
                FBUserKey: Ext.get('email').getValue()
            },
            responseSuccess: function(response){
                Ext.getCmp('CreateChatID').disable();
                Ext.getCmp('JoinChatID').disable();
                Ext.getCmp('InviteBuddiesID').enable();
                //alert(response);
 

                var data = {
                    "members":  Ext.util.JSON.decode(response.members)

                }
                chatroomMemberListStore.loadData(data);

                alert("The chatroom was successfully created. You can now invite friends to your chat! Click the 'Invite Buddies' button under 'Chatroom Options' to invite friends to your chat.");
            },
            responseFailure: function(response){
                alert('There was an error trying to create your chatroom: ' + response.error);
            }
        })
    },
    Authorize : function(parmEmail,parmPassword){
        AjaxController.request({
            event: 'FacebookAuthorize',
            params: {
                email: parmEmail,
                password: parmPassword
            },
            responseSuccess: function(response){
                var loginPanel = Ext.getCmp('loginPanelID');
                loginPanel.hide();
                var myViewport = new MyViewportUi({
                    id: 'chatroomID',
                    renderTo: Ext.getBody()
                });
                myViewport.show();
                userIdKey=parmEmail;
            },
            responseFailure: function(response){
                alert('we got an exception!!!!!!!!!!!!!!');
            }
        })
    },
    RetrieveChatroomList : function(){
        AjaxController.request({
            event: 'FacebookRetrieveChatroomList',
            params: {
                FBUserKey: Ext.get('email').getValue()
            },
            responseSuccess: function(response){
                var chatroomList = new ChatroomListUi({
                    id: 'chatroomListID',
                    closeAction: 'close'

                })
                chatroomList.show();
                var data = {
                    "chatrooms":  Ext.util.JSON.decode(response.chatrooms)

                }
                chatroomListStore.loadData(data);
            },
            responseFailure: function(response){
                alert('we got an exception!!!!!!!!!!!!!!');
            }
        })
    },
    RetrieveBuddyList : function(){
        AjaxController.request({
            event: 'FacebookRetrieveBuddylist',
            params: {
                FBUserKey: Ext.get('email').getValue()
            },
            responseSuccess: function(response){
                var buddyList = new BuddyListUi({
                    id: 'buddyListID',
                    closeAction: 'close'

                })
                buddyList.show();
                /* var data =
                {
                    "buddies":[{
                        "buddy":"test1"
                    },{
                        "buddy":"test2"
                    },{
                        "buddy":"test3"
                    }]
                    };*/
                var data = response.buddylist;
                buddyListStore.loadData(data);
            },
            responseFailure: function(response){
                alert('we got an exception!!!!!!!!!!!!!!');
            }
        })
    }
}




