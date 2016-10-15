In this example, an admin and a superuser is expected to exist with respective passwords and app roles.

For example to add superuser directly into postgres run:
create extension if not exists pgcrypto;
insert into users values ('superuser', crypt('superuser', gen_salt('bf')), 'SUPERUSER');
