package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    // repository는 가급적 순수한 엔티티를 조회하는 용도로만 쓰는게 좋기 때문에 따로 OrderSimpleQueryRepository라는 리포지토리를 따로 만듦
    // 그냥 리포지토리에 있으면 사람들이 그냥 막 갖다쓰고 이러면 유지보수가 헬이 되므로
    // 얘는 그냥 조회 전용이구나~라는 느낌을 주기 위해서 개인적으로 권장한다고 함.
    public List<OrderSimpleQueryDto> findOrdersDtos() {
        return em.createQuery("select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o " +
                " join o.member m " +
                " join o.delivery d", OrderSimpleQueryDto.class).getResultList();

    }
}
