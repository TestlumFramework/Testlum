INSERT INTO t_temp_user (id, public_id, email, email_verified, first_name, last_name, password, salt,
                    phone_number, phone_number_verified, photo_profile, is_active, last_login,
                    locked, locked_cause, terms_and_conditions_version_id, created_at, created_by,
                    deleted_at, deleted_by)
VALUES (1, '7a53b6f1-aaa4-422c-9526-f160e7c7f4f4', 'vadmorev99@gmail.com', true,
        'User has registered', 'Email and Phone has verified', '$2a$10$PSVKbbXYPN5Qo0QQaYsFNupTQDZegIIGNXuoxYb7uHbRQ85gpscQ6',
        '', '+30331234567', true, null, false, current_date + 1, false, null, 1,
        '2021-09-29 18:06:16.000000', null, null, null);;

INSERT INTO  t_temp_user_to_permission (id, user_id, permission_id)
VALUES (1,1,1);;

INSERT INTO t_temp_user_to_role (id, user_id, role_id)
VALUES (1,1,1);;
