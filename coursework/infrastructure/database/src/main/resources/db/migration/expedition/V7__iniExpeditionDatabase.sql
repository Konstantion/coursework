CREATE TABLE IF NOT EXISTS public.expedition
(
    id
                    UUID
                                        NOT
                                            NULL
                                                 DEFAULT
                                                     gen_random_uuid
                                                     (
                                                     ),
    name            VARCHAR(255) UNIQUE NOT NULL,
    capacity        INTEGER,
    expedition_type VARCHAR(255),
    camp_id         UUID,
    equipment_id    UUID,
    active          BOOLEAN             NOT NULL DEFAULT true,
    created_at      TIMESTAMP WITHOUT TIME ZONE,
    deleted_at      TIMESTAMP
                        WITHOUT TIME ZONE,
    password        VARCHAR(64)         NOT NULL UNIQUE,
    CONSTRAINT pk_expedition PRIMARY KEY
        (
         id
            ),
    CONSTRAINT fk_expedition_on_camp FOREIGN KEY
        (
         camp_id
            ) REFERENCES public.camp
        ON DELETE SET NULL
);