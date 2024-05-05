package com.konstantion.expedition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.konstantion.user.Permission.getDefaultTablePermission;
import static com.konstantion.user.Role.TABLE;
import static java.util.Objects.nonNull;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class Expedition implements UserDetails {
    private UUID id;
    private String name;
    private Integer capacity;
    private ExpeditionType expeditionType;
    private String password;
    private UUID campId;
    private UUID equipmentId;
    @Builder.Default
    private Set<UUID> guidesId = new HashSet<>();
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public Expedition() {
    }

    public boolean hasOrder() {
        return nonNull(equipmentId);
    }

    public void removeOrder() {
        this.equipmentId = null;
    }

    public boolean hasWaiters() {
        return guidesId.isEmpty();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = getDefaultTablePermission()
                .stream()
                .map(Enum::name)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + TABLE.name()));

        return grantedAuthorities;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Expedition table)) return false;
        return com.google.common.base.Objects.equal(id, table.id) && com.google.common.base.Objects.equal(name, table.name) && com.google.common.base.Objects.equal(capacity, table.capacity) && expeditionType == table.expeditionType && com.google.common.base.Objects.equal(password, table.password) && com.google.common.base.Objects.equal(campId, table.campId) && com.google.common.base.Objects.equal(equipmentId, table.equipmentId) && com.google.common.base.Objects.equal(guidesId, table.guidesId) && com.google.common.base.Objects.equal(active, table.active) && com.google.common.base.Objects.equal(createdAt, table.createdAt) && com.google.common.base.Objects.equal(deletedAt, table.deletedAt);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(id, name, capacity, expeditionType, password, campId, equipmentId, guidesId, active, createdAt, deletedAt);
    }
}
