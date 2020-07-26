package jpa_study.jpashop.service;

import jpa_study.jpashop.Repository.OrderRepository;
import jpa_study.jpashop.domain.Address;
import jpa_study.jpashop.domain.Member;
import jpa_study.jpashop.domain.Order;
import jpa_study.jpashop.domain.OrderStatus;
import jpa_study.jpashop.domain.item.Book;
import jpa_study.jpashop.exception.NotEnoughStockException;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = new Member();
        member.setName("member1");
        member.setAddress(new Address("aaa", "bbb", "ccc"));
        em.persist(member);

        Book book = new Book();
        book.setName("jpa book");
        book.setPrice(10000);
        book.setStockQuantity(10);
        em.persist(book);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, getOrder.getStatus());
        assertEquals(1, getOrder.getOrderItems().size());
        assertEquals(10000 * orderCount, getOrder.getTotalPrice());
        assertEquals(8, book.getStockQuantity());

    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = new Member();
        member.setName("member1");
        member.setAddress(new Address("aaa", "bbb", "ccc"));
        em.persist(member);

        Book book = new Book();
        book.setName("jpa book");
        book.setPrice(10000);
        book.setStockQuantity(10);
        em.persist(book);

        int orderCount = 11;

        //when
        try{
            Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        }catch (NotEnoughStockException e){
            return;
        }

        //then
        fail("재고 수량 부족 예외가 발생해야 한다.");
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = new Member();
        member.setName("member1");
        member.setAddress(new Address("aaa", "bbb", "ccc"));
        em.persist(member);

        Book book = new Book();
        book.setName("jpa book");
        book.setPrice(10000);
        book.setStockQuantity(10);
        em.persist(book);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);


        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals(10, book.getStockQuantity());

    }


}