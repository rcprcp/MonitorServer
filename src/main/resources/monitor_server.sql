create database if not exists monitordb;

use monitordb;
drop table if exists start_events;
drop table if exists methods;
drop table if exists start_events;
drop table if exists elapsed_events;
drop table if exists method_mapping;
drop table if exists config;
drop table if exists app_name_mapping;
drop table if exists host_name_mapping;

create table app_name_mapping (
  app_name   varchar(500),
  app_number integer auto_increment primary key
);
create unique index app_name_mapping_ix1
  on app_name_mapping (app_name);

create table host_name_mapping (
  host_name   varchar(500),
  host_number integer auto_increment primary key
);
create unique index host_name_mapping_ix1
  on host_name_mapping (host_name);

-- start_events represents one "run" of an app on a given host, starting at a time.
create table app_start_events (
  app_number     integer,
  foreign key (app_number) references app_name_mapping (app_number)
    on delete cascade,
  app_start_time  bigint       not null,
  host_number     bigint       not null
);
-- all, chronologically.
create unique index app_start_events_ix1
  on app_start_events (app_start_time, app_number);
--specific app, by time descending
create unique index app_start_events_ix2
  on app_start_events (app_number, app_start_time desc);
-- by host, by time desc, then app name.
create unique index app_start_events_ix3
  on app_start_events (host_number, app_start_time desc, app_number);

-- map the method name to a number for faster access (and save a bit of space)
create table method_name_mapping (
  method_number bigint not null auto_increment primary key,
  method_name   varchar(500)
);

create unique index methods_ix1
  on method_name_mapping (method_name, method_number);

-- save the application's configuration data in this table;
create table config (
  app_number     integer,
  app_start_time bigint,
  foreign key (app_number) references app_name_mapping (app_number)
    on delete cascade,
  property_key   varchar(100),
  property_value varchar(1000)
);

create unique index config_ix1
  on config (app_number, app_start_time, property_key);

-- each instrumented routine sends an elapsed_event evey time it's invoked.
create table elapsed_events (
  app_number            bigint not null,
  app_start_time        bigint not null,
  foreign key (app_start_time) references app_start_events (app_start_time)
    on delete cascade,
  thread_id             biging not null,
  elapsed_interval_ns   bigint not null,
  method_number         bigint not null,
  method_start_sequence bigint not null,
  method_end_sequence   bigint not null,

  primary key (app_start_time, app_number, method_number, elapsed_interval_ns)
);
