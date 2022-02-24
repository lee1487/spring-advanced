package hello.advanced.template.app.v4;

import org.springframework.stereotype.Repository;

import hello.advanced.template.trace.logtrace.LogTrace;
import hello.advanced.template.trace.template.AbstractTemplate;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryV4 {
	
	private final LogTrace trace;

	public void save(String itemId) {
		
		AbstractTemplate<Void> template = new AbstractTemplate<Void>(trace) {

			@Override
			protected Void call() {
				
				//저장 로직 
				if (itemId.contentEquals("ex")) {
					throw new IllegalStateException("예외 발생!");
				}
				sleep(1000);
				
				return null;
			}
		};
		template.execute("OrderRepository.save()");
		
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
