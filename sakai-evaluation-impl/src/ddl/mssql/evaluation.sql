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
        GROUP_ID numeric(19,0) not null,
        USER_ID varchar(255) not null,
        EVALUATEES_INDEX int not null,
        primary key (GROUP_ID, EVALUATEES_INDEX)
    );

    create table EVAL_ADHOC_GROUP (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        TITLE varchar(255) null,
        primary key (ID)
    );

    create table EVAL_ADHOC_PARTICIPANTS (
        GROUP_ID numeric(19,0) not null,
        USER_ID varchar(255) not null,
        PARTICIPANTS_INDEX int not null,
        primary key (GROUP_ID, PARTICIPANTS_INDEX)
    );

    create table EVAL_ADHOC_USER (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        EMAIL varchar(255) not null unique,
        USER_TYPE varchar(255) null,
        USERNAME varchar(255) null unique,
        DISPLAY_NAME varchar(255) null,
        primary key (ID)
    );

    create table EVAL_ADMIN (
        ID numeric(19,0) identity not null,
        USER_ID varchar(255) not null,
        ASSIGN_DATE datetime not null,
        ASSIGNOR_USER_ID varchar(255) not null,
        primary key (ID)
    );

    create table EVAL_ANSWER (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime not null,
        TEMPLATEITEM_FK numeric(19,0) not null,
        ITEM_FK numeric(19,0) null,
        RESPONSE_FK numeric(19,0) not null,
        TEXT_ANSWER text null,
        NUM_ANSWER int null,
        MULTI_ANSWER_CODE varchar(255) null,
        ASSOCIATED_ID varchar(255) null,
        ASSOCIATED_TYPE varchar(255) null,
        COMMENT_ANSWER text null,
        primary key (ID)
    );

    create table EVAL_ASSIGN_GROUP (
        ID numeric(19,0) identity not null,
        EID varchar(255) null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        group_id varchar(255) not null,
        group_type varchar(255) not null,
        INSTRUCTOR_APPROVAL tinyint not null,
        INSTRUCTORS_VIEW_RESULTS tinyint not null,
        INSTRUCTORS_VIEW_ALL_RESULTS tinyint null,
        STUDENTS_VIEW_RESULTS tinyint not null,
        EVALUATION_FK numeric(19,0) not null,
        NODE_ID varchar(255) null,
        SELECTION_SETTINGS varchar(2000) null,
        primary key (ID)
    );

    create table EVAL_ASSIGN_HIERARCHY (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        NODE_ID varchar(255) not null,
        INSTRUCTOR_APPROVAL tinyint not null,
        INSTRUCTORS_VIEW_RESULTS tinyint not null,
        INSTRUCTORS_VIEW_ALL_RESULTS tinyint null,
        STUDENTS_VIEW_RESULTS tinyint not null,
        EVALUATION_FK numeric(19,0) not null,
        primary key (ID)
    );

    create table EVAL_ASSIGN_USER (
        ID numeric(19,0) identity not null,
        EID varchar(255) null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        USER_ID varchar(255) not null,
        GROUP_ID varchar(255) not null,
        ASSIGN_TYPE varchar(255) not null,
        ASSIGN_STATUS varchar(255) not null,
        LIST_ORDER int not null,
        AVAILABLE_EMAIL_SENT datetime null,
        REMINDER_EMAIL_SENT datetime null,
        COMPLETED_DATE datetime null,
        ASSIGN_GROUP_ID numeric(19,0) null,
        EVALUATION_FK numeric(19,0) not null,
        primary key (ID),
        unique (USER_ID, GROUP_ID, ASSIGN_TYPE, EVALUATION_FK)
    );

    create table EVAL_CONFIG (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime not null,
        NAME varchar(255) not null unique,
        VALUE varchar(255) not null,
        primary key (ID)
    );

    create table EVAL_EMAIL_PROCESSING_QUEUE (
        ID numeric(19,0) identity not null,
        EAU_ID numeric(19,0) null,
        USER_ID varchar(255) null,
        GROUP_ID varchar(255) not null,
        EMAIL_TEMPLATE_ID numeric(19,0) null,
        EVALUATION_ID numeric(19,0) null,
        RESPONSE_ID numeric(19,0) null,
        EVAL_DUE_DATE datetime null,
        PROCESSING_STATUS tinyint null,
        primary key (ID)
    );

    create table EVAL_EMAIL_TEMPLATE (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        TEMPLATE_TYPE varchar(255) not null,
        SUBJECT text not null,
        MESSAGE text not null,
        defaultType varchar(255) null,
        EID varchar(255) null,
        primary key (ID)
    );

    create table EVAL_EVALUATION (
        ID numeric(19,0) identity not null,
        EID varchar(255) null,
        LAST_MODIFIED datetime not null,
        EVAL_TYPE varchar(255) default 'Evaluation' not null,
        OWNER varchar(255) not null,
        TITLE varchar(255) not null,
        INSTRUCTIONS text null,
        START_DATE datetime not null,
        DUE_DATE datetime null,
        STOP_DATE datetime null,
        VIEW_DATE datetime null,
        STUDENT_VIEW_RESULTS tinyint null,
        INSTRUCTOR_VIEW_RESULTS tinyint null,
        INSTRUCTOR_VIEW_ALL_RESULTS tinyint default null null,
        STUDENTS_DATE datetime null,
        INSTRUCTORS_DATE datetime null,
        STATE varchar(255) not null,
        INSTRUCTOR_OPT varchar(255) null,
        REMINDER_DAYS int not null,
        REMINDER_FROM_EMAIL varchar(255) null,
        TERM_ID varchar(255) null,
        AVAILABLE_EMAIL_SENT tinyint default 0 null,
        AVAILABLE_EMAIL_TEMPLATE_FK numeric(19,0) null,
        REMINDER_EMAIL_TEMPLATE_FK numeric(19,0) null,
        TEMPLATE_FK numeric(19,0) not null,
        RESULTS_SHARING varchar(255) default 'visible' not null,
        BLANK_RESPONSES_ALLOWED tinyint null,
        MODIFY_RESPONSES_ALLOWED tinyint null,
        ALL_ROLES_PARTICIPATE tinyint null,
        SECTION_AWARE tinyint null,
        UNREGISTERED_ALLOWED tinyint null,
        LOCKED tinyint null,
        AUTH_CONTROL varchar(255) null,
        EVAL_CATEGORY varchar(255) null,
        AUTO_USE_TAG varchar(255) null,
        AUTO_USE_INSERTION varchar(255) null,
        SELECTION_SETTINGS varchar(2000) null,
        EMAIL_OPEN_NOTIFICATION tinyint null,
        REMINDER_STATUS varchar(255) null,
        LOCAL_SELECTOR varchar(255) null,
        primary key (ID)
    );

    create table EVAL_GROUPNODES (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime not null,
        NODE_ID varchar(255) not null,
        primary key (ID)
    );

    create table EVAL_GROUPNODES_GROUPS (
        ID numeric(19,0) not null,
        GROUPS varchar(255) not null,
        GROUPS_INDEX int not null,
        primary key (ID, GROUPS_INDEX)
    );

    create table EVAL_HIERARCHY_RULE (
        ID numeric(19,0) identify not null,
        NODE_ID numeric(19,0) not null,
        RULE varchar(255) not null,
        OPT varchar(10) not null,
        primary key (ID)
    );

    create table EVAL_ITEM (
        ID numeric(19,0) identity not null,
        EID varchar(255) null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        ITEM_TEXT text not null,
        DESCRIPTION text null,
        SHARING varchar(255) not null,
        CLASSIFICATION varchar(255) not null,
        EXPERT tinyint not null,
        EXPERT_DESCRIPTION text null,
        SCALE_FK numeric(19,0) null,
        USES_NA tinyint null,
        USES_COMMENT tinyint null,
        DISPLAY_ROWS int null,
        SCALE_DISPLAY_SETTING varchar(255) null,
        COMPULSORY tinyint null,
        CATEGORY varchar(255) null,
        LOCKED tinyint null,
        COPY_OF numeric(19,0) null,
        HIDDEN tinyint null,
        AUTO_USE_TAG varchar(255) null,
        IG_ITEM_ID numeric(19,0) null,
        primary key (ID)
    );

    create table EVAL_ITEMGROUP (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime null,
        OWNER varchar(255) not null,
        type varchar(255) not null,
        title varchar(80) not null,
        description text null,
        EXPERT tinyint null,
        GROUP_PARENT_FK numeric(19,0) null,
        primary key (ID)
    );

    create table EVAL_LOCK (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime not null,
        NAME varchar(255) not null unique,
        HOLDER varchar(255) not null,
        primary key (ID)
    );

    create table EVAL_RESPONSE (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        GROUP_ID varchar(255) not null,
        START_TIME datetime not null,
        COMMENT_RESPONSE text null,
        SELECTIONS_CODE text null,
        END_TIME datetime null,
        EVALUATION_FK numeric(19,0) not null,
        primary key (ID),
        unique (OWNER, GROUP_ID, EVALUATION_FK)
    );

    create table EVAL_SCALE (
        ID numeric(19,0) identity not null,
        EID varchar(255) null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        TITLE varchar(255) not null,
        SCALE_MODE varchar(255) not null,
        SHARING varchar(255) not null,
        EXPERT tinyint not null,
        EXPERT_DESCRIPTION text null,
        IDEAL varchar(255) null,
        LOCKED tinyint null,
        COPY_OF numeric(19,0) null,
        HIDDEN tinyint null,
        primary key (ID)
    );

    create table EVAL_SCALE_OPTIONS (
        ID numeric(19,0) not null,
        SCALE_OPTION varchar(255) not null,
        SCALE_OPTION_INDEX int not null,
        primary key (ID, SCALE_OPTION_INDEX)
    );

    create table EVAL_TAGS (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime null,
        TAG varchar(255) not null,
        ENTITY_TYPE varchar(255) not null,
        ENTITY_ID varchar(255) not null,
        primary key (ID)
    );

    create table EVAL_TAGS_META (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime null,
        OWNER varchar(255) not null,
        TAG varchar(255) not null,
        TITLE varchar(255) null,
        DESCRIPTION text null,
        primary key (ID)
    );

    create table EVAL_TEMPLATE (
        ID numeric(19,0) identity not null,
        EID varchar(255) null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        TYPE varchar(255) not null,
        TITLE varchar(255) not null,
        DESCRIPTION text null,
        SHARING varchar(255) not null,
        EXPERT tinyint not null,
        expertDescription text null,
        LOCKED tinyint null,
        COPY_OF numeric(19,0) null,
        HIDDEN tinyint null,
        AUTO_USE_TAG varchar(255) null,
        primary key (ID)
    );

    create table EVAL_TEMPLATEITEM (
        ID numeric(19,0) identity not null,
        EID varchar(255) null,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        template_id numeric(19,0) not null,
        item_id numeric(19,0) not null,
        DISPLAY_ORDER int not null,
        ITEM_CATEGORY varchar(255) not null,
        HIERARCHY_LEVEL varchar(255) not null,
        HIERARCHY_NODE_ID varchar(255) not null,
        DISPLAY_ROWS int null,
        SCALE_DISPLAY_SETTING varchar(255) null,
        USES_NA tinyint null,
        USES_COMMENT tinyint null,
        COMPULSORY tinyint null,
        BLOCK_PARENT tinyint null,
        BLOCK_ID numeric(19,0) null,
        RESULTS_SHARING varchar(255) null,
        COPY_OF numeric(19,0) null,
        HIDDEN tinyint null,
        AUTO_USE_TAG varchar(255) null,
        AUTO_USE_INSERT_TAG varchar(255) null,
        primary key (ID)
    );

    create table EVAL_TRANSLATION (
        ID numeric(19,0) identity not null,
        LAST_MODIFIED datetime null,
        LANGUAGE_CODE varchar(255) not null,
        OBJECT_CLASS varchar(255) not null,
        OBJECT_ID varchar(255) not null,
        FIELD_NAME varchar(255) not null,
        TRANSLATION text not null,
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
