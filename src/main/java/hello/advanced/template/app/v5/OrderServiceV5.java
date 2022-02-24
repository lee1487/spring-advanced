package hello.advanced.template.app.v5;

import org.springframework.stereotype.Service;

import hello.advanced.template.trace.callback.TraceTemplate;
import hello.advanced.template.trace.logtrace.LogTrace;

@Service
public class OrderServiceV5 {

	private final OrderRepositoryV5 orderRepository;
	private final TraceTemplate template;

	public OrderServiceV5(OrderRepositoryV5 orderRepository, LogTrace trace) {
		super();
		this.orderRepository = orderRepository;
		this.template = new TraceTemplate(trace);
	}

	public void orderItem(String itemId) {
		template.execute("OrderService.orderItem()", () -> {
			orderRepository.save(itemId);
			return null;
		});

	}
}
