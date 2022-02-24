package hello.advanced.template.app.v5;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import hello.advanced.template.trace.callback.TraceCallback;
import hello.advanced.template.trace.callback.TraceTemplate;
import hello.advanced.template.trace.logtrace.LogTrace;

@RestController
public class OrderControllerV5 {

	private final OrderServiceV5 orderService;
	private final TraceTemplate template;

	@Autowired
	public OrderControllerV5(OrderServiceV5 orderService, LogTrace trace) {
		this.orderService = orderService;
		this.template = new TraceTemplate(trace);
	}

	@GetMapping("/v5/request")
	public String request(String itemId) {
		return template.execute("OrderController.request()", new TraceCallback<String>() {
			@Override
			public String call() {
				orderService.orderItem(itemId);
				
				return "ok";
			}
		});
 		
	}
}
