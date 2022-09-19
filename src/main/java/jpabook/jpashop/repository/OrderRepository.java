package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static jpabook.jpashop.domain.QMember.*;
import static jpabook.jpashop.domain.QOrder.*;

@Repository
public class OrderRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * QueryDsl 소개
     * 장점: 컴파일 시점에 오류를 다 잡을 수 있다.
     *      동적 쿼리 가능
     */
    public List<Order> findAll(OrderSearch orderSearch) {

        return query.select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private static BooleanExpression nameLike(String memberName) {
        if (!StringUtils.hasText(memberName)) {
            return null;
        }
        return member.name.like(memberName);
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null) {
            return null;
        }
        return order.status.eq(statusCond);
    }
    /**
     * JPA String
     * 권장하는 방법 아님 (실수할 가능성이 많음)
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {

        // status와 name이 모두 null 일때는...?
        // -> 그래서 동적 쿼리가 필요하다..
//        em.createQuery("select o from Order o join o.member m" +
//                        " where o.status = :status " +
//                        " and m.name like :name", Order.class)
//                .setParameter("status", orderSearch.getOrderStatus())
//                .setParameter("name", orderSearch.getMemberName())
//                .setMaxResults(1000) // 최대 1000개까지 조회
//                .getResultList();

        // 이 방법 강사님 안쓴다고 함..! 그래서 복붙 했음!
        // 이 방법은 번거롭고, 실수로 오류를 내기 참 쉬움
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();

    }

    /**
     * JPA Criteria
     * 권장하는 방법은 아님..ㅎㅎ
     * 실무에서 사용하기에는 너무 복잡함
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(criteria.toArray(new Predicate[criteria.size()]));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);

        return query.getResultList();
    }

    // LAZY 로딩의 N+1 문제 해결 방법 : JPQL의 fetch join
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery("select o from Order o " +
                " join fetch o.member m" +
                " join fetch o.delivery d", Order.class).getResultList();
    }

    // XToOne 관계는 페치 조인을 하더라도 페이징이 잘 된다.
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery("select o from Order o " +
                " join fetch o.member m" +
                " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }


    // JPA distinct 기능
    // 1. DB SQL에 distinct 추가
    // 2. 엔티티 중복된게 있으면 중복 제거 해서 컬렉션에 담아줌.
    // 단점으로 oneToMany에 대해서 페치 조인을 하면 페이징이 불가능하다..!!!! 메모리에서 페이징 처리를 해서 out of memory 날 가능성이 높다..
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d " +
                        " join fetch o.orderItems oi " +
                        " join fetch  oi.item i", Order.class)
                .getResultList();
    }


    // JPA에서 DTO로 바로 조회하는 방법 -> 원하는 데이터만 가져올 수 있다.
    // 근데 재사용성이 적다..
    // API 스펙이 여기에 들어와있는 셈..
    // repository는 가급적 순수한 엔티티를 조회하는 용도로만 쓰는게 좋기 때문에 따로 OrderSimpleQueryRepository라는 리포지토리를 따로 만듦
//    public List<OrderSimpleQueryDto> findOrdersDtos() {
//        return em.createQuery("select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) from Order o " +
//                " join o.member m " +
//                " join o.delivery d", OrderSimpleQueryDto.class).getResultList();
//
//
//    }


}
