import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class Server {

    private static final String storageDirectory = "./Server_Storage";
    
    public void run() throws IOException, UnknownHostException{

        //checking if firectory exist or create if it doesn't exist
        File directory = new File(storageDirectory);
        if(!directory.exists()){
            directory.mkdir();
        }

        //initializing port and socket
        int port = 8010;

        try{
        ServerSocket socket = new ServerSocket(port);
        socket.setSoTimeout(20000);
        System.out.println("File Manager Server is listening on port: "+port);
        System.out.println("Serving files from: " + directory.getAbsolutePath());

        //run the server on while loop until client sends quit parameter.
        while(true){
            try{
            //initializing connection
            Socket acceptedConnection = socket.accept();
            System.out.println("Connected to "+acceptedConnection.getRemoteSocketAddress());
            
            //Initializing PrintWriter and Bufferedreader for read/write 
            PrintWriter writer = new PrintWriter(acceptedConnection.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(acceptedConnection.getInputStream()));
            

            writer.println("welcome to CLI File Manager. Commands: LIST, READ <file>, DELETE <file>, QUIT");
            String inputLine;

            //Reads input commands in loop for connected client (One client at a time)
            while((inputLine = reader.readLine()) != null){
                inputLine = inputLine.trim();

                //check if the input is QUIT and break
                if(inputLine.equalsIgnoreCase("QUIT")){
                    writer.println("BYE");
                    break;
                }

                String response = handleCommand(inputLine);
                writer.println(response);
                System.out.println("Client Disconnected. Waiting for next client...\n");
            }

            }
            catch (Exception ex){
                System.out.println("Connection error: " + ex.getMessage()); 
            }
        }
    }
    catch(IOException e){

    }
    }

    private static String handleCommand (String commandLine) {

    StringTokenizer tokenizer = new StringTokenizer(commandLine);

    String command = tokenizer.nextToken().toUpperCase();

    switch(command){

        case "LIST":
            File dir = new File(storageDirectory);
                File[] files = dir.listFiles();
                if (files == null || files.length == 0) {
                    return "DIRECTORY IS EMPTY";
                }
                StringBuilder sb = new StringBuilder("FILES:\n");
                for (File f : files) {
                    if (f.isFile()) {
                        sb.append(" - ").append(f.getName()).append(" (").append(f.length()).append(" bytes)\n");
                    }
                }
                return sb.toString().trim();

        case "READ":
            if(!tokenizer.hasMoreTokens()){
                return "Error: Missing filename.";
            }

            String readFile = tokenizer.nextToken();
            File fileToRead = new File(storageDirectory, readFile);

            if(!fileToRead.exists() || !fileToRead.isFile()){
                return "Error: File not found: " + readFile;
            }

            try(BufferedReader fileReader = new BufferedReader(new FileReader(fileToRead)))
            {
                StringBuilder content = new StringBuilder("-----Start of " + readFile + " -----\n");

                String line;
                while((line = fileReader.readLine()) !=null){
                    content.append(line).append("\n");
                }
                content.append("----- End of File -----");
                return content.toString();
                
            }
            catch(IOException ex){
                return "ERROR: Could not read file: " + ex.getMessage();
            }


        case "DELETE":
            if(!tokenizer.hasMoreTokens()){
                return "Error: Missing filename.";
            }

            String deleteFile = tokenizer.nextToken();
            File fileToDelete = new File(storageDirectory, deleteFile);

            if(!fileToDelete.exists()){
                return "ERROR: File not found ";
            }

            if(fileToDelete.delete()){
                return "Success: deleted ";
            } else{
                return "Error: failed to delete file";
            }

        default:
            return "ERROR: Invalid Input. Use LIST, READ, DELETE, or QUIT.";
        
    }

        

    }

    public static void main(String[] args){
        Server server = new Server();
        try{
            server.run();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}