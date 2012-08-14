--
-- Copyright 2012 Unicon (R) Licensed under the
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

-- create the tables needed for the UCB BSpace group provider
-- mysql focused but this should work for oracle as well with some tweaks

delimiter $$

CREATE TABLE `BSPACE_CLASS_ROSTER_VW` (
  `TERM_YR` int(4) NOT NULL,
  `TERM_CD` varchar(1) DEFAULT NULL,
  `COURSE_CNTL_NUM` int(5) NOT NULL,
  `STUDENT_LDAP_UID` int(10) NOT NULL,
  `UG_GRAD_FLAG` varchar(1) DEFAULT NULL,
  `ROLE_CD` varchar(1) NOT NULL,
  `UNIT` int(3) DEFAULT NULL,
  `ENROLL_STATUS` varchar(1) DEFAULT NULL,
  `PNP_FLAG` varchar(2) DEFAULT NULL,
  `WAIT_LIST_SEQ_NUM` int(11) DEFAULT NULL,
  `CREDIT_CD` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`COURSE_CNTL_NUM`,`STUDENT_LDAP_UID`,`ROLE_CD`,`TERM_YR`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

CREATE TABLE `BSPACE_COURSE_INFO_VW` (
  `TERM_YR` int(4) NOT NULL,
  `TERM_CD` varchar(1) DEFAULT NULL,
  `COURSE_CNTL_NUM` int(5) NOT NULL,
  `STUDENT_LDAP_UID` int(10) NOT NULL,
  `DEPT_NAME` varchar(7) DEFAULT NULL,
  `CATALOG_ID` varchar(7) DEFAULT NULL,
  `CATALOG_PREFIX` varchar(1) DEFAULT NULL,
  `CATALOG_ROOT` int(3) DEFAULT NULL,
  `CATALOG_SUFFIX_1` varchar(1) DEFAULT NULL,
  `CATALOG_SUFFIX_2` varchar(1) DEFAULT NULL,
  `PRIMARY_SECONDARY_CD` varchar(1) DEFAULT NULL,
  `SECTION_NUM` varchar(3) DEFAULT NULL,
  `COURSE_TITLE` varchar(65) DEFAULT NULL,
  `LOWER_RANGE_UNIT` int(3) DEFAULT NULL,
  `UPPER_RANGE_UNIT` int(3) DEFAULT NULL,
  `VARIABLE_UNIT_CD` varchar(1) DEFAULT NULL,
  `FIXED_UNIT` int(3) DEFAULT NULL,
  `INSTRUCTION_FORMAT` varchar(3) DEFAULT NULL,
  `CRED_CD` varchar(2) DEFAULT NULL,
  `ENROLL_LIMIT` int(5) DEFAULT NULL,
  `CROSS_LISTED_FLAG` varchar(1) DEFAULT NULL,
  `INSTR_FUNC` varchar(1) DEFAULT NULL,
  `SCHEDULE_PRINT_CD` varchar(1) DEFAULT NULL,
  `SECTION_CANCEL_FLAG` varchar(1) DEFAULT NULL,
  `COURSE_TITLE_SHORT` varchar(19) DEFAULT NULL,
  `CATALOG_DESCRIPTION` varchar(1300) DEFAULT NULL,
  `COURSE_OPTION` varchar(2) DEFAULT NULL,
  `DEPT_DESCRIPTION` varchar(35) DEFAULT NULL,
  PRIMARY KEY (`COURSE_CNTL_NUM`,`SECTION_NUM`,`TERM_YR`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

CREATE TABLE `BSPACE_COURSE_INSTRUCTOR_VW` (
  `TERM_YR` int(4) NOT NULL,
  `TERM_CD` varchar(1) DEFAULT NULL,
  `COURSE_CNTL_NUM` int(5) NOT NULL,
  `INSTRUCTOR_LDAP_UID` int(10) NOT NULL,
  `MULTI_ENTRY_CD` varchar(1) DEFAULT NULL,
  `SUB_TERM_CD` varchar(1) DEFAULT NULL,
  `INSTRUCTOR_FUNC` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`COURSE_CNTL_NUM`,`INSTRUCTOR_LDAP_UID`,`TERM_YR`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

CREATE TABLE `BSPACE_CROSS_LISTING_VW` (
  `TERM_YR` int(4) NOT NULL,
  `TERM_CD` varchar(1) DEFAULT NULL,
  `COURSE_CNTL_NUM` int(5) NOT NULL,
  `CROSSLIST_HASH` int(11) DEFAULT NULL,
  PRIMARY KEY (`COURSE_CNTL_NUM`,`CROSSLIST_HASH`,`TERM_YR`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

