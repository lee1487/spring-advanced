package hello.advanced.template.trace.template;

import hello.advanced.template.trace.TraceStatus;
import hello.advanced.template.trace.logtrace.LogTrace;

public abstract class AbstractTemplate<T> {

	private final LogTrace trace;

	public AbstractTemplate(LogTrace trace) {
		this.trace = trace;
	}
	
	public T execute(String message) {
		TraceStatus status = null;
		try {
			status = trace.begin(message);
			
			T result = call();
			
			trace.end(status);
			return result;
			
		} catch (Exception e) {
			trace.exception(status, e);
			throw e;
		}
	}

	protected abstract T call();
	
	
}
