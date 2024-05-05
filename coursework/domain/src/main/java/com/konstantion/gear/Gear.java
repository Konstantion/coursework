package com.konstantion.gear;

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
public class Gear {
    private UUID id;
    private String name;
    private Double price;
    private Double weight;
    private UUID categoryId;
    private byte[] imageBytes;
    private String description;
    private UUID creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime deactivateAt;
    private Boolean active;

    public Gear() {
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gear gear)) return false;
        return Objects.equal(id, gear.id) && Objects.equal(name, gear.name) && Objects.equal(price, gear.price) && Objects.equal(weight, gear.weight) && Objects.equal(categoryId, gear.categoryId) && Objects.equal(imageBytes, gear.imageBytes) && Objects.equal(description, gear.description) && Objects.equal(creatorId, gear.creatorId) && Objects.equal(createdAt, gear.createdAt) && Objects.equal(deactivateAt, gear.deactivateAt) && Objects.equal(active, gear.active);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, price, weight, categoryId, imageBytes, description, creatorId, createdAt, deactivateAt, active);
    }
}
