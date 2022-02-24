# SPRING 기초 학습

## 예제 만들기 

### 예제 프로젝트 만들기 -V0
```
  학습을 위한 간단한 예제 프로젝트를 만들어보자 
  상품을 주문하는 프로세스로 가정하고, 일반적인 웹 애플리케이션에서 Controller->Service->Repository로 
  이어지는 흐름을 최대한 단순하게 만들어보자.
```

### 로그 추적기 V1 - 요구사항 분석 
```
  여러분이 새로운 회사에 입사했는데, 수 년간 운영중인 거대한 프로젝트에 투입되었다. 전체 소스 코드는
  수 십만 라인이고, 클래스 수도 수백개 이상이다. 여러분에게 처음 맡겨진 요구사항은 
  로그 추적기를 만드는 것이다. 애플리케이션이 커지면서 점점 모니터링과 운영이 중요해지는 단계이다. 
  특히 최근 자주 병목이 발생하고 있다. 어떤 부분에서 병목이 발생하는지, 
  그리고 어떤 부분에서 예외가 발생하는지를 로그를 통해서 확인하는 것이 점점 중요해지고 있다.
  기존에는 개발자가 문제가 발생한 다음에 관련 부분을 어렵게 찾아서 로그를 하나하나 직접 만들어서 남겼다.
  로그를 미리 남겨둔다면 이런 부분을 손쉽게 찾을 수 있을 것이다. 
  이 부분을 개선하고 자동화 하는 것이 여러분의 미션이다. 
  
  요구사항 
    - 모든 PUBLIC 메서드의 호출과 응답 정보를 로그로 출력 
	- 애플리케이션의 흐름을 변경하면 안됨
	  - 로그를 남긴다고 해서 비즈니스 로직의 동작에 영향을 주면 안됨
	- 메서드 호출에 걸린 시간 
	- 정상 흐름과 예외 흐름 구분 
	  - 예외 발생시 예외 정보가 남아야 함
	- 메서드 호출의 깊이 표현 
	- HTTP 요청을 구분 
	  - HTTP 요청 단위로 특정 ID를 남겨서 어떤 HTTP 요청에서 시작된 것인지 
	    명확하게 구분이 가능해야 함
	  - 트랜잭션 ID(DB 트랜잭션x), 여기서는 하나의 HTTP 요청이 시작해서 
	    끝날 때 까지를 하나의 트랜잭션이라 함
	  
  참고: 모니터링 툴을 도입하면 많은 부분이 해결되지만, 지금은 학습이 목적이라는 사실을 기억하자.
```

### 로그 추적기 V1 - 프로토타입 개발 
```
  애플리케이션의 모든 로직에 직접 로그를 남겨도 되지만, 그것보다는 더 효율적인 개발 방법이 필요하다.
  특히 트랜잭션ID와 깊이를 표현하는 방법은 기존 정보를 이어 받아야 하기 때문에 단순히 로그만 
  남긴다고 해결할 수 있는 것은 아니다. 
  
  요구사항에 맞추어 애플리케이션에 효과적으로 로그를 남기기 위한 로그 추적기를 개발해보자.
  먼저 프로토타입 버전을 개발해 보자. 아마 코드를 모두 작성하고 테스트 코드까지 살행해보아야
  어떤 것을 하는지 감이 올 것이다.
  
  먼저 로그 추적기를 위한 기반 데이터를 가지고 있는 TraceId, TraceStatus 클래스를 
  만들어 보자.
  
  TraceId 클래스 
    로그 추적기는 트랜잭션ID와 깊이를 표현하는 방법이 필요하다 
	여기서는 트랜잭션ID와 깊이를 표현하는 level을 묶어서 TraceId라는 개념을 만들었다.
	TraceId는 단순히 id(트랜잭션ID)와 level 정보를 함께 가지고 있다.

    UUID 
      TraceId를 처음 생성하면 createId()를 사용해서 UUID를 만들어낸다. 
	  UUID가 너무 길어서 여기서는 앞 8자리만 사용한다. 
	  이 정도면 로그를 충분히 구분할 수 있다.
	  여기서는 이렇게 만들어진 값을 트랜잭션ID로 사용한다.
	
    createNextId() 
      다음 TraceId를 만든다. 예제 로그를 잘 보면 깊이가 증가해도 트랜잭션ID는 같다. 
	  대신에 깊이가 하나 증가한다. 따라서 createNextId()를 사용해서 
	  현재 TraceId를 기반으로 다음 TraceId를 만들면 id는 기존과 같고,
	  level은 하나 증가한다. 
	
    createPreviousId() 
      createNextId()의 반대 역할을 한다 
	  id는 기존과 같고, level은 하나 감소한다.
	
    isFirstLevel() 
      첫 번째 레벨 여부를 편리하게 확인할 수 있는 메서드 
	  
  TraceStatus 클래스 
    로그의 상태 정보를 나타낸다.
	로그를 시작하면 끝이 있어야 한다. 
	TraceStaus는 로그를 시작할 때의 상태 정보를 가지고 있다. 이 상태 정보는 
	로그를 종료할 때 사용된다. 
	  - traceId: 내부에 트랜잭션ID와 level을 가지고 있다. 
	  - startTimeMs: 로그 시작시간이다. 로그 종료시 이 시작 시간을 기준으로 
					 시작~종료까지 전체 수행 시간을 구할 수 있다. 
	  - message: 시작시 사용한 메세지이다. 이후 로그 종료시에도 이 메세지를 
				 사용해서 출력한다.
	
	TraceId, TraceStatus를 사용해서 실제 로그를 생성하고, 처리하는 기능을
	개발해보자.
	
  HelloTraceV1
    HelloTraceV1을 사용해서 실제 로그를 시작하고 종료할 수 있다. 그리고 로그를 
	출력하고 실행시간도 측정할 수 있다. 
	@Component: 싱글톤으로 사용하기 위해 스프링 빈으로 등록한다. 컴포넌트 스캔의 
				대상이 된다.
	
	공개 메서드 
	  로그 추적기에 사용되는 공개 메서드는 다음 3가지이다. 
	    - begin(..)
		- end(..)
		- exception(..)
	
	  하나씩 자세히 알아보자 
	    - TraceStatus begin(String message)
		  - 로그를 시작한다.
		  - 로그 메세지를 파라미터로 받아서 시작 로그를 출력한다.
		  - 응답 결과로 현재 로그의 상태인 TraceStatus를 반환한다.
		
		- void end(TraceStatus status)
		  - 로그를 정상 종료한다.
		  - 파라미터로 시작 로그의 상태(TraceStatus)를 전달 받는다. 
		    이 값을 활용해서 실행 시간을 계산하고, 종료시에도 시작할 때와 
			동일한 로그 메세지를 출력할 수 있다.
		  - 정상 흐름에서 호출한다. 
		  
		- void exception(TraceStatus status, Exception e)
		  - 로그를 예외 상황으로 종료한다.
		  - TraceStatus, Exception 정보를 함께 전달 받아서 실행시간,
		    예외 정보를 포함한 겨롸 로그를 출력한다.
		  - 예외가 발생했을 때 호출한다.
		  
	비공개 메서드 
	  - complete(TraceStatus status, Exception e) 
	    - end(), exception()의 요청 흐름을 한 곳에서 편리하게 처리한다.
		  실행 시간을 측정하고 로그를 남긴다. 
	  - String addSpace(String prefix, int level): 다음과 같은 결과를 출력한다 
	    - prefix: -->
		  level0:
		  level1:|-->
		  level2:|  |-->
		  
		- prefix: <--
		  level0:
		  level1:|<--
		  level2:|  |<--
		  
		- prefix: <X-
		  level0:
		  level1:|<X-
		  level2:|  |<X-
	  참고로 HelloTraceV1는 아직 모든 요구사항을 만족하지는 못한다. 이후에 기능을 
	  하나씩 추가할 예정이다.
  
  테스트 작성 
    HelloTraceV1Test
	  주의! 테스트 코드는 test/java/ 하위에 위치함
	
	  테스트 코드를 보면 로그 추적기를 어떻게 실행해야 하는지, 그리고 어떻게 동작하는지 
	  이해가 될 것이다. 
	  
	  이제 실제 애플리케이션에 적용해보자 
	
	참고: 이것은 온전한 테스트 코드가 아니다. 일반적으로 테스트라고 하면 자동으로 검증하는 
		과정이 필요하다. 이 테스트는 검증하는 과정이 없고 결과를 콘솔로 직접 확인해야 한다.
		이렇게 응답값이 없는 경우를 자동으로 검증하려면 여러가지 테스트 기법이 필요하다. 
		이번 강의에서는 예제를 최대한 단순화 하기 위해 검증 테스트를 생략했다.
		
	주의: 지금까지 만든 로그 추적기가 어떻게 동작하는지 확실히 이해해야 
		다음 단계로 넘어갈 수 있다. 복습과 코드를 직접 만들어보면서 확실하게 
		본인 것으로 만들고 다음으로 넘어가자.
```

### 로그 추적기 V1 - 적용
```
  이제 애플리케이션에 우리가 개발한 로그 추적기를 적용해보자.
  기존 v0 패키지에 코드를 직접 작성해도 되지만, 기존 코드를 유지하고, 비교하기 위해서 
  v1 패키지를 새로 만들고 기존 코드를 복사하자. 복사하는 과정은 다음을 참고하자 
  
  vo -> v1 복사 
    - hello.advanced.app.v1 패키지 생성 
	- 복사 
	  - v0.OrderRepositoryV0 -> v1.OrderRepositoryV1
	  - v0.OrderServiceV0 -> v1.OrderServiceV1
	  - v0.OrderControllerV0 -> v1.OrderControllerV1
	- 코드 내부 의존관계 클래스를 V1으로 변경 
	  - OrderControllerV1: OrderServiceV0 -> OrderServiceV1
	  - OrderServiceV1: OrderRepositoryV0 -> OrderRepositoryV1
	- OrderControllerV1 매핑 정보 변경
	  - @GetMapping("/v1/request")
	  
	실행해서 정상 동작하는지 확인하자.
	  - 실행: http://localhost:8080/v1/request?itemId=hello
	  - 결과: ok
	  

  v1 적용하기 
    OrderControllerV1, OrderServiceV1, OrderRepositoryV1에 
	로그 추적기를 적용해보자.
	먼저 컨트롤러에 우리가 개발한 HelloTraceV1을 적용해 보자
	  - HelloTraceV1 trace: HelloTraceV1을 주입 받는다. 참고로 
	    HelloTraceV1은 @Component 애노테이션을 가지고 있기 때문에 
		컴포넌트 스캔의 대상이 된다. 따라서 자동으로 스프링 빈으로 등록된다. 
		
	  - trace.begin("OrderController.request()): 로그를
	    시작할 때 메시지 이름으로 컨트롤러 이름 + 메서드 이름을 주었다. 
		이렇게 하면 어떤 컨트롤러와 메서드가 호출되었는지 로그로 편리하게 
		확인할 수 있다. 물론 수작업이다.
	  - 단순하게 trace.begin(), trace.end() 코드 두 줄만 적용하면 
	    될 줄 알았지만, 실상은 그렇지 않다. trace.exception()으로 
		예외까지 처리해야 하므로 지저분한 try, catch 코드가 추가된다. 
	  - begin()의 결과 값으로 받은 TraceStatus status 값을 
	    end(), exception()에 넘겨야 한다. 결국 try, catch 블록 
		모두에 이 값을 넘겨야 한다. 따라서 try 상위에 TraceStatus status
		코드를 선언해야 한다. 만약 try 안에서 TraceStatus status를 
		선언하면 try 블록안에서만 해당 변수가 유효하기 때문에 catch 블록에
		넘길 수 없다. 따라서 컴파일 오류가 발생한다.
	  - throw e: 예외를 꼭 다시 던져주어야 한다. 그렇지 않으면 여기서 예외를 
	    먹어버리고, 이후에 정상 흐름으로 동작한다. 로그는 애플리케이션의 흐름에 
		영향을 주면 안된다. 로그 때문에 예외가 사라지면 안된다.
		
	실행 
	  - 정상: http://localhost:8080/v1/request?itemId=hello
	  - 예외: http://localhost:8080/v1/request?itemId=ex
	  실행해보면 정상 흐름과 예외 모두 로그로 잘 출력되는 것을 확인할 수 있다. 
	  나머지 부분도 완성하자.
	  
	OrderRepositoryV1
	  참고: 아직 level 관련 기능을 개발하지 않았다. 따라서 level 값은 항상 0이다.
	      그리고 트랜잭션ID 값도 다르다. 이부분은 아직 개발하지 않았다.
	
	HelloTraceV1 덕분에 직접 로그를 하나하나 남기는 것 보다는 편하게 여러가지 
	로그를 남길 수 있었다. 하지만 로그를 남기기 위한 코드가 생각보다 너무 복잡하다.
	지금은 우선 요구사항과 동작하는 것에만 집중하자. 
	
	남은 문제 
	  요구 사항 
	    - 메서드 호출의 깊이 표현 
		- HTTP 요청을 구분
		  - HTTP 요청 단위로 특정 ID를 남겨서 어떤 HTTP 요청에서 시작된 것인지 
		    명확하게 구분이 가능해야 함
		  - 트랜잭선 ID(DB 트랜잭션X)
	
	아직 구현하지 못한 요구사항은 메서드 호출의 깊이를 표현하고, 같은 HTTP 요청이면 
	같은 트랜잭션 ID를 남기는 것이다. 이 기능은 직전 로그의 깊이와 트랜잭션 ID가 
	무엇인지 알아야 할 수 있는 일이다. 예를 들어서 OrderController.request()
	에서 로그를 남길 때 어떤 깊이와 어떤 트랜잭션 ID를 사용했는지를 그 다음에 
	로그를 남기는 OrderService.orderItem()에서 로그를 남길 때 알아야 한다.
	결국 현재 로그의 상태 정보인 트랜잭션ID와 level이 다음으로 전달되어야 한다.
	정리하면 로그에 대한 문맥(Context) 정보가 필요하다.
```

### 로그 추적기 V2 - 파라미터로 동기화 개발 
```
  트랜잭션 ID와 메서드 호출의 깊이를 표현하는 가장 단순한 방법은 첫 로그에서 사용한 트랜잭션ID와
  level을 다음 로그에 넘겨주면 된다. 현재 로그의 상태 정보인 트랜잭션ID와 level은 
  TraceId에 포함되어 있다. 따라서 TraceId를 다음 로그에 넘겨주면 된다. 
  이 기능을 구현한 HelloTraceV2를 개발해 보자.
  
    - HelloTraceV2는 기존 코드인 HelloTraceV1와 같고 beginSync(..)가 
	  추가되었다.
	
	beginSync(..)
	  - 기존 TraceId에서 createNextId()를 통해 다음 ID를 구한다.
	  - createNextId의 TraceId 생성 로직은 다음과 같다.
	    - 트랜잭션ID는 기존과 같이 유지한다.
		- 깊이를 표현하는 Level은 하나 증가한다 (0 -> 1)
	
	테스트 코드를 통해 잘 동작하는지 확인해보자.
	  처음에는 begin(..)을 사용하고, 이후에는 beginSync(..)를 사용하면 된다.
	  beginSync(..)를 호출할 때 직전 로그의 traceId정보를 넘겨주어야 한다.
	  실행 로그를 보면 같은 트랜잭션ID를 유지하고 level을 통해 메서드 호출의 
	  깊이를 표현하는 것을 확인할 수 있다.
```

### 로그 추적기 V2 - 적용 
```
  이제 로그 추적기를 애플리케이션에 적용해보자
  
  v1 -> v2 복사 
    로그 추적기 V2를 적용하기 전에 먼저 기존 코드를 복사하자.
	  - hello.advanced.app.v2 패키지 생성 
	  - 복사 
	    - v1.OrderRepositoryV1 -> v2.OrderRepositoryV2
	    - v1.OrderServiceV1 -> v2.OrderServiceV2
	    - v1.OrderControllerV1 -> v2.OrderControllerV2
	  - 코드 내부 의존관계 클래스를 V2으로 변경 
	    - OrderControllerV2: OrderServiceV1 -> OrderServiceV2
	    - OrderServiceV2: OrderRepositoryV1 -> OrderRepositoryV2
	  - OrderControllerV2 매핑 정보 변경
	    - @GetMapping("/v2/request")
	  - app.v2에서는 HelloTraceV1 -> HelloTraceV2를 사용하도록 변경
	    - OrderControllerV2
		- OrderServiceV2
		- OrderRepositoryV2
	실행해서 정상 동작하는지 확인하자.
	  - 실행 : http://localhost:8080/v2/request?itemId=hello
	  - 결과 : v1의 코드를 그대로 복사했기 때문에 v1과 같은 로그가 출력되면 성공이다.
	  
  V2 적용하기
    메서드 호출의 깊이를 표현하고, HTTP 요청도 구분해보자.
	이렇게 하려면 처음 로그를 남기는 OrderController.request()에서 로그를 남길 때
	어떤 깊이와 어떤 트랜잭션 ID를 사용했는지 다음 차례인 OrderService.orderItem()에서
	로그를 남기는 시점에 알아야한다.
	결국 현재 로그의 상태 정보인 트랜잭션ID와 level이 다음으로 전달되어야 한다. 
	이 정보는 TraceStatus.traceId에 담겨있다. 따라서 traceId를 컨트롤러에서 
	서비스를 호출할 때 넘겨주면 된다.
	
	traceId를 넘기도록 V2 전체 코드를 수정하자. 
	
	OrderControllerV2
	  - TraceStatus status = trace.begin()에서 반환 받은 TraceStatus에는 
	    트랜잭션ID와 level 정보가 있는 TraceId가 있다. 
	  - orderService.orderItem()을 호출할 때 TraceId를 파라미터로 전달한다.
	  - TraceId를 파라미터로 전달하기 위해 OrderServiceV2.orderItem()의 
	    파라미터에 TraceId를 추가해야 한다.
		
	OrderServiceV2
	  - orderItem()은 파라미터로 전달 받은 traceId를 사용해서 
	    trace.beginSync()를 실행한다.
	  - beginSync()는 내부에서 다음 traceId를 생성하면서 트랜잭션 ID는 
	    유지하고 level은 하나 증가시킨다.
	  - beginSync()가 반환한 새로운 TraceStatus를 
	    orderRepository.save()를 호출하면서 파라미터로 전달한다.
	  - TraceId를 파라미터로 전달하기 위해 orderRepository.save()의
	    파라미터에 TraceId를 추가해야 한다.
		
	OrderRepositoryV2
	  - save()는 파라미터로 전달받은 traceId를 사용해서 
	    trace.beginSync()를 실행한다.
	  - beginSync()는 내부에서 다음 traceId를 생성하면서 트랜잭션 ID는 
	    유지하고 level은 하나 증가시킨다.
	  - beginSync()는 이렇게 갱신된 traceId로 새로운 TraceStatus를
	    반환한다.
	  - trace.end(status)를 호출하면서 반환된 TraceStatus를 전달한다.
	  
	실행 로그를 보면 같은 HTTP 요청에 대해서 트랜잭션ID가 유지되고, level도 
	잘 표현되는 것을 확인할 수 있다.
```

### 정리 
```
  드디어 모든 요구사항을 만족했다.
	
    남은 문제 
	  - HTTP 요청을 구분하고 깊이를 표현하기 위해서 TraceId 동기화가 필요하다.
	  - TraceId의 동기화를 위해서 관련 메서드의 모든 파라미터를 수정해야 한다.
	    - 만약 인터페이스가 있다면 인터페이스까지 모두 고쳐야 하는 상황이다.
	  - 로그를 처음 시작할 때는 begin()을 호출하고, 처음이 아닐때는 
	    beginSync()를 호출해야 한다. 
	    - 만약에 컨트롤러를 통해서 서비스를 호출하는 것이 아니라, 다른 곳에서 
		  서비스를 처음으로 호출하는 상황이라면 파라미터로 넘길 TraceId가 없다.
		  
	HTTP 요청을 구분하고 깊이를 표현하기 위해서 TraceId를 
	파라미터로 넘기는 것 말고 다른 대안은 없을까?
```

## 쓰레드 로컬 - ThreadLocal

### 필드 동기화 - 개발 
```
  앞서 로그 추적기를 만들면서 다음 로그를 출력할 때 트랜잭션ID와 level을 동기화 하는 
  문제가 있었다. 이 문제를 해결하기 위해 TraceId를 파라미터로 넘기도록 구현했다. 
  이렇게 해서 동기화는 성공했지만, 로그를 출력하는 모든 메서드에 TraceId 파라미터를 
  추가해야 하는 문제가 발생했다. TraceId를 파라미터로 넘기지 않고 이문제를 
  해결할 수 있는 방법은 없을까?
  
  이런 문제를 해결할 목적으로 새로운 로그 추적기를 만들어보자. 
  이제 프로토타입 버전이 아닌 정식 버전으로 제대로 개발해보자. 
  향후 다양한 구현체로 변경할 수 있도록 LogTrace 인터페이스를 먼저 만들고, 구현해보자.
  
  LogTrace 인터페이스 
    - LogTrace 인터페이스에는 로그 추적기를 위한 최소한의 기능인 begin(), end(),
	  exception()를 정의했다. 
	- 이제 파라미터를 넘기지 않고 TraceId를 동기화 할 수 있는 FieldLogTrace
	  구현체를 만들어보자.
  
  FieldLogTrace
    - FieldLogTrace는 기존에 만들었던 HelloTraceV2와 거의 같은 기능을 한다. 
	- TraceId를 동기화 하는 부분만 파라미터를 사용하는 것에서
	  TraceId traceIdHolder 필드를 사용하도록 변경되었다. 
	- 이제 직전 로그의 TraceId는 파라미터로 전달되는 것이 아니라 FieldLogTrace의
	  필드인 traceIdHolder에 저장된다. 
	  
	- 여기서 중요한 부분은 로그를 시작할 때 호출하는 syncTraceId()와
	  로그를 종료할 때 호출하는 releaseTraceId()이다. 
	- syncTraceId()
	  - TraceId를 새로 만들거나 앞선 로그의 TraceId를 참고해서 동기화하고, 
	    level도 증가한다. 
	  - 최초 호출이면 TraceId를 새로 만든다. 
	  - 직전 로그가 있으면 해당 로그의 TraceId를 참고해서 동기화하고,
	    level도 하나 증가한다. 
	  - 결과를 traceIdHolder에 보관한다 
	- releaseTraceId()
	  - 메서드를 추가로 호출할 때는 level이 하나 증가해야 하지만, 
	    메서드 호출이 끝나면 level이 하나 감소해야 한다. 
	  - releaseTraceId()는 level을 하나 감소한다.
	  - 만약 최초 호출(level=0)이면 내부에서 관리하는 traceId를 제거한다.
	  
  테스트 코드를 통해서 실행해 보자 
    실행 결과를 보면 트랜잭션ID도 동일하게 나오고, level을 통한 깊이도 잘 표현된다.
	FieldLogTrace.traceIdHolder 필드를 사용해서 TraceId가 잘 동기화 
	되는 것을 확인할 수 있다. 이제 불필요하게 TraceId를 파라미터로 전달하지 않아도 
	되고, 애플리케이션의 메서드 파라미터도 변경하지 않아도 된다.
```

### 필드 동기화 - 적용
```
  지금까지 만든 FieldLogTrace를 애플리케이션에 적용해 보자.
  
  LogTrace 스프링 빈 등록 
    - FieldLogTrace를 수동으로 스프링 빈으로 등록하자. 수동으로 등록하면 향후 
	  구현체를 편리하게 변경할 수 있다는 장점이 있다. 
  
    LogTraceConfig
	
  v2 -> v3 복사 
    로그 추적기 V3를 적용하기 전에 먼저 기존 코드를 복사하자.
	  - hello.advanced.app.v3 패키지 생성 
	  - 복사 
	    - v2.OrderControllerV2 -> v3.OrderControllerV3
		- v2.OrderServiceV2 -> v3.OrderServiceV3
		- v2.OrderRepositoryV2 -> v3.OrderRepositoryV3
	  - 코드 내부 의존관계 클래스를 V3으로 변경
	    - OrderControllerV3: OrderServiceV2 -> OrderServiceV3
		- OrderServiceV3: OrderRepositoryV2 -> OrderRepositoryV3
	  - OrderControllerV3 매핑 정보 변경 
	    - @GetMapping("/v3/request")
	  - HelloTraceV2 -> LogTrace 인터페이스 사용 -> 주의!
	  - TraceId traceId 파라미터를 모두 제거 
	  - beginSync() -> begin() 으로 사용하도록 변경 
	  
	traceIdHolder 필드를 사용한 덕분에 파라미터 추가 없는 깔끔한 로그 추적기를 완성했다.
	이제 실제 서비스에 배포한다고 가정해보자.
``` 

### 필드 동기화 - 동시성 문제 
```
  잘 만든 로그 추적기를 실제 서비스에 배포했다 가정해보자.
  테스트 할 때는 문제가 없는 것 처럼 보인다. 사실 직전에 만든 FieldLogTrace는 심각한 
  동시성 문제를 가지고 있다. 동시성 문제를 확인하려면 동시에 여러번 호출해보면 된다. 
  
  동시성 문제 확인 
    - 다음 로직을 1초안에 2번 실행해보자
	  - http://localhost:8080/v3/request?itemId=hello
	  - http://localhost:8080/v3/request?itemId=hello
	  
  기대한 것과 전혀 다른 문제가 발생한다. 트랜잭션ID도 동일하고 level도 뭔가
  많이 꼬인 것 같다. 분명히 테스트 코드로 작성할 때는 문제가 없었는데,
  무엇이 문제일까?
  
  동시성 문제 
    - 사실 이 문제는 동시성 문제이다.
	  FieldLogTrace는 싱글톤으로 등록된 스프링 빈이다. 이 객체의 인스턴스가 
	  애플리케이션에 딱 1개 존재한다는 뜻이다. 이렇게 하나만 있는 인스턴스의 
	  FieldLogTrace.traceIdHolder 필드를 여러 쓰레드가 동시에 
	  접근하기 때문에 문제가 발생한다. 
	  실무에서 한번 나타나면 개발자를 가장 괴롭히는 문제도 바로 이러한 
	  동시성 문제이다.
```

### 동시성 문제 - 예제 코드 
```
  동시성 문제가 어떻게 발생하는지 단순화해서 알아보자 
  
  테스트에도 lombok을 사용하기 위해 다음 코드를 추가하자.
  build.gradle
  
  dependencies {
	testCompileOnly 'org.projectlombok:lombok'
	testAnnotationProcessor 'org.projectlombok:lombok'
  }
  
  이렇게 해야 테스트 코드에서 @Slfj4 같은 애노테이션이 작동한다. 
  FieldService 
    - 주의: 테스트 코드(src/test)에 위치한다.
	- 매우 단순한 로직이다. 파라미터로 넘어온 name을 필드인 nameStore에 
	  저장한다. 그리고 1초간 쉰 다음 필드에 저장된 nameStore를 반환한다.
	  
  FieldServiceTest
    순서대로 실행 
	  - sleep(2000)을 설정해서 thread-A의 실행이 끝나고 나서 
	    thread-B가 실행되도록 해보자. 참고로 FieldService.logic()
		메서드는 내부에 sleep(1000)으로 1초의 지연이 있다. 따라서 
		1초 이후에 호출하면 순서대로 실행할 수 있다. 여기서는 넉넉하게 
		2초 (2000ms)를 설정했다. 
	
	  실행 결과를 보면 문제가 없다.
	    - Thread-A는 userA를 nameStore에 저장했다. 
	    - Thread-A는 userA를 nameStore에서 조회했다.
	    - Thread-B는 userB를 nameStore에 저장했다. 
	    - Thread-B는 userB를 nameStore에서 조회했다.
	  
	동시성 문제 발생 코드 
	  이번에는 sleep(100)을 설정해서 thread-A의 작업이 끝나기 전에 
	  thread-B가 실행되도록 해보자. 참고로 FieldService.logic()
	  메서드 내부에 sleep(1000)으로 1초의 지연이 있다. 따라서 1초 이후에 
	  호출하면 순서대로 실행할 수 있다. 다음에 설정할 100(ms)는 0.1초이기
	  때문에 thread-A의 작업이 끝나기 전에 Thread-B가 실행된다.
	  
	  실행결과를 보자. 
	    - 먼저 thread-A가 userA 값을 nameStore에 보관한다.
		- 0.1초 이후에 thread-B가 userB의 값을 nameStore에
		  보관한다. 기존에 nameStore에 보관되어 있던 userA 값은 
		  제거되고 userB 값이 저장된다. 
		- thread-A의 호출이 끝나면서 nameStore의 결과를 반환받는데, 
		  이때 nameStore는 앞의 2번에서 userB의 값으로 대체되었다.
		  따라서 기대했던 userA의 값이 아니라 userB의 값이 반환된다.
		- thread-B의 호출이 끝나면서 nameStore의 결과인 userB를
		  반환받는다.
		  
	  정리하면 다음과 같다 
	    1. Thread-A는 userA를 nameStore에 저장했다.
		2. Thread-B는 userB를 nameStore에 저장했다.
		3. Thread-A는 userB를 nameStore에서 조회했다.
		4. Thread-B는 userB를 nameStore에서 조회했다. 
		
  동시성 문제 
    결과적으로 Thread-A 입장에서는 저장한 데이터와 조회한 데이터가 다른 문제가 발생한다.
	이처럼 여러 쓰레드가 동시에 같은 인스턴스의 필드 값을 변경하면서 발생하는 문제를 
	동시성 문제라 한다. 이런 동시성 문제는 여러 쓰레드가 같은 인스턴스의 필드에 
	접근해야 하기 때문에 트래픽이 적은 상황에서는 확률상 잘 나타나지 않고, 트래픽이 
	점점 많아질수록 자주 발생한다. 특히 스프링 빈 처럼 싱글톤 객체의 필드를 
	변경하며 사용할 때 이러한 동시성 문제를 조심해야 한다.
	
  참고 
    - 이런 동시성 문제는 지역 변수에서는 발생하지 않는다. 지역 변수는 쓰레드마다 각각 다른 
	  메모리 영역이 할당된다. 
	- 동시성 문제가 발생하는 곳은 같은 인스턴스의 필드(주로 싱글톤에서 자주 발생), 또는 
	  static 같은 공용 필드에 접근할 때 발생한다.
	- 동시성 문제는 값을 읽기만 하면 발생하지 않는다. 어디선가 값을 변경하기 때문에 발생한다.
	
  그렇다면 지금처럼 싱글톤 객체의 필드를 사용하면서 동시성 문제를 해결하려면 어떻게 해야할까?
  다시 파라미터를 전달하는 방식으로 돌아가야 할까? 이럴 때 사용하는 것이 바로 쓰레드 로컬이다.
```

### ThreadLocal - 소개 
```
  쓰레드 로컬은 해당 쓰레드만 접근할 수 있는 특별한 저장소를 말한다. 쉽게 이야기해서 물건 
  보관 창구를 떠올리면 된다. 여러 사람이 같은 물건 보관 창구를 사용하더라도 창구 직원은 
  사용자를 인식해서 사용자별로 확실하게 물건을 구분해 준다. 
  사용자A, 사용자B 모두 창구 직원을 통해서 물건을 보관하고, 꺼내지만 창구 직원이
  사용자에 따라 보관한 물건을 구분해 주는 것이다.
  
  일반적인 변수 필드 
    - 여러 쓰레드가 같은 인스턴스의 필드에 접근하면 처음 쓰레드가 보관한 데이터가 
	  사라질 수 있다.
	  
  쓰레드 로컬 
    - 쓰레드 로컬을 사용하면 각 쓰레드마다 별도의 내부 저장소를 제공한다. 따라서 
	  같은 인스턴스의 쓰레드 로컬 필드에 접근해도 문제 없다.
    - 자바는 언어 차원에서 쓰레드 로컬을 지원하기 위한 java.lang.ThreadLocal
	  클래스를 제공한다.
```

### ThreadLocal - 예제 코드 
```
  예제 코드를 통해서 ThreadLocal을 학습해보자 
  
  ThreadLocalService 
    주의: 테스트 코드(src/test)에 위치한다.
	기존에 있던 FieldService와 거의 같은 코드인데, nameStore 필드가 일반 
	String 타입에서 ThreadLocal을 사용하도록 변경되었다. 
	
	ThreadLocal 사용법 
	  - 값 저장: ThreadLocal.set(xxx)
	  - 값 조회: ThreadLocal.get()
	  - 값 제거: ThreadLocal.remove()
	  
	  주의  
	    해당 쓰레드가 쓰레드 로컬을 모두 사용하고 나면 ThreadLocal.remove()를 
		호출해서 쓰레드 로컬에 저장된 값을 제거해주어야 한다. 제거하는 구체적인 
		예제는 조금 뒤에 설명하겠다. 
  
  ThreadLocalServiceTest
    - 쓰레드 로컬 덕분에 쓰레드 마다 각각 별도의 데이터 저장소를 가지게 되었다.
	  결과적으로 동시성 문제도 해결되었다.
``` 

### 쓰레드 로컬 동기화 - 개발 
```
  - FieldLogTrace에서 발생했던 동시성 문제를 ThreadLocal로 해결해보자
  - TraceId traceIdHolder 필드를 쓰레드 로컬을 사용하도록 
    ThreadLocal<TraceId> traceIdHolder로 변경하면 된다. 

  - 필드 대신에 쓰레드 로컬을 사용해서 데이터를 동기화하는 
    ThreadLocalLogTrace를 새로 만들자 
	
  ThreadLocalLogTrace
    - traceIdHolder가 필드에서 ThreadLocal로 변경되었다. 따라서 값을 
	  저장할 때는 set(..)을 사용하고 값을 조회할 때는 get()을 사용한다.
	  
    ThreadLocal.remove()
	  - 추가로 쓰레드 로컬을 모두 사용하고 나면 꼭 ThreadLocal.remove()를 
	    호출해서 쓰레드 로컬에 저장된 값을 제거해주어야 한다. 
	  - 쉽게 이야기해서 다음의 마지막 로그를 출력하고 나면 쓰레드 로컬의 값을 
	    제거해야 한다.
	
		[3f902f0b] hello1
		[3f902f0b] |-->hello2
		[3f902f0b] |<--hello2 time=2ms
		[3f902f0b] hello1 time=6ms //end() -> releaseTraceId() -> level==0, 
		ThreadLocal.remove() 호출
		
	  - 여기서는 releaseTraceId()를 통해 level이 점점 낮아져서 2->1->0
	    이 되면 로그를 처음 호출한 부분으로 돌아온 것이다. 따라서 이 경우 
		연관된 로그 출력이 끝난 것이다. 이제 더 이상 TraceId값을 
		추적하지 않아도 된다. 그래서 traceId.isFirstLevel()(level==0)인
		경우 ThreadLocal.remove()를 호출해서 쓰레드 로컬에 저장된
		값을 제거해준다.
		
	
  코드에 문제가 없는지 간단한 테스트틀 만들어서 확인해보자 
  ThreadLocalLogTraceTest
    - 멀티쓰레드 상황에서 문제가 없는지 애플리케이션에 ThreadLocalLogTrace를 
	  적용해서 확인해보자.
```

### 쓰레드 로컬 동기화 - 적용 
```
  LogTraceConfig 수정 
    - 동시성 문제가 있는 FieldLogTrace 대신에 문제를 해결한 
	  ThreadLocalLogTrace를 스프링 빈으로 등록하자 
	  
  동시 요청 
    동시성 문제 확인 
	  다음 로직을 1초안에 2번 실행해보자.
	    - http://localhost:8080/v3/request?itemId=hello
		- http://localhost:8080/v3/request?itemId=hello
	
	  로그를 직접 분리해서 확인해보면 각각의 쓰레드 별로 로그가 정확하게 
	  나누어 진 것을 확인할 수 있다. 
``` 

### 쓰레드 로컬 - 주의사항 
```
  쓰레드 로컬의 값을 사용 후 제거하지 않고 그냥 두면 WAS(톰캣)처럼 쓰레드 풀을 사용하는
  경우에 심각한 문제가 발생할 수 있다. 
  다음 예시를 통해서 알아보자 
  
  사용자A 저장 요청 
    1. 사용자A가 저장 HTTP를 요청했다.
	2. WAS는 쓰레드 풀에서 쓰레드를 하나 조회한다.
	3. 쓰레드 thread-A가 할당되었다.
	4. thread-A는 사용자A의 데이터를 쓰레드 로컬에 저장한다.
	5. 쓰레드 로컬의 thread-A 전용 보관소에 사용자A 데이터를 보관한다.
	
  사용자A 저장 요청 종료 
    1. 사용자의 HTTP 응답이 끝난다.
	2. WAS는 사용이 끝난 thread-A를 쓰레드 풀에 반환한다. 쓰레드를 생성하는 
	   비용은 비싸기 때문에 쓰레드를 제거하지 않고, 보통 쓰레드 풀을 통해서 
	   쓰레드를 재사용한다. 
	3. thread-A는 쓰레드풀에 아직 살아있다. 따라서 쓰레드 로컬의 thread-A
	   전용 보관소에 사용자A 데이터도 함께 살아있게 된다.
 
  사용자B 조회 요청 
    1. 사용자B가 조회를 위한 새로운 HTTP 요청을 한다.
	2. WAS는 쓰레드 풀에서 쓰레드 하나를 조회한다.
	3. 쓰레드 thread-A가 할당되었다.(물론 다른 쓰레드가 할당될 수 도 있다.)
	4. 이번에는 조회하는 요청이다. thread-A는 쓰레드 로컬에서 데이터를 조회한다.
	5. 쓰레드 로컬은 thread-A 전용 보관소에 있는 사용자A값을 반환한다.
	6. 결과적으로 사용자A 값이 반환된다.
	7. 사용자B는 사용자A의 정보를 조회하게 된다.
	
  결과적으로 사용자B는 사용자A의 데이터를 확인하게되는 심각한 문제가 발생하게 된다.
  이런 문제를 예방하려면 사용자A의 요청이 끝날 때 쓰레드 로컬의 값을 
  ThreadLocal.remove()를 통해서 꼭 제거해야 한다. 
  쓰레드 로컬을 사용할 때는 이 부분을 꼭! 기억하자.
```

## 템플릿 메서드 패턴과 콜백 패턴 

### 템플릿 메서드 패턴 - 시작 
```
  지금까지 로그 추적기를 열심히 잘 만들었다. 요구사항도 만족하고, 파라미터를 넘기는 
  불편함을 제거하기 위해 쓰레드 로컬도 도입했다. 그런데 로그 추적기를 막상
  프로젝트에 도입하려고 하니 개발자들의 반대의 목소리가 높다 
  로그 추적기 도입 전과 도입 후의 코드를 비교해보자.
  
  V0 시절 코드와 비교해서 V3 코드를 보자 
    - V0는 해당 메서드가 실제 처리해야 하는 핵심 기능만 깔끔하게 남아있다. 
	  반면에 V3에는 핵심 기능보다 로그를 출력해야 하는 부가 기능 코드가 
	  훨씬 더 많고 복잡하다. 
	- 앞으로 코드를 설명할 때 핵심 기능과 부가 기능으로 구분해서 설명하겠다. 

  핵심 기능 vs 부가 기능 
    - 핵심 기능은 해당 객체가 제공하는 고유의 기능이다. 예를 들어서 orderService의 
	  핵심 기능은 주문로직이다. 메서드 단위로 보면 orderService.orderItem()의
	  핵심 기능은 주문 데이터를 저장하기 위해 리포지토리를 호출하는 
	  orderRepository.save(itemId) 코드가 핵심 기능이다. 
	- 부가 기능은 핵심 기능을 보조하기 위해 제공되는 기능이다. 예를 들어서 로그 추적 로직
	  ,트랜잭션 기능이 있다. 이러한 부가 기능은 단독으로 사용되지는 않고, 
	  핵심 기능과 함께 사용된다. 예를 들어서 로그 추적 기능은 어떤 핵심 기능이 
	  호출되었는지 로그를 남기기 위해 사용한다. 그러니까 핵심 기능을 보조하기 위해 
	  존재한다.
	  
  V0는 핵심 기능만 있지만, 로그 추적기를 추가한 V3코드는 핵심 기능과 부가 기능이 
  함께 섞여있다. V3를 보면 로그 추적기의 도입으로 핵심 기능 코드보다 부가 기능을 
  처리하기 위한 코드가 더 많아졌다. 소위 배보다 배꼽이 더 큰 상황이다. 만약 
  클래스가 수백 개라면 어떻게 하겠는가?
  
  이 문제를 좀 더 효율적으로 처리할 수 있는 방법이 있을까?
  V3 코드를 유심히 잘 살펴보면 다음과 같이 동일한 패턴이 있다.
  
  TraceStatus status = null;
  try {
  
	status = trace.begin("message");
	//핵심 기능 호출 
	trace.end(status);
  
  } catch(Exception e) {
	trace.exception(status,e);
	throw e;
  }
  
  Controller, Service, Repository의 코드를 잘 보면, 로그 추적기를 
  사용하는 구조는 모두 동일하다. 중간에 핵심 기능을 사용하는 코드만 다를 뿐이다.
  부가 기능과 관련된 코드가 중복이니 중복을 별도의 메서드로 뽑아내면 될 것 같다. 
  그런데 try ~ catch는 물론이고, 핵심 기능 부분이 중간에 있어서 
  단순하게 메서드로 추출하는 것은 어렵다. 
  
  변하는 것과 변하지 않는 것을 분리
    - 좋읜 설계는 변하는 것과 변하지 않는 것을 분리하는 것이다. 
	  여기서 핵심 기능 부분은 변하고, 로그 추적기를 사용하는 부분은 
	  변하지 않는 부분이다. 이 둘을 분리해서 모듈화 해야 한다. 
	  
	- 템플릿 메서드 패턴(Template Method Pattern)은 이런 문제를 
	  해결하는 디자인 패턴이다.
```

### 템플릿 메서드 패턴 - 예제1
```
  템플릿 메서드 패턴을 쉽게 이해하기 위해 단순한 예제 코드를 만들어보자.
  
  TemplateMethodTest
    - logic1(), logic2()를 호출하는 단순한 테스트 코드이다. 
	- logic1()과 logic2()는 시간을 측정하는 부분과 비즈니스 로직을 
	  실행하는 부분이 함께 존재한다.
	
	- 변하는 부분: 비즈니스 로직 
	- 변하지 않는 부분: 시간 측정 
	
	이제 템플릿 메서드 패턴을 사용해서 변하는 부분과 변하지 않는 부분을 
	분리해보자. 
```

### 템플릿 메서드 패턴 - 예제2
```
  AbstractTemplate
    - 주의: 테스트 코드(src/test)에 위치한다.
	- 템플릿 메서드 패턴은 이름 그대로 템플릿을 사용하는 방식이다. 템플릿은
	  기준이 되는 거대한 틀이다. 템플릿이라는 틀에 변하지 않는 부분을 
	  몰아둔다. 그리고 일부 변하는 부분을 별도로 추출해서 해결한다. 
	  
	- AbstractTemplate 코드를 보자. 변하지 않는 부분인 
	  시간 측정 로직을 몰아둔 것을 확인할 수 있다. 이제 이것이 하나의
	  템플릿이 된다. 그리고 템플릿 안에서 변하는 부분은 call() 메서드를 
	  호출해서 처리한다. 템플릿 메서드 패턴은 부모 클래스에 변하지 않는 
	  템플릿 코드를 둔다. 그리고 변하는 부분은 자식 클래스에 두고 상속과 
	  오버라이딩을 사용해서 처리한다.

  SubClassLogic1
    - 주의: 테스트 코드(src/test)에 위치한다.
	- 변하는 부분인 비즈니스 로직1을 처리하는 자식 클래스이다. 템플릿이 
	  호출하는 대상인 call() 메서드를 오버라이딩 한다. 
	  
  SubClassLogic2
    - 주의: 테스트 코드(src/test)에 위치한다.
	- 변하는 부분인 비즈니스 로직2을 처리하는 자식 클래스이다. 템플릿이 
	  호출하는 대상인 call() 메서드를 오버라이딩 한다.
	  
  TemplateMethodTest - templateMethodV1() 추가 
    - 템플릿 메서드 패턴으로 구현한 코드를 실행해보자.
	- template1.execute()를 호출하면 템플릿 로직인 
	  AbstractTemplate.execute()를 실행한다. 여기서 
	  중간에 call() 메서드를 호출하는데, 이 부분이 오버라이딩 되어있다.
	  따라서 현재 인스턴스인 SubClassLogic1 인스턴스의 
	  SubClassLogic1.call() 메서드가 호출된다.
	  
  템플릿 메서드 패턴은 이렇게 다형성을 사용해서 변하는 부분과 변하지 않는 
  부분을 분리하는 방법이다.
```

### 템플릿 메서드 패턴 - 예제3
```
  익명 내부 클래스 사용하기 
    - 템플릿 메서드 패턴은 SubClassLogic1, SubClassLogic2 처럼 
	  클래스를 계속 만들어야 하는 단점이 있다. 익명 내부 클래스를 사용하면 
	  이런 단점을 보완할 수 있다. 
	- 익명 내부 클래스를 사용하면 객체 인스턴스를 생성하면서 동시에 생성할 
	  클래스를 상속 받은 자식 클래스를 정의할 수 있다. 이 클래스는 
	  SubClassLogic1 처럼 직접 지정하는 이름이 없고 클래스 내부에 
	  선언되는 클래스여서 익명 내부 클래스라 한다. 익명 내부 클래스에 대한 
	  자세한 내용은 자바 기본 문법을 참고하자 
  
  TemplateMethodTest - templateMethodV2() 추가
    - 실행 결과를 보면 자바가 임의로 만들어주는 익명 내부 클래스 이름은 
	  TemplateMethodTest$1, TemplateMethodTest$2 
	  인 것을 확인 할 수 있다.
```

### 템플릿 메서드 패턴 - 적용1 
```
  이제 우리가 만든 애플리케이션의 로그 추적기 로직에서 템플릿 메서드 패턴을 적용해보자.
  
  AbstractTemplate
    - AbstractTemplate은 템플릿 메서드 패턴에서 부모 클래스이고, 
	  템플릿 역할을 한다.
	- <T> 제네릭을 사용했다. 반환 타입을 정의한다.
	- 객체를 생성할 때 내부에서 사용할 LogTrace trace를 전달 받는다 
	- 로그에 출력할 message를 외부에서 파라미터로 전달받는다. 
	- 템플릿 코드 중간에 call() 메서드를 통해서 변하는 부분을 처리한다. 
	- abstract T call()은 변하는 부분을 처리하는 메서드이다. 
	  이 부분은 상속으로 구현해야 한다. 
	  
  v3 -> v4 복사 
    - 먼저 기존 프로젝트 코드를 유지하기 위해 v4 애플리케이션을 
	  복사해서 만들자 
	  
	- hello.advanced.app.v4 패키지 생성 
	- 복사 
	  - v3.OrderControllerV3 -> v4.OrderControllerV4
	  - v3.OrderServiceV3 -> V4.OrderServiceV4
	  - v3.OrderRepositoryV3 -> v4.OrderRepositoryV4
	- 코드 내부 의존관계 클래스를 V4로 변경 
	  - OrderControllerV4: OrderServiceV3 -> OrderServiceV4
	  - OrderServiceV4: OrderRepositoryV3 -> OrderRepositoryV4
	- OrderControllerV4 매핑 정보 변경 
	  - @GetMapping("/v4/request")
	- AbstractTemplate을 사용하도록 코드 변경 
	
  OrderControllerV4
    - AbstractTemplate<String>
	  - 제네릭을 String으로 설정했다. 따라서 AbstractTemplate의
	    반환 타입은 String이 된다. 
	- 익명 내부 클래스 
	  - 익명 내부 클래스를 사용한다. 객체를 생성하면서 AbstractTemplate를
	    상속받은 자식 클래스를 정의했다. 
	  - 따라서 별도의 자식 클래스를 직접 만들지 않아도 된다. 
	- template.execute("OrderController.request()")
	  - 템플릿을 실행하면서 로그로 남길 message를 전달한다. 
	  
  OrderServiceV4
    - AbstractTemplate<Void>
	  - 제네릭에서 반환 타입이 필요한데, 반환할 내용이 없으면 Void 타입을 
	    사용하고 null을 반환하면 된다. 참고로 기본 타입인 void, int등을
		선언할 수 없다. 

  OrderRepositoryV4
```