CREATE TABLE IF NOT EXISTS call_waiter
(
    call_id
        UUID
        NOT
            NULL,
    waiter_id
        UUID
        NOT
            NULL,
    CONSTRAINT
        fk_call_waiter_on_call
        FOREIGN
            KEY
            (
             call_id
                ) REFERENCES public.call ON DELETE CASCADE,
    CONSTRAINT fk_call_waiter_on_user FOREIGN KEY
        (
         waiter_id
            ) REFERENCES public.user
        ON DELETE CASCADE
);