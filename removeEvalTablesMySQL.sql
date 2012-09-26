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

-- This is the SQL for use on MySQL to clear the Sakai Evaluation tables from the database.
--
-- WARNING: This will delete all your evaluation data permanently! It should only be used for development purposes.
--
set FOREIGN_KEY_CHECKS=0;
drop table if exists  eval_adhoc_evaluatees;
drop table if exists  eval_adhoc_group;
drop table if exists  eval_adhoc_participants;
drop table if exists  eval_adhoc_user;
drop table if exists  eval_admin;
drop table if exists  eval_answer;
drop table if exists  eval_assign_group;
drop table if exists  eval_assign_hierarchy;
drop table if exists  eval_assign_user;
drop table if exists  eval_config;
drop table if exists  eval_email_processing_queue;
drop table if exists  eval_email_template;
drop table if exists  eval_evaluation;
drop table if exists  eval_groupnodes;
drop table if exists  eval_groupnodes_groups;
drop table if exists  eval_item;
drop table if exists  eval_itemgroup;
drop table if exists  eval_lock;
drop table if exists  eval_response;
drop table if exists  eval_scale;
drop table if exists  eval_scale_options;
drop table if exists  eval_tags;
drop table if exists  eval_tags_meta;
drop table if exists  eval_template;
drop table if exists  eval_templateitem;
drop table if exists  eval_translation;
set FOREIGN_KEY_CHECKS=1;
