var loader = (function(){

    var loaderWin;
    var errorWin;

    return {
        displayLoader : function(){
            if(errorWin){
                errorWin.hide();
            }

            if(!loaderWin){
                loaderWin=new Ext.Window({
                    applyTo     : 'load-handler',
                    layout      : 'fit',
                    width       : 200,
                    height      : 60,
                    closeAction :'hide',
                    plain       : true,
                    modal       : true,
                    maximizable : false,
                    draggable	: false,
                    closable 	: false,
                    resizable 	: false,
                    items       : new Ext.Panel({
                        id: 'loadHandlerPanel',
                        width: 150,
                        html: '<p><center><table border="0" cellpadding="4" cellspacing="4"><tr><td><img src="images/loading.gif"></td><td><font color="black" size="2"><b>Processing Request...</b></font></td></tr></table></center></p>'
                    })
                });
            }

            loaderWin.restore();
            loaderWin.show();
        },
        hideLoader : function(){
            loaderWin.hide();
        },
        displayError : function(msg){
            if(loaderWin){
                loaderWin.hide();
            }
            if(!errorWin){
                errorWin=new Ext.Window({
                    applyTo     : 'error-handler',
                    layout      : 'fit',
                    width       : 450,
                    height      : 350,
                    closeAction :'hide',
                    plain       : true,
                    modal		: true,
                    maximizable : true,
                    draggable	: false,
                    closable 	: true,
                    items       : {
                        id: 'errorMsgPanelId',
                        width: 450,
                        html: '<div id="errorMsgDiv"></div>'
                    }
                });

                document.getElementById('errorMsgDiv').innerHTML = '<p>' + msg + '</p>';

            }

            errorWin.restore();
            errorWin.show();
        }

    }

}());