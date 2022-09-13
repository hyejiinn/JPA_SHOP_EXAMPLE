package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 회원 등록 API V1
     * 엔티티에서 @NotEmpty 같은 검증 어노테이션을 넣으면 안됨
     * 엔티티를 변경해버리면 API 스펙이 바뀐다..
     * -> API 스펙을 위한 별도의 DTO를 만들어야 한다.
     * 엔티티를 파라미터로 받아서도 안되고 외부로 노출해서도 안된다.
     * @param member
     * @return
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * 회원 등록 API V2
     * 엔티티를 변경하더라도 API 스펙이 변경되지 않는다.
     * 파라미터로 넘어올 값을 바로 확인이 가능하다. (문서 확인 안해도 됨)
     * 요청 값으로 Member 엔티티 대신에 별도의 DTO를 받는다.
     * @param request
     * @return
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    /**
     * 회원 수정 API
     * PUT 방식은 전체 업데이트를 할 때 사용하는게 맞는것으로 부분 업데이트를 하려면 PATCH를 사용하거나
     * POS를 사용하는 것이 REST 스타일에 맞다.
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        // 커멘드와 쿼리를 분리하는 스타일 -> 강사님 스타일 -> 유지보수성 증대
        // 이렇게 말고 update에서 member를 그냥 반환해도 되는데 이러면 준영속 상태? 의 값이 반환되기 때문에...
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);

        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }


    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
