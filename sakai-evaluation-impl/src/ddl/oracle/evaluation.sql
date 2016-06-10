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


    create table EVAL_ADHOC_EVALUATEES (
        GROUP_ID number(19,0) not null,
        USER_ID varchar2(255 char) not null,
        EVALUATEES_INDEX number(10,0) not null,
        primary key (GROUP_ID, EVALUATEES_INDEX)
    );

    create table EVAL_ADHOC_GROUP (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        TITLE varchar2(255 char),
        primary key (ID)
    );

    create table EVAL_ADHOC_PARTICIPANTS (
        GROUP_ID number(19,0) not null,
        USER_ID varchar2(255 char) not null,
        PARTICIPANTS_INDEX number(10,0) not null,
        primary key (GROUP_ID, PARTICIPANTS_INDEX)
    );

    create table EVAL_ADHOC_USER (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        EMAIL varchar2(255 char) not null unique,
        USER_TYPE varchar2(255 char),
        USERNAME varchar2(255 char) unique,
        DISPLAY_NAME varchar2(255 char),
        primary key (ID)
    );

    create table EVAL_ADMIN (
        ID number(19,0) not null,
        USER_ID varchar2(255 char) not null,
        ASSIGN_DATE timestamp not null,
        ASSIGNOR_USER_ID varchar2(255 char) not null,
        primary key (ID)
    );

    create table EVAL_ANSWER (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp not null,
        TEMPLATEITEM_FK number(19,0) not null,
        ITEM_FK number(19,0),
        RESPONSE_FK number(19,0) not null,
        TEXT_ANSWER clob,
        NUM_ANSWER number(10,0),
        MULTI_ANSWER_CODE varchar2(255 char),
        ASSOCIATED_ID varchar2(255 char),
        ASSOCIATED_TYPE varchar2(255 char),
        COMMENT_ANSWER clob,
        primary key (ID)
    );

    create table EVAL_ASSIGN_GROUP (
        ID number(19,0) not null,
        EID varchar2(255 char),
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        group_id varchar2(255 char) not null,
        group_type varchar2(255 char) not null,
        INSTRUCTOR_APPROVAL number(1,0) not null,
        INSTRUCTORS_VIEW_RESULTS number(1,0) not null,
        INSTRUCTORS_VIEW_ALL_RESULTS number(1,0),
        STUDENTS_VIEW_RESULTS number(1,0) not null,
        EVALUATION_FK number(19,0) not null,
        NODE_ID varchar2(255 char),
        SELECTION_SETTINGS varchar2(2000 char),
        primary key (ID)
    );

    create table EVAL_ASSIGN_HIERARCHY (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        NODE_ID varchar2(255 char) not null,
        INSTRUCTOR_APPROVAL number(1,0) not null,
        INSTRUCTORS_VIEW_RESULTS number(1,0) not null,
        INSTRUCTORS_VIEW_ALL_RESULTS number(1,0),
        STUDENTS_VIEW_RESULTS number(1,0) not null,
        EVALUATION_FK number(19,0) not null,
        primary key (ID)
    );

    create table EVAL_ASSIGN_USER (
        ID number(19,0) not null,
        EID varchar2(255 char),
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        USER_ID varchar2(255 char) not null,
        GROUP_ID varchar2(255 char) not null,
        ASSIGN_TYPE varchar2(255 char) not null,
        ASSIGN_STATUS varchar2(255 char) not null,
        LIST_ORDER number(10,0) not null,
        AVAILABLE_EMAIL_SENT timestamp,
        REMINDER_EMAIL_SENT timestamp,
        COMPLETED_DATE timestamp,
        ASSIGN_GROUP_ID number(19,0),
        EVALUATION_FK number(19,0) not null,
        primary key (ID),
        unique (USER_ID, GROUP_ID, ASSIGN_TYPE, EVALUATION_FK)
    );

    create table EVAL_CONFIG (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp not null,
        NAME varchar2(255 char) not null unique,
        VALUE varchar2(255 char) not null,
        primary key (ID)
    );

    create table EVAL_EMAIL_PROCESSING_QUEUE (
        ID number(19,0) not null,
        EAU_ID number(19,0),
        USER_ID varchar2(255 char),
        GROUP_ID varchar2(255 char) not null,
        EMAIL_TEMPLATE_ID number(19,0),
        EVALUATION_ID number(19,0),
        RESPONSE_ID number(19,0),
        EVAL_DUE_DATE timestamp,
        PROCESSING_STATUS number(3,0),
        primary key (ID)
    );

    create table EVAL_EMAIL_TEMPLATE (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        TEMPLATE_TYPE varchar2(255 char) not null,
        SUBJECT clob not null,
        MESSAGE clob not null,
        defaultType varchar2(255 char),
        EID varchar2(255 char),
        primary key (ID)
    );

    create table EVAL_EVALUATION (
        ID number(19,0) not null,
        EID varchar2(255 char),
        LAST_MODIFIED timestamp not null,
        EVAL_TYPE varchar2(255 char) default 'Evaluation' not null,
        OWNER varchar2(255 char) not null,
        TITLE varchar2(255 char) not null,
        INSTRUCTIONS clob,
        START_DATE timestamp not null,
        DUE_DATE timestamp,
        STOP_DATE timestamp,
        VIEW_DATE timestamp,
        STUDENT_VIEW_RESULTS number(1,0),
        INSTRUCTOR_VIEW_RESULTS number(1,0),
        INSTRUCTOR_VIEW_ALL_RESULTS number(1,0) default null,
        STUDENTS_DATE timestamp,
        INSTRUCTORS_DATE timestamp,
        STATE varchar2(255 char) not null,
        INSTRUCTOR_OPT varchar2(255 char),
        REMINDER_DAYS number(10,0) not null,
        REMINDER_FROM_EMAIL varchar2(255 char),
        TERM_ID varchar2(255 char),
        AVAILABLE_EMAIL_SENT number(1,0) default 0,
        AVAILABLE_EMAIL_TEMPLATE_FK number(19,0),
        REMINDER_EMAIL_TEMPLATE_FK number(19,0),
        TEMPLATE_FK number(19,0) not null,
        RESULTS_SHARING varchar2(255 char) default 'visible' not null,
        BLANK_RESPONSES_ALLOWED number(1,0),
        MODIFY_RESPONSES_ALLOWED number(1,0),
        ALL_ROLES_PARTICIPATE number(1,0),
        SECTION_AWARE number(1,0),
        UNREGISTERED_ALLOWED number(1,0),
        LOCKED number(1,0),
        AUTH_CONTROL varchar2(255 char),
        EVAL_CATEGORY varchar2(255 char),
        AUTO_USE_TAG varchar2(255 char),
        AUTO_USE_INSERTION varchar2(255 char),
        SELECTION_SETTINGS varchar2(2000 char),
        EMAIL_OPEN_NOTIFICATION number(1,0),
        REMINDER_STATUS varchar2(255 char),
        LOCAL_SELECTOR varchar2(255 char),
        primary key (ID)
    );

    create table EVAL_GROUPNODES (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp not null,
        NODE_ID varchar2(255 char) not null,
        primary key (ID)
    );

    create table EVAL_GROUPNODES_GROUPS (
        ID number(19,0) not null,
        GROUPS varchar2(255 char) not null,
        GROUPS_INDEX number(10,0) not null,
        primary key (ID, GROUPS_INDEX)
    );

    create table EVAL_HIERARCHY_RULE (
        ID number(19,0) not null,
        NODE_ID number(19,0) not null,
        RULE varchar(255) not null,
        OPT varchar(10) not null,
        primary key (ID)
    );

    create table EVAL_ITEM (
        ID number(19,0) not null,
        EID varchar2(255 char),
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        ITEM_TEXT clob not null,
        DESCRIPTION clob,
        SHARING varchar2(255 char) not null,
        CLASSIFICATION varchar2(255 char) not null,
        EXPERT number(1,0) not null,
        EXPERT_DESCRIPTION clob,
        SCALE_FK number(19,0),
        USES_NA number(1,0),
        USES_COMMENT number(1,0),
        DISPLAY_ROWS number(10,0),
        SCALE_DISPLAY_SETTING varchar2(255 char),
        COMPULSORY number(1,0),
        CATEGORY varchar2(255 char),
        LOCKED number(1,0),
        COPY_OF number(19,0),
        HIDDEN number(1,0),
        AUTO_USE_TAG varchar2(255 char),
        IG_ITEM_ID number(19,0),
        primary key (ID)
    );

    create table EVAL_ITEMGROUP (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp,
        OWNER varchar2(255 char) not null,
        type varchar2(255 char) not null,
        title varchar2(80 char) not null,
        description clob,
        EXPERT number(1,0),
        GROUP_PARENT_FK number(19,0),
        primary key (ID)
    );

    create table EVAL_LOCK (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp not null,
        NAME varchar2(255 char) not null unique,
        HOLDER varchar2(255 char) not null,
        primary key (ID)
    );

    create table EVAL_RESPONSE (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        GROUP_ID varchar2(255 char) not null,
        START_TIME timestamp not null,
        COMMENT_RESPONSE clob,
        SELECTIONS_CODE clob,
        END_TIME timestamp,
        EVALUATION_FK number(19,0) not null,
        primary key (ID),
        unique (OWNER, GROUP_ID, EVALUATION_FK)
    );

    create table EVAL_SCALE (
        ID number(19,0) not null,
        EID varchar2(255 char),
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        TITLE varchar2(255 char) not null,
        SCALE_MODE varchar2(255 char) not null,
        SHARING varchar2(255 char) not null,
        EXPERT number(1,0) not null,
        EXPERT_DESCRIPTION clob,
        IDEAL varchar2(255 char),
        LOCKED number(1,0),
        COPY_OF number(19,0),
        HIDDEN number(1,0),
        primary key (ID)
    );

    create table EVAL_SCALE_OPTIONS (
        ID number(19,0) not null,
        SCALE_OPTION varchar2(255 char) not null,
        SCALE_OPTION_INDEX number(10,0) not null,
        primary key (ID, SCALE_OPTION_INDEX)
    );

    create table EVAL_TAGS (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp,
        TAG varchar2(255 char) not null,
        ENTITY_TYPE varchar2(255 char) not null,
        ENTITY_ID varchar2(255 char) not null,
        primary key (ID)
    );

    create table EVAL_TAGS_META (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp,
        OWNER varchar2(255 char) not null,
        TAG varchar2(255 char) not null,
        TITLE varchar2(255 char),
        DESCRIPTION clob,
        primary key (ID)
    );

    create table EVAL_TEMPLATE (
        ID number(19,0) not null,
        EID varchar2(255 char),
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        TYPE varchar2(255 char) not null,
        TITLE varchar2(255 char) not null,
        DESCRIPTION clob,
        SHARING varchar2(255 char) not null,
        EXPERT number(1,0) not null,
        expertDescription clob,
        LOCKED number(1,0),
        COPY_OF number(19,0),
        HIDDEN number(1,0),
        AUTO_USE_TAG varchar2(255 char),
        primary key (ID)
    );

    create table EVAL_TEMPLATEITEM (
        ID number(19,0) not null,
        EID varchar2(255 char),
        LAST_MODIFIED timestamp not null,
        OWNER varchar2(255 char) not null,
        template_id number(19,0) not null,
        item_id number(19,0) not null,
        DISPLAY_ORDER number(10,0) not null,
        ITEM_CATEGORY varchar2(255 char) not null,
        HIERARCHY_LEVEL varchar2(255 char) not null,
        HIERARCHY_NODE_ID varchar2(255 char) not null,
        DISPLAY_ROWS number(10,0),
        SCALE_DISPLAY_SETTING varchar2(255 char),
        USES_NA number(1,0),
        USES_COMMENT number(1,0),
        COMPULSORY number(1,0),
        BLOCK_PARENT number(1,0),
        BLOCK_ID number(19,0),
        RESULTS_SHARING varchar2(255 char),
        COPY_OF number(19,0),
        HIDDEN number(1,0),
        AUTO_USE_TAG varchar2(255 char),
        AUTO_USE_INSERT_TAG varchar2(255 char),
        primary key (ID)
    );

    create table EVAL_TRANSLATION (
        ID number(19,0) not null,
        LAST_MODIFIED timestamp,
        LANGUAGE_CODE varchar2(255 char) not null,
        OBJECT_CLASS varchar2(255 char) not null,
        OBJECT_ID varchar2(255 char) not null,
        FIELD_NAME varchar2(255 char) not null,
        TRANSLATION clob not null,
        primary key (ID)
    );

    alter table EVAL_ADHOC_EVALUATEES 
        add constraint FK91C74B304BC805E4 
        foreign key (GROUP_ID) 
        references EVAL_ADHOC_GROUP;

    create index eval_ahgroup_owner on EVAL_ADHOC_GROUP (OWNER);

    alter table EVAL_ADHOC_PARTICIPANTS 
        add constraint FKAF16E1894BC805E4 
        foreign key (GROUP_ID) 
        references EVAL_ADHOC_GROUP;

    create index eval_ahuser_type on EVAL_ADHOC_USER (USER_TYPE);

    create index eval_eval_admin_user_id on EVAL_ADMIN (USER_ID);

    create index eval_answer_num on EVAL_ANSWER (NUM_ANSWER);

    alter table EVAL_ANSWER 
        add constraint ANSWER_RESPONSE_FKC 
        foreign key (RESPONSE_FK) 
        references EVAL_RESPONSE;

    alter table EVAL_ANSWER 
        add constraint ANSWER_ITEM_FKC 
        foreign key (ITEM_FK) 
        references EVAL_ITEM;

    alter table EVAL_ANSWER 
        add constraint ANSWER_TEMPLATEITEM_FKC 
        foreign key (TEMPLATEITEM_FK) 
        references EVAL_TEMPLATEITEM;

    create index eval_assigngroup_eid on EVAL_ASSIGN_GROUP (EID);

    create index eval_assign_group_nodeid on EVAL_ASSIGN_GROUP (NODE_ID);

    create index eval_assign_groupid on EVAL_ASSIGN_GROUP (group_id);

    alter table EVAL_ASSIGN_GROUP 
        add constraint ASSIGN_COURSE_EVALUATION_FKC 
        foreign key (EVALUATION_FK) 
        references EVAL_EVALUATION;

    create index eval_assign_hier_nodeid on EVAL_ASSIGN_HIERARCHY (NODE_ID);

    alter table EVAL_ASSIGN_HIERARCHY 
        add constraint ASSIGN_HIER_EVALUATION_FKC 
        foreign key (EVALUATION_FK) 
        references EVAL_EVALUATION;

    create index eval_asgnuser_userid on EVAL_ASSIGN_USER (USER_ID);

    create index eval_asgnuser_eid on EVAL_ASSIGN_USER (EID);

    create index eval_asgnuser_reminderSent on EVAL_ASSIGN_USER (REMINDER_EMAIL_SENT);

    create index eval_asgnuser_status on EVAL_ASSIGN_USER (ASSIGN_STATUS);

    create index eval_asgnuser_groupid on EVAL_ASSIGN_USER (GROUP_ID);

    create index eval_asgnuser_type on EVAL_ASSIGN_USER (ASSIGN_TYPE);

    create index eval_asgnuser_completedDate on EVAL_ASSIGN_USER (COMPLETED_DATE);

    create index eval_asgnuser_availableSent on EVAL_ASSIGN_USER (AVAILABLE_EMAIL_SENT);

    alter table EVAL_ASSIGN_USER 
        add constraint ASSIGN_USER_EVALUATION_FKC 
        foreign key (EVALUATION_FK) 
        references EVAL_EVALUATION;

    create index eval_config_name on EVAL_CONFIG (NAME);

    create index eval_user_temp_map on EVAL_EMAIL_PROCESSING_QUEUE (USER_ID, EMAIL_TEMPLATE_ID);

    create index eval_emailq_duedate on EVAL_EMAIL_PROCESSING_QUEUE (EVAL_DUE_DATE);

    create index eval_emailq_userid on EVAL_EMAIL_PROCESSING_QUEUE (USER_ID);

    create index eval_emailq_id on EVAL_EMAIL_PROCESSING_QUEUE (EAU_ID, EMAIL_TEMPLATE_ID);

    create index eval_emailq_evalid on EVAL_EMAIL_PROCESSING_QUEUE (EVALUATION_ID);

    create index eval_templ_owner on EVAL_EMAIL_TEMPLATE (OWNER);

    create index eval_templ_type on EVAL_EMAIL_TEMPLATE (TEMPLATE_TYPE);

    create index eval_templ_eid on EVAL_EMAIL_TEMPLATE (EID);

    create index eval_eval_state on EVAL_EVALUATION (STATE);

    create index eval_eval_viewdate on EVAL_EVALUATION (VIEW_DATE);

    create index eval_eval_category on EVAL_EVALUATION (EVAL_CATEGORY);

    create index eval_eval_term on EVAL_EVALUATION (TERM_ID);

    create index eval_eval_type on EVAL_EVALUATION (EVAL_TYPE);

    create index eval_eval_owner on EVAL_EVALUATION (OWNER);

    create index eval_eval_duedate on EVAL_EVALUATION (DUE_DATE);

    create index eval_eval_startdate on EVAL_EVALUATION (START_DATE);

    create index eval_evaluation_eid on EVAL_EVALUATION (EID);

    alter table EVAL_EVALUATION 
        add constraint EVALUATION_REMINDER_EMAIL_TEMP 
        foreign key (REMINDER_EMAIL_TEMPLATE_FK) 
        references EVAL_EMAIL_TEMPLATE;

    alter table EVAL_EVALUATION 
        add constraint EVALUATION_AVAILABLE_EMAIL_TEM 
        foreign key (AVAILABLE_EMAIL_TEMPLATE_FK) 
        references EVAL_EMAIL_TEMPLATE;

    alter table EVAL_EVALUATION 
        add constraint EVALUATION_TEMPLATE_FKC 
        foreign key (TEMPLATE_FK) 
        references EVAL_TEMPLATE;

    create index eval_group_nodeid on EVAL_GROUPNODES (NODE_ID);

    alter table EVAL_GROUPNODES_GROUPS 
        add constraint FK2248663E2E2E0ED0 
        foreign key (ID) 
        references EVAL_GROUPNODES;

    create index eval_item_owner on EVAL_ITEM (OWNER);

    create index eval_item_sharing on EVAL_ITEM (SHARING);

    create index eval_item_eid on EVAL_ITEM (EID);

    create index eval_item_expert on EVAL_ITEM (EXPERT);

    alter table EVAL_ITEM 
        add constraint ITEM_SCALE_FKC 
        foreign key (SCALE_FK) 
        references EVAL_SCALE;

    alter table EVAL_ITEM 
        add constraint FKD19984D6F71AFC2F 
        foreign key (IG_ITEM_ID) 
        references EVAL_ITEMGROUP;

    create index eval_itemgroup_owner on EVAL_ITEMGROUP (OWNER);

    create index eval_itemgroup_type on EVAL_ITEMGROUP (type);

    create index eval_itemgroup_expert on EVAL_ITEMGROUP (EXPERT);

    alter table EVAL_ITEMGROUP 
        add constraint ITEM_GROUP_PARENT_FKC 
        foreign key (GROUP_PARENT_FK) 
        references EVAL_ITEMGROUP;

    create index eval_lock_name on EVAL_LOCK (NAME);

    create index eval_response_groupid on EVAL_RESPONSE (GROUP_ID);

    create index eval_response_owner on EVAL_RESPONSE (OWNER);

    alter table EVAL_RESPONSE 
        add constraint RESPONSE_EVALUATION_FKC 
        foreign key (EVALUATION_FK) 
        references EVAL_EVALUATION;

    create index eval_scale_owner on EVAL_SCALE (OWNER);

    create index eval_scale_mode on EVAL_SCALE (SCALE_MODE);

    create index eval_scale_sharing on EVAL_SCALE (SHARING);

    create index eval_scale_eid on EVAL_SCALE (EID);

    alter table EVAL_SCALE_OPTIONS 
        add constraint FKE8093A06DEE55A42 
        foreign key (ID) 
        references EVAL_SCALE;

    create index eval_tags_tag on EVAL_TAGS (TAG);

    create index eval_tagsmeta_owner on EVAL_TAGS_META (OWNER);

    create index eval_tagsmeta_tag on EVAL_TAGS_META (TAG);

    create index eval_template_sharing on EVAL_TEMPLATE (SHARING);

    create index eval_template_eid on EVAL_TEMPLATE (EID);

    create index eval_template_owner on EVAL_TEMPLATE (OWNER);

    create index eval_templateitem_blockid on EVAL_TEMPLATEITEM (BLOCK_ID);

    create index eval_templateitem_eid on EVAL_TEMPLATEITEM (EID);

    create index eval_templateitem_owner on EVAL_TEMPLATEITEM (OWNER);

    alter table EVAL_TEMPLATEITEM 
        add constraint FK35F30150B6DB815D 
        foreign key (item_id) 
        references EVAL_ITEM;

    alter table EVAL_TEMPLATEITEM 
        add constraint FK35F30150EDECBA7D 
        foreign key (template_id) 
        references EVAL_TEMPLATE;

    create index eval_trans_field on EVAL_TRANSLATION (FIELD_NAME);

    create index eval_trans_langcode on EVAL_TRANSLATION (LANGUAGE_CODE);

    create index eval_trans_class on EVAL_TRANSLATION (OBJECT_CLASS);

    create index eval_trans_objectid on EVAL_TRANSLATION (OBJECT_ID);

    create sequence hibernate_sequence;
