
    create table EVAL_ADHOC_EVALUATEES (
        GROUP_ID number(19,0) not null,
        USER_ID varchar2(255) not null,
        EVALUATEES_INDEX number(10,0) not null,
        primary key (GROUP_ID, EVALUATEES_INDEX)
    );

    create table EVAL_ADHOC_GROUP (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        TITLE varchar2(255),
        primary key (ID)
    );

    create table EVAL_ADHOC_PARTICIPANTS (
        GROUP_ID number(19,0) not null,
        USER_ID varchar2(255) not null,
        PARTICIPANTS_INDEX number(10,0) not null,
        primary key (GROUP_ID, PARTICIPANTS_INDEX)
    );

    create table EVAL_ADHOC_USER (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        EMAIL varchar2(255) not null unique,
        USER_TYPE varchar2(255),
        USERNAME varchar2(255) unique,
        DISPLAY_NAME varchar2(255),
        primary key (ID)
    );

    create table EVAL_ANSWER (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        TEMPLATEITEM_FK number(19,0) not null,
        ITEM_FK number(19,0),
        RESPONSE_FK number(19,0) not null,
        TEXT_ANSWER clob,
        NUM_ANSWER number(10,0),
        MULTI_ANSWER_CODE varchar2(255),
        ASSOCIATED_ID varchar2(255),
        ASSOCIATED_TYPE varchar2(255),
        COMMENT_ANSWER clob,
        primary key (ID)
    );

    create table EVAL_ASSIGN_GROUP (
        ID number(19,0) not null,
        EID varchar2(255),
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        group_id varchar2(255) not null,
        group_type varchar2(255) not null,
        INSTRUCTOR_APPROVAL number(1,0) not null,
        INSTRUCTORS_VIEW_RESULTS number(1,0) not null,
        STUDENTS_VIEW_RESULTS number(1,0) not null,
        EVALUATION_FK number(19,0) not null,
        NODE_ID varchar2(255),
        primary key (ID)
    );

    create table EVAL_ASSIGN_HIERARCHY (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        NODE_ID varchar2(255) not null,
        INSTRUCTOR_APPROVAL number(1,0) not null,
        INSTRUCTORS_VIEW_RESULTS number(1,0) not null,
        STUDENTS_VIEW_RESULTS number(1,0) not null,
        EVALUATION_FK number(19,0) not null,
        primary key (ID)
    );

    create table EVAL_CONFIG (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        NAME varchar2(255) not null unique,
        VALUE varchar2(255) not null,
        primary key (ID)
    );

    create table EVAL_EMAIL_TEMPLATE (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        TEMPLATE_TYPE varchar2(255) not null,
        SUBJECT clob not null,
        MESSAGE clob not null,
        defaultType varchar2(255),
        primary key (ID)
    );

    create table EVAL_EVALUATION (
        ID number(19,0) not null,
        EID varchar2(255),
        LAST_MODIFIED date not null,
        EVAL_TYPE varchar2(255) default 'Evaluation' not null,
        OWNER varchar2(255) not null,
        TITLE varchar2(255) not null,
        INSTRUCTIONS clob,
        START_DATE date not null,
        DUE_DATE date,
        STOP_DATE date,
        VIEW_DATE date,
        STUDENT_VIEW_RESULTS number(1,0),
        INSTRUCTOR_VIEW_RESULTS number(1,0),
        STUDENTS_DATE date,
        INSTRUCTORS_DATE date,
        STATE varchar2(255) not null,
        INSTRUCTOR_OPT varchar2(255),
        REMINDER_DAYS number(10,0) not null,
        REMINDER_FROM_EMAIL varchar2(255),
        TERM_ID varchar2(255),
        AVAILABLE_EMAIL_TEMPLATE_FK number(19,0),
        REMINDER_EMAIL_TEMPLATE_FK number(19,0),
        TEMPLATE_FK number(19,0) not null,
        RESULTS_SHARING varchar2(255) default 'visible' not null,
        BLANK_RESPONSES_ALLOWED number(1,0),
        MODIFY_RESPONSES_ALLOWED number(1,0),
        UNREGISTERED_ALLOWED number(1,0),
        LOCKED number(1,0),
        AUTH_CONTROL varchar2(255),
        EVAL_CATEGORY varchar2(255),
        AUTO_USE_TAG varchar2(255),
        AUTO_USE_INSERTION varchar2(255),
        primary key (ID)
    );

    create table EVAL_GROUPNODES (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        NODE_ID varchar2(255) not null,
        primary key (ID)
    );

    create table EVAL_GROUPNODES_GROUPS (
        ID number(19,0) not null,
        GROUPS varchar2(255) not null,
        GROUPS_INDEX number(10,0) not null,
        primary key (ID, GROUPS_INDEX)
    );

    create table EVAL_ITEM (
        ID number(19,0) not null,
        EID varchar2(255),
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        ITEM_TEXT clob not null,
        DESCRIPTION clob,
        SHARING varchar2(255) not null,
        CLASSIFICATION varchar2(255) not null,
        EXPERT number(1,0) not null,
        EXPERT_DESCRIPTION clob,
        SCALE_FK number(19,0),
        USES_NA number(1,0),
        USES_COMMENT number(1,0),
        DISPLAY_ROWS number(10,0),
        SCALE_DISPLAY_SETTING varchar2(255),
        CATEGORY varchar2(255),
        LOCKED number(1,0),
        COPY_OF number(19,0),
        HIDDEN number(1,0),
        AUTO_USE_TAG varchar2(255),
        IG_ITEM_ID number(19,0),
        primary key (ID)
    );

    create table EVAL_ITEMGROUP (
        ID number(19,0) not null,
        LAST_MODIFIED date,
        OWNER varchar2(255) not null,
        type varchar2(255) not null,
        title varchar2(80) not null,
        description clob,
        EXPERT number(1,0),
        GROUP_PARENT_FK number(19,0),
        primary key (ID)
    );

    create table EVAL_LOCK (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        NAME varchar2(255) not null unique,
        HOLDER varchar2(255) not null,
        primary key (ID)
    );

    create table EVAL_RESPONSE (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        GROUP_ID varchar2(255) not null,
        START_TIME date not null,
        END_TIME date,
        EVALUATION_FK number(19,0) not null,
        COMMENT_RESPONSE clob,
        primary key (ID)
    );

    create table EVAL_SCALE (
        ID number(19,0) not null,
        EID varchar2(255),
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        TITLE varchar2(255) not null,
        SCALE_MODE varchar2(255) not null,
        SHARING varchar2(255) not null,
        EXPERT number(1,0) not null,
        EXPERT_DESCRIPTION clob,
        IDEAL varchar2(255),
        LOCKED number(1,0),
        COPY_OF number(19,0),
        HIDDEN number(1,0),
        primary key (ID)
    );

    create table EVAL_SCALE_OPTIONS (
        ID number(19,0) not null,
        SCALE_OPTION varchar2(255) not null,
        SCALE_OPTION_INDEX number(10,0) not null,
        primary key (ID, SCALE_OPTION_INDEX)
    );

    create table EVAL_TAGS (
        ID number(19,0) not null,
        LAST_MODIFIED date,
        TAG varchar2(255) not null,
        ENTITY_TYPE varchar2(255) not null,
        ENTITY_ID varchar2(255) not null,
        primary key (ID)
    );

    create table EVAL_TAGS_META (
        ID number(19,0) not null,
        LAST_MODIFIED date,
        OWNER varchar2(255) not null,
        TAG varchar2(255) not null,
        TITLE varchar2(255),
        DESCRIPTION clob,
        primary key (ID)
    );

    create table EVAL_TEMPLATE (
        ID number(19,0) not null,
        EID varchar2(255),
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        TYPE varchar2(255) not null,
        TITLE varchar2(255) not null,
        DESCRIPTION clob,
        SHARING varchar2(255) not null,
        EXPERT number(1,0) not null,
        expertDescription clob,
        LOCKED number(1,0),
        COPY_OF number(19,0),
        HIDDEN number(1,0),
        AUTO_USE_TAG varchar2(255),
        primary key (ID)
    );

    create table EVAL_TEMPLATEITEM (
        ID number(19,0) not null,
        EID varchar2(255),
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        template_id number(19,0) not null,
        item_id number(19,0) not null,
        DISPLAY_ORDER number(10,0) not null,
        ITEM_CATEGORY varchar2(255) not null,
        HIERARCHY_LEVEL varchar2(255) not null,
        HIERARCHY_NODE_ID varchar2(255) not null,
        DISPLAY_ROWS number(10,0),
        SCALE_DISPLAY_SETTING varchar2(255),
        USES_NA number(1,0),
        USES_COMMENT number(1,0),
        BLOCK_PARENT number(1,0),
        BLOCK_ID number(19,0),
        RESULTS_SHARING varchar2(255),
        COPY_OF number(19,0),
        HIDDEN number(1,0),
        AUTO_USE_TAG varchar2(255),
        AUTO_USE_INSERT_TAG varchar2(255),
        primary key (ID)
    );

    create table EVAL_TRANSLATION (
        ID number(19,0) not null,
        LAST_MODIFIED date,
        LANGUAGE_CODE varchar2(255) not null,
        OBJECT_CLASS varchar2(255) not null,
        OBJECT_ID varchar2(255) not null,
        FIELD_NAME varchar2(255) not null,
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

    create sequence hibernate_sequence;
