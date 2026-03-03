package com.orderevent.controller;

import com.orderevent.dto.OrderRequest;
import com.orderevent.dto.OrderResponse;
import com.orderevent.dto.enums.OrderStatus;
import com.orderevent.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest){
        OrderResponse orderResponse = orderService.placeOrder(orderRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @PutMapping("/confirm/{orderId}")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable String orderId){
        OrderResponse orderResponse = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(orderResponse);
    }

    @PutMapping("/ship/{orderId}")
    public ResponseEntity<String> shipOrder(@PathVariable String orderId) {
        orderService.shipOrder(orderId);
        return new ResponseEntity<>("Order Shipped Successfully", HttpStatus.OK);
    }

    @PutMapping("/deliver/{orderId}")
    public ResponseEntity<String> deliverOrder(@PathVariable String orderId) {
        orderService.deliverOrder(orderId);
        return new ResponseEntity<>("Order Delivered Successfully", HttpStatus.OK);
    }
}
