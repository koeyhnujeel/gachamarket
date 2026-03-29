create table members (
    id uuid primary key,
    email varchar(255) not null unique,
    nickname varchar(32) not null unique,
    active_title_id uuid null,
    nickname_change_free_used boolean not null default false,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table wallets (
    member_id uuid primary key references members(id),
    current_point int not null,
    updated_at timestamp not null
);

create table categories (
    id uuid primary key,
    parent_id uuid null references categories(id),
    slug varchar(64) not null unique,
    name varchar(64) not null,
    depth int not null,
    sort_order int not null,
    visible boolean not null
);

insert into categories (id, parent_id, slug, name, depth, sort_order, visible) values
('00000000-0000-0000-0000-000000000001', null, 'sports', '스포츠', 0, 0, true),
('00000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000001', 'football', '축구', 1, 0, true),
('00000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000001', 'baseball', '야구', 1, 1, true),
('00000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000001', 'basketball', '농구', 1, 2, true),
('00000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000011', 'football-epl', 'EPL', 2, 0, true),
('00000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000012', 'baseball-kbo', 'KBO', 2, 0, true),
('00000000-0000-0000-0000-000000000023', '00000000-0000-0000-0000-000000000013', 'basketball-nba', 'NBA', 2, 0, true);
