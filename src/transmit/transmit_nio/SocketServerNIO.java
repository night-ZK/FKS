package transmit.transmit_nio;

import message.*;
import org.jetbrains.annotations.NotNull;
import tablebeans.User;
import tablejson.ResponseImage;
import tools.GetterTools;
import tools.ObjectTool;
import tools.SenderTools;
import tools.TransmitTool;
import transmit.BusinessProcess;

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
	//��¼�û�
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
			//ServerSocket ͨ��
			serverSocketChannel = ServerSocketChannel.open();
			//������ģʽ
			serverSocketChannel.configureBlocking(false);
			//Ϊͨ���е�Socket�󶨶˿�
			serverSocketChannel.socket().bind(new InetSocketAddress(8898));
			//ͨ��������
			this.selector = Selector.open();
			//ע������¼���������
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private void startSocketServer() {
		try{
			while(true){
				//���¼��򷵻�, ��������
				selector.select();

				Iterator iterator = this.selector.selectedKeys().iterator();
				while (iterator.hasNext()){
					SelectionKey selectionKey = (SelectionKey)iterator.next();
					iterator.remove();//ɾ������������key
					if(selectionKey.isAcceptable()){
						//�������¼�
						//����key�ķ�����ͨ��
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();

						//��ͻ��˵�ͨ��
						SocketChannel socketChannel = serverSocketChannel.accept();
						//��ͨ������Ϊ������
						socketChannel.configureBlocking(false);

						//Ϊ��ͨ��ע������ݵļ���Ȩ��
						socketChannel.register(selector, SelectionKey.OP_READ);
					}else if(selectionKey.isReadable()){
						//���Ͷ��¼���ͨ��
						SocketChannel socketChannel = (SocketChannel)selectionKey.channel();

						//������תbyte[]
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

						//TODO ����ת����
						MessageModel requestMessageModel = (MessageModel) GetterTools.byteArraysToObject(byteArrays);
						MessageInterface responseMessageModel = resolutionClientSocket(requestMessageModel, socketChannel);

						if (ObjectTool.isNull(responseMessageModel)) continue;

						//TODO ������ظ�����
						ByteBuffer responseByteBuffer = TransmitTool.sendResponseMessage(responseMessageModel);
						socketChannel.write(responseByteBuffer);
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

		MessageInterface responseMessageModel = null;

		if (ObjectTool.isNull(requestMessageModel)) {
			return null;
		}

		MessageHead messageHead = requestMessageModel.getMessageHead();
		Integer requestType = messageHead.getType();
		switch (requestType) {
			case 1:
				responseMessageModel = BusinessProcess.loginServer(requestMessageModel);
				MessageContext mc = null;
				if (responseMessageModel instanceof MessageModel){
					mc = ((MessageModel) responseMessageModel).getMessageContext();
				}
				//check
				if (!ObjectTool.isNull(mc)){
					User loginUser = (User)mc.getObject();
					Integer idKey = loginUser.getId().intValue();
					if(_saveChatSocketList.containsKey(idKey)){
						//message: ���û�������
					}else{
						_saveChatSocketList.put(idKey, socketChannel);
					}
				}
				break;
			case 2:
				responseMessageModel = BusinessProcess.getFriendsIDServer(requestMessageModel);
				break;

			case 3:
				responseMessageModel = BusinessProcess.getUserFriendInfoServer(requestMessageModel);

				break;

			case 4:
				responseMessageModel = BusinessProcess.getUserFriendImageServer(requestMessageModel);
				break;

			case 5:
				forwardMessage(requestMessageModel);
				break;

			case 6:
				responseMessageModel = BusinessProcess.getUserFriendInfoListServer(requestMessageModel);
				break;
            case 7:
                closeSocketChannel(socketChannel);
                break;
			default:
				break;
		}

		return responseMessageModel;
	}

    private void closeSocketChannel(@NotNull SocketChannel socketChannel) throws IOException {
	    String line = "state:close";
        byte[] closeBytes = line.getBytes("UTF-8");
        ByteBuffer closeBuffer = TransmitTool.sendResponseForNIDByRule(closeBytes);
        socketChannel.write(closeBuffer);
//        socketChannel.get
        socketChannel.close();
        //TODO �ı�����״̬
    }

    private void forwardMessage(@NotNull MessageModel messageModel) throws IOException {
		ByteBuffer forwardByteBuffer = TransmitTool.sendResponseMessage(messageModel);
		ChatMessages chatMessage = (ChatMessages)messageModel.getMessageContext();
		if(_saveChatSocketList.containsKey(chatMessage.getGetterID())){
			SocketChannel forwardSocketChannel = _saveChatSocketList.get(chatMessage.getGetterID());
			forwardSocketChannel.write(forwardByteBuffer);
		}
	}


	/**
	 * ��÷��������Socket��Map
	 * @return ���Socket��Map
	 */
	public static Map<Integer, SocketChannel> get_saveChatSocketList() {
		return _saveChatSocketList;
	}

	/**
	 * ���Ӽ�ֵ�Ե�Map
	 * @param senderID key
	 * @param socketChannel value
	 */
	public synchronized static void addSaveChatSocketList(Integer senderID, SocketChannel socketChannel) {
		_saveChatSocketList.put(senderID, socketChannel);
	}

	/**
	 * ͨ��userID��÷�������ͻ��˵�Socket, ������Ϣ��ת��
	 * @param key_UserId
	 * @return ����ת����Socket
	 */
	public static SocketChannel getChatObjectSocket(Integer key_UserId) {
		return SocketServerNIO.get_saveChatSocketList().get(key_UserId);
	}

	public static void main(String[] args) {
		new SocketServerNIO().startSocketServer();
	}
}