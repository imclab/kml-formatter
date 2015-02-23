import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;


//Jack Jamieson 2015
//This class handles the parsing of KML files using the JAK library.  Also requires Apache commons io.
//This does not have anything to do with the GUI representation of the file breakdown.
public class Parse {

	private Kml kml;
	private Document document;

	// Separate folders for each of the groups represented in the GUI.
	private Folder marks;
	private Folder groups;
	private Folder ages;
	private Folder agesWithLithic;


	
	public Parse() {

	}

	//Read in the KML
	public Parse(InputStream is, boolean isKMZ) {


		String str;
		try {
			str = IOUtils.toString(is);
			
			//The JAK library cannot read the old GE KML header so we have to replace it before reading.
			IOUtils.closeQuietly(is);
			str = str
					.replace(
							"xmlns=\"http://earth.google.com/kml/2.2\"",
							"xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"");
			ByteArrayInputStream bais = new ByteArrayInputStream(
					str.getBytes("UTF-8"));

			//KMZ files are a whole nother mess, just don't read them.
			if (!isKMZ) {
				kml = Kml.unmarshal(bais);
				document = (Document) kml.getFeature();// Extract the features and save them in a document.

			}
			else {
				JOptionPane.showMessageDialog(null, "KMZ files are not supported.", "Error", 2);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	

	}

	public String getDocumentName() {

		return document.getName();

	}

	public String getDocumentDescription() {

		return document.getDescription();
	}

	// Easy way to get the features.
	public List<Feature> getFeatures() {

		List<Feature> t = (List<Feature>) document.getFeature();

		return t;

	}
	
	// Useful for getting the set of features after they have been moved to the 'marks' folder.
	public List<Feature> secondaryGetFeatures() {
		
		List<Feature> t = (List<Feature>) marks.getFeature();

		return t;

	}

	//Create new folders in the in-memory KML
	public void createSepFolders() {
		
		// Remove the old folders before making new ones so the next export does not get cluttered.
		document.getFeature().remove(marks);
		document.getFeature().remove(groups);
		document.getFeature().remove(ages);
		document.getFeature().remove(agesWithLithic);


		marks = document.createAndAddFolder();
		marks.setName("Features");

		groups = document.createAndAddFolder();
		groups.setName("Lithic Groups");

		ages = document.createAndAddFolder();
		ages.setName("Age");
		
		agesWithLithic = document.createAndAddFolder();
		agesWithLithic.setName("Ages with Lithic Subgroups");

	}

	public Folder addLithicGroupFolders(String folderName) {
		
		Folder folder = groups.createAndAddFolder();
		folder.setName(folderName);

		return folder;
		

	}

	public Folder addAgeFolders(String folderName) {

		Folder folder = ages.createAndAddFolder();
		folder.setName(folderName);

		return folder;
	}
	
	public Folder addAgeLithicFolders(String folderName) {

		Folder folder = agesWithLithic.createAndAddFolder();
		folder.setName(folderName);

		return folder;
	}

	// The standard add, add the given placemark to the given folder, remove it afterwards from the top level.
	public void addToFolder(Folder folder, Placemark placemark) {
		folder.getFeature().add(placemark);
		marks.getFeature().add(placemark);
		document.getFeature().remove(placemark);//By removing them, the second time we export we need to check somewhere else
												//aka the 'marks' folder.

	}
	


	// Add a folder to another folder.
	public void addToFolder(Folder folder, Folder folder2) {
		folder.getFeature().add(folder2);
		
	}

	// The given placemark to the given folder, and don't delete it.
	public void addToFolderNoDelete(Folder folder, Placemark placemark) {
		folder.getFeature().add(placemark);

	}

	public void reWriteKML(String fileName) {

		try {
			kml.marshal(new File(fileName));//Write the file to a KML.
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void reWriteKMZ(String fileName) {

		try {
			kml.marshalAsKmz(fileName, kml);//Write the file to a KMZ.
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	


}