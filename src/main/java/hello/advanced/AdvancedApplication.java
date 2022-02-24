package hello.advanced;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import hello.advanced.proxy.config.AppV1Config;

@Import(AppV1Config.class)
@SpringBootApplication(scanBasePackages = "hello.advanced.proxy.app")	//주의
public class AdvancedApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdvancedApplication.class, args);
	}

}
