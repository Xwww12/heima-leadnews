package com.heima.user.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserFollowMapper {
    public Boolean follow(Integer userId, Integer followId);

    public Boolean fan(Integer userId, Integer followId);

    Boolean cancelFan(Integer userId, Integer authorId);

    Boolean cancelFollow(Integer userId, Integer authorId);
}
