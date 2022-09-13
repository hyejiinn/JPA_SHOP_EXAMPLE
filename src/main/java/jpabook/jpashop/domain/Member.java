package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    // 연관관계의 주인이 아니기 때문에 여기서 값을 변경하려고 해도 변경되지 않는다~
    @OneToMany(mappedBy = "member") // Order 테이블에 있는 member 필드에 의해 매핑된거라는 뜻!
    private List<Order> orders = new ArrayList<>();


}
