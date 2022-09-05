package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository // 컴포넌트 스캔 등록, 자동으로 빈 관리
@RequiredArgsConstructor
public class MemberRepository {

//    @PersistenceContext // 스프링이 엔티티 매니저를 만들어서 자동으로 주입해준다. 근데 boot는 이를 자동으로 주입해주기 때문에..?
//    private EntityManager em;

    // @Autowired 이런식으로 작성해줘도 된다.
    // 원래는 @PersistenceContext로 주입되어야 하는데 스프링부트가 @Autowired 되도록 지원을 해주기 때문에 가능한 것이다.
    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class) //JPQL
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }

}
