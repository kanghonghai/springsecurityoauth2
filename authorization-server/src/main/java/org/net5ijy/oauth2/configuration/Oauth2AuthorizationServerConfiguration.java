package org.net5ijy.oauth2.configuration;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.net5ijy.oauth2.provider.code.RedisAuthorizationCodeServices;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

/**
 * Oauth2授权服务器配置
 *
 * @author xuguofeng
 * @date 2019/8/28 11:23
 */
@Configuration
public class Oauth2AuthorizationServerConfiguration extends
    AuthorizationServerConfigurerAdapter {

  @Resource
  private UserDetailsService userDetailsService;

  @Resource
  private DataSource dataSource;

  @Resource
  private RedisConnectionFactory redisConnectionFactory;

  @Override
  public void configure(AuthorizationServerSecurityConfigurer security) {
    security.checkTokenAccess("hasAuthority('ROLE_TRUSTED_CLIENT')");
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients)
      throws Exception {
    // 数据库管理client
    clients.withClientDetails(new JdbcClientDetailsService(dataSource));
  }

  @Bean
  public EmbeddedServletContainerFactory getEmbeddedServletContainerFactory() {
    TomcatEmbeddedServletContainerFactory containerFactory = new TomcatEmbeddedServletContainerFactory();
    containerFactory
        .addConnectorCustomizers(new TomcatConnectorCustomizer() {
          @Override
          public void customize(Connector connector) {
            ((AbstractProtocol) connector.getProtocolHandler()).setConnectionTimeout(600000);
          }
        });
    return containerFactory;
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {

    // 用户信息查询服务
    endpoints.userDetailsService(userDetailsService);

    // 数据库管理access_token和refresh_token
    TokenStore tokenStore = new JdbcTokenStore(dataSource);

    // Redis管理access_token和refresh_token
    RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);

    endpoints.tokenStore(redisTokenStore);

    DefaultTokenServices tokenServices = new DefaultTokenServices();
    tokenServices.setTokenStore(redisTokenStore);
    tokenServices.setSupportRefreshToken(true);
    tokenServices.setClientDetailsService(new JdbcClientDetailsService(dataSource));
    // tokenServices.setAccessTokenValiditySeconds(180);
    // tokenServices.setRefreshTokenValiditySeconds(180);

    endpoints.tokenServices(tokenServices);

    // 数据库管理授权码
//    endpoints.authorizationCodeServices(new JdbcAuthorizationCodeServices(dataSource));
    endpoints.authorizationCodeServices(new RedisAuthorizationCodeServices(redisConnectionFactory));
    // 数据库管理授权信息
    ApprovalStore approvalStore = new JdbcApprovalStore(dataSource);

    TokenApprovalStore tokenApprovalStore = new TokenApprovalStore();
    tokenApprovalStore.setTokenStore(redisTokenStore);

    endpoints.approvalStore(tokenApprovalStore);

    DefaultAccessTokenConverter defaultAccessTokenConverter = new DefaultAccessTokenConverter();
    defaultAccessTokenConverter.setUserTokenConverter(new CustomUserAuthenticationConverter());

    endpoints.accessTokenConverter(defaultAccessTokenConverter);
  }
}
