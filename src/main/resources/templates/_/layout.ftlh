<#import "/spring.ftl" as spring/>
<#assign known = SPRING_SECURITY_CONTEXT??>
<#assign signedIn = false>
<#assign admin = false>
<#if known && SPRING_SECURITY_CONTEXT.authentication??>
    <#assign auth = SPRING_SECURITY_CONTEXT.authentication>
    <#assign user = auth.principal>
    <#assign signedIn = auth.isAuthenticated()>
    <#list auth.authorities as authority>
        <#if authority.authority == "ROLE_admin">
            <#assign admin = true>
        </#if>
    </#list>
</#if>
<#include "/jmaster/components.ftlh">

<#macro layout>
    <#if ajax??><#nested><#return></#if>
    <!doctype html>
    <html lang="en">
    <head>
        <#include "/_/layout/head.ftlh">
    </head>
    <body>
        <div id="geoServerVersion" class="geo-version-badge geo-version-server">${TemplateUtil.getBuildVersion()}</div>
        <header class="geo-header">
            <nav class="navbar navbar-expand-lg bg-body border-bottom">
                <div class="container-fluid geo-shell">
                    <a class="navbar-brand geo-brand" href="/">
                        <img class="geo-brand-mark" src="/static/favicon.svg" alt="">
                        <span>etm</span>
                    </a>
                    <#if admin>
                        <#include "/_/layout/admin-menu.ftlh">
                    </#if>
                </div>
            </nav>
        </header>

        <main class="geo-main">
            <div class="container-fluid geo-shell">
                <#include "/_/layout/messages.ftlh">
                <#nested>
            </div>
        </main>

    </body>
    </html>
</#macro>
