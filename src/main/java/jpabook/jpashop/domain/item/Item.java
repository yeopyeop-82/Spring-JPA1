package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // 비즈니스 로직 !! 재고 관리
    // 도메인 주도 설계, 엔티티 자체에서 해결 할 수 있는것은 엔티티에 넣는 것이 좋다.
    // 데이터에 가까운 곳에 비즈니스 로직이 있는 것이 Good

    /**
     * stock 증가
     */
    public void addStock(int quantity) {
         this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NotEnoughStockException("재고가 없습니다.");
        }
        this.stockQuantity = restStock;
    }
}
