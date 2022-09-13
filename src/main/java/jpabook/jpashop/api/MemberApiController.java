package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 회원 조회 API V1
     * 순수하게 회원 정보만 보고 싶은데 orders도 같이 보이게 된다.
     * (주문 정보가 만약 들어있다면 주문 정보도 노출된다..!)
     * 엔티티를 직접 노출하고, 엔티티를 변경하면 API 스펙이 바뀌어 버린다.. -> 큰 장애를 일으킨다.
     * -> API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
     * 그리고 array를 그냥 반환하면 스펙 확장이 어렵다.
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /**
     * 회원 조회 API V2
     * 응답 값으로 엔티티가 아닌 별도의 DTO 사용
     * 엔티티가 변해도 API 스펙이 변경되지 않는다.
     */
    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        return new Result(collect.size(), collect);
    }

    /**
     * 회원 등록 API V1
     * 엔티티에서 @NotEmpty 같은 검증 어노테이션을 넣으면 안됨
     * 엔티티를 변경해버리면 API 스펙이 바뀐다..
     * -> API 스펙을 위한 별도의 DTO를 만들어야 한다.
     * 엔티티를 파라미터로 받아서도 안되고 외부로 노출해서도 안된다.
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

    /**
     * array로 반환했던 회원조회 v1 api에서는 api 확장이 어려웠지만,
     * Result<T> 처럼 한번 감싸주면 확장하기 쉬워진다.
     * @param <T>
     */
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
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
