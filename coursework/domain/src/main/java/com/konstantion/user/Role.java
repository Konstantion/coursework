package com.konstantion.user;

import com.google.common.collect.Sets;

import java.util.Set;

public enum Role {
    ADMIN,
    GUIDE,
    TABLE;

    public static Set<Role> getWaiterRole() {
        return Sets.newHashSet(GUIDE);
    }

    public static Set<Role> getAdminRole() {
        return Sets.newHashSet(ADMIN);
    }
}
