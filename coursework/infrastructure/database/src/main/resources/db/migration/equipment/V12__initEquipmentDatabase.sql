CREATE TABLE IF NOT EXISTS public.equipment
(
    id
                  UUID
                          NOT
                              NULL
                                   DEFAULT
                                       gen_random_uuid
                                       (
                                       ),
    expedition_id UUID,
    user_id       UUID,
    log_id        UUID,
    created_at    TIMESTAMP WITHOUT TIME ZONE,
    closed_at     TIMESTAMP
                      WITHOUT TIME ZONE,
    active        BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT pk_equipment PRIMARY KEY
        (
         id
            ),
    CONSTRAINT fk_equipment_on_expedition FOREIGN KEY
        (
         expedition_id
            ) REFERENCES public.expedition
        ON DELETE SET NULL,
    CONSTRAINT fk_order_on_user FOREIGN KEY
        (
         user_id
            ) REFERENCES public.user
        ON DELETE SET NULL
);