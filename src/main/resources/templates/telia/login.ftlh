<@layout>
    <section class="mx-auto" style="max-width: 960px;">
        <h1 class="h3 mb-3">Telia session</h1>
        <p class="text-secondary">
            Start the Telia sign-in flow in Firefox. When Telia displays a Smart-ID confirmation code,
            it will appear here. Confirm the request in the Smart-ID app yourself.
        </p>

        <button id="startTeliaSession" class="btn btn-primary" type="button">
            Start Telia session
        </button>

        <section class="mt-4" aria-live="polite">
            <h2 class="h5">Smart-ID confirmation code</h2>
            <div id="confirmationCode" class="display-4 font-monospace" aria-label="Confirmation code">
                —
            </div>
            <div id="sessionState" class="text-secondary mt-2">Ready.</div>
        </section>

        <section class="mt-4">
            <h2 class="h5">Session log</h2>
            <pre id="sessionLog" class="bg-body-tertiary border rounded p-3 small overflow-auto mb-0"
                 style="min-height: 180px; max-height: 420px;" aria-live="polite"></pre>
        </section>
    </section>

    <script>
        (() => {
            const startButton = document.getElementById('startTeliaSession');
            const codeElement = document.getElementById('confirmationCode');
            const stateElement = document.getElementById('sessionState');
            const logElement = document.getElementById('sessionLog');
            let refreshInProgress = false;

            async function refreshStatus() {
                if (refreshInProgress) return;
                refreshInProgress = true;
                try {
                    const response = await fetch('/telia/login/status', {headers: {'Accept': 'application/json'}});
                    if (!response.ok) throw new Error('Status request failed: ' + response.status);
                    const status = await response.json();
                    logElement.textContent = status.logs.join('\n');
                    logElement.scrollTop = logElement.scrollHeight;
                    codeElement.textContent = status.confirmationCode || '—';
                    startButton.disabled = status.running;
                    stateElement.textContent = status.running
                        ? (status.confirmationCode
                            ? 'Code received. Confirm the request in Smart-ID to continue.'
                            : 'Telia sign-in is running…')
                        : (status.error || (status.listRequestCaptured
                            ? 'Authenticated mobile-list request captured and saved.'
                            : status.confirmationCode
                                ? 'Code received. Confirm the request in Smart-ID.'
                                : 'Ready.'));
                } catch (error) {
                    stateElement.textContent = error.message;
                } finally {
                    refreshInProgress = false;
                }
            }

            startButton.addEventListener('click', async () => {
                startButton.disabled = true;
                stateElement.textContent = 'Starting Telia sign-in…';
                try {
                    const response = await fetch('/telia/login/start', {method: 'POST'});
                    if (!response.ok) throw new Error('Start request failed: ' + response.status);
                    await refreshStatus();
                } catch (error) {
                    startButton.disabled = false;
                    stateElement.textContent = error.message;
                }
            });

            refreshStatus();
            window.setInterval(refreshStatus, 750);
        })();
    </script>
</@layout>
