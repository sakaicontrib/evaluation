-- This is the SQL for use on Oracle DBs to clear the Sakai Evaluation tables from
-- the database.
--
-- WARNING: This will delete all your evaluation data permanently! It should only
--          be used for development purposes.
--
drop table EVAL_ADHOC_EVALUATEES;
drop table EVAL_ADHOC_PARTICIPANTS;
drop table EVAL_ADHOC_GROUP cascade constraints;
drop table EVAL_ADHOC_USER;
drop table EVAL_CONFIG;
drop table EVAL_LOCK;
drop table EVAL_TAGS;
drop table EVAL_TAGS_META;
drop table EVAL_TRANSLATION;
drop table EVAL_ASSIGN_GROUP;
drop table EVAL_ASSIGN_USER;
drop table EVAL_ANSWER;
drop table EVAL_RESPONSE;
drop table EVAL_TEMPLATEITEM;
drop table EVAL_EVALUATION;
drop table EVAL_EMAIL_TEMPLATE;
drop table EVAL_TEMPLATE;
drop table EVAL_ITEMGROUP cascade constraints;
drop table EVAL_ITEM cascade constraints;
drop table EVAL_SCALE cascade constraints;
drop table EVAL_SCALE_OPTIONS;
drop table EVAL_GROUPNODES;
drop table EVAL_GROUPNODES_GROUPS;
drop table EVAL_ASSIGN_HIERARCHY;
