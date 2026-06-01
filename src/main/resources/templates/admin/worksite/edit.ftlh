<@layout>
    <@formCard formState />

    <#assign worksite = formState.bean>
    <#if worksite.latitude?? && worksite.longitude??>
        <#assign worksiteLocation = {
            "latitude": worksite.latitude,
            "longitude": worksite.longitude,
            "accuracy": worksite.radiusMeters!0
        }>
        <@map location=worksiteLocation />
    </#if>

</@layout>
