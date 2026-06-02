<@layout>
    <section class="mx-auto" style="max-width: 960px;">
        <div class="mb-3">
            <p class="text-secondary mb-2">
                Parse request text acquired by "Copy as fetch (Node.js)" from Chrome DevTools.
            </p>
            <pre class="bg-body-tertiary border rounded p-3 small overflow-auto"><code>fetch("https://www.elisa.ee/rest/customer/gsm/getMobileInternetUsageData/53065326", {
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
        <#if lastError??>
            <div class="alert alert-danger mt-3" role="alert">
                <div class="fw-semibold">Last consumption query error</div>
                <div>${lastError.date?datetime}</div>
                <div>${lastError.message}</div>
            </div>
        </#if>
        <pre id="fetchConfig" class=""></pre>
        <script>
            const config = ${fetchConfig?no_esc};
            $('#fetchConfig').text(JSON.stringify(config, null, 2));
        </script>
    </section>
</@layout>
