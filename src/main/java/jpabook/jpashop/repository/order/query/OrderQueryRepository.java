package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관심사의 분리를 위해서 화면이나 API 관련된 쿼리를 할 때 는 이렇게 따로 repository를 두는 것이 좋다.
 * 핵심 비지니스 로직은 따로 OrderRepository에 두고..
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); //Query 1번

        result.forEach(o -> {
             List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // 똑같은 쿼리 N번 즉 2번 query 나감 => N + 1 문제 발생
             o.setOrderItems(orderItems);
        });

        return result;
    }
    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders();

        List<Long> orderIds = toOrderIds(result); // result에서 orderId만 뽑아서 List로 저장

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds); // 이 orderIds를 가지고 쿼리 실행 후 Map을 사용해서 매칭 성능 향상

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId()))); // orderId에 맞는 orderItems를 set해줌

        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id,i.name, oi.orderPrice, oi.count) from OrderItem oi " +
                                " join oi.item i " +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
        return orderItemMap;
    }

    private static List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id,i.name, oi.orderPrice, oi.count) from OrderItem oi " +
                                " join oi.item i " +
                                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o " +
                                " join o.member m " +
                                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }


    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status,d.address, i.name, oi.orderPrice, oi.count)" +
                                " from Order o " +
                                " join o.member m " +
                                " join o.delivery d" +
                                " join o.orderItems oi " +
                                " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
