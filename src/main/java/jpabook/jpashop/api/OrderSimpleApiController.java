package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * XToOne (ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    // 간단한 주문 조회 V1:  엔티티 직접 노출 -> 완전 비추!!!
    // 엔티티를 그대로 노출하는 이 방법은 사용하면 안됨!! -> 다시 강조하기 위해 보여준 것
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // LAZY 강제 초기화
            order.getDelivery().getAddress(); // LAZY 강제 초기화
        }
        return all;
    }

    // 간단한 주문 조회 v2 : 엔티티를 DTO로 변환
    // 지연로딩으로 인해 쿼리가 너무 많이 나가는 단점 존재 => N+1 문제
    // 그렇다고 해결 방법이라고 EAGER로 전략을 바꾸면 예상치 못한 쿼리가 더 많이 나오게 된다..!! -> LAZY가 답이긴 하다.
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
//        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
//        List<SimpleOrderDto> result = orders.stream().map(o -> new SimpleOrderDto(o)).collect(toList());

        return orderRepository.findAllByString(new OrderSearch()) // ORDERS 조회 = 2개
                .stream().map(SimpleOrderDto::new) // N + 1 -> 회원 N + 배송 N  -> 총 5번 의 쿼리가 나가게 됨..!
                .collect(toList());
    }

    // 간단한 주문 조회 v3 : 엔티티를 DTO로 변환 - 패치조인 최적화
    // 패치 조인을 사용함으로써 쿼리를 하나만 나갈 수 있도록 해결 !
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream().map(o -> new SimpleOrderDto(o)).collect(toList());
    }

    // 간단한 주문 조회 v4: JPA에서 DTO로 바로 조회 -> 원하는 값들만 조회할 수 있음.
    // new 명령어를 사용해서 JPQL 결과를 DTO로 즉시 변환
    // 근데 V3과 어느것이 더 좋다고 우열을 가리기는 힘들다.
    // 리포지토리 재사용성이 떨어짐
    // 성능상으로는 v4가 더 좋긴? 하다고 함 -> 트레이드오프 발생 (근데 성능차이가 그렇게 많이 나지는 않음)
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrdersDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화 (프록시 초기화) 쿼리 나가는 시점
            orderDate = order.getOrderDate();
            address = order.getDelivery().getAddress();  // LAZY 초기화 (프록시 초기화) 쿼리 나가는 시점
            orderStatus = order.getStatus();
        }
    }

}
