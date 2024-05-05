CREATE TABLE IF NOT EXISTS public.user_role
(
    user_id
        UUID
        NOT
            NULL,
    role
        VARCHAR(255),
    CONSTRAINT fk_user_role_on_user FOREIGN KEY
        (
         user_id
            ) REFERENCES public.user ON DELETE CASCADE
);