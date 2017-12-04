package com.czhappy.mina;

import java.net.InetSocketAddress;
import java.util.Date;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class MinaService {

	public static void main(String[] args) {
		IoAcceptor acceptor = new NioSocketAcceptor();
		//添加日志过滤器
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
		acceptor.setHandler(new DemoServerHandler());
		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		try {
			acceptor.bind(new InetSocketAddress(9226));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("启动服务");
	}
	
	/**  
	 * @ClassName: DemoServerHandler  
	 * @Description: 负责session对象的创建和监听以及消息的创建和接收监听
	 * @author chenzheng
	 * @date 2016-12-9 下午3:57:11  
	 */
	private static class DemoServerHandler extends IoHandlerAdapter{
		
		//服务器与客户端创建连接
		@Override
		public void sessionCreated(IoSession session) throws Exception {
			System.out.println("服务器与客户端创建连接...");
			super.sessionCreated(session);
		}

		
		@Override
		public void sessionOpened(IoSession session) throws Exception {
			System.out.println("服务器与客户端连接打开...");
			super.sessionOpened(session);
		}
		
		//消息的接收处理
		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {
			// TODO Auto-generated method stub
			super.messageReceived(session, message);
			String str = message.toString();
			Date date = new Date();
			session.write(date.toString());
			System.out.println("接收到的数据："+str);
			messageSent(session, "我接受到数据了");
		}
		
		@Override
		public void messageSent(IoSession session, Object message)
				throws Exception {
			// TODO Auto-generated method stub
			super.messageSent(session, message);
		}
		
		@Override
		public void sessionClosed(IoSession session) throws Exception {
			// TODO Auto-generated method stub
			super.sessionClosed(session);
		}
	}
}
