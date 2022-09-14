package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
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
