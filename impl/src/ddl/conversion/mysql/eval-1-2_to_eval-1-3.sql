/* MySQL conversion script */
create table EVAL_ASSIGN_USER (
	ID bigint not null auto_increment,
	EID varchar(255), 
	LAST_MODIFIED datetime not null,
	OWNER varchar(255) not null,
	USER_ID varchar(255) not null,
	GROUP_ID varchar(255) not null,
	ASSIGN_TYPE varchar(255) not null,
	ASSIGN_STATUS varchar(255) not null,
	LIST_ORDER  integer not null,
	ASSIGN_GROUP_ID bigint not null,
	primary key (ID),
	unique key USER_ID (USER_ID,GROUP_ID,ASSIGN_TYPE,EVALUATION_FK),
  	key ASSIGN_USER_EVALUATION_FKC (EVALUATION_FK),
  	constraint ASSIGN_USER_EVALUATION_FKC foreign key (EVALUATION_FK) references eval_evaluation (ID)
);

alter table EVAL_ASSIGN_GROUP add column SELECTION_SETTINGS text;
alter table EVAL_EVALUATION add column EMAIL_OPEN_NOTIFICATION bit(1); 
alter table EVAL_EVALUATION add column REMINDER_STATUS varchar(255);
alter table EVAL_EVALUATION add column SELECTION_SETTINGS text;
alter table EVAL_ITEM add column COMPULSORY bit(1);
alter table EVAL_RESPONSE add column SELECTIONS_CODE text;
alter table EVAL_TEMPLATEITEM add column COMPULSORY bit(1);

