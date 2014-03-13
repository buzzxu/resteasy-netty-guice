# resteasy-netty-guice

Resteasy结合netty提供RESTful Web Services，使用Guice作为依赖注入容器
# Examples

        public void startServer() {
        
          Person p = new Person();
          p.setName("phoenix");
          final NettyBootstrap server = new NettyBootstrap().createDeployment().createInjector().start();
          ResteasyClient client = new ResteasyClientBuilder().build();
          ResteasyWebTarget target = client.target("http://localhost:8976/test/test4/45");
          Response response = target.request().post(Entity.entity(p, MediaType.APPLICATION_JSON));
  
          if (response.getStatus() != 200) {
              throw new RuntimeException("Failed : HTTP error code : "
                              + response.getStatus());
          }
          System.out.println("Server response : \n");
          System.out.println(response.readEntity(Person.class));
          response.close();
          
          // Arrange to stop the server at shutdown
          Runtime.getRuntime().addShutdownHook(new Thread() {
              @Override
              public void run() {
                  try {
                      server.stop();
                  } catch (InterruptedException e) {
                      Thread.currentThread().interrupt();
                  }
              }
          });
          
        }