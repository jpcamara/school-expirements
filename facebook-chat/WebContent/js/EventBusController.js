var AjaxController = (function(){
    var conn = new Ext.data.Connection();
    

    return{
        request : function(config){
           
            if(config.listeners){
                if(config.listeners.beforerequest){
                    config.listeners.beforerequest(config);
                    config.listeners = null;
                    delete config.listeners;
                }
            }

            Ext.applyIf(config, {
                method: 'post',
                url: '{0}://{1}/{2}?event={3}',
                protocol: 'http',
                host: refHost + ":" + refPort,
                location: 'FBPP/EventDelegateServlet',
                pageName: 'upsdelegate',
                event: 'EventBusStream',
                callback: function(options, success, response){
                    var jsonResponse = Ext.util.JSON.decode(response.responseText);
                    var responseId;
                    var processStatus;

                    try{
                        //alert(response.responseText);
                        success = jsonResponse.success;
                        responseId= jsonResponse.responseId;
                    }catch(e){
                        //alert('errors happened');
                        return;
                    }

                    if (!success || success == 'false') {
                        alert('failed on callback response.');
                    }else{
                      EventBusMessageManager.manageMessage(jsonResponse);
                    }
                    
                },
                timeout: 999999
            });
            config.url = String.format(config.url, config.protocol, config.host, config.location,config.event);
            conn.request(config);
        }
    }

}());
