package com.konstantion.log;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class Log {
    private UUID id;
    private UUID guideId;
    private UUID equipmentId;
    private UUID guestId;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private Boolean active;
    private Double price;
    private Double priceWithDiscount;

    public Log() {
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Log log)) return false;
        return Objects.equal(id, log.id) && Objects.equal(guideId, log.guideId) && Objects.equal(equipmentId, log.equipmentId) && Objects.equal(guestId, log.guestId) && Objects.equal(createdAt, log.createdAt) && Objects.equal(closedAt, log.closedAt) && Objects.equal(active, log.active) && Objects.equal(price, log.price) && Objects.equal(priceWithDiscount, log.priceWithDiscount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, guideId, equipmentId, guestId, createdAt, closedAt, active, price, priceWithDiscount);
    }
}
