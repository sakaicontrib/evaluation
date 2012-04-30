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
package org.sakaiproject.evaluation.aspectj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This aspect helps us profile the evaluation system logic layer code
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public aspect EvalServicesProfiler {

   private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(EvalServicesProfiler.class);
   public static int logSummary = 1000;
   public static boolean logMethodCalls = true;

   public Map<String, ServiceMethodProfile> profiles = new ConcurrentHashMap<String, ServiceMethodProfile>();
   private int outputCounter = 0;

   /**
    * pointcut on all public methods of major logic impls + EvalJobLogicImpl
    */
   pointcut evalLogicImpl():
      (execution(public * org.sakaiproject.evaluation.logic.*Impl.*(..)) ||
       execution(public * org.sakaiproject.evaluation.logic.scheduling.EvalJobLogicImpl.*(..))
   );

   /**
    * around advice for all methods in the evalLogicImpl pointcut
    */
   Object around() : evalLogicImpl() {
      if (enabled) {
         long startTime = System.nanoTime();
         Object methodReturn = proceed(); // this executes the method and gets the return value
         long runTime = System.nanoTime() - startTime;
         org.aspectj.lang.Signature sig = thisJoinPoint.getSignature(); // find out info about the method
         String serviceName = sig.getDeclaringTypeName();
         String methodName = sig.getName();
         String serviceMethodName = serviceName + ":" + methodName;
   
         ServiceMethodProfile smp = null;
         if (profiles.containsKey(serviceMethodName)) {
            smp = profiles.get(serviceMethodName);
         } else {
            smp = new ServiceMethodProfile(sig.getDeclaringTypeName(), sig.getName());
            profiles.put(serviceMethodName, smp);
         }
         // track the total methodCalls and total runTime
         smp.methodCalls = smp.methodCalls + 1;
         smp.runTime = smp.runTime + runTime;
   
         // log the method profile
         if (logMethodCalls) {
            log.info("PROFILING: " + serviceMethodName + " :: runTime=" + runTime + " ns :: totalCalls=" + smp.methodCalls + " :: " + sig.toLongString());
   
            // log a summary every 1000 (or whatever logsummary is set to)
            outputCounter++;
            if (outputCounter % logSummary == 0) {
               generateLogSummary("");
            }
         }
         // standard return
         return methodReturn;
      } else {
         // skip the logging
         return proceed();
      }
   }

   /**
    * This pointcut allows use to intercept static calls to the generateSummary method
    */
   pointcut makeSummary(): execution(public static String org.sakaiproject.evaluation.aspectj.ProfilerControl.generateSummary());

   Object around() : makeSummary() {
      // simply ignore the execution in the method are replace it with the summary from this aspect
      String summary = generateSummary( new java.util.Date().toString() );
      log.info(summary);
      return summary;
   }

   pointcut enableProfiler(): execution(public static void org.sakaiproject.evaluation.aspectj.ProfilerControl.enableProfiler(..));

   boolean enabled = false; // off by default
   after() returning() : enableProfiler() {
      if (enabled) {
         enabled = false;
      } else {
         enabled = true;
      }
      log.info("Set the profiler to enabled=" + enabled);
   }

   pointcut resetProfiler(): execution(public static void org.sakaiproject.evaluation.aspectj.ProfilerControl.resetProfiler(..));

   after() returning() : resetProfiler() {
      String summary = generateSummary( new java.util.Date().toString() );
      log.info(summary);
      profiles.clear();
      outputCounter = 0;
      log.info("Cleared the profiler data");
   }

   pointcut enableMethodLogging(): execution(public static void org.sakaiproject.evaluation.aspectj.ProfilerControl.enableMethodLogging(..));

   after() returning() : enableMethodLogging() {
      if (logMethodCalls) {
         logMethodCalls = false;
      } else {
         logMethodCalls = true;
      }
      log.info("Set the profiler to enabled=" + enabled);
   }


   public void generateLogSummary(String note) {
      log.info( generateSummary(note) );
   }

   public String generateSummary(String note) {
      outputCounter = 0;
      StringBuilder sb = new StringBuilder();
      List<ServiceMethodProfile> l = new ArrayList<ServiceMethodProfile>();
      l.addAll(profiles.values());
      Collections.sort(l, new PerformanceComparator());
      sb.append("PROFILING: Profile summary (Evaluation Services): " + l.size() + " methods" + note + "\n");
      for (ServiceMethodProfile profile : l) {
         sb.append("    " + profile.toString() + "\n");
      }
      return sb.toString();
   }

   // internal classes to support the summary reporting

   public class ServiceMethodProfile {
      public String serviceName;
      public String methodName;
      public Integer methodCalls = 0;
      public Long runTime = Long.valueOf(0);
      public ServiceMethodProfile(String serviceName, String methodName) {
         this.serviceName = serviceName;
         this.methodName = methodName;
      }
      public String getId() {
         return serviceName + ":" + methodName;
      }
      public Long getAvg() {
         return (methodCalls == 0 ? 0 : runTime / methodCalls);
      }
      @Override
      public String toString() {
         return serviceName + ":" + methodName + ": calls=" + methodCalls + ": runTime=" + runTime 
               + " ns: avgRunTimePerCall=" + getAvg() + " ns";
      }
   }

   public static class PerformanceComparator implements Comparator<ServiceMethodProfile>  {
      public int compare(ServiceMethodProfile smp0, ServiceMethodProfile smp1) {
         int comparison = 0;
         comparison = smp1.getAvg().compareTo( smp0.getAvg() );
         if (comparison == 0 ) {
            comparison = smp0.getId().compareTo( smp1.getId() );
         }
         return comparison;
      }
   }

}
