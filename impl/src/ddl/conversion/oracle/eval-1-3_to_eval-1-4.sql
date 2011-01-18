-- Oracle conversion - 1.3 to 1.4

-- AVAILABLE_EMAIL_SENT was included in ddl files but not in hibernate mapping files 
-- alter table EVAL_EVALUATION add (AVAILABLE_EMAIL_SENT number(1,0));