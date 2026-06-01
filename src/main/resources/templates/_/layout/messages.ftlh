<#if errorMessage??>
    <div class="alert alert-danger d-flex align-items-center gap-2" role="alert">
        <@icon name="exclamation-triangle-fill" />
        <div>${errorMessage}</div>
    </div>
</#if>

<#if infoMessage??>
    <div class="alert alert-info d-flex align-items-center gap-2" role="alert">
        <@icon name="info-circle-fill" />
        <div>${infoMessage}</div>
    </div>
</#if>
