package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
public class MemberForm {

    @NotEmpty(message = "회원명을 입력해주세요.")
    @Size(min = 2, message = "회원명은 최소 2글자 이상으로 입력해주세요.")
    private String name;

    private String city;
    private String street;
    private String zipcode;
}
