/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.standalone;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;

import com.mysql.management.driverlaunched.ServerLauncherSocketFactory;

/**
 * Manages an embedded tomcat instance.
 */
public class TomcatManager {
	
	private Embedded container = null;
	
	/**
	 * Creates a single webapp configuration to be run in Tomcat.
	 * 
	 * @param contextName the context name without leading slash, for example, "openmrs"
	 * @param port the port at which to run tomcat.
	 */
	public TomcatManager(String contextName, int port) {
		
		// create server
		container = new Embedded();
		container.setCatalinaHome("tomcat");
		
		// create context
		Context rootContext = container.createContext("/" + contextName, contextName);
		rootContext.setReloadable(true);
		
		// create host
		Host localHost = container.createHost("localhost", "webapps");
		localHost.addChild(rootContext);
		
		// create engine
		Engine engine = container.createEngine();
		engine.setName("Catalina");
		engine.addChild(localHost);
		engine.setDefaultHost(localHost.getName());
		container.addEngine(engine);
		
		// create http connector
		Connector httpConnector = container.createConnector((InetAddress) null, port, false);
		container.addConnector(httpConnector);
	}
	
	/**
	 * Starts the embedded Tomcat server.
	 */
	public void run() throws LifecycleException, MalformedURLException {
		container.setAwait(true);
		container.start();
	}
	
	/**
	 * Stops the embedded Tomcat server.
	 */
	public boolean stop() {
		
		boolean stopMySql = false;
		
		//stop tomcat.
		try {
			if (container != null) {
				container.stop();
				container = null;
				stopMySql = true;
			}
		}
		catch (LifecycleException exception) {
			System.out.println("Cannot Stop Tomcat" + exception.getMessage());
			return false;
		}
		
		//stop mysql.
		if(stopMySql)
			StandaloneUtil.stopMySqlServer();
		
		return true;
	}
}
