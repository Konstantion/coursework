CREATE TABLE public.equipment_gear
(
    equipment_id UUID NOT NULL,
    gear_id      UUID NOT NULL,
    CONSTRAINT fk_equipment_gear_on_equipment FOREIGN KEY (equipment_id) REFERENCES public.equipment ON DELETE CASCADE,
    CONSTRAINT fk_equipment_gear_on_gear FOREIGN KEY (gear_id) REFERENCES public.gear ON DELETE CASCADE
);