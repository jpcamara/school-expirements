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
            var error = XmlTools.getResponseException(response.responseXML);
            ErrorHandler.displayError(error);
            ErrorHandler.hideLoader();
        }
    };

    manager.put(0,nullResponseConfig); //this never gets removed!

    var responseCallback = function(processStatus,responseId,response){
        var responseInst = manager.get(responseId);
        if(processStatus){
            ErrorHandler.hideLoader();
            responseInst.success(response);
        }else{
            var ExceptionID = XmlTools.getResponseExceptionId(response.responseXML);
            if(ExceptionID == 'ST010'){ //Session Expired
                ErrorHandler.handleExpiredSession();
                return;
            }
            ErrorHandler.hideLoader();
            ErrorHandler.log(XmlTools.getResponseLog(response.responseXML) + "<br><br><b>Stack Trace:</b><br>" + XmlTools.getResponseStackTrace(response.responseXML),false);
            responseInst.failure(response);
        }

        if(responseId != 0){
            manager.remove(responseId);
        }
    };

    return{
        request : function(config){
            ErrorHandler.clearLog();

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
                url: '{0}://{1}/{2}?pagename={3}&nextpage={4}&application={5}&service={6}&action={7}&responseId={8}',
                protocol: 'http',
                host: currentHost,//'localhost:8081',
                location: currentApp + '/AWFServlet',
                pageName: 'upsdelegate',
                nextPage: '/ajax-response.jsp',
                application: 'UncollectibleSystem',
                callback: function(options, success, response){
                    var xml = response.responseXML;
                    var successNode = Ext.query('message', xml);
                    var success;
                    var responseId;
                    var processStatus;

                    try{
                        success= successNode[0].getAttribute("success");
                        responseId= successNode[0].getAttribute("responseId");
                    }catch(e){
                        ErrorHandler.hideLoader();
                        ErrorHandler.displayError(ErrorHandler.getSystemErrorMsg);
                        ErrorHandler.log("No detail errors to display.",false);
                        return;
                    }

                    if (!success || success == 'false') {
                        processStatus=false;
                    }else{
                        processStatus=true;
                    }
                    responseCallback(processStatus,responseId,response);
                },
                timeout: 999999
            });
            config.url = String.format(config.url, config.protocol, config.host, config.location, config.pageName, config.nextPage, config.application, config.service, config.action, autoId);
            conn.request(config);
            ErrorHandler.displayLoader();
        }
    }

})();