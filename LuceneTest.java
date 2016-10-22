import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeMap;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class LuceneTest {
	public static void main(String[] args) {
		try {
			String indexPath = args[0];
			String outputPath = args[1];
			String inputPath = args[2];
			File file = new File(outputPath);
			file.delete();
			file.createNewFile();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath),StandardCharsets.UTF_8));
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath),StandardCharsets.UTF_8));
			//BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:/Users/Navneet/Desktop/IR/Invertedindex.txt"),StandardCharsets.UTF_8));
					//new FileWriter(new File("C:/Users/Navneet/Desktop/IR/Invertedindex.txt")));// args[1]

			HashMap<String, LinkedList<Integer>> invertedIndex = getInvertedIndex(indexPath);
			String text = null;
			while ((text = reader.readLine()) != null) {
//				System.out.println(text);
				ArrayList<String> list = new ArrayList<String>();
				for (String value : text.split(" ")) {
					if (!value.equals(""))
						list.add(value);
				}
				getPostings(invertedIndex, list, writer);
				getTaatAnd(invertedIndex, list, writer);
				getTaatOr(invertedIndex, list, writer);
				PerformDAAT(invertedIndex, list, writer);
			}

			reader.close();
			writer.close();
		//	bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String printLinkedList(LinkedList<Integer> finalList) {
		String string = "";
		//System.out.println(finalList.size());
		for (Integer integer : finalList) {
			string = string + " " + integer;
		}
		if (string.length() < 1) {
			string = "empty";
		}
		return string.trim();
	}

	private static String printArrayList(ArrayList<String> list) {
		String string = "";
		for (String term : list) {
			string = string + term + " ";
		}
		return string.trim();
	}

	private static void PerformDAAT(HashMap<String, LinkedList<Integer>> invertedIndex, ArrayList<String> list,
			BufferedWriter writer) {
		try {
			int count = 0;
			ArrayList<LinkedList<Integer>> listbox = new ArrayList<LinkedList<Integer>>();
			ArrayList<ListIterator<Integer>> listItr = new ArrayList<ListIterator<Integer>>();
			int minVal1 = Integer.MAX_VALUE;
			TreeMap<Integer, Integer> daatmap = new TreeMap();
			LinkedList<Integer> daatAndFinalList = new LinkedList();
			LinkedList<Integer> daatOrFinalList = new LinkedList();
			LinkedList<Integer> arrayOfMin = new LinkedList();
			boolean run = true;
			int dID = 0, finishCount = 0;
			int noOfTerms = list.size();

			for (int i = 0; i <= noOfTerms - 1; i++) {
				listItr.add(new LinkedList<Integer>(invertedIndex.get(list.get(i))).listIterator());
			}

			boolean[] check = new boolean[listItr.size()];
			// writer.write("Check ==>" + Arrays.toString(check));
			while (run) {
				// writer.write("\n*******************************");
				// writer.write("\nMIN => " + minVal1);
				for (ListIterator itr : listItr) {
					// writer.write("\n===========================");
					if (!itr.hasPrevious()) {
						dID = itr.hasNext() ? (int) itr.next() : -1;
					} else if (check[listItr.indexOf(itr)] == false) {
						itr.previous();
						dID = itr.hasNext() ? (int) itr.next() : -1;
					} else
						dID = -1;

					// writer.write("\nIterator Index = " + listItr.indexOf(itr)
					// + " <===> DocID =" + dID);
					if (dID > -1) {
						if (dID < minVal1) {
							count++;
							// writer.write("\nComparison#" + count + " => " +
							// dID + " < " + minVal1);
							minVal1 = dID;
							arrayOfMin = new LinkedList<>();
							arrayOfMin.add(listItr.indexOf(itr));
							if (minVal1 < Integer.MAX_VALUE) {
								// writer.write("Adding value to daatmap > " +
								// minVal1);
								daatmap.put(minVal1, 1);
							}
						} else if (dID == minVal1) {
							count++;
							// writer.write("\nComparison#" + count + " => " +
							// dID + " < " + minVal1);
							if (!arrayOfMin.contains(listItr.indexOf(itr))) {
								arrayOfMin.add(listItr.indexOf(itr));
								// writer.write("\nAdding value of doc ID to
								// daatmap > " + dID);
								daatmap.put(minVal1, daatmap.get(minVal1) + 1);
							}
						}
					} else {
						finishCount++;
						check[listItr.indexOf(itr)] = true;
					}

					// writer.write("\nMIN => " + minVal1);
					// writer.write("\nArrayOfMin => " + arrayOfMin.toString());
				}
				// writer.write("\nDaatMap => " + daatmap.size());
				// writer.write("\nDaatMap => " + daatmap.toString());
				// writer.write("\nfinish count =>" + finishCount);
				// writer.write("\nCheck ==>" + Arrays.toString(check));
				// writer.write("\n*******************************");
				if (finishCount == listItr.size())
					run = false;

				for (ListIterator itr : listItr) {
					if (arrayOfMin.contains(listItr.indexOf(itr))) {
						if (check[listItr.indexOf(itr)] == false)
							if (itr.hasNext() == false) {
								check[listItr.indexOf(itr)] = true;
							} else {
								dID = (int) itr.next();
							}
						// if (check[listItr.indexOf(itr)]) {
						// writer.write("\nnew docID of Index#" +
						// listItr.indexOf(itr) + "=> " + itr.next());

						/*
						 * if (dID == -1) check[listItr.indexOf(itr)] = true;
						 */
						// }
						// writer.write("\nnew docID of Index#" +
						// listItr.indexOf(itr) + "=> " + dID);
					}
				}
				minVal1 = Integer.MAX_VALUE;
				finishCount = 0;
			}
			for (Integer key : daatmap.keySet()) {
				if (daatmap.get(key) == noOfTerms) {
					daatAndFinalList.add(key);
				}
				if (daatmap.get(key) >= 1) {
					daatOrFinalList.add(key);
				}
			}

			writer.write("DaatAnd");
			writer.write("\n" + printArrayList(list));
			if (daatAndFinalList.size() != 0) {
				writer.write("\nResults: " + printLinkedList(daatAndFinalList));
				writer.write("\nNumber of documents in results: " + daatAndFinalList.size());
				writer.write("\nNumber of comparisons: " + count);
				writer.newLine();
			} else {
				writer.write("\nResults: empty");
				writer.write("\nNumber of documents in results: 0");
				writer.write("\nNumber of comparisons: " + count);
				writer.newLine();
			}

			writer.write("DaatOr");
			writer.write("\n" + printArrayList(list));
			if (daatOrFinalList.size() != 0) {
				writer.write("\nResults: " + printLinkedList(daatOrFinalList));
				writer.write("\nNumber of documents in results: " + daatOrFinalList.size());
				writer.write("\nNumber of comparisons: " + count);
				writer.newLine();
			} else {
				writer.write("\nResults: empty");
				writer.write("\nNumber of documents in results: 0");
				writer.write("\nNumber of comparisons: " + count);
				writer.newLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getTaatAnd(HashMap<String, LinkedList<Integer>> invertedIndex, ArrayList<String> list,
			BufferedWriter writer) {
		LinkedList<Integer> l1 = null;
		// LinkedList<Integer> l2 = new LinkedList<Integer>();
		LinkedList<Integer> finalList = null;

		int count = 0;
		try {
			for (int i = 0; i <= list.size() - 1; i++) {
				// writer.write("\ni = " + i);
				l1 = new LinkedList<Integer>(invertedIndex.get(list.get(i)));
				if (finalList == null)
					finalList = new LinkedList<Integer>(l1);
				else {
					ListIterator<Integer> l1it = l1.listIterator();
					ListIterator<Integer> l2it = finalList.listIterator();
					// writer.write("\nInside Else");
					Integer r = l1it.hasNext() ? l1it.next() : null;
					Integer p = l2it.hasNext() ? l2it.next() : null;

					// writer.write("\n========================================");
					while (r != null || p != null) {
						if (r != null && p != null) {
							// writer.write("\nFinalList ==>" + finalList);
							// writer.write("\nL1 =========>" + l1);
							if (r.intValue() == p.intValue()) {
								count++;
								// writer.write("\nComparison : " + count + " =>
								// " + r + " = " + p);
								l1it.remove();
								r = l1it.hasNext() ? l1it.next() : null;
								p = l2it.hasNext() ? l2it.next() : null;

							} else if (r.intValue() < p.intValue()) {
								count++;
								// writer.write("\nComparison : " + count + " =>
								// " + r + " < " + p);
								l1it.remove();
								// l1it = l1.listIterator();
								r = l1it.hasNext() ? l1it.next() : null;
							} else if (r.intValue() > p.intValue()) {
								count++;
								// writer.write("\nComparison : " + count + " =>
								// " + r + " > " + p);
								l2it.remove();
								// l2it = finalList.listIterator();
								p = l2it.hasNext() ? l2it.next() : null;
							}

							writer.flush();
							// writer.write(r);
							// writer.write(p);
						} else if (p != null && r == null)
							do {
								l2it.remove();
								p = l2it.hasNext() ? l2it.next() : null;
							} while (p != null);
						if (p == null && r != null)
							break;
					}
				}
			}
			writer.write("TaatAnd");
			writer.write("\n" + printArrayList(list));
			if (finalList.size() != 0) {
				writer.write("\nResults: " + printLinkedList(finalList));
				writer.write("\nNumber of documents in results: " + finalList.size());
				writer.write("\nNumber of comparisons: " + count);
				writer.newLine();
			} else {
				writer.write("\nResults: empty");
				writer.write("\nNumber of documents in results: 0");
				writer.write("\nNumber of comparisons: " + count);
				writer.newLine();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getTaatOr(HashMap<String, LinkedList<Integer>> invertedIndex, ArrayList<String> list,
			BufferedWriter writer) {
		LinkedList<Integer> l1 = null;
		// LinkedList<Integer> l2 = new LinkedList<Integer>();
		LinkedList<Integer> finalList = null;

		int count = 0;
		try {
			for (int i = 0; i <= list.size() - 1; i++) {
				// writer.write("\ni = " + i);
				l1 = new LinkedList<Integer>(invertedIndex.get(list.get(i)));
				if (finalList == null)
					finalList = new LinkedList<Integer>(l1);
				else {
					ListIterator<Integer> currentDocIt = l1.listIterator();
					ListIterator<Integer> finalDocIt = finalList.listIterator();
					// writer.write("\nInside Else");
					Integer currentDoc = currentDocIt.hasNext() ? currentDocIt.next() : null;
					Integer finalDoc = finalDocIt.hasNext() ? finalDocIt.next() : null;

					// writer.write("\n========================================");
					while (currentDoc != null || finalDoc != null) {
						if (currentDoc != null && finalDoc != null) {
							// writer.write("\nFinalList ==>" + finalList);
							// writer.write("\nL1 =========>" + l1);
							if (currentDoc.intValue() == finalDoc.intValue()) {
								count++;
								// writer.write("\nComparison : " + count + " =>
								// " + currentDoc + " = " + finalDoc);
								currentDocIt.remove();
								currentDoc = currentDocIt.hasNext() ? currentDocIt.next() : null;
								finalDoc = finalDocIt.hasNext() ? finalDocIt.next() : null;

							} else if (currentDoc.intValue() < finalDoc.intValue()) {
								count++;
								// writer.write("\nComparison : " + count + " =>
								// " + currentDoc + " < " + finalDoc);

								finalDocIt.previous();
								finalDocIt.add(currentDoc);
								currentDocIt.remove();
								currentDoc = currentDocIt.hasNext() ? currentDocIt.next() : null;
								finalDoc = finalDocIt.hasNext() ? finalDocIt.next() : null;
								if (currentDoc == null)
									break;

							} else if (currentDoc.intValue() > finalDoc.intValue()) {
								count++;
								// writer.write("\nComparison : " + count + " =>
								// " + currentDoc + " > " + finalDoc);
								finalDoc = finalDocIt.hasNext() ? finalDocIt.next() : null;

							}
							writer.flush();
						} else if (finalDoc == null && currentDoc != null) {
							do {
								// finalDocIt.previous();
								finalDocIt.add(currentDoc);
								currentDocIt.remove();
								currentDoc = currentDocIt.hasNext() ? currentDocIt.next() : null;
							} while (currentDoc != null);
						} else if (currentDoc == null) {
							break;
						}
					}

				}
			}
			writer.write("TaatOr");
			writer.write("\n" + printArrayList(list));
			if (finalList.size() != 0) {
				writer.write("\nResults:" + printLinkedList(finalList));
				writer.write("\nNumber of documents in results: " + finalList.size());
				writer.write("\nNumber of comparisons: " + count);
				writer.newLine();
			} else {
				writer.write("\nResults: empty");
				writer.write("\nNumber of documents in results: 0");
				writer.write("\nNumber of comparisons: " + count);
				writer.newLine();
			}

		} catch (

		IOException e)

		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void getPostings(HashMap<String, LinkedList<Integer>> invertedIndex, ArrayList<String> list,
			BufferedWriter writer) {
		try {
			// writer.write("\n#############################");
			for (String term : list) {
				writer.write("GetPostings");
				writer.newLine();
				writer.write(term);
				writer.newLine();
				// LinkedList value = ;
				System.out.println(term);
				System.out.println(invertedIndex.get(term));
				writer.write("Postings list: " + printLinkedList(invertedIndex.get(term)));
				writer.newLine();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static HashMap<String, LinkedList<Integer>> getInvertedIndex( String path)
			throws IOException {
		FileSystem fs = FileSystems.getDefault();
		Path path1 = fs.getPath(path);
		IndexReader reader = DirectoryReader.open(FSDirectory.open(path1));
		Fields fields = MultiFields.getFields(reader);
		HashMap<String, LinkedList<Integer>> invertedIndex = new HashMap<>();
		LinkedList<Integer> docIDList = null;
		for (String fieldName : fields) {
			if (fieldName.contains("text_")) {
				// writer.write("fieldName:" + fieldName);
				Terms terms = fields.terms(fieldName);
				TermsEnum termsEnum = terms.iterator();
				BytesRef text = null;
				while ((text = termsEnum.next()) != null) {
					String term = text.utf8ToString();
					// System.out.print("---->"+term+":");
					PostingsEnum postings = MultiFields.getTermDocsEnum(reader, fieldName, text);
					int docID = 0;
					docIDList = new LinkedList<Integer>();
					while ((docID = postings.nextDoc()) != postings.NO_MORE_DOCS) {
						// System.out.print("|" + docID);
						docIDList.add(docID);
					}
					// writer.write(docIDList.size());
					invertedIndex.put(term, docIDList);
					//bw.write(term + ":" + docIDList);
					//bw.newLine();
					// writer.write();
				}
				// writer.write("******************");
				// bw.write("Size of Map: " + invertedIndex.size());
			}
		}
		//bw.close();
		//System.out.println(invertedIndex.size());
		return invertedIndex;
	}
}