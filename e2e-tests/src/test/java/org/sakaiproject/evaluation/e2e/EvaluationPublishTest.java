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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
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
    private Frame toolFrame;
    private Path artifactDir;
    private String toolContext = TOOL_CONTEXT;

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
        toolFrame = page.mainFrame();
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
        openEvaluationThroughPortal();

        String templateId = createTemplate(templateTitle);
        addTextItem(templateId, "What worked well in this course?");

        String evaluationId = createEvaluation(templateId, evaluationTitle);
        saveEvaluationSettings(evaluationId, evaluationTitle);
        publishEvaluationWithoutAssignments(evaluationId);

        gotoToolPath("/control_evaluations?maxAgeToDisplay=12");
        assertThat(toolFrame.locator("body")).containsText(evaluationTitle);
        assertThat(toolFrame.locator(".inqueueTable, .activeTable").filter(new Locator.FilterOptions()
                .setHasText(evaluationTitle))).isVisible();
    }

    @Test
    void anonymousRequestsCannotAccessAdministration() {
        assertAdminAccessDenied();
    }

    @Test
    void studentCannotAccessAdministrationOrPrivateTemplates() {
        loginAsAdmin();
        String templateId = createTemplate("Private access test " + Instant.now().toEpochMilli());
        addTextItem(templateId, "Private question must not be exposed");
        String templateItemId = queryParam(toolFrame.locator("a[href*=\"preview_item?\"]").first()
                .getAttribute("href"), "templateItemId");
        context.clearCookies();
        loginAs("student0011", "sakai");
        assertAdminAccessDenied();
        assertPrivateTemplateAccessDenied(context, templateId);
        assertPrivateTemplateItemAccessDenied(context, templateItemId);
        try (BrowserContext anonymous = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL(baseUrl()).setIgnoreHTTPSErrors(true))) {
            assertPrivateTemplateAccessDenied(anonymous, templateId);
            assertPrivateTemplateItemAccessDenied(anonymous, templateItemId);
        }
    }

    @Test
    void adminCanOpenEvaluationThroughPortal() {
        loginAsAdmin();
        openEvaluationThroughPortal();
        toolFrame.getByRole(AriaRole.LINK, new Frame.GetByRoleOptions().setName("Administrate").setExact(true)).click();
        assertThat(toolFrame.getByRole(AriaRole.LINK, new Frame.GetByRoleOptions()
                .setName("Control Email Settings").setExact(true))).isVisible();
    }

    private void openEvaluationThroughPortal() {
        page.navigate("/portal/site/!admin");
        Locator tool = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions()
                .setName("Evaluation System").setExact(true));
        if (tool.count() == 0) {
            // Use Sakai's fixture API to install the tool on a fresh CI instance.
            APIRequestContext fixture = playwright.request().newContext(new APIRequest.NewContextOptions()
                    .setBaseURL(baseUrl()).setIgnoreHTTPSErrors(true));
            try {
                APIResponse session = fixture.post("/direct/session/new", RequestOptions.create()
                        .setForm(FormData.create().set("_username", "admin").set("_password", "admin")));
                assertTrue(session.ok(), "Fixture login failed: HTTP " + session.status());
                APIResponse added = fixture.get("/sakai-ws/rest/sakai/addToolAndPageToSite",
                        RequestOptions.create().setQueryParam("sessionid", session.text().trim())
                                .setQueryParam("siteid", "!admin").setQueryParam("toolid", "sakai.rsf.evaluation")
                                .setQueryParam("position", "99"));
                assertTrue(added.ok(), "Installing the tool in the test site must succeed");
                assertEquals("success", added.text().trim());
            } finally {
                fixture.dispose();
            }
            page.reload();
        }
        tool.click();
        Locator portalFrame = page.locator("iframe.portletMainIframe[title^='Evaluation System']");
        if (portalFrame.count() > 0) {
            toolFrame = page.frame(portalFrame.getAttribute("name"));
        }
        assertThat(toolFrame.locator("body")).containsText("Evaluations dashboard");
        String path = java.net.URI.create(toolFrame.url()).getPath();
        toolContext = path.endsWith("/summary") ? path.substring(0, path.length() - "/summary".length()) : path;
        if (toolContext.endsWith("/")) {
            toolContext = toolContext.substring(0, toolContext.length() - 1);
        }
    }

    private void assertAdminAccessDenied() {
        for (String path : new String[]{"/administrate", "/administrate_email", "/administrate_reporting",
                "/control_eval_admin", "/export_settings"}) {
            APIResponse response = context.request().get(TOOL_CONTEXT + path);
            assertEquals(403, response.status(), path);
        }
        APIResponse response = context.request().post(TOOL_CONTEXT + "/administrate",
                RequestOptions.create().setForm(FormData.create()));
        assertEquals(403, response.status(), "Unauthorized settings writes must be denied");
    }

    private void assertPrivateTemplateAccessDenied(BrowserContext requester, String templateId) {
        for (String path : new String[]{"/modify_template", "/modify_template_items", "/preview_eval", "/remove_template", "/evaluation_create"}) {
            APIResponse response = requester.request().get(TOOL_CONTEXT + path + "?templateId=" + templateId);
            assertEquals(403, response.status(), path);
        }
    }

    private void assertPrivateTemplateItemAccessDenied(BrowserContext requester, String templateItemId) {
        for (String path : new String[]{"/modify_item", "/preview_item", "/remove_item"}) {
            APIResponse response = requester.request().get(TOOL_CONTEXT + path + "?templateItemId=" + templateItemId);
            assertEquals(403, response.status(), path);
        }
    }

    private void loginAsAdmin() {
        loginAs("admin", "admin");
    }

    private void loginAs(String username, String password) {

        page.navigate("/portal/", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        Locator userId = toolFrame.locator("input[name=\"eid\"], #eid").first();
        assertThat(userId).isVisible();
        userId.fill(username);
        toolFrame.locator("input[name=\"pw\"], #pw").first().fill(password);
        toolFrame.locator("#submit, button[type=\"submit\"], input[type=\"submit\"]").first().click();
        page.waitForLoadState();
        assertThat(toolFrame.locator("body")).containsText(Pattern.compile("Administration Workspace|Home"));
    }

    private String createTemplate(String templateTitle) {
        gotoToolPath("/modify_template");
        toolFrame.locator("input[name=\"title\"]").fill(templateTitle);
        toolFrame.locator("textarea[name=\"description\"]").fill("Created by Playwright e2e.");
        submitForm("#basic-form");
        toolFrame.waitForURL(Pattern.compile(".*/modify_template_items\\?templateId=\\d+.*"));
        assertThat(toolFrame.locator("body")).containsText(templateTitle);
        return queryParam(toolFrame.url(), "templateId");
    }

    private void addTextItem(String templateId, String itemText) {
        gotoToolPath("/modify_item?itemClassification=Essay&templateId=" + templateId);
        setFormValue("textarea[name=\"itemText\"]", itemText);
        toolFrame.locator("select[name=\"displayRows\"]").selectOption("2");
        Locator courseCategory = toolFrame.locator("input[name=\"category\"][value=\"course\"]");
        if (courseCategory.count() > 0) {
            courseCategory.check();
        }
        submitForm("#item-form");
        toolFrame.waitForURL(Pattern.compile(".*/modify_template_items\\?templateId=" + Pattern.quote(templateId) + ".*"));
        assertThat(toolFrame.locator("body")).containsText(itemText);
    }

    private String createEvaluation(String templateId, String evaluationTitle) {
        gotoToolPath("/evaluation_create?templateId=" + templateId);
        setFormValue("input[name=\"title\"]", evaluationTitle);
        setFormValue("textarea[name=\"instructions\"]", "Please complete this test evaluation.");
        submitForm("form");
        toolFrame.waitForURL(Pattern.compile(".*/evaluation_settings\\?evaluationId=\\d+.*"));
        return queryParam(toolFrame.url(), "evaluationId");
    }

    private void saveEvaluationSettings(String evaluationId, String evaluationTitle) {
        LocalDateTime now = LocalDateTime.now();
        gotoToolPath("/evaluation_settings?evaluationId=" + evaluationId);
        setFormValue("input[name=\"title\"]", evaluationTitle);
        setFormValue("textarea[name=\"instructions\"]", "Published by Playwright e2e.");
        toolFrame.locator("input[name=\"startDate\"]").fill(now.plusMinutes(10).format(DATE_TIME_LOCAL));
        toolFrame.locator("input[name=\"dueDate\"]").fill(now.plusDays(2).format(DATE_TIME_LOCAL));
        Locator authControl = toolFrame.locator("select[name=\"authControl\"]");
        if (authControl.count() > 0 && authControl.isEnabled()) {
            authControl.selectOption("NONE");
        }
        Locator reminderFrom = toolFrame.locator("input[name=\"reminderFromEmail\"]");
        if (reminderFrom.count() > 0 && reminderFrom.isVisible()) {
            reminderFrom.fill("admin@example.edu");
        }
        toolFrame.locator("form").first().locator("input[type=\"submit\"]").click();
        toolFrame.waitForURL(Pattern.compile(".*/evaluation_assign\\?evaluationId=" + Pattern.quote(evaluationId) + ".*"));
    }

    private void publishEvaluationWithoutAssignments(String evaluationId) {
        toolFrame.locator("#eval-assign-form input[type=\"submit\"]").click();
        toolFrame.waitForURL(Pattern.compile(".*/evaluation_assign_confirm\\?evaluationId=" + Pattern.quote(evaluationId) + ".*"));
        assertThat(toolFrame.locator("body")).containsText("No groups selected");
        toolFrame.locator("form").first().locator("input[type=\"submit\"]").click();
        toolFrame.waitForURL(Pattern.compile(".*/control_evaluations.*"));
    }

    private void gotoToolPath(String path) {
        toolFrame.navigate(toolContext + path, new Frame.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    private void setFormValue(String selector, String value) {
        Locator locator = toolFrame.locator(selector).first();
        String id = locator.getAttribute("id");
        Locator editor = toolFrame.locator("[id=\"cke_" + id + "\"]");
        if (editor.count() > 0) {
            editor.frameLocator("iframe.cke_wysiwyg_frame").locator("body").fill(value);
        } else {
            locator.fill(value);
        }
    }

    private void submitForm(String selector) {
        toolFrame.locator(selector).evaluate("form => form.submit()");
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
