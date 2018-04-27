#tag loadSql("queryUserByName2")
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