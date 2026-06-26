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
package org.sakaiproject.evaluation.tool.controllers;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.logic.exceptions.BlankRequiredFieldException;
import org.sakaiproject.evaluation.logic.exceptions.InvalidDatesException;
import org.sakaiproject.evaluation.logic.exceptions.InvalidEvalCategoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Several service-layer save methods (saveItem, saveScale, saveTemplate,
 * saveEvaluation, evalBeanUtils date/category validation) throw
 * BlankRequiredFieldException, InvalidDatesException or InvalidEvalCategoryException
 * when the user submits invalid input. These are user-input validation failures,
 * not bugs, but most controllers that call into them have no try/catch around the
 * save call, so the exception used to propagate uncaught and the user saw a raw
 * stack trace instead of a validation message.
 *
 * This sends the user back to the form they submitted (the Referer) with a
 * flash "errorMessage" instead. ModifyBlockController and EvaluationSettingsController
 * already validate/catch these themselves and are unaffected by this handler.
 */
@Slf4j
@ControllerAdvice
public class EvalValidationExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(BlankRequiredFieldException.class)
    public String handleBlankRequiredField(BlankRequiredFieldException ex, HttpServletRequest request,
                                            RedirectAttributes redirectAttrs) {
        return redirectWithError(ex.messageKey, new Object[]{ ex.fieldName }, ex, request, redirectAttrs);
    }

    @ExceptionHandler(InvalidDatesException.class)
    public String handleInvalidDates(InvalidDatesException ex, HttpServletRequest request,
                                      RedirectAttributes redirectAttrs) {
        return redirectWithError(ex.messageKey, new Object[]{ ex.dateField }, ex, request, redirectAttrs);
    }

    @ExceptionHandler(InvalidEvalCategoryException.class)
    public String handleInvalidEvalCategory(InvalidEvalCategoryException ex, HttpServletRequest request,
                                             RedirectAttributes redirectAttrs) {
        return redirectWithError(ex.messageKey, null, ex, request, redirectAttrs);
    }

    private String redirectWithError(String messageKey, Object[] args, RuntimeException ex,
                                      HttpServletRequest request, RedirectAttributes redirectAttrs) {
        Locale locale = RequestContextUtils.getLocale(request);
        String message;
        try {
            message = messageSource.getMessage(messageKey, args, locale);
        } catch (Exception resolveFailure) {
            message = ex.getMessage();
        }
        log.info("Rejected invalid user input on {}: {}", request.getRequestURI(), message);
        redirectAttrs.addFlashAttribute("errorMessage", message);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/summary");
    }
}
