#include("/sql/all.sql")

#tag loadSql("queryContentByName")
#[[
    select * from content where 1=1
    #if(id)
     and id=${p(id)}
    #end

    #if(title)
     and title=${p(title)}
    #end
]]#
#end

#tag loadSql("queryUserByName")
#[[
    select * from wp_users
    #tag where()

        #if(id)
        and id=${p(id)}
        #end

        #if(username)
        and login_name=${p(username)}
        #end

        #if(nicename)
        and nicename=${p(nicename)}
        #end

    #end
]]#
#end