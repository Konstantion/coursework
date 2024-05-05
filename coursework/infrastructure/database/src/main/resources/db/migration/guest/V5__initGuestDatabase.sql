CREATE TABLE IF NOT EXISTS guest
(
    id
                 UUID
                                     NOT
                                         NULL
                                              DEFAULT
                                                  gen_random_uuid
                                                  (
                                                  ),
    name         VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(255),
    discount_percent DOUBLE PRECISION,
    created_at   TIMESTAMP WITHOUT TIME ZONE,
    active       BOOLEAN             NOT NULL DEFAULT true,
    CONSTRAINT pk_guest PRIMARY KEY
        (
         id
            )
);