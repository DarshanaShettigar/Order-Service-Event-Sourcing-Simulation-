package com.orderevent.service;

import com.orderevent.dto.OrderCreatedEvent;
import com.orderevent.dto.OrderRequest;
import com.orderevent.dto.OrderResponse;
import com.orderevent.dto.OrderStatus;
import com.orderevent.exception.OrderEventException;
import com.orderevent.exception.OrderNotFoundException;
import com.orderevent.repository.OrderDataRepo;
import com.orderevent.repository.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Value("${order.event.topicName}")
    private String topicName;

    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    OrderDataRepo orderDataRepo;

    //Sends events to Kafka Topic
    public void sendToKafka(OrderCreatedEvent orderEvent){
        kafkaTemplate.send(topicName, orderEvent);
    }

    public OrderResponse placeOrder(OrderRequest orderRequest){
        String orderId = UUID.randomUUID().toString().split("-")[0];
        orderRequest.setOrderId(orderId);
        orderDataRepo.save(orderRequest);
        OrderCreatedEvent event =  new OrderCreatedEvent(orderId, OrderStatus.CREATED, "Order Created Successfully", LocalDateTime.now().toString());
        orderRepo.save(event);
        sendToKafka(event);
        return new OrderResponse(orderId, OrderStatus.CREATED);
    }

    public OrderResponse confirmOrder(String orderId){
        orderDataRepo.findById(orderId).orElseThrow(()->new OrderNotFoundException("No Orders Found with the given OrderId: "+orderId));
        List<OrderCreatedEvent> orderevents = orderRepo.findByOrderId(orderId);
        if(orderevents.getLast().getStatus().equals(OrderStatus.CONFIRMED) ||
                orderevents.getLast().getStatus().equals(OrderStatus.SHIPPED) ||
                orderevents.getLast().getStatus().equals(OrderStatus.DELIVERED)) {
            throw new OrderEventException("Order with " +orderId+ " is already Confirmed");
        }
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, OrderStatus.CONFIRMED, "Order Confirmed Successfully", LocalDateTime.now().toString());
        orderRepo.save(event);
        sendToKafka(event);
        return new OrderResponse(orderId, OrderStatus.CONFIRMED);
    }


    //Receives events from kafka topic
    @KafkaListener(topics = "order-events", groupId = "shipping-service")
    public void receiveFromKafka(OrderCreatedEvent orderEvent) {
        if (orderEvent.getStatus().equals(com.orderevent.dto.OrderStatus.CONFIRMED)) {
            shipOrder(orderEvent.getOrderId().toString());
        }
    }

    public void shipOrder(String orderId) {
        List<OrderCreatedEvent> orderevents = orderRepo.findByOrderId(orderId);
        if(orderevents.isEmpty()){
            throw new OrderNotFoundException("No Orders Found with the given OrderId: "+orderId);
        } else if(orderevents.getLast().getStatus().equals(OrderStatus.SHIPPED) ||
                orderevents.getLast().getStatus().equals(OrderStatus.DELIVERED)) {
            throw new OrderEventException("Order with id (" +orderId+ ") is already Shipped");
        } else if(!(orderevents.getLast().getStatus().equals(OrderStatus.CONFIRMED))) {
            throw new OrderEventException("Order with id (" +orderId+ ") is Yet to be Confirmed");
        } else {
            OrderCreatedEvent event =  new OrderCreatedEvent(orderId, OrderStatus.SHIPPED, "Order Shipped Successfully", LocalDateTime.now().toString());
            orderRepo.save(event);
        }
    }

    public void deliverOrder(String orderId) {
        List<OrderCreatedEvent> orderevents = orderRepo.findByOrderId(orderId);
        if(orderevents.isEmpty()){
            throw new OrderNotFoundException("No Orders Found with the given OrderId: "+orderId);
        } else if (orderevents.getLast().getStatus().equals(OrderStatus.DELIVERED)) {
            throw new OrderEventException("Order with id (" +orderId+ ") is already Delivered");
        } else if(!(orderevents.getLast().getStatus().equals(OrderStatus.SHIPPED))) {
            throw new OrderNotFoundException("Order with id (" +orderId+ ") is Yet to be Shipped");
        }  else {
            OrderCreatedEvent event =  new OrderCreatedEvent(orderId, OrderStatus.DELIVERED, "Order Delivered Successfully", LocalDateTime.now().toString());
            orderRepo.save(event);
        }
    }

}
