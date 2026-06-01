<div class="offcanvas offcanvas-end" tabindex="-1" id="clientSessionHistory" aria-labelledby="clientSessionHistoryLabel">
    <div class="offcanvas-header">
        <h5 class="offcanvas-title" id="clientSessionHistoryLabel"><@icon name="clock-history"/>Session history</h5>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body" id="clientSessionHistoryBody">
        <div class="text-secondary">Loading...</div>
    </div>
</div>
<script>
    $(function () {
        const $history = $("#clientSessionHistory");
        const $body = $("#clientSessionHistoryBody");

        $history.on("show.bs.offcanvas", function () {
            $body.html('<div class="text-secondary">Loading...</div>');
            $.ajax({
                url: "/client/session-history",
                method: "GET",
                dataType: "html"
            }).done(function (html) {
                $body.html(html);
            }).fail(function () {
                $body.html('<div class="alert alert-danger mb-0">Failed to load session history.</div>');
            });
        });
    });
</script>
