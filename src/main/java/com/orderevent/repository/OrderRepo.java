package com.orderevent.repository;

import com.orderevent.dto.OrderCreatedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends MongoRepository<OrderCreatedEvent,String> {

    List<OrderCreatedEvent> findByOrderId(String orderId);
}
