ALTER TABLE public.equipment
    ADD CONSTRAINT fk_equipment_on_log FOREIGN KEY (log_id) REFERENCES public.log ON DELETE SET NULL;