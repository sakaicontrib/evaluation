/**
 * Copyright 2012 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.provider;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;


/**
 * DAO for the provider (uses spring JDBC)
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class UCBEvalGroupsProviderDao {

    private static Log log = LogFactory.getLog(UCBEvalGroupsProviderDao.class);

    public static String COURSES_TABLE = "BSPACE_COURSE_INFO_VW";
    public static String MEMBERS_TABLE = "BSPACE_CLASS_ROSTER_VW";
    public static String INSTRUCTORS_TABLE = "BSPACE_COURSE_INSTRUCTOR_VW";
    public static String CROSSLIST_TABLE = "BSPACE_CROSS_LISTING_VW";

    private SimpleJdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    public SimpleJdbcTemplate getSimpleJdbcTemplate() {
        return jdbcTemplate;
    }

    public int getCoursesCount() {
        String sql = "SELECT COUNT(*) FROM "+COURSES_TABLE;
        return jdbcTemplate.queryForInt(sql, new Object[] {});
    }

    public int getMembersCount() {
        String sql = "SELECT COUNT(*) FROM "+MEMBERS_TABLE;
        return jdbcTemplate.queryForInt(sql, new Object[] {});
    }

    public int getInstructorsCount() {
        String sql = "SELECT COUNT(*) FROM "+INSTRUCTORS_TABLE;
        return jdbcTemplate.queryForInt(sql, new Object[] {});
    }

    public int getCrosslistCount() {
        String sql = "SELECT COUNT(*) FROM "+CROSSLIST_TABLE;
        return jdbcTemplate.queryForInt(sql, new Object[] {});
    }

    public List<Map<String, Object>> getCourses() {
        String sql = "SELECT * FROM "+COURSES_TABLE;
        List<Map<String, Object>> courses = jdbcTemplate.queryForList(sql, new Object[] {});
        fixRequiredColumns(courses);
        return courses;
    }

    /**
     * BSPACE_CLASS_ROSTER_VW table will only have students. 
     * Do not need to filter on the ROLE_CD column.
     * Need to filter on "ENROLL_STATUS":
     * "E" for Enrolled
     * "W" for Waitlisted
     * "D" for Dropped at the beginning of the term
     * 
     * @return list of rows which are a map of column->value
     */
    public List<Map<String, Object>> getStudents() {
        String sql = "SELECT * FROM "+MEMBERS_TABLE+" WHERE ENROLL_STATUS=?";
        List<Map<String, Object>> studs = jdbcTemplate.queryForList(sql, new Object[] {"E"});
        fixRequiredColumns(studs);
        return studs;
    }

    /**
     * The instructors/GSIs are listed in the BSPACE_COURSE_INSTRUCTOR_VW table. The thing to check there is the "INSTRUCTOR_FUNC":
     * 1 = teaching and in charge
     * 2 = teaching but not in charge
     * 3 = not teaching but in charge
     * 4 = no valid teaching title
     * 
     * @return list of rows which are a map of column->value
     */
    public List<Map<String, Object>> getInstructors() {
        String sql = "SELECT * FROM "+INSTRUCTORS_TABLE+" WHERE (INSTRUCTOR_FUNC=? OR INSTRUCTOR_FUNC=?)";
        List<Map<String, Object>> insts = jdbcTemplate.queryForList(sql, new Object[] {1,2});
        fixRequiredColumns(insts);
        return insts;
    }

    private void fixRequiredColumns(List<Map<String, Object>> data) {
        for (Map<String, Object> row : data) {
            if (row.get("TERM_YR") == null || StringUtils.isEmpty( row.get("TERM_YR").toString() )) {
                log.warn("Invalid term year ("+row.get("TERM_YR")+"), using the default of 2012");
                row.put("TERM_YR", 2012);
            }
            if (row.get("TERM_CD") == null || StringUtils.isEmpty( row.get("TERM_CD").toString() )) {
                log.warn("Invalid TERM_CD ("+row.get("TERM_CD")+"), using the default of ?");
                row.put("TERM_CD", "?");
            }
            if (row.get("COURSE_CNTL_NUM") == null || StringUtils.isEmpty( row.get("COURSE_CNTL_NUM").toString() )) {
                log.warn("Invalid COURSE_CNTL_NUM ("+row.get("COURSE_CNTL_NUM")+"), using the default of 0");
                row.put("COURSE_CNTL_NUM", 0);
            }
        }
    }

}
