package com.konstantion.equipment;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;


@Builder
@AllArgsConstructor
@Getter
@Setter
public class Equipment {
    private UUID id;
    @Builder.Default
    private List<UUID> gearsId = new ArrayList<>();
    private UUID expeditionId;
    private UUID userId;
    private UUID logId;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    @Builder.Default
    private Boolean active = true;

    public Equipment() {
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasBill() {
        return nonNull(logId);
    }

    public void removeBill() {
        this.logId = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipment equipment)) return false;
        return Objects.equal(id, equipment.id) && Objects.equal(gearsId, equipment.gearsId) && Objects.equal(expeditionId, equipment.expeditionId) && Objects.equal(userId, equipment.userId) && Objects.equal(logId, equipment.logId) && Objects.equal(createdAt, equipment.createdAt) && Objects.equal(closedAt, equipment.closedAt) && Objects.equal(active, equipment.active);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, gearsId, expeditionId, userId, logId, createdAt, closedAt, active);
    }
}
