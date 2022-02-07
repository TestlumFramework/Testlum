CREATE TABLE IF NOT EXISTS test_db.t_user
(
    id             Int256,
    username       String,
    email          String,
    email_verified UInt8      DEFAULT FALSE,
    first_name     String,
    last_name      String,
    password       String,
    is_active      UInt8      DEFAULT FALSE,
    last_login     DateTime64,
    locked         UInt8      DEFAULT FALSE,
    locked_cause   String,
    created_at     DateTime64 DEFAULT now(),
    created_by     Int256,
    deleted_at     DateTime64,
    deleted_by     Int256) ENGINE = Memory;;
INSERT INTO test_db.t_user (*)
VALUES (1, 'testuser1', 'email1@gmail.com', 1, 'testuser1', 'testuser1',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        1, null, 0, null, '2021-09-29 17:06:16.000000', null, null, null);;

INSERT INTO test_db.t_user (*)
VALUES (2, 'testuser2', 'email2@knubisoft.uk', 1, 'testuser2', 'testuser2',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        1, null, 0, null, '2021-09-29 17:06:16.000000', null, null, null);;

INSERT INTO test_db.t_user (*)
VALUES (3, 'testuser3', 'email3@gmail.com', 1, 'testuser3', 'testuser3',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        1, null, 0, null, '2021-09-29 18:06:16.000000', null, null, null);;

INSERT INTO test_db.t_user (*)
VALUES (4, 'testuser4', 'email4@gmail.com', 1, 'testuser4', 'testuser4',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        1, null, 0, null, '2021-09-29 18:06:16.000000', null, null, null);;

INSERT INTO test_db.t_user (*)
VALUES (5, 'testuser5', 'email5@gmail.com', 1, 'testuser5', 'testuser5',
        '$2a$10$neQFs2lytkzy12dAj./dS.gWbzEKRzaEpgfAbb2Z1SeIMWF4WYCou',
        1, null, 0, null, '2021-09-29 18:06:16.000000', null, null, null);;