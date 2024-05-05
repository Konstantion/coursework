CREATE TABLE IF NOT EXISTS public.call_guide
(
    call_id
        UUID
        NOT
            NULL,
    guide_id
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
    CONSTRAINT fk_call_guide_on_user FOREIGN KEY
        (
         guide_id
            ) REFERENCES public.user
        ON DELETE CASCADE
);