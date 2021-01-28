package ScrumMasters;

import java.io.File;
import java.util.BitSet;

public class hjcc {
	
public static void main(String[] args) {
BitSet x = new BitSet();

File file = new File("metaData" +"db" + ".csv");
System.out.println(file.exists());
file .delete();
	
}
}
