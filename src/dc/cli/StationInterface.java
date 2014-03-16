package cli;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import dc.DCStation;
import dc.testing.DummyConnection;

public class StationInterface extends CLC {
	private DCStation c;
	private Action create, send, dummy;
	
	public StationInterface() {		
		

		//Define actions
		create = new Action() {
			@Override
			public void execute(ArgSet args) {
				String host = "192.168.2.74";
				try {
					int port = args.fetchInteger();
					if(port < 0 || port >= 65536) 
						throw new IllegalArgumentException("The provided port has to be in bounds [0..65536]");
					DCStation c = new DCStation(host, port);
				} catch (UnknownHostException e) {
					Debugger.println(1, e.toString());
//					e.printStackTrace();
				} catch (IOException e) {
					Debugger.println(1, e.toString());
//					e.printStackTrace();
				} catch (NumberFormatException e) {

				}
			}
		};
		
		send = new Action() {
			@Override
			public void execute(ArgSet args) {
				if(c != null) {
					try {
						String s = args.fetchString();
						Debugger.println(2, "Trying to send message " + s);
						c.send(s);
					} catch (IOException e) {
						Debugger.println(1, e.toString());
					}
				}
			}
		};
		
		dummy = new Action() {
			@Override
			public void execute(ArgSet args) {
				try {
					DummyConnection dc = new DummyConnection();
					c = new DCStation(dc);
					DCServer.getServer().connect(dc);
				} catch(IOException e) {
					System.err.println("[StationInterface] Establishing a connection to the server failed.");
				}
			}
		};	
		
		teachCommands();
	}
	
	private void teachCommands() {
		mapCommand("create", create);
		mapCommand("send", send);
		mapCommand("dummy", dummy);
	}
	
	protected void onEntering() {
		
	}

}