package io.github.douglasdreer.managerorder.application.mapper;

import io.github.douglasdreer.managerorder.application.dto.OrderInputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderItemOutputDTO;
import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;
import io.github.douglasdreer.managerorder.domain.entity.Order;
import io.github.douglasdreer.managerorder.domain.entity.OrderItem;
import io.github.douglasdreer.managerorder.domain.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class OrderMapperImpl implements  OrderMapper {

    @Override
    public Order toEntity(OrderInputDTO dto) {
        Order order = Order.builder()
                .externalId(dto.externalId())
                .status(OrderStatus.RECEIVED)
                .build();

        List<OrderItem> items = dto.items().stream()
                .map(itemDto -> OrderItem.builder()
                        .productName(itemDto.productName())
                        .unitPrice(itemDto.unitPrice())
                        .quantity(itemDto.quantity())
                        .order(order) // Vincula o pai ao filho
                        .build())
                .toList();

        order.setItems(new ArrayList<>(items));

        return order;
    }

    @Override
    public OrderOutputDTO toDto(Order entity) {
        List<OrderItemOutputDTO> itemDTOs = entity.getItems().stream()
                .map(item -> new OrderItemOutputDTO(
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getTotalAmount()
                ))
                .toList();

        return new OrderOutputDTO(
                entity.getId(),
                entity.getExternalId(),
                entity.getTotalValue(),
                entity.getStatus(),
                entity.getCreatedAt(),
                itemDTOs
        );
    }
}
