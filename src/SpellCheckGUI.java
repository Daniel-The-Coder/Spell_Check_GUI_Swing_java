import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

public class SpellCheckGUI {
	
	//word - similarity factor class
	//similarity factor of correct word = (number of letters coinciding with misspelt word)/(length of word)
	public class WordSimilarity{
		String Word;
		float Similarity;
	}
	
	public static class errorCount{
		static int total = 0; //initially 0
		static int corrected = 0; //initially 0
		static int ignored = 0; //initially 0
	}
	
	public static class option{
		static String option1 = "";
		static String option2 = "";
		static String option3 = "";
		static String option4 = "";
		static String option5 = "";
		static String option6 = "";
		static String option7 = "";
		static String selected = ""; //Ignore by default
	}
	
	public static class file{
		static List<List<String>> inputFileList;
		static List<String> currentInputLineList;
		static List<String> linesList;
		static String currentNewLine = "";
		static int lineIndex = -1;
		static int wordIndex = -1;
		static PrintStream newFile;
	}
	
	public static class status{
		static boolean flag = true;
		static boolean isError=false;
	}
	
	private JFrame mainFrame;
	private JLabel errorsFound;
	private JLabel errorsCorrected;
	private JLabel errorsIgnored;
	
	/*
	public SpellCheckGUI() throws IOException{
		prepareGUI();
	}
	*/
	//function to create a list of wordLength objects and count all words for length of WordLength array
	Map readWordFile()throws FileNotFoundException {
		Map<Integer, List> WordsMap = new HashMap<Integer, List>();
		Scanner WordsFile = new Scanner(new File("words.txt"));
		//to create an array of WordLength type
		while (WordsFile.hasNext()) {
		    String word = WordsFile.nextLine();
			//to create a WordLength object
			int Length = word.length();
			if (WordsMap.containsKey(Length)){
				List wordsList = WordsMap.get(Length);
				wordsList.add(word);
				WordsMap.put(Length,  wordsList);
			}
			else{
				List<String> wordsList = new ArrayList<String>();
				wordsList.add(word);
				WordsMap.put(Length,  wordsList);
			}
		}
		WordsFile.close();
		return WordsMap;
	}
	
	List<List<String>> readInputFile (String fileName)throws FileNotFoundException {
		List<List<String>> inputFileList = new ArrayList<List<String>>();
		Scanner inputFile = new Scanner(new File(fileName));
		while (inputFile.hasNext()) {
			List<String> wordsInLine = new ArrayList<String>();
		    String line = inputFile.nextLine();
		    char[] chars = line.toCharArray();
		    String currentWord = "";
		    int flag = 0;
		    for (char c : chars) {
		        if(Character.isLetter(c)){
		        	if (flag==1){//if last char was a letter
			        	String d = Character.toString(c);
			        	currentWord+=d;
			        	flag=1;
		        	}
		        	else{
		        		if (currentWord!=""){
			        		wordsInLine.add(currentWord);
			        		currentWord= Character.toString(c);
			        		flag=1;
		        		}
		        		else{
		        			currentWord= Character.toString(c);
			        		flag=1;
		        		}
		        	}
		        }
		        else{
		        	if (flag==2){//if last char was NOT a letter
			        	String d = Character.toString(c);
			        	currentWord+=d;
			        	flag=2;
		        	}
		        	else{
		        		if (currentWord != ""){
			        		wordsInLine.add(currentWord);
			        		currentWord=Character.toString(c);;
			        		flag=2;
		        		}
		        		else{
		        			currentWord=Character.toString(c);;
			        		flag=2;
		        		}
		        	}
		        }
		    }
		    wordsInLine.add(currentWord);//to add 
		    inputFileList.add(wordsInLine);
		}		
		inputFile.close();
		return inputFileList;
	}
	
	// function to compare 2 words and return similarity factor (no of coinciding letters/length)
	float SimilarityFactor(String word1, String word2) throws FileNotFoundException{
		float L = (float)word1.length(); //it is equal to word2.length();
		float SimChar = (float)0;
		int i;
		for (i=0; i<L; i++){
			if (word1.substring(i,i+1).equals(word2.substring(i,i+1))){
				//didn't work with ==
				SimChar++;
			}
		}
		float SimFact = SimChar/L;
		return SimFact;
	}
	
	//function to return best match for the input word
	List WordMatch(String word) throws FileNotFoundException{
		Map<Integer, List> WordsMap=readWordFile();
		int Len = word.length();
		List wordsList = WordsMap.get(Len);//to get list of words of same length as input word
		String wordLowerCase = "";
		List<Boolean> wordCaseList = new ArrayList<Boolean>();
		for (int i=0; i<word.length(); i++){
			//to convert word to lower case for comparison
			wordLowerCase += word.substring(i, i+1).toLowerCase();
			//to create a boolean list to store case(upper/lower)
			if (word.substring(i, i+1).equals(word.substring(i, i+1).toUpperCase())){
				wordCaseList.add(true);
			}
			else{
				wordCaseList.add(false);
			}
		}
		boolean flag = false;
		for (int j=0; j<wordsList.size(); j++){
			if (wordsList.get(j).equals(wordLowerCase)){
				flag = true;
				List<String> oneWordList = new ArrayList<String>();
				oneWordList.add(word);
				return oneWordList;
			}
		}
				
		// if word is in the list, i.e. if it's correct
		if(flag == true){
			List<String> oneWordList = new ArrayList<String>();
			oneWordList.add(word);
			return oneWordList;
		}
		else{
			//list of words with same length as input word
			int count2 = wordsList.size();
			List<WordSimilarity> WordSimilarityList = new ArrayList<WordSimilarity>();
			for (int k=0; k<count2; k++){
				String WORD = (String) wordsList.get(k);
				float simFactor = SimilarityFactor(WORD, wordLowerCase); // DEFINE SimilarityFactor
				WordSimilarity SimObj = new WordSimilarity(); 
				SimObj.Word = WORD;
				SimObj.Similarity = simFactor;
				WordSimilarityList.add(SimObj);
				}
				
			// to create list of 5 most similar words similar word
			List<String> similarWordsList = new ArrayList<String>();
			
			if (WordSimilarityList.size()>=5) {
				for (int i=0; i<5; i++){
					int count3 = WordSimilarityList.size();
					WordSimilarity MostSimilarWordObj = WordSimilarityList.get(0);
					for (int m=0; m<count3; m++){
						if (WordSimilarityList.get(m).Similarity > MostSimilarWordObj.Similarity){
							MostSimilarWordObj = WordSimilarityList.get(m);
						}
					}
					String newWordLower = MostSimilarWordObj.Word;
					String newWord = "";
					for (int j=0; j<newWordLower.length(); j++){
						//if upper case
						if (wordCaseList.get(j)){
							//to convert letter to upper case and add
							newWord += newWordLower.substring(j,j+1).toUpperCase();
						}
						else{
							//to add letter in lower case
							newWord += newWordLower.substring(j,j+1);
						}
					}
					//to remove most similar word and find next most similar word
					WordSimilarityList.remove(MostSimilarWordObj);
					similarWordsList.add(newWord);
				}
			
			return similarWordsList;
			}
			else{ //to make it work even if less than 5 similar words found (still returns list with size 5)
				String NEWWORD="";
				int L = WordSimilarityList.size();
				for (int i=0; i<L; i++){
					int count3 = WordSimilarityList.size();
					WordSimilarity MostSimilarWordObj = WordSimilarityList.get(0);
					for (int m=0; m<count3; m++){
						if (WordSimilarityList.get(m).Similarity > MostSimilarWordObj.Similarity){
							MostSimilarWordObj = WordSimilarityList.get(m);
						}
					}
					String newWordLower = MostSimilarWordObj.Word;
					String newWord = "";
					for (int j=0; j<newWordLower.length(); j++){
						//if upper case
						if (wordCaseList.get(j)){
							//to convert letter to upper case and add
							newWord += newWordLower.substring(j,j+1).toUpperCase();
						}
						else{
							//to add letter in lower case
							newWord += newWordLower.substring(j,j+1);
						}
					}
					//to remove most similar word and find next most similar word
					WordSimilarityList.remove(MostSimilarWordObj);
					similarWordsList.add(newWord);
					NEWWORD=newWord;
				}
				for (int i=0; i<5-L; i++){
					similarWordsList.add(NEWWORD);
				}
			
			return similarWordsList;
			}
		}
	}
	
	private static ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = SpellCheckGUI.class.getResource(path);
		if (imgURL != null) {
		return new ImageIcon(imgURL, description);
		}
		else {            
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	
	private void prepareGUI() throws IOException{
	     mainFrame = new JFrame("Spell Check App");
	     mainFrame.setSize(3000, 2000);
	     mainFrame.setLayout(null);
	     
	     Font font1 = new Font(null, 0, 40);
	     /*
	     /////////// ADYA LABEL ///////////
	     JLabel AdyaLabel = new JLabel("<html><font size=500 color=red>Adya, I won the JAVA bet!!!</font></html>");
	     AdyaLabel.setLocation(1500,300);
	     AdyaLabel.setSize(1000,200);
	     mainFrame.add(AdyaLabel);
	     */
	     JLabel enterLabel = new JLabel("<html><font size=20>Enter path to text file you want to edit: </font></html>");
	     enterLabel.setLocation(200,100);
	     enterLabel.setSize(700,150);
	     mainFrame.add(enterLabel);
	     
	     JTextField pathField = new JTextField();
	     pathField.setLocation(900,135);
	     pathField.setSize(700,70);
	     pathField.setFont(font1);
	     mainFrame.add(pathField);
	     
	     /////////// ERROR COUNT LABELS ///////////
	     JLabel errorsFoundLabel = new JLabel("<html><font size=20>Errors Found:</font></html>");
	     errorsFoundLabel.setLocation(200,450);
	     errorsFoundLabel.setSize(600,150);
	     mainFrame.add(errorsFoundLabel);
	     
	     JLabel errorsCorrectedLabel = new JLabel("<html><font size=20>Errors corrected:</font></html>");
	     errorsCorrectedLabel.setLocation(200,550);
	     errorsCorrectedLabel.setSize(600,150);
	     mainFrame.add(errorsCorrectedLabel);
	     
	     JLabel errorsIgnoredLabel = new JLabel("<html><font size=20>Errors ignored:</font></html>");
	     errorsIgnoredLabel.setLocation(200,650);
	     errorsIgnoredLabel.setSize(600,150);
	     mainFrame.add(errorsIgnoredLabel);
	     
	     /////////// GO GREEN ARROW LABEL ///////////
	     JLabel goArrowLabel = new JLabel();
	     goArrowLabel.setLocation(500,235);
	     goArrowLabel.setSize(128,128);
	     mainFrame.add(goArrowLabel);
	     
	     /////////// GO BUTTON ///////////
	     BufferedImage buttonIcon = ImageIO.read(new File("arrow.png"));
	     JButton goButton = new JButton(new ImageIcon(buttonIcon));
	     goButton.setLocation(350,250);
	     goButton.setSize(100,100);
	     mainFrame.add(goButton);
	     
	     goButton.addActionListener(new ActionListener() {
	    	 public void actionPerformed(ActionEvent e) {
	    		 ImageIcon green_arrow = new ImageIcon("green_arrow_rotated.png");
	    		 goArrowLabel.setIcon(green_arrow);
	    		 try {
					file.inputFileList = readInputFile(pathField.getText()); //class variable
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    	 
	    		String newFileName = "C:\\Users\\Lord Daniel\\Desktop\\edited_file.txt";
	    		try {
					file.newFile = new PrintStream(newFileName);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    			
	    	 }
	     });
	     
	     /////////// CURRENT ERROR COUNT LABEL ///////////
	     JLabel errorLabel = new JLabel("<html><font size=20>Error number</font></html>");
	     errorLabel.setLocation(200,900);
	     errorLabel.setSize(600,150);
	     mainFrame.add(errorLabel);
	     
	     /////////// OPTION LABELS ///////////
	     JLabel option1Label = new JLabel("<html><font size=20 color=blue>Option 1</font></html>");
	     option1Label.setLocation(700,900);
	     option1Label.setSize(600,150);
	     mainFrame.add(option1Label);
	     
	     JLabel option2Label = new JLabel("<html><font size=20 color=blue>Option 2</font></html>");
	     option2Label.setLocation(700,1000);
	     option2Label.setSize(600,150);
	     mainFrame.add(option2Label);
	     
	     JLabel option3Label = new JLabel("<html><font size=20 color=blue>Option 3</font></html>");
	     option3Label.setLocation(700,1100);
	     option3Label.setSize(600,150);
	     mainFrame.add(option3Label);
	     
	     JLabel option4Label = new JLabel("<html><font size=20 color=blue>Option 4</font></html>");
	     option4Label.setLocation(700,1200);
	     option4Label.setSize(600,150);
	     mainFrame.add(option4Label);
	     
	     JLabel option5Label = new JLabel("<html><font size=20 color=blue>Option 5</font></html>");
	     option5Label.setLocation(700,1300);
	     option5Label.setSize(600,150);
	     mainFrame.add(option5Label);
	     
	     JLabel option6Label = new JLabel("<html><font size=20 color=blue>Option 6: Ignore error.</font></html>");
	     option6Label.setLocation(700,1400);
	     option6Label.setSize(600,150);
	     mainFrame.add(option6Label);
	     
	     JLabel option7Label = new JLabel("<html><font size=20 color=blue>Option 7: Enter word:</font></html>");
	     option7Label.setLocation(700,1500);
	     option7Label.setSize(600,150);
	     mainFrame.add(option7Label);
	     
	     JTextField wordField = new JTextField();
	     wordField.setLocation(1100,1550);
	     wordField.setSize(400,70);
	     wordField.setFont(font1);
	     mainFrame.add(wordField);
	     
	     /////////// CHECK LABELS ///////////
	     ImageIcon check = new ImageIcon("check1.png");
	     JLabel checkLabel1 = new JLabel();
	     checkLabel1.setLocation(1720,935);
	     checkLabel1.setSize(100,100);
	     mainFrame.add(checkLabel1);
	     
	     JLabel checkLabel2 = new JLabel();
	     checkLabel2.setLocation(1720,1035);
	     checkLabel2.setSize(100,100);
	     mainFrame.add(checkLabel2);
	     
	     JLabel checkLabel3 = new JLabel();
	     checkLabel3.setLocation(1720,1135);
	     checkLabel3.setSize(100,100);
	     mainFrame.add(checkLabel3);
	     
	     JLabel checkLabel4 = new JLabel();
	     checkLabel4.setLocation(1720,1235);
	     checkLabel4.setSize(100,100);
	     mainFrame.add(checkLabel4);
	     
	     JLabel checkLabel5 = new JLabel();
	     checkLabel5.setLocation(1720,1335);
	     checkLabel5.setSize(100,100);
	     mainFrame.add(checkLabel5);
	     
	     JLabel checkLabel6 = new JLabel();
	     checkLabel6.setLocation(1720,1435);
	     checkLabel6.setSize(100,100);
	     mainFrame.add(checkLabel6);
	     
	     JLabel checkLabel7 = new JLabel();
 	     checkLabel7.setLocation(1720,1535);
	     checkLabel7.setSize(100,100);
	     mainFrame.add(checkLabel7);
	     
	     /////////// CURRENT LINE LABEL ///////////
	     JLabel currentLineLabel = new JLabel("<html><font size=20 color=green>Current line</font></html>");
	     currentLineLabel.setLocation(1000,500);
	     currentLineLabel.setSize(1800,400);
	     mainFrame.add(currentLineLabel);
	     
	     /////////// FINISHED LABEL ///////////
	     JLabel finishedLabel = new JLabel();
	     finishedLabel.setLocation(2200,900);
	     finishedLabel.setSize(300,300);
	     mainFrame.add(finishedLabel);
	     
	     /////////// SELECT BUTTONS ///////////
	     JButton select1Button = new JButton("<html><font size=20>Select</font></html>");
	     select1Button.setLocation(1550, 950);
	     select1Button.setSize(150,70);
	     mainFrame.add(select1Button);
	     select1Button.addActionListener(new ActionListener(){
	    	 public void actionPerformed(ActionEvent e){
	    		option.selected = option.option1;
	    	    ImageIcon check = new ImageIcon("check1.png");
				checkLabel1.setIcon(check);
	     		checkLabel2.setIcon(null);
	     		checkLabel3.setIcon(null);
	     		checkLabel4.setIcon(null);
	     		checkLabel5.setIcon(null);
	     		checkLabel6.setIcon(null);
	     		checkLabel7.setIcon(null);
	     		
	     		errorCount.corrected+=1;
				errorsCorrectedLabel.setText("<html><font size=20>Errors corrected: "+Integer.toString(errorCount.corrected)+"</font></html>");
	    	 }
	     });
	     
	     JButton select2Button = new JButton("<html><font size=20>Select</font></html>");
	     select2Button.setLocation(1550, 1050);
	     select2Button.setSize(150,70);
	     mainFrame.add(select2Button);
	     select2Button.addActionListener(new ActionListener(){
	    	 public void actionPerformed(ActionEvent e){
	    		 option.selected = option.option2;
	    		 ImageIcon check = new ImageIcon("check1.png");
	    		 checkLabel1.setIcon(null);
		     	 checkLabel2.setIcon(check);
		     	 checkLabel3.setIcon(null);
		     	 checkLabel4.setIcon(null);
		     	 checkLabel5.setIcon(null);
		     	 checkLabel6.setIcon(null);
		     	 checkLabel7.setIcon(null);
		     	 
		     	errorCount.corrected+=1;
				errorsCorrectedLabel.setText("<html><font size=20>Errors corrected: "+Integer.toString(errorCount.corrected)+"</font></html>");
	    	 }
	     });
	     
	     JButton select3Button = new JButton("<html><font size=20>Select</font></html>");
	     select3Button.setLocation(1550, 1150);
	     select3Button.setSize(150,70);
	     mainFrame.add(select3Button);
	     select3Button.addActionListener(new ActionListener(){
	    	 public void actionPerformed(ActionEvent e){
	    		 option.selected = option.option3;
	    		 ImageIcon check = new ImageIcon("check1.png");
	    		 checkLabel1.setIcon(null);
		     	 checkLabel2.setIcon(null);
		     	 checkLabel3.setIcon(check);
		     	 checkLabel4.setIcon(null);
		     	 checkLabel5.setIcon(null);
		     	 checkLabel6.setIcon(null);
		     	 checkLabel7.setIcon(null);
		     	 
		     	errorCount.corrected+=1;
				errorsCorrectedLabel.setText("<html><font size=20>Errors corrected: "+Integer.toString(errorCount.corrected)+"</font></html>");
	    	 }
	     });
	     
	     JButton select4Button = new JButton("<html><font size=20>Select</font></html>");
	     select4Button.setLocation(1550, 1250);
	     select4Button.setSize(150,70);
	     mainFrame.add(select4Button);
	     select4Button.addActionListener(new ActionListener(){
	    	 public void actionPerformed(ActionEvent e){
	    		 option.selected = option.option4;
	    		 ImageIcon check = new ImageIcon("check1.png");
	    		 checkLabel1.setIcon(null);
		     	 checkLabel2.setIcon(null);
		     	 checkLabel3.setIcon(null);
		     	 checkLabel4.setIcon(check);
		     	 checkLabel5.setIcon(null);
		     	 checkLabel6.setIcon(null);
		     	 checkLabel7.setIcon(null);
		     	 
		     	errorCount.corrected+=1;
				errorsCorrectedLabel.setText("<html><font size=20>Errors corrected: "+Integer.toString(errorCount.corrected)+"</font></html>");
	    	 }
	     });
	     
	     JButton select5Button = new JButton("<html><font size=20>Select</font></html>");
	     select5Button.setLocation(1550, 1350);
	     select5Button.setSize(150,70);
	     mainFrame.add(select5Button);
	     select5Button.addActionListener(new ActionListener(){
	    	 public void actionPerformed(ActionEvent e){
	    		 option.selected = option.option5;
	    		 ImageIcon check = new ImageIcon("check1.png");
	    		 checkLabel1.setIcon(null);
		     	 checkLabel2.setIcon(null);
		     	 checkLabel3.setIcon(null);
		     	 checkLabel4.setIcon(null);
		     	 checkLabel5.setIcon(check);
		     	 checkLabel6.setIcon(null);
		     	 checkLabel7.setIcon(null);
		     	 
		     	errorCount.corrected+=1;
				errorsCorrectedLabel.setText("<html><font size=20>Errors corrected: "+Integer.toString(errorCount.corrected)+"</font></html>");
	    	 }
	     });
	     
	     JButton select6Button = new JButton("<html><font size=20>Select</font></html>");
	     select6Button.setLocation(1550, 1450);
	     select6Button.setSize(150,70);
	     mainFrame.add(select6Button);
	     select6Button.addActionListener(new ActionListener(){
	    	 public void actionPerformed(ActionEvent e){
	    		 //do nothing, option.selected is same word by default
	    		 ImageIcon check = new ImageIcon("check1.png");
	    		 checkLabel1.setIcon(null);
		     	 checkLabel2.setIcon(null);
		     	 checkLabel3.setIcon(null);
		     	 checkLabel4.setIcon(null);
		     	 checkLabel5.setIcon(null);
		     	 checkLabel6.setIcon(check);
		     	 checkLabel7.setIcon(null);
		     	 
		     	 errorCount.ignored+=1;
				 errorsIgnoredLabel.setText("<html><font size=20>Errors ignored: "+Integer.toString(errorCount.ignored)+"</font></html>");
	    	 }
	     });
	     
	     JButton select7Button = new JButton("<html><font size=20>Select</font></html>");
	     select7Button.setLocation(1550, 1550);
	     select7Button.setSize(150,70);
	     mainFrame.add(select7Button);
	     select7Button.addActionListener(new ActionListener(){
	    	 public void actionPerformed(ActionEvent e){
	    		 option.selected = wordField.getText();
	    		 ImageIcon check = new ImageIcon("check1.png");
	    		 checkLabel1.setIcon(null);
		     	 checkLabel2.setIcon(null);
		     	 checkLabel3.setIcon(null);
		     	 checkLabel4.setIcon(null);
		     	 checkLabel5.setIcon(null);
		     	 checkLabel6.setIcon(null);
		     	 checkLabel7.setIcon(check);
		     	 
		     	errorCount.corrected+=1;
				errorsCorrectedLabel.setText("<html><font size=20>Errors corrected: "+Integer.toString(errorCount.corrected)+"</font></html>");
	    	 }
	     });
	     
	     JButton nextButton = new JButton("<html><font size=20>Next</font></html>");
	     nextButton.setLocation(1400,1700);
	     nextButton.setSize(170,90);
	     mainFrame.add(nextButton);
	     nextButton.addActionListener(new ActionListener(){
	    	 public void actionPerformed(ActionEvent e){
 	 			
	    		 checkLabel1.setIcon(null);
		     	 checkLabel2.setIcon(null);
		     	 checkLabel3.setIcon(null);
		     	 checkLabel4.setIcon(null);
		     	 checkLabel5.setIcon(null);
		     	 checkLabel6.setIcon(null);
		     	 checkLabel7.setIcon(null);
		     	 
	    		 file.currentNewLine += option.selected; //to add selected word to line

	    		 while (status.isError==false){//to keep adding words to new file till error found
		    		 
		    		 if (file.lineIndex==-1){
		    			 file.lineIndex=0;
		    			 file.wordIndex=0;
		    		 }
		    		 else{
			    		 if (file.wordIndex < (file.inputFileList.get(file.lineIndex).size() -1)){
			    			 file.wordIndex+=1;
			    		 }
			    		 else{//going to next line
			    			 if (file.lineIndex < (file.inputFileList.size() -1)){
			    	 			file.lineIndex+=1;
			    	 			file.wordIndex=0;
			    	 			file.newFile.println(file.currentNewLine);
			    	 			System.out.println(file.currentNewLine);//
			    	 			file.currentNewLine="";
			    	 			
			    			 }
			    			 else{
			    				 status.flag=false;//end of file
			    			 }
			    		 }
		    		 }
		    		 
		    		 //to display current line
		    		 String displayCurrentLine = "<html><font size=20 color=green>Current line:</font><br><br><font size=20>";
		    		 List lineList = file.inputFileList.get(file.lineIndex);
		    		 int S = lineList.size();
		    		 int T = 0;
		    		 for (int i=0; i<S; i++){
		    			 if (T<30){
		    				 T+=1;
			    			 String word = (String) lineList.get(i);
			    			 if (word.equals((String) file.inputFileList.get(file.lineIndex).get(file.wordIndex))) {
			    				 displayCurrentLine += "<font color=red>"+word+"</font>";
			    			 }
			    			 else{
			    				 displayCurrentLine += word;
			    			 }
		    			 }
		    			 else{
		    				 T=0;
		    				 String word = (String) lineList.get(i);
			    			 if (word.equals((String) file.inputFileList.get(file.lineIndex).get(file.wordIndex))) {
			    				 displayCurrentLine += "<font color=red>"+word+"</font><br>";
			    			 }
			    			 else{
			    				 displayCurrentLine += word+"<br>";
			    			 }
		    			 }
		    		 }
		    		 displayCurrentLine += "</font></html>";
		    		 currentLineLabel.setText(displayCurrentLine);

		    		 if (status.flag == true) { //if not end
		    			 String currentWord = (String) file.inputFileList.get(file.lineIndex).get(file.wordIndex);{
		    			 System.out.println("currentWord: "+currentWord);
		    			 if (Character.isLetter(currentWord.substring(0,1).charAt(0))){ //if word and not special characters
		    				 List similarWords;
							 try {
								 similarWords = WordMatch(currentWord);
			    				 if (similarWords.size()==1){
			    					 file.currentNewLine += currentWord;
			    				   	 status.isError=false;
			    				 }
			    				 else{
			    					 errorCount.total+=1;
			    					 errorsFoundLabel.setText("<html><font size=20>Errors found: "+Integer.toString(errorCount.total)+"</font></html>");
			    					 errorLabel.setText("<html><font size=20>Error "+Integer.toString(errorCount.total)+": </font><font color=red size=20>"+currentWord+"</font></html>");
			    					 
			    					 option.option1=(String) similarWords.get(0);
			    					 option.option2=(String) similarWords.get(1);
			    					 option.option3=(String) similarWords.get(2);
			    					 option.option4=(String) similarWords.get(3);
			    					 option.option5=(String) similarWords.get(4);
			    					 
			    					 option1Label.setText("<html><font size=20 color=blue>Option 1 </font><font size=25>"+option.option1+"</font></html>");
			    					 option2Label.setText("<html><font size=20 color=blue>Option 2 </font><font size=25>"+option.option2+"</font></html>");
			    					 option3Label.setText("<html><font size=20 color=blue>Option 3 </font><font size=25>"+option.option3+"</font></html>");
			    					 option4Label.setText("<html><font size=20 color=blue>Option 4 </font><font size=25>"+option.option4+"</font></html>");
			    					 option5Label.setText("<html><font size=20 color=blue>Option 5 </font><font size=25>"+option.option5+"</font></html>");
			    					 
			    					 option.option6=currentWord;
			    					 option.option7=wordField.getText();
			    					 status.isError=true;
				    				 }
								}
								catch (FileNotFoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
			    			 }
			    			 else{//if space/special characters
			    				  file.currentNewLine+=currentWord;
			    				  status.isError=false;
			    			 }
			    		 }
		    	     }
		    	     else{//end of file
		    	    	 System.out.println("THE END");
		    	    	 file.newFile.println(file.currentNewLine);
		    	    	 file.newFile.close();
		    	    	 finishedLabel.setText("<html><font size=40 color=purple>All errors rectified<br>and<br>file saved.</font></html>");
		    	    	 status.isError=true;
		    	     }
		    	 }
	    		 status.isError=false;
	    	 }
	     });
	  
	     mainFrame.setVisible(true);  
	}
 
	public static void main(String[] args) throws IOException{
		SpellCheckGUI  spellcheckgui = new SpellCheckGUI();      
	    spellcheckgui.prepareGUI();
	    }
	}

