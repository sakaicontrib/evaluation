/**
 * Copyright 2026 Sakai Foundation Licensed under the
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

package org.sakaiproject.evaluation.spring;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;

/**
 * Bean-name transaction proxy creator for the legacy evaluation component
 * context.
 * <p>
 * Evaluation still uses XML bean-name transaction proxying. In Sakai, those
 * component beans must remain visible through the component classloader, so
 * this class forces Spring's generated proxies to use that loader. Keep this
 * limited to that classloader bridge; do not add DAO or transaction policy
 * behavior here.
 */
public class SakaiComponentBeanNameAutoProxyCreator extends BeanNameAutoProxyCreator {

    private static final long serialVersionUID = 1L;

    private final ClassLoader componentClassLoader = SakaiComponentBeanNameAutoProxyCreator.class.getClassLoader();

    public SakaiComponentBeanNameAutoProxyCreator() {
        super.setProxyClassLoader(componentClassLoader);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        super.setBeanClassLoader(componentClassLoader);
    }
}
