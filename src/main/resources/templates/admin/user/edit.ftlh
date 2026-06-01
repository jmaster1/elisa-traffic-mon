<@layout>
    <div class="row g-4">
        <div class="col-12 col-xl-6">
            <@formCard formState />
        </div>
        <#if clientProfileFormState??>
            <div class="col-12 col-xl-6">
                <@formCard clientProfileFormState />
            </div>
        </#if>
    </div>

    <#if worksiteOptions??>
        <@card title="Worksites" icon="building">
            <form method="post" action="${worksitesAction}">
                <div class="list-group mb-3">
                    <#list worksiteOptions as option>
                        <label class="list-group-item d-flex align-items-center gap-3">
                            <input class="form-check-input m-0"
                                   type="checkbox"
                                   name="worksiteIds"
                                   value="${option.worksite.id}"
                                   <#if option.assigned>checked</#if>>
                            <span>${option.worksite.name}</span>
                        </label>
                    <#else>
                        <div class="list-group-item text-secondary">No worksites yet.</div>
                    </#list>
                </div>

                <button type="submit" class="btn btn-primary">
                    <@spring.message "common.action.save"/>
                </button>
            </form>
        </@card>
    </#if>

    <#if devicesPageState??>
        <#macro uuid_tdBody column bean>
            <code>${bean.uuid}</code>
        </#macro>

        <#macro verificationCode_tdBody column bean>
            <#if bean.verificationCode??>
                <span class="badge text-bg-warning">${bean.verificationCode}</span>
            <#else>
                <span class="text-secondary">Signed up</span>
            </#if>
        </#macro>

        <@page devicesPageState/>
    </#if>

    <#if deviceFormState??>
        <@formCard deviceFormState />
    </#if>

    <#if clientSessionsPageState??>
        <@page clientSessionsPageState/>
    </#if>
</@layout>
