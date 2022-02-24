package hello.advanced.template;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hello.advanced.template.trace.logtrace.LogTrace;
import hello.advanced.template.trace.logtrace.ThreadLocalLogTrace;

@Configuration
public class LogTraceConfig {

	@Bean
	public LogTrace logTrace() {
		return new ThreadLocalLogTrace();
	}
}
