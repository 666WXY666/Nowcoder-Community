package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status; // 0:有效; 1:无效
    private Date expired; // 过期时间
}
