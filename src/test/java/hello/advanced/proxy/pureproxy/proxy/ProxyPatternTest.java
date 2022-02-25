package hello.advanced.proxy.pureproxy.proxy;

import org.junit.jupiter.api.Test;

import hello.advanced.proxy.pureproxy.proxy.code.ProxyPatternClient;
import hello.advanced.proxy.pureproxy.proxy.code.RealSubject;

public class ProxyPatternTest {

	@Test
	void noProxyTest() {
		RealSubject realSubject = new RealSubject();
		
		ProxyPatternClient client = new ProxyPatternClient(realSubject);
		client.execute();
		client.execute();
		client.execute();
	}
}

