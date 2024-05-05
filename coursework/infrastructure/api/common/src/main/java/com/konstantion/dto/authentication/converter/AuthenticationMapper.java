package com.konstantion.dto.authentication.converter;

import com.konstantion.authentication.model.AuthenticationResponse;
import com.konstantion.dto.authentication.dto.AuthenticationResponseDto;
import com.konstantion.dto.table.dto.TableDto;
import com.konstantion.dto.user.dto.UserDto;
import com.konstantion.expedition.Expedition;
import com.konstantion.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Map;

import static com.konstantion.utils.EntityNameConstants.ENTITY;
import static com.konstantion.utils.EntityNameConstants.TABLE;
import static com.konstantion.utils.EntityNameConstants.USER;

@Mapper
public interface AuthenticationMapper {
    AuthenticationMapper INSTANCE = Mappers.getMapper(AuthenticationMapper.class);

    default AuthenticationResponseDto toDto(AuthenticationResponse authenticationResponse) {
        if (authenticationResponse.type().equals(USER)
                && authenticationResponse.userDetails() instanceof User user) {

            return new AuthenticationResponseDto<>(
                    authenticationResponse.token(),
                    Map.of(authenticationResponse.type(), new UserDto(
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getUsername(),
                            user.getPhoneNumber(),
                            user.getAge(),
                            user.getActive(),
                            user.getRoles(),
                            user.getCreatedAt(),
                            user.getPermissions()
                    )));
        } else if (authenticationResponse.type().equals(TABLE)
                && authenticationResponse.userDetails() instanceof Expedition table) {
            return new AuthenticationResponseDto<>(
                    authenticationResponse.token(),
                    Map.of(authenticationResponse.type(),
                            new TableDto(
                                    table.getId(),
                                    table.getName(),
                                    table.getCapacity(),
                                    table.getExpeditionType(),
                                    table.getCampId(),
                                    table.getEquipmentId(),
                                    table.getGuidesId(),
                                    table.getCreatedAt(),
                                    table.getDeletedAt(),
                                    table.getActive()
                            ))
            );
        } else {
            return new AuthenticationResponseDto<>(
                    authenticationResponse.token(),
                    Map.of(ENTITY, authenticationResponse.userDetails())
            );
        }
    }
}
