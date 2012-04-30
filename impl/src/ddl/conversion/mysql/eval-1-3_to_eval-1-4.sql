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

-- MySQL conversion script - 1.3 to 1.4 
alter table EVAL_EVALUATION add (LOCAL_SELECTOR varchar(255));

update EVAL_EMAIL_TEMPLATE set template_type='ConsolidatedAvailable' where template_type='SingleEmailAvailable';
update EVAL_EMAIL_TEMPLATE set template_type='ConsolidatedReminder' where template_type='SingleEmailReminder';

create table EVAL_EMAIL_PROCESSING_QUEUE 
(
	ID bigint not null auto_increment, 
	EAU_ID bigint,  
	USER_ID varchar(255), 
	GROUP_ID varchar(255),
	EMAIL_TEMPLATE_ID bigint, 
	EVAL_DUE_DATE datetime, 
	PROCESSING_STATUS tinyint,
	EVALUATION_ID bigint,
	RESPONSE_ID bigint,
	primary key (ID)
);

create index eval_user_temp_map on EVAL_EMAIL_PROCESSING_QUEUE (USER_ID, EMAIL_TEMPLATE_ID);
create index eval_emailq_duedate on EVAL_EMAIL_PROCESSING_QUEUE (EVAL_DUE_DATE);
create index eval_emailq_userid on EVAL_EMAIL_PROCESSING_QUEUE (USER_ID);
create index eval_emailq_id on EVAL_EMAIL_PROCESSING_QUEUE (EAU_ID, EMAIL_TEMPLATE_ID); 
create index eval_emailq_evalid on EVAL_EMAIL_PROCESSING_QUEUE (EVALUATION_ID);

alter table EVAL_ASSIGN_GROUP add column AVAILABLE_EMAIL_SENT datetime DEFAULT NULL;
alter table EVAL_ASSIGN_GROUP add column REMINDER_EMAIL_SENT datetime DEFAULT NULL;
create index ASSIGN_USER_AES_IDX on EVAL_ASSIGN_USER (AVAILABLE_EMAIL_SENT);
create index ASSIGN_USER_RES_IDX on EVAL_ASSIGN_USER (REMINDER_EMAIL_SENT);
	
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE',true);

alter table EVAL_EVALUATION add (AVAILABLE_EMAIL_SENT bit);

alter table EVAL_EVALUATION add (INSTRUCTOR_VIEW_ALL_RESULTS bit);
alter table EVAL_EVALUATION add (ALL_ROLES_PARTICIPATE bit);

alter table EVAL_ASSIGN_HIERARCHY add (INSTRUCTORS_VIEW_ALL_RESULTS bit);
alter table EVAL_ASSIGN_GROUP add (INSTRUCTORS_VIEW_ALL_RESULTS bit);

alter table EVAL_ASSIGN_USER add (COMPLETED_DATE datetime);
create index eval_asgnuser_completedDate on EVAL_ASSIGN_USER (COMPLETED_DATE);

insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'ENABLE_SAKAI_ADMIN_ACCESS','true');
