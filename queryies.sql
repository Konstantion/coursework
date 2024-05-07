SELECT * FROM public."user";

SELECT * FROM public.gear WHERE price BETWEEN 50 AND 100;

SELECT * FROM public.expedition
WHERE name IN ('Adventure Camp', 'Discovery Expedition');

SELECT * FROM public.guest
WHERE name LIKE 'A%';

SELECT * FROM public.equipment
WHERE active = true AND expedition_id IS NOT NULL;

SELECT * FROM public.expedition
WHERE capacity > 50 OR active = true;

SELECT DISTINCT role FROM public.user_role;

SELECT MIN(price) FROM public.gear;

SELECT AVG(execution_time) FROM public.flyway_schema_history;

SELECT COUNT(*) FROM public.camp WHERE active = true;

SELECT expedition_id, COUNT(*) as total_calls
FROM public.call
GROUP BY expedition_id;

SELECT expedition_id, COUNT(*) as total_gear
FROM public.equipment_gear
WHERE gear_id IS NOT NULL
GROUP BY expedition_id;

SELECT expedition_id, COUNT(*) as total_guides FROM public.expedition_guide GROUP BY expedition_id HAVING COUNT(*) > 3;

SELECT expedition_id, COUNT(*) as total_guides
FROM public.expedition_guide
WHERE expedition_id IS NOT NULL
GROUP BY expedition_id
HAVING COUNT(*) > 1
ORDER BY total_guides DESC;

SELECT e.name, c.name as camp_name
FROM public.expedition e
INNER JOIN public.camp c ON e.camp_id = c.id;

SELECT e.name, g.name as guest_name
FROM public.expedition e
LEFT JOIN public.log l ON e.equipment_id = l.equipment_id
LEFT JOIN public.guest g ON l.guest_id = g.id;

SELECT g.name, l.id as log_id
FROM public.guest g
RIGHT JOIN public.log l ON g.id = l.guest_id;

SELECT e.name, g.name as guide_name
FROM public.expedition e
INNER JOIN public.expedition_guide eg ON e.id = eg.expedition_id
INNER JOIN public."user" g ON eg.guide_id = g.id
WHERE e.active = true;

SELECT e.name, g.name as guide_name
FROM public.expedition e
INNER JOIN public.expedition_guide eg ON e.id = eg.expedition_id
INNER JOIN public."user" g ON eg.guide_id = g.id
WHERE g.name LIKE 'J%';

SELECT e.id as expedition_id, COUNT(eg.guide_id) as guide_count
FROM public.expedition e
INNER JOIN public.expedition_guide eg ON e.id = eg.expedition_id
GROUP BY e.id;

SELECT e.id as expedition_id, COUNT(eg.guide_id) as guide_count FROM public.expedition e INNER JOIN public.expedition_guide eg ON e.id = eg.expedition_id GROUP BY e.id HAVING COUNT(eg.guide_id) > 2;

SELECT * FROM public.gear
WHERE price > (SELECT AVG(price) FROM public.gear);

SELECT * FROM public.expedition
WHERE id IN (
  SELECT expedition_id FROM public.expedition_guide
  GROUP BY expedition_id
  HAVING COUNT(guide_id) > 2
);

SELECT * FROM public.expedition
WHERE EXISTS (
  SELECT 1 FROM public.expedition_guide eg
  WHERE eg.expedition_id = public.expedition.id
);

SELECT * FROM public.expedition WHERE capacity > ANY ( SELECT capacity FROM public.expedition WHERE active = true);

SELECT * FROM public."user"
WHERE id IN (
  SELECT user_id FROM public.user_role WHERE role = 'admin'
);

SELECT u.id, u.first_name, ur.role
FROM public."user" u
INNER JOIN (
  SELECT user_id, role FROM public.user_role WHERE role = 'admin'
) ur ON u.id = ur.user_id;
