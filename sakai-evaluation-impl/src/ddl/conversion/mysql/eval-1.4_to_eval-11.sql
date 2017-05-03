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

-- MySQL conversion script - 1.4 to 11

alter table EVAL_EVALUATION add (SECTION_AWARE bit not null default 0);

create table EVAL_HIERARCHY_RULE (
    ID bigint not null auto_increment,
    NODE_ID bigint not null,
    RULE varchar(255) not null,
    OPT varchar(10) not null,
    primary key (ID)
) ENGINE=InnoDB;

INSERT INTO EVAL_EMAIL_TEMPLATE (LAST_MODIFIED, OWNER, TEMPLATE_TYPE, SUBJECT, MESSAGE, defaultType, EID) VALUES (now(),'admin','Available Evaluatee','The Evaluation: ${EvalTitle} for ${EvalGroupTitle} is available to be taken','All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to ${HelpdeskEmail}. \n\nAn evaluation (${EvalTitle}) for: ${EvalGroupTitle} is ready to be filled out by students.It has an open date of ${EvalStartDate} and is due by ${EvalDueDate} at the latest.\n\nYou may access the evaluation at:\n${URLtoTakeEval} \nIf the above link is not working then please follow the Alternate Instructions at the bottom of the message. \nEnter the site using your username and password.\n------------------------------------------------------------\nShould you encounter any technical difficulty in filling out the evaluation, please send an email to ${HelpdeskEmail} clearly indicating the problem you encountered. For any other concerns please contact your department.\n\nAlternate Instructions: \n1) Go to ${URLtoSystem} \n2) Enter your username and password and click on \'Login\' button. \n3) Click on \'Evaluation System\' in the left navigation menu under My Workspace. \n4) Click on \'${EvalGroupTitle}\' link under \'${EvalTitle}\'.. \n','Available Evaluatee',NULL)
