<div class="d-flex align-items-center gap-2">
    <button class="btn btn-outline-secondary btn-sm"
            type="button"
            title="Session history"
            aria-label="Session history"
            data-bs-toggle="offcanvas"
            data-bs-target="#clientSessionHistory"
            aria-controls="clientSessionHistory">
        <@icon name="clock-history" style="font-size: 0.875rem;"/>
    </button>
    <span><@icon name="person" style="font-size: 0.875rem;"/> ${user.name}</span>
</div>
