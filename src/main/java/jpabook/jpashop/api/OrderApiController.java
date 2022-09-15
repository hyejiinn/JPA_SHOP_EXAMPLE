package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 일대다(OneToMany) 관계 조회 및 최적화하는 방법
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * 주문조회 v1: 엔티티 직접 노출
     * 엔티티가 변하면 API 스펙이 변한다.
     * 트랜잭션 안에서 지연로딩 필요
     * 양방향 연관관계 문제가 있다 -> 무한루프 걸리지 않게 한쪽에 @JsonIgnore 해줘야함
     * 엔티티를 직접 노출하는 방법은 안좋습니다~~.. 비추 비추
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            // 프록시 강제 초기화
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }

        return all;
    }

    /**
     * 주문 조회 v2: 엔티티를 DTO로 변환
     * 지연로딩으로 인한 너무 많은 SQL이 실행됨 -> N + 1 문제 발생
     * ORDER 1번
     * MEMBER, ADDRESS N 번
     * ORDERITEM N번
     * ITEM N번 (ORDERITEM 조회 수 만큼)
     * 1 + 2 + 2 + 2+ (2 + 2) = 11
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 주문 조회 v3: 엔티티를 DTO로 변환 - 페치 조인 최적화
     * v2 버전과 코드는 보면 똑같다.
     * 근데 패치조인으로 인해서 성능 최적화가 됐다!!
     * 참고로 컬렉션 패치 조인은 1개만 사용해야 한다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    /**
     * 주문 조회 v3.1 : 엔티티를 DTO로 변환 - 페이징 한계 돌파
     *  1. XToOne 관계는 모두 페치조인 한다.
     *  2. 컬렉션은 지연 로딩으로 조회한다.
     *  3. 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size 또는 @BatchSize를 적용한다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //        private List<OrderItem> orderItems; // 엔티티를 이렇게 반환해서는 안된다. -> dto로 바꿔야한다.
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
//            order.getOrderItems().stream().forEach(o -> o.getItem().getName());
//            orderItems = order.getOrderItems();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto{

        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
