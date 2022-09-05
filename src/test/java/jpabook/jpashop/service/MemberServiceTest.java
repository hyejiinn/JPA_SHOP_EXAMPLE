package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional // 이거 있어야 rollback 가능(반복적인 test 가능)
public class MemberServiceTest {

    @Autowired MemberService memberService; // test로 다른 곳에서 참조하거나 하는 곳이 없기 때문에 그냥 필드 주입 선택
    @Autowired MemberRepository memberRepository;

    @Test
//    @Rollback(value = false) DB 쿼리 나가는것을 확인하기 위해서는 false로 해서 확인할 수 있음!
    public void 회원가입() {
        // given :
        Member member = new Member();
        member.setName("kim");

        // when
        Long savedId = memberService.join(member);

        // then
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() {
        // given
        Member memberA = new Member();
        memberA.setName("member");

        Member memberB = new Member();
        memberB.setName("member");

        // when
        memberService.join(memberA);
        memberService.join(memberB); // 예외가 발생해야 한다!!

        // then
        Assertions.fail("예외가 발생해야 한다!!"); // 여기까지 오면 안되고 위에서 에러가 발생해야 한다.
    }
}