INSERT INTO t_user (id, username, email, email_verified, first_name, last_name, salt, password,
                    is_active, last_login, locked, locked_cause, created_at, created_by,deleted_at, deleted_by)
VALUES (1, 'testuser1', 'email1@gmail.com', true, 'testuser1', 'testuser1','',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        true, current_date + 1, false, null, '2021-09-29 18:06:16.000000', null, null, null),

 (2, 'testuser2', 'email2@knubisoft.uk', true, 'testuser2', 'testuser2','',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        true, current_date + 1, false, null, '2021-09-29 17:06:16.000000', null, null, null),

 (3, 'testuser3', 'email3@gmail.com', true, 'testuser3', 'testuser3','',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        true, current_date + 1, false, null, '2021-09-29 18:06:16.000000', null, null, null),

 (4, 'testuser4', 'email4@gmail.com', true, 'testuser4', 'testuser4','',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        true, current_date + 1, false, null, '2021-09-29 18:06:16.000000', null, null, null),

 (5, 'testuser5', 'email5@gmail.com', true, 'testuser5', 'testuser5','',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        true, current_date + 1, false, null, '2021-09-29 18:06:16.000000', null, null, null);;