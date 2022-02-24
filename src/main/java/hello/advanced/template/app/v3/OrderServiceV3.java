package hello.advanced.template.app.v3;

import org.springframework.stereotype.Service;

import hello.advanced.template.trace.TraceStatus;
import hello.advanced.template.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceV3 {

	private final OrderRepositoryV3 orderRepository;
	private final LogTrace trace;
	
	public void orderItem(String itemId) {
		
		TraceStatus status = null;
		try {
			status = trace.begin("OrderService.orderItem()");
			orderRepository.save(itemId);
			trace.end(status);
		} catch (Exception e) {
			trace.exception(status, e);
			throw e; 
		}
		
	}
}
