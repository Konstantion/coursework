package com.konstantion.call;


import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class Call {
    private UUID id;
    private UUID tableId;
    @Builder.Default
    private Set<UUID> waitersId = new HashSet<>();
    private Purpose purpose;
    private LocalDateTime openedAt;

    public Call() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Call call)) return false;
        return Objects.equal(id, call.id) && Objects.equal(tableId, call.tableId) && Objects.equal(waitersId, call.waitersId) && purpose == call.purpose && Objects.equal(openedAt, call.openedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, tableId, waitersId, purpose, openedAt);
    }
}
