package transmit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class SendMessage {
//	
//	/**
//	 * ͨ��userID��÷�������ͻ��˵�Socket, ������Ϣ��ת��
//	 * @param userID��ΪKeyֵ
//	 * @return ����ת����Socket
//	 */
//	public static Socket getChatObjectSocket(Integer key_UserId) {
//		return SocketServerNIO.get_saveChatSocketList().get(key_UserId);
//	}
//	
//	public static MessageInterface sendChatMessage(ChatMessages message) {
//		
//		ObjectOutputStream oos = null;
//		ErrerMessage errerMessage = null;
//		try {
//			Socket socket = getChatObjectSocket(message.getGetterID());
//			oos = new ObjectOutputStream(socket.getOutputStream());
//			oos.writeObject(message);
//			oos.flush();
//			errerMessage = new ErrerMessage(true, "Success Send.");
//		} catch (IOException e) {
//			e.printStackTrace();
//			errerMessage = new ErrerMessage(false, e.getMessage());
//		}finally {
//			try {
//				if (oos != null) {
//					oos.close();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//				errerMessage = new ErrerMessage(false, "oos close faile.");
//			}
//		}
//		return errerMessage;
//	}
	
}
