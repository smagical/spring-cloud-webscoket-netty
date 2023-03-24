# spring-cloud-webscoket-netty

#### 介绍

#### 软件架构

```text
annotation   注解类
    involve  websocket几个常用注解 如@OnMessage表示接受信息
    param  参数解析注解，参考springmvc
    ServerEndpoint websocket端点注解
config
   WebSocketAutoConfiguration 自动配置
   WebSocketMethodParamResloveConfiguration 参数处理器配置
   WebSocketProperties websocket配置信息
   WebSocketScan 端点扫描类
constant
exception
pojo
  Session websocket会话，简单封装了Channel
server
   handler 
       DistributeHander 负责http请求升级为websocket请求
       WebSocketEcodeHander 简单编码，把session中直接输出的的对象包装在WebSocketFrame,可覆盖自己实现
       WebSocketHander 处理websocket相关注解方法的调用,可覆盖自己实现
   WebSocketServer netty server的实现
support   
   event 
     WebSocketServerHandshakerEvent 握手事件
   json json处理器，用于参数处理
   methodparamreslove 参数处理器,默认order排序，覆盖只需要添加个同样支持同类型参数的处理器并把order设置较小
   PathServerEndpointMapping 存储所有端点端点信息 （path->端点）
   ServerEndpointMethodMapping 处理端点方法，把于注解相对应的方法保存起来
   WebSocketMethodParamResloveCollection 所有的参数处理器，用来获取对应参数的处理器
       
```

#### 使用教程

无(随缘)

#### 待处理(有待添加...)
- stomp Stomp协议
- 参数处理器不太合理
  - 依赖json转换，只支持json，string
  - 待续...
- session封装不全
- WebSocketEcodeHander  只能转json
- http服务
- 引用释放问题,部分netty msg重复消费
- 没有解码器


ps: 估计没希望了.......随缘
