package ScrumMasters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public class DBApp {
	public static ArrayList<Table> tdb = new ArrayList<Table>();

	public static void createTable(String strTableName, String strClusteringKeyColumn,
			Hashtable<String, String> htblColNameType) throws Exception {
		boolean flag = false;
		Table t = new Table(strTableName, strClusteringKeyColumn, htblColNameType);

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(strTableName + ".ser")));
		oos.writeObject(t);
		oos.close();
		try {
			@SuppressWarnings("resource")
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

				writer.write("False" + ",");
				writer.write("\n");

			}
			writer.write(strTableName + ",");
			writer.write("touchDate" + ",");
			writer.write("java.lang.Date" + ",");
			writer.write("False" + ",");
			writer.write("False" + ",");

			writer.close();
			tdb.add(t);

		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	public static ArrayList selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws Exception {
		ArrayList<Hashtable> finalOut = new ArrayList<Hashtable>();
		for (int i = 0; i < arrSQLTerms.length; i += 2) {
			BitSet outIndexed1; // is the main
			BitSet outIndexed2;

			ArrayList<Hashtable> tempArray = new ArrayList<Hashtable>(); // hold the value of i,i+1
			ArrayList<Hashtable> tempArray1 = new ArrayList<Hashtable>();
			SQLTerm sqlTerm = arrSQLTerms[i];
			String TargetTableName = sqlTerm._strTableName;
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TargetTableName + ".ser"));
			Table TargetTable = (Table) ois.readObject();
			ois.close();
			String TargetCol = sqlTerm._strColumnName;
			if (TargetTable.indexedCol.contains(TargetCol)) {
				outIndexed1 = (TargetTable.selectFromTableindexed(sqlTerm));
				if (i + 1 < arrSQLTerms.length) {
					String TargetCol2 = arrSQLTerms[i + 1]._strColumnName;
					if (TargetTable.indexedCol.contains(TargetCol2)) {
						System.out.println("the code is supposed to be hereeeeeeeee");
						outIndexed2 = (TargetTable.selectFromTableindexed(arrSQLTerms[i + 1]));
						String operator = strarrOperators[i];
						System.out.println(strarrOperators[i] + "<===this is the operator---------");

						switch (operator) {
						case "OR":
							outIndexed1.or(outIndexed2);
							break;
						case "AND":
							outIndexed1.and(outIndexed2);
							break;
						case "XOR":
							outIndexed1.xor(outIndexed2);
							break;
						}
						for (int x = 0; x < TargetTable.pages.size(); x++) {
							Page TargetPage = TargetTable.getPage(x + ".ser");
							for (int y = 0; y < TargetPage.rows.size(); y++) {
								if (outIndexed1.get((x * TargetPage.rows.size()) + y)) {
									tempArray.add(TargetPage.rows.get(y));

								}
							}
						}
					} else {
						ArrayList<Hashtable> fstTarget = new ArrayList<Hashtable>();
						for (int x = 0; x < TargetTable.pages.size(); x++) {
							Page TargetPage = TargetTable.getPage(x + ".ser");
							for (int y = 0; y < TargetPage.rows.size(); y++) {
								if (outIndexed1.get((x * TargetPage.rows.size()) + y)) {
									fstTarget.add(TargetPage.rows.get(y));

								}
							}
						}
						ArrayList<Hashtable> secTarget = TargetTable.selectFromTable(arrSQLTerms[i + 1]);
						ArrayList<Hashtable> in1 = new ArrayList<Hashtable>();
						ArrayList<Hashtable> in2 = new ArrayList<Hashtable>();

						String operator = strarrOperators[i];
						switch (operator) {
						case "OR":
							for (int x = 0; x < fstTarget.size(); x++) {
								tempArray.add(fstTarget.get(x));
							}
							for (int y = 0; y < secTarget.size(); y++) {
								if (!(fstTarget.contains(secTarget.get(y)))) {
									tempArray.add(secTarget.get(y));
								}
							}
							break;
						case "AND":
							for (int x = 0; x < fstTarget.size(); x++) {
								if (secTarget.contains(fstTarget.get(x))) {
									tempArray.add(fstTarget.get(x));
								}
							}
							break;
						case "XOR":
							for (int x = 0; x < fstTarget.size(); x++) {
								if (!(secTarget.contains(fstTarget.get(x)))) {
									in1.add(fstTarget.get(x));
								}
							}
							for (int x = 0; x < secTarget.size(); x++) {
								if (!(fstTarget.contains(secTarget.get(x)))) {
									in2.add(secTarget.get(x));
								}
							}
							for (int x = 0; x < in1.size(); x++) {
								tempArray.add(in1.get(x));
							}
							for (int x = 0; x < in2.size(); x++) {
								if (!(in1.contains(in2.get(x)))) {
									tempArray.add(in2.get(x));
								}
							}

							break;
						}

					}

				} else {
					for (int x = 0; x < TargetTable.pages.size(); x++) {
						Page TargetPage = TargetTable.getPage(x + ".ser");
						for (int y = 0; y < TargetPage.rows.size(); y++) {
							if (outIndexed1.get((x * TargetPage.rows.size()) + y)) {
								tempArray.add(TargetPage.rows.get(y));

							}
						}
					}
				}

			} else {
				ArrayList<Hashtable> fstTarget = TargetTable.selectFromTable(sqlTerm);
				ArrayList<Hashtable> secTarget = new ArrayList<Hashtable>();
				if (i + 1 < arrSQLTerms.length) {
					String TargetCol2 = arrSQLTerms[i + 1]._strColumnName;
					if (TargetTable.indexedCol.contains(TargetCol2)) {
						outIndexed2 = (TargetTable.selectFromTableindexed(sqlTerm));
						for (int x = 0; x < TargetTable.pages.size(); x++) {
							Page TargetPage = TargetTable.getPage(x + ".ser");
							for (int y = 0; y < TargetPage.rows.size(); y++) {
								if (outIndexed2.get((x * TargetPage.rows.size()) + y)) {
									secTarget.add(TargetPage.rows.get(y));

								}
							}
						}
					} else {
						secTarget = TargetTable.selectFromTable(arrSQLTerms[i + 1]);
					}
					ArrayList<Hashtable> in1 = new ArrayList<Hashtable>();
					ArrayList<Hashtable> in2 = new ArrayList<Hashtable>();
					String operator = strarrOperators[i];
					switch (operator) {
					case "OR":
						for (int x = 0; x < fstTarget.size(); x++) {
							tempArray.add(fstTarget.get(x));
						}
						for (int y = 0; y < secTarget.size(); y++) {
							if (!(fstTarget.contains(secTarget.get(y)))) {
								tempArray.add(secTarget.get(y));
							}
						}
						break;
					case "AND":
						for (int x = 0; x < fstTarget.size(); x++) {
							if (secTarget.contains(fstTarget.get(x))) {
								tempArray.add(fstTarget.get(x));
							}
						}
						break;
					case "XOR":
						for (int x = 0; x < fstTarget.size(); x++) {
							if (!(secTarget.contains(fstTarget.get(x)))) {
								in1.add(fstTarget.get(x));
							}
						}
						for (int x = 0; x < secTarget.size(); x++) {
							if (!(fstTarget.contains(secTarget.get(x)))) {
								in2.add(secTarget.get(x));
							}
						}
						for (int x = 0; x < in1.size(); x++) {
							tempArray.add(in1.get(x));
						}
						for (int x = 0; x < in2.size(); x++) {
							if (!(in1.contains(in2.get(x)))) {
								tempArray.add(in2.get(x));
							}
						}

						break;
					}

				} else {
					tempArray = TargetTable.selectFromTable(sqlTerm);
				}

			}
			if (i == 0) {
				System.out.println("entered the condition here-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
				for (int x = 0; x < tempArray.size(); x++) {
					tempArray1.add(tempArray.get(x));
					finalOut.add(tempArray.get(x));

				}
			} else {
				ArrayList<Hashtable> in1 = new ArrayList<Hashtable>();
				ArrayList<Hashtable> in2 = new ArrayList<Hashtable>();
				String operator = strarrOperators[i - 1];
				switch (operator) {
				case "OR":
					for (int x = 0; x < tempArray.size(); x++) {
						finalOut.add(tempArray.get(x));
					}
					for (int y = 0; y < tempArray1.size(); y++) {
						if (!(tempArray.contains(tempArray1.get(y)))) {
							finalOut.add(tempArray1.get(y));
						}
					}
					break;
				case "AND":
					for (int x = 0; x < tempArray.size(); x++) {
						if (tempArray1.contains(tempArray.get(x))) {
							finalOut.add(tempArray1.get(x));
						}
					}
					break;
				case "XOR":
					for (int x = 0; x < tempArray.size(); x++) {
						if (!(tempArray1.contains(tempArray.get(x)))) {
							in1.add(tempArray.get(x));
						}
					}
					for (int x = 0; x < tempArray1.size(); x++) {
						if (!(tempArray.contains(tempArray1.get(x)))) {
							in2.add(tempArray1.get(x));
						}
					}
					for (int x = 0; x < in1.size(); x++) {
						finalOut.add(in1.get(x));
					}
					for (int x = 0; x < in2.size(); x++) {
						if (!(in1.contains(in2.get(x)))) {
							finalOut.add(in2.get(x));
						}
					}

					break;
				}

			}

		}
		return finalOut;
	}

	public static void insertIntoTable(String strTableName, Hashtable<String, Object> htblColNameValue,
			String clustringKey) throws Exception

	{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(strTableName + ".ser"));
		Table t = (Table) ois.readObject();
		ois.close();
		if (!(checker(strTableName, htblColNameValue))) {
			System.out.println("Wrong Type");
		} else {
			t.insert(htblColNameValue, clustringKey);
			ObjectOutputStream tables = new ObjectOutputStream(new FileOutputStream(strTableName + ".ser"));
			tables.writeObject(t);
			tables.close();
		}

	}

	public static boolean checker(String tablename, Hashtable<String, Object> namevalue) throws Exception {

		try (BufferedReader br = new BufferedReader(new FileReader("metaData" + tablename + ".csv"))) {
			String line;
			ArrayList<String[]> records = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				records.add(values);
			}

			Set<java.util.Map.Entry<String, Object>> set1 = namevalue.entrySet();
			Iterator<java.util.Map.Entry<String, Object>> it1 = set1.iterator();
			while (it1.hasNext()) {
				java.util.Map.Entry<String, Object> en = it1.next();
				for (int i = 0; i < records.size(); i++) {
				
					if ((en.getKey()).equals((records.get(i))[1]) && tablename.equals((records.get(i))[0])) {
						if ((en.getValue().getClass()).equals(Class.forName((records.get(i))[2]))) {
							break;
						} else

							return false;

					}
				}

			}

		}
		return true;
	}

	public static void createBitmapIndex(String strTableName, String strColName) throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(strTableName + ".ser"));
		Table t = (Table) ois.readObject();
		ois.close();
		t.createBitmapIndex(strColName);
		ObjectOutputStream tables = new ObjectOutputStream(new FileOutputStream(strTableName + ".ser"));
		tables.writeObject(t);
		tables.close();

	}

	public static void deleteFromTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws Exception

	{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(strTableName + ".ser"));
		Table t = (Table) ois.readObject();
		ois.close();
		t.deleteRow(htblColNameValue);
		ObjectOutputStream tables = new ObjectOutputStream(new FileOutputStream(strTableName + ".ser"));
		tables.writeObject(t);
		tables.close();

	}

	public static void updateTable(String strTableName, String strKey, Hashtable<String, Object> htblColNameValue)
			throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(strTableName + ".ser"));
		Table t = (Table) ois.readObject();
		ois.close();
		t.updateRow(htblColNameValue, strKey);
		ObjectOutputStream tables = new ObjectOutputStream(new FileOutputStream(strTableName + ".ser"));
		tables.writeObject(t);
		tables.close();
	}

	public static void main(String[] args) throws Exception {
		Hashtable<String, String> p = new Hashtable<String, String>();
		p.put("name", "java.lang.String");
		p.put("ID", "java.lang.Integer");
		p.put("add", "java.lang.String");
		createTable("db", "ID", p);

		Hashtable<String, Object> hashtable1 = new Hashtable<String, Object>();
		hashtable1.put("name", new String("mostafa"));
		hashtable1.put("ID", new Integer(3));
		hashtable1.put("add", new String("mostafa"));
		// System.out.println((tdb.get(0)).getPage("0" + ".ser").rows.get(0));
		Hashtable<String, Object> hashtable2 = new Hashtable<String, Object>();
		hashtable2.put("name", new String("tremo"));
		hashtable2.put("ID", new Integer(1));
		hashtable2.put("add", new String("tremo"));

		Hashtable<String, Object> hashtable3 = new Hashtable<String, Object>();
		hashtable3.put("name", new String("mostafa"));
		hashtable3.put("ID", new Integer(2));
		hashtable3.put("add", new String("mostad1d2a"));

		Hashtable<String, Object> hashtable4 = new Hashtable<String, Object>();
		hashtable4.put("name", new String("tremo"));
		hashtable4.put("ID", new Integer(4));
		hashtable4.put("add", new String("mostad1d2a"));

		Hashtable<String, Object> hashtable5 = new Hashtable<String, Object>();
		hashtable5.put("name", new String("ah"));
		hashtable5.put("ID", new Integer(0));
		hashtable5.put("add", new String("moddd2a"));

		Hashtable<String, Object> hashtable6 = new Hashtable<String, Object>();
		hashtable6.put("name", new String("ahmed"));
		hashtable6.put("ID", new Integer(5));
		hashtable6.put("add", new String("moddd2a"));
//
//		// Insertion into Table

		// System.out.println("page 0"+(tdb.get(0)).getPage("0" +
		// ".ser").rows.toString());
		// System.out.println((tdb.get(0)).getPage("0" + ".ser").rows.toString());
//		for (int i = 0; i < 5; i++) {
//			insertIntoTable("db", hashtable2);
//		}
//
//		// Print after insertion
//	
//
//		// Deletion from table
////		deleteFromTable("db", hashtable1);
////		for (int i = 0; i < 5; i++) {
////			deleteFromTable("db", hashtable2);
////		}
//
		// for(int x =0;x<30;x++) {
//----------------------------------------------------------insert to the tables
		insertIntoTable("db", hashtable6, "ID");
		insertIntoTable("db", hashtable5, "ID");
		insertIntoTable("db", hashtable4, "ID");
		insertIntoTable("db", hashtable3, "ID");
		insertIntoTable("db", hashtable1, "ID");
		insertIntoTable("db", hashtable2, "ID");
//------------------------------------------------------------print the pages		
		for (int x = 0; x < 3; x++) {
			System.out.println("page" + x + (tdb.get(0)).getPage(x + "" + ".ser").rows.toString());
		}

		System.out.println("the update part ----------------------------------------------------------------------");

		// createBitmapIndex("db", "ID");

		ObjectInputStream oi = new ObjectInputStream(new FileInputStream("db" + ".ser"));
		Table t2 = (Table) oi.readObject();
		oi.close();

//		System.out.println("values :" + t2.getindexPage("0indexID.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("0indexID.ser").Index.get(0)[1].toString());
//		System.out.println("values :" + t2.getindexPage("0indeXID.ser").Index.get(1)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("0indexID.ser").Index.get(1)[1].toString());
//		System.out.println("values :" + t2.getindexPage("1indexID.ser").Index.get(1)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("1indexID.ser").Index.get(1)[1].toString());
//		System.out.println("values :" + t2.getindexPage("1indexID.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("1indexID.ser").Index.get(0)[1].toString());
//	 		
//	 		
//		System.out.println("values :" + t2.getindexPage("2indexID.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("2indexID.ser").Index.get(0)[1].toString());
//		System.out.println("values :" + t2.getindexPage("2indexID.ser").Index.get(1)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("2indexID.ser").Index.get(1)[1].toString());

//-----------------------------------------------------------------update table
		createBitmapIndex("db", "ID");
		createBitmapIndex("db", "name");

		Hashtable<String, Object> s = new Hashtable<String, Object>();
		s.put("name", new String("dawy"));
		s.put("ID", new Integer(1));
		s.put("add", new String("mostafa el nahas street"));
		updateTable("db", "1", s);

		for (int x = 0; x < 3; x++) {
			System.out.println("page" + x + (tdb.get(0)).getPage(x + "" + ".ser").rows.toString());
		}

//		System.out.println("values :" + t2.getindexPage("0indexID.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("0indexID.ser").Index.get(0)[1].toString());
//		System.out.println("values :" + t2.getindexPage("0indeXID.ser").Index.get(1)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("0indexID.ser").Index.get(1)[1].toString());
//		System.out.println("values :" + t2.getindexPage("1indexID.ser").Index.get(1)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("1indexID.ser").Index.get(1)[1].toString());
//		System.out.println("values :" + t2.getindexPage("1indexID.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("1indexID.ser").Index.get(0)[1].toString());
////		 		
////		 		
//		System.out.println("values :" + t2.getindexPage("2indexID.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("2indexID.ser").Index.get(0)[1].toString());
//		System.out.println("values :" + t2.getindexPage("2indexID.ser").Index.get(1)[0].toString());
//		System.out.println("bit map index  :" + t2.getindexPage("2indexID.ser").Index.get(1)[1].toString());
//		
//		
//		 System.out.println("values :"+t2.getindexPage("0indexname.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :"+t2.getindexPage("0indexname.ser").Index.get(0)[1].toString());
//		 System.out.println("values :"+t2.getindexPage("0indexname.ser").Index.get(1)[0].toString());
//		System.out.println("bit map index  :"+t2.getindexPage("0indexname.ser").Index.get(1)[1].toString());
//	    	 System.out.println("values :"+t2.getindexPage("1indexname.ser").Index.get(1)[0].toString());
//	 		System.out.println("bit map index  :"+t2.getindexPage("1indexname.ser").Index.get(1)[1].toString());
//	 		System.out.println("values :"+t2.getindexPage("1indexname.ser").Index.get(0)[0].toString());
//	 		System.out.println("bit map index  :"+t2.getindexPage("1indexname.ser").Index.get(0)[1].toString());
////	 		
//	 		System.out.println("values :" + t2.getindexPage("2indexname.ser").Index.get(0)[0].toString());
//			System.out.println("bit map index  :" + t2.getindexPage("2indexname.ser").Index.get(0)[1].toString());
//			System.out.println("values :" + t2.getindexPage("2indexname.ser").Index.get(1)[0].toString());
//			System.out.println("bit map index  :" + t2.getindexPage("2indexname.ser").Index.get(1)[1].toString());
//----------------------------------------------------------------delete from table
//			
//			 hashtable3.remove("touchDate");
//			 deleteFromTable("db", hashtable3);
//			
//			
//			 for (int x = 0; x < 3; x++) {
//					System.out.println("page" + x + (tdb.get(0)).getPage(x + "" + ".ser").rows.toString());
//				}	
//			 hashtable1.remove("touchDate");
//			 deleteFromTable("db", hashtable1);
//			
//			
//			 for (int x = 0; x < 3; x++) {
//					System.out.println("page" + x + (tdb.get(0)).getPage(x + "" + ".ser").rows.toString());
//				}	

//			

		// }

		// insertIntoTable("db", hashtable1,"ID");

//

		// System.out.println(tdb.get(0).indexpages.size() + "we are no looking
		// here******************");

//     
//
//		ObjectInputStream oi = new ObjectInputStream(new FileInputStream("db" + ".ser"));
//		Table t2 = (Table) oi.readObject();
//		oi.close();
//		for (int i = 0; i < t2.indexpages.size(); i++) {
//			System.out.println(
//					t2.indexpages.get(i).colname + "***************************************************colname");
//		}

//	 System.out.println("values :"+t2.getindexPage("0indexname.ser").Index.get(0)[0].toString());
//	System.out.println("bit map index  :"+t2.getindexPage("0indexname.ser").Index.get(0)[1].toString());
//	 System.out.println("values :"+t2.getindexPage("0indexname.ser").Index.get(1)[0].toString());
//	System.out.println("bit map index  :"+t2.getindexPage("0indexname.ser").Index.get(1)[1].toString());
//    	 System.out.println("values :"+t2.getindexPage("1indexname.ser").Index.get(1)[0].toString());
// 		System.out.println("bit map index  :"+t2.getindexPage("1indexname.ser").Index.get(1)[1].toString());
// 		System.out.println("values :"+t2.getindexPage("1indexname.ser").Index.get(0)[0].toString());
// 		System.out.println("bit map index  :"+t2.getindexPage("1indexname.ser").Index.get(0)[1].toString());
// 		
// 		
// 		 System.out.println("values :"+t2.getindexPage("0indexID.ser").Index.get(0)[0].toString());
// 		System.out.println("bit map index  :"+t2.getindexPage("0indexID.ser").Index.get(0)[1].toString());
// 		 System.out.println("values :"+t2.getindexPage("0indeXID.ser").Index.get(1)[0].toString());
// 		System.out.println("bit map index  :"+t2.getindexPage("0indexID.ser").Index.get(1)[1].toString());
// 	    	 System.out.println("values :"+t2.getindexPage("1indexID.ser").Index.get(1)[0].toString());
// 	 		System.out.println("bit map index  :"+t2.getindexPage("1indexID.ser").Index.get(1)[1].toString());
// 	 		System.out.println("values :"+t2.getindexPage("1indexID.ser").Index.get(0)[0].toString());
// 	 		System.out.println("bit map index  :"+t2.getindexPage("1indexID.ser").Index.get(0)[1].toString());
//// 		
//// 		
// 		 System.out.println("values :"+t2.getindexPage("2indexID.ser").Index.get(0)[0].toString());
// 		System.out.println("bit map index  :"+t2.getindexPage("2indexID.ser").Index.get(0)[1].toString());
// 		 System.out.println("values :"+t2.getindexPage("2indexID.ser").Index.get(1)[0].toString());
// 		System.out.println("bit map index  :"+t2.getindexPage("2indexID.ser").Index.get(1)[1].toString());
// 	    	 System.out.println("values :"+t2.getindexPage("3index.ser").Index.get(1)[0].toString());
// 	 		System.out.println("bit map index  :"+t2.getindexPage("3index.ser").Index.get(1)[1].toString());
// 	 		System.out.println("values :"+t2.getindexPage("3index.ser").Index.get(0)[0].toString());
// 	 		System.out.println("bit map index  :"+t2.getindexPage("3index.ser").Index.get(0)[1].toString());
// 	 		System.out.println("values :"+t2.getindexPage("4index.ser").Index.get(0)[0].toString());
// 	 		System.out.println("bit map index  :"+t2.getindexPage("4index.ser").Index.get(0)[1].toString());
// 	 		System.out.println("values :"+t2.getindexPage("4index.ser").Index.get(1)[0].toString());
// 	 		System.out.println("bit map index  :"+t2.getindexPage("4index.ser").Index.get(1)[1].toString());

		SQLTerm[] arrSQLTerms;
		arrSQLTerms = new SQLTerm[4];
		arrSQLTerms[0] = new SQLTerm("db", "name", "=", "tremo");
		arrSQLTerms[1] = new SQLTerm("db", "ID", "=", "0");
		arrSQLTerms[2] = new SQLTerm("db", "name", "=", "mostafa");
		arrSQLTerms[3] = new SQLTerm("db", "name", "=", "ahmed");

		String[] strarrOperators = new String[3];
		strarrOperators[0] = "OR";
		strarrOperators[1] = "OR";
		strarrOperators[2] = "OR";

		ArrayList<Hashtable> t = selectFromTable(arrSQLTerms, strarrOperators);
		System.out.println(
				"*******************************************************************************************************");
		for (int i = 0; i < t.size(); i++) {
			System.out.println(t.get(i).toString());
		}

//     	System.out.println("page 0"+(tdb.get(0)).getPage("0" + ".ser").rows.toString());
//    	System.out.println("page 1"+(tdb.get(0)).getPage("1" + ".ser").rows.toString());
//    	System.out.println("page 2 "+(tdb.get(0)).getPage("2" + ".ser").rows.toString());

//     	System.out.println("****************************************************");

//		
//		
//		System.out.println("page 0"+(tdb.get(0)).getPage("0" + ".ser").rows.toString());
//    	System.out.println("page 1"+(tdb.get(0)).getPage("1" + ".ser").rows.toString());
//    	System.out.println("page 2 "+(tdb.get(0)).getPage("2" + ".ser").rows.toString());
//     	
//     	
//     	ObjectInputStream ois = new ObjectInputStream(new FileInputStream("db"+ ".ser"));
//		Table t = (Table) ois.readObject();
//		ois.close();

//		System.out.println("values :"+t.getindexPage("0index.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :"+t.getindexPage("0index.ser").Index.get(0)[1].toString());
//		System.out.println("values :"+t.getindexPage("0index.ser").Index.get(1)[0].toString());
//		System.out.println("bit map index  :"+t.getindexPage("0index.ser").Index.get(1)[1].toString());
//		System.out.println("values :"+t.getindexPage("1index.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :"+t.getindexPage("1index.ser").Index.get(0)[1].toString());
//		System.out.println("values :"+t.getindexPage("1index.ser").Index.get(1)[0].toString());
//		
//		System.out.println("bit map index  :"+t.getindexPage("1index.ser").Index.get(1)[1].toString());
//		System.out.println("values :"+t.getindexPage("2index.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :"+t.getindexPage("2index.ser").Index.get(0)[1].toString());
//		System.out.println("page 0"+(tdb.get(0)).getPage("0" + ".ser").rows.toString());
//    	System.out.println("page 1"+(tdb.get(0)).getPage("1" + ".ser").rows.toString());
//    	System.out.println("page 2 "+(tdb.get(0)).getPage("2" + ".ser").rows.toString());
//     	 System.out.println("hy2bdate aho**!*!*!*!*!*!*");
//     	 
//		 Hashtable<String, Object> s = new Hashtable<String, Object>();
//		 s.put("name", new String("mo"));
//		 s.put("ID", new String("7"));
//		 s.put("add", new String("m"));
//		 updateTable("db", "1", s);
//
//     	System.out.println("page 0"+(tdb.get(0)).getPage("0" + ".ser").rows.toString());
//    	System.out.println("page 1"+(tdb.get(0)).getPage("1" + ".ser").rows.toString());
//    	System.out.println("page 2 "+(tdb.get(0)).getPage("2" + ".ser").rows.toString());
//     	

//		System.out.println("values :"+t2.getindexPage("0index.ser").Index.get(1)[0].toString());
//		System.out.println("bit map index  :"+t2.getindexPage("0index.ser").Index.get(1)[1].toString());
//		System.out.println("values :"+t2.getindexPage("1index.ser").Index.get(0)[0].toString());
//		System.out.println("bit map index  :"+t2.getindexPage("1index.ser").Index.get(0)[1].toString());
//		System.out.println("values :"+t.getindexPage("1index.ser").Index.get(1)[0].toString());
//		System.out.println("bit map index  :"+t.getindexPage("1index.ser").Index.get(1)[1].toString());
		// System.out.println("values
		// :"+t.getindexPage("2index.ser").Index.get(0)[0].toString());
		// System.out.println("bit map index
		// :"+t.getindexPage("2index.ser").Index.get(0)[1].toString());

//	   deleteFromTable("db", hashtable3);
//		System.out.println((tdb.get(0)).getPage("0" + ".ser").rows.toString());

//		System.out.println((tdb.get(0)).getPage("0" + ".ser").rows.toString());
		// deleteFromTable("db", s);
		// deleteFromTable("db", h);
		// deleteFromTable("db", h);

	}
}
