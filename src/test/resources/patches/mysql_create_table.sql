CREATE TABLE IF NOT EXISTS news
(
    id            BIGINT                not null
    primary key,
    title         varchar(512)          not null
    unique,
    text          varchar(512)          not null,
    logo          varchar(512)          not null,
    slug          varchar(512)          not null,
    is_published  boolean default false not null,
    created_at    timestamp             not null,
    updated_at    timestamp,
    published_at  timestamp
    );;