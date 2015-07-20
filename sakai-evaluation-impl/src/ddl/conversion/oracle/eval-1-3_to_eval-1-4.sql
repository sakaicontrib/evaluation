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

-- Oracle conversion script - 1.3 to 1.4 
alter table EVAL_EVALUATION add (LOCAL_SELECTOR varchar2(255 char));

update eval_email_template set template_type='ConsolidatedAvailable' where template_type='SingleEmailAvailable';
update eval_email_template set template_type='ConsolidatedReminder' where template_type='SingleEmailReminder';

create table EVAL_EMAIL_PROCESSING_QUEUE 
(
	ID number(19,0) not null, 
	EAU_ID number(19,0),  
	USER_ID varchar2(255), 
	GROUP_ID varchar2(255),
	EMAIL_TEMPLATE_ID number(19,0), 
	EVAL_DUE_DATE timestamp(6), 
	PROCESSING_STATUS number(4,0),
	EVALUATION_ID number(19,0),
	RESPONSE_ID number(19,0),
	primary key (ID)
);

create index eval_user_temp_map on EVAL_EMAIL_PROCESSING_QUEUE (USER_ID, EMAIL_TEMPLATE_ID);
create index eval_emailq_duedate on EVAL_EMAIL_PROCESSING_QUEUE (EVAL_DUE_DATE);
create index eval_emailq_userid on EVAL_EMAIL_PROCESSING_QUEUE (USER_ID);
create index eval_emailq_id on EVAL_EMAIL_PROCESSING_QUEUE (EAU_ID, EMAIL_TEMPLATE_ID); 
create index eval_emailq_evalid on EVAL_EMAIL_PROCESSING_QUEUE (EVALUATION_ID);


alter table EVAL_ASSIGN_USER add AVAILABLE_EMAIL_SENT timestamp(6) DEFAULT NULL;
alter table EVAL_ASSIGN_USER add REMINDER_EMAIL_SENT timestamp(6) DEFAULT NULL;
create index ASSIGN_USER_AES_IDX on EVAL_ASSIGN_USER (AVAILABLE_EMAIL_SENT);
create index eval_asgnuser_userid on EVAL_ASSIGN_USER (USER_ID);
create index eval_asgnuser_eid on EVAL_ASSIGN_USER (EID);
create index eval_asgnuser_reminderSent on EVAL_ASSIGN_USER (REMINDER_EMAIL_SENT);
create index eval_asgnuser_status on EVAL_ASSIGN_USER (ASSIGN_STATUS);
create index eval_asgnuser_groupid on EVAL_ASSIGN_USER (GROUP_ID);
create index eval_asgnuser_type on EVAL_ASSIGN_USER (ASSIGN_TYPE);
create index eval_asgnuser_availableSent on EVAL_ASSIGN_USER (AVAILABLE_EMAIL_SENT);
alter table EVAL_ASSIGN_USER add constraint ASSIGN_USER_EVALUATION_FKC foreign key (EVALUATION_FK) references EVAL_EVALUATION;

insert into EVAL_CONFIG (ID,LAST_MODIFIED, NAME, VALUE) VALUES (hibernate_sequence.NEXTVAL,CURRENT_TIMESTAMP(6),'CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE',1);

alter table eval_evaluation add  (AVAILABLE_EMAIL_SENT NUMBER(1,0));
alter table EVAL_EVALUATION add (INSTRUCTOR_VIEW_ALL_RESULTS NUMBER(1,0));
alter table EVAL_EVALUATION add (ALL_ROLES_PARTICIPATE NUMBER(1,0));

alter table EVAL_ASSIGN_HIERARCHY add (INSTRUCTORS_VIEW_ALL_RESULTS NUMBER(1,0));
alter table EVAL_ASSIGN_GROUP add (INSTRUCTORS_VIEW_ALL_RESULTS NUMBER(1,0));

alter table EVAL_EMAIL_TEMPLATE add (EID varchar2(255));
create index eval_templ_eid on EVAL_EMAIL_TEMPLATE (EID);

create table EVAL_ADMIN (
    ID number(19,0) not null,
    USER_ID varchar2(255) not null,
    ASSIGN_DATE date not null,
    ASSIGNOR_USER_ID varchar2(255) not null,
    primary key (ID)
);

create index eval_eval_admin_user_id on EVAL_ADMIN (USER_ID);

insert into EVAL_CONFIG (ID,LAST_MODIFIED, NAME, VALUE) VALUES (hibernate_sequence.NEXTVAL,CURRENT_TIMESTAMP(6),'ENABLE_SAKAI_ADMIN_ACCESS','true');

create unique index EVAL_RESP_OGE_IDX on EVAL_RESPONSE (OWNER, GROUP_ID, EVALUATION_FK);

alter table EVAL_ASSIGN_USER add (COMPLETED_DATE date);
create index eval_asgnuser_completedDate on EVAL_ASSIGN_USER (COMPLETED_DATE);

