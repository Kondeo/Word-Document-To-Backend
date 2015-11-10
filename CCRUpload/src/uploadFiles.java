import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import javax.naming.NamingException;
import javax.swing.JFileChooser;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


/**
 * 
 * @author torch2424
 * 
 * This is to be used with the macro for Word Document to Html Macro:
 * https://gist.github.com/torch2424/657c7b75e52d516de148
 * 
 * This class will take a selected folder, and convert all of the html plages within to UTF, and then send it to a specified backend
 *
 */
public class uploadFiles {
	
	/**
	 * @param args
	 * @throws NamingException
	 * @throws SQLException
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws SQLException, FileNotFoundException
	{
		//Welcome The User!
		System.out.println("Hello! Welcome to the book uploader, plese selected a foler for html pages to be uploaded!");
		
		// Open our file chooser
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			// Get thefolder
			File selectedFile = chooser.getSelectedFile();
			if (selectedFile.isDirectory())
			{	
				//First convert it all to UTF
				selectedFile = utfConvert(selectedFile);
				
				//Backend Properties SQL
//				String url = "jdbc:mysql://devdb.kondeo.com:3306/";
//				String dbName = "mwwwordpairs";
//				String driver = "com.mysql.jdbc.Driver";
//				String userName = "m5rrckbr9fwpzjwj";
//				String password = "3gZQeML72QHQSFQW";
				
				try
				{
					//Create our connection to the backend, SQL
//					Class.forName(driver).newInstance();
//					Connection conn = DriverManager.getConnection(url + dbName, userName, password);
					
					//Create a new Mongo Client going to the url with the port 3000
					 MongoClient mongoClient = new MongoClient("devdb.kondeo.com", 3000);

					 //Get the database table
					MongoDatabase db = mongoClient.getDatabase("indexes");
					MongoCollection<Document> collection = db.getCollection("test");
					
					//Get our files in our directory
					File[] pages = selectedFile.listFiles();
					
					//Create a scanner
					Scanner scan = null;
					String totalString = "";
					
					//Getting the the pat of the selected 
					String paths = selectedFile.getAbsolutePath();
					
					//Loop for every file in our directory
					for(int i = 0; i < pages.length; ++i)
					{

						//Our total string
						totalString = "";
						scan = new Scanner(new File(paths 
								+ "/Page" + (i + 1) + ".html"), "UTF-8");
						
						System.out.println("Uploading Page #" + i + "!");
						
						//skip the first 6 lines
						for(int j = 0; j < 5; ++j)
						{
							if(scan.hasNextLine())
							{
								scan.nextLine();
							}
						}
						
						while(scan.hasNextLine())
						{
							//Get the line
							String code = scan.nextLine();
							
							//Check it is not /body
							if(code.contentEquals("</body>"))
							{
								//Exit the loop
								break;
							}
							else if(code.contentEquals("<body lang=EN-US>") || code.contentEquals("</head>"))
							{
								//Skip it
								//print the line
								System.out.println("SKIPPED" + code);
							}
							else
							{
								//print the line
								System.out.println(code);
								//Add it to our total string
								totalString = totalString + code;
								
							}
						}
						
//						String insertTableSQL = "INSERT INTO book (page,content) VALUES (?,?)";
//						java.sql.PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);
//						preparedStatement.setInt(1, i + 1);
//						preparedStatement.setString(2, totalString);
//						// execute insert SQL stetement
//						preparedStatement .executeUpdate();
						
						//Insert the page to the DB
						
						
					}
					
					scan.close();
//					conn.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

			}
			else
			{
				// They did not open memo.txt, warn them, and tell them to
				// re-open program
				System.out.println("Not a directory");
			}

		}

	}
	
	
	
	//Function to convert our files to UTF
	public static File utfConvert(File selectedFile)
	{
		
		//Place all of the files in the directory into an arraylist
		ArrayList<File> files = new ArrayList<File>(Arrays.asList(selectedFile.listFiles()));
		
		//Sort the arraylist
		Collections.sort(files);
		
		//Create the Converted subfolder, and delete it if it already exists
		File convertedFolder = new File(selectedFile.getAbsolutePath() + "/Converted");
		if(convertedFolder.exists() && convertedFolder.isDirectory()) 
		{
				//Delete the files within the folder
				for(File file: convertedFolder.listFiles()) file.delete();
		}
		else convertedFolder.mkdir();
		

		// Do this for all the files in the folder
		Reader in = null;
		Writer out = null;
		FileInputStream fis = null;
		for (int i = 1; i < files.size(); ++i)
		{
			try
			{
				//Create our input
				fis = new FileInputStream(new File(selectedFile.getAbsolutePath() + "/Page" + (i) + ".html"));
				InputStreamReader isr = new InputStreamReader(fis, "Windows-1252");

				in = new BufferedReader(isr);
				StringBuffer buffer = new StringBuffer();

				//Read in the lines of HTML
				int ch = in.read();
				while (ch > -1)
				{
					buffer.append((char) ch);
					ch = in.read();
				}

				// Create the file, and spit all read HTML into it (As UTF)
				File newFile = new File(files.get(i).getParentFile() + "/Converted/Page" + i
						+ ".html");
				FileOutputStream fos = new FileOutputStream(newFile.getAbsolutePath());
				out = new OutputStreamWriter(fos, "UTF-8");
				String converted = buffer.toString();
				out.write(converted);
				out.flush();
			}
			catch (Exception e)
			{
				System.out.println(e);
			}
		}
		
		//Try and catch closing all of our Streams
		try
		{
			in.close();
			fis.close();
			out.close();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//Inform User of success
		System.out.print("Converted HTML into UTF and placed into a Converted sub folder");
		
		//Return the converted foldr directory
		return convertedFolder;
	}

}
