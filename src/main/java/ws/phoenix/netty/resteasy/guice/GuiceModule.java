package ws.phoenix.netty.resteasy.guice;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.annotation.PostConstruct;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by xux on 14-3-12.
 */
public final class GuiceModule  extends AbstractModule{

    private Map<String,String> properties;
    private ResteasyDeployment rd;

    public GuiceModule(Map<String,String> properties,ResteasyDeployment rd){
        this.properties = properties;

        this.rd = rd;
    }
    @Override
    protected void configure() {

        //default values
        defaultValue("netty.rootpath",String.class,"");
        defaultValue("resteasy.guice.stage",String.class,Stage.PRODUCTION.toString());
        defaultValue("netty.threadCount",Integer.class,16);
        defaultValue("netty.backlog",Integer.class,128);


        Names.bindProperties(binder(), properties );
        bindRequestScope();
        bindResource();
        bindProvider();

        bind(GuiceNettyJaxrsServer.class);
    }

    @Provides
    @Singleton
    ResteasyDeployment getResteasyDeployment(){
        return rd;
    }

    private void bindResource(){
        for(String pkg : properties.get("resteasy.scan.pkgs").split("\\s+")){
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(pkg)))
                    .setUrls(ClasspathHelper.forPackage(pkg))
                    .setScanners(new TypeAnnotationsScanner()));
            Set<Class<?>> pathClass = reflections.getTypesAnnotatedWith(Path.class);
            for (Class<?> resource : pathClass){
                bind(resource);
            }
            // install modules
            install(reflections);
        }
    }
    private void install(Reflections reflections){
        Set<Class<?>> moduleClass = reflections.getTypesAnnotatedWith(Module.class);
        for(Class<?> module : moduleClass){
            install(com.google.inject.Module.class.cast(module));
        }
    }
    private void bindProvider(){
        for (String provider : rd.getScannedProviderClasses()){
            try {
                bind(Class.forName(provider));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private <T> void defaultValue(String propName,Class<T> clazz,T value){
        if(!properties.containsKey(propName)){
            bind(clazz).annotatedWith(Names.named(propName)).toInstance(value);
        }
    }
    /**
     * @see <code>>org.jboss.resteasy.plugins.guice.ext.RequestScopeModule.ResteasyContextProvider</code>
     */
    private void bindRequestScope(){
        bindScope(RequestScoped.class, new Scope()
        {
            @Override
            public <T> com.google.inject.Provider<T> scope(final Key<T> key, final com.google.inject.Provider<T> creator)
            {
                return new com.google.inject.Provider<T>()
                {
                    @SuppressWarnings("unchecked")
                    @Override
                    public T get()
                    {
                        Class<T> instanceClass = (Class<T>) key.getTypeLiteral().getType();
                        T instance = ResteasyProviderFactory.getContextData(instanceClass);

                        if (instance == null) {
                            instance = creator.get();
                            ResteasyProviderFactory.pushContext(instanceClass, instance);
                        }

                        return instance;
                    }

                    @Override
                    public String toString() {
                        return String.format("%s[%s]", creator, this);
                    }
                };
            }
        });
        bind(HttpRequest.class).toProvider(new ResteasyContextProvider<HttpRequest>(HttpRequest.class)).in(RequestScoped.class);
        bind(HttpResponse.class).toProvider(new ResteasyContextProvider<HttpResponse>(HttpResponse.class)).in(RequestScoped.class);
        bind(Request.class).toProvider(new ResteasyContextProvider<Request>(Request.class)).in(RequestScoped.class);
        bind(HttpHeaders.class).toProvider(new ResteasyContextProvider<HttpHeaders>(HttpHeaders.class)).in(RequestScoped.class);
        bind(UriInfo.class).toProvider(new ResteasyContextProvider<UriInfo>(UriInfo.class)).in(RequestScoped.class);
        bind(SecurityContext.class).toProvider(new ResteasyContextProvider<SecurityContext>(SecurityContext.class)).in(RequestScoped.class);
    }

    /**
     * Copy RequestScopeModule.ResteasyContextProvider
     * @see <code>org.jboss.resteasy.plugins.guice.ext.RequestScopeModule.ResteasyContextProvider</code>
     * @param <T>
     */
    private static class ResteasyContextProvider<T> implements com.google.inject.Provider<T> {

        private final Class<T> instanceClass;

        public ResteasyContextProvider(Class<T> instanceClass)
        {
            this.instanceClass = instanceClass;
        }

        @Override
        public T get() {
            return ResteasyProviderFactory.getContextData(instanceClass);
        }
    }



}
