INSERT INTO t_authority_group (name)
VALUES ('base_group');;

INSERT INTO t_authority (name, group_id, description, enabled)
VALUES ('BASE_RIGHTS',
        (SELECT id FROM t_authority_group WHERE name = 'base_group'),'BASE_RIGHTS description',true);;

INSERT INTO t_role (name, description, enabled)
VALUES ('OWNER', 'Owner role description', true),
       ('ADMIN', 'Owner role description', true),
       ('LAWYER', 'Owner role description', true),
       ('ASSOCIATE', 'Owner role description', true),
       ('CLIENT', 'Owner role description', true);;

INSERT INTO t_role_to_authority (role_id, authority_id)
VALUES ((SELECT id FROM t_role WHERE name = 'OWNER'),
        (SELECT id FROM t_authority WHERE name = 'BASE_RIGHTS')),
       ((SELECT id FROM t_role WHERE name = 'ADMIN'),
        (SELECT id FROM t_authority WHERE name = 'BASE_RIGHTS')),
       ((SELECT id FROM t_role WHERE name = 'LAWYER'),
        (SELECT id FROM t_authority WHERE name = 'BASE_RIGHTS')),
       ((SELECT id FROM t_role WHERE name = 'ASSOCIATE'),
        (SELECT id FROM t_authority WHERE name = 'BASE_RIGHTS')),
       ((SELECT id FROM t_role WHERE name = 'CLIENT'),
        (SELECT id FROM t_authority WHERE name = 'BASE_RIGHTS'));;

INSERT INTO t_public_html_page (id, name, type, version, html, enabled, created_at)
VALUES (1, 'Privacy Policy Version 1 - active', 'privacy_policy', 1, '<!DOCTYPE html>
<html>
<body>
<h1>Privacy policy VERSION = 1 active</h1>
</body>
</html>', true, '2021-09-10 07:03:46.203215'),
       (2, 'Privacy Policy Version 2 - active', 'privacy_policy', 2, '<!DOCTYPE html>
<html>
<body>
<h1>Privacy policy VERSION = 2 active</h1>
</body>
</html>', true, '2021-09-10 07:03:46.203215'),
       (3, 'Privacy Policy Version 3 - disabled', 'privacy_policy', 3, '<!DOCTYPE html>
<html>
<body>
<h1>Privacy policy VERSION = 3 disabled</h1>
</body>
</html>', false, '2021-09-10 07:03:46.203215'),
       (4, 'Cookie Policy 1 - active', 'cookie_policy', 1, '<!DOCTYPE html>
<html>
<body>
<h1>Cookie policy VERSION = 1 active</h1>
</body>
</html>', true, '2021-09-10 07:03:46.203215'),
       (5, 'Cookie Policy 2 - disabled', 'cookie_policy', 2, '<!DOCTYPE html>
<html>
<body>
<h1>Cookie policy VERSION = 2 disabled</h1>
</body>
</html>', false, '2021-09-10 07:03:46.203215'),
       (6, 'Terms and conditions 1 - active', 'terms_and_conditions', 1, '<!DOCTYPE html>
<html>
<body>
<h1>Terms and conditions VERSION = 1 active</h1>
</body>
</html>', true, '2021-09-10 07:03:46.203215'),
       (7, 'Terms and conditions - disabled', 'terms_and_conditions', 2, '<!DOCTYPE html>
<html>
<body>
<h1>Terms and conditions VERSION = 2 disabled</h1>
</body>
</html>', false, '2021-09-10 07:03:46.203215'),
       (8, 'Terms and conditions 3 - disabled', 'terms_and_conditions', 3, '<!DOCTYPE html>
<html>
<body>
<h1>Terms and conditions VERSION = 3 disabled</h1>
</body>
</html>', false, '2021-09-10 07:03:46.203215'),
       (9, 'Terms and conditions 400 - active', 'terms_and_conditions', 400, '<!DOCTYPE html>
<html>
<body>
<h1>Terms and conditions VERSION = 400 active</h1>
</body>
</html>', false, '2021-09-10 16:18:23.473648');

INSERT INTO t_email_template (name, title, html, created_at)
VALUES ('EMAIL_VERIFICATION', 'Email verification', '<p>NickName: ${email}</p><br><p>Email: ${token}</p><br><p>Subject: ${url}</p> <br>', current_date + 1),
       ('PASSWORD_RECOVERY', 'Password recovery', '<p>NickName: ${email}</p><br><p>Email: ${token}</p><br><p>Subject: ${url}</p><br>', current_date + 1),
       ('SUPPORT_PAGE', 'Support email','<p>NickName: ${nickName}</p><br><p>Email: ${email}</p><br><p>Subject: ${subject}</p><br>', current_date + 1),
       ('LOGIN_2FA', 'Login verification PME','<h1>Code for login ${email}</h1><h2>${code}</h2>', now());
