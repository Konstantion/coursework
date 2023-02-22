package com.konstantion.order;

import com.konstantion.bucket.Bucket;
import com.konstantion.exceptions.ValidationException;
import com.konstantion.order.dto.OrderDto;
import com.konstantion.order.validator.OrderValidator;
import com.konstantion.user.User;
import com.konstantion.utils.validator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public record DomainOrderService(
        OrderRepository orderRepository,
        OrderValidator orderValidator,
        Logger logger
) implements OrderService {
    private static final OrderMapper MAPPER = OrderMapper.INSTANCE;

    public DomainOrderService(OrderRepository orderRepository, OrderValidator orderValidator) {
        this(orderRepository, orderValidator, LoggerFactory.getLogger(DomainOrderService.class));
    }

    @Override
    public OrderDto createOrder(User user, Bucket bucket) {
        Order order = Order.builder()
                .customer(user)
                .products(bucket.getProducts())
                .totalPrice(bucket.getTotalPrice())
                .placedAt(LocalDateTime.now())
                .build();

        ValidationResult validationResult = orderValidator.isOrderValid(order);

        if (validationResult.isErrorsPresent()) {
            logger.warn("Failed to create order {}, given data is invalid {}",
                    order, validationResult.getErrorsAsMap());
            throw new ValidationException("Failed to create order, given data is invalid",
                    validationResult.getErrorsAsMap());
        }

        order = orderRepository.save(order);

        return MAPPER.toDto(order);
    }
}
