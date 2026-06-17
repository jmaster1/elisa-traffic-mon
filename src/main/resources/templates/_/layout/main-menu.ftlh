<button class="navbar-toggler" type="button" data-bs-toggle="offcanvas" data-bs-target="#geoMainMenu"
        aria-controls="geoMainMenu" aria-expanded="false" aria-label="Toggle navigation">
    <span class="navbar-toggler-icon"></span>
</button>

<div class="offcanvas-lg offcanvas-end flex-grow-1" tabindex="-1" id="geoMainMenu" aria-labelledby="geoMainMenuLabel">
    <div class="offcanvas-header border-bottom">
        <div class="offcanvas-title geo-brand" id="geoMainMenuLabel">
            <span>etm</span>
        </div>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" data-bs-target="#geoMainMenu"
                aria-label="Close"></button>
    </div>

    <div class="offcanvas-body align-items-lg-center">
        <ul class="navbar-nav geo-nav me-lg-auto">
            <#if admin>
                <li class="nav-item">
                    <a class="nav-link" href="/consumption/report">
                        <@icon name="graph-up"/>Report</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/consumption/report/daily-usage">
                        <@icon name="bar-chart"/>Daily usage</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/consumption/config">
                        <@icon name="sliders"/>Config</a>
                </li>
                <@adminMenu/>
            </#if>
        </ul>
    </div>
</div>
