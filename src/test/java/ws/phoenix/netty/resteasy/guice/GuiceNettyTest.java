package ws.phoenix.netty.resteasy.guice;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.*;
import com.google.inject.Module;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by xux on 14-3-12.
 */
public class GuiceNettyTest {

    @Test
    public void testNettyBootstrap(){
        Person p = new Person();
        p.setName("phoenix");
        NettyBootstrap bootstrap = new NettyBootstrap();
        bootstrap.start();
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
        bootstrap.stop();
    }

    @Test
    public void testResteasyNettyBootstrap(){
        Person p = new Person();
        p.setName("phoenix");
        new ResteasyNettyBootstrap().start();
        Client client =  ClientBuilder.newClient();
        Response response = client.target("http://localhost:8976").path("/test/test4/48").request().post(Entity.entity(p, MediaType.APPLICATION_JSON));
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
        System.out.println("Server response : \n");
        System.out.println(response.readEntity(Person.class));
        response.close();
    }

    @Test
    public void testGuiceModule(){
        Person p = new Person();
        p.setName("phoenix");
        ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        map.put("netty.port","8976");
//        map.put("netty.rootpath","");
//        map.put("netty.backlog","128");
//        map.put("netty.threadCount","16");
        map.put("resteasy.scan.pkgs","ws.phoenix.netty.resteasy.guice");
//        map.put("resteasy.guice.stage","DEVELOPMENT");
        ResteasyDeployment rd = new ResteasyDeployment();
        new ResteasyNettyBootstrap(map.build()).injector(Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {

            }
        })).start();

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

    }
}
