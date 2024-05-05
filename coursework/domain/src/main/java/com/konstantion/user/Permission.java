package com.konstantion.user;

import com.google.common.collect.Sets;

import java.util.Set;

public enum Permission {
    //super admin permission
    SUPER_USER,
    //table actions
    CREATE_EXPEDITION, DELETE_EXPEDITION, UPDATE_EXPEDITION, CHANGE_EXPEDITION_STATE,
    ASSIGN_GUIDE_TO_EXPEDITION, REMOVE_GUIDE_FROM_EXPEDITION,
    //user actions
    CREATE_USER, CHANGE_USER_STATE,
    //product actions
    CREATE_GEAR, DELETE_GEAR, UPDATE_GEAR, CHANGE_GEAR_STATE,
    DELETE_GEAR_FROM_EQUIPMENT, ADD_GEAR_TO_EQUIPMENT,
    //hall actions
    CREATE_CAMP, DELETE_CAMP, UPDATE_CAMP, CHANGE_CAMP_STATE,
    //bill actions
    CREATE_BILL_FROM_ORDER, CANCEL_BILL, GET_BILL, CLOSE_BILL, CHANGE_BILL_STATE,
    //call actions
    CALL_GUIDE, CLOSE_CALL,
    //order actions
    OPEN_EQUIPMENT, CLOSE_EQUIPMENT, TRANSFER_EQUIPMENT, DELETE_EQUIPMENT,
    //guest actions
    CREATE_GUEST, UPDATE_GUEST, DELETE_GUEST, CHANGE_GUEST_STATE,
    //category actions
    CREATE_CATEGORY, DELETE_CATEGORY, UPDATE_CATEGORY;


    public static Set<Permission> getDefaultWaiterPermission() {
        return Sets.newHashSet(
                OPEN_EQUIPMENT, CLOSE_EQUIPMENT,
                ADD_GEAR_TO_EQUIPMENT,
                TRANSFER_EQUIPMENT,
                CLOSE_BILL, CREATE_BILL_FROM_ORDER,
                CLOSE_CALL
        );
    }

    public static Set<Permission> getDefaultAdminPermission() {
        Set<Permission> adminPermissions = Sets.newHashSet(
                DELETE_GEAR_FROM_EQUIPMENT, DELETE_EQUIPMENT,
                CREATE_EXPEDITION, UPDATE_EXPEDITION, CHANGE_EXPEDITION_STATE,
                CREATE_USER, CHANGE_USER_STATE,
                CANCEL_BILL, CHANGE_BILL_STATE,
                CREATE_GEAR, UPDATE_GEAR, CHANGE_GEAR_STATE,
                CREATE_CAMP, UPDATE_CAMP, CHANGE_CAMP_STATE,
                CREATE_GUEST, UPDATE_GUEST, CHANGE_GUEST_STATE,
                CREATE_CATEGORY, UPDATE_CATEGORY
        );

        adminPermissions.addAll(getDefaultWaiterPermission());

        return adminPermissions;
    }

    public static Set<Permission> getDefaultTablePermission() {
        return Set.of(
                CALL_GUIDE, GET_BILL
        );
    }
}
