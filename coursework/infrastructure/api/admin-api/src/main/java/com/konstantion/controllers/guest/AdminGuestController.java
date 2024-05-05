package com.konstantion.controllers.guest;

import com.konstantion.dto.guest.converter.GuestMapper;
import com.konstantion.dto.guest.dto.CreateGuestRequestDto;
import com.konstantion.dto.guest.dto.GuestDto;
import com.konstantion.dto.guest.dto.UpdateGuestRequestDto;
import com.konstantion.guest.GuestService;
import com.konstantion.response.ResponseDto;
import com.konstantion.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.konstantion.utils.EntityNameConstants.GUEST;
import static com.konstantion.utils.EntityNameConstants.GUESTS;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/admin-api/guests")
public record AdminGuestController(
        GuestService guestService
) {
    private static final GuestMapper guestMapper = GuestMapper.INSTANCE;

    @GetMapping()
    public ResponseDto getAllGuests() {
        List<GuestDto> dtos = guestMapper.toDto(guestService.getAll(false));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message("All guests")
                .data(Map.of(GUESTS, dtos))
                .build();
    }

    @PostMapping()
    public ResponseDto createGuest(
            @RequestBody CreateGuestRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        GuestDto dto = guestMapper.toDto(guestService.create(
                guestMapper.toCreateGuestRequest(requestDto),
                user
        ));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message("Guest successfully created")
                .data(Map.of(GUEST, dto))
                .build();
    }


    @PutMapping("/{id}")
    public ResponseDto updateGuestById(
            @PathVariable("id") UUID id,
            @RequestBody UpdateGuestRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        GuestDto dto = guestMapper.toDto(guestService.update(
                id,
                guestMapper.toUpdateGuestRequest(requestDto),
                user
        ));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message(format("Guest with id %s successfully edited", id))
                .data(Map.of(GUEST, dto))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseDto deleteGuestById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        GuestDto dto = guestMapper.toDto(guestService.delete(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message(format("Guest with id %s successfully deleted", id))
                .data(Map.of(GUEST, dto))
                .build();
    }

    @PutMapping("/{id}/activate")
    public ResponseDto activateGuestById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        GuestDto dto = guestMapper.toDto(guestService.activate(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message(format("Guest with id %s successfully activated", id))
                .data(Map.of(GUEST, dto))
                .build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseDto deactivateGuestById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        GuestDto dto = guestMapper.toDto(guestService.deactivate(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message(format("Guest with id %s successfully deactivated", id))
                .data(Map.of(GUEST, dto))
                .build();
    }
}
