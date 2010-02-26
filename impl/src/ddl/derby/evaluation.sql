
    create table EVAL_ADHOC_EVALUATEES (
        GROUP_ID bigint not null,
        USER_ID varchar(255) not null,
        EVALUATEES_INDEX integer not null,
        primary key (GROUP_ID, EVALUATEES_INDEX)
    );

    create table EVAL_ADHOC_GROUP (
        ID bigint not null,
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        TITLE varchar(255),
        primary key (ID)
    );

    create table EVAL_ADHOC_PARTICIPANTS (
        GROUP_ID bigint not null,
        USER_ID varchar(255) not null,
        PARTICIPANTS_INDEX integer not null,
        primary key (GROUP_ID, PARTICIPANTS_INDEX)
    );

    create table EVAL_ADHOC_USER (
        ID bigint not null,
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        EMAIL varchar(255) not null unique,
        USER_TYPE varchar(255),
        USERNAME varchar(255),
        DISPLAY_NAME varchar(255),
        primary key (ID)
    );

    create table EVAL_ANSWER (
        ID bigint not null,
        LAST_MODIFIED timestamp not null,
        TEMPLATEITEM_FK bigint not null,
        ITEM_FK bigint,
        RESPONSE_FK bigint not null,
        TEXT_ANSWER clob(255),
        NUM_ANSWER integer,
        MULTI_ANSWER_CODE varchar(255),
        ASSOCIATED_ID varchar(255),
        ASSOCIATED_TYPE varchar(255),
        COMMENT_ANSWER clob(255),
        primary key (ID)
    );

    create table EVAL_ASSIGN_GROUP (
        ID bigint not null,
        EID varchar(255),
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        group_id varchar(255) not null,
        group_type varchar(255) not null,
        INSTRUCTOR_APPROVAL smallint not null,
        INSTRUCTORS_VIEW_RESULTS smallint not null,
        STUDENTS_VIEW_RESULTS smallint not null,
        EVALUATION_FK bigint not null,
        NODE_ID varchar(255),
        primary key (ID)
    );

    create table EVAL_ASSIGN_HIERARCHY (
        ID bigint not null,
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        NODE_ID varchar(255) not null,
        INSTRUCTOR_APPROVAL smallint not null,
        INSTRUCTORS_VIEW_RESULTS smallint not null,
        STUDENTS_VIEW_RESULTS smallint not null,
        EVALUATION_FK bigint not null,
        primary key (ID)
    );

    create table EVAL_CONFIG (
        ID bigint not null,
        LAST_MODIFIED timestamp not null,
        NAME varchar(255) not null unique,
        VALUE varchar(255) not null,
        primary key (ID)
    );

    create table EVAL_EMAIL_TEMPLATE (
        ID bigint not null,
        EID varchar(255),
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        TEMPLATE_TYPE varchar(255) not null,
        SUBJECT clob(255) not null,
        MESSAGE clob(255) not null,
        defaultType varchar(255),
        primary key (ID)
    );

    create table EVAL_EVALUATION (
        ID bigint not null,
        EID varchar(255),
        LAST_MODIFIED timestamp not null,
        EVAL_TYPE varchar(255) default 'Evaluation' not null,
        OWNER varchar(255) not null,
        TITLE varchar(255) not null,
        INSTRUCTIONS clob(255),
        START_DATE timestamp not null,
        DUE_DATE timestamp,
        STOP_DATE timestamp,
        VIEW_DATE timestamp,
        STUDENT_VIEW_RESULTS smallint,
        INSTRUCTOR_VIEW_RESULTS smallint,
        STUDENTS_DATE timestamp,
        INSTRUCTORS_DATE timestamp,
        STATE varchar(255) not null,
        INSTRUCTOR_OPT varchar(255),
        REMINDER_DAYS integer not null,
        REMINDER_FROM_EMAIL varchar(255),
        TERM_ID varchar(255),
        AVAILABLE_EMAIL_SENT smallint,
        AVAILABLE_EMAIL_TEMPLATE_FK bigint,
        REMINDER_EMAIL_TEMPLATE_FK bigint,
        TEMPLATE_FK bigint not null,
        RESULTS_SHARING varchar(255) default 'visible' not null,
        BLANK_RESPONSES_ALLOWED smallint,
        MODIFY_RESPONSES_ALLOWED smallint,
        UNREGISTERED_ALLOWED smallint,
        LOCKED smallint,
        AUTH_CONTROL varchar(255),
        EVAL_CATEGORY varchar(255),
        AUTO_USE_TAG varchar(255),
        AUTO_USE_INSERTION varchar(255),
        LOCAL_SELECTOR varchar(255),
        primary key (ID)
    );

    create table EVAL_GROUPNODES (
        ID bigint not null,
        LAST_MODIFIED timestamp not null,
        NODE_ID varchar(255) not null,
        primary key (ID)
    );

    create table EVAL_GROUPNODES_GROUPS (
        ID bigint not null,
        GROUPS varchar(255) not null,
        GROUPS_INDEX integer not null,
        primary key (ID, GROUPS_INDEX)
    );

    create table EVAL_ITEM (
        ID bigint not null,
        EID varchar(255),
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        ITEM_TEXT clob(255) not null,
        DESCRIPTION clob(255),
        SHARING varchar(255) not null,
        CLASSIFICATION varchar(255) not null,
        EXPERT smallint not null,
        EXPERT_DESCRIPTION clob(255),
        SCALE_FK bigint,
        USES_NA smallint,
        USES_COMMENT smallint,
        DISPLAY_ROWS integer,
        SCALE_DISPLAY_SETTING varchar(255),
        CATEGORY varchar(255),
        LOCKED smallint,
        COPY_OF bigint,
        HIDDEN smallint,
        AUTO_USE_TAG varchar(255),
        IG_ITEM_ID bigint,
        primary key (ID)
    );

    create table EVAL_ITEMGROUP (
        ID bigint not null,
        LAST_MODIFIED timestamp,
        OWNER varchar(255) not null,
        type varchar(255) not null,
        title varchar(80) not null,
        description clob(255),
        EXPERT smallint,
        GROUP_PARENT_FK bigint,
        primary key (ID)
    );

    create table EVAL_LOCK (
        ID bigint not null,
        LAST_MODIFIED timestamp not null,
        NAME varchar(255) not null unique,
        HOLDER varchar(255) not null,
        primary key (ID)
    );

    create table EVAL_RESPONSE (
        ID bigint not null,
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        GROUP_ID varchar(255) not null,
        START_TIME timestamp not null,
        END_TIME timestamp,
        EVALUATION_FK bigint not null,
        COMMENT_RESPONSE clob(255),
        primary key (ID)
    );

    create table EVAL_SCALE (
        ID bigint not null,
        EID varchar(255),
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        TITLE varchar(255) not null,
        SCALE_MODE varchar(255) not null,
        SHARING varchar(255) not null,
        EXPERT smallint not null,
        EXPERT_DESCRIPTION clob(255),
        IDEAL varchar(255),
        LOCKED smallint,
        COPY_OF bigint,
        HIDDEN smallint,
        primary key (ID)
    );

    create table EVAL_SCALE_OPTIONS (
        ID bigint not null,
        SCALE_OPTION varchar(255) not null,
        SCALE_OPTION_INDEX integer not null,
        primary key (ID, SCALE_OPTION_INDEX)
    );

    create table EVAL_TAGS (
        ID bigint not null,
        LAST_MODIFIED timestamp,
        TAG varchar(255) not null,
        ENTITY_TYPE varchar(255) not null,
        ENTITY_ID varchar(255) not null,
        primary key (ID)
    );

    create table EVAL_TAGS_META (
        ID bigint not null,
        LAST_MODIFIED timestamp,
        OWNER varchar(255) not null,
        TAG varchar(255) not null,
        TITLE varchar(255),
        DESCRIPTION clob(255),
        primary key (ID)
    );

    create table EVAL_TEMPLATE (
        ID bigint not null,
        EID varchar(255),
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        TYPE varchar(255) not null,
        TITLE varchar(255) not null,
        DESCRIPTION clob(255),
        SHARING varchar(255) not null,
        EXPERT smallint not null,
        expertDescription clob(255),
        LOCKED smallint,
        COPY_OF bigint,
        HIDDEN smallint,
        AUTO_USE_TAG varchar(255),
        primary key (ID)
    );

    create table EVAL_TEMPLATEITEM (
        ID bigint not null,
        EID varchar(255),
        LAST_MODIFIED timestamp not null,
        OWNER varchar(255) not null,
        template_id bigint not null,
        item_id bigint not null,
        DISPLAY_ORDER integer not null,
        ITEM_CATEGORY varchar(255) not null,
        HIERARCHY_LEVEL varchar(255) not null,
        HIERARCHY_NODE_ID varchar(255) not null,
        DISPLAY_ROWS integer,
        SCALE_DISPLAY_SETTING varchar(255),
        USES_NA smallint,
        USES_COMMENT smallint,
        BLOCK_PARENT smallint,
        BLOCK_ID bigint,
        RESULTS_SHARING varchar(255),
        COPY_OF bigint,
        HIDDEN smallint,
        AUTO_USE_TAG varchar(255),
        AUTO_USE_INSERT_TAG varchar(255),
        primary key (ID)
    );

    create table EVAL_TRANSLATION (
        ID bigint not null,
        LAST_MODIFIED timestamp,
        LANGUAGE_CODE varchar(255) not null,
        OBJECT_CLASS varchar(255) not null,
        OBJECT_ID varchar(255) not null,
        FIELD_NAME varchar(255) not null,
        TRANSLATION clob(255) not null,
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

    create index eval_answer_num on EVAL_ANSWER (NUM_ANSWER);

    alter table EVAL_ANSWER 
        add constraint ANSWER_ITEM_FKC 
        foreign key (ITEM_FK) 
        references EVAL_ITEM;

    alter table EVAL_ANSWER 
        add constraint ANSWER_TEMPLATEITEM_FKC 
        foreign key (TEMPLATEITEM_FK) 
        references EVAL_TEMPLATEITEM;

    alter table EVAL_ANSWER 
        add constraint ANSWER_RESPONSE_FKC 
        foreign key (RESPONSE_FK) 
        references EVAL_RESPONSE;

    create index eval_assign_groupid on EVAL_ASSIGN_GROUP (group_id);

    create index eval_assigngroup_eid on EVAL_ASSIGN_GROUP (EID);

    create index eval_assign_group_nodeid on EVAL_ASSIGN_GROUP (NODE_ID);

    alter table EVAL_ASSIGN_GROUP 
        add constraint ASSIGN_COURSE_EVALUATION_FKC 
        foreign key (EVALUATION_FK) 
        references EVAL_EVALUATION;

    create index eval_assign_hier_nodeid on EVAL_ASSIGN_HIERARCHY (NODE_ID);

    alter table EVAL_ASSIGN_HIERARCHY 
        add constraint ASSIGN_HIER_EVALUATION_FKC 
        foreign key (EVALUATION_FK) 
        references EVAL_EVALUATION;

    create index eval_config_name on EVAL_CONFIG (NAME);

    create index eval_templ_type on EVAL_EMAIL_TEMPLATE (TEMPLATE_TYPE);

    create index eval_templ_owner on EVAL_EMAIL_TEMPLATE (OWNER);

    create index eval_emailtemplate_eid on EVAL_EMAIL_TEMPLATE (EID);

    create index eval_eval_term on EVAL_EVALUATION (TERM_ID);

    create index eval_eval_startdate on EVAL_EVALUATION (START_DATE);

    create index eval_eval_owner on EVAL_EVALUATION (OWNER);

    create index eval_eval_state on EVAL_EVALUATION (STATE);

    create index eval_eval_duedate on EVAL_EVALUATION (DUE_DATE);

    create index eval_eval_category on EVAL_EVALUATION (EVAL_CATEGORY);

    create index eval_eval_viewdate on EVAL_EVALUATION (VIEW_DATE);

    create index eval_evaluation_eid on EVAL_EVALUATION (EID);

    create index eval_eval_type on EVAL_EVALUATION (EVAL_TYPE);

    alter table EVAL_EVALUATION 
        add constraint EVALUATION_AVAILABLE_EMAIL_TEM 
        foreign key (AVAILABLE_EMAIL_TEMPLATE_FK) 
        references EVAL_EMAIL_TEMPLATE;

    alter table EVAL_EVALUATION 
        add constraint EVALUATION_TEMPLATE_FKC 
        foreign key (TEMPLATE_FK) 
        references EVAL_TEMPLATE;

    alter table EVAL_EVALUATION 
        add constraint EVALUATION_REMINDER_EMAIL_TEMP 
        foreign key (REMINDER_EMAIL_TEMPLATE_FK) 
        references EVAL_EMAIL_TEMPLATE;

    create index eval_group_nodeid on EVAL_GROUPNODES (NODE_ID);

    alter table EVAL_GROUPNODES_GROUPS 
        add constraint FK2248663E2E2E0ED0 
        foreign key (ID) 
        references EVAL_GROUPNODES;

    create index eval_item_eid on EVAL_ITEM (EID);

    create index eval_item_sharing on EVAL_ITEM (SHARING);

    create index eval_item_owner on EVAL_ITEM (OWNER);

    create index eval_item_expert on EVAL_ITEM (EXPERT);

    alter table EVAL_ITEM 
        add constraint FKD19984D6F71AFC2F 
        foreign key (IG_ITEM_ID) 
        references EVAL_ITEMGROUP;

    alter table EVAL_ITEM 
        add constraint ITEM_SCALE_FKC 
        foreign key (SCALE_FK) 
        references EVAL_SCALE;

    create index eval_itemgroup_type on EVAL_ITEMGROUP (type);

    create index eval_itemgroup_expert on EVAL_ITEMGROUP (EXPERT);

    create index eval_itemgroup_owner on EVAL_ITEMGROUP (OWNER);

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

    create index eval_scale_mode on EVAL_SCALE (SCALE_MODE);

    create index eval_scale_eid on EVAL_SCALE (EID);

    create index eval_scale_sharing on EVAL_SCALE (SHARING);

    create index eval_scale_owner on EVAL_SCALE (OWNER);

    alter table EVAL_SCALE_OPTIONS 
        add constraint FKE8093A06DEE55A42 
        foreign key (ID) 
        references EVAL_SCALE;

    create index eval_tags_tag on EVAL_TAGS (TAG);

    create index eval_tagsmeta_owner on EVAL_TAGS_META (OWNER);

    create index eval_tagsmeta_tag on EVAL_TAGS_META (TAG);

    create index eval_template_owner on EVAL_TEMPLATE (OWNER);

    create index eval_template_eid on EVAL_TEMPLATE (EID);

    create index eval_template_sharing on EVAL_TEMPLATE (SHARING);

    create index eval_templateitem_eid on EVAL_TEMPLATEITEM (EID);

    create index eval_templateitem_blockid on EVAL_TEMPLATEITEM (BLOCK_ID);

    create index eval_templateitem_owner on EVAL_TEMPLATEITEM (OWNER);

    alter table EVAL_TEMPLATEITEM 
        add constraint FK35F30150B6DB815D 
        foreign key (item_id) 
        references EVAL_ITEM;

    alter table EVAL_TEMPLATEITEM 
        add constraint FK35F30150EDECBA7D 
        foreign key (template_id) 
        references EVAL_TEMPLATE;

    create index eval_trans_class on EVAL_TRANSLATION (OBJECT_CLASS);

    create index eval_trans_objectid on EVAL_TRANSLATION (OBJECT_ID);

    create index eval_trans_field on EVAL_TRANSLATION (FIELD_NAME);

    create index eval_trans_langcode on EVAL_TRANSLATION (LANGUAGE_CODE);

    create table hibernate_unique_key (
         next_hi integer 
    );

    insert into hibernate_unique_key values ( 0 );
