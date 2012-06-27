import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class BootstrapTest{
	
	public static void main(String[] args) throws UnknownHostException, IOException
	{
		Socket s= new Socket("localhost", 12345);
		OutputStream os =s.getOutputStream();
		PrintWriter pw =new PrintWriter(os);
		
		String info="shutdown";
		pw.write(info);
		pw.flush();
		s.shutdownOutput();
		pw.close();
		os.close();
		s.close();
		
	}
}