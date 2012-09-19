/*
 * File: LoginPanel.ui.js
 * Date: Mon Apr 12 2010 18:22:10 GMT-0400 (Eastern Daylight Time)
 * 
 * This file was generated by Ext Designer version xds-1.0.0.7c.
 * http://www.extjs.com/products/designer/
 *
 * This file will be auto-generated each and everytime you export.
 *
 * Do NOT hand edit this file.
 */

LoginPanelUi = Ext.extend(Ext.Panel, {
	title : 'Sign into FacebookExt',
	width : 311,
	height : 185,
	initComponent : function() {
		this.bbar = {
			xtype : 'toolbar',
			items : [ {
				xtype : 'buttongroup',
				title : 'Signing in',
				columns : 2,
				items : [ {
					xtype : 'button',
					text : 'Sign in',
					handler : function() {
						var emailAddr = Ext.get('email').getValue();
						var pass = Ext.get('password').getValue();

						Connector.receive.request( {
							command : Command.LOGIN,
							data : {
								userName : emailAddr,
								password : pass
							},
							callback : function() {
								loader.hideLoader();
							}
						});
						loader.displayLoader();

					}
				}, {
					xtype : 'button',
					text : 'Reset'
				} ]
			}, {
				xtype : 'buttongroup',
				title : 'Help',
				columns : 2,
				items : [ {
					xtype : 'button',
					text : 'Docs'
				}, {
					xtype : 'button',
					text : 'About'
				} ]
			} ]
		};
		this.items = [ {
			xtype : 'fieldset',
			title : 'Facebook Signon Information',
			layout : 'form',
			items : [ {
				id : 'email',
				xtype : 'textfield',
				fieldLabel : 'Email Address',
				anchor : '100%'
			}, {
				id : 'password',
				xtype : 'textfield',
				fieldLabel : 'Password',
				anchor : '100%',
				inputType: 'password'
			} ]
		} ];
		LoginPanelUi.superclass.initComponent.call(this);
	}
});
