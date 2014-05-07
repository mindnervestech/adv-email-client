# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table content (
  id                        bigint auto_increment not null,
  description               longtext,
  constraint pk_content primary key (id))
;

create table image_info (
  id                        bigint auto_increment not null,
  mail_object_model_id      bigint,
  url                       longtext,
  image_byte                longblob,
  alt                       longtext,
  constraint pk_image_info primary key (id))
;

create table links (
  id                        bigint auto_increment not null,
  url                       longtext,
  mail_id_id                bigint,
  status                    tinyint(1) default 0,
  htmlcontent               longtext,
  path                      varchar(255),
  constraint pk_links primary key (id))
;

create table mail_object_model (
  id                        bigint auto_increment not null,
  mail_name                 varchar(255),
  mail_path                 varchar(255),
  status                    tinyint(1) default 0,
  domain                    varchar(255),
  senders_email             varchar(255),
  sent_date                 datetime,
  received_date             datetime,
  content_id                bigint,
  constraint pk_mail_object_model primary key (id))
;

create table save_search_set (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  query_sql                 varchar(255),
  constraint uq_save_search_set_name unique (name),
  constraint pk_save_search_set primary key (id))
;

alter table image_info add constraint fk_image_info_mailObjectModel_1 foreign key (mail_object_model_id) references mail_object_model (id) on delete restrict on update restrict;
create index ix_image_info_mailObjectModel_1 on image_info (mail_object_model_id);
alter table links add constraint fk_links_mail_id_2 foreign key (mail_id_id) references mail_object_model (id) on delete restrict on update restrict;
create index ix_links_mail_id_2 on links (mail_id_id);
alter table mail_object_model add constraint fk_mail_object_model_content_3 foreign key (content_id) references content (id) on delete restrict on update restrict;
create index ix_mail_object_model_content_3 on mail_object_model (content_id);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table content;

drop table image_info;

drop table links;

drop table mail_object_model;

drop table save_search_set;

SET FOREIGN_KEY_CHECKS=1;

