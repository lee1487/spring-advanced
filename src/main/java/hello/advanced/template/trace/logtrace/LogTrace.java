package hello.advanced.template.trace.logtrace;

import hello.advanced.template.trace.TraceStatus;

public interface LogTrace {

	TraceStatus begin(String message);
	
	void end(TraceStatus status);
	
	void exception(TraceStatus status, Exception e); 
}
