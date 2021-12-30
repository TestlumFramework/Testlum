CREATE TABLE IF NOT EXISTS t_user
(
    id                              bigserial                   not null
    constraint t_user_pkey
    primary key,
    email                           varchar(512)                not null
    constraint idx_email_unq
    unique,
    email_verified                  boolean   default false     not null,
    first_name                      varchar(512)                not null,
    last_name                      varchar(512)                not null,
    username                      varchar(512)                not null,
    password                        varchar(512)                not null,
    salt                            varchar(512)                not null,
    phone_number                    varchar(64)
    constraint idx_phone_number_unq
    unique,
    phone_number_verified           boolean   default false     not null,
    photo_profile                   varchar(1024),
    is_active                       boolean   default false     not null,
    last_login                      timestamp,
    locked                          boolean   default false     not null,
    locked_cause                    text,
    created_at                      timestamp                   not null,
    created_by                      bigint,
    deleted_at                      timestamp,
    deleted_by                      bigint
    );