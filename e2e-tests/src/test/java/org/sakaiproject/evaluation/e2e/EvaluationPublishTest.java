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
package org.sakaiproject.evaluation.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.WaitUntilState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EvaluationPublishTest {

    private static final String TOOL_CONTEXT = "/sakai-evaluation-tool";
    private static final Path ARTIFACT_ROOT = Path.of("target", "playwright-artifacts");
    private static final DateTimeFormatter DATE_TIME_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;
    private Path artifactDir;

    @BeforeAll
    static void launchBrowser() throws Exception {
        Files.createDirectories(ARTIFACT_ROOT);
        playwright = Playwright.create();
        browser = browserType(playwright, browserName()).launch(new BrowserType.LaunchOptions()
                .setHeadless(headless()));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void createContext() throws Exception {
        artifactDir = ARTIFACT_ROOT.resolve("EvaluationPublishTest-" + Instant.now().toEpochMilli());
        Files.createDirectories(artifactDir);

        context = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL(baseUrl())
                .setIgnoreHTTPSErrors(true));
        context.setDefaultTimeout(30_000);
        context.setDefaultNavigationTimeout(120_000);
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        try {
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(artifactDir.resolve("final.png"))
                    .setFullPage(true));
        } catch (RuntimeException e) {
            // Preserve the test failure and close the browser context.
        }
        try {
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(artifactDir.resolve("trace.zip")));
        } catch (RuntimeException e) {
            // Preserve the test failure and close the browser context.
        }
        context.close();
    }

    @Test
    void adminCanCreateAndPublishAnonymousEvaluation() {
        String suffix = String.valueOf(Instant.now().toEpochMilli());
        String templateTitle = "Playwright Template " + suffix;
        String evaluationTitle = "Playwright Published Evaluation " + suffix;

        loginAsAdmin();

        String templateId = createTemplate(templateTitle);
        addTextItem(templateId, "What worked well in this course?");

        String evaluationId = createEvaluation(templateId, evaluationTitle);
        saveEvaluationSettings(evaluationId, evaluationTitle);
        publishEvaluationWithoutAssignments(evaluationId);

        gotoToolPath("/control_evaluations?maxAgeToDisplay=12");
        assertThat(page.locator("body")).containsText(evaluationTitle);
        assertThat(page.locator(".inqueueTable, .activeTable").filter(new Locator.FilterOptions()
                .setHasText(evaluationTitle))).isVisible();
    }

    private void loginAsAdmin() {
        page.navigate("/portal/", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        Locator userId = page.locator("input[name=\"eid\"], #eid").first();
        assertThat(userId).isVisible();
        userId.fill("admin");
        page.locator("input[name=\"pw\"], #pw").first().fill("admin");
        page.locator("#submit, button[type=\"submit\"], input[type=\"submit\"]").first().click();
        page.waitForLoadState();
        assertThat(page.locator("body")).containsText(Pattern.compile("Administration Workspace|Home"));
    }

    private String createTemplate(String templateTitle) {
        gotoToolPath("/modify_template");
        page.locator("input[name=\"title\"]").fill(templateTitle);
        page.locator("textarea[name=\"description\"]").fill("Created by Playwright e2e.");
        submitForm("#basic-form");
        page.waitForURL(Pattern.compile(".*/modify_template_items\\?templateId=\\d+.*"));
        assertThat(page.locator("body")).containsText(templateTitle);
        return queryParam(page.url(), "templateId");
    }

    private void addTextItem(String templateId, String itemText) {
        gotoToolPath("/modify_item?itemClassification=Essay&templateId=" + templateId);
        setFormValue("textarea[name=\"itemText\"]", itemText);
        page.locator("select[name=\"displayRows\"]").selectOption("2");
        Locator courseCategory = page.locator("input[name=\"category\"][value=\"course\"]");
        if (courseCategory.count() > 0) {
            courseCategory.check();
        }
        submitForm("#item-form");
        page.waitForURL(Pattern.compile(".*/modify_template_items\\?templateId=" + Pattern.quote(templateId) + ".*"));
        assertThat(page.locator("body")).containsText(itemText);
    }

    private String createEvaluation(String templateId, String evaluationTitle) {
        gotoToolPath("/evaluation_create?templateId=" + templateId);
        setFormValue("input[name=\"title\"]", evaluationTitle);
        setFormValue("textarea[name=\"instructions\"]", "Please complete this test evaluation.");
        submitForm("form");
        page.waitForURL(Pattern.compile(".*/evaluation_settings\\?evaluationId=\\d+.*"));
        return queryParam(page.url(), "evaluationId");
    }

    private void saveEvaluationSettings(String evaluationId, String evaluationTitle) {
        LocalDateTime now = LocalDateTime.now();
        gotoToolPath("/evaluation_settings?evaluationId=" + evaluationId);
        setFormValue("input[name=\"title\"]", evaluationTitle);
        setFormValue("textarea[name=\"instructions\"]", "Published by Playwright e2e.");
        page.locator("input[name=\"startDate\"]").fill(now.plusMinutes(10).format(DATE_TIME_LOCAL));
        page.locator("input[name=\"dueDate\"]").fill(now.plusDays(2).format(DATE_TIME_LOCAL));
        Locator authControl = page.locator("select[name=\"authControl\"]");
        if (authControl.count() > 0 && authControl.isEnabled()) {
            authControl.selectOption("NONE");
        }
        Locator reminderFrom = page.locator("input[name=\"reminderFromEmail\"]");
        if (reminderFrom.count() > 0 && reminderFrom.isVisible()) {
            reminderFrom.fill("admin@example.edu");
        }
        page.locator("form").first().locator("input[type=\"submit\"]").click();
        page.waitForURL(Pattern.compile(".*/evaluation_assign\\?evaluationId=" + Pattern.quote(evaluationId) + ".*"));
    }

    private void publishEvaluationWithoutAssignments(String evaluationId) {
        page.locator("#eval-assign-form input[type=\"submit\"]").click();
        page.waitForURL(Pattern.compile(".*/evaluation_assign_confirm\\?evaluationId=" + Pattern.quote(evaluationId) + ".*"));
        assertThat(page.locator("body")).containsText("No groups selected");
        page.locator("form").first().locator("input[type=\"submit\"]").click();
        page.waitForURL(Pattern.compile(".*/control_evaluations.*"));
    }

    private void gotoToolPath(String path) {
        page.navigate(TOOL_CONTEXT + path, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    private void setFormValue(String selector, String value) {
        Locator locator = page.locator(selector).first();
        locator.evaluate("(element, text) => { element.value = text; element.dispatchEvent(new Event('input', { bubbles: true })); element.dispatchEvent(new Event('change', { bubbles: true })); }", value);
    }

    private void submitForm(String selector) {
        page.locator(selector).evaluate("form => form.submit()");
    }

    private static String queryParam(String url, String name) {
        Matcher matcher = Pattern.compile("[?&]" + Pattern.quote(name) + "=([^&#]+)").matcher(url);
        if (!matcher.find()) {
            throw new IllegalStateException("Missing query parameter " + name + " in URL: " + url);
        }
        return matcher.group(1);
    }

    private static String baseUrl() {
        String value = System.getProperty("PLAYWRIGHT_BASE_URL");
        if (value == null || value.isBlank()) {
            value = System.getenv("PLAYWRIGHT_BASE_URL");
        }
        return value == null || value.isBlank() ? "http://127.0.0.1:8080" : value;
    }

    private static String browserName() {
        String value = System.getProperty("PLAYWRIGHT_BROWSER");
        if (value == null || value.isBlank()) {
            value = System.getenv("PLAYWRIGHT_BROWSER");
        }
        return value == null || value.isBlank() ? "chromium" : value;
    }

    private static boolean headless() {
        String value = System.getProperty("PLAYWRIGHT_HEADLESS");
        if (value == null || value.isBlank()) {
            value = System.getenv("PLAYWRIGHT_HEADLESS");
        }
        return value == null || !"false".equals(value.toLowerCase(Locale.ROOT));
    }

    private static BrowserType browserType(Playwright playwrightInstance, String selectedBrowser) {
        if ("firefox".equalsIgnoreCase(selectedBrowser)) {
            return playwrightInstance.firefox();
        }
        if ("webkit".equalsIgnoreCase(selectedBrowser)) {
            return playwrightInstance.webkit();
        }
        return playwrightInstance.chromium();
    }
}
