package ws.phoenix.netty.resteasy.guice;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * Created by xux on 14-3-12.
 */
public class GuiceNettyTest {

    @Test
    public void test1(){
        Person p = new Person();
        p.setName("phoenix");
        new NettyBootstrap().createDeployment().createInjector().start();
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

    @Test
    public void testResteasyNettyBootstrap(){
        Person p = new Person();
        p.setName("phoenix");
        new ResteasyNettyBootstrap().createDeployment().createInjector().start();
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
}
