package ws.phoenix.netty.resteasy.guice;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.resteasy.spi.HttpRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.Map;

/**
 * Created by xux on 14-3-6.
 */
@Path("/test")
@Singleton
public class TestEndpoint {

    @Inject
    @Context
    private HttpRequest request;

    @GET
    @Path("/say")
    @Produces("text/plain")
    public String say(){
        System.out.println(this);
        return "helloword!";
    }

    @GET
    @Path("/test1")
    @Produces(value="application/json;charset=UTF-8")
    public Map test1(){
        Map<String,String> map = Maps.newHashMap();
        System.out.println(this);
        map.put("name","徐翔");
        map.put("address","西安");
        map.put("path",request.getUri().getPath());
        return map;
    }


    @GET
    @Path("/test2/{name}")
    @Produces("application/json;charset=UTF-8")
    public Map test12(@PathParam("name")String name){
        System.out.println(this);
        Map<String,String> map = Maps.newHashMap();
        map.put("name",name);
        map.put("address","西安");
        return map;
    }

    @POST
    @Path("/test3")
    @Consumes("application/json")
    @Produces("application/json;charset=UTF-8")
    public Map test13(Map<String,String> map){
        System.out.println(this);
        return map;
    }

    @POST
    @Path("/test4/{id}")
    @Consumes("application/json")
    @Produces("application/json;charset=UTF-8")
    public Person test14(@PathParam("id") int id,Person person){
        person.setId(id);
        System.out.println(this);
        System.out.println(request);
        return person;
    }
}
