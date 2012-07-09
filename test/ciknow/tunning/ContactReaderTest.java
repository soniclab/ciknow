package ciknow.tunning;

import java.io.BufferedReader;
import java.io.FileReader;

import ciknow.io.ContactReader;
import ciknow.util.Beans;

public class ContactReaderTest {
	public static void main(String[] args) throws Exception {
		Beans.init();
        ContactReader contactReader = (ContactReader) Beans.getBean("contactReader");
        
		String filename = "data/tunning/million_nodes.txt";
		//testCreate(contactReader, filename);
		//testUpdate(contactReader, filename);
		testValidate(contactReader, filename);
	}
	
	private static void testUpdate(ContactReader contactReader, String filename) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
        contactReader.updateOnly(reader);
	}
	
	private static void testCreate(ContactReader contactReader, String filename) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
        contactReader.createOnly(reader);
	}
	
	private static void testValidate(ContactReader contactReader, String filename) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
        contactReader.validate(reader);
	}
}
