INSERT INTO public."user" (id, first_name, last_name, email, phone_number, age, password, created_at, active)
VALUES ('011a7637-8bd4-4c7c-ac82-cd3e4d7d3f19', 'kostya', 'yagudin', 'admin', null, null,
        crypt('admin', gen_salt('bf', 8)), now()::timestamptz, true);