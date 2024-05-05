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
    table_id  UUID,
    user_id   UUID,
    bill_id   UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    closed_at TIMESTAMP
                  WITHOUT TIME ZONE,
    active    BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT pk_order PRIMARY KEY
        (
         id
            ),
    CONSTRAINT fk_order_on_table FOREIGN KEY
        (
         table_id
            ) REFERENCES public.table
        ON DELETE SET NULL,
    CONSTRAINT fk_order_on_user FOREIGN KEY
        (
         user_id
            ) REFERENCES public.user
        ON DELETE SET NULL
);