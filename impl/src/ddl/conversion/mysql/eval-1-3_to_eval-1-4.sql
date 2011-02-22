-- MySQL conversion script - 1.3 to 1.4 
alter table EVAL_EVALUATION add column LOCAL_SELECTOR varchar(255);

-- AVAILABLE_EMAIL_SENT was included in ddl files but not in hibernate mapping files 
-- alter table EVAL_EVALUATION add column AVAILABLE_EMAIL_SENT bit(1);
-- If AVAILABLE_EMAIL_SENT is being added, give it a reasonable default -- assume emails have been sent for Active evals and not for others?
-- update EVAL_EVALUATION set AVAILABLE_EMAIL_SENT='1' where STATE='Active' or STATE='Closed';
-- update EVAL_EVALUATION set AVAILABLE_EMAIL_SENT='0' where AVAILABLE_EMAIL_SENT is null;
