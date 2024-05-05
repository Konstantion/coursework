package com.konstantion.expedition.model;

import java.util.UUID;

public record TableWaitersRequest(
        UUID waiterId
) {
}
