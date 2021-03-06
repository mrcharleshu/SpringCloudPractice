package com.charles.springcloud.data.sharding.server.repository;

import com.charles.springcloud.data.sharding.server.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
