import java.io.*;
import java.util.*;
import java.net.*;
 
// Server class
public class Server 
{
    // Vector to store all clients, chatroom names
    static Vector<ClientHandler> ar = new Vector<>();
    static Vector<String> cr = new Vector<>();
    static int n = 3;
     
    // counter for clients
    static int i = 0;
 
    public static void main(String[] args) throws IOException 
    {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(2000);
        Socket s;
         
        while (true) 
        {
            // Accept the incoming request
            s = ss.accept();
 
            // obtain input and output streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
             
            // Create a new handler object for handling this request.
            ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos);
 
            // Create a new Thread with this object.
            Thread t = new Thread(mtch);
             
            // add this client to active clients list
            ar.add(mtch);
 
            // start the thread.
            t.start();
            i++;
         }
    }
}
 
// ClientHandler class
class ClientHandler implements Runnable 
{
    Scanner scn = new Scanner(System.in);
    private String name;
    String chatroom;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;
     
    // constructor
    public ClientHandler(Socket s, String name,
                            DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.s = s;
        this.isloggedin=true;
        this.chatroom = "\0";

        try{
            this.dos.writeUTF("Please enter your Username");
            name = this.dis.readUTF();
            this.name = name;
        } catch(IOException e){
            e.printStackTrace();
        }
        System.out.println(this.name + "just connected to the server.\n");
    }
 
    @Override
    public void run() {
 
        String received;
        while (true) 
        {
            try
            {
                // receive the string
                received = dis.readUTF();
                String msg = "";
                 
                System.out.println(this.name + "@" + this.chatroom + ": " + received);
                 
                if(received.equals("logout")){
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }
                 
                // break the string received based on spaces
                StringTokenizer st = new StringTokenizer(received, " ");

                //
                String word = st.nextToken();

                //Create Chatroom Command Handling 
                if(new String("create").equals(word)){
                    word = st.nextToken();
                    if(new String("chatroom").equals(word)){
                        word = st.nextToken();
                        msg = msg + word + " chatroom created.";
                        Server.cr.add(word);
                    }
                    this.dos.writeUTF(msg);
                    continue;
                }

                //List Chatroom names or User names Commands Handling
                if(new String("list").equals(word)){
                    word = st.nextToken();
                    if(new String("chatrooms").equals(word)){
                        for (String str : Server.cr){
                            msg = msg + str + "\n";    
                        }
                    }
                    else if(new String("users").equals(word)){
                        for (ClientHandler mc : Server.ar){
                           msg = msg + mc.name + "\n";    
                        }
                    }
                    this.dos.writeUTF(msg);
                    continue;
                }

                //Join Command Handling
                if(new String("join").equals(word)){
                    word = st.nextToken();
                    for(String str : Server.cr){
                        if(new String(str).equals(word)){
                            this.chatroom = str;
                            msg = "Joined " + this.chatroom;
                        }
                        else{
                            msg = word + " does not exits.";
                        }
                    }
                    this.dos.writeUTF(msg);
                    continue;
                }

                //Leave Command Handling
                if(new String("leave").equals(word)){
                    String str = this.chatroom;
                    this.chatroom = "\0";
                    msg = "Left " + str;
                    this.dos.writeUTF(msg);
                    continue;
                }

                //Add Command Handling
                int flag = 0;
                if(new String("add").equals(word)){
                    word = st.nextToken();
                    for(ClientHandler mc : Server.ar){
                        if(new String(mc.name).equals(word)){
                            if(new String(mc.chatroom).equals("\0")){
                                mc.chatroom = this.chatroom;
                                msg = msg + mc.name + " added to " + mc.chatroom;
                            }
                            else{
                                msg = msg + mc.name + " Already belongs to another chatroom";
                            }
                            break;
                        }
                    }
                    if(flag == 0){
                        msg = msg + "User does not exist";
                    }
                    this.dos.writeUTF(msg);
                    continue;
                }

                //Reply Command Handling
                if(new String("reply").equals(word)){
                    if(new String(this.chatroom).equals("\0")){
                        msg = msg + "You dont belong to any chatroom.";
                    }
                    else{
                        msg = msg + this.name + ":";
                        int count = st.countTokens();
                        for(int i=1;i<=count;i++){
                            String next = st.nextToken();
                            msg = msg + " " + next;
                        }
                        for(ClientHandler mc : Server.ar){
                            if(new String(mc.chatroom).equals(this.chatroom) && ! new String(mc.name).equals(this.name)){
                                mc.dos.writeUTF(msg);
                            }
                        }
                    }
                    continue;
                }

                //Send Command Handling

            } catch (IOException e) {
                 
                e.printStackTrace();
            }             
        }
        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();
             
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}