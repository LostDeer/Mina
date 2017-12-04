package com.czhappy.minaclient;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;

/**
 * Description:
 * User: chenzheng
 * Date: 2016/12/9 0009
 * Time: 16:21
 */
public class ConnectionManager {

    private static final String BROADCAST_ACTION = "com.commonlibrary.mina.broadcast";
    private static final String MESSAGE = "message";
    private ConnectionConfig mConfig;
    private WeakReference<Context> mContext;

    private NioSocketConnector mConnection;
    private IoSession mSession;
    private InetSocketAddress mAddress;

    public ConnectionManager(ConnectionConfig config){

        this.mConfig = config;
        this.mContext = new WeakReference<Context>(config.getContext());
        init();
    }

    private void init() {
        // 继承IoService，服务器端接收器
        mAddress = new InetSocketAddress(mConfig.getIp(), mConfig.getPort());
        mConnection = new NioSocketConnector();
        // 设置读缓存区大小
        mConnection.getSessionConfig().setReadBufferSize(mConfig.getReadBufferSize());
        // 如果10秒钟没有任何读写，就设置成空闲状态。 BOTH_IDLE（读和写）
        mConnection.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,10);
        // 添加过滤器
        mConnection.getFilterChain().addLast("logging", new LoggingFilter());
        // 只能传输序列化后的对象
        mConnection.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        // 回调
        mConnection.setHandler(new DefaultHandler(mContext.get()));
        // 设置IP地址和端口
        mConnection.setDefaultRemoteAddress(mAddress);
    }

    /**
     * 与服务器连接
     * @return
     */
    public boolean connnect(){
        Log.e("tag", "准备连接");
        try{
            ConnectFuture future = mConnection.connect();
            future.awaitUninterruptibly();
            mSession = future.getSession();

            SessionManager.getInstance().setSeesion(mSession);

        }catch (Exception e){
            e.printStackTrace();
            Log.e("tag", "连接失败");
            return false;
        }

        return mSession == null ? false : true;
    }

    /**
     * 断开连接
     */
    public void disContect(){
        mConnection.dispose();
        mConnection=null;
        mSession=null;
        mAddress=null;
        mContext = null;
        Log.e("tag", "断开连接");
    }

    private static class DefaultHandler extends IoHandlerAdapter{

        private Context mContext;
        private DefaultHandler(Context context){
            this.mContext = context;

        }
        // session创建时回调
        @Override
        public void sessionCreated(IoSession session) throws Exception {
            super.sessionCreated(session);
        }
        
        // session打开时回调
        @Override
        public void sessionOpened(IoSession session) throws Exception {
            super.sessionOpened(session);
        }

        // 消息接收时回调
        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            Log.e("tag", "接收到服务器端消息："+message.toString());
            if(mContext!=null){
                Intent intent = new Intent(BROADCAST_ACTION);
                intent.putExtra(MESSAGE, message.toString());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }

        // 消息发送时回调
        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
        }

        // session关闭时回调
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);
        }
    }
}
