package org.axt.db2.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class Proxy {

	private final Provider<Worker> workerProvider;
	
	@Inject @Named("localPort")
	private int localPort;

	@Inject
	private Proxy(Provider<Worker> workerProvider) {
		this.workerProvider = workerProvider;
	}
	
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(localPort);
			while(true) {
				Socket socket = serverSocket.accept();
				Worker worker = workerProvider.get();
				worker.start(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
