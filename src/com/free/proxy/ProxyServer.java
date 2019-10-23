/*
 * Copyright 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.free.proxy;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * This class implements a simple multi-threaded proxy server including failed over scenarios.
 * 
 *
 * @author Farouk Korteby farouk.korteby@gmail.com
 * @link https://github.com/fkorteby/
 * 
 */
public class ProxyServer {

	/** The JDBC proxy port. */
	public static int JDBCProxyPORT = 100;
	
	/** The socket timout. */
	public static int SOCKET_TIMOUT = 100;

	static List<String> serverConfigList = new ArrayList();
	static ServerSocket server;

	private static final Logger log = Logger.getLogger(ProxyServer.class);

	/** The main method parses arguments and passes them to runServer */
	public static void main(String[] args) {
		try {
			// Log4j configuration
			// Switch to file configuration
			// BasicConfigurator.configure();

			// Check the arguments
			if (args.length < 2)
				throw new IllegalArgumentException("insuficient arguments");

			// Read the port and timeout
			JDBCProxyPORT = Integer.parseInt(args[0].split(":")[0]);
			SOCKET_TIMOUT = Integer.parseInt(args[0].split(":")[1]);

			// Retrieve the servers ip and ports list
			for (int i = 0; i < args.length; i++) {
				if (i > 0) {
					serverConfigList.add(args[i]);
				}
			}

			// Print a start-up message
			log.info("Starting proxy on port " + JDBCProxyPORT);
			server = new ServerSocket(JDBCProxyPORT);

			// Wait until finish
			while (true) {
				// Refresh the server connection
				if (server.isClosed())
					server = new ServerSocket(JDBCProxyPORT);

				// Wait for a connection on the local port
				Socket client = server.accept();

				// Create the ThreadProxy
				ProxyThread proxy = new ProxyThread(client, serverConfigList);

				// Start treatment
				proxy.start();
			}

		} catch (Exception e) {
			log.error(e);
			log.error("Usage: java -jar proxy.jar <local-port>:<time-out>"
					+ "<host1>:<remote-port1> <host2>:<remote-port2> ...");
		}
	}

}