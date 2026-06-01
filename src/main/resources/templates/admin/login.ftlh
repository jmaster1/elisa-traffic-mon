<@layout>
    <section class="mx-auto" style="max-width: 420px;">
        <div class="card border-0 shadow-sm">
            <div class="card-body p-4">
                <p class="text-uppercase text-secondary fw-semibold small mb-1">GeoLog Admin</p>
                <h1 class="h3 fw-semibold mb-4">Sign in</h1>

                <form method="post" action="/admin/login" class="d-grid gap-3">
                    <div>
                        <label for="username" class="form-label">Username</label>
                        <input id="username" name="username" class="form-control" autocomplete="username" required autofocus>
                    </div>
                    <div>
                        <label for="password" class="form-label">Password</label>
                        <input id="password" name="password" class="form-control" type="password" autocomplete="current-password" required>
                    </div>
                    <button class="btn btn-primary" type="submit">Sign in</button>
                </form>
            </div>
        </div>
    </section>
</@layout>
