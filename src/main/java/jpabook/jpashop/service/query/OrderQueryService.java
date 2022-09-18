package jpabook.jpashop.service.query;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class OrderQueryService {

    /**
     * OSIV 옵션을 false로 지정하면 트랜잭션이 끝나면 데이터베이스 커넥션이 끊기게 된다.
     * 그러므로 트랜잭션 밖에서 즉, 컨트롤러에서는 지연로딩이 되지 않는다. (프록시 초기화도 안되기 때문)
     * 따라서 별도의 Service를 만들어서 화면이나 API에 맞춘 서비스를 만드는것이 좋다 (주로 읽기 전용 트랜잭션 사용)
     *
     * OrderService: 핵심 비지니스 로직
     * OrderQueryService: 화면이나 API에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)
     * -> 핵심 비지니스 로직과 화면이나 API에 맞춘 로직은 서로 라이프 사이클이 다르다.
     * 화면이나 API에 맞춘 로직은 라이프 사이클이 빠른 편이다. ( 변경도 많고)
     * 그런데 핵심 비지니스 로직은 변경되는 일이 거의 없다.
     * 그러므로 크고 복잡한 애플리케이션을 개발한다면, 이 둘의 관심사를 명확하게 분리하는 선택은 유지보수 관점에서 충분한 의미가 있다.
     * (근데 작은 서비스라면 분리 굳이 안해도 되고 서비스가 커지면 분리해도 된다.)
     *
     * OSIV TRUE
     * 장점: 트랜잭션 종료와 상관없이 지연로딩이 가능하기 때문에 Controller에서도 변환 할 수 있다.
     * 단점: 너무 오랜시간동안 데이터베이스 커넥션 리소스를 사용하기 때문에, 실시간 트래픽이 중요한 애플리케이션에서는 커넥션이 모자랄 수 있다. -> 결국 장애로 이어진다.
     *
     * OSIV FALSE
     * 장점: 트랜잭션이 종료되는 시점에 영속성 컨텍스트를 닫고, 데이터베이스 커넥션도 닫기 때문에 리소스 낭비하지 않음
     * 단점: OSIV를 끄면 지연로딩을 모두 트랜잭션 안에서 처리해야한다.
     */
}