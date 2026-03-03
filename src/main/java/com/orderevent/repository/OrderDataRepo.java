package com.orderevent.repository;

import com.orderevent.dto.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDataRepo extends JpaRepository<OrderRequest, String> {
}
