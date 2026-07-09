/**
 * Copyright 2005 Sakai Foundation Licensed under the
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
package org.sakaiproject.evaluation.test;

import java.util.List;
import java.util.Properties;

import org.sakaiproject.evaluation.dao.EvaluationDaoBase;
import org.sakaiproject.evaluation.dao.PreloadDataImpl;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;
import org.sakaiproject.evaluation.test.mocks.MockEvalJobLogic;
import org.sakaiproject.evaluation.test.mocks.MockExternalHierarchyLogic;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring test fixture for evaluation service and DAO tests.
 */
@Configuration
@EnableTransactionManagement
@ImportResource({
        "classpath:org/sakaiproject/evaluation/spring-hibernate.xml",
        "classpath:org/sakaiproject/evaluation/logic-support.xml",
        "classpath:org/sakaiproject/evaluation/logic-services.xml"
})
@PropertySource("classpath:/hibernate.properties")
public class EvaluationServiceTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    @Qualifier("evaluation.hbmMappingList")
    private List<String> hbmMappingList;

    @Override
    protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
        AdditionalHibernateMappingsImpl mappings = new AdditionalHibernateMappingsImpl();
        mappings.setMappingResources(hbmMappingList.toArray(new String[hbmMappingList.size()]));
        return mappings;
    }

    @Override
    @Bean
    public Properties hibernateProperties() {
        Properties properties = super.hibernateProperties();
        properties.setProperty("hibernate.query.substitutions", "true 1, false 0");
        return properties;
    }

    @Bean(name = "org.sakaiproject.evaluation.logic.externals.EvalExternalLogic")
    public MockEvalExternalLogic evalExternalLogic() {
        return new MockEvalExternalLogic();
    }

    @Bean(name = "org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic")
    public MockExternalHierarchyLogic externalHierarchyLogic() {
        return new MockExternalHierarchyLogic();
    }

    @Bean(name = "org.sakaiproject.evaluation.logic.EvalJobLogic")
    public EvalJobLogic evalJobLogic() {
        return new MockEvalJobLogic();
    }

    @Bean(name = "org.sakaiproject.evaluation.logic.EvalEmailsLogic", initMethod = "init")
    public EvalEmailsLogic evalEmailsLogic(
            EvalCommonLogic commonLogic,
            EvalSettings settings,
            EvalEvaluationService evaluationService) {
        EvalEmailsLogicImpl emailsLogic = new EvalEmailsLogicImpl();
        emailsLogic.setCommonLogic(commonLogic);
        emailsLogic.setSettings(settings);
        emailsLogic.setEvaluationService(evaluationService);
        return emailsLogic;
    }

    @Bean(name = "org.sakaiproject.evaluation.test.PreloadTestData", initMethod = "init")
    public PreloadTestDataImpl preloadTestData(EvaluationDaoBase persistence, PreloadDataImpl preloadData) {
        PreloadTestDataImpl preloadTestData = new PreloadTestDataImpl();
        preloadTestData.setPersistence(persistence);
        preloadTestData.setPreloadData(preloadData);
        return preloadTestData;
    }
}
