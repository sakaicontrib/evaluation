-- MySQL conversion script - 1.3 to 1.4 
alter table EVAL_EVALUATION add column LOCAL_SELECTOR varchar(255);

update eval_email_template set template_type='ConsolidatedAvailable' where template_type='SingleEmailAvailable';
update eval_email_template set template_type='ConsolidatedReminder' where template_type='SingleEmailReminder';

create table EVAL_EMAIL_PROCESSING_QUEUE 
(
	ID bigint not null auto_increment, 
	EAU_ID bigint,  
	USER_ID varchar(255), 
	GROUP_ID varchar(255),
	EMAIL_TEMPLATE_ID bigint, 
	EVAL_DUE_DATE datetime, 
	PROCESSING_STATUS tinyint, 
	primary key (ID)
);

create index EVAL_EPQ_UITI_IDX on EVAL_EMAIL_PROCESSING_QUEUE (EMAIL_TEMPLATE_ID,USER_ID); 
 

alter table EVAL_ASSIGN_GROUP add column AVAILABLE_EMAIL_SENT datetime DEFAULT NULL;
alter table EVAL_ASSIGN_GROUP add column REMINDER_EMAIL_SENT datetime DEFAULT NULL;
create index ASSIGN_USER_AES_IDX on EVAL_ASSIGN_USER (AVAILABLE_EMAIL_SENT);
create index ASSIGN_USER_RES_IDX on EVAL_ASSIGN_USER (REMINDER_EMAIL_SENT);
	
insert into EVAL_CONFIG (LAST_MODIFIED, NAME, VALUE) VALUES (CURRENT_TIMESTAMP(),'CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE',true);

alter table eval_evaluation add (AVAILABLE_EMAIL_SENT bit);

alter table EVAL_EVALUATION add (INSTRUCTOR_VIEW_ALL_RESULTS bit);
alter table EVAL_ASSIGN_HIERARCHY add (INSTRUCTORS_VIEW_ALL_RESULTS bit);
alter table EVAL_ASSIGN_GROUP add (INSTRUCTORS_VIEW_ALL_RESULTS bit);