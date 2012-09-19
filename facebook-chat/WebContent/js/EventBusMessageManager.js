/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

var EventBusMessageManager = (function(){

    var pollTheServer = function(){
        EventBusController.request({
            params: {
                FBUserKey: Ext.get('email').getValue()
            }
        });
    }

    var manageMessage = function(message){
        var command = message.command;

        if(command == 'CHATROOM_JOIN'){
            command_chatroomjoin(message);
        }

        if(command == 'CHATROOM_LEAVE'){
            command_chatroomleave(message);
        }

        if(command == 'CHATROOM_SENDMESSAGE'){
            command_chatroomsendmessage(message)
        }

    
    };

    var command_chatroomsendmessage = function(message){

    };

    var command_chatroomjoin = function(message){
        var data = {
            "members":  Ext.util.JSON.decode(response.members)

        }

        chatroomMemberDataStore.loadData(data,true);
    };

    var command_chatroomleave = function(message){
        var data = {
            "members":  Ext.util.JSON.decode(response.members)

        }

        chatroomMemberDataStore.loadData(data);
    };

    return{
        manageMessage : function(message){
            var messages = new Array(message);
            var messageCount = messages.length;

            for(var i = 0; i < messageCount; i++){
                manageMessage(messages[i]);
            }

            pollTheServer();
        }
    }
    
}())

