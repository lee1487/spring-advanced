package hello.advanced.template.app.v0;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryV0 {

	public void savn(String itemId) {
		//저장 로직 
		if (itemId.contentEquals("ex")) {
			throw new IllegalStateException("예외 발생!");
		}
		sleep(1000);
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
