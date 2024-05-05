package com.konstantion.controllers.user;

import com.konstantion.dto.user.converter.UserMapper;
import com.konstantion.dto.user.dto.CreateUserRequestDto;
import com.konstantion.dto.user.dto.UpdateUserRequestDto;
import com.konstantion.dto.user.dto.UserDto;
import com.konstantion.response.ResponseDto;
import com.konstantion.user.Permission;
import com.konstantion.user.Role;
import com.konstantion.user.User;
import com.konstantion.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.konstantion.utils.EntityNameConstants.ORDER;
import static com.konstantion.utils.EntityNameConstants.USER;
import static com.konstantion.utils.EntityNameConstants.USERS;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/admin-api/users")
public record AdminUserController(
        UserService userService
) {
    private static final UserMapper userMapper = UserMapper.INSTANCE;

    @GetMapping()
    public ResponseDto getAllUsers() {
        List<UserDto> dtos = userMapper.toDto(userService.getAll(false));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message("All users successfully returned")
                .timeStamp(now())
                .data(Map.of(USERS, dtos))
                .build();
    }

    @PostMapping("/waiters")
    public ResponseDto createWaiter(
            @RequestBody CreateUserRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        UserDto dto = userMapper.toDto(
                userService.createWaiter(
                        userMapper.toCreateUserRequest(requestDto),
                        user
                )
        );

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message("Waiter successfully created")
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }

    @PostMapping("/admins")
    public ResponseDto createAdmin(
            @RequestBody CreateUserRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        UserDto dto = userMapper.toDto(
                userService.createAdmin(
                        userMapper.toCreateUserRequest(requestDto),
                        user
                )
        );

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message("Admin successfully created")
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }

    @GetMapping("/waiters")
    public ResponseDto getAllWaiters() {
        List<UserDto> dto = userMapper.toDto(
                userService.getAll(false, Role.GUIDE)
        );

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message("All waiters successfully returned")
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }

    @GetMapping("/admins")
    public ResponseDto getAllAdmins() {
        List<UserDto> dto = userMapper.toDto(
                userService.getAll(false, Role.ADMIN)
        );

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message("All admins successfully returned")
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }


    @DeleteMapping("/{id}")
    public ResponseDto deleteUserById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        UserDto dto = userMapper.toDto(userService.delete(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("User with id %s successfully deleted", id))
                .timeStamp(now())
                .data(Map.of(ORDER, dto))
                .build();
    }

    @PutMapping("/{id}")
    public ResponseDto updateUserById(
            @PathVariable("id") UUID id,
            @RequestBody UpdateUserRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        UserDto dto = userMapper.toDto(userService.update(
                id,
                userMapper.toUpdateUserRequest(requestDto),
                user
        ));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("User with id %s successfully updated", id))
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }

    @PutMapping("/{id}/activate")
    public ResponseDto activateUserById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        UserDto dto = userMapper.toDto(userService.activate(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("User with id %s successfully activated", id))
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseDto deactivateUserById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        UserDto dto = userMapper.toDto(userService.deactivate(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("User with id %s successfully deactivated", id))
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }

    @PutMapping("/{id}/roles")
    public ResponseDto addRoleByUserId(
            @PathVariable("id") UUID id,
            @RequestParam("role") Role role,
            @AuthenticationPrincipal User user
    ) {
        UserDto dto = userMapper.toDto(userService.addRole(id, role, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Role %s successfully added to User with id %s", role, id))
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }

    @PutMapping("/{id}/permissions")
    public ResponseDto addPermissionByUserId(
            @PathVariable("id") UUID id,
            @RequestParam("permission") Permission permission,
            @AuthenticationPrincipal User user
    ) {
        UserDto dto = userMapper.toDto(userService.addPermission(id, permission, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Permission %s successfully added to User with id %s", permission, id))
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }

    @DeleteMapping("/{id}/permissions")
    public ResponseDto removePermissionByUserId(
            @PathVariable("id") UUID id,
            @RequestParam("permission") Permission permission,
            @AuthenticationPrincipal User user
    ) {
        UserDto dto = userMapper.toDto(userService.removePermission(id, permission, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Permission %s successfully removed from User with id %s", permission, id))
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }

    @DeleteMapping("/{id}/roles")
    public ResponseDto removeRoleByUserId(
            @PathVariable("id") UUID id,
            @RequestParam("role") Role role,
            @AuthenticationPrincipal User user
    ) {
        UserDto dto = userMapper.toDto(userService.removeRole(id, role, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Role %s successfully removed from User with id %s", role, id))
                .timeStamp(now())
                .data(Map.of(USER, dto))
                .build();
    }
}
