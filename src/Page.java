package ScrumMasters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class Page implements Serializable {

	private static final long serialVersionUID = 1L;
	String pagenumber;
	
	boolean checkPosition=false; 
	int currentline = 0;
	ArrayList<Hashtable<String, Object>> rows;
	

	Page(String pageno, int noofcol) {
		this.pagenumber = pageno;
		rows = new ArrayList<Hashtable<String, Object>>();
	}
	
	
	public boolean insert(Hashtable<String, Object> nameval, String clustringkey,int lastpage) {
		nameval.put("touchDate", (new Date()));
		boolean flag=false;

		if (rows.isEmpty()) {
			rows.add(nameval);
			
			return true;
		} else {
			for (int i = 0; i < rows.size(); i++) {

				Hashtable currentRow = rows.get(i);
				int checker = ((String) (currentRow.get(clustringkey)+"")).compareTo((String) (nameval.get(clustringkey)+""));
				

				
				
				if (checker > 0 && !exceedLimit()) {
					rows.add(i, nameval);
					
					return true;
				}
			}
			System.out.println("the page number is :"+pagenumber);
			System.out.println("last page is :"+(lastpage-1));
			if(rows.size()<2 && pagenumber.equals(lastpage-1+"") ) {
				rows.add(nameval);
				return true;
			}
			return false;

		}
	}


	public boolean exceedLimit() {
		if (rows.size() == 2)
			return true;
		return false;
	}
	public boolean found( Hashtable<String, Object> nameval,String clustringKey) {
		for(int i=0 ;i<rows.size();i++) {
			Hashtable currentRow= rows.get(i);
			System.out.println(nameval.get(clustringKey));
			if(currentRow.get(clustringKey).equals(nameval.get(clustringKey))) {
				return true;
			}
		}
		return false;
		
	}

	public static void main(String[] args) {
		Page p = new Page("0", 4);
		Hashtable<String, Object> h = new Hashtable<String, Object>();
		h.put("id", "2");
		h.put("department", "csen");
		h.put("gpa", "0.95");
		//p.insert(h, "id");
		Hashtable<String, Object> s = new Hashtable<String, Object>();
		s.put("id", "2");
		s.put("department", "cse");
		s.put("gpa", "0.95");
		//p.insert(s, "id",);
		Hashtable<String, Object> l = new Hashtable<String, Object>();
		l.put("id", "0");
		l.put("department", "cs");
		l.put("gpa", "0.15");
		//System.out.println(p.insert(l, "id"));
		
		System.out.println(p.rows.toString());
	}
}