-- Oracle conversion script - 1.3 to 1.4 
alter table EVAL_EVALUATION add (LOCAL_SELECTOR varchar2(255 char));

update eval_email_template set template_type='ConsolidatedAvailable' where template_type='SingleEmailAvailable';
update eval_email_template set template_type='ConsolidatedReminder' where template_type='SingleEmailReminder';

create table EVAL_EMAIL_PROCESSING_QUEUE 
(
	ID number(19,0) not null, 
	EAU_ID number(19,0),  
	USER_ID varchar2(255), 
	EMAIL_TEMPLATE_ID number(19,0), 
	EVAL_DUE_DATE timestamp(6), 
	PROCESSING_STATUS number(4,0), 
	primary key (ID)
);

alter table EVAL_ASSIGN_USER add AVAILABLE_EMAIL_SENT timestamp(6) DEFAULT NULL;
alter table EVAL_ASSIGN_USER add REMINDER_EMAIL_SENT timestamp(6) DEFAULT NULL;

insert into EVAL_CONFIG (ID,LAST_MODIFIED, NAME, VALUE) VALUES (hibernate_sequence.NEXTVAL,CURRENT_TIMESTAMP(6),'CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE',1);