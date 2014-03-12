package ws.phoenix.netty.resteasy.guice;

/**
 * Created by xux on 14-3-12.
 */
public final class ResteasyNettyBootstrap extends NettyBootstrap {

    public ResteasyNettyBootstrap(){
        super();
    }
    @Override
    public NettyBootstrap createDeployment() {
        ListenerBootstrap config = new ListenerBootstrap(this);
        return super.createDeployment(config.createDeployment());
    }
}
