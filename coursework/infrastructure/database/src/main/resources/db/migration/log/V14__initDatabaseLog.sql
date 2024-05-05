CREATE TABLE IF NOT EXISTS public.log
(
    id
                        UUID
                                         NOT
                                             NULL
                                                  DEFAULT
                                                      gen_random_uuid
                                                      (
                                                      ),
    guide_id            UUID,
    equipment_id        UUID             NOT NULL UNIQUE,
    guest_id            UUID,
    created_at          TIMESTAMP WITHOUT TIME ZONE,
    closed_at           TIMESTAMP
                            WITHOUT TIME ZONE,
    active              BOOLEAN          NOT NULL DEFAULT true,
    price               DOUBLE PRECISION NOT NULL,
    price_with_discount DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_log PRIMARY KEY
        (
         id
            ),
    CONSTRAINT fk_log_equipment FOREIGN KEY
        (
         equipment_id
            ) REFERENCES public.equipment
        ON DELETE CASCADE,
    CONSTRAINT fk_log_user FOREIGN KEY
        (
         guide_id
            ) REFERENCES public."user"
        ON DELETE SET NULL,
    CONSTRAINT fk_log_guest FOREIGN KEY
        (
         guest_id
            ) REFERENCES public."guest"
        ON DELETE SET NULL
);