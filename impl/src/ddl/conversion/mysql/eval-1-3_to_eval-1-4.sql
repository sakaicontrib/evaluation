-- MySQL conversion - 1.3 to 1.4 

-- AVAILABLE_EMAIL_SENT was included in ddl files but not in hibernate mapping files 
-- alter table EVAL_EVALUATION add column AVAILABLE_EMAIL_SENT bit(1);