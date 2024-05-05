CREATE TABLE IF NOT EXISTS public."user"
(
    id
             UUID
                          NOT
                              NULL
                                   DEFAULT
                                       gen_random_uuid
                                       (
                                       ),
    first_name   VARCHAR(255),
    last_name    VARCHAR(255),
    email        VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(255),
    age          INTEGER,
    password VARCHAR(255) NOT NULL,
    active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_user PRIMARY KEY
        (
         id
            )
);
