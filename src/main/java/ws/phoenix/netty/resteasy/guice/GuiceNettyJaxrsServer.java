package ws.phoenix.netty.resteasy.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.netty.*;
import org.jboss.resteasy.spi.ResteasyDeployment;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 *
 * Created by xux on 14-3-11.
 */
@Singleton
public final class GuiceNettyJaxrsServer extends NettyJaxrsServer {
    private EventLoopGroup eventLoopGroup;
    private EventLoopGroup eventExecutor;
    private int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;
    //16
    private int executorThreadCount;
    private SSLContext sslContext;
    private int maxRequestSize = 1024 * 1024 * 10;
    //128
    private int backlog;

    @Inject
    public GuiceNettyJaxrsServer(ResteasyDeployment deployment,@Named("netty.port") String port,
                                 @Named("netty.rootpath")String rootResourcePath,@Named("netty.threadCount") int executorThreadCount,
                                 @Named("netty.backlog") int backlog ){
        super();
        this.port = Integer.valueOf(port);
        this.executorThreadCount = executorThreadCount;
        this.backlog = backlog;
        this.setRootResourcePath(rootResourcePath);
        this.deployment = deployment;
    }

    public void setSSLContext(SSLContext sslContext)
    {
        this.sslContext = sslContext;
    }

    /**
     * Specify the worker count to use. For more information about this please see the javadocs of {@link io.netty.channel.EventLoopGroup}
     *
     * @param ioWorkerCount
     */
    public void setIoWorkerCount(int ioWorkerCount)
    {
        this.ioWorkerCount = ioWorkerCount;
    }

    /**
     * Set the number of threads to use for the EventExecutor. For more information please see the javadocs of {@link io.netty.util.concurrent.EventExecutor}.
     * If you want to disable the use of the {@link io.netty.util.concurrent.EventExecutor} specify a value <= 0.  This should only be done if you are 100% sure that you don't have any blocking
     * code in there.
     *
     * @param executorThreadCount
     */
    public void setExecutorThreadCount(int executorThreadCount)
    {
        this.executorThreadCount = executorThreadCount;
    }

    /**
     * Set the max. request size in bytes. If this size is exceed we will send a "413 Request Entity Too Large" to the client.
     *
     * @param maxRequestSize the max request size. This is 10mb by default.
     */
    public void setMaxRequestSize(int maxRequestSize)
    {
        this.maxRequestSize  = maxRequestSize;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setBacklog(int backlog)
    {
        this.backlog = backlog;
    }


    @Override
    public void start() {
        eventLoopGroup = new NioEventLoopGroup(ioWorkerCount);
        eventExecutor = new NioEventLoopGroup(executorThreadCount);
        // this is the only line that's different in the whole class.
        final RequestDispatcher dispatcher = this.createRequestDispatcher();
        // Configure the server.
        if (sslContext == null) {
            bootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpRequestDecoder());
                            ch.pipeline().addLast(new HttpObjectAggregator(maxRequestSize));
                            ch.pipeline().addLast(new HttpResponseEncoder());
                            ch.pipeline().addLast(new RestEasyHttpRequestDecoder(dispatcher.getDispatcher(), root, RestEasyHttpRequestDecoder.Protocol.HTTP));
                            ch.pipeline().addLast(new RestEasyHttpResponseEncoder(dispatcher));
                            ch.pipeline().addLast(eventExecutor, new RequestHandler(dispatcher));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, backlog)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
        } else {
            final SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(false);
            bootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addFirst(new SslHandler(engine));
                            ch.pipeline().addLast(new HttpRequestDecoder());
                            ch.pipeline().addLast(new HttpObjectAggregator(maxRequestSize));
                            ch.pipeline().addLast(new HttpResponseEncoder());
                            ch.pipeline().addLast(new RestEasyHttpRequestDecoder(dispatcher.getDispatcher(), root, RestEasyHttpRequestDecoder.Protocol.HTTPS));
                            ch.pipeline().addLast(new RestEasyHttpResponseEncoder(dispatcher));
                            ch.pipeline().addLast(eventExecutor, new RequestHandler(dispatcher));

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, backlog)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
        }

        bootstrap.bind(port).syncUninterruptibly();

    }

    protected RequestDispatcher createRequestDispatcher() {
        return new GuiceRequestDispatcher((SynchronousDispatcher)deployment.getDispatcher(),
                deployment.getProviderFactory(), domain);
    }
    @Override
    public void stop() {
        eventLoopGroup.shutdownGracefully();
        eventExecutor.shutdownGracefully();
    }
}
