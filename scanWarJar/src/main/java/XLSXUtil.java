package main.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSXUtil {
	
	private static int[] widths = {300, 70, 100};

	/**
	 * 
	 * @param path
	 * @param list
	 */
	public static void writeToExcel(String path, Map<String, List<JarVersion>> map) {
		 //读取excel模板,创建excel对象
		 Workbook wb = null;
		 File file = new File(path);
		
		try {
			 FileInputStream inputStream = new FileInputStream(file);
			if (path.contains(".xlsx")) {
				wb = new XSSFWorkbook(inputStream);
			} else {
				wb = new HSSFWorkbook(inputStream);
			}
			
			if (inputStream != null) {
				inputStream.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    //删除已存在的信息
	    deleteExsitSofaVersion(map, wb);
	    
	    //信息保存到excel表格中
	    writeToExcel(wb, map);
	    
	    //保存到表格
	    saveExcel(wb, file);
	    
	    if (wb != null) {
	    	try {
				wb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	    
	}
	
	public static void saveExcel(Workbook wb, File file) {
		try {
			FileOutputStream stream = new FileOutputStream(file);
			if (stream != null) {
				wb.write(stream);
				
				//设置表格的宽度
				Sheet sheet = wb.getSheetAt(0);
				for (int i = 0; i < widths.length; i++) {
					sheet.setColumnWidth(i, widths[i]);
				}
				
				stream.flush();
				stream.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 删除已存在的sofa版本
	 * @param map 需要保存的map信息
	 * @param sheet 表格信息
	 */
	public static void deleteExsitSofaVersion(Map<String, List<JarVersion>> map, Workbook wb) {
		Sheet sheet = wb.getSheetAt(0);
		int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
		List<String> exists = new ArrayList<String>();
		for (int i = 0; i < physicalNumberOfRows; i++) {
			Row row = sheet.getRow(i);
			if (row != null) {
				Cell cell = row.getCell(0);
				if (cell != null) {
					String sofaVersion = cell.getStringCellValue();
					if (!StringUtils.isEmpty(sofaVersion)) {
						Set<String> keys = map.keySet();
						for (String key : keys) {
							if (sofaVersion.equals(key)) {
								exists.add(key);
								break;
							}
						}
					}
				}
			}
		}
		
		for (String key : exists) {
			map.remove(key);
		}
	}


	/**
	 * 将信息写进Excel 中
	 * @param sheet 表格
	 * @param map 信息
	 */
	public static void writeToExcel(Workbook wb, Map<String, List<JarVersion>> map) {
		Sheet sheet = wb.getSheetAt(0);
		int rows = sheet.getPhysicalNumberOfRows();
		
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
        font.setBold(true);//粗体显示style.setFont(font);
        style.setFont(font);
		
		int i = 0;
		Set<String> keySet = map.keySet();
		for (String key : keySet) {
			//设置sofa版本
			i++;
			
			
			Row row = sheet.createRow(rows + i);
			Cell cell = row.createCell(0, CellType.STRING);
			cell.setCellValue(key);
			cell.setCellStyle(style);
			CellRangeAddress region = new CellRangeAddress(rows + i, rows + i, 0, 2);
	        sheet.addMergedRegion(region);

			List<JarVersion> list = map.get(key);
			
			//设置jar版本信息
			for (JarVersion jarVersion : list) {
				i++;
				Row jarRow = sheet.createRow(rows + i);
				
				//设置jar信息
				Cell jarNameCell = jarRow.createCell(0, CellType.STRING);
				jarNameCell.setCellValue(jarVersion.getJarName());
				
				Cell jarBundleCell = jarRow.createCell(1, CellType.STRING);
				jarBundleCell.setCellValue(jarVersion.getJarVersion());
				
				Cell jarBuildCell = jarRow.createCell(2, CellType.STRING);
				jarBuildCell.setCellValue(jarVersion.getJarBuildTime());
			}
		}
	}
}
