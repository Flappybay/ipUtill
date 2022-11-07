# ipUtill
ip地址获取和归属地查询

## ip归属地查询工具类
- 查询ip归属地是调用第三方ip归属地查询网站得到，因为第三方网站提供的ip归属地查询接口大多需要付费，于是就自己写个工具类爬取ip查询网站数据并解析成需要的格式。
- 这种爬取方式的缺点就是一旦网站页面发生变化就需要重新调整代码解析方式，如果想稳定查询最好还是花点小费通过接口调用。
- 爬取方式采用Jsoup类似Ajax选择器一样获取网页指定位置数据。

## Jsoup的maven依赖

```
  <!--HTML解析器-->
  <dependency>
      <groupId>org.jsoup</groupId>
      <artifactId>jsoup</artifactId>
      <version>1.14.3</version>
  </dependency>
```
