package transmit;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import message.ChatMessages;
import message.ErrorMessage;
import message.MessageContext;
import message.MessageHead;
import message.MessageInterface;
import message.MessageModel;
import tablejson.ResponesImage;
import threadmangagement.ThreadConsole;
import tools.GetterTools;
import tools.ObjectTool;
import tools.SenderTools;
import tools.TransmitTool;

public class SocketServer {
	private static ServerSocket _serverSocket;
	private static Map<Integer, Socket> _saveChatSocketList;
	

//	InputStream is = null;
//	OutputStream os = null;
	
	static {
		try {
			_serverSocket = new ServerSocket(8898);
			_saveChatSocketList = 
					new HashMap<Integer, Socket>();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	public SocketServer() {
	}
	
	private void startSocketServer() {
		try {
			while(true) {
				System.out.println("wait..");
				Socket socket = _serverSocket.accept();
				System.out.println("some one connect..");
				
				Thread responseThread = new Thread() {
//					boolean isClose = false;
					@Override
					public void run() {
						try {							
							while(true) {
								resolutionClientSocket(socket);
							}
						} catch (Exception e) {
							e.printStackTrace();

						}finally {
							try {
//								if(!ObjectTool.isNull(is)) is.close();
//								if(!ObjectTool.isNull(os)) os.close();
								if(!ObjectTool.isNull(socket)) socket.close();
								System.out.println("this.socket is close: " + socket.isClosed());
//								ThreadConsole.useThreadPool().shutdown();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				};

				ThreadConsole.useThreadPool().execute(responseThread);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void resolutionClientSocket(Socket socket) throws IOException, ClassNotFoundException {

		MessageModel responesMessageModel = null;
		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();

		SenderTools senderTools = new SenderTools(os);

		MessageModel messageModel = GetterTools.streamToObject(is);

		if (ObjectTool.isNull(messageModel)) {
			return;
		}

		MessageHead messageHead = messageModel.getMessageHead();
		Integer requestType = messageHead.getType();


		switch (requestType) {
		case 1:

			responesMessageModel = BusinessProcess.loginServer(messageModel);
			sendMessageModel(responesMessageModel, senderTools);
			break;
		case 2:
			responesMessageModel = BusinessProcess.getFriendsIDServer(messageModel);
			sendMessageModel(responesMessageModel, senderTools);

			break;

		case 3:
			responesMessageModel = BusinessProcess.getUserFriendInfoServer(messageModel);
			sendMessageModel(responesMessageModel, senderTools);

			break;

		case 4:
			ResponesImage responesImage = BusinessProcess.getUserFriendImageServer(messageModel);
			sendImage(responesImage, senderTools);

			break;

		case 5:

			break;

		case 6:
			responesMessageModel = BusinessProcess.getUserFriendInfoListServer(messageModel);
			sendMessageModel(responesMessageModel, senderTools);
			break;

		default:
			break;
		}
	}

	private void sendImage(ResponesImage responesImage, SenderTools senderTools) throws IOException {
		byte[] imageByte = responesImage.getImageByte();
		//responesImage.getImageDescribe() : userID
		String imageDescribe = "imageName:" + responesImage.getImageDescribe() + " imageSize:"
				+ imageByte.length;
		int imageDescribeLength = imageDescribe.length();
		String responseLine = "state:200 length:" + imageDescribeLength + " type:Image" + " existJson:true";

		String endFlg = "Image send Done..";

		senderTools.sendLine(responseLine).sendLine(imageDescribe)
				.sendImageByteArrays(imageByte)
//				.sendImageByteArraysForBig(imageByte)
				.sendLine(endFlg).sendDone();

	}

	private void sendMessageModel(MessageModel responesMessageModel, SenderTools senderTools) throws IOException {
		//响应的状态行
		String responseLine = "state:200 length:100 type:MessageModel";

		senderTools.sendLine(responseLine)
			.sendMessage(responesMessageModel).sendDone();
	}

	private void forwardMessage() throws IOException {
		while(true) {
			System.out.println("wait..");
			Socket socket = _serverSocket.accept();
			Thread forwardThread = new Thread() {
				@Override
				public void run() {					
					System.out.println("some one connect..");
					
					ChatMessages message = getChatMessage(socket);
					addSaveChatSocketList(message.getSenderID(), socket);
					//TODO return message to client
					sendChatMessage(message);
				}
			};
			//
			ThreadConsole.useThreadPool().execute(forwardThread);
		}
	}
	
	
	/**
	 * 获得客户端发来的聊天信息
	 * @param socket
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static ChatMessages getChatMessage(Socket socket) {
		try {			
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			
			ChatMessages message = (ChatMessages)ois.readObject();
			System.out.println("messages: " + message);
			
			return message;
		} catch (ClassNotFoundException e) {
			
		}catch (IOException e) {
			
		}
		return null;
	}

	/**
	 * 获得服务器存放Socket的Map
	 * @return 存放Socket的Map
	 */
	public static Map<Integer, Socket> get_saveChatSocketList() {
		return _saveChatSocketList;
	}

	/**
	 * 添加键值对到Map
	 * @param senderID key
	 * @param socket value
	 */
	public synchronized static void addSaveChatSocketList(Integer senderID, Socket socket) {
		_saveChatSocketList.put(senderID, socket);
	}

	/**
	 * 通过userID获得服务器与客户端的Socket, 用于消息的转发
	 * @param
	 * @return 用于转发的Socket
	 */
	public static Socket getChatObjectSocket(Integer key_UserId) {
		return SocketServer.get_saveChatSocketList().get(key_UserId);
	}

	/**
	 * 以对象流的形式发送消息
	 * @param message 
	 * @return 包含发送消息状态的信息
	 */
	public static MessageInterface sendChatMessage(ChatMessages message) {
		
		ObjectOutputStream oos = null;
//		ErrerMessage errerMessage = null;
		try {
			Socket socket = getChatObjectSocket(message.getGetterID());
			if(socket == null) {
				throw new NullPointerException();
			}
			synchronized(socket) {				
				oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(message);
				oos.flush();
				return new ErrorMessage(true, "Success Send..");
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			return new ErrorMessage(false, "user is not login..");
		} catch (IOException e) {
			e.printStackTrace();
			return new ErrorMessage(false, e.getMessage());
		}finally {
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return new ErrorMessage(false, "oos close faile.");
			}
		}
//		return errerMessage;
	}
	
	/**
	 * 读取request消息
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private byte[] readRequest(InputStream is) throws IOException{
//		StringBuffer responseLine = new StringBuffer();
		
		//两个字节表示响应消息的长度
		int lengthFirst = is.read();
		
		if (lengthFirst == -1) {
			return null;
		}
		
		int lengthEnd = is.read();
		int length = (lengthFirst << 8) + lengthEnd;
		
		byte[] responseLineByte = new byte[length];
		is.read(responseLineByte);
//		responseLine.append(new String(b, "UTF-8"));
//		System.out.println("char: " + (char)b);
//		System.out.println("String: "+ new String(b, 0, length, "UTF-8"));
//		responseLine.append(new String(b, 0, length, "UTF-8"));
		
		return responseLineByte;
	}
	
	public static void main(String[] args) {
		new SocketServer().startSocketServer();
	}
}
