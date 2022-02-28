package hello.advanced.proxy.proxyfactory;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

import hello.advanced.proxy.common.advice.TimeAdvice;
import hello.advanced.proxy.common.service.ConcreteService;
import hello.advanced.proxy.common.service.ServiceImpl;
import hello.advanced.proxy.common.service.ServiceInterface;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProxyFactoryTest {

	@Test
	@DisplayName("인터페이스가 있으면 JDK 동적 프록시 사용")
	void interfaceProxy() {
		ServiceInterface target = new ServiceImpl();
		ProxyFactory proxyFactory = new ProxyFactory(target);
		proxyFactory.addAdvice(new TimeAdvice());
		ServiceInterface proxy = (ServiceInterface)proxyFactory.getProxy();
		log.info("targetClass={}", target.getClass());
		log.info("proxyClass={}", proxy.getClass());

		proxy.save();

		Assertions.assertThat(AopUtils.isAopProxy(proxy)).isTrue();
		Assertions.assertThat(AopUtils.isJdkDynamicProxy(proxy)).isTrue();
		Assertions.assertThat(AopUtils.isCglibProxy(proxy)).isFalse();
	}

	@Test
	@DisplayName("구체 클래스만 있으면 JDK 동적 프록시 사용")
	void concreteProxy() {
		ConcreteService target = new ConcreteService();
		ProxyFactory proxyFactory = new ProxyFactory(target);
		proxyFactory.addAdvice(new TimeAdvice());
		ConcreteService proxy = (ConcreteService)proxyFactory.getProxy();
		log.info("targetClass={}", target.getClass());
		log.info("proxyClass={}", proxy.getClass());

		proxy.call();

		Assertions.assertThat(AopUtils.isAopProxy(proxy)).isTrue();
		Assertions.assertThat(AopUtils.isJdkDynamicProxy(proxy)).isFalse();
		Assertions.assertThat(AopUtils.isCglibProxy(proxy)).isTrue();
	}

	@Test
	@DisplayName("ProxyTargetClass 옵션을 사용하면 인터페이스가 있어도 CGLIB를 사용하고, 클래스 기반 프록시 사용")
	void proxyTargetProxy() {
		ServiceInterface target = new ServiceImpl();
		ProxyFactory proxyFactory = new ProxyFactory(target);
		proxyFactory.setProxyTargetClass(true);
		proxyFactory.addAdvice(new TimeAdvice());
		ServiceInterface proxy = (ServiceInterface)proxyFactory.getProxy();
		log.info("targetClass={}", target.getClass());
		log.info("proxyClass={}", proxy.getClass());

		proxy.save();

		Assertions.assertThat(AopUtils.isAopProxy(proxy)).isTrue();
		Assertions.assertThat(AopUtils.isJdkDynamicProxy(proxy)).isFalse();
		Assertions.assertThat(AopUtils.isCglibProxy(proxy)).isTrue();
	}
}
