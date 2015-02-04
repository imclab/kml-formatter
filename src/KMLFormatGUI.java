import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

public class KMLFormatGUI implements TreeSelectionListener {

	// private JList list;
	private JTree tree;

	private JFrame frmKmlFormatter;
	private JEditorPane contentPane;

	private Parse parse;

	private Set<String> lithicGroups;
	private Set<String> ages;
	private Set<String> agesWithLithicGroups;

	private JProgressBar progressBar;

	private JFileChooser fc;

	private Boolean hasFile = false;
	private Boolean createdGUI = false;

	private JSplitPane splitPane;

	private JMenuBar menuBar_1;
	private JMenuItem mntmOpenKml;
	private JMenu mnExportAs;

	private int count = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					KMLFormatGUI window = new KMLFormatGUI();
					window.frmKmlFormatter.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public KMLFormatGUI() {
		initialize();

		// BEGIN MY CODE
		// Parse p = new Parse();
		// List<Feature> f = p.getFeatures();

		/*
		 * DefaultListModel model = new DefaultListModel(); for (Object o : f) {
		 * 
		 * Placemark placemark = (Placemark) o;
		 * model.addElement(placemark.getName()); }
		 * 
		 * list.setModel(model);
		 */
	}

	private void openFile() {

		count++;

		if (createdGUI) {
			frmKmlFormatter.getContentPane().remove(splitPane);

		}
		if (!createdGUI) {
			createdGUI = true;
		}

		DefaultMutableTreeNode top = new DefaultMutableTreeNode(
				parse.getDocumentName());
		createNodes(top);

		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		tree.addTreeSelectionListener(this);

		JScrollPane scrollPane = new JScrollPane(tree);
		// scrollPane.setBounds(10, 25, 572, 537);
		// frmKmlFormatter.getContentPane().add(scrollPane);

		contentPane = new JEditorPane();
		contentPane.setEditable(false);
		JScrollPane contentScroll = new JScrollPane(contentPane);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(scrollPane);
		splitPane.setRightComponent(contentScroll);

		// Dimension minimumSize = new Dimension(250, 200);
		frmKmlFormatter.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		// scrollPane.setMinimumSize(minimumSize);
		// contentScroll.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(300);
		// splitPane.setPreferredSize(new Dimension(200, 500));

		frmKmlFormatter.getContentPane().add(splitPane);

		frmKmlFormatter.validate();

	}

	// Prepare for agnostic use?
	/*
	 * private void createNodesAgnostic(DefaultMutableTreeNode top) {
	 * DefaultMutableTreeNode children = null; List<Feature> features =
	 * parse.getFeatures();
	 * 
	 * for (Feature feat : features) { children = new
	 * DefaultMutableTreeNode(feat.getName());
	 * 
	 * for (Object feat2 : feat.getFeatureSimpleExtension()) {
	 * DefaultMutableTreeNode down = new DefaultMutableTreeNode( ((Feature)
	 * feat2).getName()); children.add(down); } top.add(children); } }
	 */

	// Create the nodes for the USGS KML parser
	private void createNodes(DefaultMutableTreeNode top) {
		try {
			DefaultMutableTreeNode list = null;
			DefaultMutableTreeNode groups = null;
			DefaultMutableTreeNode age = null;
			DefaultMutableTreeNode ageWithLithic = null;

			DefaultMutableTreeNode child = null;

			list = new DefaultMutableTreeNode("Features");
			top.add(list);

			groups = new DefaultMutableTreeNode("Lithic Groups");
			top.add(groups);

			age = new DefaultMutableTreeNode("Age");
			top.add(age);

			ageWithLithic = new DefaultMutableTreeNode(
					"Age with Lithic Subgroups");
			top.add(ageWithLithic);

			List<Feature> placemarks = parse.getFeatures();
			lithicGroups = new TreeSet<String>();
			ages = new TreeSet<String>();

			// Add the already existing features into a folder
			for (Object obj : placemarks) {

				try {
					Placemark p = (Placemark) obj;
					Pmark pmark = new Pmark(p.getName(), p.getDescription(),
							p.getStyleUrl());

					lithicGroups.add(pmark.getLithicGroup());
					ages.add(pmark.getAge());

					child = new DefaultMutableTreeNode(pmark);
					list.add(child);
				} catch (ClassCastException e) {
				}

			}

			// Add the lithic groups to the top level folder
			for (String str : lithicGroups) {

				Dir d = new Dir(str);

				child = new DefaultMutableTreeNode(d);
				groups.add(child);
			}

			// Add lithic group items
			for (Object obj : placemarks) {
				Placemark p = (Placemark) obj;
				String group = Pmark.getLithicGroupGlobal(p.getStyleUrl());

				int index = 0;
				try {
					while (groups.children().hasMoreElements()) {
						if (groups.getChildAt(index).toString().equals(group)) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) groups
									.getChildAt(index);

							node.add(new DefaultMutableTreeNode(p.getName()));
						}
						index++;
					}
				} catch (IndexOutOfBoundsException excp) {
				}

			}

			// Create directiories for the ages
			for (String str : ages) {
				Dir d = new Dir(str);

				child = new DefaultMutableTreeNode(d);
				age.add(child);
				// ageWithLithic.add(child);

			}

			for (String str : ages) {
				Dir d = new Dir(str);

				child = new DefaultMutableTreeNode(d);
				// age.add(child);
				ageWithLithic.add(child);

			}

			// Add the ages to the folder
			for (Object obj : placemarks) {
				Placemark p = (Placemark) obj;
				String ageStr = Pmark.getAgeGlobal(p.getName());

				int index = 0;
				try {
					while (age.children().hasMoreElements()) {
						if (age.getChildAt(index).toString().equals(ageStr)) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) age
									.getChildAt(index);

							node.add(new DefaultMutableTreeNode(p.getName()));
						}
						index++;
					}
				} catch (IndexOutOfBoundsException excp) {
				}

			}

			// Add the ages to the folder w/ lithic groups
			for (Object obj : placemarks) {
				Placemark p = (Placemark) obj;
				String ageStr = Pmark.getAgeGlobal(p.getName());
				DefaultMutableTreeNode node = null;
				DefaultMutableTreeNode subChild = null;
				DefaultMutableTreeNode subSubChild = null;

				int index = 0;
				try {
					while (ageWithLithic.children().hasMoreElements()) {
						
						List<DefaultMutableTreeNode> seenNodes = new ArrayList<DefaultMutableTreeNode>();
						
						if (ageWithLithic.getChildAt(index).toString()
								.equals(ageStr)) {
							node = (DefaultMutableTreeNode) ageWithLithic
									.getChildAt(index);
							
							// node.add(new
							// DefaultMutableTreeNode(p.getName()));

							// Add the lithic groups to the top level folder
							for (String str : lithicGroups) {

								Dir d = new Dir(str);

								subChild = new DefaultMutableTreeNode(d);
								if (Pmark.getLithicGroupGlobal(p.getStyleUrl())
										.equals(str)) {
									seenNodes.add(subChild);
									
									node.add(subChild);
									Dir d2 = (Dir) subChild.getUserObject();
									// if(d2.toString().equals)
									// node = (DefaultMutableTreeNode)
									// ageWithLithic
									// .getChildAt(index);

									subChild.add(new DefaultMutableTreeNode(p
											.getName()));

								}

							}

					
						}

						index++;
					}
				}

				/*
				 * while(ageWithLithic.children().hasMoreElements()){ subChild =
				 * (DefaultMutableTreeNode)
				 * ageWithLithic.children().nextElement(); int subIndex = 0;
				 * 
				 * while(subChild.children().hasMoreElements()) { subSubChild =
				 * (DefaultMutableTreeNode) subChild.getChildAt(subIndex);
				 * Object subSubObj = subSubChild.getUserObject();//subSubChild
				 * = (Placemark) subSubChild; //subSubObj = (String) subSubObj;
				 * 
				 * agesWithLithicGroups.add((String)subSubObj);
				 * System.out.println((String)subSubObj); subIndex++;
				 * 
				 * }
				 * 
				 * 
				 * }
				 */
				// }

				catch (IndexOutOfBoundsException excp) {
				}

			}

			// Try and catch errors when it is not a proper USGS file
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frmKmlFormatter,
					"Possible errors opening this file!\nAre you sure it is"
							+ " an unchanged USGS State KML?", "Error", 2);
		}

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't use system look and feel.");
		}

		frmKmlFormatter = new JFrame();
		frmKmlFormatter.setTitle("USGS KML Formatter");
		frmKmlFormatter.setBounds(100, 100, 800, 600);
		frmKmlFormatter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// parse.addLithicGroupFolders();//Test folder

		// list = new JList();
		// Create the nodes.
		if (hasFile) {
			DefaultMutableTreeNode top = new DefaultMutableTreeNode(
					parse.getDocumentName());
			createNodes(top);

			tree = new JTree(top);
			tree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);

			tree.addTreeSelectionListener(this);

			JScrollPane scrollPane = new JScrollPane(tree);
			// scrollPane.setBounds(10, 25, 572, 537);
			// frmKmlFormatter.getContentPane().add(scrollPane);

			contentPane = new JEditorPane();
			contentPane.setEditable(false);
			JScrollPane contentScroll = new JScrollPane(contentPane);

			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setLeftComponent(scrollPane);
			splitPane.setRightComponent(contentScroll);

			// Dimension minimumSize = new Dimension(250, 200);
			frmKmlFormatter.getContentPane().setLayout(
					new GridLayout(0, 1, 0, 0));
			// scrollPane.setMinimumSize(minimumSize);
			// contentScroll.setMinimumSize(minimumSize);
			splitPane.setDividerLocation(300);
			// splitPane.setPreferredSize(new Dimension(200, 500));

			frmKmlFormatter.getContentPane().add(splitPane);
		}

		fc = new JFileChooser();

		menuBar_1 = new JMenuBar();
		frmKmlFormatter.setJMenuBar(menuBar_1);

		JMenu mnFile = new JMenu("File");
		menuBar_1.add(mnFile);

		/*
		 * 
		 * 
		 * OPEN FILE DIALOG
		 */
		mntmOpenKml = new JMenuItem("Open File");
		mntmOpenKml.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if (mntmOpenKml.isEnabled()) {
					int returnValue = fc.showOpenDialog(null);

					if (returnValue == JFileChooser.APPROVE_OPTION) {
						mntmOpenKml.setEnabled(false);
						mnExportAs.setEnabled(false);

						progressBar.setVisible(true);
						class MyWorker1 extends SwingWorker<String, Void> {
							protected String doInBackground() {
					
								File selectedFile = fc.getSelectedFile();

								try {
									InputStream is = new FileInputStream(
											selectedFile);

									if (selectedFile.getName().contains("kmz")) {
										parse = new Parse(is, true);
										parse = null;
									} else {
										parse = new Parse(is, false);
										openFile();
									}
								} catch (IOException e) {

								}
								hasFile = true;

								return "Done";

							}

							protected void done() {
								mnExportAs.setEnabled(true);
								mntmOpenKml.setEnabled(true);

								progressBar.setVisible(false);

							}
						}

						new MyWorker1().execute();

					} else {

					}
				}
			}
		});
		mnFile.add(mntmOpenKml);

		mnExportAs = new JMenu("Export as...");
		mnFile.add(mnExportAs);
		mnExportAs.setEnabled(false);

		JMenuItem mntmKmlall = new JMenuItem("KML");
		mntmKmlall.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				int returnVal = fc.showSaveDialog(null);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					mntmOpenKml.setEnabled(false);
					mnExportAs.setEnabled(false);

					final File file = fc.getSelectedFile();

					List<Feature> placemarks = parse.getFeatures();
					Object[] placm = placemarks.toArray();

					if (count == 1) {
						parse.createSepFolders();
						count = 0;
					}

					for (String str : ages) {

						Folder folder = parse.addAgeFolders(str);

						for (Object obj : placm) {
							try {
								Placemark p = (Placemark) obj;
								if (Pmark.getAgeGlobal(p.getName()).equals(str)) {
									parse.addToFolderNoDelete(folder, p);

								}
							} catch (ClassCastException e2) {
							}
						}

					}

					for (String str : lithicGroups) {

						Folder folder = parse.addLithicGroupFolders(str);

						for (Object obj : placm) {
							try {
								Placemark p = (Placemark) obj;
								if (Pmark.getLithicGroupGlobal(p.getStyleUrl())
										.equals(str)) {
									parse.addToFolder(folder, p);

								}
							} catch (ClassCastException e) {
							}
						}

					}


					for (String str : ages) {

						Folder folder = parse.addAgeLithicFolders(str);
						List<Folder> folders = new ArrayList<Folder>();

						for (Object obj : placm) {
							try {
								Placemark p = (Placemark) obj;
								if (Pmark.getAgeGlobal(p.getName()).equals(str)) {
									Pmark temp = new Pmark(p.getName(), p
											.getDescription(), p.getStyleUrl());

				
									//If there is already a folder with the same name, put the feature in there.
									boolean foundIt = false;
									for (Folder f : folders) {
										if (f.getName().equals(
												temp.getLithicGroup())) {
											parse.addToFolderNoDelete(f, p);
											foundIt = true;
										}
									}
									if (folders.size() == 0 || foundIt == false) {
										Folder folder2 = folder
												.createAndAddFolder();
										folder2.setName(temp.getLithicGroup());
										folders.add(folder2);
										// parse.addToFolder(folder2,
										// folder);
										parse.addToFolderNoDelete(folder2, p);
									}

								}

							} catch (ClassCastException e2) {
							}
						}

					}
					progressBar.setVisible(true);
					progressBar.setIndeterminate(true);
					class MyWorker extends SwingWorker<String, Void> {
						protected String doInBackground() {

							parse.reWriteKML(file.toString());

							return "Done";

						}

						protected void done() {
							mnExportAs.setEnabled(true);
							mntmOpenKml.setEnabled(true);
							progressBar.setVisible(false);

						}
					}

					new MyWorker().execute();

				}

			}
		});
		mnExportAs.add(mntmKmlall);

		JMenuItem mntmKmz = new JMenuItem("KMZ");
		mntmKmz.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				int returnVal = fc.showSaveDialog(null);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					final File file = fc.getSelectedFile();

					List<Feature> placemarks = parse.getFeatures();
					Object[] placm = placemarks.toArray();

					if (count == 1) {
						parse.createSepFolders();
						count = 0;
					}

					for (String str : ages) {

						Folder folder = parse.addAgeFolders(str);

						for (Object obj : placm) {
							try {
								Placemark p = (Placemark) obj;
								if (Pmark.getAgeGlobal(p.getName()).equals(str)) {
									parse.addToFolderNoDelete(folder, p);

								}
							} catch (ClassCastException e2) {
							}
						}

					}

					for (String str : lithicGroups) {

						Folder folder = parse.addLithicGroupFolders(str);

						for (Object obj : placm) {
							try {
								Placemark p = (Placemark) obj;
								if (Pmark.getLithicGroupGlobal(p.getStyleUrl())
										.equals(str)) {
									parse.addToFolder(folder, p);

								}
							} catch (ClassCastException e) {
							}
						}

					}

					progressBar.setVisible(true);
					progressBar.setIndeterminate(true);
					class MyWorker extends SwingWorker<String, Void> {
						protected String doInBackground() {

							parse.reWriteKMZ(file.toString());

							return "Done";

						}

						protected void done() {
							progressBar.setVisible(false);

						}
					}

					new MyWorker().execute();

				}

			}
		});
		mnExportAs.add(mntmKmz);

		JMenu mnHelp = new JMenu("Help");
		menuBar_1.add(mnHelp);

		JMenuItem mntmUsgsStateKmls = new JMenuItem("USGS State KMLs");
		mntmUsgsStateKmls.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {

				try {
					java.awt.Desktop
							.getDesktop()
							.browse(URI
									.create("http://mrdata.usgs.gov/geology/state/"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		mnHelp.add(mntmUsgsStateKmls);

		JMenuItem mntmHelpContent = new JMenuItem("Help Content");
		mnHelp.add(mntmHelpContent);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {

				JOptionPane.showMessageDialog(frmKmlFormatter,
						"USGS KML Formatter. Developed by Jack Jamieson 2015.");

			}
		});
		mnHelp.add(mntmAbout);

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);

		menuBar_1.add(progressBar);

		// scrollPane.setViewportView(tree);

	}

	public void displayContents(Pmark p) {

		// contentPane.setText("Description:\n-----\n" + p.getDescription() +
		// "\n\n\nStyle URL:\n-----\n" + p.getStyleURL());
		contentPane.setContentType("text/html");
		HTMLDocument doc = (HTMLDocument) contentPane.getDocument();
		HTMLEditorKit editorKit = (HTMLEditorKit) contentPane.getEditorKit();
		String text = "<h3>Description:</h3>" + p.getDescription() + "\n";
		try {
			editorKit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
		} catch (BadLocationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void displayContents(Dir d) {

		// contentPane.setText("Description:\n-----\n" + p.getDescription() +
		// "\n\n\nStyle URL:\n-----\n" + p.getStyleURL());
		contentPane.setContentType("text/html");
		HTMLDocument doc = (HTMLDocument) contentPane.getDocument();
		HTMLEditorKit editorKit = (HTMLEditorKit) contentPane.getEditorKit();
		String text = "<h3>Description:</h3>" + d.toString();
		try {
			editorKit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
		} catch (BadLocationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();

		if (node == null)
			return;

		Object nodeInfo = node.getUserObject();
		if (node.isLeaf()) {
			try {
				Pmark pmark = (Pmark) nodeInfo;
				contentPane.setText("");
				displayContents(pmark);
			} catch (ClassCastException cce) {

				if (nodeInfo instanceof String) {
					Pmark p = findFeatureFromString((String) nodeInfo);
					contentPane.setText("");

					displayContents(p);
				}

				else {
					Dir dir = (Dir) nodeInfo;
					contentPane.setText("");
					displayContents(dir);
				}
			}

		}

	}

	public Pmark findFeatureFromString(String placemarkName) {
		List<Feature> placemarks = parse.getFeatures();

		for (Object obj : placemarks) {
			Placemark p = (Placemark) obj;
			Pmark pmark = new Pmark(p.getName(), p.getDescription(),
					p.getStyleUrl());
			if (pmark.getName().equals(placemarkName)) {
				return pmark;
			}

		}

		return null;
	}

}
