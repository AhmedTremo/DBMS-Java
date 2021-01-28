package ScrumMasters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.GZIPOutputStream; 

public class Table implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	String strTableName;
	String currentPage = "0";
	String currentindex = "0";
	String indexCol = "";
	int noofrows = 0;
	ArrayList<String> indexedCol = new ArrayList<String>();
	ArrayList<String> pagesNumber = new ArrayList<String>();
	static ArrayList<Page> pages = new ArrayList<Page>();
	ArrayList<indexpage> indexpages = new ArrayList<indexpage>();

	String strClusteringKeyColumn;
	Hashtable<String, String> htblColNameType;

	Table(String strTableName, String strClusteringKeyColumn, Hashtable<String, String> htblColNameType) {
		this.strTableName = strTableName;
		this.strClusteringKeyColumn = strClusteringKeyColumn;
		this.htblColNameType = htblColNameType;

	}

	public static Page getPage(String filename) throws Exception {
		if (new File(filename).exists()) {
			ObjectInputStream serpage = new ObjectInputStream(new FileInputStream(filename));
			Page page = (Page) serpage.readObject();
			serpage.close();
			return page;
		}
		return null;

	}

	public static indexpage getindexPage(String filename) throws Exception {
		if (new File(filename).exists()) {
			ObjectInputStream serpage = new ObjectInputStream(new FileInputStream(filename));
			indexpage page = (indexpage) serpage.readObject();
			serpage.close();
			return page;
		}
		return null;

	}

	public void insert(Hashtable<String, Object> htblColNameValue, String clusteringKey) throws Exception {
		updatePages();

		for (int i = 0; i < pages.size(); i++) {

			Page p = getPage(i + "" + ".ser");

			if (p.insert(htblColNameValue, clusteringKey, pages.size())) {
				noofrows++;

			
				String pageNum = p.pagenumber;
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(i + ".ser"));
				oos.writeObject(p);
				oos.close();
				if(!(indexedCol.isEmpty())) {
				for (int j = 0; j < indexpages.size(); j++) {
					String pagename = indexpages.get(j).indexnumber;

					System.out.println("this is the page name *****************" + pagename);
					File indexfile = new File("C:\\Users\\mostafa\\Documents\\project\\database\\" + pagename + ".ser");
					
					if (indexfile.exists()) {
						indexfile.delete();
					}

				}
				
					indexpages.clear();
					int colsize = indexedCol.size();
					for(int x =0;x<colsize;x++) {
					currentindex = "0";
					String indexColumn="";
					if(x==0)
					 indexColumn = indexedCol.remove(x); 
					else
						indexColumn = indexedCol.remove(x-1); 
					
					createBitmapIndex(indexColumn);
					}
				
			}
				break;

			} else if (p.exceedLimit()) {
				Hashtable lastrow = p.rows.remove(p.rows.size() - 1);
			
				System.out.println("take care this is the last row : " + lastrow.toString());
				boolean insert = p.insert(htblColNameValue, clusteringKey, pages.size());
				int checker = ((String) (lastrow.get(clusteringKey) + ""))
						.compareTo((String) (htblColNameValue.get(clusteringKey) + ""));
				System.out.println("inserted or not  " + insert);

				for (int x = 0; x < pages.size(); x++) {
					Page now = getPage(x + ".ser");

					if (now.found(htblColNameValue, strClusteringKeyColumn)) {
						System.out.println(
								"sorry  you are not able to use the same ID==================================>>>>>>>>>>>>>>>>");
						return;
					}
				}

				if (insert || checker > 0) {
					if (!insert) {
						p.rows.add(htblColNameValue);
					}
					ObjectOutputStream oos1 = new ObjectOutputStream(new FileOutputStream(p.pagenumber + ".ser"));
					oos1.writeObject(p);
					oos1.close();
					Page nextpage = getPage((i + 1) + ".ser");
					System.out.println(nextpage.pagenumber);
					insert(lastrow, clusteringKey);

//
					break;

				} else {
					p.rows.add(lastrow);
					ObjectOutputStream oos1 = new ObjectOutputStream(new FileOutputStream(p.pagenumber + ".ser"));
					oos1.writeObject(p);
					oos1.close();

				}
			}

		}

	}

	public void updatePages() throws Exception {

		Page p;
		if (pages.isEmpty()) {
			p = new Page(currentPage, htblColNameType.size());
			pagesNumber.add(currentPage);
			pages.add(p);

		} else {
			p = getPage(currentPage + ".ser");

			if (p.exceedLimit()) {
				currentPage = (Integer.parseInt(currentPage) + 1) + "";
				p = new Page(currentPage, htblColNameType.size());
				pagesNumber.add(currentPage);
				pages.add(p);

			}
		}

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(currentPage + ".ser"));
		oos.writeObject(p);
		oos.close();

	}

	public void updateindexpages(String colname) throws Exception {

		indexpage p;
		if (indexpages.isEmpty()) {
			System.out.println("this is the size of the indexPages array " + indexpages.size());
			p = new indexpage(currentindex + "index" + colname, colname);
			indexpages.add(p);
			System.out.println("hena el check el gdid" + indexpages.size());

		} else {
			p = getindexPage(currentindex + "index" + colname + ".ser");
			System.out.println(p + "============================dih el page el medy2ana ");
	

			if (p.exceedLimit()) {
				currentindex = (Integer.parseInt(currentindex) + 1) + "";
				p = new indexpage(currentindex + "index" + colname, colname);
				indexpages.add(p);
				System.out.println("hena el check el gdid" + indexpages.size());

			}
		}

		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(currentindex + "index" + colname + ".ser"));
		oos.writeObject(p);
		oos.close();

	}

	public void deleteRow(Hashtable<String, Object> nameval) throws Exception {
		int ifDeleted = 0;
		boolean del = false;

		// i is the current page Number
		for (int i = 0; i < pages.size(); i++) {
			Page page = getPage(i + ".ser");

			if (page != null) {
				int rowss = page.rows.size();
				for (int j = 0; j < rowss; j++) {
					// j is the row Number

					Date toAdd = (Date) ((page.rows.get(j)).remove("touchDate"));
					nameval.remove("touchDate");
					System.out.println("this is the removed touchdate:  " + toAdd);
					System.out.println("current row:  " + (page.rows.get(j)).toString());
					System.out.println("input row:  " + nameval.toString());
					if (((page.rows.get(j)).equals(nameval))) {

						(page.rows).remove(nameval);
						System.out.println("dah el anta betbos 3aleh *************" + page.rows.size());
						ifDeleted++;

						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(i + ".ser"));
						oos.writeObject(page);
						oos.close();
						noofrows--;
						del = true;
                         
						System.out.println("The row number is " + page.rows.size());
						if(!(indexedCol.isEmpty())&&del) {
						for (int x = 0; x < indexpages.size(); x++) {
							String pagename = indexpages.get(x).indexnumber;

							System.out.println("this is the page name *****************" + pagename);
							File indexfile = new File(
									"C:\\Users\\mostafa\\Documents\\project\\database\\" + pagename + ".ser");
							System.out.println("hal el folder mawgood ?=> " + indexfile.exists());
							if (indexfile.exists()) {
								indexfile.delete();
							}

						}
					
							indexpages.clear();
							int indexedColsize= indexedCol.size();
							for(int x=0;x<indexedColsize;x++) {
							currentindex = "0";
							String col="";
							if(x==0) {
							 col = indexedCol.remove(x);}
							else {
								col=indexedCol.remove(x-1);
							}
							createBitmapIndex(col);}
						
						}
						break;
					} else {
						page.rows.get(j).put("touchDate", toAdd);
					}

				}

				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(i + ".ser"));
				oos.writeObject(page);
				oos.close();
				// System.out.println(p.rows.isEmpty());
				System.out.println("Pages array size is " + pages.size() + " and the current page(starting from 0) is "
						+ i + '\n');

				if (page.rows.isEmpty()) {
					// If the page is empty we will set it's place a null
//					pages.set(Integer.parseInt(currentPage), null);
					pages.remove(i);
					System.out.println(pages.size());
					if ((Integer.parseInt(currentPage) != 0))
						currentPage = ((Integer.parseInt(currentPage)) - 1) + "";

					pagesNumber.remove(i + "");

					// reorganize files
					reorganizeFiles(i);
				}
				if (ifDeleted > 0) {
					break;
				}
			} else
				System.out.println("The page number " + i + " is empty (null)");
		}
	}

	public void createBitmapIndex(String strColName) throws Exception {
		ArrayList<indexpage> templist = new ArrayList<indexpage>();
		indexedCol.add(strColName);
        updateMetadata();
		for (int i = 0; i < indexpages.size(); i++) {
			templist.add(indexpages.get(i));
		}
		// System.out.println(templist.size()+"This is THE TEMP LIST
		// SIZE***********************");
		indexpages.clear();
		indexCol = strColName;
		currentindex = "0";
		System.out.println("NO OF ROWS YA TAFTOFY AHO =>" + noofrows);
		Object[] column = new String[noofrows];
		Object[] bitmapindexandname = new Object[2];
		if (strColName == strClusteringKeyColumn) {
			for (int i = 0; i < pages.size(); i++) {
				Page p = getPage(i + ".ser");
				for (int j = 0; j < p.rows.size(); j++) {
					if (((i * 2) + j) == noofrows) {
						for (int x = 0; x < column.length; x++) {
							if (column[x] == null) {
								column[x] = p.rows.get(j).get(strColName);
							}
						}
					} else {
						column[(i * 2) + j] = p.rows.get(j).get(strColName);
					}
				}
			}
			for (int x = 0; x < column.length; x++) {
				System.out.print(column[x] + " ,");
			}

			for (int i = 0; i < column.length; i++) {
				Object bitMapValue = column[i];

				// ArrayList<Integer> bitMap = new ArrayList<Integer>();
				BitSet bitMap = new BitSet();
				for (int j = 0; j < column.length; j++) {
					System.out.println("the bit map value = " + bitMapValue);
					if (column[j].equals(bitMapValue) && !(bitMapValue.equals("#"))) {
						bitMap.set(j, true);
						column[j] = "#";
					} else {
						bitMap.set(j, false);
					}
				}
				if (!(bitMapValue.equals("#"))) {
					bitmapindexandname[0] = bitMapValue;
					bitmapindexandname[1] = bitMap;
					updateindexpages(strColName);
					indexpage p = getindexPage(currentindex + "index" + strColName + ".ser");
					System.out.println(p.toString());
					p.Index.add(bitmapindexandname);
					ObjectOutputStream oos = new ObjectOutputStream(
							new FileOutputStream(currentindex + "index" + strColName + ".ser"));
					GZIPOutputStream gzipOuputStream = new GZIPOutputStream(oos);
					oos.writeObject(p);
					oos.close();
					// System.out.println(p.Index.get(0)[0].toString());
					// System.out.println(p.Index.get(0)[1].toString());

				}

			}

		} else {
			for (int i = 0; i < pages.size(); i++) {
				Page p = getPage(i + ".ser");
				for (int j = 0; j < p.rows.size(); j++) {
					System.out.println("this is the value of the id :" + p.rows.get(j).get(strColName));
					System.out.println((i * 2) + j);
					if (((i * 2) + j) == noofrows) {
						for (int x = 0; x < column.length; x++) {
							if (column[x] == null) {
								column[x] = p.rows.get(j).get(strColName) + "";
							}
						}
					} else {
						column[(i * 2) + j] = p.rows.get(j).get(strColName) + "";
					}
					// System.out.println("this is the value of the id :" + column[(i * 2) + j]);
				}
			}
			// bubbleSort(column);
			for (int x = 0; x < column.length; x++) {
				System.out.print(column[x] + " ,");
			}
			// System.out.println("column lenso eh ?:"+column.length);
			for (int i = 0; i < column.length; i++) {
				Object bitMapValue = column[i];

				// ArrayList<Integer> bitMap = new ArrayList<Integer>();
				BitSet bitMap = new BitSet();
				System.out.println("akedlyyabny 3al collength:" + column.length);
				for (int j = 0; j < column.length; j++) {
					System.out.println("the bit map value = " + bitMapValue);
					if (column[j].equals(bitMapValue) && !(bitMapValue.equals("#"))) {
						bitMap.set(j, true);
						;
						column[j] = "#";
					} else {
						bitMap.set(j, false);
					}
				}
				System.out.println(bitMap.toString());
				if (!(bitMapValue.equals("#"))) {
					System.out.println(bitMap.toString());
					bitmapindexandname[0] = bitMapValue;
					bitmapindexandname[1] = bitMap;
					updateindexpages(strColName);
					indexpage p = getindexPage(currentindex + "index" + strColName + ".ser");

					p.Index.add(bitmapindexandname);
					ObjectOutputStream oos = new ObjectOutputStream(
							new FileOutputStream(currentindex + "index" + strColName + ".ser"));
					oos.writeObject(p);
					oos.close();
					// System.out.println("el size beta3 el index ya hubby :"+p.Index.size());
					// System.out.println(p.Index.get(0)[0].toString());
					// System.out.println(p.Index.get(0)[1].toString());

				}

			}
		}
		System.out.println(indexpages.size() + "This is THE index LIST SIZE***********************BEFORE");
		System.out.println(templist.size() + "This is THE temp LIST SIZE***********************BEFORE");

		for (int i = 0; i < templist.size(); i++) {
			indexpages.add(templist.get(i));

		}
		System.out.println(templist.size() + "This is THE temp LIST SIZE***********************AFTER");
		System.out.println(indexpages.size() + "This is THE index LIST SIZE***********************AFTER");

	}
	public void updateMetadata() throws Exception {
		boolean flag = false;
          File file = new File("metaData" + strTableName + ".csv");
          file.delete();
          FileWriter writer = new FileWriter("metaData" + strTableName + ".csv");

			Set<java.util.Map.Entry<String, String>> set1 = htblColNameType.entrySet();
			Iterator<java.util.Map.Entry<String, String>> iterator1 = set1.iterator();

			while (iterator1.hasNext()) {
				java.util.Map.Entry<String, String> en = iterator1.next();
				writer.write(strTableName + ",");
				writer.write(en.getKey() + ",");
//				String check = (en.getValue().split("."))[0];
				StringTokenizer st = new StringTokenizer(en.getValue(), ".");
				String s1 = st.nextToken();
				s1 = st.nextToken();
				s1 = st.nextToken();

				switch (s1) {
				case "Integer":
					break;
				case "String":
					break;
				case "Boolean":
					break;
				case "Double":
					break;
				case "Date":
					break;
				default:
					flag = true;
					System.out.println("Wrong Input");
					break;
				}
				if (flag) {
					return;

				}
				writer.write(en.getValue() + ",");
				if (strClusteringKeyColumn.equals(en.getKey())) {
					writer.write("True" + ",");
				} else
					writer.write("False" + ",");
				
                 if(indexedCol.contains(en.getKey())) {
                	 writer.write("True" + ",");
                 }else {
				writer.write("False" + ",");}
				writer.write("\n");

			}
			writer.write(strTableName + ",");
			writer.write("touchDate" + ",");
			writer.write("java.lang.Date" + ",");
			writer.write("False" + ",");
			writer.write("False" + ",");

			writer.close();
		
	

	}

	public void bubbleSort(Object[] arr) {
		int n = arr.length;
		for (int i = 0; i < n - 1; i++)
			for (int j = 0; j < n - i - 1; j++) {
				System.out.println("this is what we'll find in the Array" + (String) arr[j]);
				System.out.println("this is what we will find next" + (String) arr[j + 1]);
				if (((String) arr[j]).compareTo(((String) arr[j + 1])) > 0) {
					// swap arr[j+1] and arr[i]
					Object temp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp;
				}
			}
	}

	public void updateRow(Hashtable<String, Object> htblColNameValue, String strKey) throws Exception {
		htblColNameValue.put("touchDate", new Date());

		for (int i = 0; i < pages.size(); i++) {
			Page p = getPage(i + "" + ".ser");

			for (int j = 0; j < p.rows.size(); j++) {
				Set<java.util.Map.Entry<String, Object>> set1 = p.rows.get(j).entrySet();
				Iterator<java.util.Map.Entry<String, Object>> it1 = set1.iterator();

				while (it1.hasNext()) {
					java.util.Map.Entry<String, Object> en = it1.next();

					if ((en.getValue() + "").equals(strKey)) {
//						for (int x = 0; x < pages.size(); x++) {
//							Page now = getPage(x + ".ser");
//
//							if (now.found(htblColNameValue, strClusteringKeyColumn)) {
//								System.out.println(
//										"sorry  you are not able to use the same ID==================================>>>>>>>>>>>>>>>>");
//								return;
//							}
//						}
						p.rows.remove(j);
						p.rows.add(j, htblColNameValue);
						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(i + ".ser"));
						oos.writeObject(p);
						oos.close();
                          if(!(indexedCol.isEmpty())) {
						for (int x = 0; x < indexpages.size(); x++) {
							String pagename = indexpages.get(x).indexnumber;

							System.out.println("this is the page name *****************" + pagename);
							File indexfile = new File(
									"C:\\Users\\mostafa\\Documents\\project\\database\\" + pagename + ".ser");
							System.out.println("hal el folder mawgood ?=> " + indexfile.exists());
							if (indexfile.exists()) {
								indexfile.delete();
							}

						}
						
						
							indexpages.clear();
							System.out.println("hwa wesl le hena ****************");
							int indexedColsize =indexedCol.size();
						    for(int x =0;x<indexedColsize;x++) {
						    currentindex = "0";
						    String col="";
						    if(x==0) {
						   col= indexedCol.remove(x);	}
						    else {
						    col= indexedCol.remove(x-1);
						    }
							createBitmapIndex(col);}
						
					}

						ObjectOutputStream oosi = new ObjectOutputStream(new FileOutputStream(i + ".ser"));
						oosi.writeObject(p);
						oosi.close();
						break;

					}

				}

			}
		}

	}

	public void reorganizeFiles(int deletePageNo) {
		// Delete this page

		File file = new File(deletePageNo + ".ser");
		if (file.delete())
			System.out.println("File deleted successfully");
		else
			System.out.println("Failed to delete file");

		// Shift following pages back by one
		for (int nextPage = deletePageNo + 1; nextPage < 100; nextPage++) {
			File nextFile = new File(nextPage + ".ser");
			if (nextFile.exists()) {
				File f1 = new File(nextPage + ".ser");
				File f2 = new File((nextPage - 1) + ".ser");
				if (f1.renameTo(f2))
					System.out.println("File Renamed Successfully");
				// if b is true, then the file has been renamed successfully
			}
		}
	}

	public BitSet selectFromTableindexed(SQLTerm SQLTerm) throws Exception {
		String colname = SQLTerm._strColumnName;
		BitSet outputBitset = new BitSet();
		int count = 0;

		for (int i = 0; i < indexpages.size(); i++) {
			if (indexpages.get(i).colname.equals(colname)) {
				indexpage target = getindexPage(count + "index" + colname + ".ser");
				count++;
				for (int j = 0; j < target.Index.size(); j++) {
					int compare = ((String) (target.Index.get(j)[0])).compareTo((String) (SQLTerm._objValue));
					BitSet current = (BitSet) target.Index.get(j)[1];
					switch (SQLTerm._strOperator) {
					case "=":
						if (compare == 0) {
							outputBitset.or(current);
						}
						break;
					case ">":
						if (compare > 0) {
							outputBitset.or(current);
						}
						break;

					case "<":
						if (compare < 0) {
							outputBitset.or(current);
						}
						break;

					case ">=":
						if (compare > 0 || compare == 0) {
							outputBitset.or(current);
						}
						break;

					case "<=":
						if (compare < 0 || compare == 0) {
							outputBitset.or(current);
						}
						break;

					case "!=":
						if (compare != 0) {
							outputBitset.or(current);
						}
						break;

					}
				}

			}

		}
		return outputBitset;

	}

	public  ArrayList<Hashtable> selectFromTable(SQLTerm sqlTerm) throws Exception  {
		ArrayList<Hashtable> selectOutput = new ArrayList<Hashtable>();
		String targetCol = sqlTerm._strColumnName;
		for (int i = 0; i < pages.size(); i++) {
		Page targetPage = getPage(i + ".ser");
		for (int j = 0; j < targetPage.rows.size(); j++) {
			int compare = ((targetPage.rows.get(j).get(targetCol)+""))
					.compareTo((sqlTerm._objValue)+"");
			switch (sqlTerm._strOperator) {
			case "=":
				if (compare == 0) {
					selectOutput.add(targetPage.rows.get(j));
				}
				break;
			case ">":
				if (compare > 0) {
					selectOutput.add(targetPage.rows.get(j));
				}
				break;

			case "<":
				if (compare < 0) {
					selectOutput.add(targetPage.rows.get(j));
				}
				break;

			case ">=":
				if (compare > 0 || compare == 0) {
					selectOutput.add(targetPage.rows.get(j));
				}
				break;

			case "<=":
				if (compare < 0 || compare == 0) {
					selectOutput.add(targetPage.rows.get(j));
				}
				break;

			case "!=":
				if (compare != 0) {
					selectOutput.add(targetPage.rows.get(j));
				}
				break;
			default:
				System.out.println("WRONG OPERATOR");
				break;
			}

		}

	}
		return selectOutput;
	}

	public static void main(String[] args) throws Exception {
		// Creating new Table
		String strTableName = "Student";
		Hashtable htblColNameType = new Hashtable();
		htblColNameType.put("id", "java.lang.String");
		htblColNameType.put("name", "java.lang.String");
		htblColNameType.put("gpa", "java.lang.double");
		Table t = new Table(strTableName, "id", htblColNameType);

		// Inserting the first value

		// Inserting the second value
		Hashtable htb = new Hashtable();
		htb.put("id", new String("453455"));
		htb.put("name", new String("Ahmed Noor"));
		htb.put("gpa", new Double(0.95));
		t.insert(htb, "id");

		Hashtable htblColNameValue = new Hashtable();
		htblColNameValue.put("id", new String("23"));
		htblColNameValue.put("name", new String("Khaled saleh"));
		htblColNameValue.put("gpa", new Double(2.22));
		t.insert(htblColNameValue, "id");

		// Inserting the third value
		Hashtable ht = new Hashtable();
		ht.put("id", new String("5674567"));
		ht.put("name", new String("Dalia Noor"));
		ht.put("gpa", new Double(1.25));
		t.insert(ht, "id");

		Hashtable htblColNameValue1 = new Hashtable();
		htblColNameValue1.put("id", new String("2335"));
		htblColNameValue1.put("name", new String("Khaled saleh"));
		htblColNameValue1.put("gpa", new Double(2.22));
		t.insert(htblColNameValue1, "id");

		// System.out.println(t.currentPage);
		System.out.println(t.getPage("0.ser").rows.toString());
		System.out.println(t.getPage("1.ser").rows.toString());
		t.createBitmapIndex("name");

		Hashtable htb2 = new Hashtable();
		htb2.put("id", new String("455"));
		htb2.put("name", new String("AhNoor"));
		htb2.put("gpa", new Double(1.95));
		t.insert(htb2, "id");
		System.out.println(t.getindexPage("2index.ser").Index.get(0)[0].toString());
		System.out.println(t.getindexPage("2index.ser").Index.get(0)[1].toString());

		System.out.println(t.getindexPage("1index.ser").Index.get(0)[0].toString());
		System.out.println(t.getindexPage("1index.ser").Index.get(0)[1].toString());
		System.out.println(t.getindexPage("1index.ser").Index.get(1)[0].toString());
		System.out.println(t.getindexPage("1index.ser").Index.get(1)[1].toString());

		System.out.println(t.getindexPage("0index.ser").Index.get(0)[0].toString());
		System.out.println(t.getindexPage("0index.ser").Index.get(0)[1].toString());
		System.out.println(t.getindexPage("0index.ser").Index.get(1)[0].toString());
		System.out.println(t.getindexPage("0index.ser").Index.get(1)[1].toString());

//		Hashtable st = new Hashtable();
//		st.put("id", new Integer(5674567));
//		st.put("name", new String("Dalia Noor"));
//		st.put("gpa", new Double(1.25));
//
//		t.deleteRow(st);
//
//		Hashtable colval = new Hashtable();
//		colval.put("id", new Integer(5674));
//		colval.put("name", new String("Dar"));
//		colval.put("gpa", new Double(1.5));
//
//		t.updateRow(colval, "453455");
//		System.out.println(t.getPage("0.ser").rows.toString());

	}
}
