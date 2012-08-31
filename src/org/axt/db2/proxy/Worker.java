package org.axt.db2.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class Worker {
	
	public static int counter = 0;

	private Socket inSocket = null;
	private Socket outSocket = null;
	
	private final String host;
	
	private final int port;
	
	@Inject
	Worker(@Named("host") String host, @Named("port") int port) {
		this.host = host;
		this.port = port;
	}

	public void start(Socket socket) {
		this.inSocket = socket;
		counter++;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				byte buffer[] = new byte[4096];
				int len;
				
				try {
					System.err.println("[+] Connected to " + host+":"+port);
					outSocket = new Socket(host, port);
					int round = 0;
					while(true) {
						len = inSocket.getInputStream().read(buffer, 0, 0x29);
						if(len == 0x29) {
							//System.err.println("[+] Received command head. Command: " +buffer[36]+", Len: "+commandLen(buffer));
							outSocket.getOutputStream().write(fuzz(buffer, len, round, true), 0, len);
							
							len = inSocket.getInputStream().read(buffer);
							//System.err.println("[+] Received command data. Command: " +buffer[36]+", Len: "+len);
							if(len == -1) {
								cleanup();
								break;
							}
							
							outSocket.getOutputStream().write(fuzz(buffer, len, round, false),0, len);
							
							len = outSocket.getInputStream().read(buffer, 0, 0x29);
							if(len == 0x29) {
								//System.err.println("[+] Received response head. Command: " +buffer[36]+", Len: "+commandLen(buffer));
								inSocket.getOutputStream().write(buffer, 0, len);
								len = outSocket.getInputStream().read(buffer);
								//System.err.println("[+] Received response data. Command: " +buffer[36]+", Len: "+len);
								if(len == -1) {
									cleanup();
									break;
								}
								inSocket.getOutputStream().write(buffer,0, len);
							} else {
								cleanup();
								break;
							}
							
						} else {
							cleanup();
							break;
						}
						round++;
					}
					
				} catch (UnknownHostException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			private void cleanup() throws IOException {
				inSocket.close();
				outSocket.close();
			}

			private int u2i(byte b) {
				return (int) b & 0xFF;
			}

			private int commandLen(byte[] buffer) {
				int commandLen = ((((u2i(buffer[40])*256)+u2i(buffer[39]))*256)+u2i(buffer[38]))*256+u2i(buffer[37]);
				return commandLen;
			}
		}).start();
	}

	protected byte[] fuzz(byte[] buffer, int len, int round, boolean b) {
		if(round > 2 && b == false) {
			int pos = 
				//counter / 256; 
				(int)(Math.random()*len);
			int val = 
				//counter % 256; 
				(int)(Math.random()*256);

			int pos2 = 
				//counter / 256; 
				(int)(Math.random()*len);
			int val2 = 
				//counter % 256; 
				(int)(Math.random()*256);

			buffer[pos]=(byte)(val & 0xFF);
			buffer[pos2]=(byte)(val2 & 0xFF);
			System.err.println(String.format("%d: R%d [%d]<-%d", counter, round, pos, val));
		}
		return buffer;
	}
}
