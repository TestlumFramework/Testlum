INSERT INTO t_authority_group(id, name)
VALUES (1, 'base_group'),
       (2, 'no_rights_group');

INSERT INTO t_authority (id, name, group_id, description, enabled)
VALUES (1, 'BASE_RIGHTS', 1, 'BASE_RIGHTS', true),
       (2, 'NO_RIGHTS', 2, 'NO_RIGHTS', true);

INSERT INTO t_role (id, name, description, enabled)
values (1, 'OWNER', 'Owner role', true),
       (2, 'ADMIN', 'Admin role', true),
       (3, 'LAWYER', 'Lawyer role', true),
       (4, 'ASSOCIATE', 'Associate role', true),
       (5, 'CLIENT', 'Client role', true);

INSERT INTO t_role_to_authority(id, role_id, authority_id)
VALUES (1, 1, 1),
       (2, 2, 1),
       (3, 3, 1),
       (4, 4, 1);

SELECT setval('t_authority_group_id_seq', (SELECT MAX(id) FROM t_authority_group));
SELECT setval('t_authority_id_seq', (SELECT MAX(id) FROM t_authority));
SELECT setval('t_role_id_seq', (SELECT MAX(id) FROM t_role));
SELECT setval('t_role_to_authority_id_seq', (SELECT MAX(id) FROM t_role_to_authority));

INSERT INTO t_user (id, username, email, email_verified, first_name, last_name, password,
                    is_active, last_login, locked, locked_cause, created_at, created_by,deleted_at, deleted_by)
VALUES (100, 'username1', 'email@gmail.com', true, 'John', 'Doe',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        true, current_date + 1, false, null, '2021-09-29 18:06:16.000000', null, null, null);

INSERT INTO t_user (id, username, email, email_verified, first_name, last_name, password,
                    is_active, last_login, locked, locked_cause, created_at, created_by,deleted_at, deleted_by)
VALUES (200, 'username2', 'email@knubisoft.uk', true, 'testName', 'testLastName',
        '$2a$10$RRmNUIBwrniphqRsqIM/GubnuhJ8v4e9YCpCg3BLqXG9Ae02zlWwm',
        true, current_date + 1, false, null, '2021-09-29 17:06:16.000000', null, null, null);

INSERT INTO  t_user_to_authority (id, user_id, authority_id)
VALUES (1, 100, 1),
       (2, 200, 2);

INSERT INTO t_user_to_role (id, user_id, role_id)
VALUES (1, 100, 1),
       (2, 200, 4);