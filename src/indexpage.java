package ScrumMasters;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class indexpage implements Serializable {

	private static final long serialVersionUID = 1L;
	ArrayList<Object[]> Index;
	String indexnumber;
	String colname;
	
   indexpage(String indexno,String colaname) {
		indexnumber = indexno;
		this.colname= colaname;
		Index = new ArrayList<Object[]>();
	}

	public void insertBitMap(Object[] bitmapAndValue) {
		Index.add(bitmapAndValue);
	}
	public boolean exceedLimit() {
		if (Index.size() == 2)
			return true;
		return false;
	}
public static void main(String[] args) throws Exception {
	
}

}
