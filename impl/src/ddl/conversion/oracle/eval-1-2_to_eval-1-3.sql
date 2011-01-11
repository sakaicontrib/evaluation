/* Oracle conversion script */
create table EVAL_ASSIGN_USER (
	ID number(19,0) not null,
	EID varchar2(255 char), 
	LAST_MODIFIED timestamp(6) not null,
	OWNER varchar2(255 char) not null,
	USER_ID varchar2(255 char) not null,
	GROUP_ID varchar2(255 char) not null,
	ASSIGN_TYPE varchar2(255 char) not null,
	ASSIGN_STATUS varchar2(255 char) not null,
	LIST_ORDER  number(10,0) not null,
	ASSIGN_GROUP_ID number(19,0) not null,
    EVALUATION_FK NUMBER(19,0) not null,
	primary key (ID)
);

create index ASSIGN_USER_EVALUATION_FKC on EVAL_ASSIGN_USER (EVALUATION_FK);
create unique index ASSIGN_USER_MUL_IDX on EVAL_ASSIGN_USER (USER_ID,GROUP_ID,ASSIGN_TYPE,EVALUATION_FK);

alter table EVAL_ASSIGN_USER  
	add constraint ASSIGN_USER_EVALUATION_FKC 
	foreign key (EVALUATION_FK) 
	references eval_evaluation (ID);

alter table EVAL_ASSIGN_GROUP add ( SELECTION_SETTINGS VARCHAR2(2000 CHAR) );
alter table EVAL_EVALUATION add ( EMAIL_OPEN_NOTIFICATION number(1,0) ); 
alter table EVAL_EVALUATION add ( REMINDER_STATUS varchar2(255) );
alter table EVAL_EVALUATION add ( SELECTION_SETTINGS VARCHAR2(2000 CHAR) );
alter table EVAL_ITEM add ( COMPULSORY number(1,0) );
alter table EVAL_RESPONSE add ( SELECTIONS_CODE clob );
alter table EVAL_TEMPLATEITEM add ( COMPULSORY number(1,0) );

