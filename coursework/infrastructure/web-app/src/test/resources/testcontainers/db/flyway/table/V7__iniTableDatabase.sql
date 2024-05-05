CREATE TABLE IF NOT EXISTS public.table
(
    id
               UUID
        NOT
            NULL
        DEFAULT
            gen_random_uuid
            (
            ),
    name       VARCHAR(255) UNIQUE NOT NULL,
    capacity   INTEGER,
    table_type VARCHAR(255),
    hall_id    UUID,
    order_id   UUID,
    active     BOOLEAN             NOT NULL DEFAULT true,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_at TIMESTAMP
                   WITHOUT TIME ZONE,
    password   VARCHAR(64)         NOT NULL UNIQUE,
    CONSTRAINT pk_table PRIMARY KEY
        (
         id
            ),
    CONSTRAINT fk_table_on_hall FOREIGN KEY
        (
         hall_id
            ) REFERENCES public.camp
        ON DELETE SET NULL
);