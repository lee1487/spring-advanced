package hello.advanced.proxy.app.v2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping
@ResponseBody
public class OrderControllerV2 {

	private final OrderServiceV2 orderService;

	public OrderControllerV2(OrderServiceV2 orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/proxy/v2/request")
	public String request(String itemId) {
		orderService.orderItem(itemId);
		return "ok";
	}

	@GetMapping("/proxy/v2/no-log")
	public String noLog() {
		return "ok";
	}
}
