create table if not exists "table"(
                                      id serial primary key,
                                      number int not null check (number > 0)
);

alter table "Order"
    add column id_table int;

alter table "Order"
    add constraint fk_order_table
        foreign key (id_table) references "table"(id);

alter table "Order" add column installation_time timestamp default now();
alter table "Order" add column departure_time timestamp default now();

create table if not exists tableorder (
                                          id serial primary key,
                                          id_order int not null references "Order"(id) on delete cascade,
                                          id_table int not null references "table"(id) on delete cascade
);

