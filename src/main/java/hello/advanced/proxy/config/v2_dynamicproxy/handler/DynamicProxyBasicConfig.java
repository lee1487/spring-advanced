package hello.advanced.proxy.config.v2_dynamicproxy.handler;

import java.lang.reflect.Proxy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hello.advanced.proxy.app.v1.OrderControllerV1;
import hello.advanced.proxy.app.v1.OrderControllerV1Impl;
import hello.advanced.proxy.app.v1.OrderRepositoryV1;
import hello.advanced.proxy.app.v1.OrderRepositoryV1Impl;
import hello.advanced.proxy.app.v1.OrderServiceV1Impl;
import hello.advanced.proxy.app.v1.OrderServiceV1;
import hello.advanced.template.trace.logtrace.LogTrace;

@Configuration
public class DynamicProxyBasicConfig {

	@Bean
	public OrderControllerV1 orderControllerV1(LogTrace logTrace) {
		OrderControllerV1Impl orderController = new OrderControllerV1Impl(orderServiceV1(logTrace));

		OrderControllerV1 proxy = (OrderControllerV1) Proxy.newProxyInstance(
				OrderControllerV1.class.getClassLoader(),
				new Class[] {OrderControllerV1.class},
				new LogTraceBasicHandler(orderController, logTrace)
		);

		return proxy;
	}


	@Bean
	public OrderServiceV1 orderServiceV1(LogTrace logTrace) {
		OrderServiceV1Impl orderService = new OrderServiceV1Impl(orderRepositoryV1(logTrace));

		OrderServiceV1 proxy = (OrderServiceV1)	Proxy.newProxyInstance(
				OrderServiceV1.class.getClassLoader(),
				new Class[] {OrderServiceV1.class},
				new LogTraceBasicHandler(orderService, logTrace)
		);
		return proxy;
	}

	@Bean
	public OrderRepositoryV1 orderRepositoryV1(LogTrace logTrace) {
		OrderRepositoryV1 orderRepository = new OrderRepositoryV1Impl();

		OrderRepositoryV1 proxy = (OrderRepositoryV1)Proxy.newProxyInstance(
				OrderRepositoryV1.class.getClassLoader(),
				new Class[] { OrderRepositoryV1.class },
				new LogTraceBasicHandler(orderRepository, logTrace)
		);

		return proxy;
	}
}
