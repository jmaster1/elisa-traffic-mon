<@layout>
    <div class="d-flex flex-column flex-md-row align-items-md-end justify-content-between gap-3 mb-4">
        <div>
            <div class="text-secondary small text-uppercase fw-semibold mb-1">Admin</div>
            <h1 class="h3 mb-0">Dashboard</h1>
        </div>
        <div class="d-flex flex-wrap gap-2">
            <a class="btn btn-primary" href="/admin/user/create">
                <@icon name="person-plus" class="me-2" />New user
            </a>
            <a class="btn btn-outline-secondary" href="/admin/worksite/create">
                <@icon name=GeoIcon.WORKSITE class="me-2" />New worksite
            </a>
        </div>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-12 col-sm-6 col-lg-3">
            <a class="card text-decoration-none h-100" href="/admin/user/list">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <span class="text-secondary fw-semibold">Users</span>
                        <@icon name="people" class="fs-4 text-primary" />
                    </div>
                    <div class="display-6 fw-semibold text-body">${usersCount}</div>
                </div>
            </a>
        </div>

        <div class="col-12 col-sm-6 col-lg-3">
            <a class="card text-decoration-none h-100" href="/admin/device/list">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <span class="text-secondary fw-semibold">Devices</span>
                        <@icon name=GeoIcon.DEVICE class="fs-4 text-primary" />
                    </div>
                    <div class="display-6 fw-semibold text-body">${devicesCount}</div>
                    <div class="small text-secondary mt-2">${verifiedDevicesCount} verified</div>
                </div>
            </a>
        </div>

        <div class="col-12 col-sm-6 col-lg-3">
            <a class="card text-decoration-none h-100" href="/admin/worksite/list">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <span class="text-secondary fw-semibold">Worksites</span>
                        <@icon name=GeoIcon.WORKSITE class="fs-4 text-primary" />
                    </div>
                    <div class="display-6 fw-semibold text-body">${worksitesCount}</div>
                </div>
            </a>
        </div>

        <div class="col-12 col-sm-6 col-lg-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <span class="text-secondary fw-semibold">Active sessions</span>
                        <@icon name=GeoIcon.CLIENT_SESSION class="fs-4 text-success" />
                    </div>
                    <div class="display-6 fw-semibold">${activeSessionsCount}</div>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-3">
        <div class="col-12 col-lg-8">
            <div class="card">
                <div class="card-header bg-body fw-semibold">Manage</div>
                <div class="list-group list-group-flush">
                    <a class="list-group-item list-group-item-action d-flex align-items-center justify-content-between" href="/admin/user/list">
                        <span><@icon name="people" class="me-2 text-secondary" />Users</span>
                        <@icon name="chevron-right" class="text-secondary" />
                    </a>
                    <a class="list-group-item list-group-item-action d-flex align-items-center justify-content-between" href="/admin/device/list">
                        <span><@icon name=GeoIcon.DEVICE class="me-2 text-secondary" />Devices</span>
                        <@icon name="chevron-right" class="text-secondary" />
                    </a>
                    <a class="list-group-item list-group-item-action d-flex align-items-center justify-content-between" href="/admin/worksite/list">
                        <span><@icon name=GeoIcon.WORKSITE class="me-2 text-secondary" />Worksites</span>
                        <@icon name="chevron-right" class="text-secondary" />
                    </a>
                    <a class="list-group-item list-group-item-action d-flex align-items-center justify-content-between" href="/admin/httpsession/list">
                        <span><@icon name="clock-history" class="me-2 text-secondary" />Sessions</span>
                        <@icon name="chevron-right" class="text-secondary" />
                    </a>
                    <a class="list-group-item list-group-item-action d-flex align-items-center justify-content-between" href="/admin/prefs/list">
                        <span><@icon name="sliders" class="me-2 text-secondary" />Settings</span>
                        <@icon name="chevron-right" class="text-secondary" />
                    </a>
                </div>
            </div>
        </div>
    </div>
</@layout>
