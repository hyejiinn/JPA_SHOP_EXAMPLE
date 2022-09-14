package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne( fetch = FetchType.LAZY, mappedBy = "delivery")
    @JsonIgnore // 양방향 관계에는 꼭 한쪽에 JsonIgnore을 걸어줘야 한다.
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; // READY, COMP
}
