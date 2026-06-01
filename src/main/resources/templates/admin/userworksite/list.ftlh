<@layout>
    <@card title="User worksites" icon=GeoIcon.USER_WORKSITE>
        <form method="post" action="${matrixAction}">
            <div class="table-responsive">
                <table class="${matrixTableClass}">
                    <thead>
                        <tr>
                            <th>User/Worksite</th>
                            <#list matrixWorksites as worksite>
                                <td class="text-center">
                                    <a href="/admin/worksite/${worksite.id}/edit">${worksite.name}</a>
                                </td>
                            </#list>
                        </tr>
                    </thead>
                    <tbody>
                        <#list matrixUsers as user>
                            <tr>
                                <td>
                                    <a href="/admin/user/${user.id}/edit">${user.nameOrEmail}</a>
                                </td>
                                <#list matrixWorksites as worksite>
                                    <#assign matrixKey = user.id?c + ":" + worksite.id?c>
                                    <td class="text-center">
                                        <input class="form-check-input"
                                               type="checkbox"
                                               name="assignments"
                                               value="${matrixKey}"
                                               <#if matrixAssignedKeys[matrixKey]!false>checked</#if>>
                                    </td>
                                </#list>
                            </tr>
                        <#else>
                            <tr>
                                <td colspan="${matrixWorksites?size + 1}" class="text-center text-muted">
                                    <@spring.message "common.message.noData"/>
                                </td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>

            <div class="d-flex justify-content-end mt-3">
                <button type="submit" class="btn btn-primary">
                    <@icon name="floppy" class="me-2" />
                    <@spring.message "common.action.save"/>
                </button>
            </div>
        </form>
    </@card>
</@layout>
