import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.*;

public class SpreadSheets {
	private static ArrayList<ArrayList<String>> filecontent = new ArrayList<ArrayList<String>>();
    private static String[] headers = {"BeginDate","AB","BC","CD","DE","EF","FG","GH","HI","IJ","JK","EndDate"};
	private static String SpreadSheetName = "TestResultsN5";
    
	static void ReadFile( String filename ) throws IOException, FileNotFoundException
	{
		File file = new File( "./TestsResults/" + filename );
		
		try
		{
			if( file.exists() )
			{
				BufferedReader in = new BufferedReader( new FileReader( file.getAbsoluteFile() ) );
				try{				
					String s;
					int number=0;
					while ( (s= in.readLine()) != null)
					{
						filecontent.add( new ArrayList<String>() );
						for(String word : s.split("\t")){
							filecontent.get(number).add(word);
						}
						number++;
					}
					}finally{
						in.close();
					}
			} else throw new FileNotFoundException(file.getName());
		
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		
		return; 
	}

    public static void main(String[] args) throws IOException, ServiceException {
        //System.out.println("Hello, World");
//===========================================Creating new SpreadSheet==========================================        
//credential for GoogleDrive   
        File p12 = new File("./key.p12");
        HttpTransport httpTransportDRIVE = new NetHttpTransport();
        JacksonFactory jsonFactoryDRIVE = new JacksonFactory();
        String[] SCOPESArrayDRIVE ={"https://www.googleapis.com/auth/drive","https://spreadsheets.google.com/feeds","https://docs.google.com/feeds"};
        final List SCOPESDRIVE = Arrays.asList(SCOPESArrayDRIVE);
        GoogleCredential credentialDRIVE=null;
		try {
			credentialDRIVE = new GoogleCredential.Builder()
					.setTransport(httpTransportDRIVE)
					.setJsonFactory(jsonFactoryDRIVE)
			        .setServiceAccountId("263848504358-n311ju0dbbn6em683pu2ftj6e1qa1a@developer.gserviceaccount.com")
			        .setServiceAccountScopes(SCOPESDRIVE)
			        .setServiceAccountPrivateKeyFromP12File(p12)
			        .build();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

       Drive serviceDRIVE = new Drive.Builder(httpTransportDRIVE, jsonFactoryDRIVE, credentialDRIVE).setApplicationName("qwerty").build();    

//Creating a new SpreadSheet
       com.google.api.services.drive.model.File  fileDRIVE = new com.google.api.services.drive.model.File();
       fileDRIVE.setTitle(SpreadSheetName);
       fileDRIVE.setDescription("asd");
       fileDRIVE.setMimeType("application/vnd.google-apps.spreadsheet");
       serviceDRIVE.files().insert(fileDRIVE).execute();
 
// Reading files from Drive
       FileList result = serviceDRIVE.files().list().setMaxResults(10).execute();
       List<com.google.api.services.drive.model.File> files = result.getItems();
       
       String id = files.get(0).getId(); //id for SharingPermission
       JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {

           @Override
           public void onSuccess(Permission permission, HttpHeaders responseHeaders) {
             System.out.println("Success!");
           }

           @Override
           public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
             System.out.println("Error Message: " + e.getMessage());
           }
         };

       BatchRequest batch = serviceDRIVE.batch();
       Permission newPermissionDRIVE = new Permission();

       newPermissionDRIVE.setValue("practice2015@gmail.com");
       newPermissionDRIVE.setType("user");
       newPermissionDRIVE.setRole("writer");
       //System.out.println("Shared file id: "+ id);
       serviceDRIVE.permissions().insert(id, newPermissionDRIVE).queue(batch, callback);
       
       batch.execute();
       
       System.out.println("A new SpreadSheet " + SpreadSheetName + " has been created.\n");
//================================================Uploading files to our SpreadSheet====================================
//First part(Authorisation)----Credential for Google SpreadSheets
        //File p12 = new File("./key.p12");
        URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        String[] SCOPESArray = {"https://spreadsheets.google.com/feeds", "https://spreadsheets.google.com/feeds/spreadsheets/private/full", "https://docs.google.com/feeds"};
        final List SCOPES = Arrays.asList(SCOPESArray);
        GoogleCredential credential = null;
		try {
			credential = new GoogleCredential.Builder()
			        .setTransport(httpTransport)
			        .setJsonFactory(jsonFactory)
			        .setServiceAccountId("263848504358-n311ju0dbbn6em683pu2ftj6e1qa1a@developer.gserviceaccount.com")
			        .setServiceAccountScopes(SCOPES)
			        .setServiceAccountPrivateKeyFromP12File(p12)
			        .build();
		} catch (GeneralSecurityException e1) {
			e1.printStackTrace();
		}
	
        SpreadsheetService service = new SpreadsheetService("Test");

        service.setOAuth2Credentials(credential);
        SpreadsheetFeed feed = null;
		try {
			feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		
        List<SpreadsheetEntry> spreadsheets = feed.getEntries();   
        
        if (spreadsheets.size() == 0) {
            System.out.println("No spreadsheets found.");
        }
//Second part(Reading and uploading files)-----------------------------------------------------------------------------------------------------------------------------
         SpreadsheetEntry spreadsheet = null;
        for (int i = 0; i < spreadsheets.size(); i++) {
            if (spreadsheets.get(i).getTitle().getPlainText().startsWith(SpreadSheetName)) {
                spreadsheet = spreadsheets.get(i);
                System.out.println("Name of editing spreadsheet: " + spreadsheets.get(i).getTitle().getPlainText());
                System.out.println("ID of SpreadSheet: " + i);
                
                //Reading files---------------------
                File f = new File("./TestsResults"); 
                String[] names = f.list();
                      
                for( int j = 0 ; j< names.length ; j++ )
                {
                ReadFile( names[j] );
                
                //Rename first if j==0
                if( j== 0 )
                {
                	WorksheetFeed worksheetFeed = service.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
                	List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
                	WorksheetEntry worksheet = worksheets.get(0);
                	
                	worksheet.setTitle( new PlainTextConstruct(names[j]));
                	worksheet.setColCount(filecontent.get(1).size());
                	worksheet.setRowCount(1);
                	worksheet.update();
                }
                else
                {
                //Creating new WorkSheet-------------
                WorksheetEntry worksheet = new WorksheetEntry();
                worksheet.setTitle(new PlainTextConstruct(names[j]));
                worksheet.setColCount(filecontent.get(1).size());
                worksheet.setRowCount(1);
                
                URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
                try {
					service.insert(worksheetFeedUrl, worksheet);
				} catch (ServiceException e) {
					e.printStackTrace();
				}
                }
                //Copying to the WorkSheet-------------
                WorksheetFeed worksheetFeed = service.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
                List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
                WorksheetEntry worksheet = worksheets.get(j);
                //worksheet.get
               
                //Preparing headers
                URL cellFeedUrl = worksheet.getCellFeedUrl();
                
                CellQuery query = new CellQuery(cellFeedUrl);
                query.setReturnEmpty(true);
                CellFeed cellFeed = service.query(query, CellFeed.class);
                
                for ( int n = 0; n< headers.length; n++ ) {
                	CellEntry cell = cellFeed.getEntries().get(n);
                	cell.changeInputValueLocal( headers[n] );
                    cell.update();
                }
                
                System.out.println("\nStarting to upload " + names[j]);
                //Inserting rows
                URL listFeedUrl = worksheet.getListFeedUrl();
                ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
          
                for( int k = 1 ; k< filecontent.size(); k++ )
                {  	
                ListEntry row = new ListEntry();
                for( int n = 0; n<12; n++ )
                {
                	row.getCustomElements().setValueLocal( headers[n], filecontent.get(k).get(n));
                }
                row = service.insert(listFeedUrl, row);
                }
                //-----------------------------------
                
                System.out.print("--FILE READY: "+ names[j] + "\n");
               
                //Deleting filecontent
                for( ArrayList<String> sentence : filecontent )
                {
                	sentence.clear();
                }
                filecontent.clear();
                
                }
                System.out.println("it's done!");  
                System.out.println("To make plots use APP SCRIPT(./AppScript.txt) =)");
            }
        }
    }
}

