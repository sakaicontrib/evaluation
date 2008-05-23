/**
 * $Id: ProfilerAspect.aj 1000 May 23, 2008 9:24:20 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * ProfilerAspect.aj - evaluation - May 23, 2008 9:24:20 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
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

   public static int logSummary = 100;

   pointcut evalLogicImpl():
      execution(* xxx.org.sakaiproject.evaluation.logic.*Impl.*(..)); // currently disabled => xxx. -AZ

   public Map<String, ServiceMethodProfile> profiles = new ConcurrentHashMap<String, ServiceMethodProfile>();
   private int outputCounter = 0;

   Object around() : evalLogicImpl() {
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
      log.info("PROFILING: " + serviceMethodName + " :: runTime=" + runTime + " ns :: totalCalls=" + smp.methodCalls + " :: " + sig.toLongString());

      // log a summary every 100 (or whatever logsummary is set to)
      outputCounter++;
      if (outputCounter % logSummary == 0) {
         generateLogSummary("");
      }
      // standard return
      return methodReturn;
   }

   public void generateLogSummary(String note) {
      outputCounter = 0;
      StringBuilder sb = new StringBuilder();
      List<ServiceMethodProfile> l = new ArrayList<ServiceMethodProfile>();
      l.addAll(profiles.values());
      Collections.sort(l, new PerformanceComparator());
      sb.append("PROFILING: Profile summary (Evaluation Services): " + l.size() + " methods" + note + "\n");
      for (ServiceMethodProfile profile : l) {
         sb.append("    " + profile.toString() + "\n");
      }
      log.info(sb);
   }

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
