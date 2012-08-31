package org.axt.db2.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.name.Names;

public class Main {
	
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
			@Override
			protected void configure() {
				bindConstant().annotatedWith(Names.named("host")).to("192.168.0.129");
				bindConstant().annotatedWith(Names.named("port")).to(523);
				bindConstant().annotatedWith(Names.named("localPort")).to(1523);
				
				//bind(Worker.class).toProvider(WorkerProvider.class);
				//bind(Worker.class).annotatedWith(Names.named("alwaysNew"));
			}
		});
		
		injector.getInstance(Proxy.class).run();
	}
}
