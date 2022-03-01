package hello.advanced.proxy.config.v3_proxyfactory.advice;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import hello.advanced.template.trace.TraceStatus;
import hello.advanced.template.trace.logtrace.LogTrace;

public class LogTraceAdvice implements MethodInterceptor{

	private final LogTrace logTrace;

	public LogTraceAdvice(LogTrace logTrace) {
		this.logTrace = logTrace;
	}



	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		TraceStatus status = null;
		try {
			Method method = invocation.getMethod();
			String message = method.getDeclaringClass().getSimpleName() + "." +
					method.getName() + "()";
			status = logTrace.begin(message);

			// 로직 호출
//			Object result = method.invoke(target, args);
			Object result = invocation.proceed();
			logTrace.end(status);
			return result;
		} catch (Exception e) {
			logTrace.exception(status, e);
			throw e;
		}
	}

}
