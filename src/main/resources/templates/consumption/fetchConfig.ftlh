<@layout>
    <section class="mx-auto" style="max-width: 960px;">
        <form method="post" class="d-grid gap-3">
            <textarea
                    name="data"
                    class="form-control font-monospace"
                    rows="10"
                    required
                    autofocus></textarea>
            <div>
                <button class="btn btn-primary" type="submit">Parse</button>
            </div>
        </form>
        <pre id="fetchConfig" class=""></pre>
        <script>
            const config = ${fetchConfig?no_esc};
            $('#fetchConfig').text(JSON.stringify(config, null, 2));
        </script>
    </section>
</@layout>
