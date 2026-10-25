create table stories (
  id serial primary key,
  title text not null,
  status text not null default 'todo' check (status in ('todo', 'doing', 'done')),
  points int not null default 1 check (points between 1 and 13),
  assignee text
);

insert into stories (title, status, points, assignee) values
  ('Login page', 'done', 3, 'Ana'),
  ('Password reset email', 'doing', 5, 'Ben'),
  ('Dashboard totals', 'todo', 8, null),
  ('Export to CSV', 'todo', 2, 'Ana'),
  ('Dark mode', 'todo', 1, null);
