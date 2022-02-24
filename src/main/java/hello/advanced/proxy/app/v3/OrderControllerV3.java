package hello.advanced.proxy.app.v3;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class OrderControllerV3 {

	private final OrderServiceV3 orderService;

	public OrderControllerV3(OrderServiceV3 orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/proxy/v3/request")
	public String request(String itemId) {
		orderService.orderItem(itemId);
		return "ok";
	}

	@GetMapping("/proxy/v3/no-log")
	public String noLog() {
		return "ok";
	}
}
