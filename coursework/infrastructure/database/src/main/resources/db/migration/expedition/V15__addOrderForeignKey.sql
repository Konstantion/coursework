ALTER TABLE public.expedition
    ADD CONSTRAINT fk_expedition_on_equipment FOREIGN KEY (equipment_id) REFERENCES public.equipment ON DELETE SET NULL;