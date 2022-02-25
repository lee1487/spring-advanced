package hello.advanced.proxy.pureproxy.decorator;

import org.junit.jupiter.api.Test;

import hello.advanced.proxy.pureproxy.decorator.code.Component;
import hello.advanced.proxy.pureproxy.decorator.code.DecoratorPatternClient;
import hello.advanced.proxy.pureproxy.decorator.code.RealComponent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DecoratorPatternTest {

	@Test
	void noDecorator() {
		Component realComponent = new RealComponent();
		DecoratorPatternClient client = new DecoratorPatternClient(realComponent);
		client.execute();
	}
}
