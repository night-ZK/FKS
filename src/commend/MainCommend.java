package commend;

import transmit.transmit_nio.SocketServerNIO;

import java.util.Scanner;

public class MainCommend {
    private static String _dbId = "001";
    public static void main(String[] args) {
        System.out.println("Welcome use to this software...");
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        System.out.print("please entry use dbID: ");
        String dbId = scanner.next();
        while (!dbId.equals("001") && !dbId.equals("002")){
            System.out.println("dbID entry error... ");
            System.out.print("please entry use dbID: ");
            dbId = scanner.next();
        }

        set_dbId(dbId);
        //TODO name and password

        System.out.println("please entry commend... ");
        String commend = scanner.next();
        if (commend.contains("fks")){
            String[] fksCommends = commend.split(" ");
            while (fksCommends.length < 2){
                System.out.println("please entry commend... ");
                fksCommends = scanner.next().split(" ");
                System.out.println("length: " + fksCommends.length);
            }

            if(fksCommends[1].equals("-ss")){
                System.out.println("Server will start..");
                new SocketServerNIO().startSocketServer();
            }else{
                //TODO
                System.out.println("dbID entry error... ");
            }
        }else{
            //TODO
            System.out.println("dbID entry error... ");
        }

    }
    public static String get_dbId() {
        return _dbId;
    }
    public static void set_dbId(String _dbId) {
        MainCommend._dbId = _dbId;
    }
}
