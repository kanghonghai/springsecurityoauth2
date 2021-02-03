package org.net5ijy.oauth2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * org.net5ijy.oauth2.Oauth2Application 类
 *
 * @author xuguofeng
 * @date 2019/8/28 12:13
 */
@SpringBootApplication
@EnableAuthorizationServer
@EnableResourceServer
@MapperScan("org.net5ijy.oauth2.repository")
public class Oauth2Application {

  public static void main(String[] args) {

    // args = new String[] { "--debug" };

    SpringApplication.run(Oauth2Application.class, args);
  }
}
