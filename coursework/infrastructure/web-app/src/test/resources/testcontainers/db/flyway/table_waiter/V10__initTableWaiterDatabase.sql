CREATE TABLE IF NOT EXISTS table_waiter
(
    table_id
        UUID
        NOT
            NULL,
    waiter_id
        UUID
        NOT
            NULL,
    CONSTRAINT
        fk_table_waiter_on_table
        FOREIGN
            KEY
            (
             table_id
                ) REFERENCES public.table ON DELETE CASCADE,
    CONSTRAINT fk_table_waiter_on_user FOREIGN KEY
        (
         waiter_id
            ) REFERENCES public.user
        ON DELETE CASCADE
);


