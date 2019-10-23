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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Handles a socket connection to the proxy server from the client and uses 2
 * threads to proxy between server and client
 *
 * @author Farouk Korteby farouk.korteby@gmail.com
 * @link https://github.com/fkorteby/
 * 
 */
class ProxyThread extends Thread {
	
	private Socket serverSocket;
	private List<String> serverConfigList;
	
	private static final Logger log = Logger.getLogger(ProxyThread.class);
	

	/**
	 * Instantiates a new thread proxy.
	 *
	 * @param sClient the s client
	 * @param serverConfigList the server config list
	 */
	ProxyThread(Socket sClient, List<String> serverConfigList) {
		this.serverConfigList = serverConfigList;
		this.serverSocket = sClient;
	}
	
	/**
	 * Instantiates a new thread proxy.
	 *
	 * @param serverConfigList the server config list
	 */
	ProxyThread(List<String> serverConfigList) {
		this.serverConfigList = serverConfigList;
	}

	/**
	 * Sets the server socket.
	 *
	 * @param serverSocket the new server socket
	 */
	public void setServerSocket(Socket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	/**
	 * Builds the socket from serverConfigList.
	 *
	 * @return the socket
	 */
	private boolean buildSocketServer() {		
		// try one by one until get successful connection
		for (String serverConfig : this.serverConfigList) {
			// connects a socket to the server
			try {
				//Socket server = new Socket(getIP(serverConfig), getPort(serverConfig));
				this.serverSocket = new Socket();
				this.serverSocket.connect(new InetSocketAddress(getIP(serverConfig),  getPort(serverConfig)), ProxyServer.SOCKET_TIMOUT);
				
				if(log.isDebugEnabled())
					log.debug("The connction to the Server "+serverConfig+" is successfully established.");
				
				return true; 
			} catch (SocketException e) {
				// if the connection is already established return true
				if(e.getMessage().contains("already connected")) return true;
			} catch (Exception e) {
				if(log.isDebugEnabled())
					log.debug("Server "+serverConfig+" is not accessible, try next in the list.");
			}
		}
		// If all servers are unavailable return null
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			// Initialize variables
			final byte[] request = new byte[1024];
			byte[] reply = new byte[4096];
			final InputStream inFromClient = serverSocket.getInputStream();
			final OutputStream outToClient = serverSocket.getOutputStream();
			Socket client = null;
			
			// connects a socket to the server
			// if server is unavailable throw exception
			if(!buildSocketServer()){
				PrintWriter out = new PrintWriter(new OutputStreamWriter(outToClient));
				out.flush();
				throw new RuntimeException("All given servers configurations are down, please check and try again.");
			}
			
			// a new thread to manage streams from server to client (DOWNLOAD)
			final InputStream inFromServer = this.serverSocket.getInputStream();
			final OutputStream outToServer = this.serverSocket.getOutputStream();
			
			// a new thread for uploading to the server
			new Thread() {
				public void run() {
					int bytes_read;
					try {
						while ((bytes_read = inFromClient.read(request)) != -1) {
							outToServer.write(request, 0, bytes_read);
							outToServer.flush();
							// ------------------------------ //
							// YOU CAN CREATE YOUR LOGIC HERE
							// ------------------------------ //
							if(log.isDebugEnabled())
								log.debug("read from client and write to the server size="+bytes_read);
						}
					} catch (IOException e) {
					}
					try {
						outToServer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
			// current thread manages streams from server to client (DOWNLOAD)
			int bytes_read;
			try {
				while ((bytes_read = inFromServer.read(reply)) != -1) {
					outToClient.write(reply, 0, bytes_read);
					outToClient.flush();
					// ------------------------------ //
					// YOU CAN CREATE YOUR LOGIC HERE
					// ------------------------------ //
					if(log.isDebugEnabled())
						log.debug("read from server and write to the client size="+bytes_read);
				}
			} catch (IOException e) {
				log.error(e);
			} finally {
				try {
					if (this.serverSocket != null)
						this.serverSocket.close();
					if (client != null)
						client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			outToClient.close();
			serverSocket.close();
		} catch (IOException e) {
			log.error(e);
		}
	}
	
	/**
	 * Gets the port.
	 *
	 * @param serverConfig the server config
	 * @return the port
	 */
	public static int getPort(String serverConfig){
		return Integer.parseInt(serverConfig.split(":")[1]);
	}
	
	/**
	 * Gets the ip.
	 *
	 * @param serverConfig the server config
	 * @return the ip
	 */
	public static String getIP(String serverConfig){
		return serverConfig.split(":")[0];
	}
}
