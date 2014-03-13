package ws.phoenix.netty.resteasy.guice;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.*;
import com.google.inject.name.Names;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.spi.ResteasyDeployment;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.inject.Module;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
/**
 * Created by xux on 14-3-12.
 */
@Singleton
public class NettyBootstrap {
    private static Logger logger = Logger.getLogger(NettyBootstrap.class);
    private Injector injector;
    private ImmutableMap<String, String> properties;
    private List<Module> modules = Lists.newArrayList();
    private ResteasyDeployment deployment;

    public NettyBootstrap(){
        //load netty.properties
        this.properties = Maps.fromProperties(loadProp());
    }
    public NettyBootstrap(Properties prop){
        this.properties = Maps.fromProperties(prop);
    }
    public NettyBootstrap(ImmutableMap<String,String> properties){
        this.properties = properties;
    }
    public NettyBootstrap install(Module... module){
        this.modules.addAll(Lists.newArrayList(module));
;        return this;
    }

    public NettyBootstrap injector(Injector injector){
        if(properties == null || properties.isEmpty()){
            logger.error("resteasy's properties is empty.");
            System.exit(-1);
        }
        createDeployment();
        modules.add(new GuiceModule(properties,deployment));
        this.injector = injector.createChildInjector(modules);
        return this;
    }
    protected NettyBootstrap createInjector(){
        if(injector == null){
            modules.add(new GuiceModule(this.properties,deployment));
            final Stage stage = getStage();
            if (stage == null)
            {
                injector = Guice.createInjector(modules);
            }
            else
            {
                injector = Guice.createInjector(stage, modules);
            }
        }
        ModuleProcessor processor = new ModuleProcessor(deployment.getRegistry(),deployment.getProviderFactory());
        processor.processInjector(injector);

        Injector parent = injector.getParent();
        while (parent != null) {
            processor.processInjector(parent);
            parent = parent.getParent();
        }
        triggerAnnotatedMethods(PostConstruct.class);
        return this;
    }
    protected NettyBootstrap  createDeployment(){
       return createDeployment(null);
    }
    protected NettyBootstrap  createDeployment(ResteasyDeployment rd){
        if(rd == null){
            deployment = new ResteasyDeployment();
        }else{
            deployment = rd;
        }
        deployment.start();
        return this;
    }





    private void triggerAnnotatedMethods(final Class<? extends Annotation> annotationClass)
    {
        for (Module module : this.modules)
        {
            final Method[] methods = module.getClass().getMethods();
            for (Method method : methods)
            {
                if (method.isAnnotationPresent(annotationClass))
                {
                    if(method.getParameterTypes().length > 0)
                    {
                        logger.warn("Cannot execute expected module {}'s @{} method {} because it has unexpected parameters: skipping.", module.getClass().getSimpleName(), annotationClass.getSimpleName(), method.getName());
                        continue;
                    }
                    try
                    {
                        method.invoke(module);
                    } catch (InvocationTargetException ex) {
                        logger.warn("Problem running annotation method @" + annotationClass.getSimpleName(), ex);
                    } catch (IllegalAccessException ex) {
                        logger.warn("Problem running annotation method @" + annotationClass.getSimpleName(), ex);
                    }
                }
            }
        }
    }
    protected Properties loadProp(){
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = NettyBootstrap.class.getResourceAsStream("/netty.properties");
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return properties;
    }
    protected Stage getStage()
    {
        final String stageAsString = properties.get("resteasy.guice.stage");
        if (stageAsString == null)
        {
            return null;
        }
        try
        {
            return Stage.valueOf(stageAsString.trim());
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("Injector stage is not defined properly. " + stageAsString + " is wrong value." +
                    " Possible values are PRODUCTION, DEVELOPMENT, TOOL.");
        }
    }

    protected Map<String,String> getProperties(){
        return this.properties;
    }
    public void start(SecurityDomain domain){
        if(deployment == null){
            createDeployment();
        }
        createInjector();
        GuiceNettyJaxrsServer server = this.injector.getInstance(GuiceNettyJaxrsServer.class);
        server.setSecurityDomain(domain);
        server.start();

    }

    public Injector injector(){
        return this.injector;
    }
    public void start(){
        start(null);
    }

    public void stop(){
        triggerAnnotatedMethods(PreDestroy.class);
        injector.getInstance(ResteasyDeployment.class).stop();
        injector.getInstance(GuiceNettyJaxrsServer.class).stop();

    }

}
