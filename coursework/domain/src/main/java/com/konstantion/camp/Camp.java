package com.konstantion.camp;

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
public class Camp {
    private UUID id;
    private String name;
    private LocalDateTime createdAt;
    private Boolean active;

    public Camp() {
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Camp camp)) return false;
        return Objects.equal(id, camp.id) && Objects.equal(name, camp.name) && Objects.equal(createdAt, camp.createdAt) && Objects.equal(active, camp.active);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, createdAt, active);
    }
}
