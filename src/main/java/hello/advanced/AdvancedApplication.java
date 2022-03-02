package hello.advanced;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import hello.advanced.proxy.config.v2_dynamicproxy.DynamicProxyFilterConfig;
import hello.advanced.proxy.config.v3_proxyfactory.ProxyFactoryConfigV1;
import hello.advanced.proxy.config.v3_proxyfactory.ProxyFactoryConfigV2;
import hello.advanced.proxy.config.v4_postprocessor.BeanPostProcessorConfig;
import hello.advanced.proxy.config.v5_autoproxy.AutoProxyConfig;
import hello.advanced.proxy.config.v6_aop.AopConfig;
import hello.advanced.template.trace.logtrace.LogTrace;
import hello.advanced.template.trace.logtrace.ThreadLocalLogTrace;

//@Import({AppV1Config.class, AppV2Config.class})
//@Import(InterfaceProxyConfig.class)
//@Import(ConcreteProxyConfig.class)
//@Import(DynamicProxyBasicConfig.class)
//@Import(DynamicProxyFilterConfig.class)
//@Import(ProxyFactoryConfigV1.class)
//@Import(ProxyFactoryConfigV2.class)
//@Import(BeanPostProcessorConfig.class)
//@Import(AutoProxyConfig.class)
@Import(AopConfig.class)
@SpringBootApplication(scanBasePackages = "hello.advanced.proxy.app")	//주의
public class AdvancedApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdvancedApplication.class, args);
	}

	@Bean
	public LogTrace logTrace() {
		return new ThreadLocalLogTrace();
	}

}
