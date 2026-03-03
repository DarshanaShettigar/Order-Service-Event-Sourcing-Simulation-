package com.orderevent.dto;

import com.orderevent.dto.enums.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class OrderRequest {
    @Id
    private String orderId;
    private String name;
    private int quantity;
    private double price;
    private String userId;
}
