<@layout>
    <section class="mx-auto" style="max-width: 960px;">
        <div class="accordion mb-3" id="fetchConfigHints">
            <div class="accordion-item">
                <h2 class="accordion-header" id="elisaFetchHintHeading">
                    <button class="accordion-button" type="button" data-bs-toggle="collapse"
                            data-bs-target="#elisaFetchHint" aria-expanded="true" aria-controls="elisaFetchHint">
                        Elisa
                    </button>
                </h2>
                <div id="elisaFetchHint" class="accordion-collapse collapse show"
                     aria-labelledby="elisaFetchHintHeading" data-bs-parent="#fetchConfigHints">
                    <div class="accordion-body">
                        <p class="text-secondary mb-2">
                            In Chrome DevTools copy the consumption request as "Copy as fetch (Node.js)".
                        </p>
                        <pre class="bg-body-tertiary border rounded p-3 small overflow-auto mb-0"><code>fetch("https://www.elisa.ee/rest/customer/gsm/getMobileInternetUsageData/53065326", {
  "headers": {
    "accept": "application/json, text/plain, *",
    "accept-language": "ru-RU,ru;q=0.9,en-GB;q=0.8,en-US;q=0.7,en;q=0.6",
    "cache-control": "no-cache",
    "pragma": "no-cache",
    "sec-ch-ua": "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"",
    "sec-ch-ua-mobile": "?0",
    "sec-ch-ua-platform": "\"Windows\"",
    "sec-fetch-dest": "empty",
    "sec-fetch-mode": "cors",
    "sec-fetch-site": "same-origin",
    "x-if-id": "8073848",
    "cookie": "ekak=false; cookie-categories=...",
    "Referer": "https://www.elisa.ee/itb/mobile-internet-usage/53065326",
    "Referrer-Policy": "strict-origin-when-cross-origin"
  },
  "body": null,
  "method": "GET"
});</code></pre>
                    </div>
                </div>
            </div>
            <div class="accordion-item">
                <h2 class="accordion-header" id="teliaFetchHintHeading">
                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                            data-bs-target="#teliaFetchHint" aria-expanded="false" aria-controls="teliaFetchHint">
                        Telia
                    </button>
                </h2>
                <div id="teliaFetchHint" class="accordion-collapse collapse"
                     aria-labelledby="teliaFetchHintHeading" data-bs-parent="#fetchConfigHints">
                    <div class="accordion-body">
                        <p class="mb-2">
                            Open <a href="/telia/login">Telia login</a> and start a session. After authentication,
                            ETM captures and saves the authenticated <code>customer_mobile_product_list/items</code>
                            request automatically. It does not use a consumption request from an individual number page.
                        </p>
                        <p class="text-secondary mb-2">
                            The provider follows all list pages, finds the configured phone numbers and then reads their domestic mobile-data usage.
                        </p>
                    </div>
                </div>
            </div>
        </div>
        <form id="parseFetchConfigForm" method="post" class="d-grid gap-3">
            <textarea
                    name="data"
                    class="form-control font-monospace"
                    rows="10"
                    required
                    autofocus></textarea>
        </form>
        <div class="d-flex gap-2 mt-3">
            <button class="btn btn-primary" form="parseFetchConfigForm" type="submit">Parse</button>
            <form method="post" action="/consumption/config/test">
                <button class="btn btn-outline-primary" type="submit">test</button>
            </form>
        </div>
        <#if parsedProvider??>
            <div class="alert alert-success mt-3 mb-0" role="status">
                <div class="fw-semibold">Fetch parsed successfully</div>
                <dl class="row mb-0 mt-2">
                    <dt class="col-sm-2">Provider</dt>
                    <dd class="col-sm-10">${parsedProvider}</dd>
                    <dt class="col-sm-2">Request URL</dt>
                    <dd class="col-sm-10 text-break mb-1">${parsedFetchUrl}</dd>
                    <dt class="col-sm-2">Headers</dt>
                    <dd class="col-sm-10 mb-0">${parsedHeaderCount}</dd>
                </dl>
            </div>
        </#if>
        <#if lastError??>
            <div class="alert alert-danger mt-3" role="alert">
                <div class="fw-semibold">Last consumption query error</div>
                <#if lastError.date??>
                    <div>${lastError.date?datetime}</div>
                </#if>
                <div>${lastError.message!"Unknown error"}</div>
            </div>
        </#if>
        <section class="mt-3">
            <h2 class="h5">Saved fetch configuration</h2>
            <pre id="fetchConfig" class="mb-0"></pre>
        </section>
        <script>
            const config = ${fetchConfig?no_esc};
            $('#fetchConfig').text(JSON.stringify(config, null, 2));
        </script>
    </section>
</@layout>
