package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;
import org.aspectj.weaver.ast.Or;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // em.find() 할때는 EAGER 전략이 좋아 보일 수는 있다 -> 쿼리하나로 조회할 수 있으니까..
    // 하지만 JPQL로 select o from Order o; -> SQL 로 바꾸면 EAGER 이런거 상관없이 쿼리가 나간다.. N + 1 문제라고도 하는데
    // ex) order의 member가 100명 있다 하면 100 + 1 (order) 가 즉 100번의 쿼리가 나가게 된다..
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id") // 외래키가 있는 쪽이 연관관계의 주인이 된다.~
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    // 일대일 관계에서는 어디에나 외래키를 둬도 되는데.. 주로 많이 엑세스하는 곳에 외래키를 두고 연관관계 주인으로 설정한다.
    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; // 주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문상태 [ORDER, CANCEL]

    //==연관관계 메서드(양방향 관계)==//
    // 연관관계 편의 메서드의 위치는 핵심적으로 컨트롤하는쪽에 있는 것이 좋다.
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성메서드==//
    // 주문 생성과 관련된 로직을 다 여기에 응집해 둔다.
    // 따라서 주문과 관련된 수정이 필요하면 이 메서드에만 들어와서 수정하면 되도록!
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비지니스 로직==//
    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //==조회로직==//

    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice= 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

}
