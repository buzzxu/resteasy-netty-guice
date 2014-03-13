package ws.phoenix.netty.resteasy.guice;

import com.google.common.collect.ImmutableMap;

import java.util.Properties;

/**
 * Created by xux on 14-3-12.
 */
public final class ResteasyNettyBootstrap extends NettyBootstrap {

    public ResteasyNettyBootstrap(){
        super();
    }

    public ResteasyNettyBootstrap(Properties prop) {
        super(prop);
    }

    public ResteasyNettyBootstrap(ImmutableMap<String,String> properties){
        super(properties);
    }

    @Override
    public NettyBootstrap createDeployment() {
        ConfigurationBootstrap config = new ConfigurationBootstrap(this);
        return super.createDeployment(config.createDeployment());
    }
}
