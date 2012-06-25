--
-- Copyright 2003 Sakai Foundation Licensed under the
-- Educational Community License, Version 2.0 (the "License"); you may
-- not use this file except in compliance with the License. You may
-- obtain a copy of the License at
--
-- http://www.osedu.org/licenses/ECL-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an "AS IS"
-- BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
-- or implied. See the License for the specific language governing
-- permissions and limitations under the License.
--

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
	EVALUATION_FK bigint not null,
	primary key (ID),
	unique key ASSIGN_USER_MUL_IDX (USER_ID,GROUP_ID,ASSIGN_TYPE,EVALUATION_FK),
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

insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION',false);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'DEFAULT_EMAIL_REMINDER_FREQUENCY',0);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'DISABLE_ITEM_BANK',false);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'DISABLE_QUESTION_BLOCKS',false);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'ENABLE_EVAL_TERM_IDS',false);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'ENABLE_FILTER_ASSIGNABLE_GROUPS',false);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'ENABLE_JOB_COMPLETION_EMAIL',false);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'ENABLE_LIST_OF_TAKERS_EXPORT',true);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'ENABLE_PROVIDER_SYNC',false);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'ENABLE_REMINDER_STATUS',false);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'ENABLE_SINGLE_EMAIL_PER_STUDENT',false);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'EVALUATION_TIME_TO_WAIT_SECS',300);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'SINGLE_EMAIL_REMINDER_DAYS',0);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'SYNC_UNASSIGNED_GROUPS_ON_STARTUP',true);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'SYNC_USER_ASSIGNMENTS_ON_GROUP_SAVE',true);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'SYNC_USER_ASSIGNMENTS_ON_GROUP_UPDATE',false);
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE',true);
