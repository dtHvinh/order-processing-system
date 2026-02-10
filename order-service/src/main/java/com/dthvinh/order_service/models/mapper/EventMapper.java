package com.dthvinh.order_service.models.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.dthvinh.order_service.models.CreateOrderRequest;
import com.dthvinh.order_service.models.event.OrderCreatedEvent;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "eventId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "orderId", expression = "java(java.util.UUID.randomUUID())")
    OrderCreatedEvent toOrderCreatedEvent(CreateOrderRequest createOrderRequest);
}
