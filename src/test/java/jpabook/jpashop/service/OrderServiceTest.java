package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    /**
     * 상품주문
     */
    @Test
    public void createOrder() throws Exception {
        //given
        Member member = createMember("회원1");
        Item book = createBook("책책1", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order order = orderRepository.findOne(orderId);
        System.out.println(order.getOrderItems().size());

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, order.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, order.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000*orderCount, order.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어들어야 한다.", 8, book.getStockQuantity());
    }

    /**
     * 주문 취소
     */
    @Test
    public void cancelOrder() throws Exception {
        //given
        Member member = createMember("회원1");
        Item book = createBook("책책1", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(),  orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order order = orderRepository.findOne(orderId);
        assertEquals("주문 취소시 상태는 CANCEL이다.", OrderStatus.CANCEL, order.getStatus());
        assertEquals("주문 취소시 재고가 원래대로 돌아와야 한다.", 10, book.getStockQuantity());

    }

    /**
     * 상품주문_재고수량초과
     */
    @Test(expected = NotEnoughStockException.class)
    public void overOrder() throws Exception {
        //given
        Member member = createMember("회원1");
        Item book = createBook("책책1", 10000, 10);

        int orderCount = 11;

        //when
        orderService.order(member.getId(), book.getId(), orderCount);

        //then
        fail("재고 수량 부족 예외가 발생해야 한다.");
    }

    private Item createBook(String name, int price, int quantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("서울", "은평", "123-123"));
        em.persist(member);
        return member;
    }
}