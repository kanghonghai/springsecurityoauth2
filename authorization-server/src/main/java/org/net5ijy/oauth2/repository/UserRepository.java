package org.net5ijy.oauth2.repository;

import org.net5ijy.oauth2.entity.User;

/**
 * 用户数据层
 *
 * @author xuguofeng
 * @date 2019/8/28 12:04
 */
public interface UserRepository {

  /**
   * 根据用户名查询用户信息
   *
   * @param username 用户名
   * @return 用户对象
   * @author xuguofeng
   * @date 2019/8/28 12:04
   */
  User findByUsername(String username);
}
