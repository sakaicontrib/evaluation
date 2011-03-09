-- MySQL conversion script - 1.3 to 1.4 
alter table EVAL_EVALUATION add column LOCAL_SELECTOR varchar(255);

update eval_email_template set template_type='ConsolidatedAvailable' where template_type='SingleEmailAvailable';
update eval_email_template set template_type='ConsolidatedReminder' where template_type='SingleEmailReminder';

create table EVAL_EMAIL_PROCESSING_QUEUE 
(
	ID bigint not null auto_increment, 
	EAU_ID bigint,  
	USER_ID varchar(255), 
	EMAIL_TEMPLATE_ID bigint, 
	EVAL_DUE_DATE datetime, 
	PROCESSING_STATUS tinyint, 
	primary key (ID)
);

alter table EVAL_ASSIGN_GROUP add column AVAILABLE_EMAIL_SENT datetime DEFAULT NULL;
alter table EVAL_ASSIGN_GROUP add column REMINDER_EMAIL_SENT datetime DEFAULT NULL;

