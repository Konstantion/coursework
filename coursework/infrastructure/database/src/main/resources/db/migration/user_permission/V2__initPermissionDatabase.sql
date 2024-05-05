CREATE TABLE IF NOT EXISTS public.user_permission
(
    user_id
        UUID
        NOT
            NULL,
    permission
        VARCHAR(255),
    CONSTRAINT fk_user_permission_on_user FOREIGN KEY
        (
         user_id
            ) REFERENCES public.user ON DELETE CASCADE
);

CREATE INDEX idx_user_permission_user_id ON public.user_permission USING BTREE (user_id);

