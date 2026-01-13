package io.github.douglasdreer.managerorder.infrastructure.messaging;

import io.github.douglasdreer.managerorder.application.dto.OrderOutputDTO;

public interface OrderProducer {
    void sendCalculatedOrder(OrderOutputDTO order);
}
