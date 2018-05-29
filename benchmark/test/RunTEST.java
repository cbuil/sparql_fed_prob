//import static org.junit.Assert.*;

import java.io.IOException;

//import org.junit.Test;

import cli.Main;


public class RunTEST {

//	@Test
	public void main() throws ClassNotFoundException, IOException {
		String eploc = "http://localhost:3030/mgi/sparql";
		
		String subqueriesloc = "resources/queries/bio/carlos_join3.ini";
		
		
		
		String [] args ={"Run",
		              "-o", "output",
		              "-e", eploc,
		              "-q", subqueriesloc,
		              "-j", "FILTER",
		              "-l",
		              "-b","500"
		              };		
		Main.main(args);
	}
}