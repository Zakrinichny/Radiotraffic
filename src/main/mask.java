package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JProgressBar;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

public class mask {
	
	private static Calendar cl = Calendar.getInstance();
	private static JProgressBar progB ;
	
	public mask(JProgressBar progressBar) throws Exception {
		progB = progressBar;	
		createCatalog(cl);
		StringBuilder[] feedbackCheck = 
				new StringBuilder[cl.getActualMaximum(Calendar.DAY_OF_MONTH)];
		for (int i=0;i<feedbackCheck.length;i++) { feedbackCheck[i] = new StringBuilder(); }
		createExcel(cl,feedbackCheck);
		createWord(cl,feedbackCheck);		
	}

	
	public static void createWord(Calendar cl,StringBuilder[] feedbackCheck) throws Exception {
		String alphabet = "КАБВГДЕЖЗІИЙЛМНОПРСТУФХЦЧШЩЬЮЯ ";      //№1 				
		for (int i=1;i<=cl.getActualMaximum(Calendar.DAY_OF_MONTH);i++ ) {	
			StringBuilder excelText = new StringBuilder("");
			String month = determineMonth(cl.get(Calendar.MONTH));
			StringBuilder date = createDate(cl,i);
			StringBuilder pathAndNameExcelFile = new StringBuilder("Радіограми "+month+"\\Розрахунок на ");
			File excelFile = new File(pathAndNameExcelFile.toString()+date.toString()+".xlsx");
			loadFromExcel(excelText, excelFile);
			processingText(excelText);
			int radiogramsNumber = determineTheNumberOfRadiograms(excelText, alphabet);
			StringBuilder radiograms = new StringBuilder();
			createRadiograms(radiogramsNumber,radiograms, alphabet, excelText, i,feedbackCheck);
			writeToWord(month,date,radiograms);	
			defineTheProgress(progB, 1);
		}
	}

	public static void createCatalog(Calendar cl) {
		String nameCatalog = determineMonth(cl.get(Calendar.MONTH));
		File catalog = new File("Радіограми "+nameCatalog);
		catalog.mkdir();
	}
	
	public static void createExcel(Calendar cl, StringBuilder[] feedbackCheck) 
	throws Exception {
		final int NUMBER_OF_CALLS = determineTheNumberOfCall();		
		final int DAYS_IN_MOUNTH = cl.getActualMaximum(Calendar.DAY_OF_MONTH);
		final int NUMBER_OF_ROWS = 100;
		StringBuilder[] calls = new StringBuilder[NUMBER_OF_CALLS];		
		StringBuilder[][] time = new StringBuilder[NUMBER_OF_CALLS][DAYS_IN_MOUNTH];
		initializationCalls(calls);
		try {
			fillTime(time, calls,DAYS_IN_MOUNTH);
		}
		catch(Exception e) {
			System.out.println(e);
		}
		
		String month = determineMonth(cl.get(Calendar.MONTH));
		for (int i=1;i<=cl.getActualMaximum(Calendar.DAY_OF_MONTH);i++) {	
			determineRadiogramWithFeedback(time,calls,i);		
			XSSFWorkbook workbook = new XSSFWorkbook();	
			XSSFSheet sheet = workbook.createSheet("лист 1");
			sheet.setColumnWidth(1,4000);
			XSSFCellStyle styleDate = createCellStyle(workbook,"Courier New",22,"center",false);
			createRegion(sheet,4,i+" "+month,styleDate);			
			XSSFCellStyle style = createCellStyle(workbook,"Calibri",16, "center",false);	
			XSSFCellStyle styleCall = createCellStyle(workbook,"Calibri",16, "left",false);
			XSSFCellStyle styleFeedback = createCellStyle(workbook,"Calibri",16, "center",true);	
			XSSFCellStyle styleCallFeedback = createCellStyle(workbook,"Calibri",16, "left",true);
			createRegion(sheet,5,"Р/М 4",style);			
			StringBuilder[] liner = new StringBuilder[NUMBER_OF_ROWS];	
			StringBuilder[] address = new StringBuilder[2];	
			fillAddresses(NUMBER_OF_ROWS, address, liner, i, cl);
			createRegion(sheet,6,"="+address[0].toString()+"=",style);
			File file = new File("src\\Row and Column numbers.txt");
			creationOfFilesForDeterminingTheNumberingOfRadiograms(liner, file);			
			final int MIN_TIME = 0;
			final int MAX_TIME = 24;
			final int LUNCH_TIME = 12;
			int numberOfRadiogramsBeforeLunch = numberOfRadiograms(time,i,MIN_TIME,LUNCH_TIME);
			int numberOfRadiogramsAfterLunch = numberOfRadiograms(time,i,LUNCH_TIME,MAX_TIME);
			for(int j=0;j<numberOfRadiogramsBeforeLunch;j++) {
				XSSFRow rows = sheet.createRow(7+j);			
				fillRow(rows,style,styleCall,styleFeedback,styleCallFeedback,i,calls,time,
						numberOfRadiogramsBeforeLunch,MIN_TIME,LUNCH_TIME,j,liner,file,
						feedbackCheck);					
			}
			createRegion(sheet,7+numberOfRadiogramsBeforeLunch,"="+address[1].toString()+"=",style);	
			for(int j=0;j<numberOfRadiogramsAfterLunch;j++) {
				XSSFRow rows = sheet.createRow(7+numberOfRadiogramsBeforeLunch+1+j);
				fillRow(rows,style,styleCall,styleFeedback,styleCallFeedback,i,calls,time,
						numberOfRadiogramsAfterLunch,LUNCH_TIME,MAX_TIME,j,liner,file,
						feedbackCheck);				
			}		
			file.delete();
			StringBuilder date = createDate(cl,i);
			try {
				FileOutputStream outExcel = new FileOutputStream(new File(
						"Радіограми "+month+"\\Розрахунок на "+date.toString()+".xlsx"));
				workbook.write(outExcel);
				outExcel.close();
				workbook.close();
			}
			catch(Exception e){
				System.out.println(e);
			}
			defineTheProgress(progB, 2);
		}		
	}
	//--------------------------------------------------------------------------------
	public static void defineTheProgress(JProgressBar progressBar, int progress) {
		int value = progressBar.getValue();
		int maximum = progressBar.getMaximum();
		int days = cl.getActualMaximum(Calendar.DAY_OF_MONTH);
		value+=progress;
		if (value==days+(days*2)) { value = 100; }
		progressBar.setValue(value);
	}
	//--------------------------------------------------------------------------------
	public static void determineRadiogramWithFeedback(StringBuilder[][] time, 
			StringBuilder[] calls, int day) {
		
		for (int i=0;i<time.length;i++) {
			if (!time[i][day-1].toString().equals("-")) {
				StringBuilder reverse = new StringBuilder(time[i][day-1].toString());			
				reverse.reverse();
				if (reverse.charAt(0)=='*') {
					calls[i].append("*");
					int index = time[i][day-1].indexOf("*");
					time[i][day-1].deleteCharAt(index);
				}
			}
		}
	}
	
	public static void creationOfFilesForDeterminingTheNumberingOfRadiograms(
			StringBuilder[] liner, File file) {
		Boolean linerA = isLinerA(liner);
		int mRows = calcMRows(liner,linerA);
		int[] mRowsValues = new int[mRows];
		fillmRowsValues(mRowsValues,liner,linerA);
		int[] columnValues = {0,1,2,3,4,5,6,7,8,9};
		try(PrintWriter write = new PrintWriter(file)) {
			writeArray(write, mRowsValues);
			write.print("#"); 
			writeArray(write, columnValues);
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}		
	}
	
	public static void writeArray(PrintWriter write, int[] array) {
		write.print("/");
		for (int i=0;i<array.length;i++) { write.print(array[i] + "/"); }
	}
	
	public static void fillAddresses(int NUMBER_OF_ROWS, StringBuilder[] address, 
			StringBuilder[] liner, int day, Calendar cl) {
		StringBuilder liners = loadFromFile(new File("src\\liners.txt"));
		StringBuilder addressestxt = loadFromFile(new File("src\\Адреса.txt"));
		StringBuilder[] addresses = new StringBuilder[NUMBER_OF_ROWS];
		identifyLiner(liners,liner,day,cl);
		int[] numberOfRow = identifyNumberOfRow(liner);		
		wrappingTextIntoAnArray(addressestxt, addresses);
		for (int i=0;i<address.length;i++) { address[i] = new StringBuilder(); }		
		for (int i=0;i<address.length;i++) {
			address[i].append(addresses[numberOfRow[i]].toString());
		}
		for (int i=0;i<address.length;i++) { address[i].deleteCharAt(3); }   //#3
	}
	
	public static int[] identifyNumberOfRow(StringBuilder[] liner) {
		int[] numberOfRow = {-1,-1};
		for (int i=0;i<liner.length;i++) {
			int one = Integer.parseInt(liner[i].substring(2,3));
			if (one==1) {
				for (int j=i;j<=i+9;j++) {
					int four = Integer.parseInt(liner[j].substring(1,2));
					if (four==4) { 
						numberOfRow[0] = j; 
						break;
					}
				}
				break;
			}
		}	
		if (numberOfRow[0]<50) { numberOfRow[1] = numberOfRow[0]+50; }
		else { numberOfRow[1] = numberOfRow[0]-50; }
		return numberOfRow;
	}
	
	public static void identifyLiner(StringBuilder liners, StringBuilder[] liner,
			int day, Calendar cl) {	
		
		StringBuilder data = loadFromFile(new File ("src\\month.txt"));
		int numberOfMonth = cl.get(Calendar.MONTH);
		int numberOfLiner = 0;
		int countRow=0, countColumn=0;
//		в файле month.txt после последнего столбика также должны стоять символы \t,
//		иначе алгоритм неправильно будет работать!!
		for (int i=0;i<data.length();i++) {		
			if (data.charAt(i)=='\n') { 
				countRow++; 
				countColumn = 0;
			}
			if (data.charAt(i)=='\t') {  countColumn++;  }
			if (countRow==day-1 && countColumn==numberOfMonth &&
					data.charAt(i)!='\n' && data.charAt(i)!='\t') {
				numberOfLiner = Integer.parseInt(data.substring(i,i+2));
				i++;
			}	
		}
		wrappingTextIntoAnArray(liners, liner);		
		for (int i=0;i<liner.length;i++) {
			liner[i].delete(0,(numberOfLiner-1)*3);
			liner[i].delete(3,liner[i].length());	
		}
	}
	
	public static void wrappingTextIntoAnArray(StringBuilder text, StringBuilder[] array) {
		for (int i=0;i<array.length;i++) { array[i] = new StringBuilder(""); }
		int index = 0;
		int countRow = 0;
		for (int i=0;i<text.length();i++) {
			if (text.charAt(i)=='\n') {
				array[countRow].append(text.substring(index,i));
				countRow++;
				index = i+1;
			}
		}
		
	}
	
	public static XSSFCellStyle createCellStyle(XSSFWorkbook workbook,String fontName, 
			int sizeText, String alignment, boolean feedback) {
		XSSFCellStyle style = workbook.createCellStyle();
		if (alignment.equals("center")) { style.setAlignment(HorizontalAlignment.CENTER); }
		if (alignment.equals("left")) { style.setAlignment(HorizontalAlignment.LEFT); }	
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);	
		if (feedback) {
			style.setFillPattern(FillPatternType.LEAST_DOTS);
			style.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		}
		XSSFFont font = workbook.createFont();
		font.setFontName(fontName);
		font.setBold(true);
		font.setFontHeight(sizeText);
		style.setFont(font);
		return style;
	}
	
	public static void createRegion(XSSFSheet sheet,int numberOfRow, String text, 
			XSSFCellStyle style) {
		XSSFRow regionRow = sheet.createRow(numberOfRow);
		Cell regionCell = regionRow.createCell(1);
		Cell regionCell2 = regionRow.createCell(2);
		Cell regionCell3 = regionRow.createCell(3);
		regionCell.setCellValue(text);
		regionCell.setCellStyle(style);
		regionCell2.setCellStyle(style);
		regionCell3.setCellStyle(style);
		CellRangeAddress region = new CellRangeAddress(
				numberOfRow,numberOfRow,1,3);
		sheet.addMergedRegion(region);
	}
	
	public static void fillRow(XSSFRow rows,XSSFCellStyle style,XSSFCellStyle styleCall, 
			XSSFCellStyle styleFeedback,XSSFCellStyle styleCallFeedback, int day, 
			StringBuilder[] calls, StringBuilder[][] time, int numberOfRadiograms, 
			int begin, int end, int index, StringBuilder[] liner, File file,
			StringBuilder[] feedbackCheck) {
		Cell cellCall = rows.createCell(1);
		Cell cellNumber = rows.createCell(2);
		Cell cellTime = rows.createCell(3);
		Cell cellRowNumberFromWhichTheRadiogramNumberIsTaken = rows.createCell(4);
		int[] currentTimeArray = new int[numberOfRadiograms];
		for (int i=0,j=0;i<time.length;i++) {
			if (!time[i][day-1].toString().equals("-")) {
				int currentTime = Integer.parseInt(time[i][day-1].toString());
				if (currentTime>begin && currentTime<end) {
					currentTimeArray[j] = currentTime;
					j++;
				}					
			}	
		}
		sortArray(currentTimeArray);
		for (int j=0;j<time.length;j++) {
			if (!time[j][day-1].toString().equals("-")) {
				int thisTime = Integer.parseInt(time[j][day-1].toString());
				if (thisTime==currentTimeArray[index]) {					
					if (calls[j].charAt(calls[j].length()-1)=='*') {
						int indexStar = calls[j].indexOf("*");
						calls[j].deleteCharAt(indexStar);
						feedbackCheck[day-1].append(calls[j]);
						cellCall.setCellStyle(styleCallFeedback);				
						cellTime.setCellStyle(styleFeedback);
						cellNumber.setCellStyle(styleFeedback);
					}
					else {			
						cellCall.setCellStyle(styleCall);											
						cellTime.setCellStyle(style);
						cellNumber.setCellStyle(style);
					}
					cellCall.setCellValue(calls[j].toString());				
					cellTime.setCellValue(time[j][day-1].toString()+"00");				
				}					
			}
		}			
		fillNumberOfRadiogram(liner, cellNumber,cellRowNumberFromWhichTheRadiogramNumberIsTaken,
				file, begin);
	}
	
	public static void fillNumberOfRadiogram(StringBuilder[] liner, Cell cellNumber,
			Cell cellRowNumberFromWhichTheRadiogramNumberIsTaken,File file,int begin) {
		StringBuilder tabletxt = loadFromFile(new File ("src\\table.txt"));
		final int NUMBER_OF_ELEMENTS_IN_LINE= 10;
		StringBuilder[][] table = new StringBuilder[liner.length][NUMBER_OF_ELEMENTS_IN_LINE];
		fillTable(tabletxt,table);
		StringBuilder RowAndColumnNumbers = loadFromFile(file);
		Boolean lattice = false;
		int numberOfRows = determineTheNumberOfElements(RowAndColumnNumbers,lattice);
		int[] rows = new int[numberOfRows];
		fillArray(rows,RowAndColumnNumbers, lattice);
		lattice = true;
		int numberOfColumns = determineTheNumberOfElements(RowAndColumnNumbers,lattice);
		int[] columns = new int[numberOfColumns];
		fillArray(columns,RowAndColumnNumbers, lattice);
		int indexRow = (int)(Math.random()*numberOfRows);
		int indexColumn = (int)(Math.random()*numberOfColumns);
		boolean linerA = isLinerA(liner);
		if (begin==0) {
			cellNumber.setCellValue(table[rows[indexRow]][columns[indexColumn]].toString());
			cellRowNumberFromWhichTheRadiogramNumberIsTaken.setCellValue(rows[indexRow]);
		}
		else {
			if (linerA) {
				cellNumber.setCellValue(table[rows[indexRow]+50][columns[indexColumn]].toString());
				cellRowNumberFromWhichTheRadiogramNumberIsTaken.setCellValue(rows[indexRow]+50);
			}
			else {  
				cellNumber.setCellValue(table[rows[indexRow]-50][columns[indexColumn]].toString());
				cellRowNumberFromWhichTheRadiogramNumberIsTaken.setCellValue(rows[indexRow]-50);
			}	
		}	
		saveArrayToFile(rows,columns,file,indexRow,indexColumn);	
	}
	
	public static void saveArrayToFile(int[] firstArray, int[] secondArray, File file, 
			int firstIndex,int secondIndex) {
		try (PrintWriter write = new PrintWriter (file)) {
			writeArrayWithoutElement(write,firstArray, firstIndex);
			write.print("#");
			writeArrayWithoutElement(write,secondArray, secondIndex);
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
	}
	
	public static void writeArrayWithoutElement (PrintWriter write, int[] array, int index) { 
		write.print("/");
		for (int i=0;i<array.length;i++) { 
			if (i!=index) { write.print(array[i] + "/");  }
			}
	}

 	public static void fillArray(int[] array, StringBuilder text, boolean lattice) {
		int indexLattice = text.indexOf("#");
		int begin=0, end=indexLattice-1;
		if (lattice) {
				begin=end+1;
				end=text.lastIndexOf("/"); 
			}	
		int index=0;
		for (int i=begin;i<end;i++) {
			if (text.charAt(i)=='/') {
				int k=i+1;
				while(text.charAt(k)!='/') { k++; }
					array[index] = Integer.parseInt(text.substring(i+1,k));
					index++;
					i=k-1; 				
			}	
		}
	}
	
	public static int determineTheNumberOfElements(StringBuilder text, boolean lattice) {
		int numberOfElements = 0;
		int indexLattice = text.indexOf("#");
		int begin=1, end=indexLattice;
		if (lattice) {
				begin=end+2;
				end=text.length();
			}
		for (int i=begin;i<end;i++) { if (text.charAt(i)=='/') { numberOfElements++; } }
		return numberOfElements;
	}
	
	public static void fillmRowsValues(int[] mRowsValues, StringBuilder[] liner,boolean linerA) {
		int begin=0, end=50, index=0;        //#2
		if(!linerA) {
			begin+=50;
			end+=50;
		}
		for (int i=begin;i<end;i++ ) {
			if (liner[i].charAt(0)=='m') {
				mRowsValues[index] = i;
				index++;
			}
		}
		
	}
	
	public static boolean isLinerA(StringBuilder[] liner) {
		boolean rezult = false;
		for (int i=0;i<liner.length;i++) {
			if (liner[i].charAt(2)=='1') {
				if (i<50) { rezult = true; }
			}
		}
		return rezult;
	}
	
	public static int calcMRows(StringBuilder[] liner, boolean linerA) {
		int number = 0;
		int begin=0, end=50;        //#2
		if(!linerA) {
			begin+=50;
			end+=50;
		}
		for (int i=begin;i<end;i++ ) {
			if (liner[i].charAt(0)=='m') {
				number = number+5;
				i = i+4;			
			}
		}	
		return number;
	}
	
	public static void fillTable(StringBuilder tabletxt, StringBuilder[][] table) {
		for (int i=0;i<table.length;i++) {
			for (int j=0;j<table[i].length;j++) {  table[i][j] = new StringBuilder();  }
		}
		int i=0,j=0,k=0;
		for (int index=0;index<tabletxt.length();index++) {
			if (tabletxt.charAt(index)=='\t') {
				table[i][j].append(tabletxt.substring(k,index));
				k = index+1;
				j++;
				index++;
			}
			if (tabletxt.charAt(index)=='\n') {
				table[i][j].append(tabletxt.substring(k,index));
				k = index+1;
				index =k;
				i++;
				j = 0;
			}
		}
		for( i=0;i<table.length;i++) { table[i][9].deleteCharAt(3); }        //#3	
	}
	
	public static void sortArray(int[] arr) {		
		for (int i=0;i<arr.length-1;i++) {
			int min = arr[i];
			for (int j=i+1;j<arr.length;j++) {
				if (arr[j]<min) {			
					arr[i] = arr[j];
					arr[j] = min;
					min = arr[i];			
				}
			}	
		}
	}
	
	public static int numberOfRadiograms(StringBuilder[][] time,int day, int begin, int end) {
		int numberOfRadiograms = 0;
		for (int i=0;i<time.length;i++) {
			if (!time[i][day-1].toString().equals("-")) {
				int timeNumber = Integer.parseInt(time[i][day-1].toString());
				if (timeNumber>begin && timeNumber<end) { numberOfRadiograms++; }
			}			
		}		
		return numberOfRadiograms;
	}
	
	public static String determineMonth(int num) {
		StringBuilder monthFile = loadFromFile(new File("src\\Місяці.txt"));
		StringBuilder month = new StringBuilder();
		for (int i=1,count=-1;i<monthFile.length();i++) {
			if(monthFile.charAt(i)=='/') { count++; }
			if(count==num) {
				int firstIndex = i-1;
				while(monthFile.charAt(firstIndex)!='/') { firstIndex--; }
				month.append(monthFile.substring(firstIndex+1,i));
				break;
			}		
		}
		return month.toString();
	}
	
	public static void fillTime (StringBuilder[][] time, StringBuilder[] calls, 
			int DAYS_IN_MOUNTH) throws Exception {
		for (int i=0;i<time.length;i++) {
			for (int j=0;j<time[i].length;j++) { time[i][j] = new StringBuilder("");} 
		}
		StringBuilder grafic = loadGrafic();
		for (int i=0;i<calls.length;i++) { 
			int index = grafic.indexOf(calls[i].toString());
			int beginRow = index+calls[i].length();	
			int lastIndexInRow = beginRow;
			while(grafic.charAt(lastIndexInRow)!='\n') { lastIndexInRow++; }
			grafic.insert(lastIndexInRow,'\t');	
			for (int j=beginRow, k=0;grafic.charAt(j)!='\n';j++) {
				if (grafic.charAt(j)=='\t' && k<DAYS_IN_MOUNTH) {
					int lastIndex = j+1;
					while(grafic.charAt(lastIndex)!='\t') { lastIndex++; }
					time[i][k].append(grafic.substring(j+1,lastIndex));
					j = lastIndex-1;
					k++;
				}
			}
		}
		for (int i=0;i<time.length;i++) {
	    	for(int j=0;j<time[i].length;j++) {
	    		if (time[i][j].toString().equals("")) { time[i][j].append("-"); } }	
	    } 	
	}
	
	public static int determineTheNumberOfCall() {
		int number = 0;
		StringBuilder callstxt = loadFromFile (new File("src\\Позивні.txt"));
		for (int i=0;i<callstxt.length();i++) {
			if (callstxt.charAt(i)=='\n') { number++; }
		}
		return number;	
	}
	
	public static void initializationCalls (StringBuilder[] calls) {
		for (int i=0;i<calls.length;i++) { calls[i] = new StringBuilder(); }
		StringBuilder callstxt = loadFromFile (new File("src\\Позивні.txt"));
		for (int j=0;j<calls.length;j++) {
			for (int i=0;i<callstxt.length();i++) {
				if (callstxt.charAt(i)=='\n') {
					calls[j].append(callstxt.substring(0,i-1));
					callstxt.delete(0,i+1);
					break;
				}
			}
		}
	}
	
	public static StringBuilder loadGrafic ()throws Exception {
		StringBuilder grafic = new StringBuilder();
		XWPFDocument graficDoc = new XWPFDocument(new FileInputStream("Графік маскірок.docx"));
		XWPFWordExtractor extractDoc = new XWPFWordExtractor(graficDoc);	
		grafic.append(extractDoc.getText());
		extractDoc.close();	
		return grafic;
	}
	
	public static void processingText(StringBuilder txt) {
		for (int i=0;i<txt.length();i++) {
			if (txt.charAt(i)=='=') {
				txt.delete(0,i);
				break;
			}
		}
	}

	public static int findDate(StringBuilder txt) {
		int date = 0;
		String nums = "0123456789";
		StringBuilder numberDate = new StringBuilder();		
		for (int i=0;i<txt.length() && txt.charAt(i)!='\t';i++) {
				for(int j=0;j<nums.length();j++) {
					if (txt.charAt(i)==nums.charAt(j)) {
						numberDate.append(txt.charAt(i));
					}
				}		
		}
		date = Integer.parseInt(numberDate.toString());
		return date;
	}
	
	public static void loadFromExcel(StringBuilder ex, File excelFile)throws Exception {
		
		XSSFRow row;
		FileInputStream fis = new FileInputStream(excelFile);      
	    XSSFWorkbook workbook = new XSSFWorkbook(fis);
	    XSSFSheet spreadsheet = workbook.getSheetAt(0);
	    Iterator < Row >  rowIterator = spreadsheet.iterator();
	    while (rowIterator.hasNext()) {
	         row = (XSSFRow) rowIterator.next();
	         Iterator < Cell >  cellIterator = row.cellIterator();
	         for (int i=0;i<3 && cellIterator.hasNext();i++) {
	        	 Cell cell = cellIterator.next();    	
		         ex.append(cell.toString()+"\t");
	         }
	         ex.append(System.lineSeparator());
	    }
	    fis.close();
	    workbook.close();
	}
	
	public static void writeToWord(String month, StringBuilder date,
			StringBuilder radiograms)throws Exception {				
		XWPFDocument doc = new XWPFDocument();
		FileOutputStream out = new FileOutputStream (new File (
				"Радіограми "+month+"\\Розрахунок на "+date.toString()+".docx"));
		doc.setMirrorMargins(true);
		CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
		CTPageMar pageMar = sectPr.addNewPgMar();
		pageMar.setLeft(BigInteger.valueOf(455));
		pageMar.setTop(BigInteger.valueOf(285));
		pageMar.setRight(BigInteger.valueOf(455));
		pageMar.setBottom(BigInteger.valueOf(285));	
		int count = 0;
		for (int i=0;i<radiograms.length();i++) {
			if (radiograms.charAt(i)=='\n') {
				XWPFParagraph paragraph = doc.createParagraph();
				XWPFRun run = paragraph.createRun();
				run.setText(radiograms.substring(count,i));
				run.setBold(true);
				run.setFontSize(16);
				run.setFontFamily("Courier New");		
				count = i+1;
			}
		}
		doc.write(out);
		out.close();		
		doc.close();
	}
	
	public static StringBuilder createDate(Calendar cl, int day) {
		StringBuilder title = new StringBuilder();
		cl.set(Calendar.DAY_OF_MONTH, day);
		Date d = cl.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.y");
		title.append(sdf.format(d));
		return title;
	}
	
	public static int determineTheNumberOfRadiograms(StringBuilder txt, String alphabet) {		
		int number  =0;
		char[] alpha = alphabet.toCharArray();		
		for (int i=0;i<txt.length();i++) {
			for (int j=0;j<alpha.length;j++) {
				if (txt.charAt(i)==alpha[j]) {
					int k=i;
					while(txt.charAt(k)!='\t') { k++; }
					i = k;
					number++;
					}
				}
		}
		return number;
		
	}

	public static void createRadiograms (int numberOf, StringBuilder text, String alphabet,
			StringBuilder excelText, int today,StringBuilder[] feedbackCheck) {	
		
		StringBuilder[] heading = new StringBuilder[numberOf];
		for (int i=0;i<heading.length;i++) { heading[i] = new StringBuilder(); }
		StringBuilder feedback = new StringBuilder("ЗВОРОТНЯ ПЕРЕВІРКА");
		for (int i=0;i<41;i++) { feedback.insert(0," "); }			
		StringBuilder[] radiogramsWithoutHeading = new StringBuilder[numberOf];
		for (int i=0;i<radiogramsWithoutHeading.length;i++) { 
			radiogramsWithoutHeading[i] = new StringBuilder(); 
		}	
		int[] numberOfGroups = new int[numberOf];
		for (int i=0;i<numberOf;i++) { numberOfGroups[i] = (int)(Math.random()*15+51); }
		Boolean type = false;
		int numberDigitalRadiogram = (int)(Math.random()*numberOf);
		for (int i=0;i<numberOf;i++) {
			if (i==numberDigitalRadiogram) {
				type = true;
				createOneRadiogram(radiogramsWithoutHeading[i], numberOfGroups[i], 
						alphabet, type, today);
				type = false;
			}
			else { createOneRadiogram(radiogramsWithoutHeading[i], numberOfGroups[i], 
					alphabet, type, today); }
		}
		createHeading(heading, numberOfGroups, alphabet, today, excelText,feedbackCheck);		
		for (int i=0;i<numberOf;i++) {			
			if (heading[i].charAt(0)=='*') {
				heading[i].delete(0,1); 
				for (int j=0;j<radiogramsWithoutHeading[i].length();j++) {
					if (radiogramsWithoutHeading[i].charAt(j)=='=') {
						radiogramsWithoutHeading[i].insert(j, 
								feedback+System.lineSeparator());
						break;
					}
				}	
			}		
			text.append(heading[i]).append(System.lineSeparator())
			.append(radiogramsWithoutHeading[i]);		
			text.append(System.lineSeparator());
		}
	}
	
	public static void createGroups (int numberOfGroups, StringBuilder row, String alphabet, 
			Boolean type) {
		final int NUMBER_OF_LETTER = 5;
		boolean beginRow = true;
		for (int j=1;j<=numberOfGroups;j++) {
			
			if (beginRow) { beginRow =false;  }
			else {  row.append(" "); }
			
			for (int k=0;k<NUMBER_OF_LETTER;k++) {				
				if (type) {
					row.append(Integer.toString((int)(Math.random()*10)));
					}
				else {
					int beginIndex = (int)(Math.random()*alphabet.length()-1);
					int lastIndex  = beginIndex+1;
					row.append(alphabet.substring(beginIndex, lastIndex));
					}	 	
			}
			if (j==9 || j%10==9) { 
				row.append(System.lineSeparator()); 
				beginRow = true;
			}
		}
	}
	
	public static void createOneRadiogram (StringBuilder radiogram,int numberOfGroups, 
			String alphabet, Boolean type, int today) {
		
		StringBuilder begin = new StringBuilder();
		StringBuilder end = new StringBuilder();		
		if (type) { 
			begin.append("11111 "); 
			if (today<10) { end.append("0"+Integer.toString(today)+"0"+numberOfGroups);}
			else { end.append(Integer.toString(today)+"0"+numberOfGroups); }
		}
		else { 		
			begin.append("AAAAA ");
			String firstLetter = alphabet.substring(numberOfGroups/10,numberOfGroups/10+1);
			String secondLetter = alphabet.substring(numberOfGroups%10,numberOfGroups%10+1);
			if (today<10) {		
				end.append("К"+alphabet.substring(today,today+1)+"К"+firstLetter+secondLetter);
			}
			else {
				String firstNumber = alphabet.substring(today/10,today/10+1);
				String secondNumber = alphabet.substring(today%10, today%10+1);
				end.append(firstNumber+secondNumber+"К"+firstLetter+secondLetter);
			}
		}
		radiogram.append(begin);
		createGroups(numberOfGroups-1, radiogram, alphabet, type);
		if (radiogram.charAt(radiogram.length()-1)=='\n') {
			radiogram.append(end+System.lineSeparator());
		}
		else { radiogram.append(" "+end+System.lineSeparator()); }		
		for (int i=0;i<59;i++) { radiogram.append("="); }
	}

	public static void createHeading(StringBuilder[] heading, int[] numberOfGroups,
			String alphabet, int today, StringBuilder txt,StringBuilder[] feedbackCheck) {	
		
		StringBuilder address = new StringBuilder();
		StringBuilder radioNumber = new StringBuilder();
		StringBuilder time = new StringBuilder();
		StringBuilder call =new StringBuilder();
		char[] alpha = alphabet.toCharArray();
		int index = 0;
		Boolean beforeLunch = false;
		Boolean feedback = false;
		for (int i=0;i<txt.length();i++) {
			if (txt.charAt(i)=='=') {
				if (beforeLunch) { address.delete(0,address.length()); }
				address.append(txt.substring(i,i+5));
				i = i+5;
				beforeLunch = true;
			}
			for (int j=0;j<alpha.length;j++) {
				if (txt.charAt(i)==alpha[j]) {				
					int k=i;
					while(txt.charAt(k)!='\t') { k++; }
					call.append(txt.substring(i,k));					
					if (txt.substring(i,k).equals(feedbackCheck[today-1].toString())) {
						feedback = true;
					}	
					i=k;
					radioNumber.append(txt.substring(i+1,i+4));
					time.append(txt.substring(i+5,i+9));
					i = i+9;
					if (feedback) { 
						heading[index].append("*"); 
						feedback = false;
					}
					int numberOfSpace = calcNumberOfSpace(call, today);
					heading[index].append("4 р/м "+call);
					for (int space=0;space<numberOfSpace;space++) { 
						heading[index].append(" "); 
					}
					heading[index].append(radioNumber+" "+(numberOfGroups[index]+2)+" "+
							today+" "+time+" "+radioNumber+" "+address);
					index++;				
					call.delete(0,call.length());
					radioNumber.delete(0,radioNumber.length());
					time.delete(0,time.length());
				}
			}	
		}
	}
	
	public static StringBuilder loadFromFile(File file) {
		StringBuilder data = new StringBuilder();
		try (BufferedReader load = new BufferedReader (new FileReader (file))) {
			String text = "";
			while ((text = load.readLine())!=null) {
				data.append(text).append(System.lineSeparator());
			}
		} catch (IOException e ) {
			System.out.println(e);
		}
		return data;
	}
	
	public static int calcNumberOfSpace(StringBuilder call, int today) {
		int numberOfSpace = 0;
		final int TOTAL_NUMBER_OF_SYMBOL_IN_ROW = 59;
		final int CONSTANT_NUMBER_OF_SYMBOL_IN_ROW = 28;
		String data = Integer.toString(today);
		numberOfSpace = TOTAL_NUMBER_OF_SYMBOL_IN_ROW - 
				CONSTANT_NUMBER_OF_SYMBOL_IN_ROW - call.length() - data.length();
		return numberOfSpace;	
	}

//	public static void writeToFile(StringBuilder text) {	
//		StringBuilder date = new StringBuilder();
//		Date d = new Date();
//		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.y");
//		date.append(sdf.format(d));
//		try (PrintWriter write = new PrintWriter ("radiograms("+date.toString()+").txt") )
//		{
//			write.print(text);
//		} catch (FileNotFoundException e) {
//			System.out.println("File not found.");
//		}	
//	}	
}
/* №1  последний символ "пробел", чтобы можно было использовать "Я"
и не было вылета за пределы массива, так как в substring
нужно указывать начальный индекс (соответствует индексу буквы) и
конечный индекс (следующий индекс за буквой) 
   #2  - вкладыш А - это первые 50 строк, вкладыш В - это следующие 50  
   #3  - удаляем \n */