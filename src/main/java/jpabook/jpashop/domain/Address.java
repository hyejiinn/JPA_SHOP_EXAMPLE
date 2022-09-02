package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

/**
 * 값 타입은 변경 불가능하게 설계해야 한다.
 * @Setter를 제거하고, 생성자에 값을 모두 초기화해서 변경 불가능한 클래스를 만들어야 한다.
 *
 */
@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    // JPA는 기본 생성자가 필수이다. (리플렉션, 프록시 등을 사용하기 위해서)
    // public 으로 하면 사람들이 막 생성하는 상황이 발생하기 때문에 jpa 스펙에서 protected까지 해준다~
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
