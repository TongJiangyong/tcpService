package com.realTek.tcpService;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
/**
 * 网络多客户端聊天室
 * 功能1： 客户端通过Java NIO连接到服务端，支持多客户端的连接
 * 功能2：客户端初次连接时，服务端提示输入昵称，如果昵称已经有人使用，提示重新输入，如果昵称唯一，则登录成功，之后发送消息都需要按照规定格式带着昵称发送消息
 * 功能3：客户端登录后，发送已经设置好的欢迎信息和在线人数给客户端，并且通知其他客户端该客户端上线
 * 功能4：服务器收到已登录客户端输入内容，转发至其他登录客户端。
 * 修改部分：
 * 增加下线功能.....
 * 包括心跳，下线信息提示 ，这部分可以用spring做aop来处理，很重要！！！
 * 
 * TODO 客户端下线检测
 */
public class ChatRoomServer {
	private Gson gson =new Gson();  
    private Selector selector = null;
	SimpleDateFormat formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static final int port = 9999;
    private Charset charset = Charset.forName("UTF-8");
    private int  bufferSize = 4096; //注意区块的大小
    private static String msgToServer ="0";
    private static String msgBroadcast ="123456";
    private static String msgToSelf ="654321";  
    private static String splitSymble ="#";
    private static double errorTime = 0;
    //记录连接对象 自己的flag ---自己的channel
    private Map<String, SocketChannel>  userList= new HashMap<String, SocketChannel>();
    //记录连接对象 自己的channel ---自己的flag
    private Map<SocketChannel,String >  userDeleteList= new HashMap<SocketChannel,String>();
    public void init() throws IOException
    {
        selector = Selector.open();
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));
        //非阻塞的方式
        server.configureBlocking(false);
        //注册到选择器上，设置为监听状态
        server.register(selector, SelectionKey.OP_ACCEPT);
        
        System.out.println("Server is listening now...");
        
        while(true) {
            int readyChannels = selector.select();
            if(readyChannels == 0) continue; 
            Set selectedKeys = selector.selectedKeys();  //可以通过这个方法，知道可用通道的集合
            Iterator keyIterator = selectedKeys.iterator();
            while(keyIterator.hasNext()) {
                 SelectionKey sk = (SelectionKey) keyIterator.next();
                 keyIterator.remove();
                 dealWithSelectionKey(server,sk);
            }
        }
    }
    
    //最好return不同的伪代码
    public boolean dealWithSelectionKey(ServerSocketChannel server,SelectionKey sk) throws IOException {
        if(sk.isAcceptable())
        {
            SocketChannel sc = server.accept();
            //非阻塞模式
            sc.configureBlocking(false);
            //注册选择器，并设置为读取模式，收到一个连接请求，然后起一个SocketChannel，并注册到selector上，之后这个连接的数据，就由这个SocketChannel处理
            if(sc!=null){
            	//坐下面的事儿
            	//sc有socket相关的信息
            }
            sc.register(selector, SelectionKey.OP_READ);
            //将此对应的channel设置为准备接受其他客户端请求,加入一个新的客户端
            sk.interestOps(SelectionKey.OP_ACCEPT);
            System.out.println("time:"+formatTime.format(new Date())+", Server is accepted from a new client :" + sc.getRemoteAddress());
        }
        //处理来自客户端的数据读取请求
        if(sk.isReadable())
        {
            //返回该SelectionKey对应的 Channel，其中有数据需要读取,则读取它送过来的数据
            SocketChannel sc = (SocketChannel)sk.channel();
           //获取数据
            ByteBuffer buff = ByteBuffer.allocate(bufferSize);
            String content = null;
            BufferedReader in;
            try
            {

                while(sc.read(buff) > 0)
                {
                    buff.flip();
                    content=charset.newDecoder().decode(buff).toString().trim(); 
                    //这里感觉用in，也不太对，容易出问题.......，比如有的buff中的数据，只读到一个.....
                    //in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buff.array())));
                    //content =in.readLine();
                }
                //一下的断开方法，仅仅对socket有效好像是，那客户端就通过socket来做吧.....服务器就改动这个.....
                if(sc.read(buff) == -1){
                    System.out.println("client disconnection activity" + sc.socket().getRemoteSocketAddress());
                    //下线通知，更新这里，并更新数据库
                    sc.close();
                    return false;
                }
                //System.out.println("Server is listening from client " + sc.getRemoteAddress());
                //将此对应的channel设置为准备下一次接受数据
                sk.interestOps(SelectionKey.OP_READ);
            }
            catch (IOException io)
            {
                sk.cancel();
                System.out.println("read or write error "+io);
                if(sk.channel() != null)
                {
                    sk.channel().close();
                    System.out.println("client disconnection client" + sc.socket().getRemoteSocketAddress());
                    //下线通知，更新这里，并更新数据库
                    this.clientDisconnect(userList,userDeleteList,sk);
                    return false;
                }
            }
            //大于一定的字节才进行处理
            if(content.length() > 5)
            {
            	//List<Data> reciveData = null;
            	Data reciveData = null;
            	System.out.println("*******************************************************"); 
            	System.out.println("data is:");
            	System.out.println(content);
                System.out.println("服务器收到的时间："+formatTime.format(new Date()));
            	String[] msgs = content.trim().split("&");
            	System.out.println("数据点数目为："+msgs.length);
            	//分割每一个字符，并释放
            	for(int i =0;i<msgs.length;i++){
                	try{  	
                		//String [] buffer =content.split("$");
                		//然后在这处理？
                		//reciveData = gson.fromJson(content, new TypeToken<List<Data>>(){}.getType());
                		reciveData = gson.fromJson(msgs[i], Data.class);
                		//System.out.println("recive info is "+reciveData.getFlag());
                	}catch (JsonSyntaxException e)
                    {
                		errorTime++;
                		System.out.println("error info "+e);
                		System.out.println("data format error time is :"+errorTime);
                		//跳过当前的数据点不处理.....
                		continue;
                    }
                	//处理对应的数据点
                    	//获取标志
                    	String[] flag = reciveData.getFlag().split(splitSymble);
                		//处理初次连接
                		if(flag[0].equals(msgToServer)){
                			//初次连接，即为通道进行注册
                			System.out.println("meg info :"+flag[0]);
                			userList.put(flag[1], sc);
                			userDeleteList.put(sc, flag[1]);
                		//处理广播主要是服务器的控制处理，比如发送信息什么的....
                		}else if(flag[0].equals(msgBroadcast)){
                			//直接进行转发即可
                			this.broadCastInfo(selector, sc, msgs[i]);
                		//返回给自身
                		}else if(flag[0].equals(msgToSelf)){
                			//buff.flip(); 以后再处理吧.....
                			sc.write(charset.encode((msgs[i])+"&"));
                			System.out.println("数据返回自身时间："+formatTime.format(new Date()));
                			//sc.write(ByteBuffer.wrap((content+'\n').getBytes()));
                		}
                			//处理互相之间的连接
                		else{
                			if(!userList.containsKey(flag)){
                                sk.channel().close();
                                System.out.println("client disconnection activity" + sc.socket().getRemoteSocketAddress());
                                //错误通知，并更新这里，并更新数据库
                                sc.close();
                                return false;	
                			}
                			try {
                				
                				this.sendToClient(userList,flag[0],msgs[i]);
                			} catch (IOException e) {
                				System.out.println("可能会出现读取出错，但是不用管.....");
                				//处理出错，以后碰着再说吧.....
                				e.printStackTrace();
                			}
                		      		
                	}		
            	}
            }
            
        }
        return true;
    }
    
    /**下线处理的过程
     * 
     * 1、socket断开,channel断开
     * 2、userlist表除名，如果可能，给互联用户下线通知 下线的逻辑为广播一下，然后让其他人做对比......
     * 3、数据库进行更新
     * 4、日志记录
     * 
    **/
    private void clientDisconnect(Map<String, SocketChannel> userList,Map<SocketChannel,String> userDeleteList,SelectionKey sk) {
    	
    		userList.remove(userDeleteList.get(sk.channel())); 
       //TODO 设计下线格式
	       try {
	    	   this.broadCastInfo(selector, (SocketChannel)sk.channel(), "下线");
	    	   sk.channel().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	       //TODO 数据库更新
	       //这里对数据库的操作以后再做......，即下线的影响，以后再处理......
	       //TODO 日志记录	       		
	}

	private void sendToClient(Map<String, SocketChannel> userList,String flag, String info) throws IOException {   			
		userList.get(flag).write(charset.encode(info+"&"));
	}
    
    public void broadCastInfo(Selector selector, SocketChannel selfChannel, String info) throws IOException {
        //广播数据到所有的SocketChannel中
        for(SelectionKey key : selector.keys())
        {
            Channel targetchannel = key.channel();
            //如果except不为空，不回发给发送此内容的客户端
            if(targetchannel instanceof SocketChannel && targetchannel!=selfChannel)
            {
                SocketChannel dest = (SocketChannel)targetchannel;
                dest.write(charset.encode(info+'&'));
            }
        }
    }

    
    public static void main(String[] args) throws IOException 
    {
        new ChatRoomServer().init();
    }
}