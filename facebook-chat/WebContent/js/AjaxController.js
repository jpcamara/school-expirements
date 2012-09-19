var AjaxController = (function(){
    var conn = new Ext.data.Connection();
    var autoId = 0;

    var manager = (function(){
        var items = [];
        return{
            get: function(name) {
                return items[name];
            },
            put: function(name, obj) {
                items[name] = obj;
            },
            remove: function(id){
                delete items[id];
            }
        }
    })();

    var nullResponseConfig = {
        id: 0,
        success: null, /* never going to be a success. That's the point of this. */
        failure: function(response){
            //var error = XmlTools.getTextFromNode('error',response.responseXML);
            //var error = XmlTools.getResponseException(response.responseXML);
            //alert("error: " +  response);
            alert(response.error);
            loader.hideLoader();
        }
    };

    manager.put(0,nullResponseConfig); //this never gets removed!

    var responseCallback = function(processStatus,responseId,response){
        var responseInst = manager.get(responseId);
        if(processStatus){
            loader.hideLoader();
            responseInst.success(response);
        }else{
           /* var ExceptionID = XmlTools.getResponseExceptionId(response.responseXML);
            if(ExceptionID == 'ST010'){ //Session Expired
                ErrorHandler.handleExpiredSession();
                return;
            }*/
            loader.hideLoader();
            //alert("failed response")
            responseInst.failure(response);
        }

        if(responseId != 0){
            manager.remove(responseId);
        }
    };

    return{
        request : function(config){
            if(autoId == (Number.MAX_VALUE-1)){
                autoId = 0;
            }
            autoId+=1;

            var responseConfig = {
                id: autoId,
                success: config.responseSuccess,
                failure: config.responseFailure
            };
            manager.put(responseConfig.id,responseConfig);

            if(config.listeners){
                if(config.listeners.beforerequest){
                    config.listeners.beforerequest(config);
                    config.listeners = null;
                    delete config.listeners;
                }
            }

            Ext.applyIf(config, {
                method: 'post',
                url: '{0}://{1}/{2}?event={3}&responseId={4}',
                protocol: 'http',
                host: refHost + ":" + refPort,
                location: 'FBPP/EventDelegateServlet',
                pageName: 'upsdelegate',
                callback: function(options, success, response){

                    /*var processedTransData =  "{response: " + Ext.util.JSON.encode(action.result.ProcessedTransHistory) + "}";
                    var pendingTransData =  "{response: " + Ext.util.JSON.encode(action.result.PendingTransHistory) + "}";
                    var advAcctDataFields = action.result.AdvAcctDataFields;
                    var amicaAcctDataFields = action.result.AmicaAcctDataFields;*/

                    //var xml = response.responseXML;
                   // var successNode = Ext.query('message', xml);

                    var jsonResponse = Ext.util.JSON.decode(response.responseText);
                    var responseId;
                    var processStatus;

                    try{
                        //alert(response.responseText);
                        success = jsonResponse.success;
                        responseId= jsonResponse.responseId;
                    }catch(e){
                        loader.hideLoader();
                        //alert('errors happened');
                        return;
                    }

                    if (!success || success == 'false') {
                        processStatus=false;
                    }else{
                        processStatus=true;
                    }
                    responseCallback(processStatus,responseId,jsonResponse);
                },
                timeout: 999999
            });
            config.url = String.format(config.url, config.protocol, config.host, config.location,config.event, autoId);
            conn.request(config);
            loader.displayLoader();
        }
    }

}());
