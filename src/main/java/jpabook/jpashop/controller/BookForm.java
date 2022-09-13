package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter @Setter
public class BookForm {

    private Long id;

    @NotEmpty(message = "상품명을 입력해주세요.")
    @Size(min = 1, message = "상품명은 최소 1글자 이상으로 입력해주세요.")
    private String name;

    @NotNull(message = "가격을 입력해주세요.")
    @Min(value = 1000, message = "가격은 최소 1000원 이상으로 입력해주세요.")
    private Integer price;

    @NotNull(message = "재고 수량을 입력해주세요.")
    @Min(value = 1, message = "재고 수량은 최소 1개 이상으로 입력해주세요.")
    private Integer stockQuantity;

    private String author;
    private String isbn;
}
