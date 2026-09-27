<@layout>
    <section class="mx-auto" style="max-width: 420px;">
        <div class="card border-0 shadow-sm">
            <div class="card-body p-4">
                <p class="text-uppercase text-secondary fw-semibold small mb-1">EtmLog Admin</p>
                <h1 class="h3 fw-semibold mb-4">Sign in</h1>

                <div id="login-error" class="alert alert-danger d-none" role="alert"></div>
                <button id="login-google" class="btn btn-outline-primary w-100" type="button">
                    <@icon name="google" attrs={"class": "me-2"}/>
                    Continue with Google
                </button>
            </div>
        </div>
    </section>

    <script type="module">
        import {initializeApp} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-app.js";
        import {
            getAuth,
            GoogleAuthProvider,
            signInWithPopup
        } from "https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js";

        const firebaseConfig = {
            apiKey: "${firebaseApiKey?js_string}",
            authDomain: "${firebaseAuthDomain?js_string}",
            projectId: "${firebaseProjectId?js_string}",
            storageBucket: "${firebaseStorageBucket?js_string}",
            messagingSenderId: "${firebaseMessagingSenderId?js_string}",
            appId: "${firebaseAppId?js_string}"
        };

        const auth = getAuth(initializeApp(firebaseConfig));
        const googleProvider = new GoogleAuthProvider();
        const errorElement = document.getElementById("login-error");
        const loginButton = document.getElementById("login-google");

        function showError(error) {
            errorElement.textContent = error?.responseJSON?.message
                || error?.message
                || "Google authentication failed";
            errorElement.classList.remove("d-none");
        }

        function submitIdToken(idToken) {
            return $.ajax({
                url: "/firebase-login",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({idToken})
            });
        }

        loginButton.addEventListener("click", () => {
            errorElement.classList.add("d-none");
            Jmaster.setBusy(true);
            signInWithPopup(auth, googleProvider)
                .then(result => result.user.getIdToken())
                .then(submitIdToken)
                .then(response => window.location.href = response?.redirectUrl || "/consumption/report")
                .catch(showError)
                .finally(() => Jmaster.setBusy(false));
        });
    </script>
</@layout>
