package util.output;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.UnderlineStyle;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


public class ExcelWriter implements ITableWriter{

        private WritableCellFormat timesColumnHeader;
        private WritableCellFormat timesRowHeader;
        private WritableCellFormat times;
        
        private String inputFile;
        private WritableWorkbook workbook;
        private WritableSheet activeSheet;
        
        public ExcelWriter(String outputFilePath){
        	this.inputFile = outputFilePath;

            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("de", "DE"));
        	
        	File file = new File(inputFile); 
        	try{
	            workbook = Workbook.createWorkbook(file, wbSettings);
	                                 
	            times = new WritableCellFormat(new WritableFont(WritableFont.TIMES, 12));
	            times.setWrap(true);
	            times.setAlignment(Alignment.CENTRE);
	            times.setBorder(Border.LEFT, BorderLineStyle.MEDIUM);
	            times.setBorder(Border.RIGHT, BorderLineStyle.MEDIUM);
	            times.setBorder(Border.BOTTOM, BorderLineStyle.HAIR);
	            
	            timesColumnHeader = new WritableCellFormat(new WritableFont(WritableFont.TIMES, 13, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE));
	            timesColumnHeader.setWrap(true);
	            timesColumnHeader.setAlignment(Alignment.CENTRE);
	            
	            timesRowHeader = timesColumnHeader;
	            timesRowHeader.setBorder(Border.RIGHT, BorderLineStyle.MEDIUM);            
	            timesColumnHeader.setBorder(Border.BOTTOM, BorderLineStyle.MEDIUM);
	            timesColumnHeader.setBorder(Border.LEFT, BorderLineStyle.MEDIUM);
	            timesColumnHeader.setBorder(Border.RIGHT, BorderLineStyle.MEDIUM);
        	}catch (IOException e){
        		e.printStackTrace();
        	}catch (WriteException e){
        		e.printStackTrace();
        	}
        }
        
        public void write(String[][] output){          
                
        		if(activeSheet == null)
        			activeSheet = workbook.createSheet("default_name", workbook.getNumberOfSheets());

                /* Creates output */                
                for(int i=0; i<output.length; i++){
                	for(int j=0; j<output[0].length; j++){
                		WritableCellFormat font = times;
                		if(i==0) font = timesColumnHeader;
                		if(j==0) font = timesRowHeader;
                		
                		CellView c = activeSheet.getColumnView(j);
                		c.setAutosize(true);
                		activeSheet.setColumnView(j, c);
                		
                        Label label = new Label(i, j, output[i][j], font);                        
                        try {
							activeSheet.addCell(label);
						} catch (WriteException e) {
							e.printStackTrace();
						}
                	}
                }
                try {
					workbook.write();
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
        
        public void close(){
        	try {
				workbook.close();
			} catch (WriteException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        public void createSheet(String sheetName){
        	int lastIndex = workbook.getNumberOfSheets();
        	activeSheet = workbook.createSheet(sheetName, lastIndex);
        }
}