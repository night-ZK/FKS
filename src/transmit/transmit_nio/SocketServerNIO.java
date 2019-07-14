package transmit.transmit_nio;

import message.*;
import threadmangagement.ThreadConsole;
import tools.GetterTools;
import tools.ObjectTool;
import tools.TransmitTool;
import transmit.Controller.KChatController;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class SocketServerNIO {
//	private static ServerSocket _serverSocket;
	//key : ID
	//登录用户
	private static Map<Integer, SocketChannel> _saveChatSocketList;

	private Selector selector;
	ServerSocketChannel serverSocketChannel;
	static {
		_saveChatSocketList = new HashMap();
//		try {
//			_serverSocket = new ServerSocket(8898);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	public SocketServerNIO() {
		try{
			//ServerSocket 通道
			serverSocketChannel = ServerSocketChannel.open();
			//非阻塞模式
			serverSocketChannel.configureBlocking(false);
			//为通道中的Socket绑定端口
			serverSocketChannel.socket().bind(new InetSocketAddress(8898));
			//通道管理器
			this.selector = Selector.open();
			//注册监听事件到管理器
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void startSocketServer() {
		System.out.println("Server started..");
		try{
			while(true){
				//有事件则返回, 无则阻塞
				selector.select();

				Iterator iterator = this.selector.selectedKeys().iterator();
				while (iterator.hasNext()){
					SelectionKey selectionKey = (SelectionKey)iterator.next();
					iterator.remove();//删除已做处理的key
					if(selectionKey.isAcceptable()){
						//是连接事件
						//所属key的服务器通道
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
						//与客户端的通道
						SocketChannel socketChannel = serverSocketChannel.accept();
						//该通道设置为非阻塞
						socketChannel.configureBlocking(false);

						//为该通道注册读数据的监听权限
						socketChannel.register(selector, SelectionKey.OP_READ);
					}else if(selectionKey.isReadable()){
						//发送读事件的通道
						SocketChannel socketChannel = (SocketChannel)selectionKey.channel();
						System.out.println("getMessage..");

						//缓冲区转byte[]
                        byte[] byteArrays = null;
                        try{
                            byteArrays= TransmitTool.channelSteamToByteArraysForNIO(socketChannel);
                        }catch (IOException e){
//                            e.printStackTrace();
                            selectionKey.cancel();
                            socketChannel.socket().close();
                            socketChannel.close();
                        }
                        if (ObjectTool.isNull(byteArrays)) continue;
						//TODO 数组转数据
						MessageModel requestMessageModel = (MessageModel) GetterTools.byteArraysToObject(byteArrays);

						Runnable responseThread = () ->{
							try {
								MessageInterface responseMessageModel = resolutionClientSocket(requestMessageModel, socketChannel);

								if (ObjectTool.isNull(responseMessageModel)) return;
								//TODO 处理后回复数据
								ByteBuffer responseByteBuffer = TransmitTool.sendResponseMessage(responseMessageModel);
								socketChannel.write(responseByteBuffer);
							}catch (IOException e){
								e.printStackTrace();
							}
						};
						ThreadConsole.useThreadPool().execute(responseThread);
					}

				}
			}
		}catch (IOException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e){
			e.printStackTrace();
		}

	}

	private MessageInterface resolutionClientSocket(MessageModel requestMessageModel, SocketChannel socketChannel) throws IOException {

		MessageInterface responseMessageModel;

		if (ObjectTool.isNull(requestMessageModel)) {
			return null;
		}

		MessageHead messageHead = requestMessageModel.getMessageHead();
		Integer requestType = messageHead.getType();
//		switch (requestType) {
//			case 1:
////				responseMessageModel = BusinessProcess.loginServer(requestMessageModel);
////				MessageContext mc = null;
////				if (responseMessageModel instanceof MessageModel){
////					mc = ((MessageModel) responseMessageModel).getMessageContext();
////				}
////				//check
////				if (!ObjectTool.isNull(mc)){
////					ArrayList loginContext = (ArrayList) mc.getObject();
////					User loginUser = (User)loginContext.get(0);
////					Integer idKey = loginUser.getId().intValue();
////					if(_saveChatSocketList.containsKey(idKey)){
////						//message: 该用户已在线
////						_saveChatSocketList.replace(idKey, socketChannel);
////					}else{
////						_saveChatSocketList.put(idKey, socketChannel);
////					}
////					if (!loginUser.getUserState().contains("zk")){
////						loginUser.setUserState("0");
////					}
////					BusinessProcess.signIn(idKey);
////				}
//				break;
//			case 2:
////				responseMessageModel = BusinessProcess.getFriendsIDServer(requestMessageModel);
//				break;
//
//			case 3:
////				responseMessageModel = BusinessProcess.getUserFriendInfoServer(requestMessageModel);
//				break;
//
//			case 4:
////				responseMessageModel = BusinessProcess.getUserFriendImageServer(requestMessageModel);
//				break;
//
//			case 5:
////				forwardMessage(requestMessageModel);
//				break;
//
//			case 6:
////				responseMessageModel = BusinessProcess.getUserFriendInfoListServer(requestMessageModel);
//				break;
//            case 7:
////            	closeSocketChannel(socketChannel, requestMessageModel);
//                break;
//			case 8:
////				responseMessageModel = BusinessProcess.updateUserInformationServer(requestMessageModel);
//				break;
//			default:
//				break;
//		}

		responseMessageModel = TransmitTool.getResponseByControllerClass(KChatController.class
				, requestType
				, socketChannel
				, requestMessageModel);

		return responseMessageModel;
	}

//    private void closeSocketChannel(@NotNull SocketChannel socketChannel, MessageModel requestMessageModel) throws IOException {
//	    String line = "state:close";
//        byte[] closeBytes = line.getBytes("UTF-8");
//        ByteBuffer closeBuffer = TransmitTool.sendResponseForNIDByRule(closeBytes);
//        socketChannel.write(closeBuffer);
////        socketChannel.get
//        socketChannel.close();
//        String closeDescribe = requestMessageModel.getMessageHead().getRequestDescribe();
//        closeDescribe = closeDescribe.replace("/","");
//        System.out.println("closeDescribe: " + closeDescribe);
//        if (ObjectTool.isInteger(closeDescribe)){
//			Integer userId = Integer.parseInt(closeDescribe);
//			//TODO 改变在线状态
//			if(_saveChatSocketList.containsKey(userId)){
//				BusinessProcess.signOut(userId);
//			}
//		}
//    }

//    private void forwardMessage(@NotNull MessageModel messageModel) throws IOException {
//		ByteBuffer forwardByteBuffer = TransmitTool.sendResponseMessage(messageModel);
//		ChatMessages chatMessage = (ChatMessages)messageModel.getMessageContext();
//		if(_saveChatSocketList.containsKey(chatMessage.getGetterID())){
//			SocketChannel forwardSocketChannel = _saveChatSocketList.get(chatMessage.getGetterID());
//			forwardSocketChannel.write(forwardByteBuffer);
//		}
//	}


	/**
	 * 获得服务器存放Socket的Map
	 * @return 存放Socket的Map
	 */
	public static Map<Integer, SocketChannel> get_saveChatSocketList() {
		return _saveChatSocketList;
	}

	/**
	 * 添加键值对到Map
	 * @param senderID key
	 * @param socketChannel value
	 */
	public synchronized static void addSaveChatSocketList(Integer senderID, SocketChannel socketChannel) {
		_saveChatSocketList.put(senderID, socketChannel);
	}

	/**
	 * 通过userID获得服务器与客户端的Socket, 用于消息的转发
	 * @param key_UserId
	 * @return 用于转发的Socket
	 */
	public static SocketChannel getChatObjectSocket(Integer key_UserId) {
		return SocketServerNIO.get_saveChatSocketList().get(key_UserId);
	}

//	public static void main(String[] args) {
//		new SocketServerNIO().startSocketServer();
//	}
}
