--
-- Copyright 2005 Sakai Foundation Licensed under the
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

-- This is the SQL for use on MySQL to clear the Sakai Evaluation tables from
-- the database.
--
-- WARNING: This will delete all your evaluation data permanently! It should only
--          be used for development purposes.
--
set FOREIGN_KEY_CHECKS=0;
drop table if exists  EVAL_ADHOC_EVALUATEES;
drop table if exists  EVAL_ADHOC_PARTICIPANTS;
drop table if exists  EVAL_ADHOC_GROUP;
drop table if exists  EVAL_ADHOC_USER;
drop table if exists  EVAL_CONFIG;
drop table if exists  EVAL_LOCK;
drop table if exists  EVAL_TAGS;
drop table if exists  EVAL_TAGS_META;
drop table if exists  EVAL_TRANSLATION;
drop table if exists  EVAL_ASSIGN_GROUP;
drop table if exists  EVAL_ASSIGN_USER;
drop table if exists  EVAL_ANSWER;
drop table if exists  EVAL_RESPONSE;
drop table if exists  EVAL_TEMPLATEITEM;
drop table if exists  EVAL_EVALUATION;
drop table if exists  EVAL_EMAIL_TEMPLATE;
drop table if exists  EVAL_TEMPLATE;
drop table if exists  EVAL_ITEMGROUP;
drop table if exists  EVAL_ITEM;
drop table if exists  EVAL_SCALE;
drop table if exists  EVAL_SCALE_OPTIONS;
drop table if exists  EVAL_GROUPNODES;
drop table if exists  EVAL_GROUPNODES_GROUPS;
drop table if exists  EVAL_ASSIGN_HIERARCHY;
set FOREIGN_KEY_CHECKS=1;
