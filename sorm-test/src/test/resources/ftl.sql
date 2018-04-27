<@loadSql name="queryUserByName">
<#noparse>
    select * from wp_users
    <@where>

        <#if id ??>
        and id=${p(id)}
        </#if>

        <#if username ??>
        and login_name=${p(username)}
        </#if>

        <#if nicename ??>
        and nicename=${p(nicename)}
        </#if>

        <#if ids ??>
        and nicename ${sqlIn(ids)}
        </#if>

   </@where>

</#noparse>
</@loadSql>

<@loadSql name="queryUserByName2">
<#noparse>
    select * from wp_users
    <@where>

        <#if id ??>
        and id=${p(id)}
        </#if>

        <#if username ??>
        and login_name=${p(username)}
        </#if>

        <#if ids ??>
        and nicename ${in(ids)}
        </#if>
   </@where>

</#noparse>
</@loadSql>