package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");
    }

    /**
     * 通过knife4j生成接口文档
     * @return
     */

//      生成Swagger文档核心Bean对象，用于配置接口文档全局信息、扫描接口范围

    @Bean
    public Docket docket1() {
        log.info("接口文档生成中...");
        Contact contact = new Contact("时间的过客", "项目地址", "邮箱");

        // 构建API文档基础信息对象
        ApiInfo apiInfo = new ApiInfoBuilder()
                // 文档标题，浏览器页面顶部展示的大标题
                .title("苍穹外卖项目接口文档")
                //配置联系人信息：作者姓名、邮箱、项目地址，需要先构建 Contact 对象传入。
                .contact(contact)
                // 项目接口文档版本号
                .version("2.0")
                // 文档描述，用来介绍项目、接口整体说明
                .description("苍穹外卖项目接口文档")
                // 完成ApiInfo对象构建
                .build();

        // 创建Docket文档配置对象，指定使用Swagger2规范生成文档
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                //文档分组
                .groupName("管理端")
                // 将上面构建好的文档基础信息绑定到当前文档实例
                .apiInfo(apiInfo)
                // 开启接口扫描筛选配置
                .select()
                // 指定扫描接口的根包路径，只会解析com.sky.controller下所有Controller接口
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.admin"))
                // 路径过滤规则：匹配所有接口路径，不做路径排除
                .paths(PathSelectors.any())
                // 完成Docket对象构建
                .build();
        return docket;
    }
    @Bean
    public Docket docket2() {
        log.info("接口文档生成中...");
        Contact contact = new Contact("时间的过客", "项目地址", "邮箱");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .contact(contact)
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("用户端")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.user"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射...");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        //先创建消息转换器对象
        MappingJackson2HttpMessageConverter converter =new MappingJackson2HttpMessageConverter();
        //给消息创建一个对象转换器,对象转化器可以将java对象转换成json数据
        converter.setObjectMapper(new JacksonObjectMapper());
        //将消息转化器加入到容器中去,并增加消息转化器的在容器中的位置
        converters.add(0,converter);
        //用来将时间数据转化为json数据
    }
}
