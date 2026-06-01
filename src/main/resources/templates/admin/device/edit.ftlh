<@layout>
    <@formCard formState>
        <#if formState.bean.id?? && !(formState.bean.verificationCode!'')?has_content>
            <button type="submit"
                    class="btn btn-outline-secondary"
                    formaction="/admin/device/${formState.bean.id}/generate-code"
                    formmethod="post">
                <@icon name="key" class="me-2" />Generate code
            </button>
        </#if>
    </@formCard>
</@layout>
