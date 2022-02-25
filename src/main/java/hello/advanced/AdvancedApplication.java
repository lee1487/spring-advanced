package hello.advanced;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import hello.advanced.proxy.config.v1_proxy.InterfaceProxyConfig;
import hello.advanced.template.trace.logtrace.LogTrace;
import hello.advanced.template.trace.logtrace.ThreadLocalLogTrace;

//@Import({AppV1Config.class, AppV2Config.class})
@Import(InterfaceProxyConfig.class)
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
