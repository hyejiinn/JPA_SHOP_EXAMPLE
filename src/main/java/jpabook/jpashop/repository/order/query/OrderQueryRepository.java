package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

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
}
