package transmit.Controller;

import message.ChatMessages;
import message.MessageContext;
import message.MessageInterface;
import message.MessageModel;
import tablebeans.User;
import tablejson.UserFriendsInformation;
import threadmangagement.ThreadConsole;
import tools.ObjectTool;
import tools.TransmitTool;
import transmit.BusinessProcess;
import transmit.Controller.Annotation.ControllerAnnotation;
import transmit.transmit_nio.SocketServerNIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class KChatController implements Controller{
    SocketChannel currentSocketChannel;
    public KChatController(SocketChannel currentChannel){
        this.currentSocketChannel = currentChannel;
    }

    @ControllerAnnotation.Controller(type = 1)
    public MessageInterface loginController(MessageModel requestMessageModel){
        MessageInterface responseMessageModel = BusinessProcess.loginServer(requestMessageModel);
        MessageContext mc = null;
        if (responseMessageModel instanceof MessageModel){
            mc = ((MessageModel) responseMessageModel).getMessageContext();
        }
        //check
        if (!ObjectTool.isNull(mc)){
            ArrayList loginContext = (ArrayList) mc.getObject();
            User loginUser = (User)loginContext.get(0);
            Integer idKey = loginUser.getId().intValue();
            Map _saveChatSocketList = SocketServerNIO.get_saveChatSocketList();
            if(_saveChatSocketList.containsKey(idKey)){
                //message: 该用户已在线
                _saveChatSocketList.replace(idKey, currentSocketChannel);
            }else{
                _saveChatSocketList.put(idKey, currentSocketChannel);
            }
            if (!loginUser.getUserState().contains("zk")){
                loginUser.setUserState("0");
            }
            BusinessProcess.signIn(idKey);
        }
        return responseMessageModel;
    }

    @ControllerAnnotation.Controller(type = 2)
    public MessageInterface getFriendsIDController(MessageModel requestMessageModel){
        return BusinessProcess.getFriendsIDServer(requestMessageModel);
    }

    @ControllerAnnotation.Controller(type = 3)
    public MessageInterface getUserFriendInfoController(MessageModel requestMessageModel){
        return BusinessProcess.getUserFriendInfoServer(requestMessageModel);
    }

    @ControllerAnnotation.Controller(type = 4)
    public MessageInterface getUserFriendImageController(MessageModel requestMessageModel){
        return BusinessProcess.getUserFriendImageServer(requestMessageModel);
    }

    @ControllerAnnotation.Controller(type = 5)
    public MessageInterface forwardMessageController(MessageModel requestMessageModel){
        try {
            ByteBuffer forwardByteBuffer = TransmitTool.sendResponseMessage(requestMessageModel);
            ChatMessages chatMessage = (ChatMessages)requestMessageModel.getMessageContext();
            Map<Integer, SocketChannel>  _saveChatSocketList = SocketServerNIO.get_saveChatSocketList();
            if(_saveChatSocketList.containsKey(chatMessage.getGetterID())){
                SocketChannel forwardSocketChannel = _saveChatSocketList.get(chatMessage.getGetterID());
                forwardSocketChannel.write(forwardByteBuffer);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @ControllerAnnotation.Controller(type = 6)
    public MessageInterface getUserFriendInfoListController(MessageModel requestMessageModel){
        MessageModel messageModel = BusinessProcess.getUserFriendInfoListServer(requestMessageModel);

        //已修改，type默认为-1
        //不能写成这样，不然编译后放射取得方法注释时会取到null
        Runnable runnable = () -> TransmitTool.sendRemind(messageModel);

        ThreadConsole.useThreadPool().execute(runnable);
        return messageModel;
    }

    @ControllerAnnotation.Controller(type = 7)
    public MessageInterface closeController(MessageModel requestMessageModel){
        try {
            String line = "state:close";
            byte[] closeBytes = line.getBytes("UTF-8");
            ByteBuffer closeBuffer = TransmitTool.sendResponseForNIDByRule(closeBytes);
            currentSocketChannel.write(closeBuffer);
            currentSocketChannel.close();
            String closeDescribe = requestMessageModel.getMessageHead().getRequestDescribe();
            closeDescribe = closeDescribe.replace("/","");
            System.out.println("closeDescribe: " + closeDescribe);
            if (ObjectTool.isInteger(closeDescribe)){
                Integer userId = Integer.parseInt(closeDescribe);
                //TODO 改变在线状态
                Map _saveChatSocketList = SocketServerNIO.get_saveChatSocketList();
                if(_saveChatSocketList.containsKey(userId)){
                    BusinessProcess.signOut(userId);
                    synchronized (_saveChatSocketList){
                        _saveChatSocketList.remove(userId);
                    }
                    //已修改，type默认为-1
                    //不能写成这样，不然编译后放射取得方法注释时会取到null
                    Runnable run = () ->{
                        ArrayList<Integer> friendsId = (ArrayList<Integer>) requestMessageModel.getMessageContext().getObject();
                        TransmitTool.sendRemind(friendsId, userId, "signOut");
                    };
                    ThreadConsole.useThreadPool().execute(run);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @ControllerAnnotation.Controller(type = 8)
    public MessageInterface updateUserInformationController(MessageModel requestMessageModel){
        return BusinessProcess.updateUserInformationServer(requestMessageModel);
    }

}
