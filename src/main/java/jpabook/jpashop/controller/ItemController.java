package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(@ModelAttribute("form") @Valid BookForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "items/createItemForm";
        }

        Book book = new Book();
        // 지금은 setXXX으로 처리했지만.. createBook() 이런 메서드를 만들어주는 것이 더 좋은 방법!
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.save(book);
//        return "redirect:/";
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm bookForm = new BookForm();
        bookForm.setId(item.getId());
        bookForm.setName(item.getName());
        bookForm.setPrice(item.getPrice());
        bookForm.setAuthor(item.getAuthor());
        bookForm.setStockQuantity(item.getStockQuantity());
        bookForm.setIsbn(item.getIsbn());

        model.addAttribute("form", bookForm);

        return "items/updateItemForm";
    }

    /**
     * 엔티티를 변경할 때는 항상 변경 감지를 사용하자.
     * 컨트롤러에서 밑에 코드처럼 어설프게 엔티티를 생성해서 하지말고
     * 트랜잭션이 있는 서비스 계층에 식별자 id와 변경할 데이터를 명확하게 전달. -> itemService.updateItem()...
     * 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고 엔티티를 직접 변경해서
     * 변경 감지 dirty checking 하는것이 좋다.
     * merge는 전체 데이터를 변경하기 때문에 데이터를 입력하지 않아서 null되는 부분이 발생하면 큰 문제가 될 수도!!
     */
    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@ModelAttribute("form") BookForm form, @PathVariable("itemId") Long itemId) {

        // 준영속 엔티티: 영속성 컨텍스트가 더는 관리하지 않는 엔티티 -> JPA가 관리 X -> DB에 UPDATE X
        // book 객체는 이미 db에 한번 저장되었기 때문에 id가 존재한다.
        // 따라서 지금 새로 만든 book 객체에 id를 세팅할 수 있다.
        // -> 이렇게 임의로 만들어낸 엔티티도 기존 식별자를 가지고 있으면 준영속 엔티티라고 볼 수 있다.
//        Book book = new Book();
//        book.setIsbn(form.getIsbn());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setId(form.getId());
//        book.setName(form.getName());

//        itemService.save(book);

        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());

        return "redirect:/items";
    }
}
