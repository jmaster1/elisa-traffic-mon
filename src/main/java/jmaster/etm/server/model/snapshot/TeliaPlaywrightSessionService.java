package jmaster.etm.server.model.snapshot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import jmaster.etm.server.model.PhoneOwner;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeliaPlaywrightSessionService {

    private static final String TELIA_MOBILE_URL = "https://iseteenindus.telia.ee/ru/teenused/mobiil";
    private static final String ACCEPT_ALL_COOKIES_SELECTOR = "#allowAll";
    private static final String TELIA_LOGIN_SELECTOR = "a[href^='https://sso.telia.ee?authlevel=1']";
    private static final String TELIA_CHECKBOX_INDICATOR_SELECTOR = "span.check-indicator-QTdnNdhDSG4-";
    private static final String TELIA_PERSONAL_CODE = "37708199511";
    private static final String TELIA_CONFIRMATION_CODE_SELECTOR = "p.h2.text-center[aria-hidden='true']";
    private static final String TELIA_MOBILE_LIST_PATH = "/myse-frontend/api/v1/products/lists/customer_mobile_product_list/items";
    private static final int MAX_PREFS_JSON_LENGTH = 8_000;
    private static final Set<String> PERSISTED_HEADERS = Set.of(
            "accept", "accept-language", "cache-control", "pragma", "priority", "referer", "cookie");
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ConsumptionRegisterService consumptionRegisterService;
    private final ExecutorService sessionExecutor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "telia-playwright-session");
        thread.setDaemon(true);
        return thread;
    });
    private final List<String> sessionLogs = new CopyOnWriteArrayList<>();
    private volatile boolean sessionRunning;
    private volatile String confirmationCode;
    private volatile String sessionError;
    private volatile boolean authenticatedListResponseSeen;
    private volatile boolean listRequestCaptured;
    private volatile boolean providerQueryStarted;
    private Playwright playwright;
    private BrowserContext browserContext;
    private Consumer<Response> listRequestListener;

    public void startTeliaSessionAsync() {
        startTeliaSessionAsync(null);
    }

    public synchronized void startTeliaSessionAsync(String triggerReason) {
        if (sessionRunning) {
            addSessionLog("A Telia session flow is already running.");
            if (triggerReason != null) {
                addSessionLog(triggerReason);
            }
            return;
        }
        sessionLogs.clear();
        confirmationCode = null;
        sessionError = null;
        authenticatedListResponseSeen = false;
        listRequestCaptured = false;
        providerQueryStarted = false;
        sessionRunning = true;
        addSessionLog("Starting Telia session flow.");
        if (triggerReason != null) {
            addSessionLog(triggerReason);
        }
        sessionExecutor.submit(() -> {
            try {
                startTeliaSession();
            } catch (Exception ex) {
                sessionError = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
                addSessionLog("Session flow failed: " + sessionError);
                log.error("Telia session flow failed", ex);
            } finally {
                sessionRunning = false;
                addSessionLog("Telia session flow finished.");
            }
        });
    }

    @EventListener
    public void onSessionRefreshRequested(TeliaSessionRefreshRequestedEvent event) {
        startTeliaSessionAsync(event.reason() + " Starting or continuing the Telia login flow.");
    }

    public SessionStatus getSessionStatus() {
        return new SessionStatus(sessionRunning, confirmationCode, listRequestCaptured,
                List.copyOf(sessionLogs), sessionError);
    }

    private void addSessionLog(String message) {
        sessionLogs.add(LocalTime.now().format(LOG_TIME_FORMAT) + "  " + message);
    }

    public record SessionStatus(boolean running, String confirmationCode, boolean listRequestCaptured,
                                List<String> logs, String error) {
    }

    public synchronized void startTeliaSession() {
        try {
            navigateToTelia();
        } catch (PlaywrightException ex) {
            if (!isClosedTarget(ex)) {
                throw ex;
            }
            log.info("Playwright Firefox context is closed; restarting it with the persistent profile");
            close();
            launchPersistentFirefox();
            navigateToTelia();
        }
    }

    private void navigateToTelia() {
        if (browserContext == null) {
            launchPersistentFirefox();
        }
        installListResponseListener();
        List<Page> pages = browserContext.pages();
        Page page = pages.isEmpty() ? browserContext.newPage() : pages.getFirst();
        addSessionLog("Opening Telia mobile services page.");
        page.navigate(TELIA_MOBILE_URL);
        if (authenticatedListResponseSeen) {
            if (listRequestCaptured) {
                addSessionLog("Authenticated mobile-services request is already available.");
                runProviderQueryAfterCapture();
            } else {
                addSessionLog("Telia is already authenticated, but the request configuration could not be saved; skipping login steps.");
            }
            return;
        }
        PersonalCodeForm openForm = findVisiblePersonalCodeForm(page);
        if (openForm != null) {
            addSessionLog("Login form is already open; skipping cookie and login-link checks.");
            log.info("Telia session result: personal-code form is already open in frame {}; skipping cookie and login-link checks",
                    openForm.frameIndex());
            enterPersonalCodeAndSubmit(openForm);
            waitForCapturedListRequest(300_000);
            return;
        }
        if (waitForCapturedListRequest(3_000)) {
            addSessionLog("An authenticated mobile-services list request was captured; login is already active.");
            return;
        }
        if (authenticatedListResponseSeen) {
            addSessionLog("Telia is already authenticated; skipping cookie and login-link checks.");
            return;
        }
        if (acceptCookiesIfPrompted(page)) {
            clickTeliaLogin(page);
        } else {
            log.warn("Skipping Telia login click because cookie consent is still blocking the page");
        }
        waitForCapturedListRequest(300_000);
    }

    private void installListResponseListener() {
        if (listRequestListener != null) {
            return;
        }
        listRequestListener = this::captureMobileListRequest;
        for (Page openPage : browserContext.pages()) {
            openPage.onResponse(listRequestListener);
        }
        browserContext.onPage(openPage -> openPage.onResponse(listRequestListener));
    }

    private void captureMobileListRequest(Response response) {
        if (listRequestCaptured) {
            return;
        }
        Request request = response.request();
        String requestUrl = request.url();
        if (!"GET".equalsIgnoreCase(request.method())
                || !requestUrl.startsWith("https://iseteenindus.telia.ee")
                || !requestUrl.contains(TELIA_MOBILE_LIST_PATH)
                || !requestUrl.matches(".*[?&]page=1(?:&.*|$)")) {
            return;
        }

        addSessionLog("Found customer_mobile_product_list/items request (HTTP " + response.status() + ").");
        if (response.status() != 200) {
            addSessionLog("List request is not successful yet; waiting for an authenticated request.");
            return;
        }

        try {
            JsonObject responseBody = JsonParser.parseString(response.text()).getAsJsonObject();
            JsonObject accessData = responseBody.getAsJsonObject("accessData");
            String accessState = accessData == null || accessData.get("state") == null
                    ? null : accessData.get("state").getAsString();
            if (!"ALLOWED".equals(accessState)) {
                addSessionLog("List response is not authenticated (access state: " + accessState + "); not saving it.");
                return;
            }
            authenticatedListResponseSeen = true;

            Map<String, String> requestHeaders = new LinkedHashMap<>();
            for (Map.Entry<String, String> header : request.allHeaders().entrySet()) {
                String name = header.getKey().toLowerCase(Locale.ROOT);
                if (!PERSISTED_HEADERS.contains(name)) {
                    continue;
                }
                String value = header.getValue();
                if ("cookie".equals(name)) {
                    String originalCookie = value;
                    value = filterTeliaCookies(value, false);
                    if (value.length() != originalCookie.length()) {
                        addSessionLog("Filtered tracking cookies from the captured request ("
                                + originalCookie.length() + " to " + value.length() + " characters).");
                    }
                }
                requestHeaders.put(name, value);
            }

            FetchConfig config = consumptionRegisterService.getFetchConfig();
            if (config == null) {
                config = new FetchConfig();
            }
            config.uri = requestUrl;
            config.headers = requestHeaders;
            int configJsonLength = new Gson().toJson(config).length();
            if (configJsonLength > MAX_PREFS_JSON_LENGTH) {
                requestHeaders.computeIfPresent("cookie", (name, value) -> filterTeliaCookies(value, true));
                configJsonLength = new Gson().toJson(config).length();
                addSessionLog("Removed non-essential cookies to fit the preferences storage limit.");
            }
            if (configJsonLength > MAX_PREFS_JSON_LENGTH) {
                throw new IllegalStateException("Captured request configuration exceeds the preferences storage limit");
            }
            consumptionRegisterService.saveFetchConfig(config);
            listRequestCaptured = true;
            sessionError = null;
            addSessionLog("Authenticated mobile-list request saved to consumption configuration.");
        } catch (Exception ex) {
            sessionError = ex instanceof IllegalStateException && ex.getMessage() != null
                    ? ex.getMessage() : ex.getClass().getSimpleName();
            addSessionLog("Could not save the mobile-list request (" + sessionError + ").");
            log.warn("Could not capture Telia mobile list request ({})", ex.getClass().getSimpleName());
        }
    }

    private String filterTeliaCookies(String cookieHeader, boolean authenticationOnly) {
        List<String> cookies = new ArrayList<>();
        for (String cookie : cookieHeader.split(";")) {
            String trimmedCookie = cookie.trim();
            int equalsIndex = trimmedCookie.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }
            String name = trimmedCookie.substring(0, equalsIndex).trim();
            String lowerName = name.toLowerCase(Locale.ROOT);
            if (authenticationOnly) {
                if (lowerName.equals("rememberme")
                        || lowerName.equals("crmfront-session")
                        || lowerName.equals("ssoui")
                        || lowerName.equals("_csrftoken")
                        || lowerName.equals("isfromselfservice")
                        || lowerName.equals("tweb-session")
                        || lowerName.equals("bcsessionid")
                        || lowerName.equals("bcwebid")
                        || lowerName.equals("jsessionid")
                        || lowerName.startsWith("bigipserver")) {
                    cookies.add(trimmedCookie);
                }
            } else if (!isTrackingCookie(lowerName)) {
                cookies.add(trimmedCookie);
            }
        }
        return String.join("; ", cookies);
    }

    private boolean isTrackingCookie(String name) {
        return name.equals("cookieconsent")
                || name.equals("segment")
                || name.equals("__ta_s")
                || name.equals("gastring")
                || name.startsWith("rum_")
                || name.startsWith("da_")
                || name.startsWith("_ga")
                || name.startsWith("_gcl")
                || name.equals("_gid")
                || name.startsWith("_uet")
                || name.equals("fpid")
                || name.equals("fplc")
                || name.startsWith("optimizely");
    }

    private boolean waitForCapturedListRequest(long timeoutMillis) {
        if (listRequestCaptured) {
            runProviderQueryAfterCapture();
            return true;
        }
        addSessionLog("Waiting for an authenticated customer_mobile_product_list/items request.");
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000L;
        while (!listRequestCaptured && !authenticatedListResponseSeen && System.nanoTime() < deadline) {
            BrowserContext context = browserContext;
            if (context == null) {
                return false;
            }
            List<Page> pages = context.pages();
            if (pages.isEmpty()) {
                return false;
            }
            pages.getFirst().waitForTimeout(500);
        }
        if (!listRequestCaptured) {
            if (authenticatedListResponseSeen) {
                addSessionLog("Authenticated page detected, but saving its request failed; stopping without login checks.");
            } else {
                addSessionLog("No authenticated mobile-list request was captured before timeout.");
            }
        } else {
            runProviderQueryAfterCapture();
        }
        return listRequestCaptured;
    }

    private void runProviderQueryAfterCapture() {
        if (!listRequestCaptured || providerQueryStarted) {
            return;
        }
        providerQueryStarted = true;
        addSessionLog("Starting Telia provider query for configured phone numbers.");
        try {
            Map<PhoneOwner, BigDecimal> usedGbByOwner = consumptionRegisterService.queryAndSaveCurrentSnapshots();
            if (usedGbByOwner.isEmpty()) {
                addSessionLog("Telia provider returned no phone-number usage values.");
                return;
            }
            for (Map.Entry<PhoneOwner, BigDecimal> entry : usedGbByOwner.entrySet()) {
                String usedGb = entry.getValue().setScale(2, RoundingMode.HALF_UP).toPlainString();
                addSessionLog(entry.getKey().name() + " (" + entry.getKey().phoneNr + "): " + usedGb + " GB");
            }
            addSessionLog("Telia provider query completed; consumption snapshots were saved.");
            sessionError = null;
        } catch (Exception ex) {
            sessionError = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            addSessionLog("Telia provider query failed: " + sessionError);
            log.error("Telia provider query failed after capturing the authenticated list request", ex);
        }
    }

    private boolean isClosedTarget(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && (message.contains("Target page, context or browser has been closed")
                    || message.contains("Browser has been closed"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean acceptCookiesIfPrompted(Page page) {
        addSessionLog("Checking for Telia cookie consent.");
        log.info("Checking Telia cookie consent on {} using selector {}", page.url(), ACCEPT_ALL_COOKIES_SELECTOR);
        CookieConsentButton consentButton = findCookieConsentButton(page);
        if (consentButton == null) {
            addSessionLog("Cookie dialog not found.");
            log.info("Telia cookie consent result: {} was not visible; no click performed",
                    ACCEPT_ALL_COOKIES_SELECTOR);
            return true;
        }

        log.info("Telia cookie consent action: clicking {} in frame {}",
                ACCEPT_ALL_COOKIES_SELECTOR, consentButton.frameIndex());
        addSessionLog("Accepting cookies.");
        try {
            consentButton.locator().click(new Locator.ClickOptions().setTimeout(5000));
        } catch (PlaywrightException ex) {
            if (isClosedTarget(ex)) {
                throw ex;
            }
            log.warn("Telia cookie consent result: click failed for {}", ACCEPT_ALL_COOKIES_SELECTOR, ex);
            return false;
        }

        try {
            consentButton.locator().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(5000));
            log.info("Telia cookie consent result: dialog closed after clicking {}",
                    ACCEPT_ALL_COOKIES_SELECTOR);
            addSessionLog("Cookie dialog closed.");
            return true;
        } catch (PlaywrightException ex) {
            if (isClosedTarget(ex)) {
                throw ex;
            }
            log.warn("Telia cookie consent result: {} remains visible after click", ACCEPT_ALL_COOKIES_SELECTOR, ex);
            return false;
        }
    }

    private void clickTeliaLogin(Page page) {
        addSessionLog("Looking for Telia login link.");
        log.info("Looking for Telia login link using selector {}", TELIA_LOGIN_SELECTOR);
        Locator loginLink = page.locator(TELIA_LOGIN_SELECTOR).first();
        try {
            loginLink.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10_000));
        } catch (PlaywrightException ex) {
            if (isClosedTarget(ex)) {
                throw ex;
            }
            log.info("Telia login result: login link {} was not found or visible; checking whether the login form is already open",
                    TELIA_LOGIN_SELECTOR);
            addSessionLog("Login link not found; checking for an already-open login form.");
            enterPersonalCodeAndSubmit(page);
            return;
        }

        log.info("Telia login action: clicking {}", TELIA_LOGIN_SELECTOR);
        addSessionLog("Opening Telia login form.");
        try {
            loginLink.click(new Locator.ClickOptions().setTimeout(10_000));
            log.info("Telia login result: click completed; current URL is {}", page.url());
        } catch (PlaywrightException ex) {
            if (isClosedTarget(ex)) {
                throw ex;
            }
            log.warn("Telia login result: click failed for {}", TELIA_LOGIN_SELECTOR, ex);
            enterPersonalCodeAndSubmit(page);
            return;
        }
        enterPersonalCodeAndSubmit(page);
    }

    private void enterPersonalCodeAndSubmit(Page page) {
        String personalCode = TELIA_PERSONAL_CODE;
        if (!personalCode.matches("\\d{11}")) {
            log.error("Telia personal-code step skipped: configured code must contain exactly 11 digits");
            return;
        }

        PersonalCodeForm form = findPersonalCodeForm(page);
        if (form == null) {
            log.warn("Telia personal-code result: input #personalCode was not found in any open page or frame");
            addSessionLog("Personal-code form was not found in the open Telia pages.");
            return;
        }
        enterPersonalCodeAndSubmit(form);
    }

    private void enterPersonalCodeAndSubmit(PersonalCodeForm form) {
        String personalCode = TELIA_PERSONAL_CODE;
        if (!personalCode.matches("\\d{11}")) {
            log.error("Telia personal-code step skipped: configured code must contain exactly 11 digits");
            return;
        }

        Locator input = form.input();
        addSessionLog("Personal-code form found.");
        log.info("Telia personal-code result: found #personalCode in frame {}", form.frameIndex());
        log.info("Telia personal-code action: filling #personalCode (value is redacted)");
        try {
            input.fill(personalCode);
            if (!personalCode.equals(input.inputValue())) {
                log.error("Telia personal-code result: field value did not match the configured value (value redacted)");
                return;
            }
            log.info("Telia personal-code result: #personalCode filled successfully (value redacted)");
            addSessionLog("Personal code filled.");
        } catch (PlaywrightException ex) {
            // Playwright call logs can contain fill arguments, so don't attach the exception or value to logs.
            log.warn("Telia personal-code result: field fill failed ({})", ex.getClass().getSimpleName());
            addSessionLog("Personal-code field could not be filled (" + ex.getClass().getSimpleName() + ").");
            return;
        }

        Frame frame = form.frame();
        if (!selectCheckboxBeforeSubmit(frame)) {
            return;
        }

        Locator submitButton = frame.getByRole(AriaRole.BUTTON,
                new Frame.GetByRoleOptions().setName("Войти").setExact(true)).first();
        try {
            submitButton.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10_000));
        } catch (PlaywrightException ex) {
            log.warn("Telia personal-code result: submit button 'Войти' was not found or visible ({})",
                    ex.getClass().getSimpleName());
            return;
        }

        log.info("Telia personal-code action: clicking submit button 'Войти'");
        addSessionLog("Submitting personal-code form.");
        try {
            submitButton.click(new Locator.ClickOptions().setTimeout(10_000));
            log.info("Telia personal-code result: submit click completed; current frame URL is {}", frame.url());
            waitForConfirmationCode();
        } catch (PlaywrightException ex) {
            // Do not log Playwright's call log here: it may include the personal code entered above.
            log.warn("Telia personal-code result: submit click failed ({})", ex.getClass().getSimpleName());
            addSessionLog("Submitting the personal-code form failed (" + ex.getClass().getSimpleName() + ").");
        }
    }

    private void waitForConfirmationCode() {
        addSessionLog("Waiting for the four-digit confirmation code.");
        long deadline = System.nanoTime() + 120_000_000_000L;
        while (confirmationCode == null && !listRequestCaptured && System.nanoTime() < deadline) {
            BrowserContext context = browserContext;
            if (context == null) {
                return;
            }
            List<Page> pages = context.pages();
            for (Page openPage : pages) {
                for (Frame frame : openPage.frames()) {
                    Locator codeElements = frame.locator(TELIA_CONFIRMATION_CODE_SELECTOR);
                    try {
                        int count = Math.min(codeElements.count(), 5);
                        for (int index = 0; index < count; index++) {
                            String candidate = codeElements.nth(index).innerText().trim();
                            if (candidate.matches("\\d{4}")) {
                                confirmationCode = candidate;
                                addSessionLog("Confirmation code received: " + candidate);
                                return;
                            }
                        }
                    } catch (PlaywrightException ex) {
                        if (isClosedTarget(ex)) {
                            throw ex;
                        }
                        // Telia can replace its DOM while the confirmation challenge loads.
                    }
                }
            }
            if (pages.isEmpty()) {
                return;
            }
            pages.getFirst().waitForTimeout(500);
        }
        if (listRequestCaptured) {
            return;
        }
        sessionError = "No confirmation code appeared within 120 seconds.";
        addSessionLog(sessionError);
    }

    private boolean selectCheckboxBeforeSubmit(Frame frame) {
        log.info("Telia checkbox action: inspecting {} before submit", TELIA_CHECKBOX_INDICATOR_SELECTOR);
        Locator indicator = frame.locator(TELIA_CHECKBOX_INDICATOR_SELECTOR).first();
        Locator checkbox = frame.locator("input[type='checkbox']").first();
        try {
            indicator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10_000));
        } catch (PlaywrightException ex) {
            log.warn("Telia checkbox result: indicator was not found or visible ({})",
                    ex.getClass().getSimpleName());
            return false;
        }

        try {
            int checkboxCount = frame.locator("input[type='checkbox']").count();
            if (checkboxCount != 1) {
                log.warn("Telia checkbox result: expected one checkbox input, found {}; submit skipped",
                        checkboxCount);
                return false;
            }
            if (!checkbox.isChecked()) {
                log.info("Telia checkbox action: clicking indicator {}", TELIA_CHECKBOX_INDICATOR_SELECTOR);
                indicator.click(new Locator.ClickOptions().setTimeout(5000));
            } else {
                log.info("Telia checkbox result: checkbox was already selected; no click needed");
            }
            boolean selected = checkbox.isChecked();
            log.info("Telia checkbox result: selected={}", selected);
            addSessionLog(selected ? "Remember-me checkbox selected." : "Remember-me checkbox was not selected.");
            return selected;
        } catch (PlaywrightException ex) {
            log.warn("Telia checkbox result: selection could not be verified ({})",
                    ex.getClass().getSimpleName());
            return false;
        }
    }

    private PersonalCodeForm findPersonalCodeForm(Page page) {
        long deadline = System.nanoTime() + 20_000_000_000L;
        while (System.nanoTime() < deadline) {
            PersonalCodeForm form = findVisiblePersonalCodeForm(page);
            if (form != null) {
                return form;
            }
            page.waitForTimeout(250);
        }
        return null;
    }

    private PersonalCodeForm findVisiblePersonalCodeForm(Page page) {
        List<Frame> frames = page.frames();
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            Frame frame = frames.get(frameIndex);
            Locator input = frame.locator("#personalCode").first();
            try {
                if (input.count() > 0 && input.isVisible()) {
                    return new PersonalCodeForm(frame, input, frameIndex);
                }
            } catch (PlaywrightException ex) {
                if (isClosedTarget(ex)) {
                    throw ex;
                }
                // The SSO page may replace its frames while loading; the caller can scan again.
            }
        }
        return null;
    }

    private record PersonalCodeForm(Frame frame, Locator input, int frameIndex) {
    }

    private CookieConsentButton findCookieConsentButton(Page page) {
        long deadline = System.nanoTime() + 10_000_000_000L;
        boolean foundButHidden = false;
        int scanNumber = 0;
        while (System.nanoTime() < deadline) {
            List<Frame> frames = page.frames();
            for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
                Locator button = frames.get(frameIndex).locator(ACCEPT_ALL_COOKIES_SELECTOR).first();
                if (button.count() == 0) {
                    continue;
                }
                if (button.isVisible()) {
                    log.info("Telia cookie consent: found visible {} in frame {} (scan {})",
                            ACCEPT_ALL_COOKIES_SELECTOR, frameIndex, scanNumber + 1);
                    return new CookieConsentButton(button, frameIndex);
                }
                if (!foundButHidden) {
                    log.info("Telia cookie consent: found {} in frame {}, but it is hidden",
                            ACCEPT_ALL_COOKIES_SELECTOR, frameIndex);
                    foundButHidden = true;
                }
            }
            scanNumber++;
            page.waitForTimeout(250);
        }
        log.info("Telia cookie consent scan finished: {} scans, {} frames on final scan, selector was {}",
                scanNumber, page.frames().size(), foundButHidden ? "found but hidden" : "not found");
        return null;
    }

    private record CookieConsentButton(Locator locator, int frameIndex) {
    }

    private void launchPersistentFirefox() {
        Path profileDirectory = Path.of(System.getProperty("user.home"), ".etm", "telia-firefox-profile");
        boolean headless = isLinuxWithoutDisplay();
        try {
            Files.createDirectories(profileDirectory);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create the persistent Telia browser profile directory", ex);
        }

        addSessionLog(headless
                ? "Starting Firefox headless because this server has no graphical display."
                : "Starting Firefox with a visible window.");
        Playwright newPlaywright = Playwright.create();
        try {
            BrowserContext newContext = newPlaywright.firefox().launchPersistentContext(
                    profileDirectory,
                    new BrowserType.LaunchPersistentContextOptions().setHeadless(headless));
            playwright = newPlaywright;
            browserContext = newContext;
        } catch (RuntimeException ex) {
            newPlaywright.close();
            throw new IllegalStateException(
                    "Unable to start Playwright Firefox. Verify the installed browser binaries, OS dependencies, and display configuration.", ex);
        }
    }

    private boolean isLinuxWithoutDisplay() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux")) {
            return false;
        }
        return isBlank(System.getenv("DISPLAY")) && isBlank(System.getenv("WAYLAND_DISPLAY"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @PreDestroy
    public void destroy() {
        sessionExecutor.shutdownNow();
        close();
    }

    public synchronized void close() {
        if (browserContext != null) {
            try {
                browserContext.close();
            } catch (RuntimeException ex) {
                log.debug("Unable to close Playwright Firefox context cleanly", ex);
            } finally {
                browserContext = null;
                listRequestListener = null;
            }
        }
        if (playwright != null) {
            try {
                playwright.close();
            } catch (RuntimeException ex) {
                log.debug("Unable to close Playwright cleanly", ex);
            } finally {
                playwright = null;
            }
        }
    }
}
