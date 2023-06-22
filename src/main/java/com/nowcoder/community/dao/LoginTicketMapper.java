package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {
    // 用注解的方式写SQL语句，注意在SQL语句中的空格，自动拼接多个字符串成一个完整SQL语句

    // 插入登录凭证
    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    // 用于自动生成主键
    int insertLoginTicket(LoginTicket loginTicket);

    // 根据凭证查询登录凭证
    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    // 根据凭证更新状态
    @Update({
            "update login_ticket set status=#{status} where ticket=#{ticket}"
    })
    // 动态SQL
    //    @Update({
    //            "<script>",
    //            "update login_ticket set status=#{status} where ticket=#{ticket}"
    //            "<if test=\"ticket!=null\">",
    //            "and 1=1",
    //            "</if>",
    //            "</script>"
    //    })
    int updateStatus(String ticket, int status);
}
